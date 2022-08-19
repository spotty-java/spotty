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

public enum SameSite {
    /**
     * means that the browser sends the cookie only for same-site requests,
     * that is, requests originating from the same site that set the cookie.
     * If a request originates from a different domain or scheme (even with the same domain),
     * no cookies with the SameSite=Strict attribute are sent.
     */
    STRICT("strict"),

    /**
     * means that the cookie is not sent on cross-site requests,
     * such as on requests to load images or frames,
     * but is sent when a user is navigating to the origin site from an external site
     * (for example, when following a link).
     * This is the default behavior if the SameSite attribute is not specified.
     */
    LAX("lax"),

    /**
     * means that the browser sends the cookie with both cross-site and same-site requests.
     * The Secure attribute must also be set when setting this value, like so SameSite=None; Secure
     */
    NONE("none");

    public final String code;

    SameSite(String code) {
        this.code = code;
    }
}
