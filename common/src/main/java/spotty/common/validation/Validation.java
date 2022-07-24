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

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
