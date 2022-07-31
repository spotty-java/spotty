package spotty.server.files.loader

import spock.lang.Specification
import spotty.common.response.SpottyResponse
import spotty.server.files.detector.FileTypeDetector
import spotty.server.files.detector.TypeDetector
import spotty.server.files.loader.impl.CacheFileLoader

import java.time.ZonedDateTime

import static java.time.ZoneOffset.UTC
import static java.time.ZonedDateTime.now
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import static spotty.common.http.HttpHeaders.CACHE_CONTROL
import static spotty.common.http.HttpHeaders.EXPIRES

class CacheFileLoaderTest extends Specification {
    def "should return content of file"() {
        given:
        var response = new SpottyResponse()
        var loader = new CacheFileLoader(new FileTypeDetector(), 10, 10)

        var file = getClass().getResource("/compressor/request.gzip")

        when:
        var content = loader.loadFile(file, response)
        var expired = ZonedDateTime.parse(response.headers().get(EXPIRES), RFC_1123_DATE_TIME.withZone(UTC))
        var maximumExpected = now(UTC).plusSeconds(12).toEpochSecond()
        var now = now(UTC).toEpochSecond()

        then:
        file.bytes == content
        response.contentType() == "application/gzip"
        response.headers().hasAndEqual(CACHE_CONTROL, "private, max-age=10")
        expired.toEpochSecond() >= now && expired.toEpochSecond() <= maximumExpected
    }

    def "should not run file load logic after cache"() {
        given:
        var response = new SpottyResponse()
        var detector = Mock(TypeDetector.class)
        var loader = new CacheFileLoader(detector, 10, 10)

        var file = getClass().getResource("/compressor/request.gzip")

        detector.detect(file) >> "application/gzip"

        when:
        loader.loadFile(file, response)
        loader.loadFile(file, response)

        then:
        1 * detector.detect(file)
    }

    def "should return error when file does not exists"() {
        given:
        var response = new SpottyResponse()
        var loader = new CacheFileLoader(new FileTypeDetector(), 1, 1)

        var file = new URL("file:/file.gzip")

        when:
        loader.loadFile(file, response)

        then:
        thrown FileNotFoundException
    }

}
