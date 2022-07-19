package spotty.server.router;

import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.ParamName;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

@VisibleForTesting
final class RouteEntryCreator {
    private static final String REGEX = "(:\\w+?)(/|$)";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static final String PARAM_REPLACEMENT = "(?<name>[\\w\\*]+?)";
    public static final String ALL_REPLACEMENT = "(.*?)";

    static RouteEntry create(String pathTemplate, HttpMethod httpMethod, String acceptType, Route route) {
        notNull(pathTemplate, "pathTemplate");
        notNull(httpMethod, "httpMethod");
        notBlank(acceptType, "acceptType");
        notNull(route, "route");

        final Result result = compileMatcher(pathTemplate);

        return new RouteEntry()
            .pathTemplate(pathTemplate)
            .acceptType(acceptType)
            .httpMethod(httpMethod)
            .pathNormalized(normalizePath(pathTemplate))
            .matcher(result.matcher)
            .pathParamKeys(result.params)
            .route(route);
    }

    static String normalizePath(String path) {
        return path.replaceAll(REGEX, "*$2");
    }

    static Result compileMatcher(String pathTemplate) {
        final Matcher m = PATTERN.matcher(pathTemplate);

        String matcher = "^" + pathTemplate.replace("*", ALL_REPLACEMENT) + "$";
        final ArrayList<ParamName> params = new ArrayList<>();
        while (m.find()) {
            final String name = m.group(1);
            final ParamName paramName = new ParamName(name);
            params.add(paramName);

            matcher = matcher.replace(name, PARAM_REPLACEMENT.replace("name", paramName.groupName));
        }

        return new Result(params, Pattern.compile(matcher));
    }

    static class Result {
        public final ArrayList<ParamName> params;
        public final Pattern matcher;

        Result(ArrayList<ParamName> params, Pattern matcher) {
            this.params = params;
            this.matcher = matcher;
        }
    }

}
