package spotty.server.router

import spock.lang.Specification
import spotty.common.router.route.ParamName
import spotty.common.router.route.RouteEntry

import static spotty.common.http.HttpMethod.GET
import static spotty.common.utils.RouterUtils.ALL_REPLACEMENT
import static spotty.common.utils.RouterUtils.PARAM_REPLACEMENT
import static RouteEntryFactory.create
import static spotty.server.router.SpottyRouter.DEFAULT_ACCEPT_TYPE

class RouteEntryFactoryTest extends Specification {

    def "should create route entry correctly"() {
        given:
        var path = "/api/*/product/:id/:category"
        var matcher = "^/api/$ALL_REPLACEMENT/product/${PARAM_REPLACEMENT.replace("name", "id")}/${PARAM_REPLACEMENT.replace("name", "category")}\$"

        var expectedEntry = new RouteEntry()
            .pathTemplate(path)
            .pathNormalized("/api/*/product/*/*")
            .pathParamKeys([new ParamName(":id"), new ParamName(":category")])
            .httpMethod(GET)
            .route({})
            .matcher(~matcher)

        when:
        var routeEntry = create(path, GET, DEFAULT_ACCEPT_TYPE, {})

        then:
        routeEntry.pathTemplate() == expectedEntry.pathTemplate()
        routeEntry.pathParamKeys() == expectedEntry.pathParamKeys()
        routeEntry.matcher().pattern() == expectedEntry.matcher().pattern()
        routeEntry.matches("/api/v1/product/7/iphone")
    }

    def "should match path template"() {
        when:
        var routeEntry = create(template, GET, DEFAULT_ACCEPT_TYPE, {})

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

}
