package spotty.server.handler.exception

import spock.lang.Specification

import java.nio.file.AccessDeniedException

class ExceptionHandlerServiceTest extends Specification {

    private def service = new ExceptionHandlerService()

    def "should find handler successfully"() {
        given:
        var ExceptionHandler exceptionHandler = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        service.register(Exception.class, exceptionHandler)
        service.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        var foundExceptionHandler = service.getHandler(Exception.class)
        var foundRuntimeExceptionHandler = service.getHandler(RuntimeException.class)

        then:
        foundExceptionHandler == exceptionHandler
        foundRuntimeExceptionHandler == runtimeExceptionHandler
    }

    def "should find handler for parent exception handler by child exception class"() {
        given:
        var ExceptionHandler illegalArgumentExceptionHandler = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        service.register(IllegalArgumentException.class, illegalArgumentExceptionHandler)
        service.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        // get child exception from IllegalArgumentException
        var found = service.getHandler(IllegalFormatFlagsException.class)

        then:
        found == illegalArgumentExceptionHandler
        service.handlers.size() == 3
        service.handlers.containsKey(IllegalFormatFlagsException.class)
    }

    def "should return null when exception or parent didn't registered"() {
        given:
        var ExceptionHandler illegalArgumentException = Mock(ExceptionHandler.class)
        var ExceptionHandler runtimeExceptionHandler = Mock(ExceptionHandler.class)

        service.register(IllegalArgumentException.class, illegalArgumentException)
        service.register(RuntimeException.class, runtimeExceptionHandler)

        when:
        var found = service.getHandler(AccessDeniedException.class)

        then:
        found == null
        service.handlers.size() == 3
        service.handlers.containsKey(AccessDeniedException.class)
    }

}
