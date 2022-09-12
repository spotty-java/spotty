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
        when:
        Validation.notEmpty("value", value)

        then:
        thrown SpottyValidationException

        where:
        value | _
        null  | _
        ""    | _
    }

    def "should does not throw exception when value is not empty"() {
        when:
        Validation.notEmpty("value", value)

        then:
        noExceptionThrown()

        where:
        value | _
        " "   | _
        "1"   | _
        "\n"  | _
        "\r"  | _
        "\t"  | _
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

    def "should check isNotBlank correctly"() {
        when:
        var result = Validation.isNotBlank(value)

        then:
        result == expectedResult

        where:
        value            | expectedResult
        null             | false
        ""               | false
        "   "            | false
        "\n"             | false
        "\r"             | false
        "\t"             | false
        "  \n  \t  \r  " | false
        "1"              | true
        "  \n \t e \r  " | true
    }

    def "should check isNull correctly"() {
        when:
        var resultNullTrue = Validation.isNull(null)
        var resultNullFalse = Validation.isNull("")

        then:
        resultNullTrue
        !resultNullFalse
    }

    def "should check isNotNull correctly"() {
        when:
        var resultIsNotNull = Validation.isNotNull("")
        var resultIsNotNullFalse = Validation.isNotNull(null)

        then:
        resultIsNotNull
        !resultIsNotNullFalse
    }
}
