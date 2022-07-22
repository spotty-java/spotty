package spotty.server.render;

import spotty.common.response.SpottyResponse;

public interface ResponseRender {
    byte[] render(SpottyResponse response, Object body);
}
