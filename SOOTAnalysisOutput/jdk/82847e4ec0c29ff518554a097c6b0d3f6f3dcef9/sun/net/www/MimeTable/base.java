package sun.net.www;

import java.io.*;
import java.net.FileNameMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

public class MimeTable implements FileNameMap {

    private Hashtable<String, MimeEntry> entries = new Hashtable<String, MimeEntry>();

    private Hashtable<String, MimeEntry> extensionMap = new Hashtable<String, MimeEntry>();

    private static String tempFileTemplate;

    static {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

            public Void run() {
                tempFileTemplate = System.getProperty("content.types.temp.file.template", "/tmp/%s");
                mailcapLocations = new String[] { System.getProperty("user.mailcap"), System.getProperty("user.home") + "/.mailcap", "/etc/mailcap", "/usr/etc/mailcap", "/usr/local/etc/mailcap", System.getProperty("hotjava.home", "/usr/local/hotjava") + "/lib/mailcap" };
                return null;
            }
        });
    }

    private static final String filePreamble = "sun.net.www MIME content-types table";

    private static final String fileMagic = "#" + filePreamble;

    MimeTable() {
        load();
    }

    private static class DefaultInstanceHolder {

        static final MimeTable defaultInstance = getDefaultInstance();

        static MimeTable getDefaultInstance() {
            return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<MimeTable>() {

                public MimeTable run() {
                    MimeTable instance = new MimeTable();
                    URLConnection.setFileNameMap(instance);
                    return instance;
                }
            });
        }
    }

    public static MimeTable getDefaultTable() {
        return DefaultInstanceHolder.defaultInstance;
    }

    public static FileNameMap loadTable() {
        MimeTable mt = getDefaultTable();
        return (FileNameMap) mt;
    }

    public synchronized int getSize() {
        return entries.size();
    }

    public synchronized String getContentTypeFor(String fileName) {
        MimeEntry entry = findByFileName(fileName);
        if (entry != null) {
            return entry.getType();
        } else {
            return null;
        }
    }

    public synchronized void add(MimeEntry m) {
        entries.put(m.getType(), m);
        String[] exts = m.getExtensions();
        if (exts == null) {
            return;
        }
        for (int i = 0; i < exts.length; i++) {
            extensionMap.put(exts[i], m);
        }
    }

    public synchronized MimeEntry remove(String type) {
        MimeEntry entry = entries.get(type);
        return remove(entry);
    }

    public synchronized MimeEntry remove(MimeEntry entry) {
        String[] extensionKeys = entry.getExtensions();
        if (extensionKeys != null) {
            for (int i = 0; i < extensionKeys.length; i++) {
                extensionMap.remove(extensionKeys[i]);
            }
        }
        return entries.remove(entry.getType());
    }

    public synchronized MimeEntry find(String type) {
        MimeEntry entry = entries.get(type);
        if (entry == null) {
            Enumeration<MimeEntry> e = entries.elements();
            while (e.hasMoreElements()) {
                MimeEntry wild = e.nextElement();
                if (wild.matches(type)) {
                    return wild;
                }
            }
        }
        return entry;
    }

    public MimeEntry findByFileName(String fname) {
        String ext = "";
        int i = fname.lastIndexOf('#');
        if (i > 0) {
            fname = fname.substring(0, i - 1);
        }
        i = fname.lastIndexOf('.');
        i = Math.max(i, fname.lastIndexOf('/'));
        i = Math.max(i, fname.lastIndexOf('?'));
        if (i != -1 && fname.charAt(i) == '.') {
            ext = fname.substring(i).toLowerCase();
        }
        return findByExt(ext);
    }

    public synchronized MimeEntry findByExt(String fileExtension) {
        return extensionMap.get(fileExtension);
    }

    public synchronized MimeEntry findByDescription(String description) {
        Enumeration<MimeEntry> e = elements();
        while (e.hasMoreElements()) {
            MimeEntry entry = e.nextElement();
            if (description.equals(entry.getDescription())) {
                return entry;
            }
        }
        return find(description);
    }

    String getTempFileTemplate() {
        return tempFileTemplate;
    }

    public synchronized Enumeration<MimeEntry> elements() {
        return entries.elements();
    }

    protected static String[] mailcapLocations;

    public synchronized void load() {
        Properties entries = new Properties();
        File file = null;
        InputStream in;
        String userTablePath = System.getProperty("content.types.user.table");
        if (userTablePath != null && (file = new File(userTablePath)).exists()) {
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                System.err.println("Warning: " + file.getPath() + " mime table not found.");
                return;
            }
        } else {
            in = MimeTable.class.getResourceAsStream("content-types.properties");
            if (in == null)
                throw new InternalError("default mime table not found");
        }
        try (BufferedInputStream bin = new BufferedInputStream(in)) {
            entries.load(bin);
        } catch (IOException e) {
            System.err.println("Warning: " + e.getMessage());
        }
        parse(entries);
    }

    void parse(Properties entries) {
        String tempFileTemplate = (String) entries.get("temp.file.template");
        if (tempFileTemplate != null) {
            entries.remove("temp.file.template");
            MimeTable.tempFileTemplate = tempFileTemplate;
        }
        Enumeration<?> types = entries.propertyNames();
        while (types.hasMoreElements()) {
            String type = (String) types.nextElement();
            String attrs = entries.getProperty(type);
            parse(type, attrs);
        }
    }

    void parse(String type, String attrs) {
        MimeEntry newEntry = new MimeEntry(type);
        StringTokenizer tokenizer = new StringTokenizer(attrs, ";");
        while (tokenizer.hasMoreTokens()) {
            String pair = tokenizer.nextToken();
            parse(pair, newEntry);
        }
        add(newEntry);
    }

    void parse(String pair, MimeEntry entry) {
        String name = null;
        String value = null;
        boolean gotName = false;
        StringTokenizer tokenizer = new StringTokenizer(pair, "=");
        while (tokenizer.hasMoreTokens()) {
            if (gotName) {
                value = tokenizer.nextToken().trim();
            } else {
                name = tokenizer.nextToken().trim();
                gotName = true;
            }
        }
        fill(entry, name, value);
    }

    void fill(MimeEntry entry, String name, String value) {
        if ("description".equalsIgnoreCase(name)) {
            entry.setDescription(value);
        } else if ("action".equalsIgnoreCase(name)) {
            entry.setAction(getActionCode(value));
        } else if ("application".equalsIgnoreCase(name)) {
            entry.setCommand(value);
        } else if ("icon".equalsIgnoreCase(name)) {
            entry.setImageFileName(value);
        } else if ("file_extensions".equalsIgnoreCase(name)) {
            entry.setExtensions(value);
        }
    }

    String[] getExtensions(String list) {
        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        int n = tokenizer.countTokens();
        String[] extensions = new String[n];
        for (int i = 0; i < n; i++) {
            extensions[i] = tokenizer.nextToken();
        }
        return extensions;
    }

    int getActionCode(String action) {
        for (int i = 0; i < MimeEntry.actionKeywords.length; i++) {
            if (action.equalsIgnoreCase(MimeEntry.actionKeywords[i])) {
                return i;
            }
        }
        return MimeEntry.UNKNOWN;
    }

    public Properties getAsProperties() {
        Properties properties = new Properties();
        Enumeration<MimeEntry> e = elements();
        while (e.hasMoreElements()) {
            MimeEntry entry = e.nextElement();
            properties.put(entry.getType(), entry.toProperty());
        }
        return properties;
    }

    protected boolean saveAsProperties(File file) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Properties properties = getAsProperties();
            properties.put("temp.file.template", tempFileTemplate);
            String tag;
            String user = System.getProperty("user.name");
            if (user != null) {
                tag = "; customized for " + user;
                properties.store(os, filePreamble + tag);
            } else {
                properties.store(os, filePreamble);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }
}
