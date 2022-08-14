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
package spotty.common.cookie;

import static spotty.common.validation.Validation.isNotBlank;
import static spotty.common.validation.Validation.isNotNull;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;

public final class Cookie {
    private final String name;
    private final String value;
    private final String domain;
    private final Long maxAge;
    private final String path;
    private final SameSite sameSite;
    private final boolean secure;
    private final boolean httpOnly;

    private final String toString;

    private Cookie(Builder builder) {
        this.name = notBlank("name", builder.name);
        this.value = notNull("value", builder.value);
        this.domain = builder.domain;
        this.maxAge = builder.maxAge;
        this.path = builder.path;
        this.sameSite = builder.sameSite;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;

        this.toString = buildString();
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public String domain() {
        return domain;
    }

    public Long maxAge() {
        return maxAge;
    }

    public String path() {
        return path;
    }

    public SameSite sameSite() {
        return sameSite;
    }

    public boolean secure() {
        return secure;
    }

    public boolean httpOnly() {
        return httpOnly;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int hashCode() {
        return toString.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return toString.equals(obj.toString());
    }

    private String buildString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);

        if (isNotBlank(domain)) {
            sb.append("; Domain=").append(domain);
        }

        if (isNotNull(maxAge)) {
            sb.append("; Max-Age=").append(maxAge);
        }

        if (isNotBlank(path)) {
            sb.append("; Path=").append(path);
        }

        if (isNotNull(sameSite)) {
            sb.append("; SameSite=").append(sameSite);
        }

        if (secure) {
            sb.append("; Secure");
        }

        if (httpOnly) {
            sb.append("; HttpOnly");
        }

        return sb.toString();
    }

    public static class Builder {
        private String name;
        private String value = "";
        private String domain;
        private Long maxAge;
        private String path;
        private SameSite sameSite;
        private boolean secure = false;
        private boolean httpOnly = false;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder sameSite(SameSite sameSite) {
            this.sameSite = sameSite;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public Cookie build() {
            return new Cookie(this);
        }
    }
}
