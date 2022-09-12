package spotty.server.handler.exception

import spock.lang.Specification
import spotty.common.exception.SpottyException
import spotty.server.registry.exception.ExceptionHandlerRegistry

import java.nio.file.AccessDeniedException

class ExceptionHandlerRegistryTest extends Specification {

    private def registry = new ExceptionHandlerRegistry()

    def "should find handler successfully"() {
        given:
        var ExceptionHandler exceptionHandler = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        registry.register(Exception.class, exceptionHandler)
        registry.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        var foundExceptionHandler = registry.getHandler(Exception.class)
        var foundRuntimeExceptionHandler = registry.getHandler(RuntimeException.class)

        then:
        foundExceptionHandler == exceptionHandler
        foundRuntimeExceptionHandler == runtimeExceptionHandler
    }

    def "should find handler for parent exception handler by child exception class"() {
        given:
        var ExceptionHandler illegalArgumentExceptionHandler = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        registry.register(IllegalArgumentException.class, illegalArgumentExceptionHandler)
        registry.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        // get child exception from IllegalArgumentException
        var found = registry.getHandler(IllegalFormatFlagsException.class)

        then:
        found == illegalArgumentExceptionHandler
        registry.handlers.size() == 3
        registry.handlers.containsKey(IllegalFormatFlagsException.class)
    }

    def "should return error when exception or parent didn't registered"() {
        given:
        var ExceptionHandler illegalArgumentException = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        registry.register(IllegalArgumentException.class, illegalArgumentException)
        registry.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        registry.getHandler(AccessDeniedException.class)

        then:
        thrown SpottyException.class
        registry.handlers.size() == 2
        !registry.handlers.containsKey(AccessDeniedException.class)
    }

}
