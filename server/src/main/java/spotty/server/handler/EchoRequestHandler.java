package spotty.server.handler;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

public final class EchoRequestHandler implements RequestHandler {

    @Override
    public void handle(SpottyRequest request, SpottyResponse response) {
        request.contentType.ifPresent(response::contentType);
        response.body(request.body);
    }
}
