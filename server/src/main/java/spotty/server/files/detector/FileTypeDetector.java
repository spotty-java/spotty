package spotty.server.files.detector;

import org.apache.tika.Tika;

import java.net.URL;

public final class FileTypeDetector implements TypeDetector {

    private final Tika tika = new Tika();

    @Override
    public String detect(URL path) throws Exception {
        return tika.detect(path);
    }

}
