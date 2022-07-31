package spotty.version;

import java.util.Objects;

import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.validate;

public final class Version implements Comparable<Version> {
    private final int major;
    private final int minor;
    private final int patch;

    private final String toString;
    private final int hash;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;

        this.toString = "v" + major + "." + minor + "." + patch;
        this.hash = Objects.hash(major, minor, patch);
    }

    public static Version parse(String version) {
        final String[] parts = notBlank("version", version).split("\\.");
        validate(parts.length == 3, "wrong version %s, must be 3 parts", version);
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            validate(isNumeric(part), "part %s is not a number %s", i + 1, part);
        }

        final int major = Integer.parseInt(parts[0]);
        final int minor = Integer.parseInt(parts[1]);
        final int patch = Integer.parseInt(parts[2]);

        return new Version(major, minor, patch);
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    @Override
    public int compareTo(Version version) {
        if (this.equals(version)) {
            return 0;
        }

        final int major = Integer.compare(this.major, version.major);
        if (major != 0) {
            return major;
        }

        final int minor = Integer.compare(this.minor, version.minor);
        if (minor != 0) {
            return minor;
        }

        return Integer.compare(this.patch, version.patch);
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    private static boolean isNumeric(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
