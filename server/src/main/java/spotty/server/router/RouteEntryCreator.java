package spotty.server.router;

import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Validate.notNull;

@VisibleForTesting
final class RouteEntryCreator {
    private static final String REGEX = "(:\\w+?)(/|$)";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    static final String PARAM_REPLACEMENT = "(\\w+?)";
    static final String ALL_REPLACEMENT = "(.+?)";

    static RouteEntry create(String rawPath, HttpMethod httpMethod, Route route) {
        notNull(rawPath, "rawPath");
        notNull(httpMethod, "httpMethod");
        notNull(route, "route");

        final Matcher m = PATTERN.matcher(rawPath);

        String pathNormalized = rawPath;
        String matcher = "^" + rawPath.replace("*", ALL_REPLACEMENT) + "$";
        final ArrayList<String> params = new ArrayList<>();
        while (m.find()) {
            final String name = m.group(1);
            params.add(name);

            matcher = matcher.replace(name, PARAM_REPLACEMENT);
            pathNormalized = pathNormalized.replace(name, "*");
        }

        return RouteEntry.builder()
            .path(rawPath)
            .httpMethod(httpMethod)
            .pathNormalized(pathNormalized)
            .matcher(Pattern.compile(matcher))
            .params(params)
            .route(route)
            .build();
    }

}
