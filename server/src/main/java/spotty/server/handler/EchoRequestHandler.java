package spotty.server.handler;

import spotty.common.request.SpottyInnerRequest;
import spotty.common.response.SpottyResponse;

public final class EchoRequestHandler implements RequestHandler {

    @Override
    public void handle(SpottyInnerRequest innerRequest, SpottyResponse response) {
        innerRequest.contentType().ifPresent(response::contentType);
        response.body(innerRequest.body());
    }
}
