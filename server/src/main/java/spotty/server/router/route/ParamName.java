package spotty.server.router.route;

import lombok.EqualsAndHashCode;

import static org.apache.commons.lang3.Validate.notBlank;

@EqualsAndHashCode
public final class ParamName {
    public final String name;
    public final String groupName; // regex group name for parser

    public ParamName(String name) {
        this.name = notBlank(name, "name").replace(":", "");
        this.groupName = normalizeGroupName(name);
    }

    private String normalizeGroupName(String name) {
        return name.replaceAll("[^0-9a-zA-Z]", "");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }

}
