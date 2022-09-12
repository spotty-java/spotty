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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static spotty.common.validation.Validation.notNull;
import static spotty.common.validation.Validation.validate;

public abstract class StateMachine<S extends Enum<S>> {
    private final Map<S, List<Runnable>> subscribers = new HashMap<>();

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

    public boolean isNot(S state) {
        return this.state != state;
    }

    protected void checkStateIs(S from) {
        validate(is(from), "%s state must be %s, but is %s", getClass().getSimpleName(), from, state);
    }

    protected void checkStateIsOneOf(S from1, S from2) {
        validate(is(from1) || is(from2), "%s state must be %s or %s, but is %s", getClass().getSimpleName(), from1, from2, state);
    }

    public void whenStateIs(S state, Runnable subscriber) {
        subscribers.computeIfAbsent(state, __ -> new ArrayList<>())
            .add(subscriber);
    }

    protected boolean changeState(S newState) {
        notNull("newState", newState);

        if (state != newState) {
            state = newState;

            subscribers.getOrDefault(newState, emptyList())
                .forEach(Runnable::run);

            return true;
        }

        return false;
    }

}
