/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyHaltException;
import spotty.common.exception.SpottyHttpException;
import spotty.common.exception.SpottyNotFoundException;
import spotty.common.exception.SpottyValidationException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.common.http.HttpStatus;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteGroup;
import spotty.server.Server;
import spotty.server.compress.Compressor;
import spotty.server.files.StaticFilesManager;
import spotty.server.handler.exception.ExceptionHandler;
import spotty.server.handler.request.DefaultRequestHandler;
import spotty.server.registry.exception.ExceptionHandlerRegistry;
import spotty.server.router.SpottyRouter;
import spotty.server.session.SessionManager;
import spotty.server.worker.ReactorWorker;

import java.util.concurrent.TimeUnit;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static spotty.common.http.HttpHeaders.DATE;
import static spotty.common.http.HttpHeaders.SERVER;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static spotty.version.SpottyVersion.VERSION;

/**
 * main Spotty facade is a set routes, filters etc.
 */
public final class Spotty {
    private static final Logger LOG = LoggerFactory.getLogger(Spotty.class);
    private static final String SPOTTY_VERSION = "Spotty v" + VERSION;
    private static final int DEFAULT_PORT = 4000;
    private static final int DEFAULT_MAX_REQUEST_BODY_SIZE = 10 * 1024 * 1024; // 10Mb
    private static final int DEFAULT_REACTOR_WORKERS = 24;

    public static final String PROTOCOL_SUPPORT = "HTTP/1.1";

    private final SessionManager sessionManager;

    private final SpottyRouter router = new SpottyRouter();
    private final StaticFilesManager staticFilesManager = new StaticFilesManager(router);
    private final ExceptionHandlerRegistry exceptionHandlerRegistry = new ExceptionHandlerRegistry();

    private final Server server;

    public Spotty() {
        this(builder());
    }

    public Spotty(int port) {
        this(builder().port(port));
    }

    private Spotty(Builder builder) {
        this.sessionManager = builder.sessionManagerBuilder.build();

        this.server = new Server(
            builder.port,
            builder.maxRequestBodySize,
            new DefaultRequestHandler(router, new Compressor(), sessionManager),
            exceptionHandlerRegistry,
            new ReactorWorker(builder.reactorWorkers)
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public synchronized void start() {
        if (server.isStarted()) {
            LOG.warn("server has been started already");
            return;
        }

        registerSpottyDefaultFilters();
        registerSpottyDefaultExceptionHandlers();

        server.start();
    }

    public void awaitUntilStart() {
        server.awaitUntilStart();
    }

    public void awaitUntilStop() {
        server.awaitUntilStop();
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused).
     *
     * @param keyStorePath       The keystore file location as string
     * @param keyStorePassword   the password for the keystore, leave null if no password
     * @param trustStorePath     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param trustStorePassword the trust store password
     */
    public void enableHttps(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) {
        server.enableHttps(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword);
    }

    /**
     * create session on request
     */
    public void enableSession() {
        sessionManager.enableSession();
    }

    public synchronized void stop() {
        server.close();
        sessionManager.close();
    }

    /**
     * get total server connections count at the current time
     *
     * @return connections count
     */
    public int connections() {
        return server.connections();
    }

    /**
     * get server port
     *
     * @return port
     */
    public int port() {
        return server.port();
    }

    /**
     * get servet host, for example: localhost
     *
     * @return host
     */
    public String host() {
        return server.host();
    }

    /**
     * get server url, for example: http://localhost:4000
     *
     * @return host url
     */
    public String hostUrl() {
        return server.hostUrl();
    }

    /**
     * Add a path-prefix to the routes declared in the routeGroup
     * The path() method adds a path-fragment to a path-stack, adds
     * routes from the routeGroup, then pops the path-fragment again.
     * It's used for separating routes into groups, for example:
     *
     * <pre>
     * {@code
     *     path("/api/user", () -> {
     *          post("/add",   User::add);
     *          put("/change", User::change);
     *     });
     * }
     * </pre>
     *
     * Multiple path() calls can be nested.
     *
     * @param pathTemplate  the path to prefix routes with
     * @param group         group of routes (can also contain path() calls)
     */
    public void path(String pathTemplate, RouteGroup group) {
        router.path(pathTemplate, group);
    }

    /**
     * Maps an array of filters to be executed before any routes
     *
     * @param filter  the filter
     * @param filters the filters
     */
    public void before(Filter filter, Filter... filters) {
        router.before(filter, filters);
    }

    /**
     * Maps an array of filters to be executed after any routes
     *
     * @param filter  the filter
     * @param filters the filters
     */
    public void after(Filter filter, Filter... filters) {
        router.after(filter, filters);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path
     *
     * @param pathTemplate  the route path
     * @param filter        the filter
     * @param filters       the filters
     */
    public void before(String pathTemplate, Filter filter, Filter... filters) {
        router.before(pathTemplate, filter, filters);
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path
     *
     * @param pathTemplate  the route path
     * @param filter        the filter
     * @param filters       the filters
     */
    public void after(String pathTemplate, Filter filter, Filter... filters) {
        router.after(pathTemplate, filter, filters);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path and http method
     *
     * @param pathTemplate  the route path
     * @param method        the route HTTP METHOD
     * @param filter        the filter
     * @param filters       the filters
     */
    public void before(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, filter, filters);
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path and http method
     *
     * @param pathTemplate  the route path
     * @param method        the route HTTP METHOD
     * @param filter        the filter
     * @param filters       the filters
     */
    public void after(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, filter, filters);
    }

    /**
     * Maps an array of filters to be executed before any matching routes by path, http method and accept-type
     *
     * @param pathTemplate  the route path
     * @param method        the route HTTP METHOD
     * @param acceptType    the route Accept-Type
     * @param filter        the filter
     * @param filters       the filters
     */
    public void before(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, acceptType, filter, filters);
    }

    /**
     * Maps an array of filters to be executed after any matching routes by path, http method and accept-type
     *
     * @param pathTemplate  the route path
     * @param method        the route HTTP METHOD
     * @param acceptType    the route Accept-Type
     * @param filter        the filter
     * @param filters       the filters
     */
    public void after(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, acceptType, filter, filters);
    }

    /**
     * Map the route for HTTP GET requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void get(String pathTemplate, Route route) {
        router.get(pathTemplate, route);
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void post(String pathTemplate, Route route) {
        router.post(pathTemplate, route);
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void put(String pathTemplate, Route route) {
        router.put(pathTemplate, route);
    }

    /**
     * Map the route for HTTP PATCH requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void patch(String pathTemplate, Route route) {
        router.patch(pathTemplate, route);
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void delete(String pathTemplate, Route route) {
        router.delete(pathTemplate, route);
    }

    /**
     * Map the route for HTTP HEAD requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void head(String pathTemplate, Route route) {
        router.head(pathTemplate, route);
    }

    /**
     * Map the route for HTTP TRACE requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void trace(String pathTemplate, Route route) {
        router.trace(pathTemplate, route);
    }

    /**
     * Map the route for HTTP CONNECT requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void connect(String pathTemplate, Route route) {
        router.connect(pathTemplate, route);
    }

    /**
     * Map the route for HTTP OPTIONS requests
     *
     * @param pathTemplate the route path
     * @param route the route handler
     */
    public void options(String pathTemplate, Route route) {
        router.options(pathTemplate, route);
    }

    /**
     * Map the route for HTTP GET requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void get(String pathTemplate, String acceptType, Route route) {
        router.get(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void post(String pathTemplate, String acceptType, Route route) {
        router.post(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void put(String pathTemplate, String acceptType, Route route) {
        router.put(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP PATCH requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void patch(String pathTemplate, String acceptType, Route route) {
        router.patch(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void delete(String pathTemplate, String acceptType, Route route) {
        router.delete(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP HEAD requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void head(String pathTemplate, String acceptType, Route route) {
        router.head(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP TRACE requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void trace(String pathTemplate, String acceptType, Route route) {
        router.trace(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP CONNECT requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void connect(String pathTemplate, String acceptType, Route route) {
        router.connect(pathTemplate, acceptType, route);
    }

    /**
     * Map the route for HTTP OPTIONS requests
     *
     * @param pathTemplate the route path
     * @param acceptType the accept-type that route bind to
     * @param route the route handler
     */
    public void options(String pathTemplate, String acceptType, Route route) {
        router.options(pathTemplate, acceptType, route);
    }

    /**
     * Immediately stops a request within a filter or route with specified status code and body content
     * NOTE: When using this don't catch exceptions of type {@link SpottyHaltException}, or if catched, re-throw otherwise
     * halt will not work
     *
     * @param status The status code
     */
    public void halt(HttpStatus status) {
        throw new SpottyHaltException(status);
    }

    /**
     * Immediately stops a request within a filter or route with specified status code and body content
     * NOTE: When using this don't catch exceptions of type {@link SpottyHaltException}, or if caught, re-throw otherwise
     * halt will not work
     *
     * @param status The status code
     * @param body   The body content
     */
    public void halt(HttpStatus status, String body) {
        throw new SpottyHaltException(status, body);
    }

    /**
     * remove all registered routes
     */
    public void clearRoutes() {
        router.clearRoutes();
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and unmaps any matches that are found.
     *
     * @param pathTemplate  the route path
     * @return              true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate is null or blank
     */
    public boolean removeRoute(String pathTemplate) throws SpottyValidationException {
        return router.removeRoute(pathTemplate);
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and HTTP method, unmaps any
     * matches that are found.
     *
     * @param pathTemplate  the route path
     * @param method        the route HTTP METHOD
     * @return              true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate or method is null or blank
     */
    public boolean removeRoute(String pathTemplate, HttpMethod method) throws SpottyValidationException {
        return router.removeRoute(pathTemplate, method);
    }

    /**
     * Remove a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path, acceptType and HTTP method, unmaps any
     * matches that are found.
     *
     * @param pathTemplate  the route path
     * @param acceptType    the route accept-type
     * @param method        the route HTTP METHOD
     * @return              true if this is a matching route which has been previously routed
     * @throws SpottyValidationException if pathTemplate, acceptType or method is null or blank
     */
    public boolean removeRoute(String pathTemplate, String acceptType, HttpMethod method) throws SpottyValidationException {
        return router.removeRoute(pathTemplate, acceptType, method);
    }

    /**
     * register exception handler to handle any exceptions that can happen during requests
     *
     * @param exceptionClass exception class type
     * @param exceptionHandler exception handler
     * @param <T> class type of registered exception
     */
    public <T extends Exception> void exception(Class<T> exceptionClass, ExceptionHandler<T> exceptionHandler) {
        exceptionHandlerRegistry.register(exceptionClass, exceptionHandler);
    }

    /**
     * register exception handler for http 404 route not found error
     *
     * @param exceptionHandler handle for not found exception
     */
    public void notFound(ExceptionHandler<SpottyNotFoundException> exceptionHandler) {
        exceptionHandlerRegistry.register(SpottyNotFoundException.class, exceptionHandler);
    }

    /**
     * enable static files for root resource directory with route path
     *
     * @param templatePath route path
     */
    public void staticFiles(String templatePath) {
        staticFilesManager.staticFiles(templatePath);
    }

    /**
     * enable static files for resource child directory with route path
     *
     * @param filesDir path to files in resources directory
     * @param templatePath route path
     */
    public void staticFiles(String filesDir, String templatePath) {
        staticFilesManager.staticFiles(filesDir, templatePath);
    }

    /**
     * enable static files for directory with route path
     *
     * @param filesDir directory path to files
     * @param templatePath route path
     */
    public void externalStaticFiles(String filesDir, String templatePath) {
        staticFilesManager.externalStaticFiles(filesDir, templatePath);
    }

    /**
     * enable files cache to not load it each time from disk
     *
     * @param cacheTtl cache time to live in seconds
     * @param cacheSize maximum elements in cache
     */
    public void staticFilesCache(long cacheTtl, long cacheSize) {
        staticFilesManager.enableCache(cacheTtl, cacheSize);
    }

    private void registerSpottyDefaultFilters() {
        before((request, response) -> {
            response.headers()
                .add(DATE, RFC_1123_DATE_TIME.format(now(UTC)))
                .add(SERVER, SPOTTY_VERSION)
            ;
        });
    }

    private void registerSpottyDefaultExceptionHandlers() {
        exception(SpottyHttpException.class, (exception, request, response) -> {
            response
                .status(exception.status)
                .body(exception.getMessage())
            ;
        });

        exception(SpottyHaltException.class, (exception, request, response) -> {
            response
                .status(exception.status)
                .body(exception.getMessage())
            ;
        });

        exception(SpottyValidationException.class, (exception, request, response) -> {
            response
                .status(BAD_REQUEST)
                .body(exception.getMessage())
            ;
        });

        exception(Exception.class, (exception, request, response) -> {
            response
                .status(INTERNAL_SERVER_ERROR)
                .body(INTERNAL_SERVER_ERROR.statusMessage)
            ;
        });
    }

    public static final class Builder {
        private int port = DEFAULT_PORT;
        private int maxRequestBodySize = DEFAULT_MAX_REQUEST_BODY_SIZE;
        private final SessionManager.Builder sessionManagerBuilder = SessionManager.builder();
        private int reactorWorkers = DEFAULT_REACTOR_WORKERS;

        private Builder() {

        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder maxRequestBodySize(int maxRequestBodySize) {
            this.maxRequestBodySize = maxRequestBodySize;
            return this;
        }

        public Builder sessionCheckTickDelay(int sessionCheckTickDelay, TimeUnit timeUnit) {
            sessionManagerBuilder.sessionCheckTickDelay(sessionCheckTickDelay, timeUnit);
            return this;
        }

        public Builder defaultSessionTtl(long defaultSessionTtl) {
            sessionManagerBuilder.defaultSessionTtl(defaultSessionTtl);
            return this;
        }

        public Builder defaultSessionCookieTtl(long defaultSessionCookieTtl) {
            sessionManagerBuilder.defaultSessionCookieTtl(defaultSessionCookieTtl);
            return this;
        }

        public Builder reactorWorkers(int reactorWorkers) {
            this.reactorWorkers = reactorWorkers;
            return this;
        }

        public Spotty build() {
            return new Spotty(this);
        }
    }

}
