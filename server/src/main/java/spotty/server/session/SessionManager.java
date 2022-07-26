package spotty.server.session;

import spotty.common.exception.SpottyValidationException;
import spotty.common.request.DefaultSpottyRequest;
import spotty.common.response.SpottyResponse;
import spotty.common.session.Session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static spotty.common.http.HttpHeaders.SPOTTY_SESSION_ID;
import static spotty.common.validation.Validation.isNull;

public final class SessionManager {
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private volatile boolean disabled = true;

    public void enableSession() {
        disabled = false;
    }

    public void register(DefaultSpottyRequest request, SpottyResponse response) {
        if (disabled) {
            return;
        }

        final Session session;
        final String rawId = request.cookies().get(SPOTTY_SESSION_ID);
        if (isNull(rawId)) {
            session = new Session();
            sessions.put(session.id, session);
            response.cookie(SPOTTY_SESSION_ID, session.id.toString());
        } else {
            final UUID sessionId = fromString(rawId);
            session = sessions.computeIfAbsent(sessionId, Session::new);
        }

        request.session(session);
    }

    private UUID fromString(String rawId) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new SpottyValidationException("invalid session id %s", e, rawId);
        }
    }
}
