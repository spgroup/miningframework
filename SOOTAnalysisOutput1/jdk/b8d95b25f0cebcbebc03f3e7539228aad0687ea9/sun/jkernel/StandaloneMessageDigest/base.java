package sun.jkernel;

import java.security.DigestException;
import java.security.ProviderException;
import java.security.NoSuchAlgorithmException;

public abstract class StandaloneMessageDigest {

    public static final boolean debug = false;

    private StandaloneMessageDigest() {
        digestLength = 0;
        blockSize = 0;
        algorithm = null;
        buffer = null;
    }

    private String algorithm;

    private static final int INITIAL = 0;

    private static final int IN_PROGRESS = 1;

    private int state = INITIAL;

    public static StandaloneMessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
        if (!algorithm.equals("SHA-1")) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        } else {
            return new StandaloneSHA();
        }
    }

    public void update(byte[] input, int offset, int len) {
        if (debug) {
            System.out.println("StandaloneMessageDigest.update");
            (new Exception()).printStackTrace();
        }
        if (input == null) {
            throw new IllegalArgumentException("No input buffer given");
        }
        if (input.length - offset < len) {
            throw new IllegalArgumentException("Input buffer too short");
        }
        engineUpdate(input, offset, len);
        state = IN_PROGRESS;
    }

    public byte[] digest() {
        if (debug) {
            System.out.println("StandaloneMessageDigest.digest");
        }
        byte[] result = engineDigest();
        state = INITIAL;
        return result;
    }

    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta.length != digestb.length)
            return false;
        for (int i = 0; i < digesta.length; i++) {
            if (digesta[i] != digestb[i]) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        if (debug) {
            System.out.println("StandaloneMessageDigest.reset");
        }
        engineReset();
        state = INITIAL;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public final int getDigestLength() {
        return engineGetDigestLength();
    }

    private byte[] oneByte;

    private final int digestLength;

    private final int blockSize;

    final byte[] buffer;

    private int bufOfs;

    long bytesProcessed;

    StandaloneMessageDigest(String algorithm, int digestLength, int blockSize) {
        this.algorithm = algorithm;
        this.digestLength = digestLength;
        this.blockSize = blockSize;
        buffer = new byte[blockSize];
    }

    protected final int engineGetDigestLength() {
        return digestLength;
    }

    protected final void engineUpdate(byte b) {
        if (oneByte == null) {
            oneByte = new byte[1];
        }
        oneByte[0] = b;
        engineUpdate(oneByte, 0, 1);
    }

    protected final void engineUpdate(byte[] b, int ofs, int len) {
        if (len == 0) {
            return;
        }
        if ((ofs < 0) || (len < 0) || (ofs > b.length - len)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (bytesProcessed < 0) {
            engineReset();
        }
        bytesProcessed += len;
        if (bufOfs != 0) {
            int n = Math.min(len, blockSize - bufOfs);
            System.arraycopy(b, ofs, buffer, bufOfs, n);
            bufOfs += n;
            ofs += n;
            len -= n;
            if (bufOfs >= blockSize) {
                implCompress(buffer, 0);
                bufOfs = 0;
            }
        }
        while (len >= blockSize) {
            implCompress(b, ofs);
            len -= blockSize;
            ofs += blockSize;
        }
        if (len > 0) {
            System.arraycopy(b, ofs, buffer, 0, len);
            bufOfs = len;
        }
    }

    protected final void engineReset() {
        if (bytesProcessed == 0) {
            return;
        }
        implReset();
        bufOfs = 0;
        bytesProcessed = 0;
    }

    protected final byte[] engineDigest() throws ProviderException {
        byte[] b = new byte[digestLength];
        try {
            engineDigest(b, 0, b.length);
        } catch (DigestException e) {
            throw (ProviderException) new ProviderException("Internal error").initCause(e);
        }
        return b;
    }

    protected final int engineDigest(byte[] out, int ofs, int len) throws DigestException {
        if (len < digestLength) {
            throw new DigestException("Length must be at least " + digestLength + " for " + algorithm + "digests");
        }
        if ((ofs < 0) || (len < 0) || (ofs > out.length - len)) {
            throw new DigestException("Buffer too short to store digest");
        }
        if (bytesProcessed < 0) {
            engineReset();
        }
        implDigest(out, ofs);
        bytesProcessed = -1;
        return digestLength;
    }

    abstract void implCompress(byte[] b, int ofs);

    abstract void implDigest(byte[] out, int ofs);

    abstract void implReset();

    static final byte[] padding;

    static {
        padding = new byte[136];
        padding[0] = (byte) 0x80;
    }
}
