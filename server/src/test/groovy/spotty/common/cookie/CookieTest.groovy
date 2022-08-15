package spotty.common.cookie

import spock.lang.Specification

import static spotty.common.cookie.SameSite.STRICT

class CookieTest extends Specification {
    def "should build cookie correctly"() {
        given:
        def name = "name"
        def value = "spotty"
        def domain = "localhost"
        def maxAge = 10
        def path = "/"
        def sameSite = STRICT
        def secure = true
        def httpOnly = true

        when:
        var cookie = Cookie.builder()
            .name(name)
            .value(value)
            .domain(domain)
            .maxAge(maxAge)
            .path(path)
            .sameSite(sameSite)
            .secure(secure)
            .httpOnly(httpOnly)
            .build()

        then:
        name == cookie.name()
        value == cookie.value()
        domain == cookie.domain()
        maxAge == cookie.maxAge()
        path == cookie.path()
        sameSite == cookie.sameSite()
        secure == cookie.secure()
        httpOnly == cookie.httpOnly()
        cookie.hashCode() == cookie.toString().hashCode()
    }
}
