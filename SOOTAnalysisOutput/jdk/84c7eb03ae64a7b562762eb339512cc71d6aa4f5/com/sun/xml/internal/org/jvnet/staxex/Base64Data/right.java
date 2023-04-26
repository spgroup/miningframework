package com.sun.xml.internal.org.jvnet.staxex;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Base64Data implements CharSequence, Cloneable {

    private DataHandler dataHandler;

    private byte[] data;

    private int dataLen;

    private boolean dataCloneByRef;

    private String mimeType;

    public Base64Data() {
    }

    private static final Logger logger = Logger.getLogger(Base64Data.class.getName());

    public Base64Data(Base64Data that) {
        that.get();
        if (that.dataCloneByRef) {
            this.data = that.data;
        } else {
            this.data = new byte[that.dataLen];
            System.arraycopy(that.data, 0, this.data, 0, that.dataLen);
        }
        this.dataCloneByRef = true;
        this.dataLen = that.dataLen;
        this.dataHandler = null;
        this.mimeType = that.mimeType;
    }

    public void set(byte[] data, int len, String mimeType, boolean cloneByRef) {
        this.data = data;
        this.dataLen = len;
        this.dataCloneByRef = cloneByRef;
        this.dataHandler = null;
        this.mimeType = mimeType;
    }

    public void set(byte[] data, int len, String mimeType) {
        set(data, len, mimeType, false);
    }

    public void set(byte[] data, String mimeType) {
        set(data, data.length, mimeType, false);
    }

    public void set(DataHandler data) {
        assert data != null;
        this.dataHandler = data;
        this.data = null;
    }

    public DataHandler getDataHandler() {
        if (dataHandler == null) {
            dataHandler = new Base64StreamingDataHandler(new Base64DataSource());
        } else if (!(dataHandler instanceof StreamingDataHandler)) {
            dataHandler = new FilterDataHandler(dataHandler);
        }
        return dataHandler;
    }

    private final class Base64DataSource implements DataSource {

        public String getContentType() {
            return getMimeType();
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(data, 0, dataLen);
        }

        public String getName() {
            return null;
        }

        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }
    }

    private final class Base64StreamingDataHandler extends StreamingDataHandler {

        Base64StreamingDataHandler(DataSource source) {
            super(source);
        }

        public InputStream readOnce() throws IOException {
            return getDataSource().getInputStream();
        }

        public void moveTo(File dst) throws IOException {
            FileOutputStream fout = new FileOutputStream(dst);
            try {
                fout.write(data, 0, dataLen);
            } finally {
                fout.close();
            }
        }

        public void close() throws IOException {
        }
    }

    private static final class FilterDataHandler extends StreamingDataHandler {

        FilterDataHandler(DataHandler dh) {
            super(dh.getDataSource());
        }

        public InputStream readOnce() throws IOException {
            return getDataSource().getInputStream();
        }

        public void moveTo(File dst) throws IOException {
            byte[] buf = new byte[8192];
            InputStream in = null;
            OutputStream out = null;
            try {
                in = getDataSource().getInputStream();
                out = new FileOutputStream(dst);
                while (true) {
                    int amountRead = in.read(buf);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buf, 0, amountRead);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }

        public void close() throws IOException {
        }
    }

    public byte[] getExact() {
        get();
        if (dataLen != data.length) {
            byte[] buf = new byte[dataLen];
            System.arraycopy(data, 0, buf, 0, dataLen);
            data = buf;
        }
        return data;
    }

    public InputStream getInputStream() throws IOException {
        if (dataHandler != null)
            return dataHandler.getInputStream();
        else
            return new ByteArrayInputStream(data, 0, dataLen);
    }

    public boolean hasData() {
        return data != null;
    }

    public byte[] get() {
        if (data == null) {
            try {
                ByteArrayOutputStreamEx baos = new ByteArrayOutputStreamEx(1024);
                InputStream is = dataHandler.getDataSource().getInputStream();
                baos.readFrom(is);
                is.close();
                data = baos.getBuffer();
                dataLen = baos.size();
                dataCloneByRef = true;
            } catch (IOException e) {
                dataLen = 0;
            }
        }
        return data;
    }

    public int getDataLen() {
        get();
        return dataLen;
    }

    public String getMimeType() {
        if (mimeType == null)
            return "application/octet-stream";
        return mimeType;
    }

    public int length() {
        get();
        return ((dataLen + 2) / 3) * 4;
    }

    public char charAt(int index) {
        int offset = index % 4;
        int base = (index / 4) * 3;
        byte b1, b2;
        switch(offset) {
            case 0:
                return Base64Encoder.encode(data[base] >> 2);
            case 1:
                if (base + 1 < dataLen)
                    b1 = data[base + 1];
                else
                    b1 = 0;
                return Base64Encoder.encode(((data[base] & 0x3) << 4) | ((b1 >> 4) & 0xF));
            case 2:
                if (base + 1 < dataLen) {
                    b1 = data[base + 1];
                    if (base + 2 < dataLen)
                        b2 = data[base + 2];
                    else
                        b2 = 0;
                    return Base64Encoder.encode(((b1 & 0xF) << 2) | ((b2 >> 6) & 0x3));
                } else
                    return '=';
            case 3:
                if (base + 2 < dataLen)
                    return Base64Encoder.encode(data[base + 2] & 0x3F);
                else
                    return '=';
        }
        throw new IllegalStateException();
    }

    public CharSequence subSequence(int start, int end) {
        StringBuilder buf = new StringBuilder();
        get();
        for (int i = start; i < end; i++) buf.append(charAt(i));
        return buf;
    }

    @Override
    public String toString() {
        get();
        return Base64Encoder.print(data, 0, dataLen);
    }

    public void writeTo(char[] buf, int start) {
        get();
        Base64Encoder.print(data, 0, dataLen, buf, start);
    }

    private static final int CHUNK_SIZE;

    static {
        int bufSize = 1024;
        try {
            String bufSizeStr = getProperty("com.sun.xml.internal.org.jvnet.staxex.Base64DataStreamWriteBufferSize");
            if (bufSizeStr != null) {
                bufSize = Integer.parseInt(bufSizeStr);
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Error reading com.sun.xml.internal.org.jvnet.staxex.Base64DataStreamWriteBufferSize property", e);
        }
        CHUNK_SIZE = bufSize;
    }

    public void writeTo(XMLStreamWriter output) throws IOException, XMLStreamException {
        if (data == null) {
            try {
                InputStream is = dataHandler.getDataSource().getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                Base64EncoderStream encWriter = new Base64EncoderStream(output, outStream);
                int b;
                byte[] buffer = new byte[CHUNK_SIZE];
                while ((b = is.read(buffer)) != -1) {
                    encWriter.write(buffer, 0, b);
                }
                outStream.close();
                encWriter.close();
            } catch (IOException e) {
                dataLen = 0;
                throw e;
            }
        } else {
            String s = Base64Encoder.print(data, 0, dataLen);
            output.writeCharacters(s);
        }
    }

    @Override
    public Base64Data clone() {
        return new Base64Data(this);
    }

    static String getProperty(final String propName) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(propName);
        } else {
            return (String) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                public java.lang.Object run() {
                    return System.getProperty(propName);
                }
            });
        }
    }
}
