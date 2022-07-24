package spotty.common.validation

import spock.lang.Specification
import spotty.common.exception.SpottyValidationException

class ValidationTest extends Specification {
    def "should return error when value is null"() {
        given:
        var value = null

        when:
        Validation.notNull("value", value)

        then:
        thrown SpottyValidationException
    }

    def "should return error when value is blank"() {
        given:
        var value = "  "

        when:
        Validation.notBlank("value", value)

        then:
        thrown SpottyValidationException
    }

    def "should return error when value is empty"() {
        given:
        var value = ""

        when:
        Validation.notEmpty("value", value)

        then:
        thrown SpottyValidationException
    }

    def "should return error when condition is false"() {
        given:
        var condition = false

        when:
        Validation.validate(condition, "error message %s %s", "param1", "param2")

        then:
        var e = thrown SpottyValidationException
        e.message == "error message param1 param2"
    }

    def "should check to empty correctly"() {
        when:
        var result = Validation.isEmpty(value)

        then:
        result == expectedResult

        where:
        value | expectedResult
        null  | true
        ""    | true
        " "   | false
        "1"   | false
        "\n"  | false
        "\r"  | false
        "\t"  | false
    }

    def "should check to blank correctly"() {
        when:
        var result = Validation.isBlank(value)

        then:
        result == expectedResult

        where:
        value            | expectedResult
        null             | true
        ""               | true
        "   "            | true
        "\n"             | true
        "\r"             | true
        "\t"             | true
        "  \n  \t  \r  " | true
        "1"              | false
        "  \n \t e \r  " | false
    }
}
