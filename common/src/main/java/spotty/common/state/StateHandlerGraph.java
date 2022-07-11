package spotty.common.state;

import spotty.common.exception.SpottyException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.apache.commons.lang3.Validate.notNull;

public final class StateHandlerGraph<S extends Enum<S>> {

    private final Map<S, Node> nodes = new HashMap<>();
    private final Map<S, Filter> filters = new HashMap<>();

    public void handleState(S state) {
        final Node node = nodes.get(state);
        if (node == null) {
            throw new SpottyException("node not found for state %s".formatted(state));
        }

        final Filter filter = filters.getOrDefault(state, Filter.EMPTY);
        if (filter.before()) {
            node.action();
            filter.after();
        }
    }

    public Function<Action, Node> entry(S state) {
        return action -> new Node(action, state);
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

    public final class Node {
        private final Action action;
        private Node next;

        private Node(Action action, S state) {
            this.action = notNull(action, "action");
            nodes.put(state, this);
        }

        public Function<Action, Node> entry(S state) {
            return action -> new Node(action, state);
        }

        public Function<Action, Node> node(S state) {
            return action -> next = new Node(action, state);
        }

        private void action() {
            if (this.action.run()) {
                nextAction();
            }
        }

        private void nextAction() {
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