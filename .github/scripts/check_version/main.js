const path = require('path');
const properties = require('properties-file');
const Version = require('./version');

let masterPathToFile = path.resolve(__dirname, '../../../master-branch/gradle.properties');
let masterProperties = properties.propertiesToJson(masterPathToFile);

let currentPathToFile = path.resolve(__dirname, '../../../gradle.properties');
let currentProperties = properties.propertiesToJson(currentPathToFile);

let masterVersion = Version.parse(masterProperties.version)
let currentVersion = Version.parse(currentProperties.version)

if (currentVersion.compare(masterVersion) <= 0) {
    throw new Error(`current version ${currentVersion} must be greater then master ${masterVersion}`);
}

console.log(`current version ${currentVersion} is greater then master ${masterVersion}, all is good :)`)
