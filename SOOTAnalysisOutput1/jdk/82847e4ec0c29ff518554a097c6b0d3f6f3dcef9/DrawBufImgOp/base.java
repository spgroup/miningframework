import java.awt.*;
import java.awt.image.*;
import java.io.File;
import javax.imageio.ImageIO;

public class DrawBufImgOp extends Canvas {

    private static final int TESTW = 600;

    private static final int TESTH = 500;

    private static boolean done;

    private static boolean ignore;

    private static final int[] srcSizes = { 32, 17 };

    private static final int[] srcTypes = { BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY };

    private static final RescaleOp rescale1band, rescale3band, rescale4band;

    private static final LookupOp lookup1bandbyte, lookup3bandbyte, lookup4bandbyte;

    private static final LookupOp lookup1bandshort, lookup3bandshort, lookup4bandshort;

    private static final ConvolveOp convolve3x3zero, convolve5x5zero, convolve7x7zero;

    private static final ConvolveOp convolve3x3noop, convolve5x5noop, convolve7x7noop;

    static {
        rescale1band = new RescaleOp(0.5f, 10.0f, null);
        rescale3band = new RescaleOp(new float[] { 0.6f, 0.4f, 0.6f }, new float[] { 10.0f, -3.0f, 5.0f }, null);
        rescale4band = new RescaleOp(new float[] { 0.6f, 0.4f, 0.6f, 0.9f }, new float[] { -1.0f, 5.0f, 3.0f, 1.0f }, null);
        int offset = 0;
        {
            byte[] invert = new byte[256];
            byte[] halved = new byte[256];
            for (int j = 0; j < 256; j++) {
                invert[j] = (byte) (255 - j);
                halved[j] = (byte) (j / 2);
            }
            ByteLookupTable lut1 = new ByteLookupTable(offset, invert);
            lookup1bandbyte = new LookupOp(lut1, null);
            ByteLookupTable lut3 = new ByteLookupTable(offset, new byte[][] { invert, halved, invert });
            lookup3bandbyte = new LookupOp(lut3, null);
            ByteLookupTable lut4 = new ByteLookupTable(offset, new byte[][] { invert, halved, invert, halved });
            lookup4bandbyte = new LookupOp(lut4, null);
        }
        {
            short[] invert = new short[256];
            short[] halved = new short[256];
            for (int j = 0; j < 256; j++) {
                invert[j] = (short) ((255 - j) * 255);
                halved[j] = (short) ((j / 2) * 255);
            }
            ShortLookupTable lut1 = new ShortLookupTable(offset, invert);
            lookup1bandshort = new LookupOp(lut1, null);
            ShortLookupTable lut3 = new ShortLookupTable(offset, new short[][] { invert, halved, invert });
            lookup3bandshort = new LookupOp(lut3, null);
            ShortLookupTable lut4 = new ShortLookupTable(offset, new short[][] { invert, halved, invert, halved });
            lookup4bandshort = new LookupOp(lut4, null);
        }
        float[] data3 = { 0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f };
        Kernel k3 = new Kernel(3, 3, data3);
        float[] data5 = { -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 24.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f };
        Kernel k5 = new Kernel(5, 5, data5);
        float[] data7 = { 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f, 0.02f };
        Kernel k7 = new Kernel(7, 7, data7);
        convolve3x3zero = new ConvolveOp(k3, ConvolveOp.EDGE_ZERO_FILL, null);
        convolve5x5zero = new ConvolveOp(k5, ConvolveOp.EDGE_ZERO_FILL, null);
        convolve7x7zero = new ConvolveOp(k7, ConvolveOp.EDGE_ZERO_FILL, null);
        convolve3x3noop = new ConvolveOp(k3, ConvolveOp.EDGE_NO_OP, null);
        convolve5x5noop = new ConvolveOp(k5, ConvolveOp.EDGE_NO_OP, null);
        convolve7x7noop = new ConvolveOp(k7, ConvolveOp.EDGE_NO_OP, null);
    }

    public void paint(Graphics g) {
        synchronized (this) {
            if (done) {
                return;
            }
        }
        VolatileImage vimg = createVolatileImage(TESTW, TESTH);
        vimg.validate(getGraphicsConfiguration());
        Graphics2D g2d = vimg.createGraphics();
        renderTest(g2d);
        g2d.dispose();
        g.drawImage(vimg, 0, 0, null);
        Toolkit.getDefaultToolkit().sync();
        synchronized (this) {
            done = true;
            notifyAll();
        }
    }

    private void renderTest(Graphics2D g2d) {
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, TESTW, TESTH);
        int yorig = 2;
        int xinc = 34;
        int yinc = srcSizes[0] + srcSizes[1] + 2 + 2;
        for (int srcType : srcTypes) {
            int y = yorig;
            for (int srcSize : srcSizes) {
                int x = 2;
                System.out.printf("type=%d size=%d\n", srcType, srcSize);
                BufferedImage srcImg = makeSourceImage(srcSize, srcType);
                ColorModel srcCM = srcImg.getColorModel();
                g2d.drawImage(srcImg, rescale1band, x, y);
                x += xinc;
                if (srcCM.getNumColorComponents() == 3 && !(ignore && srcCM.hasAlpha())) {
                    g2d.drawImage(srcImg, rescale3band, x, y);
                }
                x += xinc;
                if (srcCM.getNumComponents() == 4) {
                    g2d.drawImage(srcImg, rescale4band, x, y);
                }
                x += xinc;
                if (srcType != BufferedImage.TYPE_USHORT_GRAY) {
                    g2d.drawImage(srcImg, lookup1bandbyte, x, y);
                    x += xinc;
                    if (srcCM.getNumColorComponents() == 3) {
                        g2d.drawImage(srcImg, lookup3bandbyte, x, y);
                    }
                    x += xinc;
                    if (srcCM.getNumComponents() == 4) {
                        g2d.drawImage(srcImg, lookup4bandbyte, x, y);
                    }
                    x += xinc;
                    if (!(ignore && (srcType == BufferedImage.TYPE_3BYTE_BGR || srcType == BufferedImage.TYPE_4BYTE_ABGR))) {
                        g2d.drawImage(srcImg, lookup1bandshort, x, y);
                        x += xinc;
                        if (srcCM.getNumColorComponents() == 3 && !(ignore && srcCM.hasAlpha())) {
                            g2d.drawImage(srcImg, lookup3bandshort, x, y);
                        }
                        x += xinc;
                        if (srcCM.getNumComponents() == 4) {
                            g2d.drawImage(srcImg, lookup4bandshort, x, y);
                        }
                        x += xinc;
                    } else {
                        x += 3 * xinc;
                    }
                } else {
                    x += 6 * xinc;
                }
                if (srcType != BufferedImage.TYPE_3BYTE_BGR) {
                    g2d.drawImage(srcImg, convolve3x3zero, x, y);
                    x += xinc;
                    g2d.drawImage(srcImg, convolve5x5zero, x, y);
                    x += xinc;
                    g2d.drawImage(srcImg, convolve7x7zero, x, y);
                    x += xinc;
                    g2d.drawImage(srcImg, convolve3x3noop, x, y);
                    x += xinc;
                    g2d.drawImage(srcImg, convolve5x5noop, x, y);
                    x += xinc;
                    g2d.drawImage(srcImg, convolve7x7noop, x, y);
                    x += xinc;
                } else {
                    x += 6 * xinc;
                }
                y += srcSize + 2;
            }
            yorig += yinc;
        }
    }

    private BufferedImage makeSourceImage(int size, int type) {
        int s2 = size / 2;
        BufferedImage img = new BufferedImage(size, size, type);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.orange);
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(Color.red);
        g2d.fillRect(0, 0, s2, s2);
        g2d.setColor(Color.green);
        g2d.fillRect(s2, 0, s2, s2);
        g2d.setColor(Color.blue);
        g2d.fillRect(0, s2, s2, s2);
        g2d.setColor(new Color(255, 255, 0, 128));
        g2d.fillRect(s2, s2, s2, s2);
        g2d.setColor(Color.pink);
        g2d.fillOval(s2 - 3, s2 - 3, 6, 6);
        g2d.dispose();
        return img;
    }

    public BufferedImage makeReferenceImage() {
        BufferedImage img = new BufferedImage(TESTW, TESTH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        renderTest(g2d);
        g2d.dispose();
        return img;
    }

    public Dimension getPreferredSize() {
        return new Dimension(TESTW, TESTH);
    }

    private static void compareImages(BufferedImage refImg, BufferedImage testImg, int tolerance) {
        int x1 = 0;
        int y1 = 0;
        int x2 = refImg.getWidth();
        int y2 = refImg.getHeight();
        for (int y = y1; y < y2; y++) {
            for (int x = x1; x < x2; x++) {
                Color expected = new Color(refImg.getRGB(x, y));
                Color actual = new Color(testImg.getRGB(x, y));
                if (!isSameColor(expected, actual, tolerance)) {
                    throw new RuntimeException("Test failed at x=" + x + " y=" + y + " (expected=" + expected + " actual=" + actual + ")");
                }
            }
        }
    }

    private static boolean isSameColor(Color c1, Color c2, int e) {
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();
        int rmin = Math.max(r2 - e, 0);
        int gmin = Math.max(g2 - e, 0);
        int bmin = Math.max(b2 - e, 0);
        int rmax = Math.min(r2 + e, 255);
        int gmax = Math.min(g2 + e, 255);
        int bmax = Math.min(b2 + e, 255);
        if (r1 >= rmin && r1 <= rmax && g1 >= gmin && g1 <= gmax && b1 >= bmin && b1 <= bmax) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        boolean show = false;
        boolean dump = false;
        boolean compare = false;
        for (String arg : args) {
            if (arg.equals("-show")) {
                show = true;
            } else if (arg.equals("-dump")) {
                dump = true;
            } else if (arg.equals("-compare")) {
                compare = true;
            } else if (arg.equals("-ignore")) {
                ignore = true;
            }
        }
        DrawBufImgOp test = new DrawBufImgOp();
        Frame frame = new Frame();
        frame.add(test);
        frame.pack();
        frame.setVisible(true);
        synchronized (test) {
            while (!done) {
                try {
                    test.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Failed: Interrupted");
                }
            }
        }
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        if (gc.getColorModel() instanceof IndexColorModel) {
            System.out.println("IndexColorModel detected: " + "test considered PASSED");
            frame.dispose();
            return;
        }
        BufferedImage capture = null;
        try {
            Robot robot = new Robot();
            Point pt1 = test.getLocationOnScreen();
            Rectangle rect = new Rectangle(pt1.x, pt1.y, TESTW, TESTH);
            capture = robot.createScreenCapture(rect);
        } catch (Exception e) {
            throw new RuntimeException("Problems creating Robot");
        } finally {
            if (!show) {
                frame.dispose();
            }
        }
        if (dump || compare) {
            BufferedImage ref = test.makeReferenceImage();
            if (dump) {
                ImageIO.write(ref, "png", new File("DrawBufImgOp.ref.png"));
                ImageIO.write(capture, "png", new File("DrawBufImgOp.cap.png"));
            }
            if (compare) {
                test.compareImages(ref, capture, 1);
            }
        }
    }
}
