package spotty.server.compress

import spock.lang.Specification
import spotty.common.exception.SpottyValidationException
import spotty.common.request.WebRequestTestData

import static spotty.common.http.ContentEncoding.DEFLATE
import static spotty.common.http.ContentEncoding.GZIP

class CompressorTest extends Specification implements WebRequestTestData {

    private Compressor compressor = new Compressor()

    def "should gzip text correctly"() {
        given:
        var expected = getClass().getResourceAsStream("/compressor/request.gzip").getBytes()

        when:
        var gzip = compressor.compress(GZIP, requestBody.getBytes())

        then:
        expected == gzip
        gzip.length < requestBody.length()
    }

    def "should deflate text correctly"() {
        given:
        var expected = getClass().getResourceAsStream("/compressor/request.deflate").getBytes()

        when:
        var deflate = compressor.compress(DEFLATE, requestBody.getBytes())

        then:
        expected == deflate
        deflate.length < requestBody.length()
    }

    def "should return error when ContentEncoding does not supported"() {
        when:
        compressor.compress(null, "".getBytes())

        then:
        var e = thrown SpottyValidationException.class
        e.message == "encoding is null"
    }
}
