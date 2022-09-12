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
package spotty.common.validation;

import spotty.common.exception.SpottyValidationException;

public final class Validation {
    private Validation() {
    }

    public static <T> T notNull(String paramName, T value) {
        validate(value != null, "%s is null", paramName);
        return value;
    }

    public static String notBlank(String paramName, String value) {
        validate(!isBlank(value), "%s is blank", paramName);
        return value;
    }

    public static String notEmpty(String paramName, String value) {
        validate(value != null && !value.isEmpty(), "%s is empty", paramName);
        return value;
    }

    public static void validate(boolean condition, String message, Object... params) {
        if (!condition) {
            throw new SpottyValidationException(message, params);
        }
    }

    public static boolean isNull(Object value) {
        return value == null;
    }

    public static boolean isNotNull(Object value) {
        return value != null;
    }

    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > ' ') {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
