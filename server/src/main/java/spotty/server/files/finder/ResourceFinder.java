package spotty.server.files.finder;

import java.net.URL;

public interface ResourceFinder {
    URL find(String filePath) throws Exception;
}
