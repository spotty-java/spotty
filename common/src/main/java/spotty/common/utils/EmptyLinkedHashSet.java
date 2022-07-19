package spotty.common.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Predicate;

public class EmptyLinkedHashSet extends LinkedHashSet<Object> {

    private static final EmptyLinkedHashSet EMPTY_LINKED_HASH_SET = new EmptyLinkedHashSet();

    @SuppressWarnings("unchecked")
    public static <T> LinkedHashSet<T> emptyLinkedHashSet() {
        return (LinkedHashSet<T>) EMPTY_LINKED_HASH_SET;
    }

    private EmptyLinkedHashSet() {
        super(0);
    }

    @Override
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super Object> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
