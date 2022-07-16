package spotty.common.request.params

import spock.lang.Specification

class PathParamsTest extends Specification {

    def "should return clone of map"() {
        given:
        var expected = [name: "John", email: "email@email.com"]

        when:
        var params = PathParams.of(expected)

        then:
        params.params() == expected
    }

    def "should not be infected by map changing outside"() {
        given:
        var map = [name: "John", email: "email@email.com"]
        var expected = new HashMap<String, String>(map)

        when:
        var params = PathParams.of(map)
        map.put("title", "good_book")

        then:
        params.params() == expected
    }

}
