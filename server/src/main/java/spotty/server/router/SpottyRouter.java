package spotty.server.router;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteGroup;

import java.util.Deque;
import java.util.LinkedList;

import static spotty.common.http.HttpMethod.CONNECT;
import static spotty.common.http.HttpMethod.DELETE;
import static spotty.common.http.HttpMethod.GET;
import static spotty.common.http.HttpMethod.HEAD;
import static spotty.common.http.HttpMethod.OPTIONS;
import static spotty.common.http.HttpMethod.PATCH;
import static spotty.common.http.HttpMethod.POST;
import static spotty.common.http.HttpMethod.PUT;
import static spotty.common.http.HttpMethod.TRACE;

public final class SpottyRouter {

    private final Deque<String> pathPrefixStack = new LinkedList<>();
    private final RouteContainer routeContainer;

    public SpottyRouter(RouteContainer routeContainer) {
        this.routeContainer = routeContainer;
    }

    public void path(String path, RouteGroup group) {
        pathPrefixStack.addLast(path);
        group.addRoutes();
        pathPrefixStack.removeLast();
    }

    public void get(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), GET, route);
    }

    public void post(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), POST, route);
    }

    public void put(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), PUT, route);
    }

    public void patch(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), PATCH, route);
    }

    public void delete(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), DELETE, route);
    }

    public void head(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), HEAD, route);
    }

    public void trace(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), TRACE, route);
    }

    public void connect(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), CONNECT, route);
    }

    public void options(String path, Route route) {
        routeContainer.addRoute(pathWithPrefix(path), OPTIONS, route);
    }

    @NotNull
    private String pathWithPrefix(String path) {
        return getPathPrefix() + path;
    }

    @NotNull
    @VisibleForTesting
    String getPathPrefix() {
        if (pathPrefixStack.isEmpty()) {
            return "";
        }

        return String.join("", pathPrefixStack);
    }

}
