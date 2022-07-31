package spotty.server.files

import spock.lang.Specification
import spotty.server.router.SpottyRouter

import static spotty.common.http.HttpMethod.GET

class StaticFilesManagerTest extends Specification {
    private def router = new SpottyRouter()
    private def filesManager = new StaticFilesManager(router)

    def "should extract file path from url correctly"() {
        when:
        def result = filesManager.getFilePath("$urlTemplate/*", path)

        then:
        result == expected

        where:
        path                                          | urlTemplate             | expected
        "/file/pdf/file.pdf"                          | "/file"                 | "/pdf/file.pdf"
        "/user/1/john/agreement/contract.pdf"         | "/user/:id/*/agreement" | "/contract.pdf"
        "/user/1/john/agreement/private/contract.pdf" | "/user/:id/*/agreement" | "/private/contract.pdf"
    }

    def "should staticFiles(templatePath) and register file router correctly"() {
        when:
        filesManager.staticFiles("/file")

        then:
        router.getRoute("/file/file.gzip", GET) != null
    }

    def "should staticFiles(filesDir, templatePath) and register file router correctly"() {
        when:
        filesManager.staticFiles("/public", "/file")

        then:
        router.getRoute("/file/file.gzip", GET) != null
    }

    def "should externalStaticFiles(filesDir, templatePath) and register file router correctly"() {
        when:
        filesManager.externalStaticFiles("/public", "/file")

        then:
        router.getRoute("/file/file.gzip", GET) != null
    }

}
