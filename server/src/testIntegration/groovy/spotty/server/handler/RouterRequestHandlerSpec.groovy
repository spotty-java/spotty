package spotty.server.handler

import spotty.server.AppTestContext
import spotty.server.http.HttpClient

class RouterRequestHandlerSpec extends AppTestContext {

    def "should respond with query params correctly"() {
        given:
        SPOTTY.router.get("/hello", { req, res ->
            "hello ${req.queryParam("name")} ${req.queryParam("last_name")}"
        })

        when:
        def response = HttpClient.get("http://localhost:5050/hello?name=John&last_name=Doe")

        then:
        response == "hello John Doe"
    }

    def "should respond with path params correctly"() {
        given:
        SPOTTY.router.get("/hello/:name/:last_name", { req, res ->
            "hello ${req.param("name")} ${req.param("last_name")}"
        })

        when:
        def response = HttpClient.get("http://localhost:5050/hello/John/Doe")

        then:
        response == "hello John Doe"
    }
}
