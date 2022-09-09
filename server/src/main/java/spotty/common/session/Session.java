/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import static spotty.common.validation.Validation.isNull;
import static spotty.common.validation.Validation.notNull;
import static spotty.common.validation.Validation.validate;

public final class Session {
    private final Map<Object, Object> data = new ConcurrentHashMap<>();

    public final UUID id;
    private Instant expires;

    public Session() {
        this(randomUUID());
    }

    public Session(UUID id) {
        this.id = notNull("id", id);
    }

    /**
     * @return expired timestamp
     */
    public Instant expires() {
        return expires;
    }

    /**
     * set expires timestamp
     *
     * @param expires timestamp
     * @return this instance of session
     */
    public Session expires(Instant expires) {
        this.expires = notNull("expires", expires);
        return this;
    }

    /**
     * set expires time in seconds from now + seconds
     *
     * @param seconds time to live in seconds
     * @return this instance of session
     */
    public Session ttl(long seconds) {
        validate(seconds > 0, "session ttl must be greater than zero");

        this.expires = Instant.now().plusSeconds(seconds);
        return this;
    }

    /**
     * put data to the session
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return this instance of session
     */
    public Session put(Object key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return this instance of session
     */
    public Session putIfAbsent(Object key, Object value) {
        data.putIfAbsent(key, value);
        return this;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into this map unless {@code null}.
     * <p>
     * If the function returns {@code null} no mapping is recorded. If
     * the function itself throws an (unchecked) exception, the
     * exception is rethrown, and no mapping is recorded.
     *
     * @param key    key with which the specified value is to be associated
     * @param mapper the function to compute a value
     * @return the current (existing or computed) value associated with
     * the specified key, or null if the computed value is null
     */
    @SuppressWarnings("all")
    public <T> T computeIfAbsent(Object key, Function<Object, T> mapper) {
        return (T) data.computeIfAbsent(key, mapper);
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into this map unless {@code null}.
     * <p>
     * If the function returns {@code null}, the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * @param key    key with which the specified value is to be associated
     * @param mapper the function to compute a value
     * @return the current (existing or computed) value associated with
     * the specified key, or null if the computed value is null
     */
    @SuppressWarnings("all")
    public <T> T computeIfPresent(Object key, BiFunction<Object, T, T> mapper) {
        return (T) data.computeIfPresent(key, (BiFunction) mapper);
    }

    /**
     * Attempts to compute a mapping for the specified key and its current
     * mapped value (or {@code null} if there is no current mapping). For
     * example, to either create or append a {@code String} msg to a value
     * mapping.
     * <p>
     * If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the function itself throws an
     * (unchecked) exception, the exception is rethrown, and the current mapping
     * is left unchanged.
     *
     * @param key    key with which the specified value is to be associated
     * @param mapper the function to compute a value
     * @return the new value associated with the specified key, or null if none
     */
    @SuppressWarnings("all")
    public <T> T compute(Object key, BiFunction<Object, T, T> mapper) {
        return (T) data.compute(key, (BiFunction) mapper);
    }

    /**
     * put all data from the map to the session
     *
     * @param data to be stored in this map
     * @return this instance of session
     */
    public Session putAll(Map<Object, Object> data) {
        this.data.putAll(data);
        return this;
    }

    /**
     * Returns the value by specified key, or {@code null} if no value for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     * {@code null} if this session contains no mapping for the key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) data.get(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this session contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(Object key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the number of data in this session
     */
    public int size() {
        return data.size();
    }

    /**
     * @return <tt>true</tt> if this session contains no key-value mappings
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * @return <tt>true</tt> if this session contains key-value mappings
     */
    public boolean isNotEmpty() {
        return data.size() > 0;
    }

    /**
     * Removes the mapping for a key from this session if it is present.
     *
     * @param key key whose mapping is to be removed from the session
     * @return this instance of session
     */
    public Session remove(Object key) {
        data.remove(key);
        return this;
    }

    /**
     * remove all data from the session
     *
     * @return this instance of session
     */
    public Session clear() {
        data.clear();
        return this;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this session.
     *
     * @return a set view of the keys contained in this session
     */
    public Set<Object> keys() {
        return data.keySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this session.
     *
     * @return a collection view of the values contained in this session
     */
    public Collection<Object> values() {
        return data.values();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this session.
     *
     * @return a set view of the mappings contained in this session
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        return data.entrySet();
    }

    /**
     * Returns <tt>true</tt> if this session contains a mapping for the specified key.
     *
     * @param key key whose presence in this session is to be tested
     * @return <tt>true</tt> if this session contains a mapping for the specified key
     */
    public boolean has(Object key) {
        return data.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this session contains no mapping for the specified key.
     *
     * @param key key whose presence in this session is to be tested
     * @return <tt>true</tt> if this session contains no mapping for the specified key
     */
    public boolean hasNot(Object key) {
        return !data.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this session contains a value for the specified key and value is equal with given.
     *
     * @param key   key whose presence in this session is to be tested
     * @param value value whose presence in this session is to be tested
     * @return <tt>true</tt> if this session contains a value for the specified key and value is equal with given.
     */
    public boolean hasAndEqual(Object key, Object value) {
        final Object foundValue = data.get(key);
        if (isNull(foundValue)) {
            return false;
        }

        return foundValue.equals(value);
    }

    /**
     * Performs the given action for each key-value until all entries
     * have been processed or the action throws an exception.
     *
     * @param action The action to be performed for each key-value
     */
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
