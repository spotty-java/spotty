package spotty.common.state;

import spotty.common.exception.SpottyHttpException;

import static java.lang.String.format;
import static spotty.common.http.HttpStatus.INTERNAL_SERVER_ERROR;

public abstract class StateMachine<S extends Enum<S> & State<S>> {

    private S state;

    public StateMachine(S state) {
        this.state = state;
    }

    protected synchronized void stateTo(S newState) {
        if (state != newState) {
            if (state != newState.dependsOn() && newState.dependsOn() != null) {
                throw new SpottyHttpException(
                    INTERNAL_SERVER_ERROR,
                    format("newState %s dependsOn %s, but current state is %s", newState, newState.dependsOn(), state)
                );
            }

            state = newState;
        }
    }

    public S state() {
        return state;
    }

}
