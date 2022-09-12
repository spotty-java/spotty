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
package spotty.common.utils;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Memoized {

    private Memoized() {

    }

    public static <T> Supplier<T> lazy(Supplier<T> supplier) {
        return new Supplier<T>() {
            private T memoized;

            @Override
            public T get() {
                if (memoized == null) {
                    memoized = supplier.get();
                }

                return memoized;
            }
        };
    }

    public static IntSupplier lazy(IntSupplier supplier) {
        return new IntSupplier() {
            private boolean isMemoized = false;
            private int value;

            @Override
            public int getAsInt() {
                if (isMemoized) {
                    return value;
                }

                isMemoized = true;
                return value = supplier.getAsInt();
            }
        };
    }

}
