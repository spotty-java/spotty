package spotty.server.worker.action;

@FunctionalInterface
public interface ReactorHandler<T> {
    byte[] call(T data) throws Exception;
}
