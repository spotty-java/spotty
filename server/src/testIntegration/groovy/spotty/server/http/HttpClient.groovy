package spotty.server.http

import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import static java.nio.charset.StandardCharsets.UTF_8

class HttpClient {

    private static def client = HttpClientBuilder.create().build()

    static def get(String url) {
        final HttpResponse response = client.execute(new HttpGet(url))

        return IOUtils.toString(response.entity.content, UTF_8)
    }

}
