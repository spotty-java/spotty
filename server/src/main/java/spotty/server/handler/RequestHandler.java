package spotty.server.handler;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

public class RequestHandler {

    public RequestHandler() {

    }

    public SpottyResponse process(SpottyRequest request) {
        final var response = new SpottyResponse();
        response.setContentType(response.getContentType());
        response.setBody(request.body);

        return response;
    }
}
