package com.sun.tools.javac.platform;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import javax.annotation.processing.Processor;
import com.sun.source.util.Plugin;
import com.sun.tools.javac.jvm.Target;

public class JDKPlatformProvider implements PlatformProvider {

    @Override
    public Iterable<String> getSupportedPlatformNames() {
        return SUPPORTED_JAVA_PLATFORM_VERSIONS;
    }

    @Override
    public PlatformDescription getPlatform(String platformName, String options) {
        return new PlatformDescriptionImpl(platformName);
    }

    private static final String[] symbolFileLocation = { "lib", "ct.sym" };

    private static final Set<String> SUPPORTED_JAVA_PLATFORM_VERSIONS;

    static {
        SUPPORTED_JAVA_PLATFORM_VERSIONS = new TreeSet<>();
        Path ctSymFile = findCtSym();
        if (Files.exists(ctSymFile)) {
            try (FileSystem fs = FileSystems.newFileSystem(ctSymFile, null);
                DirectoryStream<Path> dir = Files.newDirectoryStream(fs.getRootDirectories().iterator().next())) {
                for (Path section : dir) {
                    for (char ver : section.getFileName().toString().toCharArray()) {
                        String verString = Character.toString(ver);
                        Target t = Target.lookup(verString);
                        if (t != null) {
                            SUPPORTED_JAVA_PLATFORM_VERSIONS.add(targetNumericVersion(t));
                        }
                    }
                }
            } catch (IOException | ProviderNotFoundException ex) {
            }
        }
<<<<<<< MINE
=======
        SUPPORTED_JAVA_PLATFORM_VERSIONS.add(targetNumericVersion(Target.JDK1_9));
        SUPPORTED_JAVA_PLATFORM_VERSIONS.add(targetNumericVersion(Target.DEFAULT));
>>>>>>> YOURS
    }

    private static String targetNumericVersion(Target target) {
        return Integer.toString(target.ordinal() - Target.JDK1_1.ordinal() + 1);
    }

    static class PlatformDescriptionImpl implements PlatformDescription {

        private final Map<Path, FileSystem> ctSym2FileSystem = new HashMap<>();

        private final String version;

        PlatformDescriptionImpl(String version) {
            this.version = version;
        }

        @Override
        public Collection<Path> getPlatformPath() {
<<<<<<< MINE
=======
            if (Target.lookup(version).compareTo(Target.JDK1_9) >= 0) {
                return null;
            }
>>>>>>> YOURS
            List<Path> paths = new ArrayList<>();
            Path file = findCtSym();
            if (Files.exists(file)) {
                FileSystem fs = ctSym2FileSystem.get(file);
                if (fs == null) {
                    try {
                        ctSym2FileSystem.put(file, fs = FileSystems.newFileSystem(file, null));
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                Path root = fs.getRootDirectories().iterator().next();
                try (DirectoryStream<Path> dir = Files.newDirectoryStream(root)) {
                    for (Path section : dir) {
                        if (section.getFileName().toString().contains(version)) {
                            Path systemModules = section.resolve("system-modules");
                            if (Files.isRegularFile(systemModules)) {
                                Path modules = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("modules");
                                try (Stream<String> lines = Files.lines(systemModules, Charset.forName("UTF-8"))) {
                                    lines.map(line -> modules.resolve(line)).filter(mod -> Files.exists(mod)).forEach(mod -> paths.add(mod));
                                }
                            } else {
                                paths.add(section);
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            } else {
                throw new IllegalStateException("Cannot find ct.sym!");
            }
            return paths;
        }

        @Override
        public String getSourceVersion() {
            return version;
        }

        @Override
        public String getTargetVersion() {
            return version;
        }

        @Override
        public List<PluginInfo<Processor>> getAnnotationProcessors() {
            return Collections.emptyList();
        }

        @Override
        public List<PluginInfo<Plugin>> getPlugins() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getAdditionalOptions() {
            return Collections.emptyList();
        }

        @Override
        public void close() throws IOException {
            for (FileSystem fs : ctSym2FileSystem.values()) {
                fs.close();
            }
            ctSym2FileSystem.clear();
        }
    }

    static Path findCtSym() {
        String javaHome = System.getProperty("java.home");
        Path file = Paths.get(javaHome);
        for (String name : symbolFileLocation) file = file.resolve(name);
        return file;
    }
}
