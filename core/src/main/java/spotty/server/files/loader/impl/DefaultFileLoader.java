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
package spotty.server.files.loader.impl;

import spotty.common.response.SpottyResponse;
import spotty.common.utils.IOUtils;
import spotty.server.files.detector.TypeDetector;
import spotty.server.files.loader.FileLoader;

import java.net.URL;

import static spotty.common.validation.Validation.notNull;

public final class DefaultFileLoader implements FileLoader {

    private final TypeDetector typeDetector;

    public DefaultFileLoader(TypeDetector typeDetector) {
        this.typeDetector = notNull("typeDetector", typeDetector);
    }

    @Override
    public byte[] loadFile(URL file, SpottyResponse response) throws Exception {
        notNull("file", file);
        notNull("response", response);

        response.contentType(typeDetector.detect(file));
        return IOUtils.toByteArray(file);
    }

}
