package spotty.server.stream

import spock.lang.Specification

class SpottyInputStreamTest extends Specification {

    def "should read all data from steam correctly"() {
        given:
        var data = request.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(data.length)

        when:
        var actual = new String(spottyStream.readAllBytes())

        then:
        actual == request
    }

    def "should read all data line by line from steam correctly"() {
        given:
        var input = new SpottyInputStream(new ByteArrayInputStream(request.getBytes()))

        when:
        var result = ""
        var line
        while ((line = input.readLine()) != null) {
            result += "$line\n"
        }

        result = result.trim()

        then:
        request == result
    }

    def "should read limited data from stream"() {
        given:
        var limit = 10
        var data = request.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(limit)
        var expected = new byte[limit]
        System.arraycopy(data, 0, expected, 0, limit)

        when:
        var actual = spottyStream.readAllBytes()

        then:
        actual.length == limit
        actual == expected
    }

    def "should read limited data with offset"() {
        given:
        var limit = 10
        var offset = 5
        var len = 5
        var data = request.getBytes()
        var input = new ByteArrayInputStream(data)
        var spottyStream = new SpottyInputStream(input)
        spottyStream.fixedContentSize(limit)
        var expected = new byte[limit]
        System.arraycopy(data, 0, expected, offset, len)

        when:
        var actual = new byte[limit]
        var read = spottyStream.read(actual, offset, len)

        then:
        read == len
        actual == expected
    }

    def "should read separately correctly"() {
        given:
        var inputText = "line1\r\nline2\r\n\r\nline3"
        var expected = "line1\nline2\n\nline3"
        var spottyStream = new SpottyInputStream(new ByteArrayInputStream(inputText.getBytes()))

        when:
        var content = ""
        var line
        while ((line = spottyStream.readLine()) != null) {
            if (line == "") {
                break
            }

            content += "$line\n"
        }

        spottyStream.fixedContentSize(5)
        content = "$content\n" + new String(spottyStream.readAllBytes())

        then:
        expected == content
    }

    private final def headers = """
            POST / HTTP/1.1
            Content-Type: text/plain
            User-Agent: PostmanRuntime/7.29.0
            Accept: */*
            Postman-Token: 91219957-e976-41ed-9ece-32f6642d55bf
            Host: localhost:4000
            Accept-Encoding: gzip, deflate, br
            Connection: keep-alive
            Content-Length: 2808
        """.stripIndent(true).trim()

    private final def body = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris dapibus tortor aliquam metus viverra, id iaculis libero aliquet. Vivamus tempor sapien eu metus sollicitudin laoreet. Aliquam erat volutpat. Quisque nulla augue, posuere et condimentum id, consectetur eu felis. Aenean congue nibh orci, vel lacinia diam commodo eu. Nulla sem eros, venenatis at malesuada iaculis, dapibus quis ante. In iaculis sem quis eros interdum facilisis.
            Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Sed faucibus tortor non erat pellentesque scelerisque. Phasellus nunc dolor, aliquam in urna accumsan, faucibus tristique ligula. Nunc maximus augue in lacus mollis lobortis sit amet sit amet ipsum. Proin accumsan enim sit amet lectus varius egestas. Quisque dignissim, augue non sollicitudin scelerisque, nunc turpis sollicitudin lorem, non mollis erat ex rhoncus nisi. Integer rhoncus facilisis massa vitae aliquam. Fusce neque nulla, tristique quis lacinia quis, rutrum eget elit. Mauris blandit sapien et porttitor lobortis.
            Proin et augue et eros varius pulvinar. Nullam ut velit ut ipsum bibendum sagittis vitae ut tellus. Maecenas in mattis neque. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Phasellus sapien risus, molestie eget augue non, fermentum porta nisi. Curabitur lacus ligula, tempor non lobortis id, facilisis id nisi. Nam consequat scelerisque nulla quis tempor. Sed auctor massa eget lectus fringilla, non sollicitudin quam aliquam. Suspendisse et rhoncus ante. Donec ornare massa nec risus varius, in dapibus velit tempus. Nam sollicitudin orci viverra leo blandit varius. Quisque nec dolor gravida, dictum metus non, fermentum turpis.
            Maecenas nec elementum risus, eu dignissim quam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Integer nec semper quam. Curabitur leo augue, tincidunt id turpis in, pharetra blandit ante. Pellentesque tristique ut ante ut volutpat. Etiam sodales id eros at rhoncus. Ut efficitur turpis nec mauris facilisis, sed laoreet dui elementum. Donec sit amet varius eros. Sed sit amet ligula tincidunt, pellentesque diam ac, accumsan neque. Cras et nunc urna. Suspendisse potenti. Vivamus et leo non massa convallis dictum. Aliquam et suscipit sapien, vel faucibus ex. Donec eu purus cursus, rhoncus massa at, consequat arcu.
            Curabitur egestas dui ac commodo pellentesque. Integer nec condimentum eros. Proin bibendum lorem non maximus fermentum. Mauris vel dapibus dolor, ac pellentesque augue. Suspendisse porta, lacus vitae finibus egestas, metus velit aliquam tortor, quis hendrerit nibh massa eu nibh. Donec vehicula placerat cursus. Sed tincidunt ligula id suscipit mattis. Fusce accumsan quis orci vitae molestie. Phasellus a commodo metus.
        """.stripIndent(true).trim()

    private final def request = "$headers\n\n$body"

}
