package spotty.server.worker.action;

@FunctionalInterface
public interface ReactorAction {
    void call() throws Exception;
}
