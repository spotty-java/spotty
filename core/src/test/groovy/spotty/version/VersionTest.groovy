package spotty.version

import spock.lang.Specification
import spotty.common.exception.SpottyValidationException

class VersionTest extends Specification {
    def "should parse version correctly"() {
        given:
        var expected = new Version(12, 17, 422)
        var versionString = "12.17.422"

        when:
        var ver = Version.parse(versionString)

        then:
        ver == expected
    }

    def "should return error when wrong version"() {
        when:
        Version.parse(versionString)

        then:
        thrown SpottyValidationException

        where:
        versionString | _
        "12.17.422.1" | _
        "12.17."      | _
        "12.17"       | _
        "12.sdf"      | _
        "12.sdf.ll"   | _
        ""            | _
        null          | _
    }

    def "should sort correctly"() {
        given:
        var versions = [
            new Version(21, 23, 44),
            new Version(7, 43, 222),
            new Version(1, 11, 2),
            new Version(42, 435, 2),

            new Version(1, 27, 454),
            new Version(1, 12, 4),
            new Version(1, 234, 4423),

            new Version(3, 234, 763),
            new Version(3, 234, 222),
            new Version(3, 234, 543),
            new Version(3, 234, 32),
        ]

        var expected = [
            new Version(1, 11, 2),
            new Version(1, 12, 4),
            new Version(1, 27, 454),
            new Version(1, 234, 4423),

            new Version(3, 234, 32),
            new Version(3, 234, 222),
            new Version(3, 234, 543),
            new Version(3, 234, 763),

            new Version(7, 43, 222),
            new Version(21, 23, 44),
            new Version(42, 435, 2),
        ]

        when:
        versions.sort()

        then:
        expected == versions
    }
}
