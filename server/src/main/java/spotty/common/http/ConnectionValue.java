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
package spotty.common.http;

import static spotty.common.validation.Validation.notBlank;

public enum ConnectionValue {
    KEEP_ALIVE("keep-alive"),
    CLOSE("close");

    public final String code;

    ConnectionValue(String code) {
        this.code = notBlank("code", code);
    }
}
