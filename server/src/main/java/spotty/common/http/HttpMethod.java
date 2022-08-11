package spotty.common.http;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    TRACE,
    CONNECT,
    OPTIONS;

    private static final Map<String, HttpMethod> MAPPINGS = new HashMap<>();
    private static final Set<HttpMethod> CONTENT_LENGTH_REQUIRED = Sets.newHashSet(POST, PUT, PATCH, DELETE);

    static {
        for (HttpMethod httpMethod : values()) {
            MAPPINGS.put(httpMethod.name(), httpMethod);
        }
    }

    /**
     * Check if http method required content-length header
     *
     * @return true if http method is required content-length header
     */
    public boolean isContentLengthRequired() {
        return CONTENT_LENGTH_REQUIRED.contains(this);
    }

    /**
     * Resolve the given method value to an {@code HttpMethod}.
     *
     * @param method the method value as a String
     * @return the corresponding {@code HttpMethod}, or {@code null} if not found
     * @since 4.2.4
     */
    public static HttpMethod resolve(String method) {
        return (method != null ? MAPPINGS.get(method) : null);
    }

    /**
     * Determine whether this {@code HttpMethod} matches the given method value.
     *
     * @param method the HTTP method as a String
     * @return {@code true} if it matches, {@code false} otherwise
     * @since 4.2.4
     */
    public boolean matches(String method) {
        return name().equals(method);
    }

}

