package spotty.server.files;

import com.google.common.annotations.VisibleForTesting;
import spotty.common.exception.SpottyNotFoundException;
import spotty.server.files.detector.FileTypeDetector;
import spotty.server.files.detector.TypeDetector;
import spotty.server.files.finder.ResourceFinder;
import spotty.server.files.finder.impl.ExternalResourceFinder;
import spotty.server.files.finder.impl.InnerResourceFinder;
import spotty.server.files.loader.FileLoader;
import spotty.server.files.loader.impl.CacheFileLoader;
import spotty.server.files.loader.impl.DefaultFileLoader;
import spotty.server.router.SpottyRouter;

import java.net.URL;

import static spotty.common.utils.RouterUtils.REGEX;
import static spotty.common.validation.Validation.notBlank;
import static spotty.common.validation.Validation.notNull;

public final class StaticFilesManager {
    private final TypeDetector typeDetector = new FileTypeDetector();
    private FileLoader fileLoader = new DefaultFileLoader(typeDetector);

    private final SpottyRouter router;

    public StaticFilesManager(SpottyRouter router) {
        this.router = notNull("router", router);
    }

    public void enableCache(long cacheTtl, long cacheSize) {
        this.fileLoader = new CacheFileLoader(typeDetector, cacheTtl, cacheSize);
    }

    public void staticFiles(String templatePath) {
        staticFiles("", templatePath);
    }

    public void staticFiles(String filesDir, String templatePath) {
        registerFilesRoute(new InnerResourceFinder(), filesDir, templatePath);
    }

    public void externalStaticFiles(String filesDir, String templatePath) {
        registerFilesRoute(new ExternalResourceFinder(), filesDir, templatePath);
    }

    private void registerFilesRoute(ResourceFinder resourceFinder, String filesDir, String templatePath) {
        notNull("fileFinder", resourceFinder);
        notNull("filesDir", filesDir);
        notBlank("templatePath", templatePath);

        if (!templatePath.endsWith("/*")) {
            templatePath += "/*";
        }

        final String path = templatePath;
        router.get(path, (request, response) -> {
            final URL file = resourceFinder.find(filesDir + getFilePath(path, request.path()));
            if (file == null) {
                throw new SpottyNotFoundException("file not found %s", request.path());
            }

            return fileLoader.loadFile(file, response);
        });
    }

    @VisibleForTesting
    String getFilePath(String templatePath, String urlPath) {
        final String path = templatePath.substring(0, templatePath.length() - 2) // remove /* at the end
            .replace("*", "(.+?)")
            .replaceAll(REGEX, "(.+?)");

        return urlPath.replaceFirst(path, "");
    }

}
