package spotty.common.state;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class StateMachine<S extends Enum<S>> {
    private final Map<S, List<Consumer<S>>> subscribers = new HashMap<>();

    @NotNull
    private volatile S state;

    public StateMachine(@NotNull S state) {
        this.state = state;
    }

    protected synchronized void changeState(@NotNull S newState) {
        if (state != newState) {
            final var prevState = state;
            state = newState;

            final var stateSubscribers = subscribers.getOrDefault(newState, List.of());
            stateSubscribers.forEach(s -> s.accept(prevState));
        }
    }

    public S state() {
        return state;
    }

    public synchronized void whenStateIs(@NotNull S state, @NotNull Consumer<S> subscriber) {
        subscribers.compute(state, (__, subs) -> {
            var stateSubscribers = subs;
            if (stateSubscribers == null) {
                stateSubscribers = new ArrayList<>();
            }

            stateSubscribers.add(subscriber);
            return stateSubscribers;
        });
    }

}
