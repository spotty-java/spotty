package spotty.common.utils

import spock.lang.Specification

import java.util.function.IntSupplier
import java.util.function.Supplier

class MemoizedTest extends Specification {
    def "should memoize supplier and execute once"() {
        given:
        var Supplier<String> supplier = Mock()
        1 * supplier.get() >> "hello"

        when:
        var lazy = Memoized.lazy(supplier)
        lazy.get()
        lazy.get()

        then:
        lazy.get() == "hello"
    }

    def "should memoize intSupplier and execute once"() {
        given:
        var IntSupplier supplier = Mock()
        1 * supplier.getAsInt() >> 3

        when:
        var lazy = Memoized.lazy(supplier)
        lazy.getAsInt()
        lazy.getAsInt()

        then:
        lazy.getAsInt() == 3
    }
}
