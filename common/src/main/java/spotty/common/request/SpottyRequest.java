package spotty.common.request;

import org.apache.http.entity.ContentType;
import spotty.common.http.HttpHeaders;
import spotty.common.http.HttpMethod;

import java.util.Map;
import java.util.Set;

public interface SpottyRequest {
    String protocol();

    String scheme();

    HttpMethod method();

    String path();

    int contentLength();

    ContentType contentType();

    HttpHeaders headers();

    Map<String, String> params();

    String param(String name);

    Map<String, Set<String>> queryParamsMap();

    Set<String> queryParams();

    Set<String> queryParams(String name);

    String queryParam(String name);

    void attach(Object attachment);

    Object attachment();

    byte[] body();

}
