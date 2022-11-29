import sun.jvm.hotspot.tools.*;
import sun.jvm.hotspot.runtime.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

public class SASanityChecker extends Tool {

    private static final String saJarName;

    private static final Map c2types;

    static {
        saJarName = System.getProperty("SASanityChecker.SAJarName", "sa-jdi.jar");
        c2types = new HashMap();
        Object value = new Object();
        c2types.put("sun.jvm.hotspot.code.ExceptionBlob", value);
        c2types.put("sun.jvm.hotspot.code.DeoptimizationBlob", value);
        c2types.put("sun.jvm.hotspot.code.UncommonTrapBlob", value);
    }

    public void run() {
        String classPath = System.getProperty("java.class.path");
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        String saJarPath = null;
        while (st.hasMoreTokens()) {
            saJarPath = st.nextToken();
            if (saJarPath.endsWith(saJarName)) {
                break;
            }
        }
        if (saJarPath == null) {
            throw new RuntimeException(saJarName + " is not the CLASSPATH");
        }
        String cpuDot = "." + VM.getVM().getCPU() + ".";
        String platformDot = "." + VM.getVM().getOS() + "_" + VM.getVM().getCPU() + ".";
        boolean isClient = VM.getVM().isClientCompiler();
        try {
            FileInputStream fis = new FileInputStream(saJarPath);
            JarInputStream jis = new JarInputStream(fis);
            JarEntry je = null;
            while ((je = jis.getNextJarEntry()) != null) {
                String entryName = je.getName();
                int dotClassIndex = entryName.indexOf(".class");
                if (dotClassIndex == -1) {
                    continue;
                }
                entryName = entryName.substring(0, dotClassIndex).replace('/', '.');
                if (entryName.startsWith("sun.jvm.hotspot.debugger.") || entryName.startsWith("sun.jvm.hotspot.asm.") || entryName.startsWith("sun.jvm.hotspot.type.") || entryName.startsWith("sun.jvm.hotspot.jdi.")) {
                    continue;
                }
                String runtimePkgPrefix = "sun.jvm.hotspot.runtime.";
                int runtimeIndex = entryName.indexOf(runtimePkgPrefix);
                if (runtimeIndex != -1) {
                    if (entryName.substring(runtimePkgPrefix.length() + 1, entryName.length()).indexOf('.') != -1) {
                        if (entryName.indexOf(cpuDot) == -1 && entryName.indexOf(platformDot) == -1) {
                            continue;
                        }
                    }
                }
                if (isClient) {
                    if (c2types.get(entryName) != null) {
                        continue;
                    }
                } else {
                    if (entryName.equals("sun.jvm.hotspot.c1.Runtime1")) {
                        continue;
                    }
                }
                System.out.println("checking " + entryName + " ..");
                Class.forName(entryName);
            }
        } catch (Exception exp) {
            System.out.println();
            System.out.println("FAILED");
            System.out.println();
            throw new RuntimeException(exp.getMessage());
        }
        System.out.println();
        System.out.println("PASSED");
        System.out.println();
    }

    public static void main(String[] args) {
        SASanityChecker checker = new SASanityChecker();
        checker.start(args);
        checker.stop();
    }
}
