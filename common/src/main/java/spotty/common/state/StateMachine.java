package spotty.common.state;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.Validate.notNull;
import static org.apache.commons.lang3.Validate.validState;

public abstract class StateMachine<S extends Enum<S>> {
    private final Map<S, List<Consumer<S>>> subscribers = new HashMap<>();

    @NotNull
    private volatile S state;

    public StateMachine(@NotNull S state) {
        this.state = notNull(state, "state");
    }

    public S state() {
        return state;
    }

    public boolean is(S state) {
        return this.state == state;
    }

    public synchronized void whenStateIs(@NotNull S state, @NotNull Consumer<S> subscriber) {
        subscribers.computeIfAbsent(state, __ -> new ArrayList<>())
            .add(subscriber);
    }

    protected synchronized boolean changeState(@NotNull S newState) {
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

    public synchronized void checkStateIs(S from) {
        validState(is(from), "%s state must be %s, but is %s", getClass().getSimpleName(), from, state);
    }

    public synchronized void checkStateIsOneOf(S from1, S from2) {
        validState(is(from1) || is(from2), "%s state must be %s or %s, but is %s", getClass().getSimpleName(), from1, from2, state);
    }

    public synchronized void checkStateIsOneOf(S from1, S from2, S from3) {
        validState(is(from1) || is(from2) || is(from3), "%s state must be %s, %s or %s, but is %s", getClass().getSimpleName(), from1, from2, from3, state);
    }

    @SafeVarargs
    public synchronized final void checkStateIsOneOf(S first, S second, S... rest) {
        validState(asList(first, second, rest).contains(state), "%s state must be one of [%s, %s, %s], but is %s", getClass().getSimpleName(), first, second, join(rest, ", "), state);
    }

    public synchronized void checkStateIsOneOf(Collection<S> states) {
        validState(states.contains(state), "%s state must be one of %s, but is %s", getClass().getSimpleName(), states, state);
    }

}
