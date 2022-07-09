package spotty.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import spotty.common.response.ResponseWriter;
import spotty.server.connection.ConnectionProcessor;
import spotty.server.handler.RequestHandler;
import spotty.server.worker.ReactorWorker;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spotty.common.request.RequestValidator.validate;

@Slf4j
public class Spotty implements Closeable {
    private static final int DEFAULT_PORT = 4000;
    private static final int DEFAULT_CONNECTIONS = 100;

    private final ExecutorService SERVER_RUN = Executors.newSingleThreadExecutor();
    private final ReactorWorker reactorWorker = new ReactorWorker();

    private volatile boolean running = false;
    private volatile boolean started = false;

    private final int port;
    private final int maxConnections;

    private int connections = 0;

    public Spotty() {
        this(DEFAULT_PORT);
    }

    public Spotty(int port) {
        this(port, DEFAULT_CONNECTIONS);
    }

    public Spotty(int port, int maxConnections) {
        this.port = port;
        this.maxConnections = maxConnections;
    }

    public synchronized void start() {
        if (started) {
            log.warn("server has been started already");
            return;
        }

        SERVER_RUN.execute(this::serverInit);
    }

    @Override
    public synchronized void close() {
        stop();
        SERVER_RUN.shutdownNow();
        reactorWorker.close();
    }

    public synchronized void awaitUntilStart() {
        try {
            while (!started)
                wait(1000);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public synchronized void awaitUntilStop() {
        try {
            while (started)
                wait(1000);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStarted() {
        return started;
    }

    @SneakyThrows
    private void serverInit() {
        try (final var serverSocket = ServerSocketChannel.open();
             final var selector = Selector.open()) {
            // Binding this server on the port
            serverSocket.bind(new InetSocketAddress("localhost", port), maxConnections);
            serverSocket.configureBlocking(false); // Make Server nonBlocking
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            log.info("server has been started on port " + port);
            run();
            started();

            while (running && !Thread.currentThread().isInterrupted()) {
                selector.select();
                final var keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    final var key = keys.next();
                    keys.remove();

                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    } else {
                        log.info("unsupported key ops {}", key.readyOps());
                    }
                }
            }
        } catch (IOException e) {
            log.error("start server error", e);
        } finally {
            close();

            stopped();
            log.info("server has been stopped");
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel socket = serverSocket.accept();
        socket.configureBlocking(false);

        log.info("connection accepted");

        final var connection = new ConnectionProcessor(socket);
        connections++;

        socket.register(key.selector(), SelectionKey.OP_READ)
            .attach(connection);

        log.info("connections: {}", connections);
    }

    private void read(SelectionKey key) throws IOException {
        final var connection = (ConnectionProcessor) key.attachment();
        connection.read();

        if (connection.isClosed()) {
            log.info("connection closed");
            key.cancel();
            connections--;

            log.info("connections: {}", connections);
            return;
        }

        if (connection.isMessageReady()) {
            reactorWorker.addAction(() -> {
                // TODO: routing handler
                final var handler = new RequestHandler();

                final var request = connection.request();
                validate(request);

                final var response = handler.process(request);
                final var data = ResponseWriter.write(response);
                connection.setResponse(data);

                key.interestOps(SelectionKey.OP_WRITE);
                key.selector().wakeup();
            });
        }
    }

    private void write(SelectionKey key) throws IOException {
        final var connection = (ConnectionProcessor) key.attachment();
        connection.write();

        if (connection.isWriteCompleted()) {
            connection.clearResponse();
            connection.clearRequest();

            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void run() {
        this.running = true;
    }

    private void stop() {
        this.running = false;
    }

    private synchronized void started() {
        this.started = true;
        notifyAll();
    }

    private synchronized void stopped() {
        this.started = false;
        notifyAll();
    }

}
