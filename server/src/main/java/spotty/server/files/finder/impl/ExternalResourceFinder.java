package spotty.server.files.finder.impl;

import spotty.server.files.finder.ResourceFinder;

import java.io.File;
import java.net.URL;

public final class ExternalResourceFinder implements ResourceFinder {

    @Override
    public URL find(String filePath) throws Exception {
        final File resource = new File(filePath);
        if (resource.exists()) {
            return resource.toURI().toURL();
        }

        return null;
    }

}
