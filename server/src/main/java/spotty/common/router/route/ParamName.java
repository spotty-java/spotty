package spotty.common.router.route;

import java.util.Objects;

import static spotty.common.validation.Validation.notBlank;

public final class ParamName {
    public final String name;
    public final String groupName; // regex group name for parser

    public ParamName(String name) {
        this.name = notBlank("name", name).replace(":", "");
        this.groupName = normalizeGroupName(name);
    }

    private String normalizeGroupName(String name) {
        return name.replaceAll("[^0-9a-zA-Z]", "");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParamName paramName = (ParamName) o;
        return Objects.equals(name, paramName.name)
            && Objects.equals(groupName, paramName.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, groupName);
    }
}
