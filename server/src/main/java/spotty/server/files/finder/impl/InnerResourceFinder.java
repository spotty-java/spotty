package spotty.server.files.finder.impl;

import spotty.server.files.finder.ResourceFinder;

import java.net.URL;

public final class InnerResourceFinder implements ResourceFinder {

    @Override
    public URL find(String filePath) throws Exception {
        return getClass().getResource(filePath);
    }

}
