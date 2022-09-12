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
package spotty.common.exception;

public class SpottyException extends RuntimeException {

    public SpottyException(String message, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message);
    }

    public SpottyException(Throwable cause) {
        super(cause);
    }

    public SpottyException(String message, Throwable cause, Object... args) {
        super(args.length > 0 ? String.format(message, args) : message, cause);
    }

}
