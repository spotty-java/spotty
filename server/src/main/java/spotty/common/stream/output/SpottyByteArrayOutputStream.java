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
package spotty.common.stream.output;

import static java.lang.Math.max;

/**
 * Single thread use only
 */
public final class SpottyByteArrayOutputStream extends SpottyFixedByteOutputStream {
    private static final int DEFAULT_SIZE = 1024;

    private final int initialBufferSize;

    public SpottyByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    public SpottyByteArrayOutputStream(int size) {
        super(size);

        this.initialBufferSize = size;
    }

    @Override
    protected void ensureCapacity(int len) {
        if (len > remaining()) {
            grow(size() + len);
        }
    }

    @Override
    public void limit(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        capacity(initialBufferSize);

        super.reset();
    }

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        int newCapacity = max(capacity() << 1, minCapacity);
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);

        capacity(newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();

        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

}
