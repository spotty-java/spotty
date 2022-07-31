module.exports = class Version {
    major = null;
    minor = null;
    patch = null;

    constructor(major, minor, patch) {
        this.major = parseInt(major);
        this.minor = parseInt(minor);
        this.patch = parseInt(patch);
    }

    static parse(versionString) {
        let parts = versionString.split('.');
        if (parts.length !== 3) {
            throw new Error(`invalid version string ${versionString}`)
        }

        return new Version(parts[0], parts[1], parts[2]);
    }

    compare(version) {
        if (this.major === version.major && this.minor === version.minor && this.patch === version.patch) {
            return 0;
        }

        let major = compareInt(this.major, version.major);
        if (major !== 0) {
            return major;
        }

        let minor = compareInt(this.minor, version.minor);
        if (minor !== 0) {
            return minor;
        }

        return compareInt(this.patch, version.patch);
    }

    toString() {
        return `${this.major}.${this.minor}.${this.patch}`;
    }

}

function compareInt(a, b) {
    if (a === b) {
        return 0;
    }

    return a < b ? -1 : 1;
}
