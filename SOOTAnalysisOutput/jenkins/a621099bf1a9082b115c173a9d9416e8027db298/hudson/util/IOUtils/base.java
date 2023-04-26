package hudson.util;

import hudson.Functions;
import hudson.os.PosixAPI;
import hudson.os.PosixException;
import java.io.*;
import java.util.regex.Pattern;

public class IOUtils extends org.apache.commons.io.IOUtils {

    public static void drain(InputStream in) throws IOException {
        copy(in, new NullStream());
        in.close();
    }

    public static void copy(File src, OutputStream out) throws IOException {
        FileInputStream in = new FileInputStream(src);
        try {
            copy(in, out);
        } finally {
            closeQuietly(in);
        }
    }

    public static void copy(InputStream in, File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out);
        try {
            copy(in, fos);
        } finally {
            closeQuietly(fos);
        }
    }

    public static File mkdirs(File dir) throws IOException {
        if (dir.mkdirs() || dir.exists())
            return dir;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        if (dir.mkdirs() || dir.exists())
            return dir;
        throw new IOException("Failed to create a directory at " + dir);
    }

    public static InputStream skip(InputStream in, long size) throws IOException {
        DataInputStream di = new DataInputStream(in);
        while (size > 0) {
            int chunk = (int) Math.min(SKIP_BUFFER.length, size);
            di.readFully(SKIP_BUFFER, 0, chunk);
            size -= chunk;
        }
        return in;
    }

    public static File absolutize(File base, String path) {
        if (isAbsolute(path))
            return new File(path);
        return new File(base, path);
    }

    public static boolean isAbsolute(String path) {
        Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:[\\\\/].*");
        return path.startsWith("/") || DRIVE_PATTERN.matcher(path).matches();
    }

    public static int mode(File f) throws PosixException {
        if (Functions.isWindows())
            return -1;
        return PosixAPI.jnr().stat(f.getPath()).mode();
    }

    public static String readFirstLine(InputStream is, String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(encoding == null ? new InputStreamReader(is) : new InputStreamReader(is, encoding));
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    private static final byte[] SKIP_BUFFER = new byte[8192];
}
