package com.sun.org.apache.xml.internal.security.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class Base64 {

    public static final int BASE64DEFAULTLENGTH = 76;

    private Base64() {
    }

    static final byte[] getBytes(BigInteger big, int bitlen) {
        bitlen = ((bitlen + 7) >> 3) << 3;
        if (bitlen < big.bitLength()) {
            throw new IllegalArgumentException(I18n.translate("utils.Base64.IllegalBitlength"));
        }
        byte[] bigBytes = big.toByteArray();
        if (((big.bitLength() % 8) != 0) && (((big.bitLength() / 8) + 1) == (bitlen / 8))) {
            return bigBytes;
        }
        int startSrc = 0;
        int bigLen = bigBytes.length;
        if ((big.bitLength() % 8) == 0) {
            startSrc = 1;
            bigLen--;
        }
        int startDst = bitlen / 8 - bigLen;
        byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, bigLen);
        return resizedBytes;
    }

    public static final String encode(BigInteger big) {
        return encode(getBytes(big, big.bitLength()));
    }

    public static final byte[] encode(BigInteger big, int bitlen) {
        bitlen = ((bitlen + 7) >> 3) << 3;
        if (bitlen < big.bitLength()) {
            throw new IllegalArgumentException(I18n.translate("utils.Base64.IllegalBitlength"));
        }
        byte[] bigBytes = big.toByteArray();
        if (((big.bitLength() % 8) != 0) && (((big.bitLength() / 8) + 1) == (bitlen / 8))) {
            return bigBytes;
        }
        int startSrc = 0;
        int bigLen = bigBytes.length;
        if ((big.bitLength() % 8) == 0) {
            startSrc = 1;
            bigLen--;
        }
        int startDst = bitlen / 8 - bigLen;
        byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, bigLen);
        return resizedBytes;
    }

    public static final BigInteger decodeBigIntegerFromElement(Element element) throws Base64DecodingException {
        return new BigInteger(1, Base64.decode(element));
    }

    public static final BigInteger decodeBigIntegerFromText(Text text) throws Base64DecodingException {
        return new BigInteger(1, Base64.decode(text.getData()));
    }

    public static final void fillElementWithBigInteger(Element element, BigInteger biginteger) {
        String encodedInt = encode(biginteger);
        if (encodedInt.length() > 76) {
            encodedInt = "\n" + encodedInt + "\n";
        }
        Document doc = element.getOwnerDocument();
        Text text = doc.createTextNode(encodedInt);
        element.appendChild(text);
    }

    public static final byte[] decode(Element element) throws Base64DecodingException {
        Node sibling = element.getFirstChild();
        StringBuffer sb = new StringBuffer();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.TEXT_NODE) {
                Text t = (Text) sibling;
                sb.append(t.getData());
            }
            sibling = sibling.getNextSibling();
        }
        return decode(sb.toString());
    }

    public static final Element encodeToElement(Document doc, String localName, byte[] bytes) {
        Element el = XMLUtils.createElementInSignatureSpace(doc, localName);
        Text text = doc.createTextNode(encode(bytes));
        el.appendChild(text);
        return el;
    }

    public final static byte[] decode(byte[] base64) throws Base64DecodingException {
        return decodeInternal(base64, -1);
    }

    public static final String encode(byte[] binaryData) {
        return XMLUtils.ignoreLineBreaks() ? encode(binaryData, Integer.MAX_VALUE) : encode(binaryData, BASE64DEFAULTLENGTH);
    }

    public final static byte[] decode(BufferedReader reader) throws IOException, Base64DecodingException {
        UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
        String line;
        while (null != (line = reader.readLine())) {
            byte[] bytes = decode(line);
            baos.write(bytes);
        }
        return baos.toByteArray();
    }

    static private final int BASELENGTH = 255;

    static private final int LOOKUPLENGTH = 64;

    static private final int TWENTYFOURBITGROUP = 24;

    static private final int EIGHTBIT = 8;

    static private final int SIXTEENBIT = 16;

    static private final int FOURBYTE = 4;

    static private final int SIGN = -128;

    static private final char PAD = '=';

    static final private byte[] base64Alphabet = new byte[BASELENGTH];

    static final private char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

    static {
        for (int i = 0; i < BASELENGTH; i++) {
            base64Alphabet[i] = -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            base64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            base64Alphabet[i] = (byte) (i - 'a' + 26);
        }
        for (int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i - '0' + 52);
        }
        base64Alphabet['+'] = 62;
        base64Alphabet['/'] = 63;
        for (int i = 0; i <= 25; i++) lookUpBase64Alphabet[i] = (char) ('A' + i);
        for (int i = 26, j = 0; i <= 51; i++, j++) lookUpBase64Alphabet[i] = (char) ('a' + j);
        for (int i = 52, j = 0; i <= 61; i++, j++) lookUpBase64Alphabet[i] = (char) ('0' + j);
        lookUpBase64Alphabet[62] = '+';
        lookUpBase64Alphabet[63] = '/';
    }

    protected static final boolean isWhiteSpace(byte octect) {
        return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
    }

    protected static final boolean isPad(byte octect) {
        return (octect == PAD);
    }

    public static final String encode(byte[] binaryData, int length) {
        if (length < 4) {
            length = Integer.MAX_VALUE;
        }
        if (binaryData == null)
            return null;
        int lengthDataBits = binaryData.length * EIGHTBIT;
        if (lengthDataBits == 0) {
            return "";
        }
        int fewerThan24bits = lengthDataBits % TWENTYFOURBITGROUP;
        int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
        int numberQuartet = fewerThan24bits != 0 ? numberTriplets + 1 : numberTriplets;
        int quartesPerLine = length / 4;
        int numberLines = (numberQuartet - 1) / quartesPerLine;
        char[] encodedData = null;
        encodedData = new char[numberQuartet * 4 + numberLines];
        byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;
        int encodedIndex = 0;
        int dataIndex = 0;
        int i = 0;
        for (int line = 0; line < numberLines; line++) {
            for (int quartet = 0; quartet < 19; quartet++) {
                b1 = binaryData[dataIndex++];
                b2 = binaryData[dataIndex++];
                b3 = binaryData[dataIndex++];
                l = (byte) (b2 & 0x0f);
                k = (byte) (b1 & 0x03);
                byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
                byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
                byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
                encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
                i++;
            }
            encodedData[encodedIndex++] = 0xa;
        }
        for (; i < numberTriplets; i++) {
            b1 = binaryData[dataIndex++];
            b2 = binaryData[dataIndex++];
            b3 = binaryData[dataIndex++];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
            byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6) : (byte) ((b3) >> 6 ^ 0xfc);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[(l << 2) | val3];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[b3 & 0x3f];
        }
        if (fewerThan24bits == EIGHTBIT) {
            b1 = binaryData[dataIndex];
            k = (byte) (b1 & 0x03);
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[k << 4];
            encodedData[encodedIndex++] = PAD;
            encodedData[encodedIndex++] = PAD;
        } else if (fewerThan24bits == SIXTEENBIT) {
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);
            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2) : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4) : (byte) ((b2) >> 4 ^ 0xf0);
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val1];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[val2 | (k << 4)];
            encodedData[encodedIndex++] = lookUpBase64Alphabet[l << 2];
            encodedData[encodedIndex++] = PAD;
        }
        return new String(encodedData);
    }

    public final static byte[] decode(String encoded) throws Base64DecodingException {
        if (encoded == null)
            return null;
        byte[] bytes = new byte[encoded.length()];
        int len = getBytesInternal(encoded, bytes);
        return decodeInternal(bytes, len);
    }

    protected static final int getBytesInternal(String s, byte[] result) {
        int length = s.length();
        int newSize = 0;
        for (int i = 0; i < length; i++) {
            byte dataS = (byte) s.charAt(i);
            if (!isWhiteSpace(dataS))
                result[newSize++] = dataS;
        }
        return newSize;
    }

    protected final static byte[] decodeInternal(byte[] base64Data, int len) throws Base64DecodingException {
        if (len == -1)
            len = removeWhiteSpace(base64Data);
        if (len % FOURBYTE != 0) {
            throw new Base64DecodingException("decoding.divisible.four");
        }
        int numberQuadruple = (len / FOURBYTE);
        if (numberQuadruple == 0)
            return new byte[0];
        byte[] decodedData = null;
        byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
        int i = 0;
        int encodedIndex = 0;
        int dataIndex = 0;
        dataIndex = (numberQuadruple - 1) * 4;
        encodedIndex = (numberQuadruple - 1) * 3;
        b1 = base64Alphabet[base64Data[dataIndex++]];
        b2 = base64Alphabet[base64Data[dataIndex++]];
        if ((b1 == -1) || (b2 == -1)) {
            throw new Base64DecodingException("decoding.general");
        }
        byte d3, d4;
        b3 = base64Alphabet[d3 = base64Data[dataIndex++]];
        b4 = base64Alphabet[d4 = base64Data[dataIndex++]];
        if ((b3 == -1) || (b4 == -1)) {
            if (isPad(d3) && isPad(d4)) {
                if ((b2 & 0xf) != 0)
                    throw new Base64DecodingException("decoding.general");
                decodedData = new byte[encodedIndex + 1];
                decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            } else if (!isPad(d3) && isPad(d4)) {
                if ((b3 & 0x3) != 0)
                    throw new Base64DecodingException("decoding.general");
                decodedData = new byte[encodedIndex + 2];
                decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
                decodedData[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
            } else {
                throw new Base64DecodingException("decoding.general");
            }
        } else {
            decodedData = new byte[encodedIndex + 3];
            decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
            decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
            decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
        }
        encodedIndex = 0;
        dataIndex = 0;
        for (i = numberQuadruple - 1; i > 0; i--) {
            b1 = base64Alphabet[base64Data[dataIndex++]];
            b2 = base64Alphabet[base64Data[dataIndex++]];
            b3 = base64Alphabet[base64Data[dataIndex++]];
            b4 = base64Alphabet[base64Data[dataIndex++]];
            if ((b1 == -1) || (b2 == -1) || (b3 == -1) || (b4 == -1)) {
                throw new Base64DecodingException("decoding.general");
            }
            decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
            decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
            decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
        }
        return decodedData;
    }

    public final static void decode(String base64Data, OutputStream os) throws Base64DecodingException, IOException {
        byte[] bytes = new byte[base64Data.length()];
        int len = getBytesInternal(base64Data, bytes);
        decode(bytes, os, len);
    }

    public final static void decode(byte[] base64Data, OutputStream os) throws Base64DecodingException, IOException {
        decode(base64Data, os, -1);
    }

    protected final static void decode(byte[] base64Data, OutputStream os, int len) throws Base64DecodingException, IOException {
        if (len == -1)
            len = removeWhiteSpace(base64Data);
        if (len % FOURBYTE != 0) {
            throw new Base64DecodingException("decoding.divisible.four");
        }
        int numberQuadruple = (len / FOURBYTE);
        if (numberQuadruple == 0)
            return;
        byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
        int i = 0;
        int dataIndex = 0;
        for (i = numberQuadruple - 1; i > 0; i--) {
            b1 = base64Alphabet[base64Data[dataIndex++]];
            b2 = base64Alphabet[base64Data[dataIndex++]];
            b3 = base64Alphabet[base64Data[dataIndex++]];
            b4 = base64Alphabet[base64Data[dataIndex++]];
            if ((b1 == -1) || (b2 == -1) || (b3 == -1) || (b4 == -1))
                throw new Base64DecodingException("decoding.general");
            os.write((byte) (b1 << 2 | b2 >> 4));
            os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            os.write((byte) (b3 << 6 | b4));
        }
        b1 = base64Alphabet[base64Data[dataIndex++]];
        b2 = base64Alphabet[base64Data[dataIndex++]];
        if ((b1 == -1) || (b2 == -1)) {
            throw new Base64DecodingException("decoding.general");
        }
        byte d3, d4;
        b3 = base64Alphabet[d3 = base64Data[dataIndex++]];
        b4 = base64Alphabet[d4 = base64Data[dataIndex++]];
        if ((b3 == -1) || (b4 == -1)) {
            if (isPad(d3) && isPad(d4)) {
                if ((b2 & 0xf) != 0)
                    throw new Base64DecodingException("decoding.general");
                os.write((byte) (b1 << 2 | b2 >> 4));
            } else if (!isPad(d3) && isPad(d4)) {
                if ((b3 & 0x3) != 0)
                    throw new Base64DecodingException("decoding.general");
                os.write((byte) (b1 << 2 | b2 >> 4));
                os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            } else {
                throw new Base64DecodingException("decoding.general");
            }
        } else {
            os.write((byte) (b1 << 2 | b2 >> 4));
            os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            os.write((byte) (b3 << 6 | b4));
        }
        return;
    }

    public final static void decode(InputStream is, OutputStream os) throws Base64DecodingException, IOException {
        byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;
        int index = 0;
        byte[] data = new byte[4];
        int read;
        while ((read = is.read()) > 0) {
            byte readed = (byte) read;
            if (isWhiteSpace(readed)) {
                continue;
            }
            if (isPad(readed)) {
                data[index++] = readed;
                if (index == 3)
                    data[index++] = (byte) is.read();
                break;
            }
            if ((data[index++] = readed) == -1) {
                throw new Base64DecodingException("decoding.general");
            }
            if (index != 4) {
                continue;
            }
            index = 0;
            b1 = base64Alphabet[data[0]];
            b2 = base64Alphabet[data[1]];
            b3 = base64Alphabet[data[2]];
            b4 = base64Alphabet[data[3]];
            os.write((byte) (b1 << 2 | b2 >> 4));
            os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            os.write((byte) (b3 << 6 | b4));
        }
        byte d1 = data[0], d2 = data[1], d3 = data[2], d4 = data[3];
        b1 = base64Alphabet[d1];
        b2 = base64Alphabet[d2];
        b3 = base64Alphabet[d3];
        b4 = base64Alphabet[d4];
        if ((b3 == -1) || (b4 == -1)) {
            if (isPad(d3) && isPad(d4)) {
                if ((b2 & 0xf) != 0)
                    throw new Base64DecodingException("decoding.general");
                os.write((byte) (b1 << 2 | b2 >> 4));
            } else if (!isPad(d3) && isPad(d4)) {
                b3 = base64Alphabet[d3];
                if ((b3 & 0x3) != 0)
                    throw new Base64DecodingException("decoding.general");
                os.write((byte) (b1 << 2 | b2 >> 4));
                os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            } else {
                throw new Base64DecodingException("decoding.general");
            }
        } else {
            os.write((byte) (b1 << 2 | b2 >> 4));
            os.write((byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf)));
            os.write((byte) (b3 << 6 | b4));
        }
        return;
    }

    protected static final int removeWhiteSpace(byte[] data) {
        if (data == null)
            return 0;
        int newSize = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            byte dataS = data[i];
            if (!isWhiteSpace(dataS))
                data[newSize++] = dataS;
        }
        return newSize;
    }
}
