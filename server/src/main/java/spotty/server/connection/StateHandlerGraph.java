package spotty.server.connection;

import spotty.common.exception.SpottyException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StateHandlerGraph<S extends Enum<S>> {

    private final Map<S, Node> nodes = new HashMap<>();
    private final Map<S, Filter> filters = new HashMap<>();

    public void handleState(S state) {
        final var handler = nodes.get(state);
        if (handler == null) {
            throw new SpottyException("handler not found for state %s".formatted(state));
        }

        final var filter = filters.getOrDefault(state, Filter.EMPTY);
        if (filter.before()) {
            handler.action();
            filter.after();
        }
    }

    @SafeVarargs
    public final Function<Action, Node> head(S... state) {
        return action -> createNode(action, state);
    }

    @SafeVarargs
    public final Function<Filter, StateHandlerGraph<S>> filter(S... states) {
        return filter -> {
            for (S state : states) {
                filters.put(state, filter);
            }

            return this;
        };
    }

    @SafeVarargs
    private Node createNode(Action action, S... state) {
        return new Node(state) {
            @Override
            public void action() {
                if (action.run()) {
                    nextAction();
                }
            }
        };
    }

    public abstract class Node {
        private Node next;

        @SafeVarargs
        private Node(S... states) {
            for (S state : states) {
                nodes.put(state, this);
            }
        }

        @SafeVarargs
        public final Function<Action, Node> head(S... state) {
            return action -> createNode(action, state);
        }

        @SafeVarargs
        public final Function<Action, Node> node(S... state) {
            return action -> next = createNode(action, state);
        }

        protected abstract void action();

        protected void nextAction() {
            if (next != null) {
                next.action();
            }
        }

    }

    @FunctionalInterface
    public interface Action {
        // if true run next linked action
        boolean run() throws SpottyException;
    }

    public interface Filter {
        // if false then stop execution
        boolean before();

        void after();

        Filter EMPTY = new Filter() {
            @Override
            public boolean before() {
                return true;
            }

            @Override
            public void after() {

            }
        };
    }

}
