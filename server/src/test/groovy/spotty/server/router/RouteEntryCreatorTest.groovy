package spotty.server.router

import spock.lang.Specification
import spotty.server.router.route.ParamName
import spotty.server.router.route.RouteEntry

import static spotty.common.http.HttpMethod.GET
import static spotty.server.router.RouteEntryCreator.ALL_REPLACEMENT
import static spotty.server.router.RouteEntryCreator.PARAM_REPLACEMENT
import static spotty.server.router.RouteEntryCreator.create

class RouteEntryCreatorTest extends Specification {

    def "should create route entry correctly"() {
        given:
        var path = "/api/*/product/:id/:category"
        var matcher = "^/api/$ALL_REPLACEMENT/product/${PARAM_REPLACEMENT.replace("name", "id")}/${PARAM_REPLACEMENT.replace("name", "category")}\$"

        var expectedEntry = RouteEntry.builder()
            .path(path)
            .pathNormalized("/api/*/product/*/*")
            .pathParamKeys([new ParamName(":id"), new ParamName(":category")])
            .httpMethod(GET)
            .route({})
            .matcher(~matcher)
            .build()

        when:
        var routeEntry = create(path, GET, {})

        then:
        routeEntry.path == expectedEntry.path
        routeEntry.pathParamKeys == expectedEntry.pathParamKeys
        routeEntry.matcher.pattern() == expectedEntry.matcher.pattern()
        routeEntry.matches("/api/v1/product/7/iphone")
    }

    def "should match path template"() {
        when:
        var routeEntry = create(template, GET, {})

        then:
        routeEntry.matches(path) == expectedMatch

        where:
        template                              | path                                   | expectedMatch
        "/api/product/:id/:category"          | "/api/product/1/phone"                 | true
        "/api/*/product/:id/:category"        | "/api/v1/product/1/iphone"             | true
        "/api/product/:id/:category"          | "/api/product/1/phone/enable"          | false
        "/api/*"                              | "/api/product/1/phone/enable"          | true
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/phone"     | true
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/iphone_13" | true
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/"          | false
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/phone/13"  | false
    }

    def "should normalize path correctly"() {
        when:
        var routeEntry = create(template, GET, {})

        then:
        routeEntry.pathNormalized == pathNormalized

        where:
        template                              | pathNormalized
        "/api/product/:id/:category"          | "/api/product/*/*"
        "/api/*"                              | "/api/*"
        "/api/*/product/*/category/:category" | "/api/*/product/*/category/*"
        "/:name/user/:id/*/delete"            | "/*/user/*/*/delete"
        "/*"                                  | "/*"
    }

}
