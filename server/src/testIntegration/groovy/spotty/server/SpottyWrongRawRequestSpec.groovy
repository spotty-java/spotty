package spotty.server

import spotty.AppTestContext
import spotty.utils.RawHttpClient

class SpottyWrongRawRequestSpec extends AppTestContext {

    private RawHttpClient rawHttpClient

    def setup() {
        rawHttpClient = new RawHttpClient(SPOTTY.port())
    }

    def cleanup() {
        rawHttpClient.close()
    }

    def "should respond in same order for long handling"() {
        given:
        SPOTTY.post("/", { req, res ->
            Thread.sleep(100) // long request handling
            return req.body()
        })

        when:
        rawHttpClient.post("/", "hello1")
        rawHttpClient.post("/", "hello2")

        var response1 = new String(rawHttpClient.response().body)
        var response2 = new String(rawHttpClient.response().body)

        then:
        response1 == "hello1"
        response2 == "hello2"
    }

    def "should respond in same order for long handling and request with big body"() {
        given:
        SPOTTY.post("/", { req, res ->
            Thread.sleep(100) // long request handling
            return req.body()
        })

        var bigData = getClass().getResourceAsStream("/big_file.jpeg").bytes

        when:
        rawHttpClient.post("/", "hello1")
        rawHttpClient.post("/", bigData)
        rawHttpClient.post("/", "hello2")

        var response1 = new String(rawHttpClient.response().body)
        var response2 = rawHttpClient.response().body
        var response3 = new String(rawHttpClient.response().body)

        then:
        response1 == "hello1"
        response2 == bigData
        response3 == "hello2"
    }

    def "should return error when wrong header line"() {
        given:
        var socket = new Socket("localhost", SPOTTY.port())
        var writer = new PrintWriter(socket.getOutputStream())
        var inputStream = socket.getInputStream()

        when:
        writer.println("POST HTTP/1.1")
        writer.println("content-type: 0")
        writer.println()
        writer.flush()

        var buff = new byte[128]
        var read = inputStream.read(buff)
        var result = new String(buff, 0, read)

        then:
        result == """
                    HTTP/1.1 400 Bad Request
                    content-length: 40
                    content-type: text/plain
                    connection: close

                    invalid request head line: POST HTTP/1.1
                  """.stripIndent().trim()
    }

    def "should return error when method does not supported"() {
        when:
        rawHttpClient.query("WRONG_METHOD", "/", "")
        var response = rawHttpClient.response()
        var body = response.toString()

        then:
        body == """
                    HTTP/1.1 400 Bad Request
                    content-length: 31
                    content-type: text/plain
                    connection: close

                    unsupported method WRONG_METHOD
                """.stripIndent().trim()
    }

}
