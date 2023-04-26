package jdk.vm.ci.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import jdk.internal.misc.VM;
import jdk.internal.reflect.Reflection;

public final class Services {

    public static final boolean IS_BUILDING_NATIVE_IMAGE = Boolean.parseBoolean(VM.getSavedProperty("jdk.vm.ci.services.aot"));

    public static final boolean IS_IN_NATIVE_IMAGE;

    static {
        IS_IN_NATIVE_IMAGE = false;
    }

    private Services() {
    }

    private static volatile Map<String, String> savedProperties;

    static final boolean JVMCI_ENABLED = Boolean.parseBoolean(VM.getSavedProperties().get("jdk.internal.vm.ci.enabled"));

    static void checkJVMCIEnabled() {
        if (!JVMCI_ENABLED) {
            throw new Error("The EnableJVMCI VM option must be true (i.e., -XX:+EnableJVMCI) to use JVMCI");
        }
    }

    public static Map<String, String> getSavedProperties() {
        checkJVMCIEnabled();
        if (IS_IN_NATIVE_IMAGE) {
            if (savedProperties == null) {
                throw new InternalError("Saved properties not initialized");
            }
        } else {
            if (savedProperties == null) {
                synchronized (Services.class) {
                    if (savedProperties == null) {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkPermission(new JVMCIPermission());
                        }
                        savedProperties = VM.getSavedProperties();
                    }
                }
            }
        }
        return savedProperties;
    }

    public static String getSavedProperty(String name, String def) {
        return Services.getSavedProperties().getOrDefault(name, def);
    }

    public static String getSavedProperty(String name) {
        return Services.getSavedProperties().get(name);
    }

    public static void initializeJVMCI() {
        checkJVMCIEnabled();
        try {
            Class.forName("jdk.vm.ci.runtime.JVMCI");
        } catch (ClassNotFoundException e) {
            throw new InternalError(e);
        }
    }

    private static boolean jvmciEnabled = true;

    static class LazyBootClassPath {

        static final ClassLoader bootClassPath = new ClassLoader(null) {
        };
    }

    private static ClassLoader findBootClassLoaderChild(ClassLoader start) {
        ClassLoader cl = start;
        while (cl.getParent() != null) {
            cl = cl.getParent();
        }
        return cl;
    }

    private static final Map<Class<?>, List<?>> servicesCache = IS_BUILDING_NATIVE_IMAGE ? new HashMap<>() : null;

    @SuppressWarnings("unchecked")
    private static <S> Iterable<S> load0(Class<S> service) {
        if (IS_IN_NATIVE_IMAGE || IS_BUILDING_NATIVE_IMAGE) {
            List<?> list = servicesCache.get(service);
            if (list != null) {
                return (Iterable<S>) list;
            }
            if (IS_IN_NATIVE_IMAGE) {
                throw new InternalError(String.format("No %s providers found when building native image", service.getName()));
            }
        }
        Iterable<S> providers = Collections.emptyList();
        if (jvmciEnabled) {
            ClassLoader cl = null;
            try {
                cl = getJVMCIClassLoader();
                if (cl == null) {
                    cl = LazyBootClassPath.bootClassPath;
                    cl = findBootClassLoaderChild(ClassLoader.getSystemClassLoader());
                }
                providers = ServiceLoader.load(service, cl);
            } catch (UnsatisfiedLinkError e) {
                jvmciEnabled = false;
            } catch (InternalError e) {
                if (e.getMessage().equals("JVMCI is not enabled")) {
                    jvmciEnabled = false;
                } else {
                    throw e;
                }
            }
        }
        if (IS_BUILDING_NATIVE_IMAGE) {
            synchronized (servicesCache) {
                ArrayList<S> providersList = new ArrayList<>();
                for (S provider : providers) {
                    providersList.add(provider);
                }
                servicesCache.put(service, providersList);
                providers = providersList;
            }
        }
        return providers;
    }

    static void openJVMCITo(Module otherModule) {
        Module jvmci = Services.class.getModule();
        if (jvmci != otherModule) {
            Set<String> packages = jvmci.getPackages();
            for (String pkg : packages) {
                boolean opened = jvmci.isOpen(pkg, otherModule);
                if (!opened) {
                    jvmci.addOpens(pkg, otherModule);
                }
            }
        }
    }

    public static <S> Iterable<S> load(Class<S> service) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        return load0(service);
    }

    public static <S> S loadSingle(Class<S> service, boolean required) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JVMCIPermission());
        }
        Iterable<S> providers = load0(service);
        S singleProvider = null;
        for (S provider : providers) {
            if (singleProvider != null) {
                throw new InternalError(String.format("Multiple %s providers found: %s, %s", service.getName(), singleProvider.getClass().getName(), provider.getClass().getName()));
            }
            singleProvider = provider;
        }
        if (singleProvider == null && required) {
            String javaHome = Services.getSavedProperty("java.home");
            String vmName = Services.getSavedProperty("java.vm.name");
            Formatter errorMessage = new Formatter();
            errorMessage.format("The VM does not expose required service %s.%n", service.getName());
            errorMessage.format("Currently used Java home directory is %s.%n", javaHome);
            errorMessage.format("Currently used VM configuration is: %s", vmName);
            throw new UnsupportedOperationException(errorMessage.toString());
        }
        return singleProvider;
    }

    static {
        Reflection.registerMethodsToFilter(Services.class, Set.of("getJVMCIClassLoader"));
    }

    private static ClassLoader getJVMCIClassLoader() {
        if (IS_IN_NATIVE_IMAGE) {
            return null;
        }
        return ClassLoader.getSystemClassLoader();
    }

    private static final int MAX_UNICODE_IN_UTF8_LENGTH = 3;

    private static final int MAX_UTF8_PROPERTY_STRING_LENGTH = 65535 / MAX_UNICODE_IN_UTF8_LENGTH;

    @VMEntryPoint
    private static byte[] serializeSavedProperties() throws IOException {
        if (IS_IN_NATIVE_IMAGE) {
            throw new InternalError("Can only serialize saved properties in HotSpot runtime");
        }
        return serializeProperties(Services.getSavedProperties());
    }

    private static byte[] serializeProperties(Map<String, String> props) throws IOException {
        int estimate = 4 + 4;
        int nonUtf8Props = 0;
        for (Map.Entry<String, String> e : props.entrySet()) {
            String name = e.getKey();
            String value = e.getValue();
            estimate += (2 + (name.length())) + (2 + (value.length()));
            if (name.length() > MAX_UTF8_PROPERTY_STRING_LENGTH || value.length() > MAX_UTF8_PROPERTY_STRING_LENGTH) {
                nonUtf8Props++;
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(estimate);
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(props.size() - nonUtf8Props);
        out.writeInt(nonUtf8Props);
        for (Map.Entry<String, String> e : props.entrySet()) {
            String name = e.getKey();
            String value = e.getValue();
            if (name.length() <= MAX_UTF8_PROPERTY_STRING_LENGTH && value.length() <= MAX_UTF8_PROPERTY_STRING_LENGTH) {
                out.writeUTF(name);
                out.writeUTF(value);
            }
        }
        if (nonUtf8Props != 0) {
            for (Map.Entry<String, String> e : props.entrySet()) {
                String name = e.getKey();
                String value = e.getValue();
                if (name.length() > MAX_UTF8_PROPERTY_STRING_LENGTH || value.length() > MAX_UTF8_PROPERTY_STRING_LENGTH) {
                    byte[] utf8Name = name.getBytes("UTF-8");
                    byte[] utf8Value = value.getBytes("UTF-8");
                    out.writeInt(utf8Name.length);
                    out.write(utf8Name);
                    out.writeInt(utf8Value.length);
                    out.write(utf8Value);
                }
            }
        }
        return baos.toByteArray();
    }

    @VMEntryPoint
    private static void initializeSavedProperties(byte[] serializedProperties) throws IOException {
        if (!IS_IN_NATIVE_IMAGE) {
            throw new InternalError("Can only initialize saved properties in JVMCI shared library runtime");
        }
        savedProperties = Collections.unmodifiableMap(deserializeProperties(serializedProperties));
    }

    private static Map<String, String> deserializeProperties(byte[] serializedProperties) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(serializedProperties));
        int utf8Props = in.readInt();
        int nonUtf8Props = in.readInt();
        Map<String, String> props = new HashMap<>(utf8Props + nonUtf8Props);
        int index = 0;
        while (in.available() != 0) {
            if (index < utf8Props) {
                String name = in.readUTF();
                String value = in.readUTF();
                props.put(name, value);
            } else {
                int nameLen = in.readInt();
                byte[] nameBytes = new byte[nameLen];
                in.read(nameBytes);
                int valueLen = in.readInt();
                byte[] valueBytes = new byte[valueLen];
                in.read(valueBytes);
                String name = new String(nameBytes, "UTF-8");
                String value = new String(valueBytes, "UTF-8");
                props.put(name, value);
            }
            index++;
        }
        return props;
    }
}
