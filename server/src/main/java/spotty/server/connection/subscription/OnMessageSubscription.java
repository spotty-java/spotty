package spotty.server.connection.subscription;

import spotty.server.exception.SpottyException;
import spotty.server.request.SpottyRequest;
import spotty.server.response.SpottyResponse;

@FunctionalInterface
public interface OnMessageSubscription {
    void onMessage(SpottyRequest request, SpottyResponse response) throws SpottyException;
}
