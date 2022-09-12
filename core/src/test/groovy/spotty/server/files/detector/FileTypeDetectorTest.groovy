package spotty.server.files.detector

import spock.lang.Specification

class FileTypeDetectorTest extends Specification {
    def "should detect file type correctly"() {
        given:
        var detector = new FileTypeDetector()
        var file = getClass().getResource("/compressor/request.gzip")

        when:
        var type = detector.detect(file)

        then:
        type == "application/gzip"
    }

    def "should return error when file does not exists"() {
        given:
        var detector = new FileTypeDetector()
        var file = new URL("file:/file.gzip")

        when:
        detector.detect(file)

        then:
        thrown FileNotFoundException
    }
}
