package spotty.common.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static spotty.common.validation.Validation.notNull;

public abstract class StateMachine<S extends Enum<S>> {
    private final Map<S, List<Consumer<S>>> subscribers = new HashMap<>();

    private volatile S state;

    public StateMachine(S state) {
        this.state = notNull("state", state);
    }

    public S state() {
        return state;
    }

    public boolean is(S state) {
        return this.state == state;
    }

    public synchronized void whenStateIs(S state, Consumer<S> subscriber) {
        subscribers.computeIfAbsent(state, __ -> new ArrayList<>())
            .add(subscriber);
    }

    protected synchronized boolean changeState(S newState) {
        requireNonNull(newState, "newState must be not null");

        if (state != newState) {
            final S prevState = state;
            state = newState;

            final List<Consumer<S>> stateSubscribers = subscribers.getOrDefault(newState, emptyList());
            stateSubscribers.forEach(s -> s.accept(prevState));

            return true;
        }

        return false;
    }

}