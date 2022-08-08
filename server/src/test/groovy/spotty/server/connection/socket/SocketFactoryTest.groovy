package spotty.server.connection.socket

import spock.lang.Specification
import spotty.common.exception.SpottyStreamException
import spotty.common.exception.SpottyValidationException
import stub.SocketChannelStub

import javax.net.ssl.SSLContext

class SocketFactoryTest extends Specification {

    def "should create tcp socket"() {
        given:
        var socketFactory = new SocketFactory()
        var channel = new SocketChannelStub()
        channel.configureBlocking(false)

        when:
        var socket = socketFactory.createSocket(channel)

        then:
        socket instanceof TCPSocket
    }

    def "should create ssl socket"() {
        given:
        var sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, null, null)

        var socketFactory = new SocketFactory()
        socketFactory.enableSsl(sslContext)

        var channel = new SocketChannelStub()
        channel.configureBlocking(false)

        when:
        var socket = socketFactory.createSocket(channel)

        then:
        socket instanceof SSLSocket
    }

    def "should return an error when channel is null"() {
        given:
        var socketFactory = new SocketFactory()

        when:
        socketFactory.createSocket(null)

        then:
        thrown SpottyValidationException
    }

    def "should return en error when channel is blocking"() {
        given:
        var socketFactory = new SocketFactory()

        when:
        socketFactory.createSocket(new SocketChannelStub())

        then:
        thrown SpottyStreamException
    }

}
