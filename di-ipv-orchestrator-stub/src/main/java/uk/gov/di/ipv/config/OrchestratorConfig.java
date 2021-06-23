package uk.gov.di.ipv.config;

public class OrchestratorConfig {
    public static final String PORT = getConfigValue("ORCHESTRATOR_PORT","8081");
    public static final String IPV_CLIENT_ID = getConfigValue("IPV_CLIENT_ID", "some_client_id");
    public static final String IPV_ENDPOINT = getConfigValue("IPV_ENDPOINT", "http://di-ipv-frontend/");
    public static final String IPV_USERINFO_ENDPOINT = getConfigValue("IPV_USERINFO_ENDPOINT", "http://di-ipv-frontend.di/");
    public static final String ORCHESTRATOR_REDIRECT_URL = getConfigValue("ORCHESTRATOR_REDIRECT_URL", "http://localhost:8081/orchestrator/callback");

    private static String getConfigValue(String key, String defaultValue){
        var envValue = System.getenv(key);
        if(envValue == null){
            return defaultValue;
        }

        return envValue;
    }
}
