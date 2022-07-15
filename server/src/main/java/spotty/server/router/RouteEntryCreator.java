package spotty.server.router;

import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.ParamName;
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

    public static final String PARAM_REPLACEMENT = "(?<name>\\w+?)";
    public static final String ALL_REPLACEMENT = "(.+?)";

    static RouteEntry create(String path, HttpMethod httpMethod, Route route) {
        notNull(path, "path");
        notNull(httpMethod, "httpMethod");
        notNull(route, "route");

        final Matcher m = PATTERN.matcher(path);

        String pathNormalized = path;
        String matcher = "^" + path.replace("*", ALL_REPLACEMENT) + "$";
        final ArrayList<ParamName> params = new ArrayList<>();
        while (m.find()) {
            final String name = m.group(1);
            final ParamName paramName = new ParamName(name);
            params.add(paramName);

            matcher = matcher.replace(name, PARAM_REPLACEMENT.replace("name", paramName.groupName));
            pathNormalized = pathNormalized.replace(name, "*");
        }

        return RouteEntry.builder()
            .path(path)
            .httpMethod(httpMethod)
            .pathNormalized(pathNormalized)
            .matcher(Pattern.compile(matcher))
            .pathParamKeys(params)
            .route(route)
            .build();
    }

}
