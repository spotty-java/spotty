package spotty.common.http;

public enum ContentEncoding {
    GZIP, DEFLATE;

    public static ContentEncoding of(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
