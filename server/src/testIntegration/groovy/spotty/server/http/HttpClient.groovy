package spotty.server.http

import org.apache.commons.io.IOUtils
import org.apache.http.HttpException
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultRoutePlanner
import org.apache.http.protocol.HttpContext

import static java.nio.charset.StandardCharsets.UTF_8
import static org.apache.http.impl.conn.DefaultSchemePortResolver.INSTANCE

class HttpClient implements Closeable {
    private def LOCALHOST

    private CloseableHttpClient client

    HttpClient(int port) {
        LOCALHOST = new HttpHost("localhost", port)
        client = HttpClientBuilder.create()
            .setRoutePlanner(routePlanner())
            .build()
    }

    String get(String url) {
        return get(new HttpGet(url))
    }

    String get(HttpGet get) {
        final HttpResponse response = client.execute(get)

        return IOUtils.toString(response.entity.content, UTF_8)
    }

    @Override
    void close() {
        try {
            client.close()
        } catch (Exception ignored) {
            // ignore
        }
    }

    private DefaultRoutePlanner routePlanner() {
        return new DefaultRoutePlanner(INSTANCE) {
            @Override
            HttpRoute determineRoute(
                final HttpHost host,
                final HttpRequest request,
                final HttpContext context) throws HttpException {
                HttpHost target = host == null ? LOCALHOST : host
                return super.determineRoute(target, request, context)
            }
        }
    }
}
