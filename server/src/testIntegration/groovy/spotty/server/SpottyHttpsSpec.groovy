package spotty.server

import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import spotty.AppTestContext

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.nio.file.Paths
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class SpottyHttpsSpec extends AppTestContext {

    def "should execute https request correctly"() {
        given:
        def tm = new TrustManager[]{
            new X509TrustManager() {
                @Override
                void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0]
                }
            }
        }

        def sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, tm, null)

        var client = HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLContext(sslContext)
            .build()

        SPOTTY.enableHttps(Paths.get("src/testIntegration/resources/selfsigned.jks").toAbsolutePath().toString(), "123456", null, null)
        SPOTTY.post("/", { req, res -> req.body() })

        when:
        var post = new HttpPost("https://localhost:${SPOTTY.port()}")
        post.setEntity(new StringEntity("hello"))

        var response = client.execute(post)

        then:
        response.entity.content.text == "hello"
    }

}
