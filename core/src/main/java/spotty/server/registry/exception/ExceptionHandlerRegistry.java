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
package spotty.server.registry.exception;

import com.google.common.annotations.VisibleForTesting;
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
