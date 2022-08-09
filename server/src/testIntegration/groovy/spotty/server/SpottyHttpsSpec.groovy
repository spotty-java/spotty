package spotty.server

import kong.unirest.Unirest
import kong.unirest.UnirestInstance
import spock.lang.Ignore
import spotty.AppTestContext

import java.nio.file.Paths

import static java.util.concurrent.CompletableFuture.supplyAsync
import static java.util.stream.Collectors.toList

@Ignore // FIXME: https fails by timeout on github
class SpottyHttpsSpec extends AppTestContext {

    private static UnirestInstance unirest

    def setupSpec() {
        var keystoreFile = Paths.get("src/testIntegration/resources/selfsigned.jks").toAbsolutePath().toString()
        SPOTTY.enableHttps(keystoreFile, "123456", null, null)

        unirest = Unirest.spawnInstance()
        unirest.config()
            .defaultBaseUrl(SPOTTY.hostUrl())
            .verifySsl(false)
    }

    def cleanupSpec() {
        unirest.close()
    }

    def "should execute https request correctly"() {
        given:
        SPOTTY.post("/", { req, res -> req.body() })

        when:
        var response = unirest.post("/")
            .body("hello")
            .asString()

        then:
        response.body == "hello"
    }

    def "should send file successfully"() {
        given:
        SPOTTY.post("/file", { req, res -> req.body() })
        var file = getClass().getResourceAsStream("/big_file.jpeg").bytes

        when:
        var response = unirest.post("/file")
            .body(file)
            .asBytes()

        then:
        response.body == file
    }

    def "should handle a few concurrent requests one by one successfully"() {
        given:
        SPOTTY.post("/echo", { req, res -> req.body() })
        var file = getClass().getResourceAsStream("/big_file.jpeg").bytes
        var resultExpected = []
        for (i in 0..<10) {
            resultExpected += "hello$i" as String
        }

        when:
        var fileRequest = supplyAsync {
            unirest.post("/echo")
                .body(file)
                .asBytes()
        }

        var requests = []
        for (i in 0..<10) {
            var body = "hello$i"
            requests += supplyAsync {
                unirest.post("/echo")
                    .body(body)
                    .asBytes()
            }
        }

        var results = requests.stream()
            .map { it.join() }
            .map { it.body }
            .map { new String(it) }
            .collect(toList())

        var fileResult = fileRequest.join().body

        then:
        results == resultExpected
        fileResult == file
    }

}
