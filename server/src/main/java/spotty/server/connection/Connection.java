package spotty.server.connection;

import lombok.extern.slf4j.Slf4j;
import spotty.server.connection.subscription.OnCloseSubscription;
import spotty.server.connection.subscription.OnMessageSubscription;
import spotty.server.exception.SpottyHttpException;
import spotty.server.parser.RequestParser;
import spotty.server.request.SpottyRequest;
import spotty.server.response.ResponseWriter;
import spotty.server.response.SpottyResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static spotty.server.request.RequestValidator.validate;

@Slf4j
public class Connection {
    private final List<OnCloseSubscription> onCloseSubscriptions = new ArrayList<>();
    private final List<OnMessageSubscription> onMessageSubscriptions = new ArrayList<>();

    private final Socket socket;

    public Connection(Socket socket) {
        this.socket = notNull(socket, "socket");
    }

    public void handle() {
        log.info("connection is ready");

        try {
            SpottyRequest request;
            while (!Thread.currentThread().isInterrupted() && (request = read()) != null) {
                final var response = new SpottyResponse();
                handleRequest(request, response);

                write(response);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            close();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("", e);
        } finally {
            log.info("connection closed");
            onCloseSubscriptions.forEach(subscription -> subscription.onClose(this));
        }
    }

    /**
     * @param subscription onClose event subscription
     */
    public void onCloseSubscribe(OnCloseSubscription subscription) {
        this.onCloseSubscriptions.add(subscription);
    }

    /**
     * @param subscription onMessage event subscription
     */
    public void onMessageSubscribe(OnMessageSubscription subscription) {
        this.onMessageSubscriptions.add(subscription);
    }

    private void onMessage(SpottyRequest request, SpottyResponse response) {
        onMessageSubscriptions.forEach(subscription -> subscription.onMessage(request, response));
    }

    private void handleRequest(SpottyRequest request, SpottyResponse response) {
        try {
            validate(request);
            onMessage(request, response);
        } catch (SpottyHttpException e) {
            log.error("", e);
            response.setStatus(e.status);
        } catch (IllegalArgumentException e) {
            log.error("", e);
            response.setStatus(SC_BAD_REQUEST);
        }
    }

    private SpottyRequest read() throws IOException {
        final var in = socket.getInputStream();

        return RequestParser.parse(in);
    }

    private void write(SpottyResponse response) throws IOException {
        final var out = socket.getOutputStream();

        final var data = ResponseWriter.write(response);

        out.write(data);
        out.flush();
    }
}
