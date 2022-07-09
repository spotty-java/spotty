package spotty.common.stream.output

import spock.lang.Specification

class SpottyByteArrayOutputStreamTest extends Specification {

    def "should change capacity correctly"() {
        given:
        var stream = new SpottyByteArrayOutputStream(15)

        when:
        stream.write("hello world")
        stream.capacity(capacity)

        then:
        stream.capacity() == capacity
        expected == stream.toString()

        where:
        expected      | capacity
        "hello world" | 20
        "hello worl"  | 10
        "hello"       | 5
        "he"          | 2
    }

    def "should reset correctly" () {
        given:
        var stream = new SpottyByteArrayOutputStream(15)

        when:
        stream.write("hello world")
        stream.capacity(2)

        then:
        stream.capacity() == 2
        stream.toString() == "he"

        stream.reset()
        stream.capacity() == 15
        stream.toString() == ""
    }

}
