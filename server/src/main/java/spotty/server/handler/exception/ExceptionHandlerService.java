package spotty.server.handler.exception;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

public class ExceptionHandlerService {

    @VisibleForTesting
    final Map<Class<? extends Exception>, ExceptionHandler> handlers = new HashMap<>();

    public <T extends Exception> void register(Class<T> exceptionClass, ExceptionHandler handler) {
        handlers.put(exceptionClass, handler);
    }

    public <T extends Exception> ExceptionHandler getHandler(Class<T> exceptionClass) {
        ExceptionHandler handler;
        if (handlers.containsKey(exceptionClass)) {
            handler = handlers.get(exceptionClass);
        } else {
            handler = findHandler(exceptionClass);

            // cache result
            handlers.put(exceptionClass, handler);
        }

        return handler;
    }

    private ExceptionHandler findHandler(Class<?> exceptionClass) {
        final Class<?> superclass = exceptionClass.getSuperclass();
        if (superclass == null) {
            return null;
        }

        if (handlers.containsKey(superclass)) {
            return handlers.get(superclass);
        }

        return findHandler(superclass);
    }

}
