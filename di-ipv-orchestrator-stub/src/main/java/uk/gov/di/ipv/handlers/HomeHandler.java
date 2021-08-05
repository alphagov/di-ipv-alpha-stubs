package uk.gov.di.ipv.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.ipv.domain.Attribute;
import uk.gov.di.ipv.domain.ConfidenceLevel;
import uk.gov.di.ipv.utils.ViewHelper;

import java.util.Arrays;
import java.util.HashMap;

public class HomeHandler {
    public static Route serveHomePage = (Request request, Response response) -> {

        var attributes = Arrays.stream(Attribute.values())
            .map(Attribute::getAttributeName)
            .toArray();

        var levelsOfConfidence = Arrays.stream(ConfidenceLevel.values())
            .map(ConfidenceLevel::getName)
            .toArray();

        var modelMap = new HashMap<String, Object>();
        modelMap.put("attributes", attributes);
        modelMap.put("levelsOfConfidence", levelsOfConfidence);

        return ViewHelper.render(modelMap, "home.mustache");
    };
}
