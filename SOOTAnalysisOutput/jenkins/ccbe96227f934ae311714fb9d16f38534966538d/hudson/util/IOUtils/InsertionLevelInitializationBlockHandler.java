package hudson.util;

import hudson.Functions;
import hudson.os.PosixAPI;
import hudson.os.PosixException;
import org.apache.commons.io.LineIterator;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class IOUtils {

    public static void drain(InputStream in) throws IOException {
        org.apache.commons.io.IOUtils.copy(in, new NullStream());
        in.close();
    }

    public static void copy(File src, OutputStream out) throws IOException {
        FileInputStream in = new FileInputStream(src);
        try {
            org.apache.commons.io.IOUtils.copy(in, out);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(in);
        }
    }

    public static void copy(InputStream in, File out) throws IOException {
        FileOutputStream fos = new FileOutputStream(out);
        try {
            org.apache.commons.io.IOUtils.copy(in, fos);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(fos);
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

    public static final char DIR_SEPARATOR_UNIX = org.apache.commons.io.IOUtils.DIR_SEPARATOR_UNIX;

    public static final char DIR_SEPARATOR_WINDOWS = org.apache.commons.io.IOUtils.DIR_SEPARATOR_WINDOWS;

    public static final char DIR_SEPARATOR = org.apache.commons.io.IOUtils.DIR_SEPARATOR;

    public static final String LINE_SEPARATOR_UNIX = org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;

    public static final String LINE_SEPARATOR_WINDOWS = org.apache.commons.io.IOUtils.LINE_SEPARATOR_WINDOWS;

    public static final String LINE_SEPARATOR;

    static {
        StringWriter buf = new StringWriter(4);
        PrintWriter out = new PrintWriter(buf);
        out.println();
        LINE_SEPARATOR = buf.toString();
    }

    public static void closeQuietly(Reader input) {
        org.apache.commons.io.IOUtils.closeQuietly(input);
    }

    public static void closeQuietly(Writer output) {
        org.apache.commons.io.IOUtils.closeQuietly(output);
    }

    public static void closeQuietly(InputStream input) {
        org.apache.commons.io.IOUtils.closeQuietly(input);
    }

    public static void closeQuietly(OutputStream output) {
        org.apache.commons.io.IOUtils.closeQuietly(output);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        return org.apache.commons.io.IOUtils.toByteArray(input);
    }

    public static byte[] toByteArray(Reader input) throws IOException {
        return org.apache.commons.io.IOUtils.toByteArray(input);
    }

    public static byte[] toByteArray(Reader input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.toByteArray(input, encoding);
    }

    public static byte[] toByteArray(String input) throws IOException {
        return org.apache.commons.io.IOUtils.toByteArray(input);
    }

    public static char[] toCharArray(InputStream is) throws IOException {
        return org.apache.commons.io.IOUtils.toCharArray(is);
    }

    public static char[] toCharArray(InputStream is, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.toCharArray(is, encoding);
    }

    public static char[] toCharArray(Reader input) throws IOException {
        return org.apache.commons.io.IOUtils.toCharArray(input);
    }

    public static String toString(InputStream input) throws IOException {
        return org.apache.commons.io.IOUtils.toString(input);
    }

    public static String toString(InputStream input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.toString(input, encoding);
    }

    public static String toString(Reader input) throws IOException {
        return org.apache.commons.io.IOUtils.toString(input);
    }

    public static String toString(byte[] input) throws IOException {
        return org.apache.commons.io.IOUtils.toString(input);
    }

    public static String toString(byte[] input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.toString(input, encoding);
    }

    public static List readLines(InputStream input) throws IOException {
        return org.apache.commons.io.IOUtils.readLines(input);
    }

    public static List readLines(InputStream input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.readLines(input, encoding);
    }

    public static List readLines(Reader input) throws IOException {
        return org.apache.commons.io.IOUtils.readLines(input);
    }

    public static LineIterator lineIterator(Reader reader) {
        return org.apache.commons.io.IOUtils.lineIterator(reader);
    }

    public static LineIterator lineIterator(InputStream input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.lineIterator(input, encoding);
    }

    public static InputStream toInputStream(String input) {
        return org.apache.commons.io.IOUtils.toInputStream(input);
    }

    public static InputStream toInputStream(String input, String encoding) throws IOException {
        return org.apache.commons.io.IOUtils.toInputStream(input, encoding);
    }

    public static void write(byte[] data, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(byte[] data, Writer output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(byte[] data, Writer output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output, encoding);
    }

    public static void write(char[] data, Writer output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(char[] data, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(char[] data, OutputStream output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output, encoding);
    }

    public static void write(String data, Writer output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(String data, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(String data, OutputStream output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output, encoding);
    }

    public static void write(StringBuffer data, Writer output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(StringBuffer data, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output);
    }

    public static void write(StringBuffer data, OutputStream output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.write(data, output, encoding);
    }

    public static void writeLines(Collection lines, String lineEnding, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.writeLines(lines, lineEnding, output);
    }

    public static void writeLines(Collection lines, String lineEnding, OutputStream output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.writeLines(lines, lineEnding, output, encoding);
    }

    public static void writeLines(Collection lines, String lineEnding, Writer writer) throws IOException {
        org.apache.commons.io.IOUtils.writeLines(lines, lineEnding, writer);
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return org.apache.commons.io.IOUtils.copy(input, output);
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return org.apache.commons.io.IOUtils.copyLarge(input, output);
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        org.apache.commons.io.IOUtils.copy(input, output);
    }

    public static void copy(InputStream input, Writer output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.copy(input, output, encoding);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        return org.apache.commons.io.IOUtils.copy(input, output);
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        return org.apache.commons.io.IOUtils.copyLarge(input, output);
    }

    public static void copy(Reader input, OutputStream output) throws IOException {
        org.apache.commons.io.IOUtils.copy(input, output);
    }

    public static void copy(Reader input, OutputStream output, String encoding) throws IOException {
        org.apache.commons.io.IOUtils.copy(input, output, encoding);
    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        return org.apache.commons.io.IOUtils.contentEquals(input1, input2);
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        return org.apache.commons.io.IOUtils.contentEquals(input1, input2);
    }

    private static final byte[] SKIP_BUFFER = new byte[8192];
}