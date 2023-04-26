package com.sun.media.sound;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.StringTokenizer;

final class Platform {

    private static final String libNameMain = "jsound";

    private static final String libNameALSA = "jsoundalsa";

    private static final String libNameDSound = "jsoundds";

    public static final int LIB_MAIN = 1;

    public static final int LIB_ALSA = 2;

    public static final int LIB_DSOUND = 4;

    private static int loadedLibs = 0;

    public static final int FEATURE_MIDIIO = 1;

    public static final int FEATURE_PORTS = 2;

    public static final int FEATURE_DIRECT_AUDIO = 3;

    private static boolean bigEndian;

    static {
        if (Printer.trace)
            Printer.trace(">> Platform.java: static");
        loadLibraries();
        readProperties();
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
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            System.loadLibrary(libNameMain);
            return null;
        });
        loadedLibs |= LIB_MAIN;
        String extraLibs = nGetExtraLibraries();
        StringTokenizer st = new StringTokenizer(extraLibs);
        while (st.hasMoreTokens()) {
            final String lib = st.nextToken();
            try {
                AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                    System.loadLibrary(lib);
                    return null;
                });
                if (lib.equals(libNameALSA)) {
                    loadedLibs |= LIB_ALSA;
                    if (Printer.debug)
                        Printer.debug("Loaded ALSA lib successfully.");
                } else if (lib.equals(libNameDSound)) {
                    loadedLibs |= LIB_DSOUND;
                    if (Printer.debug)
                        Printer.debug("Loaded DirectSound lib successfully.");
                } else {
                    if (Printer.err)
                        Printer.err("Loaded unknown lib '" + lib + "' successfully.");
                }
            } catch (Throwable t) {
                if (Printer.err)
                    Printer.err("Couldn't load library " + lib + ": " + t.toString());
            }
        }
    }

    static boolean isMidiIOEnabled() {
        return isFeatureLibLoaded(FEATURE_MIDIIO);
    }

    static boolean isPortsEnabled() {
        return isFeatureLibLoaded(FEATURE_PORTS);
    }

    static boolean isDirectAudioEnabled() {
        return isFeatureLibLoaded(FEATURE_DIRECT_AUDIO);
    }

    private static boolean isFeatureLibLoaded(int feature) {
        if (Printer.debug)
            Printer.debug("Platform: Checking for feature " + feature + "...");
        int requiredLib = nGetLibraryForFeature(feature);
        boolean isLoaded = (requiredLib != 0) && ((loadedLibs & requiredLib) == requiredLib);
        if (Printer.debug)
            Printer.debug("          ...needs library " + requiredLib + ". Result is loaded=" + isLoaded);
        return isLoaded;
    }

    private static native boolean nIsBigEndian();

    private static native String nGetExtraLibraries();

    private static native int nGetLibraryForFeature(int feature);

    private static void readProperties() {
        bigEndian = nIsBigEndian();
    }
}
