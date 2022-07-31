package spotty.server.files.finder

import spock.lang.Specification
import spotty.server.files.finder.impl.ExternalResourceFinder

import java.nio.file.Paths

class ExternalResourceFinderTest extends Specification {
    def "should return link to resource"() {
        given:
        var finder = new ExternalResourceFinder()
        var path = Paths.get("src/test/resources/compressor/request.gzip").toAbsolutePath()

        when:
        var resource = finder.find(path.toString())

        then:
        resource == path.toUri().toURL()
    }

    def "should return null when resource not found"() {
        given:
        var finder = new ExternalResourceFinder()

        when:
        var resource = finder.find("some/not/existing/file.gzip")

        then:
        resource == null
    }
}
