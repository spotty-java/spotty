package spotty.server.http

import org.apache.http.HttpException
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.conn.routing.HttpRoute
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultRoutePlanner
import org.apache.http.protocol.HttpContext
import spotty.common.utils.IOUtils

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

        return IOUtils.toString(response.entity.content)
    }

    String post(String url) {
        return post(new HttpPost(url))
    }

    String post(HttpPost post) {
        final HttpResponse response = client.execute(post)

        return IOUtils.toString(response.entity.content)
    }

    HttpResponse getResponse(String url) {
        return client.execute(new HttpGet(url))
    }

    HttpResponse getResponse(String url, HttpClientContext context) {
        return client.execute(new HttpGet(url), context)
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
