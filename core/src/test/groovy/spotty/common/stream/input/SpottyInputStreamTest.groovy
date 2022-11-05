package spotty.common.stream.input

import spock.lang.Specification
import spotty.common.request.WebRequestTestData
import spotty.common.stream.output.SpottyByteArrayOutputStream

import java.nio.ByteBuffer

class SpottyInputStreamTest extends Specification implements WebRequestTestData {

    def "should batch read/write to steam correctly"() {
        given:
        var content = requestBody
        var stream = new SpottyInputStream(16)

        when:
        new Thread(() -> {
            var input = ByteBuffer.wrap(content.getBytes())
            while (input.hasRemaining()) {
                stream.write(input)
            }

            stream.writeCompleted()
        }).start()

        Thread.sleep(20)
        var data = toString(stream)
        stream.close()

        then:
        data == content
    }

    def "should batch read/write content with limit correctly"() {
        given:
        var limit = 111
        var content = requestBody.substring(0, limit)
        var stream = new SpottyInputStream(16)
        stream.limit(limit)

        when:
        new Thread(() -> {
            var input = ByteBuffer.wrap(requestBody.getBytes())
            while (input.hasRemaining() && stream.hasRemaining()) {
                stream.write(input)
            }

            stream.writeCompleted()
        }).start()

        Thread.sleep(20)
        var data = toString(stream)
        stream.close()

        then:
        data == content
    }

    def "should read/write byte to byte correctly"() {
        given:
        var stream = new SpottyInputStream(16)

        when:
        new Thread(() -> {
            var input = ByteBuffer.wrap(requestBody.getBytes())
            while (input.hasRemaining()) {
                stream.write(input)
            }

            stream.writeCompleted()
        }).start()

        Thread.sleep(20)

        var sb = new StringBuilder()
        var read
        while ((read = stream.read()) != -1) {
            sb.append((char) read)
        }

        stream.close()

        then:
        sb.toString() == requestBody
    }

    private static String toString(InputStream input) {
        try (final SpottyByteArrayOutputStream out = new SpottyByteArrayOutputStream()) {
            int read
            final byte[] data = new byte[64]
            while ((read = input.read(data)) != -1) {
                Thread.sleep(10)
                out.write(data, 0, read)
            }

            return new String(out.toByteArray())
        } catch (Exception e) {
            throw new RuntimeException(e)
        }
    }

}
