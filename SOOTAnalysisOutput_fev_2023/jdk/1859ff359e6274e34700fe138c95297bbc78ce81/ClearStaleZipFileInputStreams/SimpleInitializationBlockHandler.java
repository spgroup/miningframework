import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ClearStaleZipFileInputStreams {

    private static final int ZIP_ENTRY_NUM = 5;

    private static final byte[][] data;

    static {
        data = new byte[ZIP_ENTRY_NUM][];
        Random r = new Random();
        for (int i = 0; i < ZIP_ENTRY_NUM; i++) {
            data[i] = new byte[1000];
            r.nextBytes(data[i]);
        }
    }

    private static File createTestFile(int compression) throws Exception {
        File tempZipFile = File.createTempFile("test-data" + compression, ".zip");
        tempZipFile.deleteOnExit();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile));
        zos.setLevel(compression);
        try {
            for (int i = 0; i < ZIP_ENTRY_NUM; i++) {
                String text = "Entry" + i;
                ZipEntry entry = new ZipEntry(text);
                zos.putNextEntry(entry);
                try {
                    zos.write(data[i], 0, data[i].length);
                } finally {
                    zos.closeEntry();
                }
            }
        } finally {
            zos.close();
        }
        return tempZipFile;
    }

    private static void startGcInducingThread(final int sleepMillis) {
        final Thread gcInducingThread = new Thread() {

            public void run() {
                while (true) {
                    System.gc();
                    try {
                        Thread.sleep(sleepMillis);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        gcInducingThread.setDaemon(true);
        gcInducingThread.start();
    }

    public static void main(String[] args) throws Exception {
        startGcInducingThread(500);
        runTest(ZipOutputStream.DEFLATED);
        runTest(ZipOutputStream.STORED);
    }

    private static void runTest(int compression) throws Exception {
        ReferenceQueue<InputStream> rq = new ReferenceQueue<>();
        System.out.println("Testing with a zip file with compression level = " + compression);
        File f = createTestFile(compression);
        try {
            ZipFile zf = new ZipFile(f);
            try {
                Set<Object> refSet = createTransientInputStreams(zf, rq);
                System.out.println("Waiting for 'stale' input streams from ZipFile to be GC'd ...");
                System.out.println("(The test will hang on failure)");
                while (false == refSet.isEmpty()) {
                    refSet.remove(rq.remove());
                }
                System.out.println("Test PASSED.");
                System.out.println();
            } finally {
                zf.close();
            }
        } finally {
            f.delete();
        }
    }

    private static Set<Object> createTransientInputStreams(ZipFile zf, ReferenceQueue<InputStream> rq) throws Exception {
        Enumeration<? extends ZipEntry> zfe = zf.entries();
        Set<Object> refSet = new HashSet<>();
        while (zfe.hasMoreElements()) {
            InputStream is = zf.getInputStream(zfe.nextElement());
            refSet.add(new WeakReference<InputStream>(is, rq));
        }
        return refSet;
    }
}