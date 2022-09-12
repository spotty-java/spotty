package spotty.server.handler;

import spotty.common.request.SpottyDefaultRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.handler.request.RequestHandler;

public final class EchoRequestHandler implements RequestHandler {

    @Override
    public void handle(SpottyDefaultRequest innerRequest, SpottyResponse response) {
        if (innerRequest.contentType() != null) {
            response.contentType(innerRequest.contentType());
        }

        response.body(innerRequest.body());
    }
}
