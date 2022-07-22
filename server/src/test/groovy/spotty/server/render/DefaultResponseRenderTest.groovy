package spotty.server.render

import spock.lang.Specification
import spotty.common.json.Json
import spotty.common.response.SpottyResponse

import static org.apache.http.entity.ContentType.APPLICATION_JSON

class DefaultResponseRenderTest extends Specification {

    private ResponseRender render = new DefaultResponseRender()

    def "should render bytes correctly"() {
        given:
        var data = "hello".getBytes()

        when:
        var res = render.render(new SpottyResponse(), data)

        then:
        "hello" == new String(res)
    }

    def "should render json correctly"() {
        given:
        var json = '{"title":"name"}'
        var jsonNode = Json.parse(json)

        when:
        var res = render.render(new SpottyResponse(), jsonNode)

        then:
        json == new String(res)
    }

    def "should render string correctly"() {
        given:
        var data = "hello my world"

        when:
        var res = render.render(new SpottyResponse(), data)

        then:
        data == new String(res)
    }

    def "should render any object"() {
        given:
        var response = new SpottyResponse()

        when:
        var renderResult = render.render(response, object)

        then:
        object.toString() == new String(renderResult)

        where:
        object         | _
        new Object()   | _
        new Integer(1) | _
        new Long(1)    | _
        [1, 2, 3]      | _
    }

    def "should render to json when response content-type is json"() {
        given:
        var response = new SpottyResponse()
        response.contentType(APPLICATION_JSON)

        when:
        var renderResult = render.render(response, object)

        then:
        json == new String(renderResult)

        where:
        object                         | json
        new Integer(1)                 | "1"
        new Long(2)                    | "2"
        [1, 2, 3]                      | "[1,2,3]"
        [name: "name", title: "title"] | '{"name":"name","title":"title"}'
    }
}
