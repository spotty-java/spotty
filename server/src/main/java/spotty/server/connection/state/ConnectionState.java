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
package spotty.server.connection.state;

import com.google.common.collect.Sets;

import java.util.Set;

public enum ConnectionState {
    DATA_REMAINING, // connection has some data that didn't consume and handle

    INITIALIZED,
    READY_TO_READ,
    READING_REQUEST_HEAD_LINE,
    HEADERS_READY_TO_READ,
    READING_HEADERS,
    PREPARE_HEADERS,
    BODY_READY_TO_READ,
    READING_BODY,
    BODY_READY,
    REQUEST_READY,
    REQUEST_HANDLING,
    READY_TO_WRITE,
    RESPONSE_WRITING,
    RESPONSE_WRITE_COMPLETED,
    CLOSED;

    private static final Set<ConnectionState> READING_STATES = Sets.newHashSet(
        INITIALIZED,
        READY_TO_READ,
        READING_REQUEST_HEAD_LINE,
        HEADERS_READY_TO_READ,
        READING_HEADERS,
        PREPARE_HEADERS,
        BODY_READY_TO_READ,
        READING_BODY,
        BODY_READY
    );

    public boolean isReading() {
        return READING_STATES.contains(this);
    }
}
