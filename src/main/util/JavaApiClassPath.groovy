package util

import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Class path with the Java API for Soot 4.0.0, which cannot read the JDK modules of Java 9+.
 * On Java 8 it is the rt.jar of the running JVM. On newer JVMs, a jar with the Java 8 API is extracted from the
 * lib/ct.sym of the running JDK: it only has signatures, so its methods are marked as native (no body).
 */
class JavaApiClassPath {

    private static final int ACC_NATIVE = 0x0100
    private static final int ACC_ABSTRACT = 0x0400
    private static final Map<Integer, Integer> CONSTANT_SIZES = [3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4, 11: 4,
                                                                 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2]

    static String get(File cacheDir = new File(System.getProperty('java.io.tmpdir'), 'miningframework')) {
        File javaHome = new File(System.getProperty('java.home'))
        File rtJar = new File(javaHome, 'lib/rt.jar')
        if (rtJar.exists()) {
            return rtJar.path
        }
        File stubs = new File(cacheDir, 'jdk8-api-stubs.jar')
        if (!stubs.exists()) {
            cacheDir.mkdirs()
            File tmp = new File(cacheDir, 'jdk8-api-stubs.jar.tmp')
            createJava8Stubs(new File(javaHome, 'lib/ct.sym'), tmp)
            tmp.renameTo(stubs)
        }
        return stubs.path
    }

    private static void createJava8Stubs(File ctSym, File destination) {
        Set<String> written = []
        new ZipFile(ctSym).withCloseable { ZipFile source ->
            new ZipOutputStream(destination.newOutputStream()).withCloseable { ZipOutputStream out ->
                for (ZipEntry entry : source.entries()) {
                    List<String> parts = entry.name.split('/') as List
                    // ct.sym folders combine release codes ("8", "87", "879A"...): keep the ones with the Java 8 API
                    if (!entry.name.endsWith('.sig') || !parts[0].contains('8') || parts.size() < 3) {
                        continue
                    }
                    int first = parts[1].contains('.') ? 2 : 1 // skips the module folder (e.g. java.base)
                    String path = parts.subList(first, parts.size()).join('/').replaceAll(/\.sig$/, '.class')
                    if (written.add(path)) {
                        out.putNextEntry(new ZipEntry(path))
                        out.write(toJava8Stub(source.getInputStream(entry).bytes))
                        out.closeEntry()
                    }
                }
            }
        }
    }

    /** Sets the class file version to Java 8 and marks every non abstract method as native. */
    private static byte[] toJava8Stub(byte[] classFile) {
        ByteBuffer bytes = ByteBuffer.wrap(classFile)
        bytes.putShort(4, (short) 0) // minor version
        bytes.putShort(6, (short) 52) // major version
        int position = 10
        int constants = Short.toUnsignedInt(bytes.getShort(8))
        for (int i = 1; i < constants; i++) {
            int tag = bytes.get(position)
            position += tag == 1 ? 3 + Short.toUnsignedInt(bytes.getShort(position + 1)) : 1 + CONSTANT_SIZES[tag]
            if (tag == 5 || tag == 6) {
                i++ // long and double take two slots
            }
        }
        position += 6 // access flags, this class, super class
        position += 2 + 2 * Short.toUnsignedInt(bytes.getShort(position)) // interfaces
        position = markMembers(bytes, position, false) // fields
        markMembers(bytes, position, true) // methods
        return classFile
    }

    private static int markMembers(ByteBuffer bytes, int position, boolean markAsNative) {
        int members = Short.toUnsignedInt(bytes.getShort(position))
        position += 2
        members.times {
            int flags = Short.toUnsignedInt(bytes.getShort(position))
            if (markAsNative && (flags & ACC_ABSTRACT) == 0) {
                bytes.putShort(position, (short) (flags | ACC_NATIVE))
            }
            int attributes = Short.toUnsignedInt(bytes.getShort(position + 6))
            position += 8
            attributes.times { position += 6 + bytes.getInt(position + 2) }
        }
        return position
    }
}
