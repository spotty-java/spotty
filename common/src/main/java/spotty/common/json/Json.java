package spotty.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.InputStream;

public final class Json {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public static <T> T parse(byte[] content, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    @SneakyThrows
    public static <T> T parse(String content, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    @SneakyThrows
    public static <T> T parse(InputStream content, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(content, clazz);
    }

    @SneakyThrows
    public static JsonNode parse(byte[] content) {
        return OBJECT_MAPPER.readTree(content);
    }

    @SneakyThrows
    public static JsonNode parse(String content) {
        return OBJECT_MAPPER.readTree(content);
    }

    @SneakyThrows
    public static JsonNode parse(InputStream content) {
        return OBJECT_MAPPER.readTree(content);
    }
}
