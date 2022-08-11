package spotty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public final class Spotty {
    private static final Logger LOG = LoggerFactory.getLogger(Spotty.class);
    private static final String SPOTTY_VERSION = "Spotty v" + VERSION;
    private static final int DEFAULT_PORT = 4000;
    private static final int DEFAULT_MAX_REQUEST_BODY_SIZE = 10 * 1024 * 1024; // 10Mb
    private static final int DEFAULT_REACTOR_WORKERS = 24;

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

    public Spotty(Builder builder) {
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

    public void enableHttps(String keyStorePath, String keyStorePassword, String trustStorePath, String trustStorePassword) {
        server.enableHttps(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword);
    }

    public void enableSession() {
        sessionManager.enableSession();
    }

    public synchronized void stop() {
        server.close();
        sessionManager.close();
    }

    public int connections() {
        return server.connections();
    }

    public int port() {
        return server.port();
    }

    public String host() {
        return server.host();
    }

    public String hostUrl() {
        return server.hostUrl();
    }

    public void path(String pathTemplate, RouteGroup group) {
        router.path(pathTemplate, group);
    }

    public void before(Filter filter, Filter... filters) {
        router.before(filter, filters);
    }

    public void after(Filter filter, Filter... filters) {
        router.after(filter, filters);
    }

    public void before(String pathTemplate, Filter filter, Filter... filters) {
        router.before(pathTemplate, filter, filters);
    }

    public void after(String pathTemplate, Filter filter, Filter... filters) {
        router.after(pathTemplate, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, filter, filters);
    }

    public void after(String pathTemplate, HttpMethod method, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, filter, filters);
    }

    public void before(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.before(pathTemplate, method, acceptType, filter, filters);
    }

    public void after(String pathTemplate, HttpMethod method, String acceptType, Filter filter, Filter... filters) {
        router.after(pathTemplate, method, acceptType, filter, filters);
    }

    public void get(String pathTemplate, Route route) {
        router.get(pathTemplate, route);
    }

    public void post(String pathTemplate, Route route) {
        router.post(pathTemplate, route);
    }

    public void put(String pathTemplate, Route route) {
        router.put(pathTemplate, route);
    }

    public void patch(String pathTemplate, Route route) {
        router.patch(pathTemplate, route);
    }

    public void delete(String pathTemplate, Route route) {
        router.delete(pathTemplate, route);
    }

    public void head(String pathTemplate, Route route) {
        router.head(pathTemplate, route);
    }

    public void trace(String pathTemplate, Route route) {
        router.trace(pathTemplate, route);
    }

    public void connect(String pathTemplate, Route route) {
        router.connect(pathTemplate, route);
    }

    public void options(String pathTemplate, Route route) {
        router.options(pathTemplate, route);
    }

    public void get(String pathTemplate, String acceptType, Route route) {
        router.get(pathTemplate, acceptType, route);
    }

    public void post(String pathTemplate, String acceptType, Route route) {
        router.post(pathTemplate, acceptType, route);
    }

    public void put(String pathTemplate, String acceptType, Route route) {
        router.put(pathTemplate, acceptType, route);
    }

    public void patch(String pathTemplate, String acceptType, Route route) {
        router.patch(pathTemplate, acceptType, route);
    }

    public void delete(String pathTemplate, String acceptType, Route route) {
        router.delete(pathTemplate, acceptType, route);
    }

    public void head(String pathTemplate, String acceptType, Route route) {
        router.head(pathTemplate, acceptType, route);
    }

    public void trace(String pathTemplate, String acceptType, Route route) {
        router.trace(pathTemplate, acceptType, route);
    }

    public void connect(String pathTemplate, String acceptType, Route route) {
        router.connect(pathTemplate, acceptType, route);
    }

    public void options(String pathTemplate, String acceptType, Route route) {
        router.options(pathTemplate, acceptType, route);
    }

    public void halt(HttpStatus status) {
        throw new SpottyHttpException(status);
    }

    public void halt(HttpStatus status, String body) {
        throw new SpottyHttpException(status, body);
    }

    public void clearRoutes() {
        router.clearRoutes();
    }

    public boolean removeRoute(String pathTemplate) {
        return router.removeRoute(pathTemplate);
    }

    public boolean removeRoute(String pathTemplate, HttpMethod method) {
        return router.removeRoute(pathTemplate, method);
    }

    public boolean removeRoute(String pathTemplate, String acceptType, HttpMethod method) {
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
