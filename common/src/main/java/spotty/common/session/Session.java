package spotty.common.session;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static java.util.UUID.randomUUID;
import static spotty.common.validation.Validation.notNull;

public final class Session {
    private final Map<Object, Object> data = new ConcurrentHashMap<>();

    public final UUID id;

    public Session() {
        this(randomUUID());
    }

    public Session(UUID id) {
        this.id = notNull("id", id);
    }

    public Session put(Object key, Object value) {
        data.put(key, value);
        return this;
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
