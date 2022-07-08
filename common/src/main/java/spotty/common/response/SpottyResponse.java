package spotty.common.response;

import lombok.Data;
import org.apache.http.entity.ContentType;
import spotty.common.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static spotty.common.http.HttpStatus.OK;

@Data
public class SpottyResponse {
    private String protocol = "HTTP/1.1";
    private HttpStatus status = OK;
    private ContentType contentType = TEXT_PLAIN;
    private byte[] body;

    private final Map<String, String> headers = new HashMap<>();

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public long getContentLength() {
        return body != null ? body.length : 0;
    }
}
