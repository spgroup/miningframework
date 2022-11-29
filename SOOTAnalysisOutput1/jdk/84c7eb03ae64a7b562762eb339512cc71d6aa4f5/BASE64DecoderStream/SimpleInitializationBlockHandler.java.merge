package com.sun.xml.internal.org.jvnet.mimepull;

import java.io.*;

final class BASE64DecoderStream extends FilterInputStream {

    private byte[] buffer = new byte[3];

    private int bufsize = 0;

    private int index = 0;

    private byte[] input_buffer = new byte[78 * 105];

    private int input_pos = 0;

    private int input_len = 0;

    private boolean ignoreErrors = false;

    public BASE64DecoderStream(InputStream in) {
        super(in);
        ignoreErrors = PropUtil.getBooleanSystemProperty("mail.mime.base64.ignoreerrors", false);
    }

    public BASE64DecoderStream(InputStream in, boolean ignoreErrors) {
        super(in);
        this.ignoreErrors = ignoreErrors;
    }

    @Override
    public int read() throws IOException {
        if (index >= bufsize) {
            bufsize = decode(buffer, 0, buffer.length);
            if (bufsize <= 0) {
                return -1;
            }
            index = 0;
        }
        return buffer[index++] & 0xff;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int off0 = off;
        while (index < bufsize && len > 0) {
            buf[off++] = buffer[index++];
            len--;
        }
        if (index >= bufsize) {
            bufsize = index = 0;
        }
        int bsize = (len / 3) * 3;
        if (bsize > 0) {
            int size = decode(buf, off, bsize);
            off += size;
            len -= size;
            if (size != bsize) {
                if (off == off0) {
                    return -1;
                } else {
                    return off - off0;
                }
            }
        }
        for (; len > 0; len--) {
            int c = read();
            if (c == -1) {
                break;
            }
            buf[off++] = (byte) c;
        }
        if (off == off0) {
            return -1;
        } else {
            return off - off0;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = 0;
        while (n-- > 0 && read() >= 0) {
            skipped++;
        }
        return skipped;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int available() throws IOException {
        return ((in.available() * 3) / 4 + (bufsize - index));
    }

    private final static char[] pem_array = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    private final static byte[] pem_convert_array = new byte[256];

    static {
        for (int i = 0; i < 255; i++) {
            pem_convert_array[i] = -1;
        }
        for (int i = 0; i < pem_array.length; i++) {
            pem_convert_array[pem_array[i]] = (byte) i;
        }
    }

    private int decode(byte[] outbuf, int pos, int len) throws IOException {
        int pos0 = pos;
        while (len >= 3) {
            int got = 0;
            int val = 0;
            while (got < 4) {
                int i = getByte();
                if (i == -1 || i == -2) {
                    boolean atEOF;
                    if (i == -1) {
                        if (got == 0) {
                            return pos - pos0;
                        }
                        if (!ignoreErrors) {
                            throw new DecodingException("BASE64Decoder: Error in encoded stream: " + "needed 4 valid base64 characters " + "but only got " + got + " before EOF" + recentChars());
                        }
                        atEOF = true;
                    } else {
                        if (got < 2 && !ignoreErrors) {
                            throw new DecodingException("BASE64Decoder: Error in encoded stream: " + "needed at least 2 valid base64 characters," + " but only got " + got + " before padding character (=)" + recentChars());
                        }
                        if (got == 0) {
                            return pos - pos0;
                        }
                        atEOF = false;
                    }
                    int size = got - 1;
                    if (size == 0) {
                        size = 1;
                    }
                    got++;
                    val <<= 6;
                    while (got < 4) {
                        if (!atEOF) {
                            i = getByte();
                            if (i == -1) {
                                if (!ignoreErrors) {
                                    throw new DecodingException("BASE64Decoder: Error in encoded " + "stream: hit EOF while looking for " + "padding characters (=)" + recentChars());
                                }
                            } else if (i != -2) {
                                if (!ignoreErrors) {
                                    throw new DecodingException("BASE64Decoder: Error in encoded " + "stream: found valid base64 " + "character after a padding character " + "(=)" + recentChars());
                                }
                            }
                        }
                        val <<= 6;
                        got++;
                    }
                    val >>= 8;
                    if (size == 2) {
                        outbuf[pos + 1] = (byte) (val & 0xff);
                    }
                    val >>= 8;
                    outbuf[pos] = (byte) (val & 0xff);
                    pos += size;
                    return pos - pos0;
                } else {
                    val <<= 6;
                    got++;
                    val |= i;
                }
            }
            outbuf[pos + 2] = (byte) (val & 0xff);
            val >>= 8;
            outbuf[pos + 1] = (byte) (val & 0xff);
            val >>= 8;
            outbuf[pos] = (byte) (val & 0xff);
            len -= 3;
            pos += 3;
        }
        return pos - pos0;
    }

    private int getByte() throws IOException {
        int c;
        do {
            if (input_pos >= input_len) {
                try {
                    input_len = in.read(input_buffer);
                } catch (EOFException ex) {
                    return -1;
                }
                if (input_len <= 0) {
                    return -1;
                }
                input_pos = 0;
            }
            c = input_buffer[input_pos++] & 0xff;
            if (c == '=') {
                return -2;
            }
            c = pem_convert_array[c];
        } while (c == -1);
        return c;
    }

    private String recentChars() {
        StringBuilder errstr = new StringBuilder();
        int nc = input_pos > 10 ? 10 : input_pos;
        if (nc > 0) {
            errstr.append(", the ").append(nc).append(" most recent characters were: \"");
            for (int k = input_pos - nc; k < input_pos; k++) {
                char c = (char) (input_buffer[k] & 0xff);
                switch(c) {
                    case '\r':
                        errstr.append("\\r");
                        break;
                    case '\n':
                        errstr.append("\\n");
                        break;
                    case '\t':
                        errstr.append("\\t");
                        break;
                    default:
                        if (c >= ' ' && c < 0177) {
                            errstr.append(c);
                        } else {
                            errstr.append("\\").append((int) c);
                        }
                }
            }
            errstr.append("\"");
        }
        return errstr.toString();
    }

    public static byte[] decode(byte[] inbuf) {
        int size = (inbuf.length / 4) * 3;
        if (size == 0) {
            return inbuf;
        }
        if (inbuf[inbuf.length - 1] == '=') {
            size--;
            if (inbuf[inbuf.length - 2] == '=') {
                size--;
            }
        }
        byte[] outbuf = new byte[size];
        int inpos = 0, outpos = 0;
        size = inbuf.length;
        while (size > 0) {
            int val;
            int osize = 3;
            val = pem_convert_array[inbuf[inpos++] & 0xff];
            val <<= 6;
            val |= pem_convert_array[inbuf[inpos++] & 0xff];
            val <<= 6;
            if (inbuf[inpos] != '=') {
                val |= pem_convert_array[inbuf[inpos++] & 0xff];
            } else {
                osize--;
            }
            val <<= 6;
            if (inbuf[inpos] != '=') {
                val |= pem_convert_array[inbuf[inpos++] & 0xff];
            } else {
                osize--;
            }
            if (osize > 2) {
                outbuf[outpos + 2] = (byte) (val & 0xff);
            }
            val >>= 8;
            if (osize > 1) {
                outbuf[outpos + 1] = (byte) (val & 0xff);
            }
            val >>= 8;
            outbuf[outpos] = (byte) (val & 0xff);
            outpos += osize;
            size -= 4;
        }
        return outbuf;
    }
}