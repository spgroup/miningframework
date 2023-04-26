package com.sun.java.util.jar.pack;

import com.sun.java.util.jar.pack.ConstantPool.Entry;
import com.sun.java.util.jar.pack.ConstantPool.Index;
import com.sun.java.util.jar.pack.Package.Class.Field;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Pack200;

abstract class BandStructure implements Constants {

    static final int MAX_EFFORT = 9;

    static final int MIN_EFFORT = 1;

    static final int DEFAULT_EFFORT = 5;

    PropMap p200 = Utils.currentPropMap();

    int verbose = p200.getInteger(Utils.DEBUG_VERBOSE);

    int effort = p200.getInteger(Pack200.Packer.EFFORT);

    {
        if (effort == 0)
            effort = DEFAULT_EFFORT;
    }

    boolean optDumpBands = p200.getBoolean(Utils.COM_PREFIX + "dump.bands");

    boolean optDebugBands = p200.getBoolean(Utils.COM_PREFIX + "debug.bands");

    boolean optVaryCodings = !p200.getBoolean(Utils.COM_PREFIX + "no.vary.codings");

    boolean optBigStrings = !p200.getBoolean(Utils.COM_PREFIX + "no.big.strings");

    abstract protected Index getCPIndex(byte tag);

    private int packageMajver = -1;

    public void initPackageMajver(int packageMajver) throws IOException {
        assert (packageMajver > 0 && packageMajver < 0x10000);
        if (this.packageMajver > 0) {
            throw new IOException("Package majver is already initialized to " + this.packageMajver + "; new setting is " + packageMajver);
        }
        this.packageMajver = packageMajver;
        adjustToMajver();
    }

    public int getPackageMajver() {
        if (packageMajver < 0) {
            throw new RuntimeException("Package majver not yet initialized");
        }
        return packageMajver;
    }

    private final boolean isReader = this instanceof PackageReader;

    protected BandStructure() {
    }

    final static Coding BYTE1 = Coding.of(1, 256);

    final static Coding CHAR3 = Coding.of(3, 128);

    final static Coding BCI5 = Coding.of(5, 4);

    final static Coding BRANCH5 = Coding.of(5, 4, 2);

    final static Coding UNSIGNED5 = Coding.of(5, 64);

    final static Coding UDELTA5 = UNSIGNED5.getDeltaCoding();

    final static Coding SIGNED5 = Coding.of(5, 64, 1);

    final static Coding DELTA5 = SIGNED5.getDeltaCoding();

    final static Coding MDELTA5 = Coding.of(5, 64, 2).getDeltaCoding();

    final private static Coding[] basicCodings = { null, Coding.of(1, 256, 0), Coding.of(1, 256, 1), Coding.of(1, 256, 0).getDeltaCoding(), Coding.of(1, 256, 1).getDeltaCoding(), Coding.of(2, 256, 0), Coding.of(2, 256, 1), Coding.of(2, 256, 0).getDeltaCoding(), Coding.of(2, 256, 1).getDeltaCoding(), Coding.of(3, 256, 0), Coding.of(3, 256, 1), Coding.of(3, 256, 0).getDeltaCoding(), Coding.of(3, 256, 1).getDeltaCoding(), Coding.of(4, 256, 0), Coding.of(4, 256, 1), Coding.of(4, 256, 0).getDeltaCoding(), Coding.of(4, 256, 1).getDeltaCoding(), Coding.of(5, 4, 0), Coding.of(5, 4, 1), Coding.of(5, 4, 2), Coding.of(5, 16, 0), Coding.of(5, 16, 1), Coding.of(5, 16, 2), Coding.of(5, 32, 0), Coding.of(5, 32, 1), Coding.of(5, 32, 2), Coding.of(5, 64, 0), Coding.of(5, 64, 1), Coding.of(5, 64, 2), Coding.of(5, 128, 0), Coding.of(5, 128, 1), Coding.of(5, 128, 2), Coding.of(5, 4, 0).getDeltaCoding(), Coding.of(5, 4, 1).getDeltaCoding(), Coding.of(5, 4, 2).getDeltaCoding(), Coding.of(5, 16, 0).getDeltaCoding(), Coding.of(5, 16, 1).getDeltaCoding(), Coding.of(5, 16, 2).getDeltaCoding(), Coding.of(5, 32, 0).getDeltaCoding(), Coding.of(5, 32, 1).getDeltaCoding(), Coding.of(5, 32, 2).getDeltaCoding(), Coding.of(5, 64, 0).getDeltaCoding(), Coding.of(5, 64, 1).getDeltaCoding(), Coding.of(5, 64, 2).getDeltaCoding(), Coding.of(5, 128, 0).getDeltaCoding(), Coding.of(5, 128, 1).getDeltaCoding(), Coding.of(5, 128, 2).getDeltaCoding(), Coding.of(2, 192, 0), Coding.of(2, 224, 0), Coding.of(2, 240, 0), Coding.of(2, 248, 0), Coding.of(2, 252, 0), Coding.of(2, 8, 0).getDeltaCoding(), Coding.of(2, 8, 1).getDeltaCoding(), Coding.of(2, 16, 0).getDeltaCoding(), Coding.of(2, 16, 1).getDeltaCoding(), Coding.of(2, 32, 0).getDeltaCoding(), Coding.of(2, 32, 1).getDeltaCoding(), Coding.of(2, 64, 0).getDeltaCoding(), Coding.of(2, 64, 1).getDeltaCoding(), Coding.of(2, 128, 0).getDeltaCoding(), Coding.of(2, 128, 1).getDeltaCoding(), Coding.of(2, 192, 0).getDeltaCoding(), Coding.of(2, 192, 1).getDeltaCoding(), Coding.of(2, 224, 0).getDeltaCoding(), Coding.of(2, 224, 1).getDeltaCoding(), Coding.of(2, 240, 0).getDeltaCoding(), Coding.of(2, 240, 1).getDeltaCoding(), Coding.of(2, 248, 0).getDeltaCoding(), Coding.of(2, 248, 1).getDeltaCoding(), Coding.of(3, 192, 0), Coding.of(3, 224, 0), Coding.of(3, 240, 0), Coding.of(3, 248, 0), Coding.of(3, 252, 0), Coding.of(3, 8, 0).getDeltaCoding(), Coding.of(3, 8, 1).getDeltaCoding(), Coding.of(3, 16, 0).getDeltaCoding(), Coding.of(3, 16, 1).getDeltaCoding(), Coding.of(3, 32, 0).getDeltaCoding(), Coding.of(3, 32, 1).getDeltaCoding(), Coding.of(3, 64, 0).getDeltaCoding(), Coding.of(3, 64, 1).getDeltaCoding(), Coding.of(3, 128, 0).getDeltaCoding(), Coding.of(3, 128, 1).getDeltaCoding(), Coding.of(3, 192, 0).getDeltaCoding(), Coding.of(3, 192, 1).getDeltaCoding(), Coding.of(3, 224, 0).getDeltaCoding(), Coding.of(3, 224, 1).getDeltaCoding(), Coding.of(3, 240, 0).getDeltaCoding(), Coding.of(3, 240, 1).getDeltaCoding(), Coding.of(3, 248, 0).getDeltaCoding(), Coding.of(3, 248, 1).getDeltaCoding(), Coding.of(4, 192, 0), Coding.of(4, 224, 0), Coding.of(4, 240, 0), Coding.of(4, 248, 0), Coding.of(4, 252, 0), Coding.of(4, 8, 0).getDeltaCoding(), Coding.of(4, 8, 1).getDeltaCoding(), Coding.of(4, 16, 0).getDeltaCoding(), Coding.of(4, 16, 1).getDeltaCoding(), Coding.of(4, 32, 0).getDeltaCoding(), Coding.of(4, 32, 1).getDeltaCoding(), Coding.of(4, 64, 0).getDeltaCoding(), Coding.of(4, 64, 1).getDeltaCoding(), Coding.of(4, 128, 0).getDeltaCoding(), Coding.of(4, 128, 1).getDeltaCoding(), Coding.of(4, 192, 0).getDeltaCoding(), Coding.of(4, 192, 1).getDeltaCoding(), Coding.of(4, 224, 0).getDeltaCoding(), Coding.of(4, 224, 1).getDeltaCoding(), Coding.of(4, 240, 0).getDeltaCoding(), Coding.of(4, 240, 1).getDeltaCoding(), Coding.of(4, 248, 0).getDeltaCoding(), Coding.of(4, 248, 1).getDeltaCoding(), null };

    final private static HashMap basicCodingIndexes;

    static {
        assert (basicCodings[_meta_default] == null);
        assert (basicCodings[_meta_canon_min] != null);
        assert (basicCodings[_meta_canon_max] != null);
        HashMap map = new HashMap();
        for (int i = 0; i < basicCodings.length; i++) {
            Coding c = basicCodings[i];
            if (c == null)
                continue;
            assert (i >= _meta_canon_min);
            assert (i <= _meta_canon_max);
            map.put(c, new Integer(i));
        }
        basicCodingIndexes = map;
    }

    public static Coding codingForIndex(int i) {
        return i < basicCodings.length ? basicCodings[i] : null;
    }

    public static int indexOf(Coding c) {
        Integer i = (Integer) basicCodingIndexes.get(c);
        if (i == null)
            return 0;
        return i.intValue();
    }

    public static Coding[] getBasicCodings() {
        return (Coding[]) basicCodings.clone();
    }

    protected byte[] bandHeaderBytes;

    protected int bandHeaderBytePos;

    protected int bandHeaderBytePos0;

    protected CodingMethod getBandHeader(int XB, Coding regularCoding) {
        CodingMethod[] res = { null };
        bandHeaderBytes[--bandHeaderBytePos] = (byte) XB;
        bandHeaderBytePos0 = bandHeaderBytePos;
        bandHeaderBytePos = parseMetaCoding(bandHeaderBytes, bandHeaderBytePos, regularCoding, res);
        return res[0];
    }

    public static int parseMetaCoding(byte[] bytes, int pos, Coding dflt, CodingMethod[] res) {
        if ((bytes[pos] & 0xFF) == _meta_default) {
            res[0] = dflt;
            return pos + 1;
        }
        int pos2;
        pos2 = Coding.parseMetaCoding(bytes, pos, dflt, res);
        if (pos2 > pos)
            return pos2;
        pos2 = PopulationCoding.parseMetaCoding(bytes, pos, dflt, res);
        if (pos2 > pos)
            return pos2;
        pos2 = AdaptiveCoding.parseMetaCoding(bytes, pos, dflt, res);
        if (pos2 > pos)
            return pos2;
        throw new RuntimeException("Bad meta-coding op " + (bytes[pos] & 0xFF));
    }

    static final int SHORT_BAND_HEURISTIC = 100;

    public static final int NO_PHASE = 0;

    public static final int COLLECT_PHASE = 1;

    public static final int FROZEN_PHASE = 3;

    public static final int WRITE_PHASE = 5;

    public static final int EXPECT_PHASE = 2;

    public static final int READ_PHASE = 4;

    public static final int DISBURSE_PHASE = 6;

    public static final int DONE_PHASE = 8;

    static boolean phaseIsRead(int p) {
        return (p % 2) == 0;
    }

    static int phaseCmp(int p0, int p1) {
        assert ((p0 % 2) == (p1 % 2) || (p0 % 8) == 0 || (p1 % 8) == 0);
        return p0 - p1;
    }

    abstract class Band {

        private int phase = NO_PHASE;

        private final String name;

        private int valuesExpected;

        protected long outputSize = -1;

        final public Coding regularCoding;

        final public int seqForDebug;

        public int elementCountForDebug;

        protected Band(String name, Coding regularCoding) {
            this.name = name;
            this.regularCoding = regularCoding;
            this.seqForDebug = ++nextSeqForDebug;
            if (verbose > 2)
                Utils.log.fine("Band " + seqForDebug + " is " + name);
        }

        public Band init() {
            if (isReader)
                readyToExpect();
            else
                readyToCollect();
            return this;
        }

        boolean isReader() {
            return isReader;
        }

        int phase() {
            return phase;
        }

        String name() {
            return name;
        }

        public abstract int capacity();

        protected abstract void setCapacity(int cap);

        public abstract int length();

        protected abstract int valuesRemainingForDebug();

        public final int valuesExpected() {
            return valuesExpected;
        }

        public final void writeTo(OutputStream out) throws IOException {
            assert (assertReadyToWriteTo(this, out));
            setPhase(WRITE_PHASE);
            writeDataTo(out);
            doneWriting();
        }

        abstract void chooseBandCodings() throws IOException;

        public final long outputSize() {
            if (outputSize >= 0) {
                long size = outputSize;
                assert (size == computeOutputSize());
                return size;
            }
            return computeOutputSize();
        }

        protected abstract long computeOutputSize();

        abstract protected void writeDataTo(OutputStream out) throws IOException;

        void expectLength(int l) {
            assert (assertPhase(this, EXPECT_PHASE));
            assert (valuesExpected == 0);
            assert (l >= 0);
            valuesExpected = l;
        }

        void expectMoreLength(int l) {
            assert (assertPhase(this, EXPECT_PHASE));
            valuesExpected += l;
        }

        private void readyToCollect() {
            setCapacity(1);
            setPhase(COLLECT_PHASE);
        }

        protected void doneWriting() {
            assert (assertPhase(this, WRITE_PHASE));
            setPhase(DONE_PHASE);
        }

        private void readyToExpect() {
            setPhase(EXPECT_PHASE);
        }

        public final void readFrom(InputStream in) throws IOException {
            assert (assertReadyToReadFrom(this, in));
            setCapacity(valuesExpected());
            setPhase(READ_PHASE);
            readDataFrom(in);
            readyToDisburse();
        }

        abstract protected void readDataFrom(InputStream in) throws IOException;

        protected void readyToDisburse() {
            if (verbose > 1)
                Utils.log.fine("readyToDisburse " + this);
            setPhase(DISBURSE_PHASE);
        }

        public void doneDisbursing() {
            assert (assertPhase(this, DISBURSE_PHASE));
            setPhase(DONE_PHASE);
        }

        public final void doneWithUnusedBand() {
            if (isReader) {
                assert (assertPhase(this, EXPECT_PHASE));
                assert (valuesExpected() == 0);
                setPhase(READ_PHASE);
                setPhase(DISBURSE_PHASE);
                setPhase(DONE_PHASE);
            } else {
                setPhase(FROZEN_PHASE);
            }
        }

        protected void setPhase(int newPhase) {
            assert (assertPhaseChangeOK(this, phase, newPhase));
            this.phase = newPhase;
        }

        protected int lengthForDebug = -1;

        public String toString() {
            int length = (lengthForDebug != -1 ? lengthForDebug : length());
            String str = name;
            if (length != 0)
                str += "[" + length + "]";
            if (elementCountForDebug != 0)
                str += "(" + elementCountForDebug + ")";
            return str;
        }
    }

    class ValueBand extends Band {

        private int[] values;

        private int length;

        private int valuesDisbursed;

        private CodingMethod bandCoding;

        private byte[] metaCoding;

        protected ValueBand(String name, Coding regularCoding) {
            super(name, regularCoding);
        }

        public int capacity() {
            return values == null ? -1 : values.length;
        }

        protected void setCapacity(int cap) {
            assert (length <= cap);
            if (cap == -1) {
                values = null;
                return;
            }
            values = realloc(values, cap);
        }

        public int length() {
            return length;
        }

        protected int valuesRemainingForDebug() {
            return length - valuesDisbursed;
        }

        protected int valueAtForDebug(int i) {
            return values[i];
        }

        void patchValue(int i, int value) {
            assert (this == archive_header_S);
            assert (i == AH_ARCHIVE_SIZE_HI || i == AH_ARCHIVE_SIZE_LO);
            assert (i < length);
            values[i] = value;
            outputSize = -1;
        }

        protected void initializeValues(int[] values) {
            assert (assertCanChangeLength(this));
            assert (length == 0);
            this.values = values;
            this.length = values.length;
        }

        protected void addValue(int x) {
            assert (assertCanChangeLength(this));
            if (length == values.length)
                setCapacity(length < 1000 ? length * 10 : length * 2);
            values[length++] = x;
        }

        private boolean canVaryCoding() {
            if (!optVaryCodings)
                return false;
            if (length == 0)
                return false;
            if (this == archive_header_0)
                return false;
            if (this == archive_header_S)
                return false;
            if (this == archive_header_1)
                return false;
            return (regularCoding.min() <= -256 || regularCoding.max() >= 256);
        }

        private boolean shouldVaryCoding() {
            assert (canVaryCoding());
            if (effort < MAX_EFFORT && length < SHORT_BAND_HEURISTIC)
                return false;
            return true;
        }

        protected void chooseBandCodings() throws IOException {
            boolean canVary = canVaryCoding();
            if (!canVary || !shouldVaryCoding()) {
                if (regularCoding.canRepresent(values, 0, length)) {
                    bandCoding = regularCoding;
                } else {
                    assert (canVary);
                    if (verbose > 1)
                        Utils.log.fine("regular coding fails in band " + name());
                    bandCoding = UNSIGNED5;
                }
                outputSize = -1;
            } else {
                int[] sizes = { 0, 0 };
                bandCoding = chooseCoding(values, 0, length, regularCoding, name(), sizes);
                outputSize = sizes[CodingChooser.BYTE_SIZE];
                if (outputSize == 0)
                    outputSize = -1;
            }
            if (bandCoding != regularCoding) {
                metaCoding = bandCoding.getMetaCoding(regularCoding);
                if (verbose > 1) {
                    Utils.log.fine("alternate coding " + this + " " + bandCoding);
                }
            } else if (canVary && decodeEscapeValue(values[0], regularCoding) >= 0) {
                metaCoding = defaultMetaCoding;
            } else {
                metaCoding = noMetaCoding;
            }
            if (metaCoding.length > 0 && (verbose > 2 || verbose > 1 && metaCoding.length > 1)) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < metaCoding.length; i++) {
                    if (i == 1)
                        sb.append(" /");
                    sb.append(" ").append(metaCoding[i] & 0xFF);
                }
                Utils.log.fine("   meta-coding " + sb);
            }
            assert ((outputSize < 0) || !(bandCoding instanceof Coding) || (outputSize == ((Coding) bandCoding).getLength(values, 0, length))) : (bandCoding + " : " + outputSize + " != " + ((Coding) bandCoding).getLength(values, 0, length) + " ?= " + getCodingChooser().computeByteSize(bandCoding, values, 0, length));
            if (metaCoding.length > 0) {
                if (outputSize >= 0)
                    outputSize += computeEscapeSize();
                for (int i = 1; i < metaCoding.length; i++) {
                    band_headers.putByte(metaCoding[i] & 0xFF);
                }
            }
        }

        protected long computeOutputSize() {
            outputSize = getCodingChooser().computeByteSize(bandCoding, values, 0, length);
            assert (outputSize < Integer.MAX_VALUE);
            outputSize += computeEscapeSize();
            return outputSize;
        }

        protected int computeEscapeSize() {
            if (metaCoding.length == 0)
                return 0;
            int XB = metaCoding[0] & 0xFF;
            int X = encodeEscapeValue(XB, regularCoding);
            return regularCoding.setD(0).getLength(X);
        }

        protected void writeDataTo(OutputStream out) throws IOException {
            if (length == 0)
                return;
            long len0 = 0;
            if (out == outputCounter) {
                len0 = outputCounter.getCount();
            }
            if (metaCoding.length > 0) {
                int XB = metaCoding[0] & 0xFF;
                int X = encodeEscapeValue(XB, regularCoding);
                regularCoding.setD(0).writeTo(out, X);
            }
            bandCoding.writeArrayTo(out, values, 0, length);
            if (out == outputCounter) {
                long len1 = outputCounter.getCount();
                assert (outputSize == outputCounter.getCount() - len0) : (outputSize + " != " + outputCounter.getCount() + "-" + len0);
            }
            if (optDumpBands)
                dumpBand();
        }

        protected void readDataFrom(InputStream in) throws IOException {
            length = valuesExpected();
            if (length == 0)
                return;
            if (verbose > 1)
                Utils.log.fine("Reading band " + this);
            if (!canVaryCoding()) {
                bandCoding = regularCoding;
                metaCoding = noMetaCoding;
            } else {
                assert (in.markSupported());
                in.mark(Coding.B_MAX);
                int X = regularCoding.setD(0).readFrom(in);
                int XB = decodeEscapeValue(X, regularCoding);
                if (XB < 0) {
                    in.reset();
                    XB = _meta_default;
                    bandCoding = regularCoding;
                    metaCoding = noMetaCoding;
                } else if (XB == _meta_default) {
                    bandCoding = regularCoding;
                    metaCoding = defaultMetaCoding;
                } else {
                    if (verbose > 2)
                        Utils.log.fine("found X=" + X + " => XB=" + XB);
                    bandCoding = getBandHeader(XB, regularCoding);
                    int p0 = bandHeaderBytePos0;
                    int p1 = bandHeaderBytePos;
                    metaCoding = new byte[p1 - p0];
                    System.arraycopy(bandHeaderBytes, p0, metaCoding, 0, metaCoding.length);
                }
            }
            if (bandCoding != regularCoding) {
                if (verbose > 1)
                    Utils.log.fine(name() + ": irregular coding " + bandCoding);
            }
            bandCoding.readArrayFrom(in, values, 0, length);
            if (optDumpBands)
                dumpBand();
        }

        public void doneDisbursing() {
            super.doneDisbursing();
            values = null;
        }

        private void dumpBand() throws IOException {
            assert (optDumpBands);
            PrintStream ps = new PrintStream(getDumpStream(this, ".txt"));
            String irr = (bandCoding == regularCoding) ? "" : " irregular";
            ps.print("# length=" + length + " size=" + outputSize() + irr + " coding=" + bandCoding);
            if (metaCoding != noMetaCoding) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < metaCoding.length; i++) {
                    if (i == 1)
                        sb.append(" /");
                    sb.append(" ").append(metaCoding[i] & 0xFF);
                }
                ps.print(" //header: " + sb);
            }
            printArrayTo(ps, values, 0, length);
            ps.close();
            OutputStream ds = getDumpStream(this, ".bnd");
            bandCoding.writeArrayTo(ds, values, 0, length);
            ds.close();
        }

        protected int getValue() {
            assert (phase() == DISBURSE_PHASE);
            assert (valuesDisbursed < length);
            return values[valuesDisbursed++];
        }

        public void resetForSecondPass() {
            assert (phase() == DISBURSE_PHASE);
            assert (valuesDisbursed == length());
            valuesDisbursed = 0;
        }
    }

    class ByteBand extends Band {

        private ByteArrayOutputStream bytes;

        private ByteArrayOutputStream bytesForDump;

        private InputStream in;

        public ByteBand(String name) {
            super(name, BYTE1);
        }

        public int capacity() {
            return bytes == null ? -1 : Integer.MAX_VALUE;
        }

        protected void setCapacity(int cap) {
            assert (bytes == null);
            bytes = new ByteArrayOutputStream(cap);
        }

        public void destroy() {
            lengthForDebug = length();
            bytes = null;
        }

        public int length() {
            return bytes == null ? -1 : bytes.size();
        }

        public void reset() {
            bytes.reset();
        }

        protected int valuesRemainingForDebug() {
            return (bytes == null) ? -1 : ((ByteArrayInputStream) in).available();
        }

        protected void chooseBandCodings() throws IOException {
            assert (decodeEscapeValue(regularCoding.min(), regularCoding) < 0);
            assert (decodeEscapeValue(regularCoding.max(), regularCoding) < 0);
        }

        protected long computeOutputSize() {
            return bytes.size();
        }

        public void writeDataTo(OutputStream out) throws IOException {
            if (length() == 0)
                return;
            bytes.writeTo(out);
            if (optDumpBands)
                dumpBand();
            destroy();
        }

        private void dumpBand() throws IOException {
            assert (optDumpBands);
            OutputStream ds = getDumpStream(this, ".bnd");
            if (bytesForDump != null)
                bytesForDump.writeTo(ds);
            else
                bytes.writeTo(ds);
            ds.close();
        }

        public void readDataFrom(InputStream in) throws IOException {
            int vex = valuesExpected();
            if (vex == 0)
                return;
            if (verbose > 1) {
                lengthForDebug = vex;
                Utils.log.fine("Reading band " + this);
                lengthForDebug = -1;
            }
            byte[] buf = new byte[Math.min(vex, 1 << 14)];
            while (vex > 0) {
                int nr = in.read(buf, 0, Math.min(vex, buf.length));
                if (nr < 0)
                    throw new EOFException();
                bytes.write(buf, 0, nr);
                vex -= nr;
            }
            if (optDumpBands)
                dumpBand();
        }

        public void readyToDisburse() {
            in = new ByteArrayInputStream(bytes.toByteArray());
            super.readyToDisburse();
        }

        public void doneDisbursing() {
            super.doneDisbursing();
            if (optDumpBands && bytesForDump != null && bytesForDump.size() > 0) {
                try {
                    dumpBand();
                } catch (IOException ee) {
                    throw new RuntimeException(ee);
                }
            }
            in = null;
            bytes = null;
            bytesForDump = null;
        }

        public void setInputStreamFrom(InputStream in) throws IOException {
            assert (bytes == null);
            assert (assertReadyToReadFrom(this, in));
            setPhase(READ_PHASE);
            this.in = in;
            if (optDumpBands) {
                bytesForDump = new ByteArrayOutputStream();
                this.in = new FilterInputStream(in) {

                    public int read() throws IOException {
                        int ch = in.read();
                        if (ch >= 0)
                            bytesForDump.write(ch);
                        return ch;
                    }

                    public int read(byte[] b, int off, int len) throws IOException {
                        int nr = in.read(b, off, len);
                        if (nr >= 0)
                            bytesForDump.write(b, off, nr);
                        return nr;
                    }
                };
            }
            super.readyToDisburse();
        }

        public OutputStream collectorStream() {
            assert (phase() == COLLECT_PHASE);
            assert (bytes != null);
            return bytes;
        }

        public InputStream getInputStream() {
            assert (phase() == DISBURSE_PHASE);
            assert (in != null);
            return in;
        }

        public int getByte() throws IOException {
            int b = getInputStream().read();
            if (b < 0)
                throw new EOFException();
            return b;
        }

        public void putByte(int b) throws IOException {
            assert (b == (b & 0xFF));
            collectorStream().write(b);
        }

        public String toString() {
            return "byte " + super.toString();
        }
    }

    class IntBand extends ValueBand {

        public IntBand(String name, Coding regularCoding) {
            super(name, regularCoding);
        }

        public void putInt(int x) {
            assert (phase() == COLLECT_PHASE);
            addValue(x);
        }

        public int getInt() {
            return getValue();
        }

        public int getIntTotal() {
            assert (phase() == DISBURSE_PHASE);
            assert (valuesRemainingForDebug() == length());
            int total = 0;
            for (int k = length(); k > 0; k--) {
                total += getInt();
            }
            resetForSecondPass();
            return total;
        }

        public int getIntCount(int value) {
            assert (phase() == DISBURSE_PHASE);
            assert (valuesRemainingForDebug() == length());
            int total = 0;
            for (int k = length(); k > 0; k--) {
                if (getInt() == value) {
                    total += 1;
                }
            }
            resetForSecondPass();
            return total;
        }
    }

    static int getIntTotal(int[] values) {
        int total = 0;
        for (int i = 0; i < values.length; i++) {
            total += values[i];
        }
        return total;
    }

    class CPRefBand extends ValueBand {

        Index index;

        boolean nullOK;

        public CPRefBand(String name, Coding regularCoding, byte cpTag, boolean nullOK) {
            super(name, regularCoding);
            this.nullOK = nullOK;
            if (cpTag != CONSTANT_None)
                setBandIndex(this, cpTag);
        }

        public CPRefBand(String name, Coding regularCoding, byte cpTag) {
            this(name, regularCoding, cpTag, false);
        }

        public CPRefBand(String name, Coding regularCoding, Object undef) {
            this(name, regularCoding, CONSTANT_None, false);
        }

        public void setIndex(Index index) {
            this.index = index;
        }

        protected void readDataFrom(InputStream in) throws IOException {
            super.readDataFrom(in);
            assert (assertValidCPRefs(this));
        }

        public void putRef(Entry e) {
            assert (index != null);
            addValue(encodeRefOrNull(e, index));
        }

        public void putRef(Entry e, Index index) {
            assert (this.index == null);
            addValue(encodeRefOrNull(e, index));
        }

        public void putRef(Entry e, byte cptag) {
            putRef(e, getCPIndex(cptag));
        }

        public Entry getRef() {
            if (index == null)
                Utils.log.warning("No index for " + this);
            assert (index != null);
            return decodeRefOrNull(getValue(), index);
        }

        public Entry getRef(Index index) {
            assert (this.index == null);
            return decodeRefOrNull(getValue(), index);
        }

        public Entry getRef(byte cptag) {
            return getRef(getCPIndex(cptag));
        }

        private int encodeRefOrNull(Entry e, Index index) {
            int nonNullCode;
            if (e == null) {
                nonNullCode = -1;
            } else {
                nonNullCode = encodeRef(e, index);
            }
            return (nullOK ? 1 : 0) + nonNullCode;
        }

        private Entry decodeRefOrNull(int code, Index index) {
            int nonNullCode = code - (nullOK ? 1 : 0);
            if (nonNullCode == -1) {
                return null;
            } else {
                return decodeRef(nonNullCode, index);
            }
        }
    }

    private ArrayList allKQBands = new ArrayList();

    private ArrayList needPredefIndex = new ArrayList();

    int encodeRef(Entry e, Index ix) {
        int coding = ix.indexOf(e);
        if (verbose > 2)
            Utils.log.fine("putRef " + coding + " => " + e);
        return coding;
    }

    Entry decodeRef(int n, Index ix) {
        if (n < 0 || n >= ix.size())
            Utils.log.warning("decoding bad ref " + n + " in " + ix);
        Entry e = ix.getEntry(n);
        if (verbose > 2)
            Utils.log.fine("getRef " + n + " => " + e);
        return e;
    }

    private CodingChooser codingChooser;

    protected CodingChooser getCodingChooser() {
        if (codingChooser == null) {
            codingChooser = new CodingChooser(effort, basicCodings);
            if (codingChooser.stress != null && this instanceof PackageWriter) {
                List classes = ((PackageWriter) this).pkg.classes;
                if (!classes.isEmpty()) {
                    Package.Class cls = (Package.Class) classes.get(0);
                    codingChooser.addStressSeed(cls.getName().hashCode());
                }
            }
        }
        return codingChooser;
    }

    public CodingMethod chooseCoding(int[] values, int start, int end, Coding regular, String bandName, int[] sizes) {
        assert (optVaryCodings);
        if (effort <= MIN_EFFORT) {
            return regular;
        }
        CodingChooser cc = getCodingChooser();
        if (verbose > 1 || cc.verbose > 1) {
            Utils.log.fine("--- chooseCoding " + bandName);
        }
        return cc.choose(values, start, end, regular, sizes);
    }

    static final byte[] defaultMetaCoding = { _meta_default };

    static final byte[] noMetaCoding = {};

    protected static int decodeEscapeValue(int X, Coding regularCoding) {
        if (regularCoding.B() == 1 || regularCoding.L() == 0)
            return -1;
        if (regularCoding.S() != 0) {
            if (-256 <= X && X <= -1 && regularCoding.min() <= -256) {
                int XB = -1 - X;
                assert (XB >= 0 && XB < 256);
                return XB;
            }
        } else {
            int L = regularCoding.L();
            if (L <= X && X <= L + 255 && regularCoding.max() >= L + 255) {
                int XB = X - L;
                assert (XB >= 0 && XB < 256);
                return XB;
            }
        }
        return -1;
    }

    protected static int encodeEscapeValue(int XB, Coding regularCoding) {
        assert (XB >= 0 && XB < 256);
        assert (regularCoding.B() > 1 && regularCoding.L() > 0);
        int X;
        if (regularCoding.S() != 0) {
            assert (regularCoding.min() <= -256);
            X = -1 - XB;
        } else {
            int L = regularCoding.L();
            assert (regularCoding.max() >= L + 255);
            X = XB + L;
        }
        assert (decodeEscapeValue(X, regularCoding) == XB) : (regularCoding + " XB=" + XB + " X=" + X);
        return X;
    }

    static {
        boolean checkXB = false;
        assert (checkXB = true);
        if (checkXB) {
            for (int i = 0; i < basicCodings.length; i++) {
                Coding D = basicCodings[i];
                if (D == null)
                    continue;
                if (D.B() == 1)
                    continue;
                if (D.L() == 0)
                    continue;
                for (int XB = 0; XB <= 255; XB++) {
                    encodeEscapeValue(XB, D);
                }
            }
        }
    }

    class MultiBand extends Band {

        MultiBand(String name, Coding regularCoding) {
            super(name, regularCoding);
        }

        public Band init() {
            super.init();
            setCapacity(0);
            if (phase() == EXPECT_PHASE) {
                setPhase(READ_PHASE);
                setPhase(DISBURSE_PHASE);
            }
            return this;
        }

        Band[] bands = new Band[10];

        int bandCount = 0;

        int size() {
            return bandCount;
        }

        Band get(int i) {
            assert (i < bandCount);
            return bands[i];
        }

        Band[] toArray() {
            return (Band[]) realloc(bands, bandCount);
        }

        void add(Band b) {
            assert (bandCount == 0 || notePrevForAssert(b, bands[bandCount - 1]));
            if (bandCount == bands.length) {
                bands = (Band[]) realloc(bands);
            }
            bands[bandCount++] = b;
        }

        ByteBand newByteBand(String name) {
            ByteBand b = new ByteBand(name);
            b.init();
            add(b);
            return b;
        }

        IntBand newIntBand(String name) {
            IntBand b = new IntBand(name, regularCoding);
            b.init();
            add(b);
            return b;
        }

        IntBand newIntBand(String name, Coding regularCoding) {
            IntBand b = new IntBand(name, regularCoding);
            b.init();
            add(b);
            return b;
        }

        MultiBand newMultiBand(String name, Coding regularCoding) {
            MultiBand b = new MultiBand(name, regularCoding);
            b.init();
            add(b);
            return b;
        }

        CPRefBand newCPRefBand(String name, byte cpTag) {
            CPRefBand b = new CPRefBand(name, regularCoding, cpTag);
            b.init();
            add(b);
            return b;
        }

        CPRefBand newCPRefBand(String name, Coding regularCoding, byte cpTag) {
            CPRefBand b = new CPRefBand(name, regularCoding, cpTag);
            b.init();
            add(b);
            return b;
        }

        CPRefBand newCPRefBand(String name, Coding regularCoding, byte cpTag, boolean nullOK) {
            CPRefBand b = new CPRefBand(name, regularCoding, cpTag, nullOK);
            b.init();
            add(b);
            return b;
        }

        int bandCount() {
            return bandCount;
        }

        private int cap = -1;

        public int capacity() {
            return cap;
        }

        public void setCapacity(int cap) {
            this.cap = cap;
        }

        public int length() {
            return 0;
        }

        public int valuesRemainingForDebug() {
            return 0;
        }

        protected void chooseBandCodings() throws IOException {
            for (int i = 0; i < bandCount; i++) {
                Band b = bands[i];
                b.chooseBandCodings();
            }
        }

        protected long computeOutputSize() {
            long sum = 0;
            for (int i = 0; i < bandCount; i++) {
                Band b = bands[i];
                long bsize = b.outputSize();
                assert (bsize >= 0) : b;
                sum += bsize;
            }
            return sum;
        }

        protected void writeDataTo(OutputStream out) throws IOException {
            long preCount = 0;
            if (outputCounter != null)
                preCount = outputCounter.getCount();
            for (int i = 0; i < bandCount; i++) {
                Band b = bands[i];
                b.writeTo(out);
                if (outputCounter != null) {
                    long postCount = outputCounter.getCount();
                    long len = postCount - preCount;
                    preCount = postCount;
                    if ((verbose > 0 && len > 0) || verbose > 1) {
                        Utils.log.info("  ...wrote " + len + " bytes from " + b);
                    }
                }
            }
        }

        protected void readDataFrom(InputStream in) throws IOException {
            assert (false);
            for (int i = 0; i < bandCount; i++) {
                Band b = bands[i];
                b.readFrom(in);
                if ((verbose > 0 && b.length() > 0) || verbose > 1) {
                    Utils.log.info("  ...read " + b);
                }
            }
        }

        public String toString() {
            return "{" + bandCount() + " bands: " + super.toString() + "}";
        }
    }

    private static class ByteCounter extends FilterOutputStream {

        private long count;

        public ByteCounter(OutputStream out) {
            super(out);
        }

        public long getCount() {
            return count;
        }

        public void setCount(long c) {
            count = c;
        }

        public void write(int b) throws IOException {
            count++;
            if (out != null)
                out.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            count += len;
            if (out != null)
                out.write(b, off, len);
        }

        public String toString() {
            return String.valueOf(getCount());
        }
    }

    ByteCounter outputCounter;

    void writeAllBandsTo(OutputStream out) throws IOException {
        outputCounter = new ByteCounter(out);
        out = outputCounter;
        all_bands.writeTo(out);
        if (verbose > 0) {
            long nbytes = outputCounter.getCount();
            Utils.log.info("Wrote total of " + nbytes + " bytes.");
            assert (nbytes == archiveSize0 + archiveSize1);
        }
        outputCounter = null;
    }

    protected int archiveOptions;

    protected long archiveSize0;

    protected long archiveSize1;

    protected int archiveNextCount;

    static final int AH_LENGTH_0 = 3;

    static final int AH_ARCHIVE_SIZE_HI = 0;

    static final int AH_ARCHIVE_SIZE_LO = 1;

    static final int AH_LENGTH_S = 2;

    static final int AH_LENGTH = 26;

    static final int AH_FILE_HEADER_LEN = 5;

    static final int AH_SPECIAL_FORMAT_LEN = 2;

    static final int AH_CP_NUMBER_LEN = 4;

    static final int AH_LENGTH_MIN = AH_LENGTH - (AH_SPECIAL_FORMAT_LEN + AH_FILE_HEADER_LEN + AH_CP_NUMBER_LEN);

    static final int AB_FLAGS_HI = 0;

    static final int AB_FLAGS_LO = 1;

    static final int AB_ATTR_COUNT = 2;

    static final int AB_ATTR_INDEXES = 3;

    static final int AB_ATTR_CALLS = 4;

    static IntBand getAttrBand(MultiBand xxx_attr_bands, int which) {
        IntBand b = (IntBand) xxx_attr_bands.get(which);
        switch(which) {
            case AB_FLAGS_HI:
                assert (b.name().endsWith("_flags_hi"));
                break;
            case AB_FLAGS_LO:
                assert (b.name().endsWith("_flags_lo"));
                break;
            case AB_ATTR_COUNT:
                assert (b.name().endsWith("_attr_count"));
                break;
            case AB_ATTR_INDEXES:
                assert (b.name().endsWith("_attr_indexes"));
                break;
            case AB_ATTR_CALLS:
                assert (b.name().endsWith("_attr_calls"));
                break;
            default:
                assert (false);
                break;
        }
        return b;
    }

    static private final boolean NULL_IS_OK = true;

    MultiBand all_bands = (MultiBand) new MultiBand("(package)", UNSIGNED5).init();

    ByteBand archive_magic = all_bands.newByteBand("archive_magic");

    IntBand archive_header_0 = all_bands.newIntBand("archive_header_0", UNSIGNED5);

    IntBand archive_header_S = all_bands.newIntBand("archive_header_S", UNSIGNED5);

    IntBand archive_header_1 = all_bands.newIntBand("archive_header_1", UNSIGNED5);

    ByteBand band_headers = all_bands.newByteBand("band_headers");

    MultiBand cp_bands = all_bands.newMultiBand("(constant_pool)", DELTA5);

    IntBand cp_Utf8_prefix = cp_bands.newIntBand("cp_Utf8_prefix");

    IntBand cp_Utf8_suffix = cp_bands.newIntBand("cp_Utf8_suffix", UNSIGNED5);

    IntBand cp_Utf8_chars = cp_bands.newIntBand("cp_Utf8_chars", CHAR3);

    IntBand cp_Utf8_big_suffix = cp_bands.newIntBand("cp_Utf8_big_suffix");

    MultiBand cp_Utf8_big_chars = cp_bands.newMultiBand("(cp_Utf8_big_chars)", DELTA5);

    IntBand cp_Int = cp_bands.newIntBand("cp_Int", UDELTA5);

    IntBand cp_Float = cp_bands.newIntBand("cp_Float", UDELTA5);

    IntBand cp_Long_hi = cp_bands.newIntBand("cp_Long_hi", UDELTA5);

    IntBand cp_Long_lo = cp_bands.newIntBand("cp_Long_lo");

    IntBand cp_Double_hi = cp_bands.newIntBand("cp_Double_hi", UDELTA5);

    IntBand cp_Double_lo = cp_bands.newIntBand("cp_Double_lo");

    CPRefBand cp_String = cp_bands.newCPRefBand("cp_String", UDELTA5, CONSTANT_Utf8);

    CPRefBand cp_Class = cp_bands.newCPRefBand("cp_Class", UDELTA5, CONSTANT_Utf8);

    CPRefBand cp_Signature_form = cp_bands.newCPRefBand("cp_Signature_form", CONSTANT_Utf8);

    CPRefBand cp_Signature_classes = cp_bands.newCPRefBand("cp_Signature_classes", UDELTA5, CONSTANT_Class);

    CPRefBand cp_Descr_name = cp_bands.newCPRefBand("cp_Descr_name", CONSTANT_Utf8);

    CPRefBand cp_Descr_type = cp_bands.newCPRefBand("cp_Descr_type", UDELTA5, CONSTANT_Signature);

    CPRefBand cp_Field_class = cp_bands.newCPRefBand("cp_Field_class", CONSTANT_Class);

    CPRefBand cp_Field_desc = cp_bands.newCPRefBand("cp_Field_desc", UDELTA5, CONSTANT_NameandType);

    CPRefBand cp_Method_class = cp_bands.newCPRefBand("cp_Method_class", CONSTANT_Class);

    CPRefBand cp_Method_desc = cp_bands.newCPRefBand("cp_Method_desc", UDELTA5, CONSTANT_NameandType);

    CPRefBand cp_Imethod_class = cp_bands.newCPRefBand("cp_Imethod_class", CONSTANT_Class);

    CPRefBand cp_Imethod_desc = cp_bands.newCPRefBand("cp_Imethod_desc", UDELTA5, CONSTANT_NameandType);

    MultiBand attr_definition_bands = all_bands.newMultiBand("(attr_definition_bands)", UNSIGNED5);

    ByteBand attr_definition_headers = attr_definition_bands.newByteBand("attr_definition_headers");

    CPRefBand attr_definition_name = attr_definition_bands.newCPRefBand("attr_definition_name", CONSTANT_Utf8);

    CPRefBand attr_definition_layout = attr_definition_bands.newCPRefBand("attr_definition_layout", CONSTANT_Utf8);

    MultiBand ic_bands = all_bands.newMultiBand("(ic_bands)", DELTA5);

    CPRefBand ic_this_class = ic_bands.newCPRefBand("ic_this_class", UDELTA5, CONSTANT_Class);

    IntBand ic_flags = ic_bands.newIntBand("ic_flags", UNSIGNED5);

    CPRefBand ic_outer_class = ic_bands.newCPRefBand("ic_outer_class", DELTA5, CONSTANT_Class, NULL_IS_OK);

    CPRefBand ic_name = ic_bands.newCPRefBand("ic_name", DELTA5, CONSTANT_Utf8, NULL_IS_OK);

    MultiBand class_bands = all_bands.newMultiBand("(class_bands)", DELTA5);

    CPRefBand class_this = class_bands.newCPRefBand("class_this", CONSTANT_Class);

    CPRefBand class_super = class_bands.newCPRefBand("class_super", CONSTANT_Class);

    IntBand class_interface_count = class_bands.newIntBand("class_interface_count");

    CPRefBand class_interface = class_bands.newCPRefBand("class_interface", CONSTANT_Class);

    IntBand class_field_count = class_bands.newIntBand("class_field_count");

    IntBand class_method_count = class_bands.newIntBand("class_method_count");

    CPRefBand field_descr = class_bands.newCPRefBand("field_descr", CONSTANT_NameandType);

    MultiBand field_attr_bands = class_bands.newMultiBand("(field_attr_bands)", UNSIGNED5);

    IntBand field_flags_hi = field_attr_bands.newIntBand("field_flags_hi");

    IntBand field_flags_lo = field_attr_bands.newIntBand("field_flags_lo");

    IntBand field_attr_count = field_attr_bands.newIntBand("field_attr_count");

    IntBand field_attr_indexes = field_attr_bands.newIntBand("field_attr_indexes");

    IntBand field_attr_calls = field_attr_bands.newIntBand("field_attr_calls");

    CPRefBand field_ConstantValue_KQ = field_attr_bands.newCPRefBand("field_ConstantValue_KQ", CONSTANT_Literal);

    CPRefBand field_Signature_RS = field_attr_bands.newCPRefBand("field_Signature_RS", CONSTANT_Signature);

    MultiBand field_metadata_bands = field_attr_bands.newMultiBand("(field_metadata_bands)", UNSIGNED5);

    CPRefBand method_descr = class_bands.newCPRefBand("method_descr", MDELTA5, CONSTANT_NameandType);

    MultiBand method_attr_bands = class_bands.newMultiBand("(method_attr_bands)", UNSIGNED5);

    IntBand method_flags_hi = method_attr_bands.newIntBand("method_flags_hi");

    IntBand method_flags_lo = method_attr_bands.newIntBand("method_flags_lo");

    IntBand method_attr_count = method_attr_bands.newIntBand("method_attr_count");

    IntBand method_attr_indexes = method_attr_bands.newIntBand("method_attr_indexes");

    IntBand method_attr_calls = method_attr_bands.newIntBand("method_attr_calls");

    IntBand method_Exceptions_N = method_attr_bands.newIntBand("method_Exceptions_N");

    CPRefBand method_Exceptions_RC = method_attr_bands.newCPRefBand("method_Exceptions_RC", CONSTANT_Class);

    CPRefBand method_Signature_RS = method_attr_bands.newCPRefBand("method_Signature_RS", CONSTANT_Signature);

    MultiBand method_metadata_bands = method_attr_bands.newMultiBand("(method_metadata_bands)", UNSIGNED5);

    MultiBand class_attr_bands = class_bands.newMultiBand("(class_attr_bands)", UNSIGNED5);

    IntBand class_flags_hi = class_attr_bands.newIntBand("class_flags_hi");

    IntBand class_flags_lo = class_attr_bands.newIntBand("class_flags_lo");

    IntBand class_attr_count = class_attr_bands.newIntBand("class_attr_count");

    IntBand class_attr_indexes = class_attr_bands.newIntBand("class_attr_indexes");

    IntBand class_attr_calls = class_attr_bands.newIntBand("class_attr_calls");

    CPRefBand class_SourceFile_RUN = class_attr_bands.newCPRefBand("class_SourceFile_RUN", UNSIGNED5, CONSTANT_Utf8, NULL_IS_OK);

    CPRefBand class_EnclosingMethod_RC = class_attr_bands.newCPRefBand("class_EnclosingMethod_RC", CONSTANT_Class);

    CPRefBand class_EnclosingMethod_RDN = class_attr_bands.newCPRefBand("class_EnclosingMethod_RDN", UNSIGNED5, CONSTANT_NameandType, NULL_IS_OK);

    CPRefBand class_Signature_RS = class_attr_bands.newCPRefBand("class_Signature_RS", CONSTANT_Signature);

    MultiBand class_metadata_bands = class_attr_bands.newMultiBand("(class_metadata_bands)", UNSIGNED5);

    IntBand class_InnerClasses_N = class_attr_bands.newIntBand("class_InnerClasses_N");

    CPRefBand class_InnerClasses_RC = class_attr_bands.newCPRefBand("class_InnerClasses_RC", CONSTANT_Class);

    IntBand class_InnerClasses_F = class_attr_bands.newIntBand("class_InnerClasses_F");

    CPRefBand class_InnerClasses_outer_RCN = class_attr_bands.newCPRefBand("class_InnerClasses_outer_RCN", UNSIGNED5, CONSTANT_Class, NULL_IS_OK);

    CPRefBand class_InnerClasses_name_RUN = class_attr_bands.newCPRefBand("class_InnerClasses_name_RUN", UNSIGNED5, CONSTANT_Utf8, NULL_IS_OK);

    IntBand class_ClassFile_version_minor_H = class_attr_bands.newIntBand("class_ClassFile_version_minor_H");

    IntBand class_ClassFile_version_major_H = class_attr_bands.newIntBand("class_ClassFile_version_major_H");

    MultiBand code_bands = class_bands.newMultiBand("(code_bands)", UNSIGNED5);

    ByteBand code_headers = code_bands.newByteBand("code_headers");

    IntBand code_max_stack = code_bands.newIntBand("code_max_stack", UNSIGNED5);

    IntBand code_max_na_locals = code_bands.newIntBand("code_max_na_locals", UNSIGNED5);

    IntBand code_handler_count = code_bands.newIntBand("code_handler_count", UNSIGNED5);

    IntBand code_handler_start_P = code_bands.newIntBand("code_handler_start_P", BCI5);

    IntBand code_handler_end_PO = code_bands.newIntBand("code_handler_end_PO", BRANCH5);

    IntBand code_handler_catch_PO = code_bands.newIntBand("code_handler_catch_PO", BRANCH5);

    CPRefBand code_handler_class_RCN = code_bands.newCPRefBand("code_handler_class_RCN", UNSIGNED5, CONSTANT_Class, NULL_IS_OK);

    MultiBand code_attr_bands = class_bands.newMultiBand("(code_attr_bands)", UNSIGNED5);

    IntBand code_flags_hi = code_attr_bands.newIntBand("code_flags_hi");

    IntBand code_flags_lo = code_attr_bands.newIntBand("code_flags_lo");

    IntBand code_attr_count = code_attr_bands.newIntBand("code_attr_count");

    IntBand code_attr_indexes = code_attr_bands.newIntBand("code_attr_indexes");

    IntBand code_attr_calls = code_attr_bands.newIntBand("code_attr_calls");

    MultiBand stackmap_bands = code_attr_bands.newMultiBand("StackMapTable_bands", UNSIGNED5);

    IntBand code_StackMapTable_N = stackmap_bands.newIntBand("code_StackMapTable_N");

    IntBand code_StackMapTable_frame_T = stackmap_bands.newIntBand("code_StackMapTable_frame_T", BYTE1);

    IntBand code_StackMapTable_local_N = stackmap_bands.newIntBand("code_StackMapTable_local_N");

    IntBand code_StackMapTable_stack_N = stackmap_bands.newIntBand("code_StackMapTable_stack_N");

    IntBand code_StackMapTable_offset = stackmap_bands.newIntBand("code_StackMapTable_offset", UNSIGNED5);

    IntBand code_StackMapTable_T = stackmap_bands.newIntBand("code_StackMapTable_T", BYTE1);

    CPRefBand code_StackMapTable_RC = stackmap_bands.newCPRefBand("code_StackMapTable_RC", CONSTANT_Class);

    IntBand code_StackMapTable_P = stackmap_bands.newIntBand("code_StackMapTable_P", BCI5);

    IntBand code_LineNumberTable_N = code_attr_bands.newIntBand("code_LineNumberTable_N");

    IntBand code_LineNumberTable_bci_P = code_attr_bands.newIntBand("code_LineNumberTable_bci_P", BCI5);

    IntBand code_LineNumberTable_line = code_attr_bands.newIntBand("code_LineNumberTable_line");

    IntBand code_LocalVariableTable_N = code_attr_bands.newIntBand("code_LocalVariableTable_N");

    IntBand code_LocalVariableTable_bci_P = code_attr_bands.newIntBand("code_LocalVariableTable_bci_P", BCI5);

    IntBand code_LocalVariableTable_span_O = code_attr_bands.newIntBand("code_LocalVariableTable_span_O", BRANCH5);

    CPRefBand code_LocalVariableTable_name_RU = code_attr_bands.newCPRefBand("code_LocalVariableTable_name_RU", CONSTANT_Utf8);

    CPRefBand code_LocalVariableTable_type_RS = code_attr_bands.newCPRefBand("code_LocalVariableTable_type_RS", CONSTANT_Signature);

    IntBand code_LocalVariableTable_slot = code_attr_bands.newIntBand("code_LocalVariableTable_slot");

    IntBand code_LocalVariableTypeTable_N = code_attr_bands.newIntBand("code_LocalVariableTypeTable_N");

    IntBand code_LocalVariableTypeTable_bci_P = code_attr_bands.newIntBand("code_LocalVariableTypeTable_bci_P", BCI5);

    IntBand code_LocalVariableTypeTable_span_O = code_attr_bands.newIntBand("code_LocalVariableTypeTable_span_O", BRANCH5);

    CPRefBand code_LocalVariableTypeTable_name_RU = code_attr_bands.newCPRefBand("code_LocalVariableTypeTable_name_RU", CONSTANT_Utf8);

    CPRefBand code_LocalVariableTypeTable_type_RS = code_attr_bands.newCPRefBand("code_LocalVariableTypeTable_type_RS", CONSTANT_Signature);

    IntBand code_LocalVariableTypeTable_slot = code_attr_bands.newIntBand("code_LocalVariableTypeTable_slot");

    MultiBand bc_bands = all_bands.newMultiBand("(byte_codes)", UNSIGNED5);

    ByteBand bc_codes = bc_bands.newByteBand("bc_codes");

    IntBand bc_case_count = bc_bands.newIntBand("bc_case_count");

    IntBand bc_case_value = bc_bands.newIntBand("bc_case_value", DELTA5);

    ByteBand bc_byte = bc_bands.newByteBand("bc_byte");

    IntBand bc_short = bc_bands.newIntBand("bc_short", DELTA5);

    IntBand bc_local = bc_bands.newIntBand("bc_local");

    IntBand bc_label = bc_bands.newIntBand("bc_label", BRANCH5);

    CPRefBand bc_intref = bc_bands.newCPRefBand("bc_intref", DELTA5, CONSTANT_Integer);

    CPRefBand bc_floatref = bc_bands.newCPRefBand("bc_floatref", DELTA5, CONSTANT_Float);

    CPRefBand bc_longref = bc_bands.newCPRefBand("bc_longref", DELTA5, CONSTANT_Long);

    CPRefBand bc_doubleref = bc_bands.newCPRefBand("bc_doubleref", DELTA5, CONSTANT_Double);

    CPRefBand bc_stringref = bc_bands.newCPRefBand("bc_stringref", DELTA5, CONSTANT_String);

    CPRefBand bc_classref = bc_bands.newCPRefBand("bc_classref", UNSIGNED5, CONSTANT_Class, NULL_IS_OK);

    CPRefBand bc_fieldref = bc_bands.newCPRefBand("bc_fieldref", DELTA5, CONSTANT_Fieldref);

    CPRefBand bc_methodref = bc_bands.newCPRefBand("bc_methodref", CONSTANT_Methodref);

    CPRefBand bc_imethodref = bc_bands.newCPRefBand("bc_imethodref", DELTA5, CONSTANT_InterfaceMethodref);

    CPRefBand bc_thisfield = bc_bands.newCPRefBand("bc_thisfield", CONSTANT_None);

    CPRefBand bc_superfield = bc_bands.newCPRefBand("bc_superfield", CONSTANT_None);

    CPRefBand bc_thismethod = bc_bands.newCPRefBand("bc_thismethod", CONSTANT_None);

    CPRefBand bc_supermethod = bc_bands.newCPRefBand("bc_supermethod", CONSTANT_None);

    IntBand bc_initref = bc_bands.newIntBand("bc_initref");

    CPRefBand bc_escref = bc_bands.newCPRefBand("bc_escref", CONSTANT_All);

    IntBand bc_escrefsize = bc_bands.newIntBand("bc_escrefsize");

    IntBand bc_escsize = bc_bands.newIntBand("bc_escsize");

    ByteBand bc_escbyte = bc_bands.newByteBand("bc_escbyte");

    MultiBand file_bands = all_bands.newMultiBand("(file_bands)", UNSIGNED5);

    CPRefBand file_name = file_bands.newCPRefBand("file_name", CONSTANT_Utf8);

    IntBand file_size_hi = file_bands.newIntBand("file_size_hi");

    IntBand file_size_lo = file_bands.newIntBand("file_size_lo");

    IntBand file_modtime = file_bands.newIntBand("file_modtime", DELTA5);

    IntBand file_options = file_bands.newIntBand("file_options");

    ByteBand file_bits = file_bands.newByteBand("file_bits");

    protected void setBandIndexes() {
        for (Iterator i = needPredefIndex.iterator(); i.hasNext(); ) {
            Object[] need = (Object[]) i.next();
            CPRefBand b = (CPRefBand) need[0];
            Byte which = (Byte) need[1];
            b.setIndex(getCPIndex(which.byteValue()));
        }
        needPredefIndex = null;
        if (verbose > 3) {
            printCDecl(all_bands);
        }
    }

    protected void setBandIndex(CPRefBand b, byte which) {
        Object[] need = { b, new Byte(which) };
        if (which == CONSTANT_Literal) {
            allKQBands.add(b);
        } else if (needPredefIndex != null) {
            needPredefIndex.add(need);
        } else {
            b.setIndex(getCPIndex(which));
        }
    }

    protected void setConstantValueIndex(com.sun.java.util.jar.pack.Package.Class.Field f) {
        Index ix = null;
        if (f != null) {
            byte tag = f.getLiteralTag();
            ix = getCPIndex(tag);
            if (verbose > 2)
                Utils.log.fine("setConstantValueIndex " + f + " " + ConstantPool.tagName(tag) + " => " + ix);
            assert (ix != null);
        }
        for (Iterator i = allKQBands.iterator(); i.hasNext(); ) {
            CPRefBand xxx_KQ = (CPRefBand) i.next();
            xxx_KQ.setIndex(ix);
        }
    }

    protected MultiBand[] metadataBands = new MultiBand[ATTR_CONTEXT_LIMIT];

    {
        metadataBands[ATTR_CONTEXT_CLASS] = class_metadata_bands;
        metadataBands[ATTR_CONTEXT_FIELD] = field_metadata_bands;
        metadataBands[ATTR_CONTEXT_METHOD] = method_metadata_bands;
    }

    public static final int ADH_CONTEXT_MASK = 0x3;

    public static final int ADH_BIT_SHIFT = 0x2;

    public static final int ADH_BIT_IS_LSB = 1;

    public static final int ATTR_INDEX_OVERFLOW = -1;

    public int[] attrIndexLimit = new int[ATTR_CONTEXT_LIMIT];

    protected long[] attrFlagMask = new long[ATTR_CONTEXT_LIMIT];

    protected long[] attrDefSeen = new long[ATTR_CONTEXT_LIMIT];

    protected int[] attrOverflowMask = new int[ATTR_CONTEXT_LIMIT];

    protected int attrClassFileVersionMask;

    protected HashMap attrBandTable = new HashMap();

    protected final Attribute.Layout attrCodeEmpty;

    protected final Attribute.Layout attrInnerClassesEmpty;

    protected final Attribute.Layout attrClassFileVersion;

    protected final Attribute.Layout attrConstantValue;

    HashMap attrIndexTable = new HashMap();

    protected ArrayList[] attrDefs = new ArrayList[ATTR_CONTEXT_LIMIT];

    {
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            assert (attrIndexLimit[i] == 0);
            attrIndexLimit[i] = 32;
            attrDefs[i] = new ArrayList(Collections.nCopies(attrIndexLimit[i], null));
        }
        attrInnerClassesEmpty = predefineAttribute(CLASS_ATTR_InnerClasses, ATTR_CONTEXT_CLASS, null, "InnerClasses", "");
        assert (attrInnerClassesEmpty == Package.attrInnerClassesEmpty);
        predefineAttribute(CLASS_ATTR_SourceFile, ATTR_CONTEXT_CLASS, new Band[] { class_SourceFile_RUN }, "SourceFile", "RUNH");
        predefineAttribute(CLASS_ATTR_EnclosingMethod, ATTR_CONTEXT_CLASS, new Band[] { class_EnclosingMethod_RC, class_EnclosingMethod_RDN }, "EnclosingMethod", "RCHRDNH");
        attrClassFileVersion = predefineAttribute(CLASS_ATTR_ClassFile_version, ATTR_CONTEXT_CLASS, new Band[] { class_ClassFile_version_minor_H, class_ClassFile_version_major_H }, ".ClassFile.version", "HH");
        predefineAttribute(X_ATTR_Signature, ATTR_CONTEXT_CLASS, new Band[] { class_Signature_RS }, "Signature", "RSH");
        predefineAttribute(X_ATTR_Deprecated, ATTR_CONTEXT_CLASS, null, "Deprecated", "");
        predefineAttribute(X_ATTR_OVERFLOW, ATTR_CONTEXT_CLASS, null, ".Overflow", "");
        attrConstantValue = predefineAttribute(FIELD_ATTR_ConstantValue, ATTR_CONTEXT_FIELD, new Band[] { field_ConstantValue_KQ }, "ConstantValue", "KQH");
        predefineAttribute(X_ATTR_Signature, ATTR_CONTEXT_FIELD, new Band[] { field_Signature_RS }, "Signature", "RSH");
        predefineAttribute(X_ATTR_Deprecated, ATTR_CONTEXT_FIELD, null, "Deprecated", "");
        predefineAttribute(X_ATTR_OVERFLOW, ATTR_CONTEXT_FIELD, null, ".Overflow", "");
        attrCodeEmpty = predefineAttribute(METHOD_ATTR_Code, ATTR_CONTEXT_METHOD, null, "Code", "");
        predefineAttribute(METHOD_ATTR_Exceptions, ATTR_CONTEXT_METHOD, new Band[] { method_Exceptions_N, method_Exceptions_RC }, "Exceptions", "NH[RCH]");
        assert (attrCodeEmpty == Package.attrCodeEmpty);
        predefineAttribute(X_ATTR_Signature, ATTR_CONTEXT_METHOD, new Band[] { method_Signature_RS }, "Signature", "RSH");
        predefineAttribute(X_ATTR_Deprecated, ATTR_CONTEXT_METHOD, null, "Deprecated", "");
        predefineAttribute(X_ATTR_OVERFLOW, ATTR_CONTEXT_METHOD, null, ".Overflow", "");
        for (int ctype = 0; ctype < ATTR_CONTEXT_LIMIT; ctype++) {
            MultiBand xxx_metadata_bands = metadataBands[ctype];
            if (xxx_metadata_bands == null)
                continue;
            predefineAttribute(X_ATTR_RuntimeVisibleAnnotations, ATTR_CONTEXT_NAME[ctype] + "_RVA_", xxx_metadata_bands, Attribute.lookup(null, ctype, "RuntimeVisibleAnnotations"));
            predefineAttribute(X_ATTR_RuntimeInvisibleAnnotations, ATTR_CONTEXT_NAME[ctype] + "_RIA_", xxx_metadata_bands, Attribute.lookup(null, ctype, "RuntimeInvisibleAnnotations"));
            if (ctype != ATTR_CONTEXT_METHOD)
                continue;
            predefineAttribute(METHOD_ATTR_RuntimeVisibleParameterAnnotations, "method_RVPA_", xxx_metadata_bands, Attribute.lookup(null, ctype, "RuntimeVisibleParameterAnnotations"));
            predefineAttribute(METHOD_ATTR_RuntimeInvisibleParameterAnnotations, "method_RIPA_", xxx_metadata_bands, Attribute.lookup(null, ctype, "RuntimeInvisibleParameterAnnotations"));
            predefineAttribute(METHOD_ATTR_AnnotationDefault, "method_AD_", xxx_metadata_bands, Attribute.lookup(null, ctype, "AnnotationDefault"));
        }
        Attribute.Layout stackMapDef = Attribute.lookup(null, ATTR_CONTEXT_CODE, "StackMapTable").layout();
        predefineAttribute(CODE_ATTR_StackMapTable, ATTR_CONTEXT_CODE, stackmap_bands.toArray(), stackMapDef.name(), stackMapDef.layout());
        predefineAttribute(CODE_ATTR_LineNumberTable, ATTR_CONTEXT_CODE, new Band[] { code_LineNumberTable_N, code_LineNumberTable_bci_P, code_LineNumberTable_line }, "LineNumberTable", "NH[PHH]");
        predefineAttribute(CODE_ATTR_LocalVariableTable, ATTR_CONTEXT_CODE, new Band[] { code_LocalVariableTable_N, code_LocalVariableTable_bci_P, code_LocalVariableTable_span_O, code_LocalVariableTable_name_RU, code_LocalVariableTable_type_RS, code_LocalVariableTable_slot }, "LocalVariableTable", "NH[PHOHRUHRSHH]");
        predefineAttribute(CODE_ATTR_LocalVariableTypeTable, ATTR_CONTEXT_CODE, new Band[] { code_LocalVariableTypeTable_N, code_LocalVariableTypeTable_bci_P, code_LocalVariableTypeTable_span_O, code_LocalVariableTypeTable_name_RU, code_LocalVariableTypeTable_type_RS, code_LocalVariableTypeTable_slot }, "LocalVariableTypeTable", "NH[PHOHRUHRSHH]");
        predefineAttribute(X_ATTR_OVERFLOW, ATTR_CONTEXT_CODE, null, ".Overflow", "");
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            attrDefSeen[i] = 0;
        }
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            attrOverflowMask[i] = (1 << X_ATTR_OVERFLOW);
            attrIndexLimit[i] = 0;
        }
        attrClassFileVersionMask = (1 << CLASS_ATTR_ClassFile_version);
    }

    private void adjustToMajver() {
        if (getPackageMajver() < JAVA6_PACKAGE_MAJOR_VERSION) {
            if (verbose > 0)
                Utils.log.fine("Legacy package version");
            undefineAttribute(CODE_ATTR_StackMapTable, ATTR_CONTEXT_CODE);
        }
    }

    protected void initAttrIndexLimit() {
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            assert (attrIndexLimit[i] == 0);
            attrIndexLimit[i] = (haveFlagsHi(i) ? 63 : 32);
            assert (attrDefs[i].size() == 32);
            int addMore = attrIndexLimit[i] - attrDefs[i].size();
            attrDefs[i].addAll(Collections.nCopies(addMore, null));
        }
    }

    protected boolean haveFlagsHi(int ctype) {
        int mask = 1 << (LG_AO_HAVE_XXX_FLAGS_HI + ctype);
        switch(ctype) {
            case ATTR_CONTEXT_CLASS:
                assert (mask == AO_HAVE_CLASS_FLAGS_HI);
                break;
            case ATTR_CONTEXT_FIELD:
                assert (mask == AO_HAVE_FIELD_FLAGS_HI);
                break;
            case ATTR_CONTEXT_METHOD:
                assert (mask == AO_HAVE_METHOD_FLAGS_HI);
                break;
            case ATTR_CONTEXT_CODE:
                assert (mask == AO_HAVE_CODE_FLAGS_HI);
                break;
            default:
                assert (false);
        }
        return testBit(archiveOptions, mask);
    }

    protected ArrayList getPredefinedAttrs(int ctype) {
        assert (attrIndexLimit[ctype] != 0);
        ArrayList res = new ArrayList(attrIndexLimit[ctype]);
        for (int ai = 0; ai < attrIndexLimit[ctype]; ai++) {
            if (testBit(attrDefSeen[ctype], 1L << ai))
                continue;
            Attribute.Layout def = (Attribute.Layout) attrDefs[ctype].get(ai);
            if (def == null)
                continue;
            assert (isPredefinedAttr(ctype, ai));
            res.add(def);
        }
        return res;
    }

    protected boolean isPredefinedAttr(int ctype, int ai) {
        assert (attrIndexLimit[ctype] != 0);
        if (ai >= attrIndexLimit[ctype])
            return false;
        if (testBit(attrDefSeen[ctype], 1L << ai))
            return false;
        return (attrDefs[ctype].get(ai) != null);
    }

    protected void adjustSpecialAttrMasks() {
        attrClassFileVersionMask &= ~attrDefSeen[ATTR_CONTEXT_CLASS];
        for (int i = 0; i < ATTR_CONTEXT_LIMIT; i++) {
            attrOverflowMask[i] &= ~attrDefSeen[i];
        }
    }

    protected Attribute makeClassFileVersionAttr(int minver, int majver) {
        byte[] bytes = { (byte) (minver >> 8), (byte) minver, (byte) (majver >> 8), (byte) majver };
        return attrClassFileVersion.addContent(bytes);
    }

    protected short[] parseClassFileVersionAttr(Attribute attr) {
        assert (attr.layout() == attrClassFileVersion);
        assert (attr.size() == 4);
        byte[] bytes = attr.bytes();
        int minver = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        int majver = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        return new short[] { (short) minver, (short) majver };
    }

    private boolean assertBandOKForElems(Band[] ab, Attribute.Layout.Element[] elems) {
        for (int i = 0; i < elems.length; i++) {
            assert (assertBandOKForElem(ab, elems[i]));
        }
        return true;
    }

    private boolean assertBandOKForElem(Band[] ab, Attribute.Layout.Element e) {
        Band b = null;
        if (e.bandIndex != Attribute.NO_BAND_INDEX)
            b = ab[e.bandIndex];
        Coding rc = UNSIGNED5;
        boolean wantIntBand = true;
        switch(e.kind) {
            case Attribute.EK_INT:
                if (e.flagTest(Attribute.EF_SIGN)) {
                    rc = SIGNED5;
                } else if (e.len == 1) {
                    rc = BYTE1;
                }
                break;
            case Attribute.EK_BCI:
                if (!e.flagTest(Attribute.EF_DELTA)) {
                    rc = BCI5;
                } else {
                    rc = BRANCH5;
                }
                break;
            case Attribute.EK_BCO:
                rc = BRANCH5;
                break;
            case Attribute.EK_FLAG:
                if (e.len == 1)
                    rc = BYTE1;
                break;
            case Attribute.EK_REPL:
                if (e.len == 1)
                    rc = BYTE1;
                assertBandOKForElems(ab, e.body);
                break;
            case Attribute.EK_UN:
                if (e.flagTest(Attribute.EF_SIGN)) {
                    rc = SIGNED5;
                } else if (e.len == 1) {
                    rc = BYTE1;
                }
                assertBandOKForElems(ab, e.body);
                break;
            case Attribute.EK_CASE:
                assert (b == null);
                assertBandOKForElems(ab, e.body);
                return true;
            case Attribute.EK_CALL:
                assert (b == null);
                return true;
            case Attribute.EK_CBLE:
                assert (b == null);
                assertBandOKForElems(ab, e.body);
                return true;
            case Attribute.EK_REF:
                wantIntBand = false;
                assert (b instanceof CPRefBand);
                assert (((CPRefBand) b).nullOK == e.flagTest(Attribute.EF_NULL));
                break;
            default:
                assert (false);
        }
        assert (b.regularCoding == rc) : (e + " // " + b);
        if (wantIntBand)
            assert (b instanceof IntBand);
        return true;
    }

    private Attribute.Layout predefineAttribute(int index, int ctype, Band[] ab, String name, String layout) {
        Attribute.Layout def = Attribute.find(ctype, name, layout).layout();
        if (index >= 0) {
            setAttributeLayoutIndex(def, index);
        }
        if (ab == null) {
            ab = new Band[0];
        }
        assert (attrBandTable.get(def) == null);
        attrBandTable.put(def, ab);
        assert (def.bandCount == ab.length) : (def + " // " + Arrays.asList(ab));
        assert (assertBandOKForElems(ab, def.elems));
        return def;
    }

    private Attribute.Layout predefineAttribute(int index, String bandPrefix, MultiBand addHere, Attribute attr) {
        Attribute.Layout def = attr.layout();
        int ctype = def.ctype();
        return predefineAttribute(index, ctype, makeNewAttributeBands(bandPrefix, def, addHere), def.name(), def.layout());
    }

    private void undefineAttribute(int index, int ctype) {
        if (verbose > 1) {
            System.out.println("Removing predefined " + ATTR_CONTEXT_NAME[ctype] + " attribute on bit " + index);
        }
        List defList = attrDefs[ctype];
        Attribute.Layout def = (Attribute.Layout) defList.get(index);
        assert (def != null);
        defList.set(index, null);
        attrIndexTable.put(def, null);
        assert (index < 64);
        attrDefSeen[ctype] &= ~(1L << index);
        attrFlagMask[ctype] &= ~(1L << index);
        Band[] ab = (Band[]) attrBandTable.get(def);
        for (int j = 0; j < ab.length; j++) {
            ab[j].doneWithUnusedBand();
        }
    }

    protected MultiBand[] attrBands = new MultiBand[ATTR_CONTEXT_LIMIT];

    {
        attrBands[ATTR_CONTEXT_CLASS] = class_attr_bands;
        attrBands[ATTR_CONTEXT_FIELD] = field_attr_bands;
        attrBands[ATTR_CONTEXT_METHOD] = method_attr_bands;
        attrBands[ATTR_CONTEXT_CODE] = code_attr_bands;
    }

    void makeNewAttributeBands() {
        adjustSpecialAttrMasks();
        for (int ctype = 0; ctype < ATTR_CONTEXT_LIMIT; ctype++) {
            String cname = ATTR_CONTEXT_NAME[ctype];
            MultiBand xxx_attr_bands = attrBands[ctype];
            long defSeen = attrDefSeen[ctype];
            assert ((defSeen & ~attrFlagMask[ctype]) == 0);
            for (int i = 0; i < attrDefs[ctype].size(); i++) {
                Attribute.Layout def = (Attribute.Layout) attrDefs[ctype].get(i);
                if (def == null)
                    continue;
                if (def.bandCount == 0)
                    continue;
                if (i < attrIndexLimit[ctype] && !testBit(defSeen, 1L << i)) {
                    assert (attrBandTable.get(def) != null);
                    continue;
                }
                int base = xxx_attr_bands.size();
                String pfx = cname + "_" + def.name() + "_";
                if (verbose > 1)
                    Utils.log.fine("Making new bands for " + def);
                Band[] newAB = makeNewAttributeBands(pfx, def, xxx_attr_bands);
                assert (newAB.length == def.bandCount);
                Band[] prevAB = (Band[]) attrBandTable.put(def, newAB);
                if (prevAB != null) {
                    for (int j = 0; j < prevAB.length; j++) {
                        prevAB[j].doneWithUnusedBand();
                    }
                }
            }
        }
    }

    private Band[] makeNewAttributeBands(String pfx, Attribute.Layout def, MultiBand addHere) {
        int base = addHere.size();
        makeNewAttributeBands(pfx, def.elems, addHere);
        int nb = addHere.size() - base;
        Band[] newAB = new Band[nb];
        for (int i = 0; i < nb; i++) {
            newAB[i] = addHere.get(base + i);
        }
        return newAB;
    }

    private void makeNewAttributeBands(String pfx, Attribute.Layout.Element[] elems, MultiBand ab) {
        for (int i = 0; i < elems.length; i++) {
            Attribute.Layout.Element e = elems[i];
            String name = pfx + ab.size() + "_" + e.layout;
            {
                int tem;
                if ((tem = name.indexOf('[')) > 0)
                    name = name.substring(0, tem);
                if ((tem = name.indexOf('(')) > 0)
                    name = name.substring(0, tem);
                if (name.endsWith("H"))
                    name = name.substring(0, name.length() - 1);
            }
            Band nb;
            switch(e.kind) {
                case Attribute.EK_INT:
                    nb = newElemBand(e, name, ab);
                    break;
                case Attribute.EK_BCI:
                    if (!e.flagTest(Attribute.EF_DELTA)) {
                        nb = ab.newIntBand(name, BCI5);
                    } else {
                        nb = ab.newIntBand(name, BRANCH5);
                    }
                    break;
                case Attribute.EK_BCO:
                    nb = ab.newIntBand(name, BRANCH5);
                    break;
                case Attribute.EK_FLAG:
                    assert (!e.flagTest(Attribute.EF_SIGN));
                    nb = newElemBand(e, name, ab);
                    break;
                case Attribute.EK_REPL:
                    assert (!e.flagTest(Attribute.EF_SIGN));
                    nb = newElemBand(e, name, ab);
                    makeNewAttributeBands(pfx, e.body, ab);
                    break;
                case Attribute.EK_UN:
                    nb = newElemBand(e, name, ab);
                    makeNewAttributeBands(pfx, e.body, ab);
                    break;
                case Attribute.EK_CASE:
                    if (!e.flagTest(Attribute.EF_BACK)) {
                        makeNewAttributeBands(pfx, e.body, ab);
                    }
                    continue;
                case Attribute.EK_REF:
                    byte refKind = e.refKind;
                    boolean nullOK = e.flagTest(Attribute.EF_NULL);
                    nb = ab.newCPRefBand(name, UNSIGNED5, refKind, nullOK);
                    break;
                case Attribute.EK_CALL:
                    continue;
                case Attribute.EK_CBLE:
                    makeNewAttributeBands(pfx, e.body, ab);
                    continue;
                default:
                    assert (false);
                    continue;
            }
            if (verbose > 1) {
                Utils.log.fine("New attribute band " + nb);
            }
        }
    }

    private Band newElemBand(Attribute.Layout.Element e, String name, MultiBand ab) {
        if (e.flagTest(Attribute.EF_SIGN)) {
            return ab.newIntBand(name, SIGNED5);
        } else if (e.len == 1) {
            return ab.newIntBand(name, BYTE1);
        } else {
            return ab.newIntBand(name, UNSIGNED5);
        }
    }

    protected int setAttributeLayoutIndex(Attribute.Layout def, int index) {
        int ctype = def.ctype;
        assert (ATTR_INDEX_OVERFLOW <= index && index < attrIndexLimit[ctype]);
        List defList = attrDefs[ctype];
        if (index == ATTR_INDEX_OVERFLOW) {
            index = defList.size();
            defList.add(def);
            if (verbose > 0)
                Utils.log.info("Adding new attribute at " + def + ": " + index);
            attrIndexTable.put(def, new Integer(index));
            return index;
        }
        if (testBit(attrDefSeen[ctype], 1L << index)) {
            throw new RuntimeException("Multiple explicit definition at " + index + ": " + def);
        }
        attrDefSeen[ctype] |= (1L << index);
        assert (0 <= index && index < attrIndexLimit[ctype]);
        if (verbose > (attrClassFileVersionMask == 0 ? 2 : 0))
            Utils.log.fine("Fixing new attribute at " + index + ": " + def + (defList.get(index) == null ? "" : "; replacing " + defList.get(index)));
        attrFlagMask[ctype] |= (1L << index);
        attrIndexTable.put(defList.get(index), null);
        defList.set(index, def);
        attrIndexTable.put(def, new Integer(index));
        return index;
    }

    private static final int[][] shortCodeLimits = { { 12, 12 }, { 8, 8 }, { 7, 7 } };

    public final int shortCodeHeader_h_limit = shortCodeLimits.length;

    static int shortCodeHeader(Code code) {
        int s = code.max_stack;
        int l0 = code.max_locals;
        int h = code.handler_class.length;
        if (h >= shortCodeLimits.length)
            return LONG_CODE_HEADER;
        int siglen = code.getMethod().getArgumentSize();
        assert (l0 >= siglen);
        if (l0 < siglen)
            return LONG_CODE_HEADER;
        int l1 = l0 - siglen;
        int lims = shortCodeLimits[h][0];
        int liml = shortCodeLimits[h][1];
        if (s >= lims || l1 >= liml)
            return LONG_CODE_HEADER;
        int sc = shortCodeHeader_h_base(h);
        sc += s + lims * l1;
        if (sc > 255)
            return LONG_CODE_HEADER;
        assert (shortCodeHeader_max_stack(sc) == s);
        assert (shortCodeHeader_max_na_locals(sc) == l1);
        assert (shortCodeHeader_handler_count(sc) == h);
        return sc;
    }

    static final int LONG_CODE_HEADER = 0;

    static int shortCodeHeader_handler_count(int sc) {
        assert (sc > 0 && sc <= 255);
        for (int h = 0; ; h++) {
            if (sc < shortCodeHeader_h_base(h + 1))
                return h;
        }
    }

    static int shortCodeHeader_max_stack(int sc) {
        int h = shortCodeHeader_handler_count(sc);
        int lims = shortCodeLimits[h][0];
        return (sc - shortCodeHeader_h_base(h)) % lims;
    }

    static int shortCodeHeader_max_na_locals(int sc) {
        int h = shortCodeHeader_handler_count(sc);
        int lims = shortCodeLimits[h][0];
        return (sc - shortCodeHeader_h_base(h)) / lims;
    }

    private static int shortCodeHeader_h_base(int h) {
        assert (h <= shortCodeLimits.length);
        int sc = 1;
        for (int h0 = 0; h0 < h; h0++) {
            int lims = shortCodeLimits[h0][0];
            int liml = shortCodeLimits[h0][1];
            sc += lims * liml;
        }
        return sc;
    }

    protected void putLabel(IntBand bc_label, Code c, int pc, int targetPC) {
        bc_label.putInt(c.encodeBCI(targetPC) - c.encodeBCI(pc));
    }

    protected int getLabel(IntBand bc_label, Code c, int pc) {
        return c.decodeBCI(bc_label.getInt() + c.encodeBCI(pc));
    }

    protected CPRefBand getCPRefOpBand(int bc) {
        switch(Instruction.getCPRefOpTag(bc)) {
            case CONSTANT_Class:
                return bc_classref;
            case CONSTANT_Fieldref:
                return bc_fieldref;
            case CONSTANT_Methodref:
                return bc_methodref;
            case CONSTANT_InterfaceMethodref:
                return bc_imethodref;
            case CONSTANT_Literal:
                switch(bc) {
                    case _ildc:
                    case _ildc_w:
                        return bc_intref;
                    case _fldc:
                    case _fldc_w:
                        return bc_floatref;
                    case _lldc2_w:
                        return bc_longref;
                    case _dldc2_w:
                        return bc_doubleref;
                    case _aldc:
                    case _aldc_w:
                        return bc_stringref;
                    case _cldc:
                    case _cldc_w:
                        return bc_classref;
                }
                break;
        }
        assert (false);
        return null;
    }

    protected CPRefBand selfOpRefBand(int self_bc) {
        assert (Instruction.isSelfLinkerOp(self_bc));
        int idx = (self_bc - _self_linker_op);
        boolean isSuper = (idx >= _self_linker_super_flag);
        if (isSuper)
            idx -= _self_linker_super_flag;
        boolean isAload = (idx >= _self_linker_aload_flag);
        if (isAload)
            idx -= _self_linker_aload_flag;
        int origBC = _first_linker_op + idx;
        boolean isField = Instruction.isFieldOp(origBC);
        if (!isSuper)
            return isField ? bc_thisfield : bc_thismethod;
        else
            return isField ? bc_superfield : bc_supermethod;
    }

    static int nextSeqForDebug;

    static File dumpDir;

    static OutputStream getDumpStream(Band b, String ext) throws IOException {
        return getDumpStream(b.name, b.seqForDebug, ext, b);
    }

    static OutputStream getDumpStream(Index ix, String ext) throws IOException {
        if (ix.size() == 0)
            return new ByteArrayOutputStream();
        int seq = ConstantPool.TAG_ORDER[ix.cpMap[0].tag];
        return getDumpStream(ix.debugName, seq, ext, ix);
    }

    static OutputStream getDumpStream(String name, int seq, String ext, Object b) throws IOException {
        if (dumpDir == null) {
            dumpDir = File.createTempFile("BD_", "", new File("."));
            dumpDir.delete();
            if (dumpDir.mkdir())
                Utils.log.info("Dumping bands to " + dumpDir);
        }
        name = name.replace('(', ' ').replace(')', ' ');
        name = name.replace('/', ' ');
        name = name.replace('*', ' ');
        name = name.trim().replace(' ', '_');
        name = ((10000 + seq) + "_" + name).substring(1);
        File dumpFile = new File(dumpDir, name + ext);
        Utils.log.info("Dumping " + b + " to " + dumpFile);
        return new BufferedOutputStream(new FileOutputStream(dumpFile));
    }

    static boolean assertCanChangeLength(Band b) {
        switch(b.phase) {
            case COLLECT_PHASE:
            case READ_PHASE:
                return true;
        }
        return false;
    }

    static boolean assertPhase(Band b, int phaseExpected) {
        if (b.phase() != phaseExpected) {
            Utils.log.warning("phase expected " + phaseExpected + " was " + b.phase() + " in " + b);
            return false;
        }
        return true;
    }

    static int verbose() {
        return Utils.currentPropMap().getInteger(Utils.DEBUG_VERBOSE);
    }

    static boolean assertPhaseChangeOK(Band b, int p0, int p1) {
        switch(p0 * 10 + p1) {
            case NO_PHASE * 10 + COLLECT_PHASE:
                assert (!b.isReader());
                assert (b.capacity() >= 0);
                assert (b.length() == 0);
                return true;
            case COLLECT_PHASE * 10 + FROZEN_PHASE:
            case FROZEN_PHASE * 10 + FROZEN_PHASE:
                assert (b.length() == 0);
                return true;
            case COLLECT_PHASE * 10 + WRITE_PHASE:
            case FROZEN_PHASE * 10 + WRITE_PHASE:
                return true;
            case WRITE_PHASE * 10 + DONE_PHASE:
                return true;
            case NO_PHASE * 10 + EXPECT_PHASE:
                assert (b.isReader());
                assert (b.capacity() < 0);
                return true;
            case EXPECT_PHASE * 10 + READ_PHASE:
                assert (Math.max(0, b.capacity()) >= b.valuesExpected());
                assert (b.length() <= 0);
                return true;
            case READ_PHASE * 10 + DISBURSE_PHASE:
                assert (b.valuesRemainingForDebug() == b.length());
                return true;
            case DISBURSE_PHASE * 10 + DONE_PHASE:
                assert (assertDoneDisbursing(b));
                return true;
        }
        if (p0 == p1)
            Utils.log.warning("Already in phase " + p0);
        else
            Utils.log.warning("Unexpected phase " + p0 + " -> " + p1);
        return false;
    }

    static private boolean assertDoneDisbursing(Band b) {
        if (b.phase != DISBURSE_PHASE) {
            Utils.log.warning("assertDoneDisbursing: still in phase " + b.phase + ": " + b);
            if (verbose() <= 1)
                return false;
        }
        int left = b.valuesRemainingForDebug();
        if (left > 0) {
            Utils.log.warning("assertDoneDisbursing: " + left + " values left in " + b);
            if (verbose() <= 1)
                return false;
        }
        if (b instanceof MultiBand) {
            MultiBand mb = (MultiBand) b;
            for (int i = 0; i < mb.bandCount; i++) {
                Band sub = mb.bands[i];
                if (sub.phase != DONE_PHASE) {
                    Utils.log.warning("assertDoneDisbursing: sub-band still in phase " + sub.phase + ": " + sub);
                    if (verbose() <= 1)
                        return false;
                }
            }
        }
        return true;
    }

    static private void printCDecl(Band b) {
        if (b instanceof MultiBand) {
            MultiBand mb = (MultiBand) b;
            for (int i = 0; i < mb.bandCount; i++) {
                printCDecl(mb.bands[i]);
            }
            return;
        }
        String ixS = "NULL";
        if (b instanceof CPRefBand) {
            Index ix = ((CPRefBand) b).index;
            if (ix != null)
                ixS = "INDEX(" + ix.debugName + ")";
        }
        Coding[] knownc = { BYTE1, CHAR3, BCI5, BRANCH5, UNSIGNED5, UDELTA5, SIGNED5, DELTA5, MDELTA5 };
        String[] knowns = { "BYTE1", "CHAR3", "BCI5", "BRANCH5", "UNSIGNED5", "UDELTA5", "SIGNED5", "DELTA5", "MDELTA5" };
        Coding rc = b.regularCoding;
        int rci = Arrays.asList(knownc).indexOf(rc);
        String cstr;
        if (rci >= 0)
            cstr = knowns[rci];
        else
            cstr = "CODING" + rc.keyString();
        System.out.println("  BAND_INIT(\"" + b.name() + "\"" + ", " + cstr + ", " + ixS + "),");
    }

    private HashMap prevForAssertMap;

    boolean notePrevForAssert(Band b, Band p) {
        if (prevForAssertMap == null)
            prevForAssertMap = new HashMap();
        prevForAssertMap.put(b, p);
        return true;
    }

    private boolean assertReadyToReadFrom(Band b, InputStream in) throws IOException {
        Band p = (Band) prevForAssertMap.get(b);
        if (p != null && phaseCmp(p.phase(), DISBURSE_PHASE) < 0) {
            Utils.log.warning("Previous band not done reading.");
            Utils.log.info("    Previous band: " + p);
            Utils.log.info("        Next band: " + b);
            Thread.dumpStack();
            assert (verbose > 0);
        }
        String name = b.name;
        if (optDebugBands && !name.startsWith("(")) {
            StringBuffer buf = new StringBuffer();
            int ch;
            while ((ch = in.read()) > 0) buf.append((char) ch);
            String inName = buf.toString();
            if (!inName.equals(name)) {
                StringBuffer sb = new StringBuffer();
                sb.append("Expected " + name + " but read: ");
                inName += (char) ch;
                while (inName.length() < 10) inName += (char) in.read();
                for (int i = 0; i < inName.length(); i++) sb.append(inName.charAt(i));
                Utils.log.warning(sb.toString());
                return false;
            }
        }
        return true;
    }

    private boolean assertValidCPRefs(CPRefBand b) {
        if (b.index == null)
            return true;
        int limit = b.index.size() + 1;
        for (int i = 0; i < b.length(); i++) {
            int v = b.valueAtForDebug(i);
            if (v < 0 || v >= limit) {
                Utils.log.warning("CP ref out of range " + "[" + i + "] = " + v + " in " + b);
                return false;
            }
        }
        return true;
    }

    private boolean assertReadyToWriteTo(Band b, OutputStream out) throws IOException {
        Band p = (Band) prevForAssertMap.get(b);
        if (p != null && phaseCmp(p.phase(), DONE_PHASE) < 0) {
            Utils.log.warning("Previous band not done writing.");
            Utils.log.info("    Previous band: " + p);
            Utils.log.info("        Next band: " + b);
            Thread.dumpStack();
            assert (verbose > 0);
        }
        String name = b.name;
        if (optDebugBands && !name.startsWith("(")) {
            for (int j = 0; j < name.length(); j++) {
                out.write((byte) name.charAt(j));
            }
            out.write((byte) 0);
        }
        return true;
    }

    protected static boolean testBit(int flags, int bitMask) {
        return (flags & bitMask) != 0;
    }

    protected static int setBit(int flags, int bitMask, boolean z) {
        return z ? (flags | bitMask) : (flags & ~bitMask);
    }

    protected static boolean testBit(long flags, long bitMask) {
        return (flags & bitMask) != 0;
    }

    protected static long setBit(long flags, long bitMask, boolean z) {
        return z ? (flags | bitMask) : (flags & ~bitMask);
    }

    static void printArrayTo(PrintStream ps, int[] values, int start, int end) {
        int len = end - start;
        for (int i = 0; i < len; i++) {
            if (i % 10 == 0)
                ps.println();
            else
                ps.print(" ");
            ps.print(values[start + i]);
        }
        ps.println();
    }

    static void printArrayTo(PrintStream ps, Entry[] cpMap, int start, int end) {
        StringBuffer buf = new StringBuffer();
        int len = end - start;
        for (int i = 0; i < len; i++) {
            String s = cpMap[start + i].stringValue();
            buf.setLength(0);
            for (int j = 0; j < s.length(); j++) {
                char ch = s.charAt(j);
                if (!(ch < ' ' || ch > '~' || ch == '\\')) {
                    buf.append(ch);
                } else if (ch == '\n') {
                    buf.append("\\n");
                } else if (ch == '\t') {
                    buf.append("\\t");
                } else if (ch == '\r') {
                    buf.append("\\r");
                } else {
                    buf.append("\\x" + Integer.toHexString(ch));
                }
            }
            ps.println(buf);
        }
    }

    protected static Object[] realloc(Object[] a, int len) {
        java.lang.Class elt = a.getClass().getComponentType();
        Object[] na = (Object[]) java.lang.reflect.Array.newInstance(elt, len);
        System.arraycopy(a, 0, na, 0, Math.min(a.length, len));
        return na;
    }

    protected static Object[] realloc(Object[] a) {
        return realloc(a, Math.max(10, a.length * 2));
    }

    static private int[] noInts = {};

    protected static int[] realloc(int[] a, int len) {
        if (len == 0)
            return noInts;
        if (a == null)
            return new int[len];
        int[] na = new int[len];
        System.arraycopy(a, 0, na, 0, Math.min(a.length, len));
        return na;
    }

    protected static int[] realloc(int[] a) {
        return realloc(a, Math.max(10, a.length * 2));
    }

    static private byte[] noBytes = {};

    protected static byte[] realloc(byte[] a, int len) {
        if (len == 0)
            return noBytes;
        if (a == null)
            return new byte[len];
        byte[] na = new byte[len];
        System.arraycopy(a, 0, na, 0, Math.min(a.length, len));
        return na;
    }

    protected static byte[] realloc(byte[] a) {
        return realloc(a, Math.max(10, a.length * 2));
    }
}