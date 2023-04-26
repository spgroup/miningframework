package org.apache.jmeter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public final class NewDriver {

    private static DynamicClassLoader loader;

    private static String jmDir;

    static {
        List jars = new LinkedList();
        String cp = System.getProperty("java.class.path");
        StringTokenizer tok = new StringTokenizer(cp, File.pathSeparator);
        if (tok.countTokens() == 1 || (tok.countTokens() == 2 && System.getProperty("os.name").toLowerCase().startsWith("mac os x"))) {
            File jar = new File(tok.nextToken());
            try {
                jmDir = jar.getCanonicalFile().getParentFile().getParent();
            } catch (IOException e) {
            }
        } else {
            jmDir = System.getProperty("jmeter.home", "");
            if (jmDir.length() == 0) {
                File userDir = new File(System.getProperty("user.dir"));
                jmDir = userDir.getAbsoluteFile().getParent();
            }
        }
        boolean usesUNC = System.getProperty("os.name").startsWith("Windows");
        StringBuffer classpath = new StringBuffer();
        File[] libDirs = new File[] { new File(jmDir + File.separator + "lib"), new File(jmDir + File.separator + "lib" + File.separator + "ext"), new File(jmDir + File.separator + "lib" + File.separator + "junit") };
        for (int a = 0; a < libDirs.length; a++) {
            File[] libJars = libDirs[a].listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            if (libJars == null) {
                new Throwable("Could not access " + libDirs[a]).printStackTrace();
                continue;
            }
            for (int i = 0; i < libJars.length; i++) {
                try {
                    String s = libJars[i].getPath();
                    if (usesUNC) {
                        if (s.startsWith("\\\\") && !s.startsWith("\\\\\\")) {
                            s = "\\\\" + s;
                        } else if (s.startsWith("//") && !s.startsWith("///")) {
                            s = "//" + s;
                        }
                    }
                    jars.add(new URL("file", "", s));
                    classpath.append(System.getProperty("path.separator"));
                    classpath.append(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.setProperty("java.class.path", System.getProperty("java.class.path") + classpath.toString());
        loader = new DynamicClassLoader((URL[]) jars.toArray(new URL[0]));
    }

    private NewDriver() {
    }

    public static void addURL(String url) {
        File furl = new File(url);
        try {
            loader.addURL(furl.toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

<<<<<<< MINE
=======
    public static void addURL(URL url) {
        loader.addURL(url);
    }

    public static void addPath(String path) throws MalformedURLException {
        URL url = new URL("file", "", path);
        loader.addURL(url);
        StringBuffer sb = new StringBuffer(System.getProperty("java.class.path"));
        sb.append(System.getProperty("path.separator"));
        sb.append(path);
        System.setProperty("java.class.path", sb.toString());
    }

>>>>>>> YOURS
    public static String getJMeterDir() {
        return jmDir;
    }

    public static void main(String[] args) {
        Thread.currentThread().setContextClassLoader(loader);
        if (System.getProperty("log4j.configuration") == null) {
            File conf = new File(jmDir, "bin" + File.separator + "log4j.conf");
            System.setProperty("log4j.configuration", "file:" + conf);
        }
        if (args != null && args.length > 0 && args[0].equals("report")) {
            try {
                Class JMeterReport = loader.loadClass("org.apache.jmeter.JMeterReport");
                Object instance = JMeterReport.newInstance();
                Method startup = JMeterReport.getMethod("start", new Class[] { (new String[0]).getClass() });
                startup.invoke(instance, new Object[] { args });
            } catch (Exception e) {
                e.printStackTrace();
<<<<<<< MINE
=======
                System.out.println("JMeter home directory was detected as: " + jmDir);
>>>>>>> YOURS
            }
        } else {
            try {
                Class JMeter = loader.loadClass("org.apache.jmeter.JMeter");
                Object instance = JMeter.newInstance();
                Method startup = JMeter.getMethod("start", new Class[] { (new String[0]).getClass() });
                startup.invoke(instance, new Object[] { args });
            } catch (Exception e) {
                e.printStackTrace();
<<<<<<< MINE
=======
                System.out.println("JMeter home directory was detected as: " + jmDir);
>>>>>>> YOURS
            }
        }
    }
}
