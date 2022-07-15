package spotty.server.router.route;

import spotty.common.exception.SpottyException;
import spotty.common.http.HttpMethod;
import spotty.common.request.params.PathParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

public final class RouteEntry {
    public final String path;
    public final String pathNormalized;
    public final ArrayList<ParamName> pathParamKeys;
    public final HttpMethod httpMethod;
    public final Route route;
    public final Pattern matcher;

    public RouteEntry(Builder builder) {
        this.path = notBlank(builder.path, "path");
        this.pathNormalized = notBlank(builder.pathNormalized, "pathNormalized");
        this.pathParamKeys = notNull(builder.pathParamKeys, "pathParamKeys");
        this.httpMethod = notNull(builder.httpMethod, "httpMethod");
        this.route = notNull(builder.route, "route");
        this.matcher = notNull(builder.matcher, "matcher");
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean matches(String rawPath) {
        return matcher.matcher(rawPath).matches();
    }

    public PathParams parsePathParams(String rawPath) {
        final Map<String, String> pathParams = new HashMap<>();
        final Matcher match = matcher.matcher(rawPath);
        if (!match.find()) {
            throw new SpottyException("%s %s does not match with rout %s", httpMethod, rawPath, path);
        }

        pathParamKeys.forEach(param -> {
            pathParams.put(param.name, match.group(param.groupName));
        });

        return PathParams.of(pathParams);
    }

    public static class Builder {
        private String path;
        private String pathNormalized;
        private ArrayList<ParamName> pathParamKeys;
        private HttpMethod httpMethod;
        private Route route;
        private Pattern matcher;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder pathNormalized(String pathNormalized) {
            this.pathNormalized = pathNormalized;
            return this;
        }

        public Builder pathParamKeys(ArrayList<ParamName> pathParamKeys) {
            this.pathParamKeys = pathParamKeys;
            return this;
        }

        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder route(Route route) {
            this.route = route;
            return this;
        }

        public Builder matcher(Pattern matcher) {
            this.matcher = matcher;
            return this;
        }

        public RouteEntry build() {
            return new RouteEntry(this);
        }

    }

}
