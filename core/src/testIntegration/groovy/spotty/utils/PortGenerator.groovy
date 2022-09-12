package spotty.utils

import java.util.concurrent.atomic.AtomicInteger

class PortGenerator {
    private static final def GENERATOR = new AtomicInteger(40000)

    private PortGenerator() {

    }

    static int nextPort() {
        final var port = GENERATOR.getAndIncrement()
        if (isPortAvailable(port)) {
            return port
        }

        return nextPort()
    }

    private static boolean isPortAvailable(int port) {
        try (var socket = new ServerSocket(port)) {
            return true
        } catch(IOException ignored) {
            return false
        }
    }
}
