/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.common.state;

import spotty.common.exception.SpottyException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static spotty.common.validation.Validation.notNull;

public final class StateHandlerGraph<S extends Enum<S>> {

    private final Map<S, Node> nodes = new HashMap<>();
    private final Map<S, GraphFilter> filters = new HashMap<>();

    public void handleState(S state) {
        final Node node = nodes.get(state);
        if (node == null) {
            throw new SpottyException(format("node not found for state %s", state));
        }

        final GraphFilter filter = filters.getOrDefault(state, GraphFilter.EMPTY);
        if (filter.before()) {
            try {
                node.action();
            } finally {
                filter.after();
            }
        }
    }

    public Function<Action, Node> entry(S state, S... states) {
        return action -> new Node(action, state, states);
    }

    @SafeVarargs
    public final Function<GraphFilter, StateHandlerGraph<S>> filter(S... states) {
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

        private Node(Action action, S state, S... states) {
            this.action = notNull("action", action);
            nodes.put(state, this);

            for (S s : states) {
                nodes.put(s, this);
            }
        }

        public Function<Action, Node> entry(S state, S... states) {
            return action -> new Node(action, state, states);
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

    public interface GraphFilter {
        // if false then stop execution
        boolean before();

        void after();

        GraphFilter EMPTY = new GraphFilter() {
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
