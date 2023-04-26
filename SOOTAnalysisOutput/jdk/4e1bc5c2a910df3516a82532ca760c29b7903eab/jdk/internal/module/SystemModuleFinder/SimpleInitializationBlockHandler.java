package jdk.internal.module;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.jimage.ImageLocation;
import jdk.internal.jimage.ImageReader;
import jdk.internal.jimage.ImageReaderFactory;
import jdk.internal.misc.JavaNetUriAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.module.ModuleHashes.HashSupplier;
import jdk.internal.perf.PerfCounter;

public class SystemModuleFinder implements ModuleFinder {

    private static final JavaNetUriAccess JNUA = SharedSecrets.getJavaNetUriAccess();

    private static final PerfCounter initTime = PerfCounter.newPerfCounter("jdk.module.finder.jimage.initTime");

    private static final PerfCounter moduleCount = PerfCounter.newPerfCounter("jdk.module.finder.jimage.modules");

    private static final PerfCounter packageCount = PerfCounter.newPerfCounter("jdk.module.finder.jimage.packages");

    private static final PerfCounter exportsCount = PerfCounter.newPerfCounter("jdk.module.finder.jimage.exports");

    private static final SystemModuleFinder INSTANCE;

    public static SystemModuleFinder getInstance() {
        return INSTANCE;
    }

    static {
        long t0 = System.nanoTime();
        INSTANCE = new SystemModuleFinder();
        initTime.addElapsedTimeFrom(t0);
    }

    private static class SystemImage {

        static final ImageReader READER;

        static {
            long t0 = System.nanoTime();
            READER = ImageReaderFactory.getImageReader();
            initTime.addElapsedTimeFrom(t0);
        }

        static ImageReader reader() {
            return READER;
        }
    }

    private static boolean isFastPathSupported() {
        return SystemModules.MODULE_NAMES.length > 0;
    }

    private static String[] moduleNames() {
        if (isFastPathSupported())
            return SystemModules.MODULE_NAMES;
        return SystemImage.reader().getModuleNames();
    }

    private final Set<ModuleReference> modules;

    private final Map<String, ModuleReference> nameToModule;

    private final Map<String, byte[]> hashes;

    private SystemModuleFinder() {
        String[] names = moduleNames();
        int n = names.length;
        moduleCount.add(n);
        boolean disabled = System.getProperty("jdk.system.module.finder.disabledFastPath") != null;
        ModuleDescriptor[] descriptors;
        ModuleHashes[] recordedHashes;
        ModuleResolution[] moduleResolutions;
        if (isFastPathSupported() && !disabled) {
            descriptors = SystemModules.descriptors();
            recordedHashes = SystemModules.hashes();
            moduleResolutions = SystemModules.moduleResolutions();
        } else {
            descriptors = new ModuleDescriptor[n];
            recordedHashes = new ModuleHashes[n];
            moduleResolutions = new ModuleResolution[n];
            ImageReader imageReader = SystemImage.reader();
            for (int i = 0; i < names.length; i++) {
                String mn = names[i];
                ImageLocation loc = imageReader.findLocation(mn, "module-info.class");
                ModuleInfo.Attributes attrs = ModuleInfo.read(imageReader.getResourceBuffer(loc), null);
                descriptors[i] = attrs.descriptor();
                recordedHashes[i] = attrs.recordedHashes();
                moduleResolutions[i] = attrs.moduleResolution();
            }
        }
        Map<String, byte[]> hashes = null;
        boolean secondSeen = false;
        for (ModuleHashes mh : recordedHashes) {
            if (mh != null) {
                if (hashes == null) {
                    hashes = mh.hashes();
                } else {
                    if (!secondSeen) {
                        hashes = new HashMap<>(hashes);
                        secondSeen = true;
                    }
                    hashes.putAll(mh.hashes());
                }
            }
        }
        this.hashes = (hashes == null) ? Map.of() : hashes;
        ModuleReference[] mods = new ModuleReference[n];
        @SuppressWarnings(value = { "rawtypes", "unchecked" })
        Entry<String, ModuleReference>[] map = (Entry<String, ModuleReference>[]) new Entry[n];
        for (int i = 0; i < n; i++) {
            ModuleDescriptor md = descriptors[i];
            ModuleReference mref = toModuleReference(md, recordedHashes[i], hashSupplier(names[i]), moduleResolutions[i]);
            mods[i] = mref;
            map[i] = Map.entry(names[i], mref);
            packageCount.add(md.packages().size());
            exportsCount.add(md.exports().size());
        }
        modules = Set.of(mods);
        nameToModule = Map.ofEntries(map);
    }

    @Override
    public Optional<ModuleReference> find(String name) {
        Objects.requireNonNull(name);
        return Optional.ofNullable(nameToModule.get(name));
    }

    @Override
    public Set<ModuleReference> findAll() {
        return modules;
    }

    private ModuleReference toModuleReference(ModuleDescriptor md, ModuleHashes recordedHashes, HashSupplier hasher, ModuleResolution mres) {
        String mn = md.name();
        URI uri = JNUA.create("jrt", "/".concat(mn));
        Supplier<ModuleReader> readerSupplier = new Supplier<>() {

            @Override
            public ModuleReader get() {
                return new ImageModuleReader(mn, uri);
            }
        };
        ModuleReference mref = new ModuleReferenceImpl(md, uri, readerSupplier, null, recordedHashes, hasher, mres);
        mref = ModuleBootstrap.patcher().patchIfNeeded(mref);
        return mref;
    }

    private HashSupplier hashSupplier(String name) {
        if (!hashes.containsKey(name))
            return null;
        return new HashSupplier() {

            @Override
            public byte[] generate(String algorithm) {
                return hashes.get(name);
            }
        };
    }

    static class ImageModuleReader implements ModuleReader {

        private final String module;

        private volatile boolean closed;

        private static void checkPermissionToConnect(URI uri) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    URLConnection uc = uri.toURL().openConnection();
                    sm.checkPermission(uc.getPermission());
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            }
        }

        ImageModuleReader(String module, URI uri) {
            checkPermissionToConnect(uri);
            this.module = module;
        }

        private ImageLocation findImageLocation(String name) throws IOException {
            Objects.requireNonNull(name);
            if (closed)
                throw new IOException("ModuleReader is closed");
            ImageReader imageReader = SystemImage.reader();
            if (imageReader != null) {
                return imageReader.findLocation(module, name);
            } else {
                return null;
            }
        }

        @Override
        public Optional<URI> find(String name) throws IOException {
            ImageLocation location = findImageLocation(name);
            if (location != null) {
                URI u = URI.create("jrt:/" + module + "/" + name);
                return Optional.of(u);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<InputStream> open(String name) throws IOException {
            return read(name).map(this::toInputStream);
        }

        private InputStream toInputStream(ByteBuffer bb) {
            try {
                int rem = bb.remaining();
                byte[] bytes = new byte[rem];
                bb.get(bytes);
                return new ByteArrayInputStream(bytes);
            } finally {
                release(bb);
            }
        }

        @Override
        public Optional<ByteBuffer> read(String name) throws IOException {
            ImageLocation location = findImageLocation(name);
            if (location != null) {
                return Optional.of(SystemImage.reader().getResourceBuffer(location));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void release(ByteBuffer bb) {
            Objects.requireNonNull(bb);
            ImageReader.releaseByteBuffer(bb);
        }

        @Override
        public Stream<String> list() throws IOException {
            if (closed)
                throw new IOException("ModuleReader is closed");
            Spliterator<String> s = new ModuleContentSpliterator(module);
            return StreamSupport.stream(s, false);
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    static class ModuleContentSpliterator implements Spliterator<String> {

        final String moduleRoot;

        final Deque<ImageReader.Node> stack;

        Iterator<ImageReader.Node> iterator;

        ModuleContentSpliterator(String module) throws IOException {
            moduleRoot = "/modules/" + module;
            stack = new ArrayDeque<>();
            ImageReader.Node dir = SystemImage.reader().findNode(moduleRoot);
            if (dir == null || !dir.isDirectory())
                throw new IOException(moduleRoot + " not a directory");
            stack.push(dir);
            iterator = Collections.emptyIterator();
        }

        private String next() throws IOException {
            for (; ; ) {
                while (iterator.hasNext()) {
                    ImageReader.Node node = iterator.next();
                    String name = node.getName();
                    if (node.isDirectory()) {
                        ImageReader.Node dir = SystemImage.reader().findNode(name);
                        assert dir.isDirectory();
                        stack.push(dir);
                    } else {
                        return name.substring(moduleRoot.length() + 1);
                    }
                }
                if (stack.isEmpty()) {
                    return null;
                } else {
                    ImageReader.Node dir = stack.poll();
                    assert dir.isDirectory();
                    iterator = dir.getChildren().iterator();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super String> action) {
            String next;
            try {
                next = next();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
            if (next != null) {
                action.accept(next);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<String> trySplit() {
            return null;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT + Spliterator.NONNULL + Spliterator.IMMUTABLE;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }
    }
}