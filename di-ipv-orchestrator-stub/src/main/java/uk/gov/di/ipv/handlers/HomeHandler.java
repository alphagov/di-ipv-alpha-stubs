package uk.gov.di.ipv.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.ipv.utils.ViewHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;

public class HomeHandler {
    public static Route serveHomePage = (Request request, Response response) -> {
        var attributes = new String[] {
            "givenNames", "surname", "address", "phoneNumber", "passportNumber"
        };

        var levelsOfConfidence = new String[] {
          "Low", "Medium", "High", "VeryHigh"
        };

        var modelMap = new HashMap<String, Object>();
        modelMap.put("attributes", attributes);
        modelMap.put("levels-of-confidence", levelsOfConfidence);
        modelMap.put("renderReadable", renderReadable());

        return ViewHelper.render(modelMap, "home.mustache");
    };

    public static Function<String, String> renderReadable() {
        return (obj) -> {
            var elements = obj.split("-");
            var stringBuilder = new StringBuilder();
            Arrays.stream(elements).forEach(element ->
                stringBuilder
                    .append(element.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(element.substring(1))
                    .append(" "));
            return stringBuilder.toString();
        };
    }
}
