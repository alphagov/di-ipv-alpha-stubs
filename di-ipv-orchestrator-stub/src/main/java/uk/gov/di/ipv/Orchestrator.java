package uk.gov.di.ipv;

import uk.gov.di.ipv.handlers.IpvHandler;

import static spark.Spark.*;
import static uk.gov.di.ipv.config.OrchestratorConfig.PORT;
import static uk.gov.di.ipv.handlers.HomeHandler.serveHomePage;

public class Orchestrator {

    private final IpvHandler ipvHandler;

    public Orchestrator(){
        staticFileLocation("/public");
        port(Integer.parseInt(PORT));

        ipvHandler = new IpvHandler();
        initRoutes();
    }

    public void initRoutes(){
        get("/", serveHomePage);

        path("/orchestrator", () -> {
            get("/authorize", ipvHandler.doAuthorize);
            get("/callback", ipvHandler.doCallback);
        });

        internalServerError("<html><body><h1>Oops something went wrong</h1></body></html>");
    }
}
