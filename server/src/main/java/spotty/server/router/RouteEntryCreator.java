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

    public static final String PARAM_REPLACEMENT = "(?<name>[\\w\\*]+?)";
    public static final String ALL_REPLACEMENT = "(.*?)";

    static RouteEntry create(String pathTemplate, HttpMethod httpMethod, Route route) {
        notNull(pathTemplate, "pathTemplate");
        notNull(httpMethod, "httpMethod");
        notNull(route, "route");

        final Value value = compileMatcher(pathTemplate);

        return new RouteEntry()
            .pathTemplate(pathTemplate)
            .httpMethod(httpMethod)
            .pathNormalized(normalizePath(pathTemplate))
            .matcher(value.matcher)
            .pathParamKeys(value.params)
            .route(route);
    }

    static String normalizePath(String path) {
        return path.replaceAll(REGEX, "*$2");
    }

    static Value compileMatcher(String pathTemplate) {
        final Matcher m = PATTERN.matcher(pathTemplate);

        String matcher = "^" + pathTemplate.replace("*", ALL_REPLACEMENT) + "$";
        final ArrayList<ParamName> params = new ArrayList<>();
        while (m.find()) {
            final String name = m.group(1);
            final ParamName paramName = new ParamName(name);
            params.add(paramName);

            matcher = matcher.replace(name, PARAM_REPLACEMENT.replace("name", paramName.groupName));
        }

        return new Value(params, Pattern.compile(matcher));
    }

    @lombok.Value
    static class Value {
        public ArrayList<ParamName> params;
        public Pattern matcher;
    }

}
