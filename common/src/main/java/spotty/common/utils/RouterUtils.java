package spotty.common.utils;

import spotty.common.router.route.ParamName;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RouterUtils {
    public static final String REGEX = "(:\\w+?)(/|$)";
    public static final Pattern PATTERN = Pattern.compile(REGEX);

    public static final String PARAM_REPLACEMENT = "(?<name>[\\w\\*]+?)";
    public static final String ALL_REPLACEMENT = "(.*?)";

    public static Result compileMatcher(String pathTemplate) {
        final Matcher m = PATTERN.matcher(pathTemplate);

        String matcher = "^" + pathTemplate.replace("*", ALL_REPLACEMENT) + "$";
        final ArrayList<ParamName> params = new ArrayList<>();
        while (m.find()) {
            final String name = m.group(1);
            final ParamName paramName = new ParamName(name);
            params.add(paramName);

            matcher = matcher.replace(name, PARAM_REPLACEMENT.replace("name", paramName.groupName));
        }

        return new Result(params, Pattern.compile(matcher));
    }

    public static String normalizePath(String path) {
        return path.replaceAll(REGEX, "*$2");
    }

    public static class Result {
        public final ArrayList<ParamName> params;
        public final Pattern matcher;

        Result(ArrayList<ParamName> params, Pattern matcher) {
            this.params = params;
            this.matcher = matcher;
        }
    }

}
