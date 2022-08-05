package spotty.common.utils;

@FunctionalInterface
public interface ExceptionalCallable<T> {
    T call() throws Exception;
}
