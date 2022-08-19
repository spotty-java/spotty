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
package spotty.server.router;

import com.google.common.annotations.VisibleForTesting;
import spotty.common.http.HttpMethod;
import spotty.common.router.route.Route;
import spotty.common.router.route.RouteEntry;
import spotty.common.utils.RouterUtils.Result;

import static spotty.common.utils.RouterUtils.compileMatcher;
import static spotty.common.utils.RouterUtils.normalizePath;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;

@VisibleForTesting
final class RouteEntryFactory {

    static RouteEntry create(String pathTemplate, HttpMethod httpMethod, String acceptType, Route route) {
        notNull("pathTemplate", pathTemplate);
        notNull("httpMethod", httpMethod);
        notBlank("acceptType", acceptType);
        notNull("route", route);

        final Result result = compileMatcher(pathTemplate);

        return new RouteEntry()
            .pathTemplate(pathTemplate)
            .acceptType(acceptType)
            .httpMethod(httpMethod)
            .pathNormalized(normalizePath(pathTemplate))
            .matcher(result.matcher)
            .pathParamKeys(result.params)
            .route(route);
    }

}
