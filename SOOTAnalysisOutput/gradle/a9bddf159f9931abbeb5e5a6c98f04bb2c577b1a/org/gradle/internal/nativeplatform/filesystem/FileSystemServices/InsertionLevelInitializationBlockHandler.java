package org.gradle.internal.nativeplatform.filesystem;

import com.sun.jna.Native;
import net.rubygrapefruit.platform.NativeIntegrationUnavailableException;
import net.rubygrapefruit.platform.PosixFiles;
import org.gradle.api.JavaVersion;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.nativeplatform.jna.LibC;
import org.gradle.internal.os.OperatingSystem;
import org.jruby.ext.posix.BaseNativePOSIX;
import org.jruby.ext.posix.JavaPOSIX;
import org.jruby.ext.posix.POSIX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemServices.class);

    @SuppressWarnings("UnusedDeclaration")
    public FileSystem createFileSystem(OperatingSystem operatingSystem) {
        if (operatingSystem.isWindows()) {
            return new GenericFileSystem(new EmptyChmod(), new FallbackStat(), new FallbackSymlink());
        }
        try {
            PosixFiles posixFiles = net.rubygrapefruit.platform.Native.get(PosixFiles.class);
            Symlink symlink = new NativePlatformBackedSymlink(posixFiles);
            Chmod chmod = new NativePlatformBackedChmod(posixFiles);
            Stat stat = new NativePlatformBackedStat(posixFiles);
            return new GenericFileSystem(chmod, stat, symlink);
        } catch (NativeIntegrationUnavailableException ex) {
            LOGGER.debug("Native-platform file system integration is not available. Continuing with fallback.");
        }
        LibC libC = loadLibC();
        Symlink symlink = symlink(libC);
        POSIX posix = PosixUtil.current();
        if ((libC != null && (operatingSystem.isLinux())) && posix instanceof BaseNativePOSIX) {
            FilePathEncoder filePathEncoder = new DefaultFilePathEncoder(libC);
            Chmod chmod = new LibcChmod(libC, filePathEncoder);
            Stat stat = new LibCStat(libC, operatingSystem, (BaseNativePOSIX) posix, filePathEncoder);
            return new GenericFileSystem(chmod, stat, symlink);
        }
        if (JavaVersion.current().isJava7()) {
            String jdkFilePermissionclass = "org.gradle.internal.nativeplatform.filesystem.jdk7.PosixJdk7FilePermissionHandler";
            try {
                Object handler = FileSystemServices.class.getClassLoader().loadClass(jdkFilePermissionclass).newInstance();
                return new GenericFileSystem((Chmod) handler, (Stat) handler, symlink);
            } catch (ClassNotFoundException e) {
                LOGGER.warn(String.format("Unable to load %s. Continuing with fallback.", jdkFilePermissionclass));
            } catch (Exception e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
        }
        return new GenericFileSystem(chmod(libC), stat(), symlink);
    }

    private Symlink symlink(LibC libC) {
        if (libC != null) {
            return new LibcSymlink(libC);
        }
        LOGGER.debug("Using FallbackSymlink implementation.");
        return new FallbackSymlink();
    }

    private Stat stat() {
        POSIX posix = PosixUtil.current();
        if (posix instanceof JavaPOSIX) {
            return new FallbackStat();
        } else {
            return new PosixStat(posix);
        }
    }

    private Chmod chmod(LibC libC) {
        if (libC != null) {
            return new LibcChmod(libC, new DefaultFilePathEncoder(libC));
        }
        LOGGER.debug("Using EmptyChmod implementation.");
        return new EmptyChmod();
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