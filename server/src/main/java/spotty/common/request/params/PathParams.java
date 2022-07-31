package spotty.common.request.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static spotty.common.validation.Validation.notNull;

public final class PathParams {
    public static final PathParams EMPTY = new PathParams(emptyMap());

    private final Map<String, String> params;

    private PathParams(Map<String, String> params) {
        notNull("params", params);

        this.params = unmodifiableMap(new HashMap<>(params));
    }

    public static PathParams of(Map<String, String> params) {
        return new PathParams(params);
    }

    public String param(String name) {
        return params.get(name);
    }

    public Map<String, String> params() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PathParams that = (PathParams) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }
}
