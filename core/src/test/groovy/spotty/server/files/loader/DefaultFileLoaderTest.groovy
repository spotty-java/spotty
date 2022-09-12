package spotty.server.files.loader

import spock.lang.Specification
import spotty.common.response.SpottyResponse
import spotty.server.files.detector.FileTypeDetector
import spotty.server.files.loader.impl.DefaultFileLoader

class DefaultFileLoaderTest extends Specification {

    def "should return content of file"() {
        given:
        var response = new SpottyResponse()
        var loader = new DefaultFileLoader(new FileTypeDetector())

        var file = getClass().getResource("/compressor/request.gzip")

        when:
        var content = loader.loadFile(file, response)

        then:
        file.bytes == content
        response.contentType() == "application/gzip"
    }

    def "should return error when file does not exists"() {
        given:
        var response = new SpottyResponse()
        var loader = new DefaultFileLoader(new FileTypeDetector())

        var file = new URL("file:/file.gzip")

        when:
        loader.loadFile(file, response)

        then:
        thrown FileNotFoundException
    }

}
