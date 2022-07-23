package spotty.server.render

import spock.lang.Specification
import spotty.common.json.Json

class DefaultResponseRenderTest extends Specification {

    private ResponseRender render = new DefaultResponseRender()

    def "should render bytes correctly"() {
        given:
        var data = "hello".getBytes()

        when:
        var res = render.render(data)

        then:
        "hello" == new String(res)
    }

    def "should render json correctly"() {
        given:
        var json = '{"title":"name"}'
        var jsonNode = Json.parse(json)

        when:
        var res = render.render(jsonNode)

        then:
        json == new String(res)
    }

    def "should render string correctly"() {
        given:
        var data = "hello my world"

        when:
        var res = render.render(data)

        then:
        data == new String(res)
    }

    def "should render any object"() {
        when:
        var renderResult = render.render(object)

        then:
        object.toString() == new String(renderResult)

        where:
        object         | _
        new Object()   | _
        new Integer(1) | _
        new Long(1)    | _
        [1, 2, 3]      | _
    }

}
