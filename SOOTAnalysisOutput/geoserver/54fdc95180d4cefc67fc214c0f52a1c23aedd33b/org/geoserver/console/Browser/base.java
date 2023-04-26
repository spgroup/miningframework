package org.geoserver.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Browser {

    private static int jvm;

    private static Object browser;

    private static boolean loadedWithoutErrors;

    private static Class mrjFileUtilsClass;

    private static Class mrjOSTypeClass;

    private static Class aeDescClass;

    private static Constructor aeTargetConstructor;

    private static Constructor appleEventConstructor;

    private static Constructor aeDescConstructor;

    private static Method findFolder;

    private static Method getFileCreator;

    private static Method getFileType;

    private static Method openURL;

    private static Method makeOSType;

    private static Method putParameter;

    private static Method sendNoReply;

    private static Object kSystemFolderType;

    private static Integer keyDirectObject;

    private static Integer kAutoGenerateReturnID;

    private static Integer kAnyTransactionID;

    private static Object linkage;

    private static final String JDirect_MacOSX = "/System/Library/Frameworks/Carbon.framework/Frameworks/HIToolbox.framework/HIToolbox";

    private static final int MRJ_2_0 = 0;

    private static final int MRJ_2_1 = 1;

    private static final int MRJ_3_0 = 3;

    private static final int MRJ_3_1 = 4;

    private static final int WINDOWS_NT = 5;

    private static final int WINDOWS_9x = 6;

    private static final int OTHER = -1;

    private static final String FINDER_TYPE = "FNDR";

    private static final String FINDER_CREATOR = "MACS";

    private static final String GURL_EVENT = "GURL";

    private static final String FIRST_WINDOWS_PARAMETER = "/c";

    private static final String SECOND_WINDOWS_PARAMETER = "start";

    private static final String THIRD_WINDOWS_PARAMETER = "\"\"";

    private static final String NETSCAPE_REMOTE_PARAMETER = "-remote";

    private static final String NETSCAPE_OPEN_PARAMETER_START = "openURL(";

    private static final String NETSCAPE_OPEN_PARAMETER_END = ")";

    private static String errorMessage;

    static {
        loadedWithoutErrors = true;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            String mrjVersion = System.getProperty("mrj.version");
            String majorMRJVersion = mrjVersion.substring(0, 3);
            try {
                double version = Double.valueOf(majorMRJVersion).doubleValue();
                if (version == 2) {
                    jvm = MRJ_2_0;
                } else if (version >= 2.1 && version < 3) {
                    jvm = MRJ_2_1;
                } else if (version == 3.0) {
                    jvm = MRJ_3_0;
                } else if (version >= 3.1) {
                    jvm = MRJ_3_1;
                } else {
                    loadedWithoutErrors = false;
                    errorMessage = "Unsupported MRJ version: " + version;
                }
            } catch (NumberFormatException nfe) {
                loadedWithoutErrors = false;
                errorMessage = "Invalid MRJ version: " + mrjVersion;
            }
        } else if (osName.startsWith("Windows")) {
            if (osName.indexOf("9") != -1) {
                jvm = WINDOWS_9x;
            } else {
                jvm = WINDOWS_NT;
            }
        } else {
            jvm = OTHER;
        }
        if (loadedWithoutErrors) {
            loadedWithoutErrors = loadClasses();
        }
    }

    private Browser() {
    }

    private static boolean loadClasses() {
        switch(jvm) {
            case MRJ_2_0:
                try {
                    Class aeTargetClass = Class.forName("com.apple.MacOS.AETarget");
                    Class osUtilsClass = Class.forName("com.apple.MacOS.OSUtils");
                    Class appleEventClass = Class.forName("com.apple.MacOS.AppleEvent");
                    Class aeClass = Class.forName("com.apple.MacOS.ae");
                    aeDescClass = Class.forName("com.apple.MacOS.AEDesc");
                    aeTargetConstructor = aeTargetClass.getDeclaredConstructor(new Class[] { int.class });
                    appleEventConstructor = appleEventClass.getDeclaredConstructor(new Class[] { int.class, int.class, aeTargetClass, int.class, int.class });
                    aeDescConstructor = aeDescClass.getDeclaredConstructor(new Class[] { String.class });
                    makeOSType = osUtilsClass.getDeclaredMethod("makeOSType", new Class[] { String.class });
                    putParameter = appleEventClass.getDeclaredMethod("putParameter", new Class[] { int.class, aeDescClass });
                    sendNoReply = appleEventClass.getDeclaredMethod("sendNoReply", new Class[] {});
                    Field keyDirectObjectField = aeClass.getDeclaredField("keyDirectObject");
                    keyDirectObject = (Integer) keyDirectObjectField.get(null);
                    Field autoGenerateReturnIDField = appleEventClass.getDeclaredField("kAutoGenerateReturnID");
                    kAutoGenerateReturnID = (Integer) autoGenerateReturnIDField.get(null);
                    Field anyTransactionIDField = appleEventClass.getDeclaredField("kAnyTransactionID");
                    kAnyTransactionID = (Integer) anyTransactionIDField.get(null);
                } catch (ClassNotFoundException cnfe) {
                    errorMessage = cnfe.getMessage();
                    return false;
                } catch (NoSuchMethodException nsme) {
                    errorMessage = nsme.getMessage();
                    return false;
                } catch (NoSuchFieldException nsfe) {
                    errorMessage = nsfe.getMessage();
                    return false;
                } catch (IllegalAccessException iae) {
                    errorMessage = iae.getMessage();
                    return false;
                }
                break;
            case MRJ_2_1:
                try {
                    mrjFileUtilsClass = Class.forName("com.apple.mrj.MRJFileUtils");
                    mrjOSTypeClass = Class.forName("com.apple.mrj.MRJOSType");
                    Field systemFolderField = mrjFileUtilsClass.getDeclaredField("kSystemFolderType");
                    kSystemFolderType = systemFolderField.get(null);
                    findFolder = mrjFileUtilsClass.getDeclaredMethod("findFolder", new Class[] { mrjOSTypeClass });
                    getFileCreator = mrjFileUtilsClass.getDeclaredMethod("getFileCreator", new Class[] { File.class });
                    getFileType = mrjFileUtilsClass.getDeclaredMethod("getFileType", new Class[] { File.class });
                } catch (ClassNotFoundException cnfe) {
                    errorMessage = cnfe.getMessage();
                    return false;
                } catch (NoSuchFieldException nsfe) {
                    errorMessage = nsfe.getMessage();
                    return false;
                } catch (NoSuchMethodException nsme) {
                    errorMessage = nsme.getMessage();
                    return false;
                } catch (SecurityException se) {
                    errorMessage = se.getMessage();
                    return false;
                } catch (IllegalAccessException iae) {
                    errorMessage = iae.getMessage();
                    return false;
                }
                break;
            case MRJ_3_0:
                try {
                    Class linker = Class.forName("com.apple.mrj.jdirect.Linker");
                    Constructor constructor = linker.getConstructor(new Class[] { Class.class });
                    linkage = constructor.newInstance(new Object[] { Browser.class });
                } catch (ClassNotFoundException cnfe) {
                    errorMessage = cnfe.getMessage();
                    return false;
                } catch (NoSuchMethodException nsme) {
                    errorMessage = nsme.getMessage();
                    return false;
                } catch (InvocationTargetException ite) {
                    errorMessage = ite.getMessage();
                    return false;
                } catch (InstantiationException ie) {
                    errorMessage = ie.getMessage();
                    return false;
                } catch (IllegalAccessException iae) {
                    errorMessage = iae.getMessage();
                    return false;
                }
                break;
            case MRJ_3_1:
                try {
                    mrjFileUtilsClass = Class.forName("com.apple.mrj.MRJFileUtils");
                    openURL = mrjFileUtilsClass.getDeclaredMethod("openURL", new Class[] { String.class });
                } catch (ClassNotFoundException cnfe) {
                    errorMessage = cnfe.getMessage();
                    return false;
                } catch (NoSuchMethodException nsme) {
                    errorMessage = nsme.getMessage();
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private static Object locateBrowser() {
        if (browser != null) {
            return browser;
        }
        switch(jvm) {
            case MRJ_2_0:
                try {
                    Integer finderCreatorCode = (Integer) makeOSType.invoke(null, new Object[] { FINDER_CREATOR });
                    Object aeTarget = aeTargetConstructor.newInstance(new Object[] { finderCreatorCode });
                    Integer gurlType = (Integer) makeOSType.invoke(null, new Object[] { GURL_EVENT });
                    Object appleEvent = appleEventConstructor.newInstance(new Object[] { gurlType, gurlType, aeTarget, kAutoGenerateReturnID, kAnyTransactionID });
                    return appleEvent;
                } catch (IllegalAccessException iae) {
                    browser = null;
                    errorMessage = iae.getMessage();
                    return browser;
                } catch (InstantiationException ie) {
                    browser = null;
                    errorMessage = ie.getMessage();
                    return browser;
                } catch (InvocationTargetException ite) {
                    browser = null;
                    errorMessage = ite.getMessage();
                    return browser;
                }
            case MRJ_2_1:
                File systemFolder;
                try {
                    systemFolder = (File) findFolder.invoke(null, new Object[] { kSystemFolderType });
                } catch (IllegalArgumentException iare) {
                    browser = null;
                    errorMessage = iare.getMessage();
                    return browser;
                } catch (IllegalAccessException iae) {
                    browser = null;
                    errorMessage = iae.getMessage();
                    return browser;
                } catch (InvocationTargetException ite) {
                    browser = null;
                    errorMessage = ite.getTargetException().getClass() + ": " + ite.getTargetException().getMessage();
                    return browser;
                }
                String[] systemFolderFiles = systemFolder.list();
                for (int i = 0; i < systemFolderFiles.length; i++) {
                    try {
                        File file = new File(systemFolder, systemFolderFiles[i]);
                        if (!file.isFile()) {
                            continue;
                        }
                        Object fileType = getFileType.invoke(null, new Object[] { file });
                        if (FINDER_TYPE.equals(fileType.toString())) {
                            Object fileCreator = getFileCreator.invoke(null, new Object[] { file });
                            if (FINDER_CREATOR.equals(fileCreator.toString())) {
                                browser = file.toString();
                                return browser;
                            }
                        }
                    } catch (IllegalArgumentException iare) {
                        browser = browser;
                        errorMessage = iare.getMessage();
                        return null;
                    } catch (IllegalAccessException iae) {
                        browser = null;
                        errorMessage = iae.getMessage();
                        return browser;
                    } catch (InvocationTargetException ite) {
                        browser = null;
                        errorMessage = ite.getTargetException().getClass() + ": " + ite.getTargetException().getMessage();
                        return browser;
                    }
                }
                browser = null;
                break;
            case MRJ_3_0:
            case MRJ_3_1:
                browser = "";
                break;
            case WINDOWS_NT:
                browser = "cmd.exe";
                break;
            case WINDOWS_9x:
                browser = "command.com";
                break;
            case OTHER:
            default:
                browser = "netscape";
                break;
        }
        return browser;
    }

    public static void openUrl(File file) throws IOException {
        openUrl("file://" + file.getPath());
    }

    public static void openUrl(URL url) throws IOException {
        openUrl(url.toString());
    }

    public static void openUrl(String url) throws IOException {
        if (!loadedWithoutErrors) {
            throw new IOException("Exception in finding browser: " + errorMessage);
        }
        Object browser = locateBrowser();
        if (browser == null) {
            throw new IOException("Unable to locate browser: " + errorMessage);
        }
        switch(jvm) {
            case MRJ_2_0:
                Object aeDesc = null;
                try {
                    aeDesc = aeDescConstructor.newInstance(new Object[] { url });
                    putParameter.invoke(browser, new Object[] { keyDirectObject, aeDesc });
                    sendNoReply.invoke(browser, new Object[] {});
                } catch (InvocationTargetException ite) {
                    throw new IOException("InvocationTargetException while creating AEDesc: " + ite.getMessage());
                } catch (IllegalAccessException iae) {
                    throw new IOException("IllegalAccessException while building AppleEvent: " + iae.getMessage());
                } catch (InstantiationException ie) {
                    throw new IOException("InstantiationException while creating AEDesc: " + ie.getMessage());
                } finally {
                    aeDesc = null;
                    browser = null;
                }
                break;
            case MRJ_2_1:
                Runtime.getRuntime().exec(new String[] { (String) browser, url });
                break;
            case MRJ_3_0:
                int[] instance = new int[1];
                int result = ICStart(instance, 0);
                if (result == 0) {
                    int[] selectionStart = new int[] { 0 };
                    byte[] urlBytes = url.getBytes();
                    int[] selectionEnd = new int[] { urlBytes.length };
                    result = ICLaunchURL(instance[0], new byte[] { 0 }, urlBytes, urlBytes.length, selectionStart, selectionEnd);
                    if (result == 0) {
                        ICStop(instance);
                    } else {
                        throw new IOException("Unable to launch URL: " + result);
                    }
                } else {
                    throw new IOException("Unable to create an Internet Config instance: " + result);
                }
                break;
            case MRJ_3_1:
                try {
                    openURL.invoke(null, new Object[] { url });
                } catch (InvocationTargetException ite) {
                    throw new IOException("InvocationTargetException while calling openURL: " + ite.getMessage());
                } catch (IllegalAccessException iae) {
                    throw new IOException("IllegalAccessException while calling openURL: " + iae.getMessage());
                }
                break;
            case WINDOWS_NT:
            case WINDOWS_9x:
                Process process = Runtime.getRuntime().exec(new String[] { (String) browser, FIRST_WINDOWS_PARAMETER, SECOND_WINDOWS_PARAMETER, THIRD_WINDOWS_PARAMETER, '"' + url + '"' });
                try {
                    process.waitFor();
                    process.exitValue();
                } catch (InterruptedException ie) {
                    throw new IOException("InterruptedException while launching browser: " + ie.getMessage());
                }
                break;
            case OTHER:
                String command = browser.toString() + " -raise " + NETSCAPE_REMOTE_PARAMETER + " " + NETSCAPE_OPEN_PARAMETER_START + url + NETSCAPE_OPEN_PARAMETER_END;
                process = Runtime.getRuntime().exec(command);
                try {
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        Runtime.getRuntime().exec(new String[] { (String) browser, url });
                    }
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                    throw new IOException("InterruptedException while launching browser: " + exception.getMessage());
                }
                break;
            default:
                Runtime.getRuntime().exec(new String[] { (String) browser, url });
                break;
        }
    }

    private native static int ICStart(int[] instance, int signature);

    private native static int ICStop(int[] instance);

    private native static int ICLaunchURL(int instance, byte[] hint, byte[] data, int len, int[] selectionStart, int[] selectionEnd);
}
