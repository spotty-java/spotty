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
package spotty.common.router.route;

import spotty.common.exception.SpottyException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.common.request.params.PathParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.emptySet;

public final class RouteEntry {
    private String pathTemplate;
    private String pathNormalized;
    private ArrayList<ParamName> pathParamKeys; // ArrayList for optimization, because forEach uses fori
    private String acceptType;
    private HttpMethod httpMethod;
    private Route route;
    private Pattern matcher;
    private Set<Filter> beforeFilters = emptySet();
    private Set<Filter> afterFilters = emptySet();

    public boolean matches(String rawPath) {
        return matcher.matcher(rawPath).matches();
    }

    public PathParams parsePathParams(String rawPath) {
        final Map<String, String> pathParams = new HashMap<>();
        final Matcher match = matcher.matcher(rawPath);
        if (!match.find()) {
            throw new SpottyException("%s %s does not match with rout %s", httpMethod, rawPath, pathTemplate);
        }

        pathParamKeys.forEach(param -> {
            pathParams.put(param.name, match.group(param.groupName));
        });

        return PathParams.of(pathParams);
    }

    public String pathTemplate() {
        return pathTemplate;
    }

    public RouteEntry pathTemplate(String path) {
        this.pathTemplate = path;
        return this;
    }

    public String pathNormalized() {
        return pathNormalized;
    }

    public RouteEntry pathNormalized(String pathNormalized) {
        this.pathNormalized = pathNormalized;
        return this;
    }

    public boolean hasPathParamKeys() {
        return pathParamKeys != null && !pathParamKeys.isEmpty();
    }

    public ArrayList<ParamName> pathParamKeys() {
        return pathParamKeys;
    }

    public RouteEntry pathParamKeys(ArrayList<ParamName> pathParamKeys) {
        this.pathParamKeys = pathParamKeys;
        return this;
    }

    public String acceptType() {
        return acceptType;
    }

    public RouteEntry acceptType(String acceptType) {
        this.acceptType = acceptType;
        return this;
    }

    public HttpMethod httpMethod() {
        return httpMethod;
    }

    public RouteEntry httpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public Route route() {
        return route;
    }

    public RouteEntry route(Route route) {
        this.route = route;
        return this;
    }

    public Pattern matcher() {
        return matcher;
    }

    public RouteEntry matcher(Pattern matcher) {
        this.matcher = matcher;
        return this;
    }

    public Set<Filter> beforeFilters() {
        return beforeFilters;
    }

    public RouteEntry addBeforeFilters(Collection<Filter> beforeFilters) {
        if (this.beforeFilters == EMPTY_SET) {
            this.beforeFilters = new LinkedHashSet<>();
        }

        this.beforeFilters.addAll(beforeFilters);
        return this;
    }

    public Set<Filter> afterFilters() {
        return afterFilters;
    }

    public RouteEntry addAfterFilters(Collection<Filter> afterFilters) {
        if (this.afterFilters == EMPTY_SET) {
            this.afterFilters = new LinkedHashSet<>();
        }

        this.afterFilters.addAll(afterFilters);
        return this;
    }

}
