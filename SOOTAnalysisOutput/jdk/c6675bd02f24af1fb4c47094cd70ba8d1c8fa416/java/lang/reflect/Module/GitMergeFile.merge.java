package java.lang.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Opens;
import java.lang.module.ModuleDescriptor.Version;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.BootLoader;
import jdk.internal.misc.JavaLangAccess;
import jdk.internal.misc.JavaLangReflectModuleAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.module.ServicesCatalog;
import jdk.internal.module.Resources;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.Attribute;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import sun.security.util.SecurityConstants;

public final class Module implements AnnotatedElement {

    private final Layer layer;

    private final String name;

    private final ClassLoader loader;

    private final ModuleDescriptor descriptor;

    private Module(Layer layer, ClassLoader loader, ModuleDescriptor descriptor, URI uri) {
        this.layer = layer;
        this.name = descriptor.name();
        this.loader = loader;
        this.descriptor = descriptor;
        boolean isOpen = descriptor.isOpen();
        Version version = descriptor.version().orElse(null);
        String vs = Objects.toString(version, null);
        String loc = Objects.toString(uri, null);
        String[] packages = descriptor.packages().toArray(new String[0]);
        defineModule0(this, isOpen, vs, loc, packages);
    }

    private Module(ClassLoader loader) {
        this.layer = null;
        this.name = null;
        this.loader = loader;
        this.descriptor = null;
    }

    Module(ClassLoader loader, ModuleDescriptor descriptor) {
        this.layer = null;
        this.name = descriptor.name();
        this.loader = loader;
        this.descriptor = descriptor;
    }

    public boolean isNamed() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public ClassLoader getClassLoader() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
        }
        return loader;
    }

    public ModuleDescriptor getDescriptor() {
        return descriptor;
    }

    public Layer getLayer() {
        if (isNamed()) {
            Layer layer = this.layer;
            if (layer != null)
                return layer;
            if (loader == null && name.equals("java.base")) {
                return SharedSecrets.getJavaLangAccess().getBootLayer();
            }
        }
        return null;
    }

    private static final Module ALL_UNNAMED_MODULE = new Module(null);

    private static final Module EVERYONE_MODULE = new Module(null);

    private static final Set<Module> EVERYONE_SET = Set.of(EVERYONE_MODULE);

    private volatile Set<Module> reads;

    private static final WeakPairMap<Module, Module, Boolean> reflectivelyReads = new WeakPairMap<>();

    public boolean canRead(Module other) {
        Objects.requireNonNull(other);
        if (!this.isNamed())
            return true;
        if (other == this)
            return true;
        if (other.isNamed()) {
            Set<Module> reads = this.reads;
            if (reads != null && reads.contains(other))
                return true;
        }
        if (reflectivelyReads.containsKeyPair(this, other))
            return true;
        if (!other.isNamed() && reflectivelyReads.containsKeyPair(this, ALL_UNNAMED_MODULE))
            return true;
        return false;
    }

    @CallerSensitive
    public Module addReads(Module other) {
        Objects.requireNonNull(other);
        if (this.isNamed()) {
            Module caller = Reflection.getCallerClass().getModule();
            if (caller != this) {
                throw new IllegalCallerException(caller + " != " + this);
            }
            implAddReads(other, true);
        }
        return this;
    }

    void implAddReads(Module other) {
        implAddReads(other, true);
    }

    void implAddReadsNoSync(Module other) {
        implAddReads(other, false);
    }

    private void implAddReads(Module other, boolean syncVM) {
        if (!canRead(other)) {
            if (syncVM) {
                if (other == ALL_UNNAMED_MODULE) {
                    addReads0(this, null);
                } else {
                    addReads0(this, other);
                }
            }
            reflectivelyReads.putIfAbsent(this, other, Boolean.TRUE);
        }
    }

    private volatile Map<String, Set<Module>> openPackages;

    private volatile Map<String, Set<Module>> exportedPackages;

    private static final WeakPairMap<Module, Module, Map<String, Boolean>> reflectivelyExports = new WeakPairMap<>();

    public boolean isExported(String pn, Module other) {
        Objects.requireNonNull(pn);
        Objects.requireNonNull(other);
        return implIsExportedOrOpen(pn, other, false);
    }

    public boolean isOpen(String pn, Module other) {
        Objects.requireNonNull(pn);
        Objects.requireNonNull(other);
        return implIsExportedOrOpen(pn, other, true);
    }

    public boolean isExported(String pn) {
        Objects.requireNonNull(pn);
        return implIsExportedOrOpen(pn, EVERYONE_MODULE, false);
    }

    public boolean isOpen(String pn) {
        Objects.requireNonNull(pn);
        return implIsExportedOrOpen(pn, EVERYONE_MODULE, true);
    }

    private boolean implIsExportedOrOpen(String pn, Module other, boolean open) {
        if (!isNamed())
            return true;
        if (other == this && containsPackage(pn))
            return true;
        if (descriptor.isOpen() || descriptor.isAutomatic())
            return containsPackage(pn);
        if (isStaticallyExportedOrOpen(pn, other, open))
            return true;
        if (isReflectivelyExportedOrOpen(pn, other, open))
            return true;
        return false;
    }

    private boolean isStaticallyExportedOrOpen(String pn, Module other, boolean open) {
        Map<String, Set<Module>> openPackages = this.openPackages;
        if (openPackages != null) {
            Set<Module> targets = openPackages.get(pn);
            if (targets != null) {
                if (targets.contains(EVERYONE_MODULE))
                    return true;
                if (other != EVERYONE_MODULE && targets.contains(other))
                    return true;
            }
        }
        if (!open) {
            Map<String, Set<Module>> exportedPackages = this.exportedPackages;
            if (exportedPackages != null) {
                Set<Module> targets = exportedPackages.get(pn);
                if (targets != null) {
                    if (targets.contains(EVERYONE_MODULE))
                        return true;
                    if (other != EVERYONE_MODULE && targets.contains(other))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isReflectivelyExportedOrOpen(String pn, Module other, boolean open) {
        Map<String, Boolean> exports = reflectivelyExports.get(this, EVERYONE_MODULE);
        if (exports != null) {
            Boolean b = exports.get(pn);
            if (b != null) {
                boolean isOpen = b.booleanValue();
                if (!open || isOpen)
                    return true;
            }
        }
        if (other != EVERYONE_MODULE) {
            exports = reflectivelyExports.get(this, other);
            if (exports != null) {
                Boolean b = exports.get(pn);
                if (b != null) {
                    boolean isOpen = b.booleanValue();
                    if (!open || isOpen)
                        return true;
                }
            }
            if (!other.isNamed()) {
                exports = reflectivelyExports.get(this, ALL_UNNAMED_MODULE);
                if (exports != null) {
                    Boolean b = exports.get(pn);
                    if (b != null) {
                        boolean isOpen = b.booleanValue();
                        if (!open || isOpen)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    @CallerSensitive
    public Module addExports(String pn, Module other) {
        if (pn == null)
            throw new IllegalArgumentException("package is null");
        Objects.requireNonNull(other);
        if (isNamed()) {
            Module caller = Reflection.getCallerClass().getModule();
            if (caller != this) {
                throw new IllegalCallerException(caller + " != " + this);
            }
            implAddExportsOrOpens(pn, other, false, true);
        }
        return this;
    }

    @CallerSensitive
    public Module addOpens(String pn, Module other) {
        if (pn == null)
            throw new IllegalArgumentException("package is null");
        Objects.requireNonNull(other);
        if (isNamed()) {
            Module caller = Reflection.getCallerClass().getModule();
            if (caller != this && !isOpen(pn, caller))
                throw new IllegalCallerException(pn + " is not open to " + caller);
            implAddExportsOrOpens(pn, other, true, true);
        }
        return this;
    }

    void implAddExportsNoSync(String pn, Module other) {
        if (other == null)
            other = EVERYONE_MODULE;
        implAddExportsOrOpens(pn.replace('/', '.'), other, false, false);
    }

    void implAddExports(String pn, Module other) {
        implAddExportsOrOpens(pn, other, false, true);
    }

    void implAddOpens(String pn, Module other) {
        implAddExportsOrOpens(pn, other, true, true);
    }

    private void implAddExportsOrOpens(String pn, Module other, boolean open, boolean syncVM) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(pn);
        if (!isNamed() || descriptor.isOpen() || descriptor.isAutomatic())
            return;
        if (implIsExportedOrOpen(pn, other, open))
            return;
        if (!containsPackage(pn)) {
            throw new IllegalArgumentException("package " + pn + " not in contents");
        }
        if (syncVM) {
            if (other == EVERYONE_MODULE) {
                addExportsToAll0(this, pn);
            } else if (other == ALL_UNNAMED_MODULE) {
                addExportsToAllUnnamed0(this, pn);
            } else {
                addExports0(this, pn, other);
            }
        }
        Map<String, Boolean> map = reflectivelyExports.computeIfAbsent(this, other, (m1, m2) -> new ConcurrentHashMap<>());
        if (open) {
            map.put(pn, Boolean.TRUE);
        } else {
            map.putIfAbsent(pn, Boolean.FALSE);
        }
    }

    private static final WeakPairMap<Module, Class<?>, Boolean> reflectivelyUses = new WeakPairMap<>();

    @CallerSensitive
    public Module addUses(Class<?> service) {
        Objects.requireNonNull(service);
        if (isNamed() && !descriptor.isAutomatic()) {
            Module caller = Reflection.getCallerClass().getModule();
            if (caller != this) {
                throw new IllegalCallerException(caller + " != " + this);
            }
            implAddUses(service);
        }
        return this;
    }

    void implAddUses(Class<?> service) {
        if (!canUse(service)) {
            reflectivelyUses.putIfAbsent(this, service, Boolean.TRUE);
        }
    }

    public boolean canUse(Class<?> service) {
        Objects.requireNonNull(service);
        if (!isNamed())
            return true;
        if (descriptor.isAutomatic())
            return true;
        if (descriptor.uses().contains(service.getName()))
            return true;
        return reflectivelyUses.containsKeyPair(this, service);
    }

    private volatile Map<String, Boolean> extraPackages;

    private boolean containsPackage(String pn) {
        if (descriptor.packages().contains(pn))
            return true;
        Map<String, Boolean> extraPackages = this.extraPackages;
        if (extraPackages != null && extraPackages.containsKey(pn))
            return true;
        return false;
    }

    public String[] getPackages() {
        if (isNamed()) {
            Set<String> packages = descriptor.packages();
            Map<String, Boolean> extraPackages = this.extraPackages;
            if (extraPackages == null) {
                return packages.toArray(new String[0]);
            } else {
                return Stream.concat(packages.stream(), extraPackages.keySet().stream()).toArray(String[]::new);
            }
        } else {
            Stream<Package> packages;
            if (loader == null) {
                packages = BootLoader.packages();
            } else {
                packages = SharedSecrets.getJavaLangAccess().packages(loader);
            }
            return packages.map(Package::getName).toArray(String[]::new);
        }
    }

    void implAddPackageNoSync(String pn) {
        implAddPackage(pn.replace('/', '.'), false);
    }

    private void implAddPackage(String pn, boolean syncVM) {
        if (!isNamed())
            return;
        if (containsPackage(pn))
            return;
        if (pn.isEmpty())
            throw new IllegalArgumentException("Cannot add <unnamed> package");
        for (int i = 0; i < pn.length(); i++) {
            char c = pn.charAt(i);
            if (c == '/' || c == ';' || c == '[') {
                throw new IllegalArgumentException("Illegal character: " + c);
            }
        }
        Map<String, Boolean> extraPackages = this.extraPackages;
        if (extraPackages == null) {
            synchronized (this) {
                extraPackages = this.extraPackages;
                if (extraPackages == null)
                    this.extraPackages = extraPackages = new ConcurrentHashMap<>();
            }
        }
        if (syncVM) {
            addPackage0(this, pn);
            if (descriptor.isOpen() || descriptor.isAutomatic()) {
                addExportsToAll0(this, pn);
            }
        }
        extraPackages.putIfAbsent(pn, Boolean.TRUE);
    }

    static Map<String, Module> defineModules(Configuration cf, Function<String, ClassLoader> clf, Layer layer) {
        Map<String, Module> nameToModule = new HashMap<>();
        Map<String, ClassLoader> moduleToLoader = new HashMap<>();
        boolean isBootLayer = (Layer.boot() == null);
        Set<ClassLoader> loaders = new HashSet<>();
        for (ResolvedModule resolvedModule : cf.modules()) {
            String name = resolvedModule.name();
            ClassLoader loader = clf.apply(name);
            if (loader != null) {
                moduleToLoader.put(name, loader);
                loaders.add(loader);
            } else if (!isBootLayer) {
                throw new IllegalArgumentException("loader can't be 'null'");
            }
        }
        for (ResolvedModule resolvedModule : cf.modules()) {
            ModuleReference mref = resolvedModule.reference();
            ModuleDescriptor descriptor = mref.descriptor();
            String name = descriptor.name();
            URI uri = mref.location().orElse(null);
            ClassLoader loader = moduleToLoader.get(resolvedModule.name());
            Module m;
            if (loader == null && isBootLayer && name.equals("java.base")) {
                m = Object.class.getModule();
            } else {
                m = new Module(layer, loader, descriptor, uri);
            }
            nameToModule.put(name, m);
            moduleToLoader.put(name, loader);
        }
        for (ResolvedModule resolvedModule : cf.modules()) {
            ModuleReference mref = resolvedModule.reference();
            ModuleDescriptor descriptor = mref.descriptor();
            String mn = descriptor.name();
            Module m = nameToModule.get(mn);
            assert m != null;
            Set<Module> reads = new HashSet<>();
            Map<String, Module> nameToSource = Collections.emptyMap();
            for (ResolvedModule other : resolvedModule.reads()) {
                Module m2 = null;
                if (other.configuration() == cf) {
                    m2 = nameToModule.get(other.name());
                    assert m2 != null;
                } else {
                    for (Layer parent : layer.parents()) {
                        m2 = findModule(parent, other);
                        if (m2 != null)
                            break;
                    }
                    assert m2 != null;
                    if (nameToSource.isEmpty())
                        nameToSource = new HashMap<>();
                    nameToSource.put(other.name(), m2);
                }
                reads.add(m2);
                addReads0(m, m2);
            }
            m.reads = reads;
            if (descriptor.isAutomatic()) {
                m.implAddReads(ALL_UNNAMED_MODULE, true);
            }
            initExportsAndOpens(m, nameToSource, nameToModule, layer.parents());
        }
        if (isBootLayer) {
            for (ResolvedModule resolvedModule : cf.modules()) {
                ModuleReference mref = resolvedModule.reference();
                ModuleDescriptor descriptor = mref.descriptor();
                if (!descriptor.provides().isEmpty()) {
                    String name = descriptor.name();
                    Module m = nameToModule.get(name);
                    ClassLoader loader = moduleToLoader.get(name);
                    ServicesCatalog catalog;
                    if (loader == null) {
                        catalog = BootLoader.getServicesCatalog();
                    } else {
                        catalog = ServicesCatalog.getServicesCatalog(loader);
                    }
                    catalog.register(m);
                }
            }
        }
        for (ClassLoader loader : loaders) {
            layer.bindToLoader(loader);
        }
        return nameToModule;
    }

    private static Module findModule(Layer parent, ResolvedModule resolvedModule) {
        Configuration cf = resolvedModule.configuration();
        String dn = resolvedModule.name();
        return parent.layers().filter(l -> l.configuration() == cf).findAny().map(layer -> {
            Optional<Module> om = layer.findModule(dn);
            assert om.isPresent() : dn + " not found in layer";
            Module m = om.get();
            assert m.getLayer() == layer : m + " not in expected layer";
            return m;
        }).orElse(null);
    }

    private static void initExportsAndOpens(Module m, Map<String, Module> nameToSource, Map<String, Module> nameToModule, List<Layer> parents) {
        ModuleDescriptor descriptor = m.getDescriptor();
        if (descriptor.isOpen() || descriptor.isAutomatic()) {
            assert descriptor.opens().isEmpty();
            for (String source : descriptor.packages()) {
                addExportsToAll0(m, source);
            }
            return;
        }
        Map<String, Set<Module>> openPackages = new HashMap<>();
        Map<String, Set<Module>> exportedPackages = new HashMap<>();
        for (Opens opens : descriptor.opens()) {
            String source = opens.source();
            if (opens.isQualified()) {
                Set<Module> targets = new HashSet<>();
                for (String target : opens.targets()) {
                    Module m2 = findModule(target, nameToSource, nameToModule, parents);
                    if (m2 != null) {
                        addExports0(m, source, m2);
                        targets.add(m2);
                    }
                }
                if (!targets.isEmpty()) {
                    openPackages.put(source, targets);
                }
            } else {
                addExportsToAll0(m, source);
                openPackages.put(source, EVERYONE_SET);
            }
        }
        for (Exports exports : descriptor.exports()) {
            String source = exports.source();
            Set<Module> openToTargets = openPackages.get(source);
            if (openToTargets != null && openToTargets.contains(EVERYONE_MODULE))
                continue;
            if (exports.isQualified()) {
                Set<Module> targets = new HashSet<>();
                for (String target : exports.targets()) {
                    Module m2 = findModule(target, nameToSource, nameToModule, parents);
                    if (m2 != null) {
                        if (openToTargets == null || !openToTargets.contains(m2)) {
                            addExports0(m, source, m2);
                            targets.add(m2);
                        }
                    }
                }
                if (!targets.isEmpty()) {
                    exportedPackages.put(source, targets);
                }
            } else {
                addExportsToAll0(m, source);
                exportedPackages.put(source, EVERYONE_SET);
            }
        }
        if (!openPackages.isEmpty())
            m.openPackages = openPackages;
        if (!exportedPackages.isEmpty())
            m.exportedPackages = exportedPackages;
    }

    private static Module findModule(String target, Map<String, Module> nameToSource, Map<String, Module> nameToModule, List<Layer> parents) {
        Module m = nameToSource.get(target);
        if (m == null) {
            m = nameToModule.get(target);
            if (m == null) {
                for (Layer parent : parents) {
                    m = parent.findModule(target).orElse(null);
                    if (m != null)
                        break;
                }
            }
        }
        return m;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return moduleInfoClass().getDeclaredAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return moduleInfoClass().getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return moduleInfoClass().getDeclaredAnnotations();
    }

    private volatile Class<?> moduleInfoClass;

    private Class<?> moduleInfoClass() {
        Class<?> clazz = this.moduleInfoClass;
        if (clazz != null)
            return clazz;
        synchronized (this) {
            clazz = this.moduleInfoClass;
            if (clazz == null) {
                if (isNamed()) {
                    PrivilegedAction<Class<?>> pa = this::loadModuleInfoClass;
                    clazz = AccessController.doPrivileged(pa);
                }
                if (clazz == null) {
                    class DummyModuleInfo {
                    }
                    clazz = DummyModuleInfo.class;
                }
                this.moduleInfoClass = clazz;
            }
            return clazz;
        }
    }

    private Class<?> loadModuleInfoClass() {
        Class<?> clazz = null;
        try (InputStream in = getResourceAsStream("module-info.class")) {
            if (in != null)
                clazz = loadModuleInfoClass(in);
        } catch (Exception ignore) {
        }
        return clazz;
    }

    private Class<?> loadModuleInfoClass(InputStream in) throws IOException {
        final String MODULE_INFO = "module-info";
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                cw.visit(version, Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT + Opcodes.ACC_SYNTHETIC, MODULE_INFO, null, "java/lang/Object", null);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitAttribute(Attribute attr) {
            }
        };
        ClassReader cr = new ClassReader(in);
        cr.accept(cv, 0);
        byte[] bytes = cw.toByteArray();
        ClassLoader cl = new ClassLoader(loader) {

            @Override
            protected Class<?> findClass(String cn) throws ClassNotFoundException {
                if (cn.equals(MODULE_INFO)) {
                    return super.defineClass(cn, bytes, 0, bytes.length);
                } else {
                    throw new ClassNotFoundException(cn);
                }
            }
        };
        try {
            return cl.loadClass(MODULE_INFO);
        } catch (ClassNotFoundException e) {
            throw new InternalError(e);
        }
    }

    @CallerSensitive
    public InputStream getResourceAsStream(String name) throws IOException {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (isNamed() && Resources.canEncapsulate(name)) {
            Module caller = Reflection.getCallerClass().getModule();
            if (caller != this && caller != Object.class.getModule()) {
                Set<String> packages = getDescriptor().packages();
                String pn = Resources.toPackageName(name);
                if (packages.contains(pn) && !isOpen(pn, caller)) {
                    return null;
                }
            }
        }
        String mn = this.name;
        if (loader == null) {
            return BootLoader.findResourceAsStream(mn, name);
        } else if (loader instanceof BuiltinClassLoader) {
            return ((BuiltinClassLoader) loader).findResourceAsStream(mn, name);
        }
        JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
        URL url = jla.findResource(loader, mn, name);
        if (url != null) {
            try {
                return url.openStream();
            } catch (SecurityException e) {
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if (isNamed()) {
            return "module " + name;
        } else {
            String id = Integer.toHexString(System.identityHashCode(this));
            return "unnamed module @" + id;
        }
    }

    private static native void defineModule0(Module module, boolean isOpen, String version, String location, String[] pns);

    private static native void addReads0(Module from, Module to);

    private static native void addExports0(Module from, String pn, Module to);

    private static native void addExportsToAll0(Module from, String pn);

    private static native void addExportsToAllUnnamed0(Module from, String pn);

    private static native void addPackage0(Module m, String pn);

    static {
        SharedSecrets.setJavaLangReflectModuleAccess(new JavaLangReflectModuleAccess() {

            @Override
            public Module defineUnnamedModule(ClassLoader loader) {
                return new Module(loader);
            }

            @Override
            public Module defineModule(ClassLoader loader, ModuleDescriptor descriptor, URI uri) {
                return new Module(null, loader, descriptor, uri);
            }

            @Override
            public void addReads(Module m1, Module m2) {
                m1.implAddReads(m2, true);
            }

            @Override
            public void addReadsAllUnnamed(Module m) {
                m.implAddReads(Module.ALL_UNNAMED_MODULE);
            }

            @Override
            public void addExports(Module m, String pn) {
                m.implAddExportsOrOpens(pn, Module.EVERYONE_MODULE, false, true);
            }

            @Override
            public void addExports(Module m, String pn, Module other) {
                m.implAddExportsOrOpens(pn, other, false, true);
            }

            @Override
            public void addExportsToAllUnnamed(Module m, String pn) {
                m.implAddExportsOrOpens(pn, Module.ALL_UNNAMED_MODULE, false, true);
            }

            @Override
            public void addOpens(Module m, String pn) {
                m.implAddExportsOrOpens(pn, Module.EVERYONE_MODULE, true, true);
            }

            @Override
            public void addOpens(Module m, String pn, Module other) {
                m.implAddExportsOrOpens(pn, other, true, true);
            }

            @Override
            public void addOpensToAllUnnamed(Module m, String pn) {
                m.implAddExportsOrOpens(pn, Module.ALL_UNNAMED_MODULE, true, true);
            }

            @Override
            public void addUses(Module m, Class<?> service) {
                m.implAddUses(service);
            }

            @Override
            public ServicesCatalog getServicesCatalog(Layer layer) {
                return layer.getServicesCatalog();
            }

            @Override
            public Stream<Layer> layers(Layer layer) {
                return layer.layers();
            }

            @Override
            public Stream<Layer> layers(ClassLoader loader) {
                return Layer.layers(loader);
            }
        });
    }
}
