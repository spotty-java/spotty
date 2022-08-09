package spotty.common.utils;

@FunctionalInterface
public interface ExceptionalRunnable {
    void run() throws Exception;
}
