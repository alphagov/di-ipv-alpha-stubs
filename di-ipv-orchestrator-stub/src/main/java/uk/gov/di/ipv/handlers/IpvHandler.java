package uk.gov.di.ipv.handlers;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.ipv.utils.ViewHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static uk.gov.di.ipv.config.OrchestratorConfig.*;

public class IpvHandler {

    private final Logger logger = LoggerFactory.getLogger(IpvHandler.class);
    private final Map<String, Object> stateSession = new HashMap<>();

    public Route doAuthorize = (Request request, Response response) -> {
        var state = new State();
        stateSession.put(state.getValue(), null);

        var attributes = getAttributes(request);

        var authRequest = new AuthorizationRequest.Builder(
            new ResponseType(ResponseType.Value.CODE), new ClientID(IPV_CLIENT_ID))
            .state(state)
            .scope(new Scope("openid"))
            .redirectionURI(new URI(ORCHESTRATOR_REDIRECT_URL))
            .endpointURI(new URI(IPV_ENDPOINT).resolve("/oauth2/authorize"))
            .customParameter("claims", attributes)
            .build();

        response.redirect(authRequest.toURI().toString());
        return null;
    };

    public String getAttributes(Request request) {
        var requestedAttributes = request.queryParamsValues("attribute");
        var levelOfConfidence = request.queryParamsValues("level-of-confidence");

        var json = new JSONObject();
        var attributes = new JSONObject();

        if (requestedAttributes != null) {
            for (String requestedAttribute : requestedAttributes) {
                var essential = new JSONObject();
                essential.appendField("essential", true);

                attributes.appendField(requestedAttribute, essential);
            }
        }

        var levelOfConfidenceRequested = new JSONObject();
        levelOfConfidenceRequested.appendField("essential", true);
        levelOfConfidenceRequested.appendField("value", levelOfConfidence[0]);
        attributes.appendField("levelOfConfidence", levelOfConfidenceRequested);

        json.appendField("userinfo", attributes);

        return json.toJSONString();
    }

    public Route doCallback = (Request request, Response response) -> {
        var authorizationCode = getAuthorizationCode(request);
        var accessToken = exchangeCodeForToken(authorizationCode);

        if (!isTokenValid(accessToken)) {
            throw new RuntimeException("token not valid");
        }

        var userInfo = getUserInfo(accessToken);
        var attributes = userInfo.toJSONObject();

        return ViewHelper.renderSet(attributes.entrySet(), "userinfo.mustache");
    };

    private AuthorizationCode getAuthorizationCode(Request request) throws ParseException {
        // Some basic state checking
        if (!stateSession.containsKey(request.queryParams("state"))) {
            logger.error("Returned state does not match");
            throw new RuntimeException("Returned state does not match the provided one");
        }

        var authorizationResponse = AuthorizationResponse.parse(URI.create("https:///?" + request.queryString()));
        if (!authorizationResponse.indicatesSuccess()) {
            var error = authorizationResponse.toErrorResponse().getErrorObject();
            logger.error("Failed authorization code request: {}", error);
            throw new RuntimeException("Failed authorization code request");
        }

        return authorizationResponse
            .toSuccessResponse()
            .getAuthorizationCode();
    }

    private AccessToken exchangeCodeForToken(AuthorizationCode authorizationCode) {
        TokenRequest tokenRequest = new TokenRequest(
            URI.create(IPV_BACKCHANNEL_ENDPOINT).resolve("/oauth2/token"),
            new ClientID(IPV_CLIENT_ID),
            new AuthorizationCodeGrant(authorizationCode, URI.create(ORCHESTRATOR_REDIRECT_URL))
        );

        var httpTokenResponse = sendHttpRequest(tokenRequest.toHTTPRequest());
        TokenResponse tokenResponse = parseTokenResponse(httpTokenResponse);

        if (tokenResponse instanceof TokenErrorResponse) {
            logger.error("Failed to get token: " + ((TokenErrorResponse) tokenResponse).getErrorObject());
        }

        return tokenResponse
            .toSuccessResponse()
            .getTokens()
            .getAccessToken();
    }

    public boolean isTokenValid(AccessToken accessToken) throws MalformedURLException {
        var keySource = new RemoteJWKSet<>(URI.create(IPV_BACKCHANNEL_ENDPOINT).resolve("/.well-known/jwks.json").toURL());
        var expectedJwsAlgorithm = JWSAlgorithm.RS256;
        var keySelector = new JWSVerificationKeySelector<>(expectedJwsAlgorithm, keySource);
        var jwtProcessor = new DefaultJWTProcessor<>();

        jwtProcessor.setJWSTypeVerifier(
            new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("jwt"))
        );

        jwtProcessor.setJWSKeySelector(keySelector);
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder().issuer("urn:di:ipv:ipv-core").build(),
            new HashSet<>(Arrays.asList("sub", "iat", "exp"))
        ));

        try {
            jwtProcessor.process(accessToken.toString(), null);
        } catch (java.text.ParseException | BadJOSEException | JOSEException e) {
            logger.error("Invalid JWT token received", e);
            return false;
        }

        return true;
    }

    public UserInfo getUserInfo(AccessToken accessToken) {
        var userInfoRequest = new UserInfoRequest(
            URI.create(IPV_BACKCHANNEL_ENDPOINT).resolve("/oauth2/userinfo"),
            (BearerAccessToken) accessToken
        );

        HTTPResponse userInfoHttpResponse = sendHttpRequest(userInfoRequest.toHTTPRequest());
        UserInfoResponse userInfoResponse = parseUserInfoResponse(userInfoHttpResponse);

        if (userInfoResponse instanceof UserInfoErrorResponse) {
            logger.error("Failed to get user info: " + userInfoResponse.toErrorResponse().getErrorObject());
            throw new RuntimeException("Failed to get user info");
        }

        return userInfoResponse
            .toSuccessResponse()
            .getUserInfo();
    }

    private HTTPResponse sendHttpRequest(HTTPRequest httpRequest) {
        try {
            return httpRequest.send();
        } catch (IOException | SerializeException exception) {
            logger.error("Failed to send a http request", exception);
            throw new RuntimeException("Failed to send a http request", exception);
        }
    }

    private TokenResponse parseTokenResponse(HTTPResponse httpResponse) {
        try {
            return OIDCTokenResponseParser.parse(httpResponse);
        } catch (ParseException parseException) {
            throw new RuntimeException("Failed to parse token response", parseException);
        }
    }

    private UserInfoResponse parseUserInfoResponse(HTTPResponse httpResponse) {
        try {
            return UserInfoResponse.parse(httpResponse);
        } catch (ParseException parseException) {
            throw new RuntimeException("Failed to parse user info response", parseException);
        }
    }
}
