package spotty.server.handler;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

public class RequestHandler {

    public RequestHandler() {

    }

    public void process(SpottyRequest request, SpottyResponse response) {
        request.contentType.ifPresent(response::contentType);
        response.body(request.body);
    }
}
