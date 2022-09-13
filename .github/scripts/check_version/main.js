const path = require('path');
const properties = require('properties-file');
const Version = require('./version');
const readmeParser = require('./readme');

const {currentVersion, masterVersion} = comparePropertiesFiles();
if (currentVersion.compare(masterVersion) <= 0) {
    throw new Error(`current gradle.properties version ${currentVersion} must be greater then master ${masterVersion}`);
}

const {masterMavenVersion, masterGradleVersion, currentMavenVersion, currentGradleVersion} = compareReadmeFiles();
if (currentMavenVersion.compare(currentGradleVersion) !== 0) {
    throw Error(`readme maven version ${currentMavenVersion} != readme gradle version ${currentGradleVersion}`);
}

if (currentVersion.compare(currentMavenVersion) !== 0) {
    throw Error(`properties version ${currentVersion} != readme version ${currentMavenVersion}`);
}

if (currentMavenVersion.compare(masterMavenVersion) <= 0) {
    throw new Error(`current readme maven version ${currentMavenVersion} must be greater then readme maven master ${masterMavenVersion}`);
}

if (currentGradleVersion.compare(masterGradleVersion) <= 0) {
    throw new Error(`current readme gradle version ${currentGradleVersion} must be greater then readme gradle master ${masterGradleVersion}`);
}

console.log(`current version ${currentVersion} is greater then master ${masterVersion}, all is good :)`);

function comparePropertiesFiles() {
    const masterPathToFile = path.resolve(__dirname, '../../../master-branch/gradle.properties');
    const masterProperties = properties.propertiesToJson(masterPathToFile);

    const currentPathToFile = path.resolve(__dirname, '../../../gradle.properties');
    const currentProperties = properties.propertiesToJson(currentPathToFile);

    const masterVersion = Version.parse(masterProperties.version);
    const currentVersion = Version.parse(currentProperties.version);

    return {
        currentVersion,
        masterVersion
    };
}

function compareReadmeFiles() {
    const masterReadmePathToFile = path.resolve(__dirname, '../../../master-branch/README.md');
    const {maven: masterMaven, gradle: masterGradle} = readmeParser(masterReadmePathToFile);

    const masterMavenVersion = Version.parse(masterMaven);
    const masterGradleVersion = Version.parse(masterGradle);

    const currentReadmePathToFile = path.resolve(__dirname, '../../../README.md');
    const {maven: currentMaven, gradle: currentGradle} = readmeParser(currentReadmePathToFile);

    const currentMavenVersion = Version.parse(currentMaven);
    const currentGradleVersion = Version.parse(currentGradle);

    return {
        masterMavenVersion,
        masterGradleVersion,
        currentMavenVersion,
        currentGradleVersion
    };
}
