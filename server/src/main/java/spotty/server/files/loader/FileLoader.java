package spotty.server.files.loader;

import spotty.common.response.SpottyResponse;

import java.net.URL;

public interface FileLoader {
    byte[] loadFile(URL file, SpottyResponse response) throws Exception;
}
