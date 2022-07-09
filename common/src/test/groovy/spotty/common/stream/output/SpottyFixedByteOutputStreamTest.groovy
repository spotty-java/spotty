package spotty.common.stream.output

import spock.lang.Specification

import java.nio.ByteBuffer

class SpottyFixedByteOutputStreamTest extends Specification {

    def "should write batch #text successfully"() {
        given:
        var stream = new SpottyFixedByteOutputStream(10)

        when:
        stream.write(text.getBytes())

        then:
        text == stream.toString()

        where:
        text         | _
        "0123456789" | _
        "01234"      | _
        ""           | _
        "123"        | _
    }

    def "should write data #text byte by byte successfully"() {
        given:
        var stream = new SpottyFixedByteOutputStream(10)

        when:
        for (byte b in text.getBytes()) {
            stream.write(b)
        }

        then:
        text == stream.toString()

        where:
        text         | _
        "0123456789" | _
        "01234"      | _
        ""           | _
        "123"        | _
    }

    def "should return error IndexOutOfBoundsException when write batch and stream has fulled"() {
        given:
        var text = "1234"
        var stream = new SpottyFixedByteOutputStream(3)

        when:
        stream.write(text.getBytes())

        then:
        thrown IndexOutOfBoundsException
    }

    def "should return error IndexOutOfBoundsException when write byte by byte and stream has fulled"() {
        given:
        var text = "1234"
        var stream = new SpottyFixedByteOutputStream(3)

        when:
        for (byte b in text.getBytes()) {
            stream.write(b)
        }

        then:
        thrown IndexOutOfBoundsException
    }

    def "should write ByteBuffer #text successfully"() {
        given:
        var stream = new SpottyFixedByteOutputStream(10)
        var buffer = ByteBuffer.wrap(text.getBytes())

        when:
        stream.write(buffer)

        then:
        text == stream.toString()

        where:
        text         | _
        "0123456789" | _
        "01234"      | _
        ""           | _
        "123"        | _
    }

    def "should write ByteBuffer text '#expected' with offset #offset and len #len correctly"() {
        given:
        var stream = new SpottyFixedByteOutputStream(15)
        var buffer = ByteBuffer.wrap("hello world".getBytes())

        when:
        stream.write(buffer, offset, len)

        then:
        expected == stream.toString()

        where:
        expected      | offset | len
        "hello world" | 0      | 11
        "ello world"  | 1      | 10
        " world"      | 5      | 6
        "orld"        | 7      | 4
    }

    def "should change capacity correctly"() {
        given:
        var stream = new SpottyFixedByteOutputStream(15)

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

}
