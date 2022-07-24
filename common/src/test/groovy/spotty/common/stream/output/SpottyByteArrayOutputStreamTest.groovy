package spotty.common.stream.output

import spock.lang.Specification

class SpottyByteArrayOutputStreamTest extends Specification {

    def "should change capacity correctly"() {
        given:
        var stream = new SpottyByteArrayOutputStream(15)

        when:
        stream.print("hello world")
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

    def "should reset correctly"() {
        given:
        var stream = new SpottyByteArrayOutputStream(15)

        when:
        stream.print("hello world")
        stream.capacity(2)

        then:
        stream.capacity() == 2
        stream.toString() == "he"

        stream.reset()
        stream.capacity() == 15
        stream.toString() == ""
    }

    def "should increase capacity when data to write bigger than initial buffer"() {
        given:
        var stream = new SpottyByteArrayOutputStream(5)

        when:
        stream.print(text)

        then:
        stream.capacity() == expectedCapacity
        stream.toString() == text

        where:
        text          | expectedCapacity
        "hello"       | 5
        "hello!"      | 10
        "hello world" | 11
    }

}
