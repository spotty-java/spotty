package spotty.common.request.params

import spock.lang.Specification

class QueryParamsTest extends Specification {

    def "should parse params correctly"() {
        given:
        var stringParams = "name=alex&email=email@email.com&email=second@email.com"

        when:
        var params = QueryParams.parse(stringParams)

        then:
        params.params() == ["name", "email"] as Set
        params.param("name") == "alex"
        params.param("email") == "email@email.com"
        params.params("name") == ["alex"] as Set
        params.params("email") == ["email@email.com", "second@email.com"] as Set
        params.paramsMap() == [name: ["alex"] as Set, email: ["email@email.com", "second@email.com"] as Set]
    }

}
