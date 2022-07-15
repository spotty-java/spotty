package spotty.server.handler;

import spotty.common.request.SpottyInnerRequest;
import spotty.common.response.SpottyResponse;

public interface RequestHandler {
    void handle(SpottyInnerRequest innerRequest, SpottyResponse response);
}
