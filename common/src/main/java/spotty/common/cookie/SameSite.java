package spotty.common.cookie;

public enum SameSite {
    STRICT("strict"), LAX("lax"), NONE("none");

    public final String code;

    SameSite(String code) {
        this.code = code;
    }
}
