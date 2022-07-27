package spotty.server

import org.apache.http.client.protocol.HttpClientContext
import spotty.AppTestContext

class SpottyCookieSpec extends AppTestContext {

    def "should respond with set cookies"() {
        given:
        SPOTTY.get("/hello", { req, res ->
            res.cookie("name", "John")
            res.cookie("lastName", "Doe")

            return "ok"
        })

        when:
        var context = HttpClientContext.create()
        httpClient.getResponse("/hello", context)

        var cookieStore = context.cookieStore

        then:
        cookieStore.cookies.size() == 2

        cookieStore.cookies.get(0).name == "lastName"
        cookieStore.cookies.get(0).value == "Doe"

        cookieStore.cookies.get(1).name == "name"
        cookieStore.cookies.get(1).value == "John"
    }

}
