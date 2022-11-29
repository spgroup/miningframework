package javax.activation;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import com.sun.activation.registries.MailcapFile;
import com.sun.activation.registries.LogSupport;

public class MailcapCommandMap extends CommandMap {

    private MailcapFile[] DB;

    private static final int PROG = 0;

    private static final String confDir;

    static {
        String dir = null;
        try {
            dir = (String) AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    String home = System.getProperty("java.home");
                    String newdir = home + File.separator + "conf";
                    File conf = new File(newdir);
                    if (conf.exists())
                        return newdir + File.separator;
                    else
                        return home + File.separator + "lib" + File.separator;
                }
            });
        } catch (Exception ex) {
        }
        confDir = dir;
    }

    public MailcapCommandMap() {
        super();
        List dbv = new ArrayList(5);
        MailcapFile mf = null;
        dbv.add(null);
        LogSupport.log("MailcapCommandMap: load HOME");
        try {
            String user_home = System.getProperty("user.home");
            if (user_home != null) {
                String path = user_home + File.separator + ".mailcap";
                mf = loadFile(path);
                if (mf != null)
                    dbv.add(mf);
            }
        } catch (SecurityException ex) {
        }
        LogSupport.log("MailcapCommandMap: load SYS");
        try {
            if (confDir != null) {
                mf = loadFile(confDir + "mailcap");
                if (mf != null)
                    dbv.add(mf);
            }
        } catch (SecurityException ex) {
        }
        LogSupport.log("MailcapCommandMap: load JAR");
        loadAllResources(dbv, "META-INF/mailcap");
        LogSupport.log("MailcapCommandMap: load DEF");
        mf = loadResource("/META-INF/mailcap.default");
        if (mf != null)
            dbv.add(mf);
        DB = new MailcapFile[dbv.size()];
        DB = (MailcapFile[]) dbv.toArray(DB);
    }

    private MailcapFile loadResource(String name) {
        InputStream clis = null;
        try {
            clis = SecuritySupport.getResourceAsStream(this.getClass(), name);
            if (clis != null) {
                MailcapFile mf = new MailcapFile(clis);
                if (LogSupport.isLoggable())
                    LogSupport.log("MailcapCommandMap: successfully loaded " + "mailcap file: " + name);
                return mf;
            } else {
                if (LogSupport.isLoggable())
                    LogSupport.log("MailcapCommandMap: not loading " + "mailcap file: " + name);
            }
        } catch (IOException e) {
            if (LogSupport.isLoggable())
                LogSupport.log("MailcapCommandMap: can't load " + name, e);
        } catch (SecurityException sex) {
            if (LogSupport.isLoggable())
                LogSupport.log("MailcapCommandMap: can't load " + name, sex);
        } finally {
            try {
                if (clis != null)
                    clis.close();
            } catch (IOException ex) {
            }
        }
        return null;
    }

    private void loadAllResources(List v, String name) {
        boolean anyLoaded = false;
        try {
            URL[] urls;
            ClassLoader cld = null;
            cld = SecuritySupport.getContextClassLoader();
            if (cld == null)
                cld = this.getClass().getClassLoader();
            if (cld != null)
                urls = SecuritySupport.getResources(cld, name);
            else
                urls = SecuritySupport.getSystemResources(name);
            if (urls != null) {
                if (LogSupport.isLoggable())
                    LogSupport.log("MailcapCommandMap: getResources");
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    InputStream clis = null;
                    if (LogSupport.isLoggable())
                        LogSupport.log("MailcapCommandMap: URL " + url);
                    try {
                        clis = SecuritySupport.openStream(url);
                        if (clis != null) {
                            v.add(new MailcapFile(clis));
                            anyLoaded = true;
                            if (LogSupport.isLoggable())
                                LogSupport.log("MailcapCommandMap: " + "successfully loaded " + "mailcap file from URL: " + url);
                        } else {
                            if (LogSupport.isLoggable())
                                LogSupport.log("MailcapCommandMap: " + "not loading mailcap " + "file from URL: " + url);
                        }
                    } catch (IOException ioex) {
                        if (LogSupport.isLoggable())
                            LogSupport.log("MailcapCommandMap: can't load " + url, ioex);
                    } catch (SecurityException sex) {
                        if (LogSupport.isLoggable())
                            LogSupport.log("MailcapCommandMap: can't load " + url, sex);
                    } finally {
                        try {
                            if (clis != null)
                                clis.close();
                        } catch (IOException cex) {
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (LogSupport.isLoggable())
                LogSupport.log("MailcapCommandMap: can't load " + name, ex);
        }
        if (!anyLoaded) {
            if (LogSupport.isLoggable())
                LogSupport.log("MailcapCommandMap: !anyLoaded");
            MailcapFile mf = loadResource("/" + name);
            if (mf != null)
                v.add(mf);
        }
    }

    private MailcapFile loadFile(String name) {
        MailcapFile mtf = null;
        try {
            mtf = new MailcapFile(name);
        } catch (IOException e) {
        }
        return mtf;
    }

    public MailcapCommandMap(String fileName) throws IOException {
        this();
        if (LogSupport.isLoggable())
            LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
        if (DB[PROG] == null) {
            DB[PROG] = new MailcapFile(fileName);
        }
    }

    public MailcapCommandMap(InputStream is) {
        this();
        LogSupport.log("MailcapCommandMap: load PROG");
        if (DB[PROG] == null) {
            try {
                DB[PROG] = new MailcapFile(is);
            } catch (IOException ex) {
            }
        }
    }

    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
        List cmdList = new ArrayList();
        if (mimeType != null)
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapList(mimeType);
            if (cmdMap != null)
                appendPrefCmdsToList(cmdMap, cmdList);
        }
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
            if (cmdMap != null)
                appendPrefCmdsToList(cmdMap, cmdList);
        }
        CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
        cmdInfos = (CommandInfo[]) cmdList.toArray(cmdInfos);
        return cmdInfos;
    }

    private void appendPrefCmdsToList(Map cmdHash, List cmdList) {
        Iterator verb_enum = cmdHash.keySet().iterator();
        while (verb_enum.hasNext()) {
            String verb = (String) verb_enum.next();
            if (!checkForVerb(cmdList, verb)) {
                List cmdList2 = (List) cmdHash.get(verb);
                String className = (String) cmdList2.get(0);
                cmdList.add(new CommandInfo(verb, className));
            }
        }
    }

    private boolean checkForVerb(List cmdList, String verb) {
        Iterator ee = cmdList.iterator();
        while (ee.hasNext()) {
            String enum_verb = (String) ((CommandInfo) ee.next()).getCommandName();
            if (enum_verb.equals(verb))
                return true;
        }
        return false;
    }

    public synchronized CommandInfo[] getAllCommands(String mimeType) {
        List cmdList = new ArrayList();
        if (mimeType != null)
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapList(mimeType);
            if (cmdMap != null)
                appendCmdsToList(cmdMap, cmdList);
        }
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
            if (cmdMap != null)
                appendCmdsToList(cmdMap, cmdList);
        }
        CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
        cmdInfos = (CommandInfo[]) cmdList.toArray(cmdInfos);
        return cmdInfos;
    }

    private void appendCmdsToList(Map typeHash, List cmdList) {
        Iterator verb_enum = typeHash.keySet().iterator();
        while (verb_enum.hasNext()) {
            String verb = (String) verb_enum.next();
            List cmdList2 = (List) typeHash.get(verb);
            Iterator cmd_enum = ((List) cmdList2).iterator();
            while (cmd_enum.hasNext()) {
                String cmd = (String) cmd_enum.next();
                cmdList.add(new CommandInfo(verb, cmd));
            }
        }
    }

    public synchronized CommandInfo getCommand(String mimeType, String cmdName) {
        if (mimeType != null)
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapList(mimeType);
            if (cmdMap != null) {
                List v = (List) cmdMap.get(cmdName);
                if (v != null) {
                    String cmdClassName = (String) v.get(0);
                    if (cmdClassName != null)
                        return new CommandInfo(cmdName, cmdClassName);
                }
            }
        }
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
            if (cmdMap != null) {
                List v = (List) cmdMap.get(cmdName);
                if (v != null) {
                    String cmdClassName = (String) v.get(0);
                    if (cmdClassName != null)
                        return new CommandInfo(cmdName, cmdClassName);
                }
            }
        }
        return null;
    }

    public synchronized void addMailcap(String mail_cap) {
        LogSupport.log("MailcapCommandMap: add to PROG");
        if (DB[PROG] == null)
            DB[PROG] = new MailcapFile();
        DB[PROG].appendToMailcap(mail_cap);
    }

    public synchronized DataContentHandler createDataContentHandler(String mimeType) {
        if (LogSupport.isLoggable())
            LogSupport.log("MailcapCommandMap: createDataContentHandler for " + mimeType);
        if (mimeType != null)
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            if (LogSupport.isLoggable())
                LogSupport.log("  search DB #" + i);
            Map cmdMap = DB[i].getMailcapList(mimeType);
            if (cmdMap != null) {
                List v = (List) cmdMap.get("content-handler");
                if (v != null) {
                    String name = (String) v.get(0);
                    DataContentHandler dch = getDataContentHandler(name);
                    if (dch != null)
                        return dch;
                }
            }
        }
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            if (LogSupport.isLoggable())
                LogSupport.log("  search fallback DB #" + i);
            Map cmdMap = DB[i].getMailcapFallbackList(mimeType);
            if (cmdMap != null) {
                List v = (List) cmdMap.get("content-handler");
                if (v != null) {
                    String name = (String) v.get(0);
                    DataContentHandler dch = getDataContentHandler(name);
                    if (dch != null)
                        return dch;
                }
            }
        }
        return null;
    }

    private DataContentHandler getDataContentHandler(String name) {
        if (LogSupport.isLoggable())
            LogSupport.log("    got content-handler");
        if (LogSupport.isLoggable())
            LogSupport.log("      class " + name);
        try {
            ClassLoader cld = null;
            cld = SecuritySupport.getContextClassLoader();
            if (cld == null)
                cld = this.getClass().getClassLoader();
            Class cl = null;
            try {
                cl = cld.loadClass(name);
            } catch (Exception ex) {
                cl = Class.forName(name);
            }
            return (DataContentHandler) cl.newInstance();
        } catch (IllegalAccessException e) {
            if (LogSupport.isLoggable())
                LogSupport.log("Can't load DCH " + name, e);
        } catch (ClassNotFoundException e) {
            if (LogSupport.isLoggable())
                LogSupport.log("Can't load DCH " + name, e);
        } catch (InstantiationException e) {
            if (LogSupport.isLoggable())
                LogSupport.log("Can't load DCH " + name, e);
        }
        return null;
    }

    public synchronized String[] getMimeTypes() {
        List mtList = new ArrayList();
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            String[] ts = DB[i].getMimeTypes();
            if (ts != null) {
                for (int j = 0; j < ts.length; j++) {
                    if (!mtList.contains(ts[j]))
                        mtList.add(ts[j]);
                }
            }
        }
        String[] mts = new String[mtList.size()];
        mts = (String[]) mtList.toArray(mts);
        return mts;
    }

    public synchronized String[] getNativeCommands(String mimeType) {
        List cmdList = new ArrayList();
        if (mimeType != null)
            mimeType = mimeType.toLowerCase(Locale.ENGLISH);
        for (int i = 0; i < DB.length; i++) {
            if (DB[i] == null)
                continue;
            String[] cmds = DB[i].getNativeCommands(mimeType);
            if (cmds != null) {
                for (int j = 0; j < cmds.length; j++) {
                    if (!cmdList.contains(cmds[j]))
                        cmdList.add(cmds[j]);
                }
            }
        }
        String[] cmds = new String[cmdList.size()];
        cmds = (String[]) cmdList.toArray(cmds);
        return cmds;
    }
}
