package org.opengrok.indexer.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengrok.indexer.logger.LoggerFactory;

public final class IOUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
    }

    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to close resource: ", e);
            }
        }
    }

    public static void removeRecursive(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    public static List<File> listFilesRec(File root) {
        return listFilesRec(root, null);
    }

    public static List<File> listFilesRec(File root, String suffix) {
        List<File> results = new ArrayList<>();
        List<File> files = listFiles(root);
        for (File f : files) {
            if (f.isDirectory() && f.canRead() && !f.getName().equals(".") && !f.getName().equals("..")) {
                results.addAll(listFilesRec(f, suffix));
            } else if (suffix != null && !suffix.isEmpty() && f.getName().endsWith(suffix)) {
                results.add(f);
            } else if (suffix == null || suffix.isEmpty()) {
                results.add(f);
            }
        }
        return results;
    }

    public static List<File> listFiles(File root) {
        return listFiles(root, null);
    }

    public static List<File> listFiles(File root, String suffix) {
        File[] files = root.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (suffix != null && !suffix.isEmpty()) {
                    return name.endsWith(suffix);
                } else {
                    return true;
                }
            }
        });
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(files);
    }

    public static Reader createBOMStrippedReader(InputStream stream) throws IOException {
        return createBOMStrippedReader(stream, Charset.defaultCharset().name());
    }

    public static Reader createBOMStrippedReader(InputStream stream, String defaultCharset) throws IOException {
        InputStream in = stream.markSupported() ? stream : new BufferedInputStream(stream);
        String charset = null;
        in.mark(3);
        byte[] head = new byte[3];
        int br = in.read(head, 0, 3);
        if (br >= 2 && (head[0] == (byte) 0xFE && head[1] == (byte) 0xFF) || (head[0] == (byte) 0xFF && head[1] == (byte) 0xFE)) {
            charset = "UTF-16";
            in.reset();
        } else if (br >= 3 && head[0] == (byte) 0xEF && head[1] == (byte) 0xBB && head[2] == (byte) 0xBF) {
            charset = StandardCharsets.UTF_8.name();
        }
        if (charset == null) {
            in.reset();
            charset = defaultCharset;
        }
        return new InputStreamReader(in, charset);
    }

    private static final Map<String, byte[]> BOMS = new HashMap<>();

    static {
        BOMS.put("UTF-8", new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        BOMS.put("UTF-16BE", new byte[] { (byte) 0xFE, (byte) 0xFF });
        BOMS.put("UTF-16LE", new byte[] { (byte) 0xFF, (byte) 0xFE });
    }

    public static String findBOMEncoding(byte[] sig) {
        for (Map.Entry<String, byte[]> entry : BOMS.entrySet()) {
            String encoding = entry.getKey();
            byte[] bom = entry.getValue();
            if (sig.length > bom.length) {
                int i = 0;
                while (i < bom.length && sig[i] == bom[i]) {
                    i++;
                }
                if (i == bom.length) {
                    return encoding;
                }
            }
        }
        return null;
    }

    public static int skipForBOM(byte[] sig) {
        String encoding = findBOMEncoding(sig);
        if (encoding != null) {
            byte[] bom = BOMS.get(encoding);
            return bom.length;
        }
        return 0;
    }

    public static String getFileContent(File file) {
        if (file == null || !file.canRead()) {
            return "";
        }
        FileReader fin = null;
        BufferedReader input = null;
        try {
            fin = new FileReader(file);
            input = new BufferedReader(fin);
            String line;
            StringBuilder contents = new StringBuilder();
            String EOL = System.getProperty("line.separator");
            while ((line = input.readLine()) != null) {
                contents.append(line).append(EOL);
            }
            return contents.toString();
        } catch (java.io.FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "failed to find file: {0}", e.getMessage());
        } catch (java.io.IOException e) {
            LOGGER.log(Level.WARNING, "failed to read file: {0}", e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            } else if (fin != null) {
                try {
                    fin.close();
                } catch (Exception e) {
                }
            }
        }
        return "";
    }
}
