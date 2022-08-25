package spotty.common.session

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.lang.Specification

import java.time.Instant
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

import static java.time.temporal.ChronoUnit.SECONDS
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

    def "should set expires"() {
        given:
        var expires = Instant.now().plusSeconds(10)
        var session = new Session()

        when:
        session.expires(expires)

        then:
        session.expires() == expires
    }

    def "should set ttl correctly"() {
        given:
        var session = new Session()
        var now = Instant.now()

        when:
        session.ttl(5)

        then:
        now.until(session.expires(), SECONDS) == 5
    }

    def "should putIfAbsent correctly"() {
        given:
        var session = new Session()

        when:
        session.putIfAbsent("name", "spotty")
        session.putIfAbsent("name", "new spotty")

        then:
        session.get("name") == "spotty"
    }

    def "should computeIfAbsent correctly"() {
        given:
        var session = new Session()
        var Function<Object, String> mapper = Mock()
        1 * mapper.apply("name") >> "spotty"

        when:
        var result = session.computeIfAbsent("name", mapper)

        then:
        result == "spotty"
        session.get("name") == "spotty"
    }

    def "should does not execute if value not present"() {
        given:
        var session = new Session()
        var BiFunction<Object, String, String> mapper = Mock()

        when:
        var result = session.computeIfPresent("name", mapper)

        then:
        result == null
        0 * mapper.apply("name")
    }

    def "should execute if value is present"() {
        given:
        var session = new Session()
        var BiFunction<Object, String, String> mapper = Mock()
        1 * mapper.apply("name", "new spotty") >> "spotty"

        when:
        session.put("name", "new spotty")
        var result = session.computeIfPresent("name", mapper)

        then:
        result == "spotty"
        session.get("name") == "spotty"
    }

    def "should compute correctly"() {
        given:
        var session = new Session()
        var BiFunction<Object, String, String> mapper = Mock()
        1 * mapper.apply("name", null) >> "spotty"

        when:
        var result = session.compute("name", mapper)

        then:
        result == "spotty"
        session.get("name") == "spotty"
    }

    def "should put all correctly"() {
        given:
        var session = new Session()
        var map = [name: "spotty", email: "spotty@email.com"]

        when:
        session.putAll(map)

        then:
        session.entrySet() == map.entrySet()
    }

    def "should get or default correctly"() {
        given:
        var session = new Session()

        when:
        session.put("name", "spotty")
        var result1 = session.getOrDefault("name", "new spotty")
        var result2 = session.getOrDefault("email", "some email")

        then:
        result1 == "spotty"
        result2 == "some email"
    }

    def "should remove key from session correctly"() {
        given:
        var session = new Session()

        when:
        session.put("name", "spotty")
        session.put("email", "spotty@email.com")

        session.remove("email")

        then:
        session.get("name") == "spotty"
        session.get("email") == null
        session.hasNot("email")
    }

    def "should clear all keys"() {
        given:
        var session = new Session()

        when:
        session.put("name", "spotty")
        session.put("email", "spotty@email.com")

        session.clear()

        then:
        session.isEmpty()
    }

    def "should return keys and values correctly"() {
        given:
        var session = new Session()
        var map = [name: "spotty", email: "spotty@email.com"]

        when:
        session.putAll(map)

        then:
        session.keys() == map.keySet()
        new ArrayList(session.values()) == new ArrayList(map.values())
    }

    def "should forEach execute correctly"() {
        given:
        var session = new Session()
        var map = [name: "spotty", email: "spotty@email.com"]
        var BiConsumer<Object, Object> consumer = Mock()

        when:
        session.putAll(map)
        session.forEach(consumer)

        then:
        1 * consumer.accept("name", "spotty")
        1 * consumer.accept("email", "spotty@email.com")
    }

    def "should sessions be equals when id the same"() {
        given:
        var id = randomUUID()
        var session = new Session(id)
        var session2 = new Session(id)

        when:
        session.put("name", "spotty")
        session2.put("name", "new spotty")

        then:
        session == session2
        session.hashCode() == session2.hashCode()
    }

    def "should sessions be not equals when id is not the same"() {
        when:
        var session = new Session()
        var session2 = new Session()

        then:
        session != session2
        session.hashCode() != session2.hashCode()
    }
}
