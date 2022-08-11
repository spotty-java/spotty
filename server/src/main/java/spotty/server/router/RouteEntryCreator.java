package spotty.server.router;

import com.google.common.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteEntry;
import spotty.common.utils.RouterUtils.Result;

import static spotty.common.utils.RouterUtils.compileMatcher;
import static spotty.common.utils.RouterUtils.normalizePath;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;

@VisibleForTesting
final class RouteEntryCreator {

    static RouteEntry create(String pathTemplate, HttpMethod httpMethod, String acceptType, Route route) {
        notNull("pathTemplate", pathTemplate);
        notNull("httpMethod", httpMethod);
        notBlank("acceptType", acceptType);
        notNull("route", route);

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

}
