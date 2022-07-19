package spotty.server.router;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyHttpException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;
import spotty.server.router.route.RouteGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

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
    private final Deque<String> pathPrefixStack = new LinkedList<>();
    private final Routable routable = new Routable();

    private final List<FilterHolder> beforeFilters = new ArrayList<>();
    private final List<FilterHolder> afterFilters = new ArrayList<>();

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
        final List<Filter> filterList = toList(filter, filters);
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;
        addFiltersToRoute(RouteEntry::addBeforeFilters, matcher, filterList);

        beforeFilters.add(new FilterHolder(filterList, matcher));
    }

    public void after(String pathTemplate, Filter filter, Filter... filters) {
        final List<Filter> filterList = toList(filter, filters);
        final Pattern matcher = compileMatcher(pathWithPrefix(pathTemplate)).matcher;
        addFiltersToRoute(RouteEntry::addAfterFilters, matcher, filterList);

        afterFilters.add(new FilterHolder(filterList, matcher));
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

    public RouteEntry getRoute(String rawPath, String acceptType, HttpMethod method) throws SpottyHttpException {
        return routable.getRoute(rawPath, acceptType, method);
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
        return routable.removeRoute(pathTemplate, acceptType, method);
    }

    // register filters after route added,
    private void registerAllMatchedFilters() {
        beforeFilters.forEach(holder -> {
            addFiltersToRoute(RouteEntry::addBeforeFilters, holder.matcher, holder.filters);
        });

        afterFilters.forEach(holder -> {
            addFiltersToRoute(RouteEntry::addAfterFilters, holder.matcher, holder.filters);
        });
    }

    private void addFiltersToRoute(BiConsumer<RouteEntry, List<Filter>> adder, Pattern matcher, List<Filter> filters) {
        routable.sortedList.forEachRoute(
            route -> matcher.matcher(route.pathNormalized()).matches(),
            route -> adder.accept(route, filters)
        );
    }

    private static List<Filter> toList(Filter filter, Filter... filters) {
        final ArrayList<Filter> filterList = new ArrayList<>();
        filterList.add(filter);
        filterList.addAll(Arrays.asList(filters));

        return filterList;
    }

    @NotNull
    @VisibleForTesting
    String pathWithPrefix(String pathTemplate) {
        if (pathPrefixStack.isEmpty()) {
            return pathTemplate;
        }

        return String.join("", pathPrefixStack) + pathTemplate;
    }

    @Value
    private static class FilterHolder {
        public List<Filter> filters;
        public Pattern matcher;
    }

}
