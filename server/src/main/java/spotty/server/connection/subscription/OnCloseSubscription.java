package spotty.server.connection.subscription;

import spotty.server.connection.Connection;
import spotty.server.exception.SpottyException;

@FunctionalInterface
public interface OnCloseSubscription {
    void onClose(Connection connection) throws SpottyException;
}
