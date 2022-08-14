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
package spotty.common.request.params;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static spotty.common.validation.Validation.isBlank;

public final class QueryParams {
    public static final QueryParams EMPTY = new QueryParams();
    private final Map<String, Set<String>> params = new HashMap<>();

    private Map<String, Set<String>> immutableCopy;

    private QueryParams() {

    }

    public static QueryParams parse(String query) {
        if (isBlank(query)) {
            return EMPTY;
        }

        final QueryParams queryParams = new QueryParams();
        final String[] pairs = query.split("&");
        for (final String pair : pairs) {
            final String[] parts = pair.split("=");

            queryParams.add(parts[0], parts[1]);
        }

        return queryParams;
    }

    public String param(String name) {
        final Set<String> values = params.get(name);
        if (values == null) {
            return null;
        }

        return values.iterator().next();
    }

    public Set<String> params() {
        return unmodifiableSet(params.keySet());
    }

    public Set<String> params(String name) {
        return unmodifiableSet(params.get(name));
    }

    public Map<String, Set<String>> paramsMap() {
        if (immutableCopy == null) {
            immutableCopy = deepCopy(params);
        }

        return immutableCopy;
    }

    private void add(String name, String value) {
        params.computeIfAbsent(name, __ -> new HashSet<>())
            .add(value);
    }

    private Map<String, Set<String>> deepCopy(Map<String, Set<String>> map) {
        final Map<String, Set<String>> copy = new HashMap<>();
        map.forEach((name, value) ->
            copy.put(name, unmodifiableSet(value))
        );

        return unmodifiableMap(copy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final QueryParams that = (QueryParams) o;
        return Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }
}
