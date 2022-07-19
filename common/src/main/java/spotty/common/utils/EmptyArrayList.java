package spotty.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class EmptyArrayList extends ArrayList<Object> {

    private static final EmptyArrayList EMPTY_ARRAY_LIST = new EmptyArrayList();

    @SuppressWarnings("unchecked")
    public static <E> ArrayList<E> emptyArrayList() {
        return (ArrayList<E>) EMPTY_ARRAY_LIST;
    }

    private EmptyArrayList() {
        super(0);
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Object e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Object> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Object> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(int index) {
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

    @Override
    public boolean removeIf(Predicate<? super Object> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<Object> operator) {
        throw new UnsupportedOperationException();
    }
}
