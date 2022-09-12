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

/**
 * An HTTP cookie (web cookie, browser cookie) is a small piece of data that a server sends to a user's web browser.
 * The browser may store the cookie and send it back to the same server with later requests.
 * Typically, an HTTP cookie is used to tell if two requests come from the same browser—keeping a user logged in,
 * for example. It remembers stateful information for the stateless HTTP protocol.
 *
 * Immutable thread safe cookie object
 */
public final class Cookie {
    /**
     * cookie name
     */
    private final String name;

    /**
     * cookie value
     */
    private final String value;

    /**
     * Defines the host to which the cookie will be sent.
     * If omitted, this attribute defaults to the host of the current document URL, not including subdomains.
     * Contrary to earlier specifications, leading dots in domain names (.example.com) are ignored.
     * Multiple host/domain values are not allowed, but if a domain is specified, then subdomains are always included.
     */
    private final String domain;

    /**
     * Indicates the number of seconds until the cookie expires.
     * A zero or negative number will expire the cookie immediately.
     * If both Expires and Max-Age are set, Max-Age has precedence.
     */
    private final Long maxAge;

    /**
     * Indicates the path that must exist in the requested URL for the browser to send the Cookie header.
     * The forward slash (/) character is interpreted as a directory separator,
     * and subdirectories are matched as well.
     */
    private final String path;

    /**
     * <p>The SameSite attribute lets servers specify whether/when cookies are sent with cross-site requests
     * (where Site is defined by the registrable domain and the scheme: http or https).
     * This provides some protection against cross-site request forgery attacks (CSRF).
     * It takes three possible values: Strict, Lax, and None.</p>
     *
     * <p>With Strict, the cookie is only sent to the site where it originated.
     * Lax is similar, except that cookies are sent when the user navigates to the cookie's origin site.
     * For example, by following a link from an external site.
     * None specifies that cookies are sent on both originating and cross-site requests,
     * but only in secure contexts (i.e., if SameSite=None then the Secure attribute must also be set).
     * If no SameSite attribute is set, the cookie is treated as Lax.</p>
     */
    private final SameSite sameSite;

    /**
     * Indicates that the cookie is sent to the server only
     * when a request is made with the https: scheme (except on localhost), and therefore,
     * is more resistant to man-in-the-middle attacks.
     */
    private final boolean secure;

    /**
     * Use the HttpOnly attribute to prevent access to cookie values via JavaScript.
     */
    private final boolean httpOnly;

    private final String toString;
    private final int hashCode;

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
        this.hashCode = toString.hashCode();
    }

    public static Builder builder() {
        return new Builder();
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

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int hashCode() {
        return hashCode;
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
