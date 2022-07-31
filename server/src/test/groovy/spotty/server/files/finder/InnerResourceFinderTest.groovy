package spotty.server.files.finder

import spock.lang.Specification
import spotty.server.files.finder.impl.InnerResourceFinder

import java.nio.file.Paths

class InnerResourceFinderTest extends Specification {
    def "should return link to resource"() {
        given:
        var finder = new InnerResourceFinder()
        var file = Paths.get("src/test/resources/compressor/request.gzip").toUri().toURL()

        when:
        var resource = finder.find("/compressor/request.gzip")

        then:
        resource.bytes == file.bytes
    }

    def "should return null when resource not found"() {
        given:
        var finder = new InnerResourceFinder()

        when:
        var resource = finder.find("some/not/existing/file.gzip")

        then:
        resource == null
    }
}
