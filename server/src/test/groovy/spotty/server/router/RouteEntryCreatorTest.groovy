package spotty.server.router

import spock.lang.Specification
import spotty.server.router.route.RouteEntry

import static spotty.common.http.HttpMethod.GET
import static spotty.server.router.RouteEntryCreator.ALL_REPLACEMENT
import static spotty.server.router.RouteEntryCreator.PARAM_REPLACEMENT
import static spotty.server.router.RouteEntryCreator.create

class RouteEntryCreatorTest extends Specification {

    def "should create route entry correctly"() {
        given:
        var path = "/api/*/product/:id/:category"
        var matcher = "^/api/$ALL_REPLACEMENT/product/$PARAM_REPLACEMENT/$PARAM_REPLACEMENT\$"

        var expectedEntry = RouteEntry.builder()
            .path(path)
            .params([":id", ":category"])
            .route({})
            .matcher(~matcher)
            .build()

        when:
        var routeEntry = create(path, GET, {})

        then:
        routeEntry.path == expectedEntry.path
        routeEntry.params == expectedEntry.params
        routeEntry.matcher.pattern() == expectedEntry.matcher.pattern()
    }

    def "should match path template"() {
        when:
        var routeEntry = create(template, GET, {})

        then:
        routeEntry.matches(path) == expectedMatch

        where:
        template                              | path                                  | expectedMatch
        "/api/product/:id/:category"          | "/api/product/1/phone"                | true
        "/api/product/:id/:category"          | "/api/product/1/phone/enable"         | false
        "/api/*"                              | "/api/product/1/phone/enable"         | true
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/phone"    | true
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/"         | false
        "/api/*/product/*/category/:category" | "/api/v1/product/1/category/phone/13" | false
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
