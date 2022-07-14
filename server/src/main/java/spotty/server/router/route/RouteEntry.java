package spotty.server.router.route;

import lombok.Builder;
import spotty.common.http.HttpMethod;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Builder
public final class RouteEntry {
    public final String path;
    public final String pathNormalized;
    public final ArrayList<String> params;
    public final HttpMethod httpMethod;
    public final Route route;
    public final Pattern matcher;

    public boolean matches(String rawPath) {
        return matcher.matcher(rawPath).matches();
    }

}
