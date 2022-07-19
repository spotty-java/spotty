package spotty.server.router.route

import spock.lang.Specification
import spotty.common.request.params.PathParams

import static spotty.common.http.HttpMethod.GET
import static spotty.server.router.RouteEntryCreator.ALL_REPLACEMENT
import static spotty.server.router.RouteEntryCreator.PARAM_REPLACEMENT

class RouteEntryTest extends Specification {

    def "should parse path params correctly"() {
        given:
        var path = "/api/*/product/:id/:category_name2"
        var matcher = "^/api/$ALL_REPLACEMENT/product/${PARAM_REPLACEMENT.replace("name", "id")}/${PARAM_REPLACEMENT.replace("name", "categoryname2")}\$"

        var routeEntry = new RouteEntry()
            .pathTemplate(path)
            .pathNormalized("/api/*/product/*/*")
            .pathParamKeys([new ParamName(":id"), new ParamName(":category_name2")])
            .httpMethod(GET)
            .route({})
            .matcher(~matcher)

        when:
        var pathParams = routeEntry.parsePathParams("/api/v1/product/7/iphone")

        then:
        pathParams == PathParams.of([id: "7", category_name2: "iphone"])
    }

}
