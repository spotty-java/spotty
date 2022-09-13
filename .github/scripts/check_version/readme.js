const fs = require('fs');

module.exports = function parse(readmeFilePath) {
    const data = fs.readFileSync(readmeFilePath, 'utf8');

    return {
        maven: mavenVersion(data),
        gradle: gradleVersion(data)
    }
}

function mavenVersion(fileContent) {
    const result = fileContent.match(/<version>(.*?)<\/version>/);
    if (!result[1]) {
        throw new Error("not found maven version in readme file")
    }

    return result[1];
}

function gradleVersion(fileContent) {
    const result = fileContent.match(/"com.spotty-server:core:(.*?)"/);
    if (!result[1]) {
        throw new Error("not found gradle version in readme file")
    }

    return result[1];
}