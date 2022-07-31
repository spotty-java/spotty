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
