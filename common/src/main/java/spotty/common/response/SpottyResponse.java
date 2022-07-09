package spotty.common.response;

import lombok.Data;
import org.apache.http.entity.ContentType;
import spotty.common.http.Headers;
import spotty.common.http.HttpStatus;

import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static spotty.common.http.HttpStatus.OK;

@Data
public class SpottyResponse {
    private String protocol = "HTTP/1.1";
    private HttpStatus status = OK;
    private ContentType contentType = TEXT_PLAIN;
    private byte[] body;

    private final Headers headers = new Headers();

    public void addHeader(String name, String value) {
        this.headers.add(name, value);
    }

    public void addHeaders(Headers headers) {
        this.headers.add(headers);
    }

    public void replaceHeaders(Headers headers) {
        this.headers.clear();
        this.headers.add(headers);
    }

    public long getContentLength() {
        return body != null ? body.length : 0;
    }
}
