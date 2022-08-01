package spotty.server

import spotty.AppTestContext
import spotty.utils.RawHttpClient

class SpottyWrongRawRequestSpec extends AppTestContext {

    private RawHttpClient rawHttpClient = new RawHttpClient(SPOTTY.port())

    def "should respond in same order as fast request was for long handling"() {
        given:
        SPOTTY.post("/", {req, res ->
            Thread.sleep(100) // long request handling
            return req.body()
        })

        when:
        rawHttpClient.post("/", "hello1")
        rawHttpClient.post("/", "hello2")

        var response1 = rawHttpClient.body()
        var response2 = rawHttpClient.body()

        then:
        response1 == "hello1"
        response2 == "hello2"
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
        var read =  inputStream.read(buff)
        var result = new String(buff, 0, read)

        then:
        result == """
                    HTTP/1.1 400 Bad Request
                    content-length: 25
                    content-type: text/plain
                    connection: close
                     
                    invalid request head line
                  """.stripIndent().trim()
    }

    def "should return error when method does not supported"() {
        when:
        rawHttpClient.query("WRONG_METHOD", "/", "")
        var response = rawHttpClient.response()

        then:
        response == """
                        HTTP/1.1 400 Bad Request
                        content-length: 31
                        content-type: text/plain
                        connection: close
                         
                        unsupported method WRONG_METHOD
                    """.stripIndent().trim()
    }

}
