package spotty.common.state;

public interface State<S extends Enum<S>> {
    S dependsOn();

    default boolean is(S state) {
        return this == state;
    }

    default boolean is(S state1, S state2) {
        return this.is(state1) || this.is(state2);
    }

    default boolean is(S state1, S state2, S state3) {
        return this.is(state1) || this.is(state2) || this.is(state3);
    }

    default boolean is(S state1, S state2, S state3, S state4) {
        return this.is(state1) || this.is(state2) || this.is(state3) || this.is(state4);
    }

    default boolean is(S state1, S state2, S state3, S state4, S state5) {
        return this.is(state1) || this.is(state2) || this.is(state3) || this.is(state4) || this.is(state5);
    }

    default boolean is(S... state) {
        for (S s : state) {
            if (this.is(s)) {
                return true;
            }
        }

        return false;
    }
}
