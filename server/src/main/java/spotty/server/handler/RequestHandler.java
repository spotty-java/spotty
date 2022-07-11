package spotty.server.handler;

import spotty.common.request.SpottyRequest;
import spotty.common.response.SpottyResponse;

public interface RequestHandler {
    void handle(SpottyRequest request, SpottyResponse response);
}
