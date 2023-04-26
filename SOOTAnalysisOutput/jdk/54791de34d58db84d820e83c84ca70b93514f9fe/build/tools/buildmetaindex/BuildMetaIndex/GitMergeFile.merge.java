package build.tools.buildmetaindex;

import java.io.*;
import java.util.*;
import java.util.jar.*;

public class BuildMetaIndex {

    public static void main(String[] args) throws IOException {
        if (args.length < 3 || !args[0].equals("-o")) {
            printUsage();
            System.exit(1);
        }
        try {
            PrintStream out = new PrintStream(new FileOutputStream(args[1]));
            out.println("% VERSION 2");
            out.println("% WARNING: this file is auto-generated; do not edit");
            out.println("% UNSUPPORTED: this file and its format may change and/or");
            out.println("%   may be removed in a future release");
            for (int i = 2; i < args.length; i++) {
                String filename = args[i];
                JarMetaIndex jmi = new JarMetaIndex(filename);
                HashSet<String> index = jmi.getMetaIndex();
                if (index == null) {
                    continue;
                }
                out.println(jmi.getJarFileKind().getMarkerChar() + " " + filename);
                for (String entry : index) {
                    out.println(entry);
                }
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("FileNotFoundException occurred");
            System.exit(2);
        }
    }

    private static void printUsage() {
        String usage = "BuildMetaIndex is used to generate a meta index file for the jar files\n" + "you specified. The following is its usage:\n" + " java BuildMetaIndex -o <the output meta index file> <a list of jar files> \n" + " You can specify *.jar to refer to all the jar files in the current directory";
        System.err.println(usage);
    }
}

enum JarFileKind {

    CLASSONLY('!'), RESOURCEONLY('@'), MIXED('#');

    private char markerChar;

    JarFileKind(char markerChar) {
        this.markerChar = markerChar;
    }

    public char getMarkerChar() {
        return markerChar;
    }
}

class JarMetaIndex {

    private JarFile jar;

    private volatile HashSet<String> indexSet;

    private HashMap<String, HashSet<String>> knownPrefixMap = new HashMap<>();

    static class ExtraLevel {

        public ExtraLevel(String prefix, int levels) {
            this.prefix = prefix;
            this.levels = levels;
        }

        String prefix;

        int levels;
    }

    private static ArrayList<ExtraLevel> extraLevels = new ArrayList<>();

    static {
        extraLevels.add(new ExtraLevel("com/sun/java/util/", Integer.MAX_VALUE));
        extraLevels.add(new ExtraLevel("com/sun/java/", 4));
        extraLevels.add(new ExtraLevel("com/sun/", 3));
        extraLevels.add(new ExtraLevel("com/oracle/jrockit", 3));
    }

    private static final int MAX_PKGS_WITH_KNOWN_PREFIX = 5;

    private JarFileKind jarFileKind;

    JarMetaIndex(String fileName) throws IOException {
        jar = new JarFile(fileName);
        knownPrefixMap.put("sun", new HashSet<String>());
        knownPrefixMap.put("java", new HashSet<String>());
        knownPrefixMap.put("javax", new HashSet<String>());
    }

    HashSet<String> getMetaIndex() {
        if (indexSet == null) {
            synchronized (this) {
                if (indexSet == null) {
                    indexSet = new HashSet<>();
                    Enumeration<JarEntry> entries = jar.entries();
                    boolean containsOnlyClass = true;
                    boolean containsOnlyResource = true;
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (entry.isDirectory() || name.equals("META-INF/MANIFEST.MF")) {
                            continue;
                        }
                        if (containsOnlyResource || containsOnlyClass) {
                            if (name.endsWith(".class")) {
                                containsOnlyResource = false;
                            } else {
                                containsOnlyClass = false;
                            }
                        }
                        if (name.startsWith("META-INF")) {
                            indexSet.add(name);
                            continue;
                        }
                        if (isPrefixKnown(name)) {
                            continue;
                        }
                        String[] pkgElements = name.split("/");
                        if (pkgElements.length > 2) {
                            String meta = "";
                            int levels = 2;
                            for (ExtraLevel el : extraLevels) {
                                if (name.startsWith(el.prefix)) {
                                    levels = el.levels;
                                    break;
                                }
                            }
                            for (int i = 0; i < levels && i < pkgElements.length - 1; i++) {
                                meta += pkgElements[i] + "/";
                            }
                            if (!meta.equals("")) {
                                indexSet.add(meta);
                            }
                        }
                    }
                    addKnownPrefix();
                    if (containsOnlyClass) {
                        jarFileKind = JarFileKind.CLASSONLY;
                    } else if (containsOnlyResource) {
                        jarFileKind = JarFileKind.RESOURCEONLY;
                    } else {
                        jarFileKind = JarFileKind.MIXED;
                    }
                }
            }
        }
        return indexSet;
    }

    boolean isPrefixKnown(String name) {
        int firstSlashIndex = name.indexOf("/");
        if (firstSlashIndex == -1) {
            return false;
        }
        String firstPkgElement = name.substring(0, firstSlashIndex);
        HashSet<String> pkgSet = knownPrefixMap.get(firstPkgElement);
        if (pkgSet == null) {
            return false;
        }
        String secondPkgElement = name.substring(firstSlashIndex + 1, name.indexOf("/", firstSlashIndex + 1));
        if (secondPkgElement != null) {
            pkgSet.add(secondPkgElement);
        }
        return true;
    }

    void addKnownPrefix() {
        if (indexSet == null) {
            return;
        }
        for (String key : knownPrefixMap.keySet()) {
            HashSet<String> pkgSetStartsWithKey = knownPrefixMap.get(key);
            int setSize = pkgSetStartsWithKey.size();
            if (setSize == 0) {
                continue;
            } else if (setSize > JarMetaIndex.MAX_PKGS_WITH_KNOWN_PREFIX) {
                indexSet.add(key + "/");
            } else {
                for (String secondPkgElement : pkgSetStartsWithKey) {
                    indexSet.add(key + "/" + secondPkgElement);
                }
            }
        }
    }

    JarFileKind getJarFileKind() {
        if (indexSet == null) {
            indexSet = getMetaIndex();
        }
        return jarFileKind;
    }
}
