package spotty.common.router.route

import spock.lang.Specification

class ParamNameTest extends Specification {

    def "should normalize name correctly"() {
        when:
        var paramName = new ParamName(name)

        then:
        paramName.name == nameExpected
        paramName.groupName == groupName

        where:
        name              | nameExpected     | groupName
        ":name"           | "name"           | "name"
        ":1name"          | "1name"          | "1name"
        ":category_name"  | "category_name"  | "categoryname"
        ":category_name_" | "category_name_" | "categoryname"
    }

}
