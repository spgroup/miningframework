package sun.security.provider;

import java.security.*;
import static sun.security.provider.ByteArrayAccess.*;

public final class MD4 extends DigestBase {

    private int[] state;

    private int[] x;

    private static final int S11 = 3;

    private static final int S12 = 7;

    private static final int S13 = 11;

    private static final int S14 = 19;

    private static final int S21 = 3;

    private static final int S22 = 5;

    private static final int S23 = 9;

    private static final int S24 = 13;

    private static final int S31 = 3;

    private static final int S32 = 9;

    private static final int S33 = 11;

    private static final int S34 = 15;

    private final static Provider md4Provider;

    static {
        md4Provider = new Provider("MD4Provider", 1.8d, "MD4 MessageDigest") {

            private static final long serialVersionUID = -8850464997518327965L;
        };
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                md4Provider.put("MessageDigest.MD4", "sun.security.provider.MD4");
                return null;
            }
        });
    }

    public static MessageDigest getInstance() {
        try {
            return MessageDigest.getInstance("MD4", md4Provider);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
    }

    public MD4() {
        super("MD4", 16, 64);
        state = new int[4];
        x = new int[16];
        implReset();
    }

    public Object clone() throws CloneNotSupportedException {
        MD4 copy = (MD4) super.clone();
        copy.state = copy.state.clone();
        copy.x = new int[16];
        return copy;
    }

    void implReset() {
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
    }

    void implDigest(byte[] out, int ofs) {
        long bitsProcessed = bytesProcessed << 3;
        int index = (int) bytesProcessed & 0x3f;
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        engineUpdate(padding, 0, padLen);
        i2bLittle4((int) bitsProcessed, buffer, 56);
        i2bLittle4((int) (bitsProcessed >>> 32), buffer, 60);
        implCompress(buffer, 0);
        i2bLittle(state, 0, out, ofs, 16);
    }

    private static int FF(int a, int b, int c, int d, int x, int s) {
        a += ((b & c) | ((~b) & d)) + x;
        return ((a << s) | (a >>> (32 - s)));
    }

    private static int GG(int a, int b, int c, int d, int x, int s) {
        a += ((b & c) | (b & d) | (c & d)) + x + 0x5a827999;
        return ((a << s) | (a >>> (32 - s)));
    }

    private static int HH(int a, int b, int c, int d, int x, int s) {
        a += ((b ^ c) ^ d) + x + 0x6ed9eba1;
        return ((a << s) | (a >>> (32 - s)));
    }

    void implCompress(byte[] buf, int ofs) {
        b2iLittle64(buf, ofs, x);
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];
        a = FF(a, b, c, d, x[0], S11);
        d = FF(d, a, b, c, x[1], S12);
        c = FF(c, d, a, b, x[2], S13);
        b = FF(b, c, d, a, x[3], S14);
        a = FF(a, b, c, d, x[4], S11);
        d = FF(d, a, b, c, x[5], S12);
        c = FF(c, d, a, b, x[6], S13);
        b = FF(b, c, d, a, x[7], S14);
        a = FF(a, b, c, d, x[8], S11);
        d = FF(d, a, b, c, x[9], S12);
        c = FF(c, d, a, b, x[10], S13);
        b = FF(b, c, d, a, x[11], S14);
        a = FF(a, b, c, d, x[12], S11);
        d = FF(d, a, b, c, x[13], S12);
        c = FF(c, d, a, b, x[14], S13);
        b = FF(b, c, d, a, x[15], S14);
        a = GG(a, b, c, d, x[0], S21);
        d = GG(d, a, b, c, x[4], S22);
        c = GG(c, d, a, b, x[8], S23);
        b = GG(b, c, d, a, x[12], S24);
        a = GG(a, b, c, d, x[1], S21);
        d = GG(d, a, b, c, x[5], S22);
        c = GG(c, d, a, b, x[9], S23);
        b = GG(b, c, d, a, x[13], S24);
        a = GG(a, b, c, d, x[2], S21);
        d = GG(d, a, b, c, x[6], S22);
        c = GG(c, d, a, b, x[10], S23);
        b = GG(b, c, d, a, x[14], S24);
        a = GG(a, b, c, d, x[3], S21);
        d = GG(d, a, b, c, x[7], S22);
        c = GG(c, d, a, b, x[11], S23);
        b = GG(b, c, d, a, x[15], S24);
        a = HH(a, b, c, d, x[0], S31);
        d = HH(d, a, b, c, x[8], S32);
        c = HH(c, d, a, b, x[4], S33);
        b = HH(b, c, d, a, x[12], S34);
        a = HH(a, b, c, d, x[2], S31);
        d = HH(d, a, b, c, x[10], S32);
        c = HH(c, d, a, b, x[6], S33);
        b = HH(b, c, d, a, x[14], S34);
        a = HH(a, b, c, d, x[1], S31);
        d = HH(d, a, b, c, x[9], S32);
        c = HH(c, d, a, b, x[5], S33);
        b = HH(b, c, d, a, x[13], S34);
        a = HH(a, b, c, d, x[3], S31);
        d = HH(d, a, b, c, x[11], S32);
        c = HH(c, d, a, b, x[7], S33);
        b = HH(b, c, d, a, x[15], S34);
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }
}