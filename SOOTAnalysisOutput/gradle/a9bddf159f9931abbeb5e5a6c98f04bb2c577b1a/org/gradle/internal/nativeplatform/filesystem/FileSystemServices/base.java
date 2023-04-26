package org.gradle.internal.nativeplatform.filesystem;

import com.sun.jna.Native;
import org.gradle.api.JavaVersion;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.nativeplatform.jna.LibC;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.internal.service.ServiceRegistry;
import org.jruby.ext.posix.BaseNativePOSIX;
import org.jruby.ext.posix.JavaPOSIX;
import org.jruby.ext.posix.POSIX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemServices.class);

    private static final ServiceRegistry SERVICES;

    static {
        DefaultServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        addServices(serviceRegistry);
        SERVICES = serviceRegistry;
    }

    public static ServiceRegistry getServices() {
        return SERVICES;
    }

    private static void addServices(DefaultServiceRegistry serviceRegistry) {
        OperatingSystem operatingSystem = OperatingSystem.current();
        if (operatingSystem.isWindows()) {
            serviceRegistry.add(Chmod.class, new EmptyChmod());
            serviceRegistry.add(Stat.class, new FallbackStat());
            serviceRegistry.add(Symlink.class, new FallbackSymlink());
            return;
        }
        LibC libC = loadLibC();
        serviceRegistry.add(Symlink.class, createSymlink(libC));
        POSIX posix = PosixUtil.current();
        if ((libC != null && (operatingSystem.isLinux() || operatingSystem.isMacOsX())) && posix instanceof BaseNativePOSIX) {
            FilePathEncoder filePathEncoder = createEncoder(libC);
            serviceRegistry.add(Chmod.class, new LibcChmod(libC, filePathEncoder));
            serviceRegistry.add(Stat.class, new LibCStat(libC, operatingSystem, (BaseNativePOSIX) posix, filePathEncoder));
            return;
        }
        if (JavaVersion.current().isJava7()) {
            String jdkFilePermissionclass = "org.gradle.internal.nativeplatform.filesystem.jdk7.PosixJdk7FilePermissionHandler";
            try {
                Object handler = FileSystemServices.class.getClassLoader().loadClass(jdkFilePermissionclass).newInstance();
                serviceRegistry.add(Stat.class, (Stat) handler);
                serviceRegistry.add(Chmod.class, (Chmod) handler);
                return;
            } catch (ClassNotFoundException e) {
                LOGGER.warn(String.format("Unable to load %s. Continuing with fallback.", jdkFilePermissionclass));
            } catch (Exception e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
        }
        serviceRegistry.add(Chmod.class, createChmod(libC));
        serviceRegistry.add(Stat.class, createStat());
    }

    private static Symlink createSymlink(LibC libC) {
        if (libC != null) {
            return new LibcSymlink(libC);
        }
        LOGGER.debug("Using FallbackSymlink implementation.");
        return new FallbackSymlink();
    }

    private static Stat createStat() {
        POSIX posix = PosixUtil.current();
        if (posix instanceof JavaPOSIX) {
            return new FallbackStat();
        } else {
            return new PosixStat(posix);
        }
    }

    static Chmod createChmod(LibC libC) {
        if (libC != null) {
            return new LibcChmod(libC, createEncoder(libC));
        }
        LOGGER.debug("Using EmptyChmod implementation.");
        return new EmptyChmod();
    }

    static FilePathEncoder createEncoder(LibC libC) {
        if (OperatingSystem.current().isMacOsX()) {
            return new MacFilePathEncoder();
        }
        return new DefaultFilePathEncoder(libC);
    }

    private static LibC loadLibC() {
        try {
            return (LibC) Native.loadLibrary("c", LibC.class);
        } catch (LinkageError e) {
            LOGGER.debug("Unable to load LibC library. Continuing with fallback filesystem implementations.");
            return null;
        }
    }
}
