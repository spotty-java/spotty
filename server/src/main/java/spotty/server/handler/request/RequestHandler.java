package spotty.server.handler.request;

import spotty.common.request.DefaultSpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.server.render.DefaultResponseRender;
import spotty.server.render.ResponseRender;

public interface RequestHandler {
    ResponseRender DEFAULT_RESPONSE_RENDER = new DefaultResponseRender();

    void handle(DefaultSpottyRequest innerRequest, SpottyResponse response) throws Exception;

    default ResponseRender render() {
        return DEFAULT_RESPONSE_RENDER;
    }
}
