package spotty.common.utils

import spock.lang.Specification

import java.nio.ByteBuffer

class IOUtilsTest extends Specification {
    def "should copy buffer correctly when src remaining is less then dest"() {
        given:
        var data = "hello"

        var src = ByteBuffer.wrap(data.bytes)
        var dest = ByteBuffer.allocate(20)

        when:
        var bytes = IOUtils.bufferCopyRemaining(src, dest)

        then:
        data == new String(dest.array(), 0, bytes)
    }

    def "should copy buffer correctly when src remaining is bigger then dest by one iteration"() {
        given:
        var data = "some long string"

        var src = ByteBuffer.wrap(data.bytes)
        var dest = ByteBuffer.allocate(5)

        when:
        var bytes = IOUtils.bufferCopyRemaining(src, dest)

        then:
        "some " == new String(dest.array(), 0, bytes)
    }

    def "should copy buffer fully correctly when src remaining is bigger then dest"() {
        given:
        var data = "some long string"

        var src = ByteBuffer.wrap(data.bytes)
        var dest = ByteBuffer.allocate(5)
        var result = ByteBuffer.allocate(data.length())

        when:
        var bytes = 0
        while (src.hasRemaining()) {
            bytes += IOUtils.bufferCopyRemaining(src, dest)
            dest.flip()
            result.put(dest)
            dest.clear()
        }

        then:
        data == new String(result.array(), 0, bytes)
    }
}
