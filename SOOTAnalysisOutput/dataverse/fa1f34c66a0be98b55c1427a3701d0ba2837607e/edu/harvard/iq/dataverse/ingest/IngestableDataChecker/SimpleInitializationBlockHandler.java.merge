package edu.harvard.iq.dataverse.ingest;

import static java.lang.System.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.regex.*;
import java.util.zip.*;
import java.util.logging.*;
import org.apache.commons.lang.builder.*;

public class IngestableDataChecker implements java.io.Serializable {

    private static Logger dbgLog = Logger.getLogger(IngestableDataChecker.class.getPackage().getName());

    private static String[] defaultFormatSet = { "POR", "SAV", "DTA", "RDA", "XPT" };

    private String[] testFormatSet;

    private static Map<Byte, String> stataReleaseNumber = new HashMap<Byte, String>();

    public static String STATA_13_HEADER = "<stata_dta><header><release>117</release>";

    public static String STATA_14_HEADER = "<stata_dta><header><release>118</release>";

    public static String STATA_15_HEADER = "<stata_dta><header><release>119</release>";

    private static Set<String> readableFileTypes = new HashSet<String>();

    private static Map<String, Method> testMethods = new HashMap<String, Method>();

    public static String SAS_XPT_HEADER_80 = "HEADER RECORD*******LIBRARY HEADER RECORD!!!!!!!000000000000000000000000000000  ";

    public static String SAS_XPT_HEADER_11 = "SAS     SAS";

    public static int POR_MARK_POSITION_DEFAULT = 461;

    public static String POR_MARK = "SPSSPORT";

    private static int DEFAULT_BUFFER_SIZE = 500;

    private static String regex = "^test(\\w+)format$";

    private static String rdargx = "^(52)(44)(41|42|58)(31|32)(0A)$";

    private static int RDA_HEADER_SIZE = 5;

    private static Pattern ptn;

    static {
        stataReleaseNumber.put((byte) 104, "rel_3");
        stataReleaseNumber.put((byte) 105, "rel_4or5");
        stataReleaseNumber.put((byte) 108, "rel_6");
        stataReleaseNumber.put((byte) 110, "rel_7first");
        stataReleaseNumber.put((byte) 111, "rel_7scnd");
        stataReleaseNumber.put((byte) 113, "rel_8_or_9");
        stataReleaseNumber.put((byte) 114, "rel_10");
        stataReleaseNumber.put((byte) 115, "rel_12");
        stataReleaseNumber.put((byte) 117, "rel_13");
        readableFileTypes.add("application/x-stata");
        readableFileTypes.add("application/x-spss-sav");
        readableFileTypes.add("application/x-spss-por");
        readableFileTypes.add("application/x-rlang-transport");
        readableFileTypes.add("application/x-stata-13");
        readableFileTypes.add("application/x-stata-14");
        readableFileTypes.add("application/x-stata-15");
        Pattern p = Pattern.compile(regex);
        ptn = Pattern.compile(rdargx);
        for (Method m : IngestableDataChecker.class.getDeclaredMethods()) {
            Matcher mtr = p.matcher(m.getName());
            if (mtr.matches()) {
                testMethods.put(mtr.group(1), m);
            }
        }
    }

    private boolean windowsNewLine = true;

    public IngestableDataChecker() {
        this.testFormatSet = defaultFormatSet;
    }

    public IngestableDataChecker(String[] requestedFormatSet) {
        this.testFormatSet = requestedFormatSet;
        dbgLog.fine("SubsettableFileChecker instance=" + this.toString());
    }

    public static String[] getDefaultTestFormatSet() {
        return defaultFormatSet;
    }

    public static void printUsage() {
        out.println("Usage : java subsettableFileChecker <datafileName>");
    }

    public String[] getTestFormatSet() {
        return this.testFormatSet;
    }

    public String testSAVformat(MappedByteBuffer buff) {
        String result = null;
        buff.rewind();
        boolean DEBUG = false;
        if (buff.capacity() < 4) {
            return null;
        }
        if (DEBUG) {
            out.println("applying the sav test\n");
        }
        byte[] hdr4 = new byte[4];
        buff.get(hdr4, 0, 4);
        String hdr4sav = new String(hdr4);
        if (DEBUG) {
            out.println("from string=" + hdr4sav);
        }
        if (hdr4sav.equals("$FL2")) {
            if (DEBUG) {
                out.println("this file is spss-sav type");
            }
            result = "application/x-spss-sav";
        } else {
            if (DEBUG) {
                out.println("this file is NOT spss-sav type");
            }
        }
        return result;
    }

    public String testDTAformat(MappedByteBuffer buff) {
        String result = null;
        buff.rewind();
        boolean DEBUG = false;
        if (DEBUG) {
            dbgLog.info("applying the dta test\n");
        }
        if (buff.capacity() < 4) {
            return result;
        }
        byte[] hdr4 = new byte[4];
        buff.get(hdr4, 0, 4);
        if (DEBUG) {
            for (int i = 0; i < hdr4.length; ++i) {
                dbgLog.info(String.format("%d\t%02X\n", i, hdr4[i]));
            }
        }
        if (hdr4[2] != 1) {
            if (DEBUG) {
                dbgLog.info("3rd byte is not 1: given file is not stata-dta type");
            }
        } else if ((hdr4[1] != 1) && (hdr4[1] != 2)) {
            if (DEBUG) {
                dbgLog.info("2nd byte is neither 0 nor 1: this file is not stata-dta type");
            }
        } else if (!IngestableDataChecker.stataReleaseNumber.containsKey(hdr4[0])) {
            if (DEBUG) {
                dbgLog.info("1st byte (" + hdr4[0] + ") is not within the ingestable range [rel. 3-10]: this file is NOT stata-dta type");
            }
        } else {
            if (DEBUG) {
                dbgLog.info("this file is stata-dta type: " + IngestableDataChecker.stataReleaseNumber.get(hdr4[0]) + "(No in HEX=" + hdr4[0] + ")");
            }
            result = "application/x-stata";
        }
        if ((result == null) && (buff.capacity() >= STATA_13_HEADER.length())) {
            buff.rewind();
            byte[] headerBuffer = null;
            String headerString = null;
            try {
                headerBuffer = new byte[STATA_13_HEADER.length()];
                buff.get(headerBuffer, 0, STATA_13_HEADER.length());
                headerString = new String(headerBuffer, "US-ASCII");
            } catch (Exception ex) {
            }
            if (STATA_13_HEADER.equals(headerString)) {
                result = "application/x-stata-13";
            }
        }
        if ((result == null) && (buff.capacity() >= STATA_14_HEADER.length())) {
            buff.rewind();
            byte[] headerBuffer = null;
            String headerString = null;
            try {
                headerBuffer = new byte[STATA_14_HEADER.length()];
                buff.get(headerBuffer, 0, STATA_14_HEADER.length());
                headerString = new String(headerBuffer, "US-ASCII");
            } catch (Exception ex) {
            }
            if (STATA_14_HEADER.equals(headerString)) {
                result = "application/x-stata-14";
            }
        }
        if ((result == null) && (buff.capacity() >= STATA_15_HEADER.length())) {
            buff.rewind();
            byte[] headerBuffer = null;
            String headerString = null;
            try {
                headerBuffer = new byte[STATA_15_HEADER.length()];
                buff.get(headerBuffer, 0, STATA_15_HEADER.length());
                headerString = new String(headerBuffer, "US-ASCII");
            } catch (Exception ex) {
            }
            if (STATA_15_HEADER.equals(headerString)) {
                result = "application/x-stata-15";
            }
        }
        return result;
    }

    public String testXPTformat(MappedByteBuffer buff) {
        String result = null;
        buff.rewind();
        boolean DEBUG = false;
        if (DEBUG) {
            out.println("applying the sas-transport test\n");
        }
        if (buff.capacity() < 91) {
            if (DEBUG) {
                out.println("this file is NOT sas-exort type\n");
            }
            return result;
        }
        byte[] hdr1 = new byte[80];
        byte[] hdr2 = new byte[11];
        buff.get(hdr1, 0, 80);
        buff.get(hdr2, 0, 11);
        String hdr1st80 = new String(hdr1);
        String hdrnxt11 = new String(hdr2);
        if (DEBUG) {
            out.println("1st-80  bytes=" + hdr1st80);
            out.println("next-11 bytes=" + hdrnxt11);
        }
        if ((hdr1st80.equals(IngestableDataChecker.SAS_XPT_HEADER_80)) && (hdrnxt11.equals(IngestableDataChecker.SAS_XPT_HEADER_11))) {
            if (DEBUG) {
                out.println("this file is sas-export type\n");
            }
            result = "application/x-sas-xport";
        } else {
            if (DEBUG) {
                out.println("this file is NOT sas-exort type\n");
            }
        }
        return result;
    }

    public String testPORformat(MappedByteBuffer buff) {
        String result = null;
        buff.rewind();
        boolean DEBUG = false;
        if (DEBUG) {
            out.println("applying the spss-por test\n");
        }
        int bufferCapacity = buff.capacity();
        dbgLog.fine("Subsettable Checker: buffer capacity: " + bufferCapacity);
        if (bufferCapacity < 491) {
            if (DEBUG) {
                out.println("this file is NOT spss-por type\n");
            }
            return result;
        }
        buff.rewind();
        byte[] nlch = new byte[36];
        int pos1;
        int pos2;
        int pos3;
        int ucase = 0;
        int wcase = 0;
        int mcase = 0;
        int three = 0;
        int nolines = 6;
        int nocols = 80;
        for (int i = 0; i < nolines; ++i) {
            int baseBias = nocols * (i + 1);
            pos1 = baseBias + i;
            if (pos1 > bufferCapacity - 1) {
                dbgLog.fine("Subsettable Checker: request to go beyond buffer capacity (" + pos1 + ")");
                return result;
            }
            buff.position(pos1);
            if (DEBUG) {
                out.println("\tposition(1)=" + buff.position());
            }
            int j = 6 * i;
            nlch[j] = buff.get();
            if (nlch[j] == 10) {
                ucase++;
            } else if (nlch[j] == 13) {
                mcase++;
            }
            pos2 = baseBias + 2 * i;
            if (pos2 > bufferCapacity - 2) {
                dbgLog.fine("Subsettable Checker: request to read 2 bytes beyond buffer capacity (" + pos2 + ")");
                return result;
            }
            buff.position(pos2);
            if (DEBUG) {
                out.println("\tposition(2)=" + buff.position());
            }
            nlch[j + 1] = buff.get();
            nlch[j + 2] = buff.get();
            pos3 = baseBias + 3 * i;
            if (pos3 > bufferCapacity - 3) {
                dbgLog.fine("Subsettable Checker: request to read 3 bytes beyond buffer capacity (" + pos3 + ")");
                return result;
            }
            buff.position(pos3);
            if (DEBUG) {
                out.println("\tposition(3)=" + buff.position());
            }
            nlch[j + 3] = buff.get();
            nlch[j + 4] = buff.get();
            nlch[j + 5] = buff.get();
            if (DEBUG) {
                out.println(i + "-th iteration position =" + nlch[j] + "\t" + nlch[j + 1] + "\t" + nlch[j + 2]);
                out.println(i + "-th iteration position =" + nlch[j + 3] + "\t" + nlch[j + 4] + "\t" + nlch[j + 5]);
            }
            if ((nlch[j + 3] == 13) && (nlch[j + 4] == 13) && (nlch[j + 5] == 10)) {
                three++;
            } else if ((nlch[j + 1] == 13) && (nlch[j + 2] == 10)) {
                wcase++;
            }
            buff.rewind();
        }
        if (three == nolines) {
            if (DEBUG) {
                out.println("0D0D0A case");
            }
            windowsNewLine = false;
        } else if ((ucase == nolines) && (wcase < nolines)) {
            if (DEBUG) {
                out.println("0A case");
            }
            windowsNewLine = false;
        } else if ((ucase < nolines) && (wcase == nolines)) {
            if (DEBUG) {
                out.println("0D0A case");
            }
        } else if ((mcase == nolines) && (wcase < nolines)) {
            if (DEBUG) {
                out.println("0D case");
            }
            windowsNewLine = false;
        }
        buff.rewind();
        int PORmarkPosition = POR_MARK_POSITION_DEFAULT;
        if (windowsNewLine) {
            PORmarkPosition = PORmarkPosition + 5;
        } else if (three == nolines) {
            PORmarkPosition = PORmarkPosition + 10;
        }
        byte[] pormark = new byte[8];
        buff.position(PORmarkPosition);
        buff.get(pormark, 0, 8);
        String pormarks = new String(pormark);
        if (DEBUG) {
            out.println("pormark =>" + pormarks + "<-");
        }
        if (pormarks.equals(POR_MARK)) {
            if (DEBUG) {
                out.println("this file is spss-por type");
            }
            result = "application/x-spss-por";
        } else {
            if (DEBUG) {
                out.println("this file is NOT spss-por type");
            }
        }
        return result;
    }

    public String testRDAformat(MappedByteBuffer buff) {
        String result = null;
        buff.rewind();
        if (buff.capacity() < 4) {
            return null;
        }
        boolean DEBUG = false;
        if (DEBUG) {
            out.println("applying the RData test\n");
            out.println("buffer capacity=" + buff.capacity());
        }
        if (DEBUG) {
            byte[] rawhdr = new byte[4];
            buff.get(rawhdr, 0, 4);
            for (int j = 0; j < 4; j++) {
                out.printf("%02X ", rawhdr[j]);
            }
            out.println();
            buff.rewind();
        }
        int magicNumber = buff.getInt();
        if (DEBUG) {
            out.println("magicNumber in decimal =" + magicNumber);
            out.println("in binary=" + Integer.toBinaryString(magicNumber));
            out.println("in oct=" + Integer.toOctalString(magicNumber));
            out.println("in hex=" + Integer.toHexString(magicNumber));
        }
        try {
            if (magicNumber == 0x1F8B0800) {
                if (DEBUG) {
                    out.println("magicNumber is GZIP");
                }
                int gzip_buffer_size = this.getGzipBufferSize(buff);
                byte[] hdr = new byte[gzip_buffer_size];
                buff.get(hdr, 0, gzip_buffer_size);
                GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(hdr));
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < RDA_HEADER_SIZE; i++) {
                    sb.append(String.format("%02X", gzin.read()));
                }
                String fisrt5bytes = sb.toString();
                result = this.checkUncompressedFirst5bytes(fisrt5bytes);
            } else {
                if (DEBUG) {
                    out.println("magicNumber is not GZIP:" + magicNumber);
                    out.println("test as an uncompressed RData file");
                }
                buff.rewind();
                byte[] uchdr = new byte[5];
                buff.get(uchdr, 0, 5);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < uchdr.length; i++) {
                    sb.append(String.format("%02X", uchdr[i]));
                }
                String fisrt5bytes = sb.toString();
                result = this.checkUncompressedFirst5bytes(fisrt5bytes);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public String detectTabularDataFormat(File fh) {
        boolean DEBUG = false;
        String readableFormatType = null;
        try {
            int buffer_size = this.getBufferSize(fh);
            dbgLog.fine("buffer_size: " + buffer_size);
            FileChannel srcChannel = new FileInputStream(fh).getChannel();
            MappedByteBuffer buff = srcChannel.map(FileChannel.MapMode.READ_ONLY, 0, buffer_size);
            buff.rewind();
            dbgLog.fine("before the for loop");
            for (String fmt : this.getTestFormatSet()) {
                Method mthd = testMethods.get(fmt);
                try {
                    Object retobj = mthd.invoke(this, buff);
                    String result = (String) retobj;
                    if (result != null) {
                        dbgLog.fine("result for (" + fmt + ")=" + result);
                        if (DEBUG) {
                            out.println("result for (" + fmt + ")=" + result);
                        }
                        if (readableFileTypes.contains(result)) {
                            readableFormatType = result;
                        }
                        dbgLog.fine("readableFormatType=" + readableFormatType);
                        return readableFormatType;
                    } else {
                        dbgLog.fine("null was returned for " + fmt + " test");
                        if (DEBUG) {
                            out.println("null was returned for " + fmt + " test");
                        }
                    }
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause.getMessage() != null) {
                        err.format(cause.getMessage());
                        e.printStackTrace();
                    } else {
                        dbgLog.info("cause.getMessage() was null for " + e);
                        e.printStackTrace();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (BufferUnderflowException e) {
                    dbgLog.info("BufferUnderflowException " + e);
                    e.printStackTrace();
                }
            }
            return readableFormatType;
        } catch (FileNotFoundException fe) {
            dbgLog.fine("exception detected: file was not foud");
            fe.printStackTrace();
        } catch (IOException ie) {
            dbgLog.fine("other io exception detected");
            ie.printStackTrace();
        }
        return readableFormatType;
    }

    private String checkUncompressedFirst5bytes(String fisrt5bytes) {
        boolean DEBUG = false;
        String result = null;
        if (DEBUG) {
            out.println("first5bytes=" + fisrt5bytes);
        }
        Matcher mtr = ptn.matcher(fisrt5bytes);
        if (mtr.matches()) {
            if (DEBUG) {
                out.println("RDATA type");
            }
            result = "application/x-rlang-transport";
        } else {
            if (DEBUG) {
                out.println("not binary RDATA type");
            }
        }
        return result;
    }

    private int getBufferSize(File fh) {
        boolean DEBUG = false;
        int BUFFER_SIZE = DEFAULT_BUFFER_SIZE;
        if (fh.length() < DEFAULT_BUFFER_SIZE) {
            BUFFER_SIZE = (int) fh.length();
            if (DEBUG) {
                out.println("non-default buffer_size: new size=" + BUFFER_SIZE);
            }
        }
        return BUFFER_SIZE;
    }

    private int getGzipBufferSize(MappedByteBuffer buff) {
        int GZIP_BUFFER_SIZE = 120;
        if (buff.capacity() < GZIP_BUFFER_SIZE) {
            GZIP_BUFFER_SIZE = buff.capacity();
        }
        buff.rewind();
        return GZIP_BUFFER_SIZE;
    }

    public void printHexDump(MappedByteBuffer buff, String hdr) {
        int counter = 0;
        if (hdr != null) {
            out.println(hdr);
        }
        for (int i = 0; i < buff.capacity(); i++) {
            counter = i + 1;
            out.print(String.format("%02X ", buff.get()));
            if (counter % 16 == 0) {
                out.println();
            } else {
                if (counter % 8 == 0) {
                    out.print(" ");
                }
            }
        }
        out.println();
        buff.rewind();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}