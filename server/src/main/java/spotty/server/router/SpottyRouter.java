package spotty.server.router;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyHttpException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;
import spotty.server.router.route.RouteGroup;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.asList;
import static spotty.common.http.HttpMethod.CONNECT;
import static spotty.common.http.HttpMethod.DELETE;
import static spotty.common.http.HttpMethod.GET;
import static spotty.common.http.HttpMethod.HEAD;
import static spotty.common.http.HttpMethod.OPTIONS;
import static spotty.common.http.HttpMethod.PATCH;
import static spotty.common.http.HttpMethod.POST;
import static spotty.common.http.HttpMethod.PUT;
import static spotty.common.http.HttpMethod.TRACE;
import static spotty.server.router.RouteEntryCreator.compileMatcher;

public final class SpottyRouter {
    public static final String DEFAULT_ACCEPT_TYPE = "*/*";

    private final Deque<String> pathPrefixStack = new LinkedList<>();
    private final Routable routable = new Routable();

    private final List<FilterContainer> beforeFilters = new ArrayList<>();
    private final List<FilterContainer> afterFilters = new ArrayList<>();

    public void path(String pathTemplate, RouteGroup group) {
        pathPrefixStack.addLast(pathTemplate);
        group.addRoutes();
        pathPrefixStack.removeLast();
    }

    public void before(Filter filter, Filter... filters) {
        before("*", filter, filters);
    }

    public void after(Filter filter, Filter... filters) {
        after("*", filter, filters);
    }

    public void before(String pathTemplate, Filter filter, Filter... filters) {
        before(pathTemplate, null, filter, filters);
    }

    public void after(String pathTemplate, Filter filter, Filter... filters) {
        after(pathTemplate, null, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        before(pathTemplate, method, null, filter, filters);
    }

    public void after(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        after(pathTemplate, method, null, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        final List<Filter> filterList = asList(filter, filters);
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;

        addFiltersToRoute(
            matcher,
            method,
            acceptType,
            filterList,
            RouteEntry::addBeforeFilters
        );

        beforeFilters.add(new FilterContainer(matcher, method, acceptType, filterList));
    }

    public void after(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        final List<Filter> filterList = asList(filter, filters);
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;

        addFiltersToRoute(
            matcher,
            method,
            acceptType,
            filterList,
            RouteEntry::addAfterFilters
        );

        afterFilters.add(new FilterContainer(matcher, method, acceptType, filterList));
    }

    public void get(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), GET, route);
        registerAllMatchedFilters();
    }

    public void post(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), POST, route);
        registerAllMatchedFilters();
    }

    public void put(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PUT, route);
        registerAllMatchedFilters();
    }

    public void patch(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PATCH, route);
        registerAllMatchedFilters();
    }

    public void delete(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), DELETE, route);
        registerAllMatchedFilters();
    }

    public void head(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), HEAD, route);
        registerAllMatchedFilters();
    }

    public void trace(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), TRACE, route);
        registerAllMatchedFilters();
    }

    public void connect(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), CONNECT, route);
        registerAllMatchedFilters();
    }

    public void options(String pathTemplate, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), OPTIONS, route);
        registerAllMatchedFilters();
    }

    public void get(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), GET, acceptType, route);
        registerAllMatchedFilters();
    }

    public void post(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), POST, acceptType, route);
        registerAllMatchedFilters();
    }

    public void put(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PUT, acceptType, route);
        registerAllMatchedFilters();
    }

    public void patch(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), PATCH, acceptType, route);
        registerAllMatchedFilters();
    }

    public void delete(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), DELETE, acceptType, route);
        registerAllMatchedFilters();
    }

    public void head(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), HEAD, acceptType, route);
        registerAllMatchedFilters();
    }

    public void trace(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), TRACE, acceptType, route);
        registerAllMatchedFilters();
    }

    public void connect(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), CONNECT, acceptType, route);
        registerAllMatchedFilters();
    }

    public void options(String pathTemplate, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(pathTemplate), OPTIONS, acceptType, route);
        registerAllMatchedFilters();
    }

    public RouteEntry getRoute(String rawPath, HttpMethod method) throws SpottyHttpException {
        return routable.getRoute(rawPath, method);
    }

    public RouteEntry getRoute(String rawPath, HttpMethod method, String acceptType) throws SpottyHttpException {
        return routable.getRoute(rawPath, method, acceptType);
    }

    public void clearRoutes() {
        routable.clearRoutes();
    }

    public boolean removeRoute(String pathTemplate) {
        return routable.removeRoute(pathTemplate);
    }

    public boolean removeRoute(String pathTemplate, HttpMethod method) {
        return routable.removeRoute(pathTemplate, method);
    }

    public boolean removeRoute(String pathTemplate, String acceptType, HttpMethod method) {
        return routable.removeRoute(pathTemplate, method, acceptType);
    }

    // register filters after route added,
    private void registerAllMatchedFilters() {
        beforeFilters.forEach(container -> {
            addFiltersToRoute(
                container.matcher,
                container.method,
                container.acceptType,
                container.filters,
                RouteEntry::addBeforeFilters
            );
        });

        afterFilters.forEach(container -> {
            addFiltersToRoute(
                container.matcher,
                container.method,
                container.acceptType,
                container.filters,
                RouteEntry::addAfterFilters
            );
        });
    }

    private void addFiltersToRoute(Pattern matcher, HttpMethod method, String acceptType, List<Filter> filters, BiConsumer<RouteEntry, List<Filter>> adder) {
        routable.sortedList.forEachRouteIf(
            route -> {
                if (!matcher.matcher(route.pathNormalized()).matches()) {
                    return false;
                }

                if (method != null && method != route.httpMethod()) {
                    return false;
                }

                if (acceptType == null) {
                    return true;
                }

                return acceptType.equals(route.acceptType());
            },
            route -> adder.accept(route, filters)
        );
    }

    @NotNull
    @VisibleForTesting
    String pathWithPrefix(String pathTemplate) {
        if (pathPrefixStack.isEmpty()) {
            return pathTemplate;
        }

        return String.join("", pathPrefixStack) + pathTemplate;
    }

    private static class FilterContainer {
        final Pattern matcher;
        final HttpMethod method;
        final String acceptType;
        final List<Filter> filters;

        private FilterContainer(Pattern matcher, HttpMethod method, String acceptType, List<Filter> filters) {
            this.matcher = matcher;
            this.method = method;
            this.acceptType = acceptType;
            this.filters = filters;
        }
    }

}
