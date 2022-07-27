package spotty.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.annotation.VisibleForTesting;
import spotty.common.cookie.Cookie;
import spotty.common.exception.SpottyValidationException;
import spotty.common.request.SpottyInnerRequest;
import spotty.common.response.SpottyResponse;
import spotty.common.session.Session;

import java.io.Closeable;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static spotty.common.http.HttpHeaders.SPOTTY_SESSION_ID;
import static spotty.common.utils.ThreadUtils.threadPool;
import static spotty.common.validation.Validation.isNotNull;
import static spotty.common.validation.Validation.isNull;
import static spotty.common.validation.Validation.notNull;

public final class SessionManager implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

    private static final int DEFAULT_TICK = 60;
    private static final TimeUnit DEFAULT_TIME_UNIT = SECONDS;

    @VisibleForTesting
    final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private ScheduledExecutorService executor;
    private volatile boolean disabled = true;

    private final int sessionCheckTickDelay;
    private final TimeUnit timeUnit;
    private final long defaultSessionTtl;
    private final long defaultSessionCookieTtl;

    private SessionManager(Builder builder) {
        this.sessionCheckTickDelay = builder.sessionCheckTickDelay;
        this.timeUnit = notNull("timeUnit", builder.timeUnit);
        this.defaultSessionTtl = builder.defaultSessionTtl;
        this.defaultSessionCookieTtl = builder.defaultSessionCookieTtl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void enableSession() {
        if (disabled) {
            disabled = false;

            executor = Executors.newSingleThreadScheduledExecutor(threadPool("session-checker"));
            executor.scheduleWithFixedDelay(() -> {
                final Instant now = Instant.now();
                final Iterator<Map.Entry<UUID, Session>> iterator = sessions.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Session session = iterator.next().getValue();
                    if (session.expires().isBefore(now)) {
                        iterator.remove();
                    }
                }
            }, 0, sessionCheckTickDelay, timeUnit);
        } else {
            LOG.warn("session enabled already");
        }
    }

    public void disableSession() {
        disabled = true;

        close();
    }

    public void register(SpottyInnerRequest request, SpottyResponse response) {
        if (disabled) {
            return;
        }

        final String rawId = request.cookies().get(SPOTTY_SESSION_ID);
        final Session session = isNull(rawId) ? newSession(response) : restoreSession(rawId);

        request.session(session);
    }

    @Override
    public void close() {
        try {
            executor.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
    }

    private Session newSession(SpottyResponse response) {
        final Session session = new Session();
        if (defaultSessionTtl > 0) {
            session.ttl(defaultSessionTtl);
        }

        sessions.put(session.id, session);

        final Cookie.Builder cookie = Cookie.builder()
            .name(SPOTTY_SESSION_ID)
            .value(session.id.toString());

        if (defaultSessionCookieTtl > 0) {
            cookie.maxAge(defaultSessionCookieTtl);
        }

        response.addCookie(cookie.build());

        return session;
    }

    private Session restoreSession(String rawId) {
        final UUID sessionId = fromString(rawId);
        return sessions.computeIfAbsent(sessionId, id -> {
            final Session session = new Session(id);
            if (isNotNull(defaultSessionTtl)) {
                session.ttl(defaultSessionTtl);
            }

            return session;
        });
    }

    private UUID fromString(String rawId) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            throw new SpottyValidationException("invalid session id %s", e, rawId);
        }
    }

    public static class Builder {
        private int sessionCheckTickDelay = DEFAULT_TICK;
        private TimeUnit timeUnit = DEFAULT_TIME_UNIT;
        private long defaultSessionTtl = 0;
        private long defaultSessionCookieTtl = 0;

        public Builder sessionCheckTickDelay(int sessionCheckTickDelay, TimeUnit timeUnit) {
            this.sessionCheckTickDelay = sessionCheckTickDelay;
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder defaultSessionTtl(long defaultSessionTtl) {
            this.defaultSessionTtl = defaultSessionTtl;
            return this;
        }

        public Builder defaultSessionCookieTtl(long defaultSessionCookieTtl) {
            this.defaultSessionCookieTtl = defaultSessionCookieTtl;
            return this;
        }

        public SessionManager build() {
            return new SessionManager(this);
        }
    }
}