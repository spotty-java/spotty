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
package spotty.server.files.detector;

import org.apache.tika.Tika;

import java.net.URL;
import java.util.function.Supplier;

import static spotty.common.utils.Memoized.lazy;

public final class FileTypeDetector implements TypeDetector {

    private static final Supplier<Tika> tika = lazy(() -> new Tika());

    @Override
    public String detect(URL path) throws Exception {
        return tika.get().detect(path);
    }

}
