package spotty.server.router.route;

import spotty.common.exception.SpottyException;
import spotty.common.filter.Filter;
import spotty.common.http.HttpMethod;
import spotty.common.request.params.PathParams;
import spotty.common.utils.EmptyLinkedHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spotty.common.utils.EmptyLinkedHashSet.emptyLinkedHashSet;

public final class RouteEntry {
    private String pathTemplate;
    private String pathNormalized;
    private ArrayList<ParamName> pathParamKeys; // ArrayList for optimization, because forEach uses fori
    private HttpMethod httpMethod;
    private Route route;
    private Pattern matcher;
    private Set<Filter> beforeFilters = emptyLinkedHashSet();
    private Set<Filter> afterFilters = emptyLinkedHashSet();

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

    public ArrayList<ParamName> pathParamKeys() {
        return pathParamKeys;
    }

    public RouteEntry pathParamKeys(ArrayList<ParamName> pathParamKeys) {
        this.pathParamKeys = pathParamKeys;
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
        if (this.beforeFilters.getClass() == EmptyLinkedHashSet.class) {
            this.beforeFilters = new LinkedHashSet<>();
        }

        this.beforeFilters.addAll(beforeFilters);
        return this;
    }

    public Set<Filter> afterFilters() {
        return afterFilters;
    }

    public RouteEntry addAfterFilters(Collection<Filter> afterFilters) {
        if (this.afterFilters.getClass() == EmptyLinkedHashSet.class) {
            this.afterFilters = new LinkedHashSet<>();
        }

        this.afterFilters.addAll(afterFilters);
        return this;
    }

}
