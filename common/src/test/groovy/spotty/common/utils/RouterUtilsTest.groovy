package spotty.common.utils

import spock.lang.Specification
import spotty.common.router.route.ParamName

import static spotty.common.utils.RouterUtils.ALL_REPLACEMENT
import static spotty.common.utils.RouterUtils.PARAM_REPLACEMENT
import static spotty.common.utils.RouterUtils.compileMatcher
import static spotty.common.utils.RouterUtils.normalizePath

class RouterUtilsTest extends Specification {

    def "should normalize path correctly"() {
        when:
        var result = normalizePath(template)

        then:
        result == pathNormalized

        where:
        template                              | pathNormalized
        "/api/product/:id/:category"          | "/api/product/*/*"
        "/api/*"                              | "/api/*"
        "/api/*/product/*/category/:category" | "/api/*/product/*/category/*"
        "/:name/user/:id/*/delete"            | "/*/user/*/*/delete"
        "/*"                                  | "/*"
    }

    def "should compile correctly"() {
        when:
        var result = compileMatcher(template)
        var expectedParams = params.collect { new ParamName(it) }

        then:
        result.params == expectedParams
        result.matcher.toString() == matcher

        where:
        template                              | params               | matcher
        "/api/product/:id/:category"          | [":id", ":category"] | "^/api/product/${PARAM_REPLACEMENT.replace("name", "id")}/${PARAM_REPLACEMENT.replace("name", "category")}\$"
        "/api/*"                              | []                   | "^/api/$ALL_REPLACEMENT\$"
        "/api/*/product/*/category/:category" | [":category"]        | "^/api/$ALL_REPLACEMENT/product/$ALL_REPLACEMENT/category/${PARAM_REPLACEMENT.replace("name", "category")}\$"
        "/:name/user/:id/*/delete"            | [":name", ":id"]     | "^/$PARAM_REPLACEMENT/user/${PARAM_REPLACEMENT.replace("name", "id")}/$ALL_REPLACEMENT/delete\$"
        "/*"                                  | []                   | "^/$ALL_REPLACEMENT\$"
    }

}
