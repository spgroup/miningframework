import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

public class DrawHugeImageTest {

    static {
        System.setProperty("sun.java2d.accthreshold", "1");
    }

    private static final int max_rendering_count = 5;

    private static final Color srcColor = Color.red;

    private static final Color dstColor = Color.blue;

    public static void main(String[] args) {
        BufferedImage src = createSrc();
        VolatileImage dst = createDst();
        System.out.println("Dst: " + dst);
        boolean status;
        int count = max_rendering_count;
        do {
            System.out.println("render image: " + (max_rendering_count - count));
            status = render(src, dst);
        } while (status && count-- > 0);
        if (!status || count > 0) {
            throw new RuntimeException("Test failed: " + count);
        }
    }

    private static boolean render(BufferedImage src, VolatileImage dst) {
        int cnt = 5;
        do {
            Graphics2D g = dst.createGraphics();
            g.setColor(dstColor);
            g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
            g.drawImage(src, 0, 0, null);
            g.dispose();
        } while (dst.contentsLost() && (--cnt > 0));
        if (cnt == 0) {
            System.err.println("Test failed: unable to render to volatile destination");
            return false;
        }
        BufferedImage s = dst.getSnapshot();
        return s.getRGB(1, 1) == srcColor.getRGB();
    }

    private static BufferedImage createSrc() {
        final int w = 20000;
        final int h = 5;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        g.setColor(srcColor);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    private static VolatileImage createDst() {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        return gc.createCompatibleVolatileImage(200, 200);
    }
}
