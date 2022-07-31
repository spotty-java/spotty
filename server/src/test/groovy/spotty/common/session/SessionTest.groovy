package spotty.common.session

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.lang.Specification

import static java.util.UUID.randomUUID

class SessionTest extends Specification {
    def "should generate random id when default constructor has called"() {
        when:
        var session = new Session()

        then:
        session.id != null
    }

    def "should take passed id"() {
        given:
        var id = randomUUID()

        when:
        var session = new Session(id)

        then:
        session.id == id
    }

    def "should store data correctly"() {
        given:
        var session = new Session()

        when:
        session.put("name", "alex")
        session.put(12, 12)
        session.put('c', "char")

        then:
        session.get("name") == "alex"
        session.get(12) == 12
        session.get('c') == "char"
    }

    def "should cast session data correctly"() {
        given:
        var session = new Session()
        var session2 = new Session()

        when:
        session.put("name", "alex")
        session.put(12, 12)
        session.put("char", 'c')
        session.put("session", session2)
        session.put("list", [])

        var String data1 = session.get("name")
        var Integer data2 = session.get(12)
        var Character data3 = session.get("char")
        var Session data4 = session.get("session")
        var List data5 = session.get("list")

        then:
        noExceptionThrown()
        data1 == "alex"
        data2 == 12
        data3 == 'c'
        data4 == session2
        data5 == []
    }

    def "should throw cast exception when invalid type"() {
        given:
        var session = new Session()

        when:
        session.put("key", [])

        var Integer data = session.get("key")

        then:
        thrown GroovyCastException
    }
}
