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

    @Override
    public String toString() {
        return params.toString();
    }
}
