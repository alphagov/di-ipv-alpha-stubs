package uk.gov.di.ipv.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.ipv.utils.ViewHelper;

public class HomeHandler {
    public static Route serveHomePage = (Request request, Response response) -> ViewHelper.render(null, "home.mustache");
}
