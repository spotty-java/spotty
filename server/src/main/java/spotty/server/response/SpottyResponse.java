package spotty.server.response;

import lombok.Data;
import org.apache.http.entity.ContentType;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;

@Data
public class SpottyResponse {
    private String protocol = "HTTP/1.1";
    private int status = SC_OK;
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
