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
package spotty.common.request.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotty.common.exception.SpottyHttpException;
import spotty.common.request.SpottyRequest;

import static spotty.common.http.HttpHeaders.CONTENT_LENGTH;
import static spotty.common.http.HttpHeaders.CONTENT_TYPE;
import static spotty.common.http.HttpStatus.BAD_REQUEST;
import static spotty.common.validation.Validation.isBlank;

public final class RequestValidator {
    private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class);

    public static void validate(SpottyRequest request) throws SpottyHttpException {
        if (request == null) {
            throw new SpottyHttpException(BAD_REQUEST, "request is empty");
        }

        if (request.protocol() == null) {
            throw new SpottyHttpException(BAD_REQUEST, "protocol is empty");
        }

        if (request.contentLength() < 0) {
            throw new SpottyHttpException(BAD_REQUEST, "invalid " + CONTENT_LENGTH);
        }

        if (request.contentType() == null) {
            LOG.debug("empty " + CONTENT_TYPE);
        }

        if (isBlank(request.scheme())) {
            throw new SpottyHttpException(BAD_REQUEST, "scheme is empty");
        }

        if (request.method() == null) {
            throw new SpottyHttpException(BAD_REQUEST, "method is empty");
        }

        if (request.path() == null) {
            throw new SpottyHttpException(BAD_REQUEST, "path is empty");
        }
    }

}
