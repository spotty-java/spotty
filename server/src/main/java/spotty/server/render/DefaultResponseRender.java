package spotty.server.render;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DefaultResponseRender implements ResponseRender {

    @Override
    public byte[] render(Object body) {
        if (body instanceof byte[]) {
            return (byte[]) body;
        }

        return body.toString().getBytes(UTF_8);
    }

}
