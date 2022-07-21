package spotty.server.handler.exception;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

@FunctionalInterface
public interface ExceptionHandler<E extends Exception> {
    /**
     * Invoked when an exception that is mapped to this handler occurs during routing
     *
     * @param exception The exception that was thrown during routing
     * @param request   The request object providing information about the HTTP request
     * @param response  The response object providing functionality for modifying the response
     */
    void handle(E exception, SpottyRequest request, SpottyResponse response);
}