package util

import java.util.jar.JarEntry
import java.util.jar.JarFile

class JarHelper {

    static boolean classExistsInJarFile (File jarFile, String className) {
        JarFile jar = new JarFile(jarFile)
        String wantedEntryName = convertClassNameToEntryName(className)

        boolean result = false;
        Iterator<JarEntry> jarIterator = jar.entries().iterator()
        while (jarIterator.hasNext() && !result) {
            JarEntry entry = jarIterator.next()
            if (!entry.isDirectory() && entry.getName().endsWith(".class") && entry.getName() == wantedEntryName) {
                result = true
            }
        }
        return result
    }

    private static String convertClassNameToEntryName(String className) {
        return className.replaceAll("\\.", "/") + ".class"
    }
}
