package spotty.server.files.loader.impl;

import spotty.common.response.SpottyResponse;
import spotty.common.utils.IOUtils;
import spotty.server.files.detector.TypeDetector;
import spotty.server.files.loader.FileLoader;

import java.net.URL;

import static spotty.common.validation.Validation.notNull;

public final class DefaultFileLoader implements FileLoader {

    private final TypeDetector typeDetector;

    public DefaultFileLoader(TypeDetector typeDetector) {
        this.typeDetector = notNull("typeDetector", typeDetector);
    }

    @Override
    public byte[] loadFile(URL file, SpottyResponse response) throws Exception {
        notNull("file", file);
        notNull("response", response);

        response.contentType(typeDetector.detect(file));
        return IOUtils.toByteArray(file);
    }

}
