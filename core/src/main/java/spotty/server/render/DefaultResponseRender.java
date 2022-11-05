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
package spotty.server.render;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class DefaultResponseRender implements ResponseRender {

    @Override
    public InputStream render(Object body) {
        if (body instanceof InputStream) {
            return (InputStream) body;
        }

        if (body instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) body);
        }

        return new ByteArrayInputStream(body.toString().getBytes(UTF_8));
    }

}
