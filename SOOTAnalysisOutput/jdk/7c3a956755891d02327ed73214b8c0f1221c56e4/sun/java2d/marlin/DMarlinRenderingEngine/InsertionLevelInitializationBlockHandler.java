package sun.java2d.marlin;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.security.AccessController;
import static sun.java2d.marlin.MarlinUtils.logInfo;
import sun.java2d.ReentrantContextProvider;
import sun.java2d.ReentrantContextProviderCLQ;
import sun.java2d.ReentrantContextProviderTL;
import sun.java2d.pipe.AATileGenerator;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderingEngine;
import sun.security.action.GetPropertyAction;

public final class DMarlinRenderingEngine extends RenderingEngine implements MarlinConst {

    private static enum NormMode {

        ON_WITH_AA {

            @Override
            PathIterator getNormalizingPathIterator(final DRendererContext rdrCtx, final PathIterator src) {
                return rdrCtx.nPCPathIterator.init(src);
            }
        }
        , ON_NO_AA {

            @Override
            PathIterator getNormalizingPathIterator(final DRendererContext rdrCtx, final PathIterator src) {
                return rdrCtx.nPQPathIterator.init(src);
            }
        }
        , OFF {

            @Override
            PathIterator getNormalizingPathIterator(final DRendererContext rdrCtx, final PathIterator src) {
                return src;
            }
        }
        ;

        abstract PathIterator getNormalizingPathIterator(DRendererContext rdrCtx, PathIterator src);
    }

    private static final float MIN_PEN_SIZE = 1.0f / NORM_SUBPIXELS;

    static final double UPPER_BND = Float.MAX_VALUE / 2.0d;

    static final double LOWER_BND = -UPPER_BND;

    public DMarlinRenderingEngine() {
        super();
        logSettings(DMarlinRenderingEngine.class.getName());
    }

    @Override
    public Shape createStrokedShape(Shape src, float width, int caps, int join, float miterlimit, float[] dashes, float dashphase) {
        final DRendererContext rdrCtx = getRendererContext();
        try {
            final Path2D.Double p2d = rdrCtx.getPath2D();
            strokeTo(rdrCtx, src, null, width, NormMode.OFF, caps, join, miterlimit, dashes, dashphase, rdrCtx.transformerPC2D.wrapPath2d(p2d));
            return new Path2D.Double(p2d);
        } finally {
            returnRendererContext(rdrCtx);
        }
    }

    @Override
    public void strokeTo(Shape src, AffineTransform at, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, final sun.awt.geom.PathConsumer2D consumer) {
        final NormMode norm = (normalize) ? ((antialias) ? NormMode.ON_WITH_AA : NormMode.ON_NO_AA) : NormMode.OFF;
        final DRendererContext rdrCtx = getRendererContext();
        try {
            strokeTo(rdrCtx, src, at, bs, thin, norm, antialias, rdrCtx.p2dAdapter.init(consumer));
        } finally {
            returnRendererContext(rdrCtx);
        }
    }

    final void strokeTo(final DRendererContext rdrCtx, Shape src, AffineTransform at, BasicStroke bs, boolean thin, NormMode normalize, boolean antialias, DPathConsumer2D pc2d) {
        double lw;
        if (thin) {
            if (antialias) {
                lw = userSpaceLineWidth(at, MIN_PEN_SIZE);
            } else {
                lw = userSpaceLineWidth(at, 1.0d);
            }
        } else {
            lw = bs.getLineWidth();
        }
        strokeTo(rdrCtx, src, at, lw, normalize, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase(), pc2d);
    }

    private double userSpaceLineWidth(AffineTransform at, double lw) {
        double widthScale;
        if (at == null) {
            widthScale = 1.0d;
        } else if ((at.getType() & (AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_GENERAL_SCALE)) != 0) {
            widthScale = Math.sqrt(at.getDeterminant());
        } else {
            double A = at.getScaleX();
            double C = at.getShearX();
            double B = at.getShearY();
            double D = at.getScaleY();
            double EA = A * A + B * B;
            double EB = 2.0d * (A * C + B * D);
            double EC = C * C + D * D;
            double hypot = Math.sqrt(EB * EB + (EA - EC) * (EA - EC));
            double widthsquared = ((EA + EC + hypot) / 2.0d);
            widthScale = Math.sqrt(widthsquared);
        }
        return (lw / widthScale);
    }

    final void strokeTo(final DRendererContext rdrCtx, Shape src, AffineTransform at, double width, NormMode norm, int caps, int join, float miterlimit, float[] dashes, float dashphase, DPathConsumer2D pc2d) {
        AffineTransform strokerat = null;
        int dashLen = -1;
        boolean recycleDashes = false;
        double[] dashesD = null;
        if (dashes != null) {
            recycleDashes = true;
            dashLen = dashes.length;
            dashesD = rdrCtx.dasher.copyDashArray(dashes);
        }
        if (at != null && !at.isIdentity()) {
            final double a = at.getScaleX();
            final double b = at.getShearX();
            final double c = at.getShearY();
            final double d = at.getScaleY();
            final double det = a * d - c * b;
            if (Math.abs(det) <= (2.0d * Double.MIN_VALUE)) {
                pc2d.moveTo(0.0d, 0.0d);
                pc2d.pathDone();
                return;
            }
            if (nearZero(a * b + c * d) && nearZero(a * a + c * c - (b * b + d * d))) {
                final double scale = Math.sqrt(a * a + c * c);
                if (dashesD != null) {
                    for (int i = 0; i < dashLen; i++) {
                        dashesD[i] *= scale;
                    }
                    dashphase *= scale;
                }
                width *= scale;
            } else {
                strokerat = at;
            }
        } else {
            at = null;
        }
        if (USE_SIMPLIFIER) {
            pc2d = rdrCtx.simplifier.init(pc2d);
        }
        final DTransformingPathConsumer2D transformerPC2D = rdrCtx.transformerPC2D;
        pc2d = transformerPC2D.deltaTransformConsumer(pc2d, strokerat);
        pc2d = rdrCtx.stroker.init(pc2d, width, caps, join, miterlimit);
        if (dashesD != null) {
            pc2d = rdrCtx.dasher.init(pc2d, dashesD, dashLen, dashphase, recycleDashes);
        }
        pc2d = transformerPC2D.inverseDeltaTransformConsumer(pc2d, strokerat);
        final PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, src.getPathIterator(at));
        pathTo(rdrCtx, pi, pc2d);
    }

    private static boolean nearZero(final double num) {
        return Math.abs(num) < 2.0d * Math.ulp(num);
    }

    abstract static class NormalizingPathIterator implements PathIterator {

        private PathIterator src;

        private double curx_adjust, cury_adjust;

        private double movx_adjust, movy_adjust;

        private final double[] tmp;

        NormalizingPathIterator(final double[] tmp) {
            this.tmp = tmp;
        }

        final NormalizingPathIterator init(final PathIterator src) {
            this.src = src;
            return this;
        }

        final void dispose() {
            this.src = null;
        }

        @Override
        public final int currentSegment(final double[] coords) {
            int lastCoord;
            final int type = src.currentSegment(coords);
            switch(type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    lastCoord = 0;
                    break;
                case PathIterator.SEG_QUADTO:
                    lastCoord = 2;
                    break;
                case PathIterator.SEG_CUBICTO:
                    lastCoord = 4;
                    break;
                case PathIterator.SEG_CLOSE:
                    curx_adjust = movx_adjust;
                    cury_adjust = movy_adjust;
                    return type;
                default:
                    throw new InternalError("Unrecognized curve type");
            }
            double coord, x_adjust, y_adjust;
            coord = coords[lastCoord];
            x_adjust = normCoord(coord);
            coords[lastCoord] = x_adjust;
            x_adjust -= coord;
            coord = coords[lastCoord + 1];
            y_adjust = normCoord(coord);
            coords[lastCoord + 1] = y_adjust;
            y_adjust -= coord;
            switch(type) {
                case PathIterator.SEG_MOVETO:
                    movx_adjust = x_adjust;
                    movy_adjust = y_adjust;
                    break;
                case PathIterator.SEG_LINETO:
                    break;
                case PathIterator.SEG_QUADTO:
                    coords[0] += (curx_adjust + x_adjust) / 2.0d;
                    coords[1] += (cury_adjust + y_adjust) / 2.0d;
                    break;
                case PathIterator.SEG_CUBICTO:
                    coords[0] += curx_adjust;
                    coords[1] += cury_adjust;
                    coords[2] += x_adjust;
                    coords[3] += y_adjust;
                    break;
                case PathIterator.SEG_CLOSE:
                default:
            }
            curx_adjust = x_adjust;
            cury_adjust = y_adjust;
            return type;
        }

        abstract double normCoord(final double coord);

        @Override
        public final int currentSegment(final float[] coords) {
            final double[] _tmp = tmp;
            int type = this.currentSegment(_tmp);
            for (int i = 0; i < 6; i++) {
                coords[i] = (float) _tmp[i];
            }
            return type;
        }

        @Override
        public final int getWindingRule() {
            return src.getWindingRule();
        }

        @Override
        public final boolean isDone() {
            if (src.isDone()) {
                dispose();
                return true;
            }
            return false;
        }

        @Override
        public final void next() {
            src.next();
        }

        static final class NearestPixelCenter extends NormalizingPathIterator {

            NearestPixelCenter(final double[] tmp) {
                super(tmp);
            }

            @Override
            double normCoord(final double coord) {
                return Math.floor(coord) + 0.5d;
            }
        }

        static final class NearestPixelQuarter extends NormalizingPathIterator {

            NearestPixelQuarter(final double[] tmp) {
                super(tmp);
            }

            @Override
            double normCoord(final double coord) {
                return Math.floor(coord + 0.25d) + 0.25d;
            }
        }
    }

    private static void pathTo(final DRendererContext rdrCtx, final PathIterator pi, final DPathConsumer2D pc2d) {
        rdrCtx.dirty = true;
        final double[] coords = rdrCtx.double6;
        pathToLoop(coords, pi, pc2d);
        rdrCtx.dirty = false;
    }

    private static void pathToLoop(final double[] coords, final PathIterator pi, final DPathConsumer2D pc2d) {
        boolean subpathStarted = false;
        for (; !pi.isDone(); pi.next()) {
            switch(pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                        pc2d.moveTo(coords[0], coords[1]);
                        subpathStarted = true;
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                        if (subpathStarted) {
                            pc2d.lineTo(coords[0], coords[1]);
                        } else {
                            pc2d.moveTo(coords[0], coords[1]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    if (coords[2] < UPPER_BND && coords[2] > LOWER_BND && coords[3] < UPPER_BND && coords[3] > LOWER_BND) {
                        if (subpathStarted) {
                            if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                                pc2d.quadTo(coords[0], coords[1], coords[2], coords[3]);
                            } else {
                                pc2d.lineTo(coords[2], coords[3]);
                            }
                        } else {
                            pc2d.moveTo(coords[2], coords[3]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (coords[4] < UPPER_BND && coords[4] > LOWER_BND && coords[5] < UPPER_BND && coords[5] > LOWER_BND) {
                        if (subpathStarted) {
                            if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND && coords[2] < UPPER_BND && coords[2] > LOWER_BND && coords[3] < UPPER_BND && coords[3] > LOWER_BND) {
                                pc2d.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                            } else {
                                pc2d.lineTo(coords[4], coords[5]);
                            }
                        } else {
                            pc2d.moveTo(coords[4], coords[5]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    if (subpathStarted) {
                        pc2d.closePath();
                    }
                    break;
                default:
            }
        }
        pc2d.pathDone();
    }

    @Override
    public AATileGenerator getAATileGenerator(Shape s, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, int[] bbox) {
        MarlinTileGenerator ptg = null;
        DRenderer r = null;
        final DRendererContext rdrCtx = getRendererContext();
        try {
            final AffineTransform _at = (at != null && !at.isIdentity()) ? at : null;
            final NormMode norm = (normalize) ? NormMode.ON_WITH_AA : NormMode.OFF;
            if (bs == null) {
                final PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, s.getPathIterator(_at));
                r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), pi.getWindingRule());
                pathTo(rdrCtx, pi, r);
            } else {
                r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), PathIterator.WIND_NON_ZERO);
                strokeTo(rdrCtx, s, _at, bs, thin, norm, true, r);
            }
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        } finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    @Override
    public final AATileGenerator getAATileGenerator(double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2, Region clip, int[] bbox) {
        double ldx1, ldy1, ldx2, ldy2;
        boolean innerpgram = (lw1 > 0.0d && lw2 > 0.0d);
        if (innerpgram) {
            ldx1 = dx1 * lw1;
            ldy1 = dy1 * lw1;
            ldx2 = dx2 * lw2;
            ldy2 = dy2 * lw2;
            x -= (ldx1 + ldx2) / 2.0d;
            y -= (ldy1 + ldy2) / 2.0d;
            dx1 += ldx1;
            dy1 += ldy1;
            dx2 += ldx2;
            dy2 += ldy2;
            if (lw1 > 1.0d && lw2 > 1.0d) {
                innerpgram = false;
            }
        } else {
            ldx1 = ldy1 = ldx2 = ldy2 = 0.0d;
        }
        MarlinTileGenerator ptg = null;
        DRenderer r = null;
        final DRendererContext rdrCtx = getRendererContext();
        try {
            r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), DRenderer.WIND_EVEN_ODD);
            r.moveTo(x, y);
            r.lineTo((x + dx1), (y + dy1));
            r.lineTo((x + dx1 + dx2), (y + dy1 + dy2));
            r.lineTo((x + dx2), (y + dy2));
            r.closePath();
            if (innerpgram) {
                x += ldx1 + ldx2;
                y += ldy1 + ldy2;
                dx1 -= 2.0d * ldx1;
                dy1 -= 2.0d * ldy1;
                dx2 -= 2.0d * ldx2;
                dy2 -= 2.0d * ldy2;
                r.moveTo(x, y);
                r.lineTo((x + dx1), (y + dy1));
                r.lineTo((x + dx1 + dx2), (y + dy1 + dy2));
                r.lineTo((x + dx2), (y + dy2));
                r.closePath();
            }
            r.pathDone();
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        } finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    @Override
    public float getMinimumAAPenSize() {
        return MIN_PEN_SIZE;
    }

    static {
        if (PathIterator.WIND_NON_ZERO != DRenderer.WIND_NON_ZERO || PathIterator.WIND_EVEN_ODD != DRenderer.WIND_EVEN_ODD || BasicStroke.JOIN_MITER != DStroker.JOIN_MITER || BasicStroke.JOIN_ROUND != DStroker.JOIN_ROUND || BasicStroke.JOIN_BEVEL != DStroker.JOIN_BEVEL || BasicStroke.CAP_BUTT != DStroker.CAP_BUTT || BasicStroke.CAP_ROUND != DStroker.CAP_ROUND || BasicStroke.CAP_SQUARE != DStroker.CAP_SQUARE) {
            throw new InternalError("mismatched renderer constants");
        }
    }

    private static final boolean USE_THREAD_LOCAL;

    static final int REF_TYPE;

    private static final ReentrantContextProvider<DRendererContext> RDR_CTX_PROVIDER;

    static {
        USE_THREAD_LOCAL = MarlinProperties.isUseThreadLocal();
        final String refType = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.renderer.useRef", "soft"));
        if ("hard".equalsIgnoreCase(refType)) {
            REF_TYPE = ReentrantContextProvider.REF_HARD;
        } else if ("weak".equalsIgnoreCase(refType)) {
            REF_TYPE = ReentrantContextProvider.REF_WEAK;
        } else {
            REF_TYPE = ReentrantContextProvider.REF_SOFT;
        }
        if (USE_THREAD_LOCAL) {
            RDR_CTX_PROVIDER = new ReentrantContextProviderTL<DRendererContext>(REF_TYPE) {

                @Override
                protected DRendererContext newContext() {
                    return DRendererContext.createContext();
                }
            };
        } else {
            RDR_CTX_PROVIDER = new ReentrantContextProviderCLQ<DRendererContext>(REF_TYPE) {

                @Override
                protected DRendererContext newContext() {
                    return DRendererContext.createContext();
                }
            };
        }
    }

    private static boolean SETTINGS_LOGGED = !ENABLE_LOGS;

    private static void logSettings(final String reClass) {
        if (SETTINGS_LOGGED) {
            return;
        }
        SETTINGS_LOGGED = true;
        String refType;
        switch(REF_TYPE) {
            default:
            case ReentrantContextProvider.REF_HARD:
                refType = "hard";
                break;
            case ReentrantContextProvider.REF_SOFT:
                refType = "soft";
                break;
            case ReentrantContextProvider.REF_WEAK:
                refType = "weak";
                break;
        }
        logInfo("==========================================================" + "=====================");
        logInfo("Marlin software rasterizer           = ENABLED");
        logInfo("Version                              = [" + Version.getVersion() + "]");
        logInfo("sun.java2d.renderer                  = " + reClass);
        logInfo("sun.java2d.renderer.useThreadLocal   = " + USE_THREAD_LOCAL);
        logInfo("sun.java2d.renderer.useRef           = " + refType);
        logInfo("sun.java2d.renderer.edges            = " + MarlinConst.INITIAL_EDGES_COUNT);
        logInfo("sun.java2d.renderer.pixelsize        = " + MarlinConst.INITIAL_PIXEL_DIM);
        logInfo("sun.java2d.renderer.subPixel_log2_X  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_X);
        logInfo("sun.java2d.renderer.subPixel_log2_Y  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_Y);
        logInfo("sun.java2d.renderer.tileSize_log2    = " + MarlinConst.TILE_H_LG);
        logInfo("sun.java2d.renderer.tileWidth_log2   = " + MarlinConst.TILE_W_LG);
        logInfo("sun.java2d.renderer.blockSize_log2   = " + MarlinConst.BLOCK_SIZE_LG);
        logInfo("sun.java2d.renderer.forceRLE         = " + MarlinProperties.isForceRLE());
        logInfo("sun.java2d.renderer.forceNoRLE       = " + MarlinProperties.isForceNoRLE());
        logInfo("sun.java2d.renderer.useTileFlags     = " + MarlinProperties.isUseTileFlags());
        logInfo("sun.java2d.renderer.useTileFlags.useHeuristics = " + MarlinProperties.isUseTileFlagsWithHeuristics());
        logInfo("sun.java2d.renderer.rleMinWidth      = " + MarlinCache.RLE_MIN_WIDTH);
        logInfo("sun.java2d.renderer.useSimplifier    = " + MarlinConst.USE_SIMPLIFIER);
        logInfo("sun.java2d.renderer.doStats          = " + MarlinConst.DO_STATS);
        logInfo("sun.java2d.renderer.doMonitors       = " + MarlinConst.DO_MONITORS);
        logInfo("sun.java2d.renderer.doChecks         = " + MarlinConst.DO_CHECKS);
        logInfo("sun.java2d.renderer.useLogger        = " + MarlinConst.USE_LOGGER);
        logInfo("sun.java2d.renderer.logCreateContext = " + MarlinConst.LOG_CREATE_CONTEXT);
        logInfo("sun.java2d.renderer.logUnsafeMalloc  = " + MarlinConst.LOG_UNSAFE_MALLOC);
        logInfo("sun.java2d.renderer.cubic_dec_d2     = " + MarlinProperties.getCubicDecD2());
        logInfo("sun.java2d.renderer.cubic_inc_d1     = " + MarlinProperties.getCubicIncD1());
        logInfo("sun.java2d.renderer.quad_dec_d2      = " + MarlinProperties.getQuadDecD2());
        logInfo("Renderer settings:");
        logInfo("CUB_DEC_BND  = " + DRenderer.CUB_DEC_BND);
        logInfo("CUB_INC_BND  = " + DRenderer.CUB_INC_BND);
        logInfo("QUAD_DEC_BND = " + DRenderer.QUAD_DEC_BND);
        logInfo("INITIAL_EDGES_CAPACITY               = " + MarlinConst.INITIAL_EDGES_CAPACITY);
        logInfo("INITIAL_CROSSING_COUNT               = " + DRenderer.INITIAL_CROSSING_COUNT);
        logInfo("==========================================================" + "=====================");
    }

    @SuppressWarnings({ "unchecked" })
    static DRendererContext getRendererContext() {
        final DRendererContext rdrCtx = RDR_CTX_PROVIDER.acquire();
        if (DO_MONITORS) {
            rdrCtx.stats.mon_pre_getAATileGenerator.start();
        }
        return rdrCtx;
    }

    static void returnRendererContext(final DRendererContext rdrCtx) {
        rdrCtx.dispose();
        if (DO_MONITORS) {
            rdrCtx.stats.mon_pre_getAATileGenerator.stop();
        }
        RDR_CTX_PROVIDER.release(rdrCtx);
    }
}