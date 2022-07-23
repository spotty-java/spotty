package spotty.server.render;

@FunctionalInterface
public interface ResponseRender {
    byte[] render(Object body);
}
