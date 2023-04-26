package com.sun.media.sound;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

final class Platform {

    private static final String libName = "jsound";

    private static boolean isNativeLibLoaded;

    private static boolean bigEndian;

    static {
        if (Printer.trace)
            Printer.trace(">> Platform.java: static");
        loadLibraries();
    }

    private Platform() {
    }

    static void initialize() {
        if (Printer.trace)
            Printer.trace("Platform: initialize()");
    }

    static boolean isBigEndian() {
        return bigEndian;
    }

    private static void loadLibraries() {
        if (Printer.trace)
            Printer.trace(">>Platform.loadLibraries");
        isNativeLibLoaded = true;
        try {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                System.loadLibrary(libName);
                return null;
            });
        } catch (Throwable t) {
            if (Printer.err)
                Printer.err("Couldn't load library " + libName + ": " + t.toString());
            isNativeLibLoaded = false;
        }
        if (isNativeLibLoaded) {
            bigEndian = nIsBigEndian();
        }
    }

    static boolean isMidiIOEnabled() {
        if (Printer.debug)
            Printer.debug("Platform: Checking for MidiIO; library is loaded=" + isNativeLibLoaded);
        return isNativeLibLoaded;
    }

    static boolean isPortsEnabled() {
        if (Printer.debug)
            Printer.debug("Platform: Checking for Ports; library is loaded=" + isNativeLibLoaded);
        return isNativeLibLoaded;
    }

    static boolean isDirectAudioEnabled() {
        if (Printer.debug)
            Printer.debug("Platform: Checking for DirectAudio; library is loaded=" + isNativeLibLoaded);
        return isNativeLibLoaded;
    }

    private static native boolean nIsBigEndian();
}
