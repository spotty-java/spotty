package spotty.server.render;

import com.fasterxml.jackson.databind.JsonNode;
import spotty.common.json.Json;
import spotty.common.response.SpottyResponse;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public final class DefaultResponseRender implements ResponseRender {

    @Override
    public byte[] render(SpottyResponse response, Object body) {
        if (body instanceof byte[]) {
            return (byte[]) body;
        }

        if (body instanceof JsonNode || APPLICATION_JSON.getMimeType().equals(response.contentType().getMimeType())) {
            return Json.writeValueAsBytes(body);
        }

        return body.toString().getBytes(UTF_8);
    }

}
