package spotty.server

import spotty.AppTestContext

import java.nio.file.Paths

import static spotty.common.http.HttpStatus.NOT_FOUND

class SpottyStaticFilesSpec extends AppTestContext {
    def "should download static file"() {
        given:
        SPOTTY.staticFiles("/file")
        SPOTTY.staticFiles("/public", "/public")

        when:
        var response = httpClient.get("/file/test_file.txt")
        var response2 = httpClient.get("/public/test_public_file.txt")

        then:
        response == "hello"
        response2 == "public file"
    }

    def "should download external static file"() {
        given:
        var path = Paths.get("src/testIntegration/resources/public").toAbsolutePath()
        SPOTTY.externalStaticFiles(path.toString(), "/public")

        when:
        var response = httpClient.get("/public/test_public_file.txt")

        then:
        response == "public file"
    }

    def "should return not found error when file does not exist"() {
        given:
        SPOTTY.staticFiles("/file")

        when:
        var response = httpClient.getResponse("/file/not_exist_file.txt")

        then:
        response.statusLine.statusCode == NOT_FOUND.code
        response.entity.content.text == "file not found /file/not_exist_file.txt"
    }

    def "should enable caching and download file contents"() {
        given:
        SPOTTY.staticFiles("/file-cache")
        SPOTTY.staticFiles("/public", "/public-cache")
        SPOTTY.staticFilesCache(10, 10)

        when:
        var response = httpClient.get("/file-cache/test_file.txt")
        var response2 = httpClient.get("/public-cache/test_public_file.txt")

        then:
        response == "hello"
        response2 == "public file"
    }
}
