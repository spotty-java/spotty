package spotty.common.session;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static spotty.common.validation.Validation.notNull;

public final class Session {
    private final Map<Object, Object> data = new ConcurrentHashMap<>();

    public final UUID id;
    private Instant expires = Instant.MAX;

    public Session() {
        this(randomUUID());
    }

    public Session(UUID id) {
        this.id = notNull("id", id);
    }

    public Instant expires() {
        return expires;
    }

    public Session expires(Instant expires) {
        this.expires = notNull("expires", expires);
        return this;
    }

    public Session ttl(long seconds) {
        this.expires = Instant.now().plusSeconds(seconds);
        return this;
    }

    public Session put(Object key, Object value) {
        data.put(key, value);
        return this;
    }

    public Session putIfAbsent(Object key, Object value) {
        data.putIfAbsent(key, value);
        return this;
    }

    @SuppressWarnings("all")
    public <T> T computeIfAbsent(Object key, Function<Object, T> mapper) {
        return (T) data.computeIfAbsent(key, mapper);
    }

    @SuppressWarnings("all")
    public <T> T computeIfPresent(Object key, BiFunction<Object, T, T> mapper) {
        return (T) data.computeIfPresent(key, (BiFunction) mapper);
    }

    @SuppressWarnings("all")
    public <T> T compute(Object key, BiFunction<Object, T, T> mapper) {
        return (T) data.compute(key, (BiFunction) mapper);
    }

    public Session putAll(Map<Object, Object> data) {
        this.data.putAll(data);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(Object key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public Session remove(Object key) {
        data.remove(key);
        return this;
    }

    public Session clear() {
        data.clear();
        return this;
    }

    public Set<Object> keySet() {
        return data.keySet();
    }

    public Collection<Object> values() {
        return data.values();
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return data.entrySet();
    }

    public boolean has(Object key) {
        return data.containsKey(key);
    }

    public void forEach(BiConsumer<Object, Object> action) {
        data.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Session session = (Session) o;
        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", data=" + data + "]";
    }
}
