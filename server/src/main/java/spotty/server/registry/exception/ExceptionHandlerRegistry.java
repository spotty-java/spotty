package spotty.server.registry.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import spotty.common.exception.SpottyException;
import spotty.server.handler.exception.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

public final class ExceptionHandlerRegistry {
    @VisibleForTesting
    final Map<Class<? extends Exception>, ExceptionHandler<? extends Exception>> handlers = new HashMap<>();

    public <T extends Exception> void register(Class<T> exceptionClass, ExceptionHandler<T> handler) {
        handlers.put(exceptionClass, handler);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends Exception> ExceptionHandler<? super T> getHandler(Class<T> exceptionClass) {
        ExceptionHandler<?> handler;
        if (handlers.containsKey(exceptionClass)) {
            handler = handlers.get(exceptionClass);
        } else {
            handler = findHandler(exceptionClass);
            if (handler == null) {
                throw new SpottyException("not found exception handler");
            }

            // cache result
            handlers.put(exceptionClass, handler);
        }

        return (ExceptionHandler<? super T>) handler;
    }

    private ExceptionHandler<? extends Exception> findHandler(Class<?> exceptionClass) {
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
