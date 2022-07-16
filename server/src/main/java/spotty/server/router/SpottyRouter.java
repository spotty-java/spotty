package spotty.server.router;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyHttpException;
import spotty.common.http.HttpMethod;
import spotty.server.router.route.Route;
import spotty.server.router.route.RouteEntry;
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
    private final Routable routable = new Routable();

    public void path(String path, RouteGroup group) {
        pathPrefixStack.addLast(path);
        group.addRoutes();
        pathPrefixStack.removeLast();
    }

    public void get(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), GET, route);
    }

    public void post(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), POST, route);
    }

    public void put(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), PUT, route);
    }

    public void patch(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), PATCH, route);
    }

    public void delete(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), DELETE, route);
    }

    public void head(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), HEAD, route);
    }

    public void trace(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), TRACE, route);
    }

    public void connect(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), CONNECT, route);
    }

    public void options(String path, Route route) {
        routable.addRoute(pathWithPrefix(path), OPTIONS, route);
    }

    public void get(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), GET, acceptType, route);
    }

    public void post(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), POST, acceptType, route);
    }

    public void put(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), PUT, acceptType, route);
    }

    public void patch(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), PATCH, acceptType, route);
    }

    public void delete(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), DELETE, acceptType, route);
    }

    public void head(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), HEAD, acceptType, route);
    }

    public void trace(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), TRACE, acceptType, route);
    }

    public void connect(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), CONNECT, acceptType, route);
    }

    public void options(String path, String acceptType, Route route) {
        routable.addRoute(pathWithPrefix(path), OPTIONS, acceptType, route);
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

    public boolean removeRoute(String path) {
        return routable.removeRoute(path);
    }

    public boolean removeRoute(String path, HttpMethod method) {
        return routable.removeRoute(path, method);
    }

    public boolean removeRoute(String path, String acceptType, HttpMethod method) {
        return routable.removeRoute(path, acceptType, method);
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
