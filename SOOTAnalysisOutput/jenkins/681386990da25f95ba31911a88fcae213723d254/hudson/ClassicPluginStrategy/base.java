package hudson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.util.AntWithFindResourceClassLoader;
import jenkins.util.SystemProperties;
import com.google.common.collect.Lists;
import hudson.Plugin.DummyImpl;
import hudson.PluginWrapper.Dependency;
import hudson.model.Hudson;
import jenkins.util.AntClassLoader;
import hudson.util.CyclicGraphDetector;
import hudson.util.CyclicGraphDetector.CycleDetectedException;
import hudson.util.IOUtils;
import hudson.util.MaskingClassLoader;
import hudson.util.VersionNumber;
import jenkins.ClassLoaderReflectionToolkit;
import jenkins.ExtensionFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.resources.MappedResourceCollection;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jenkinsci.bytecode.Transformer;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import javax.annotation.Nonnull;
import static org.apache.commons.io.FilenameUtils.getBaseName;

public class ClassicPluginStrategy implements PluginStrategy {

    private static final FilenameFilter JAR_FILTER = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };

    private PluginManager pluginManager;

    private final MaskingClassLoader coreClassLoader = new MaskingClassLoader(getClass().getClassLoader());

    public ClassicPluginStrategy(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public String getShortName(File archive) throws IOException {
        Manifest manifest;
        if (!archive.exists()) {
            throw new FileNotFoundException("Failed to load " + archive + ". The file does not exist");
        } else if (!archive.isFile()) {
            throw new FileNotFoundException("Failed to load " + archive + ". It is not a file");
        }
        if (isLinked(archive)) {
            manifest = loadLinkedManifest(archive);
        } else {
            try (JarFile jf = new JarFile(archive, false)) {
                manifest = jf.getManifest();
            } catch (IOException ex) {
                throw new IOException("Failed to load " + archive, ex);
            }
        }
        return PluginWrapper.computeShortName(manifest, archive.getName());
    }

    private static boolean isLinked(File archive) {
        return archive.getName().endsWith(".hpl") || archive.getName().endsWith(".jpl");
    }

    private static Manifest loadLinkedManifest(File archive) throws IOException {
        try {
            String firstLine;
            try (InputStream manifestHeaderInput = Files.newInputStream(archive.toPath())) {
                firstLine = IOUtils.readFirstLine(manifestHeaderInput, "UTF-8");
            } catch (InvalidPathException e) {
                throw new IOException(e);
            }
            if (firstLine.startsWith("Manifest-Version:")) {
            } else {
                archive = resolve(archive, firstLine);
            }
            try (InputStream manifestInput = Files.newInputStream(archive.toPath())) {
                return new Manifest(manifestInput);
            } catch (InvalidPathException e) {
                throw new IOException(e);
            }
        } catch (IOException e) {
            throw new IOException("Failed to load " + archive, e);
        }
    }

    @Override
    public PluginWrapper createPluginWrapper(File archive) throws IOException {
        final Manifest manifest;
        URL baseResourceURL = null;
        File expandDir = null;
        boolean isLinked = isLinked(archive);
        if (isLinked) {
            manifest = loadLinkedManifest(archive);
        } else {
            if (archive.isDirectory()) {
                expandDir = archive;
            } else {
                File f = pluginManager.getWorkDir();
                expandDir = new File(f == null ? archive.getParentFile() : f, getBaseName(archive.getName()));
                explode(archive, expandDir);
            }
            File manifestFile = new File(expandDir, PluginWrapper.MANIFEST_FILENAME);
            if (!manifestFile.exists()) {
                throw new IOException("Plugin installation failed. No manifest at " + manifestFile);
            }
            try (InputStream fin = Files.newInputStream(manifestFile.toPath())) {
                manifest = new Manifest(fin);
            } catch (InvalidPathException e) {
                throw new IOException(e);
            }
        }
        final Attributes atts = manifest.getMainAttributes();
        List<File> paths = new ArrayList<File>();
        if (isLinked) {
            parseClassPath(manifest, archive, paths, "Libraries", ",");
            parseClassPath(manifest, archive, paths, "Class-Path", " +");
            baseResourceURL = resolve(archive, atts.getValue("Resource-Path")).toURI().toURL();
        } else {
            File classes = new File(expandDir, "WEB-INF/classes");
            if (classes.exists()) {
                LOGGER.log(Level.WARNING, "Deprecated unpacked classes directory found in {0}", classes);
                paths.add(classes);
            }
            File lib = new File(expandDir, "WEB-INF/lib");
            File[] libs = lib.listFiles(JAR_FILTER);
            if (libs != null)
                paths.addAll(Arrays.asList(libs));
            baseResourceURL = expandDir.toPath().toUri().toURL();
        }
        File disableFile = new File(archive.getPath() + ".disabled");
        if (disableFile.exists()) {
            LOGGER.info("Plugin " + archive.getName() + " is disabled");
        }
        List<PluginWrapper.Dependency> dependencies = new ArrayList<PluginWrapper.Dependency>();
        List<PluginWrapper.Dependency> optionalDependencies = new ArrayList<PluginWrapper.Dependency>();
        String v = atts.getValue("Plugin-Dependencies");
        if (v != null) {
            for (String s : v.split(",")) {
                PluginWrapper.Dependency d = new PluginWrapper.Dependency(s);
                if (d.optional) {
                    optionalDependencies.add(d);
                } else {
                    dependencies.add(d);
                }
            }
        }
        fix(atts, optionalDependencies);
        String masked = atts.getValue("Global-Mask-Classes");
        if (masked != null) {
            for (String pkg : masked.trim().split("[ \t\r\n]+")) coreClassLoader.add(pkg);
        }
        ClassLoader dependencyLoader = new DependencyClassLoader(coreClassLoader, archive, Util.join(dependencies, optionalDependencies));
        dependencyLoader = getBaseClassLoader(atts, dependencyLoader);
        return new PluginWrapper(pluginManager, archive, manifest, baseResourceURL, createClassLoader(paths, dependencyLoader, atts), disableFile, dependencies, optionalDependencies);
    }

    private static void fix(Attributes atts, List<PluginWrapper.Dependency> optionalDependencies) {
        String pluginName = atts.getValue("Short-Name");
        String jenkinsVersion = atts.getValue("Jenkins-Version");
        if (jenkinsVersion == null)
            jenkinsVersion = atts.getValue("Hudson-Version");
        optionalDependencies.addAll(getImpliedDependencies(pluginName, jenkinsVersion));
    }

    @Nonnull
    public static List<PluginWrapper.Dependency> getImpliedDependencies(String pluginName, String jenkinsVersion) {
        List<PluginWrapper.Dependency> out = new ArrayList<>();
        for (DetachedPlugin detached : DETACHED_LIST) {
            if (detached.shortName.equals(pluginName)) {
                continue;
            }
            if (BREAK_CYCLES.contains(pluginName + ' ' + detached.shortName)) {
                LOGGER.log(Level.FINE, "skipping implicit dependency {0} → {1}", new Object[] { pluginName, detached.shortName });
                continue;
            }
            if (jenkinsVersion == null || jenkinsVersion.equals("null") || new VersionNumber(jenkinsVersion).compareTo(detached.splitWhen) <= 0) {
                out.add(new PluginWrapper.Dependency(detached.shortName + ':' + detached.requiredVersion));
                LOGGER.log(Level.FINE, "adding implicit dependency {0} → {1} because of {2}", new Object[] { pluginName, detached.shortName, jenkinsVersion });
            }
        }
        return out;
    }

    @Deprecated
    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent) throws IOException {
        return createClassLoader(paths, parent, null);
    }

    protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent, Attributes atts) throws IOException {
        if (atts != null) {
            String usePluginFirstClassLoader = atts.getValue("PluginFirstClassLoader");
            if (Boolean.valueOf(usePluginFirstClassLoader)) {
                PluginFirstClassLoader classLoader = new PluginFirstClassLoader();
                classLoader.setParentFirst(false);
                classLoader.setParent(parent);
                classLoader.addPathFiles(paths);
                return classLoader;
            }
        }
        AntClassLoader2 classLoader = new AntClassLoader2(parent);
        classLoader.addPathFiles(paths);
        return classLoader;
    }

    @Restricted(NoExternalUse.class)
    @Nonnull
    public static List<DetachedPlugin> getDetachedPlugins() {
        return DETACHED_LIST;
    }

    @Restricted(NoExternalUse.class)
    @Nonnull
    public static List<DetachedPlugin> getDetachedPlugins(@Nonnull VersionNumber since) {
        List<DetachedPlugin> detachedPlugins = new ArrayList<>();
        for (DetachedPlugin detachedPlugin : DETACHED_LIST) {
            if (!detachedPlugin.getSplitWhen().isOlderThan(since)) {
                detachedPlugins.add(detachedPlugin);
            }
        }
        return detachedPlugins;
    }

    @Restricted(NoExternalUse.class)
    public static boolean isDetachedPlugin(@Nonnull String pluginId) {
        for (DetachedPlugin detachedPlugin : DETACHED_LIST) {
            if (detachedPlugin.getShortName().equals(pluginId)) {
                return true;
            }
        }
        return false;
    }

    @Restricted(NoExternalUse.class)
    public static final class DetachedPlugin {

        private final String shortName;

        private final VersionNumber splitWhen;

        private final String requiredVersion;

        private DetachedPlugin(String shortName, String splitWhen, String requiredVersion) {
            this.shortName = shortName;
            this.splitWhen = new VersionNumber(splitWhen);
            this.requiredVersion = requiredVersion;
        }

        public String getShortName() {
            return shortName;
        }

        public VersionNumber getSplitWhen() {
            return splitWhen;
        }

        public VersionNumber getRequiredVersion() {
            return new VersionNumber(requiredVersion);
        }

        @Override
        public String toString() {
            return shortName + " " + splitWhen.toString().replace(".*", "") + " " + requiredVersion;
        }
    }

    private static final List<DetachedPlugin> DETACHED_LIST;

    private static final Set<String> BREAK_CYCLES;

    static {
        try (InputStream is = ClassicPluginStrategy.class.getResourceAsStream("/jenkins/split-plugins.txt")) {
            DETACHED_LIST = ImmutableList.copyOf(configLines(is).map(line -> {
                String[] pieces = line.split(" ");
                return new DetachedPlugin(pieces[0], pieces[1] + ".*", pieces[2]);
            }).collect(Collectors.toList()));
        } catch (IOException x) {
            throw new ExceptionInInitializerError(x);
        }
        try (InputStream is = ClassicPluginStrategy.class.getResourceAsStream("/jenkins/split-plugin-cycles.txt")) {
            BREAK_CYCLES = ImmutableSet.copyOf(configLines(is).collect(Collectors.toSet()));
        } catch (IOException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    private static Stream<String> configLines(InputStream is) throws IOException {
        return org.apache.commons.io.IOUtils.readLines(is, StandardCharsets.UTF_8).stream().filter(line -> !line.matches("#.*|\\s*"));
    }

    private ClassLoader getBaseClassLoader(Attributes atts, ClassLoader base) {
        String masked = atts.getValue("Mask-Classes");
        if (masked != null)
            base = new MaskingClassLoader(base, masked.trim().split("[ \t\r\n]+"));
        return base;
    }

    public void initializeComponents(PluginWrapper plugin) {
    }

    public <T> List<ExtensionComponent<T>> findComponents(Class<T> type, Hudson hudson) {
        List<ExtensionFinder> finders;
        if (type == ExtensionFinder.class) {
            finders = Collections.<ExtensionFinder>singletonList(new ExtensionFinder.Sezpoz());
        } else {
            finders = hudson.getExtensionList(ExtensionFinder.class);
        }
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.log(Level.FINER, "Scout-loading ExtensionList: " + type, new Throwable());
        for (ExtensionFinder finder : finders) {
            finder.scout(type, hudson);
        }
        List<ExtensionComponent<T>> r = Lists.newArrayList();
        for (ExtensionFinder finder : finders) {
            try {
                r.addAll(finder.find(type, hudson));
            } catch (AbstractMethodError e) {
                for (T t : finder.findExtensions(type, hudson)) r.add(new ExtensionComponent<T>(t));
            }
        }
        List<ExtensionComponent<T>> filtered = Lists.newArrayList();
        for (ExtensionComponent<T> e : r) {
            if (ExtensionFilter.isAllowed(type, e))
                filtered.add(e);
        }
        return filtered;
    }

    public void load(PluginWrapper wrapper) throws IOException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(wrapper.classLoader);
        try {
            String className = wrapper.getPluginClass();
            if (className == null) {
                wrapper.setPlugin(new DummyImpl());
            } else {
                try {
                    Class<?> clazz = wrapper.classLoader.loadClass(className);
                    Object o = clazz.newInstance();
                    if (!(o instanceof Plugin)) {
                        throw new IOException(className + " doesn't extend from hudson.Plugin");
                    }
                    wrapper.setPlugin((Plugin) o);
                } catch (LinkageError | ClassNotFoundException e) {
                    throw new IOException("Unable to load " + className + " from " + wrapper.getShortName(), e);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new IOException("Unable to create instance of " + className + " from " + wrapper.getShortName(), e);
                }
            }
            try {
                Plugin plugin = wrapper.getPlugin();
                plugin.setServletContext(pluginManager.context);
                startPlugin(wrapper);
            } catch (Throwable t) {
                throw new IOException("Failed to initialize", t);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void startPlugin(PluginWrapper plugin) throws Exception {
        plugin.getPlugin().start();
    }

    @Override
    public void updateDependency(PluginWrapper depender, PluginWrapper dependee) {
        DependencyClassLoader classLoader = findAncestorDependencyClassLoader(depender.classLoader);
        if (classLoader != null) {
            classLoader.updateTransientDependencies();
            LOGGER.log(Level.INFO, "Updated dependency of {0}", depender.getShortName());
        }
    }

    private DependencyClassLoader findAncestorDependencyClassLoader(ClassLoader classLoader) {
        for (; classLoader != null; classLoader = classLoader.getParent()) {
            if (classLoader instanceof DependencyClassLoader) {
                return (DependencyClassLoader) classLoader;
            }
            if (classLoader instanceof AntClassLoader) {
                DependencyClassLoader ret = findAncestorDependencyClassLoader(((AntClassLoader) classLoader).getConfiguredParent());
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    private static File resolve(File base, String relative) {
        File rel = new File(relative);
        if (rel.isAbsolute())
            return rel;
        else
            return new File(base.getParentFile(), relative);
    }

    private static void parseClassPath(Manifest manifest, File archive, List<File> paths, String attributeName, String separator) throws IOException {
        String classPath = manifest.getMainAttributes().getValue(attributeName);
        if (classPath == null)
            return;
        for (String s : classPath.split(separator)) {
            File file = resolve(archive, s);
            if (file.getName().contains("*")) {
                FileSet fs = new FileSet();
                File dir = file.getParentFile();
                fs.setDir(dir);
                fs.setIncludes(file.getName());
                for (String included : fs.getDirectoryScanner(new Project()).getIncludedFiles()) {
                    paths.add(new File(dir, included));
                }
            } else {
                if (!file.exists())
                    throw new IOException("No such file: " + file);
                paths.add(file);
            }
        }
    }

    private static void explode(File archive, File destDir) throws IOException {
        destDir.mkdirs();
        File explodeTime = new File(destDir, ".timestamp2");
        if (explodeTime.exists() && explodeTime.lastModified() == archive.lastModified())
            return;
        Util.deleteRecursive(destDir);
        try {
            Project prj = new Project();
            unzipExceptClasses(archive, destDir, prj);
            createClassJarFromWebInfClasses(archive, destDir, prj);
        } catch (BuildException x) {
            throw new IOException("Failed to expand " + archive, x);
        }
        try {
            new FilePath(explodeTime).touch(archive.lastModified());
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private static void createClassJarFromWebInfClasses(File archive, File destDir, Project prj) throws IOException {
        File classesJar = new File(destDir, "WEB-INF/lib/classes.jar");
        ZipFileSet zfs = new ZipFileSet();
        zfs.setProject(prj);
        zfs.setSrc(archive);
        zfs.setIncludes("WEB-INF/classes/");
        MappedResourceCollection mapper = new MappedResourceCollection();
        mapper.add(zfs);
        GlobPatternMapper gm = new GlobPatternMapper();
        gm.setFrom("WEB-INF/classes/*");
        gm.setTo("*");
        mapper.add(gm);
        final long dirTime = archive.lastModified();
        try (ZipOutputStream wrappedZOut = new ZipOutputStream(new NullOutputStream()) {

            @Override
            public void putNextEntry(ZipEntry ze) throws IOException {
                ze.setTime(dirTime + 1999);
                super.putNextEntry(ze);
            }
        }) {
            Zip z = new Zip() {

                protected void zipDir(Resource dir, ZipOutputStream zOut, String vPath, int mode, ZipExtraField[] extra) throws IOException {
                    super.zipDir(dir, wrappedZOut, vPath, mode, extra);
                }
            };
            z.setProject(prj);
            z.setTaskType("zip");
            classesJar.getParentFile().mkdirs();
            z.setDestFile(classesJar);
            z.add(mapper);
            z.execute();
        }
        if (classesJar.isFile()) {
            LOGGER.log(Level.WARNING, "Created {0}; update plugin to a version created with a newer harness", classesJar);
        }
    }

    private static void unzipExceptClasses(File archive, File destDir, Project prj) {
        Expand e = new Expand();
        e.setProject(prj);
        e.setTaskType("unzip");
        e.setSrc(archive);
        e.setDest(destDir);
        PatternSet p = new PatternSet();
        p.setExcludes("WEB-INF/classes/");
        e.addPatternset(p);
        e.execute();
    }

    final class DependencyClassLoader extends ClassLoader {

        private final File _for;

        private List<Dependency> dependencies;

        private volatile List<PluginWrapper> transientDependencies;

        public DependencyClassLoader(ClassLoader parent, File archive, List<Dependency> dependencies) {
            super(parent);
            this._for = archive;
            this.dependencies = dependencies;
        }

        private void updateTransientDependencies() {
            transientDependencies = null;
        }

        private List<PluginWrapper> getTransitiveDependencies() {
            if (transientDependencies == null) {
                CyclicGraphDetector<PluginWrapper> cgd = new CyclicGraphDetector<PluginWrapper>() {

                    @Override
                    protected List<PluginWrapper> getEdges(PluginWrapper pw) {
                        List<PluginWrapper> dep = new ArrayList<PluginWrapper>();
                        for (Dependency d : pw.getDependencies()) {
                            PluginWrapper p = pluginManager.getPlugin(d.shortName);
                            if (p != null && p.isActive())
                                dep.add(p);
                        }
                        return dep;
                    }
                };
                try {
                    for (Dependency d : dependencies) {
                        PluginWrapper p = pluginManager.getPlugin(d.shortName);
                        if (p != null && p.isActive())
                            cgd.run(Collections.singleton(p));
                    }
                } catch (CycleDetectedException e) {
                    throw new AssertionError(e);
                }
                transientDependencies = cgd.getSorted();
            }
            return transientDependencies;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    try {
                        Class<?> c = ClassLoaderReflectionToolkit._findLoadedClass(pw.classLoader, name);
                        if (c != null)
                            return c;
                        return ClassLoaderReflectionToolkit._findClass(pw.classLoader, name);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            } else {
                for (Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null) {
                        try {
                            return p.classLoader.loadClass(name);
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                }
            }
            throw new ClassNotFoundException(name);
        }

        @Override
        @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "Should not produce network overheads since the URL is local. JENKINS-53793 is a follow-up")
        protected Enumeration<URL> findResources(String name) throws IOException {
            HashSet<URL> result = new HashSet<URL>();
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    Enumeration<URL> urls = ClassLoaderReflectionToolkit._findResources(pw.classLoader, name);
                    while (urls != null && urls.hasMoreElements()) result.add(urls.nextElement());
                }
            } else {
                for (Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null) {
                        Enumeration<URL> urls = p.classLoader.getResources(name);
                        while (urls != null && urls.hasMoreElements()) result.add(urls.nextElement());
                    }
                }
            }
            return Collections.enumeration(result);
        }

        @Override
        protected URL findResource(String name) {
            if (PluginManager.FAST_LOOKUP) {
                for (PluginWrapper pw : getTransitiveDependencies()) {
                    URL url = ClassLoaderReflectionToolkit._findResource(pw.classLoader, name);
                    if (url != null)
                        return url;
                }
            } else {
                for (Dependency dep : dependencies) {
                    PluginWrapper p = pluginManager.getPlugin(dep.shortName);
                    if (p != null) {
                        URL url = p.classLoader.getResource(name);
                        if (url != null)
                            return url;
                    }
                }
            }
            return null;
        }
    }

    private final class AntClassLoader2 extends AntWithFindResourceClassLoader implements Closeable {

        private AntClassLoader2(ClassLoader parent) {
            super(parent, true);
        }

        @Override
        protected Class defineClassFromData(File container, byte[] classData, String classname) throws IOException {
            if (!DISABLE_TRANSFORMER)
                classData = pluginManager.getCompatibilityTransformer().transform(classname, classData, this);
            return super.defineClassFromData(container, classData, classname);
        }
    }

    public static boolean useAntClassLoader = SystemProperties.getBoolean(ClassicPluginStrategy.class.getName() + ".useAntClassLoader");

    private static final Logger LOGGER = Logger.getLogger(ClassicPluginStrategy.class.getName());

    public static boolean DISABLE_TRANSFORMER = SystemProperties.getBoolean(ClassicPluginStrategy.class.getName() + ".noBytecodeTransformer");
}
