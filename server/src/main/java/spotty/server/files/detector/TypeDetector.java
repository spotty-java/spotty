package spotty.server.files.detector;

import java.net.URL;

public interface TypeDetector {
    String detect(URL path) throws Exception;
}
