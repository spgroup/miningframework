package sun.misc;

import java.math.BigInteger;
import java.util.Arrays;

public class FDBigInteger {

    static final int[] SMALL_5_POW = { 1, 5, 5 * 5, 5 * 5 * 5, 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 };

    static final long[] LONG_5_POW = { 1L, 5L, 5L * 5, 5L * 5 * 5, 5L * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5, 5L * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 * 5 };

    private static final int MAX_FIVE_POW = 340;

    private static final FDBigInteger[] POW_5_CACHE;

    static {
        POW_5_CACHE = new FDBigInteger[MAX_FIVE_POW];
        int i = 0;
        while (i < SMALL_5_POW.length) {
            FDBigInteger pow5 = new FDBigInteger(new int[] { SMALL_5_POW[i] }, 0);
            pow5.makeImmutable();
            POW_5_CACHE[i] = pow5;
            i++;
        }
        FDBigInteger prev = POW_5_CACHE[i - 1];
        while (i < MAX_FIVE_POW) {
            POW_5_CACHE[i] = prev = prev.mult(5);
            prev.makeImmutable();
            i++;
        }
    }

    public static final FDBigInteger ZERO = new FDBigInteger(new int[0], 0);

    static {
        ZERO.makeImmutable();
    }

    private final static long LONG_MASK = 0xffffffffL;

    private int[] data;

    private int offset;

    private int nWords;

    private boolean isImmutable = false;

    private FDBigInteger(int[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.nWords = data.length;
        trimLeadingZeros();
    }

    public FDBigInteger(long lValue, char[] digits, int kDigits, int nDigits) {
        int n = Math.max((nDigits + 8) / 9, 2);
        data = new int[n];
        data[0] = (int) lValue;
        data[1] = (int) (lValue >>> 32);
        offset = 0;
        nWords = 2;
        int i = kDigits;
        int limit = nDigits - 5;
        int v;
        while (i < limit) {
            int ilim = i + 5;
            v = (int) digits[i++] - (int) '0';
            while (i < ilim) {
                v = 10 * v + (int) digits[i++] - (int) '0';
            }
            multAddMe(100000, v);
        }
        int factor = 1;
        v = 0;
        while (i < nDigits) {
            v = 10 * v + (int) digits[i++] - (int) '0';
            factor *= 10;
        }
        if (factor != 1) {
            multAddMe(factor, v);
        }
        trimLeadingZeros();
    }

    public static FDBigInteger valueOfPow52(int p5, int p2) {
        if (p5 != 0) {
            if (p2 == 0) {
                return big5pow(p5);
            } else if (p5 < SMALL_5_POW.length) {
                int pow5 = SMALL_5_POW[p5];
                int wordcount = p2 >> 5;
                int bitcount = p2 & 0x1f;
                if (bitcount == 0) {
                    return new FDBigInteger(new int[] { pow5 }, wordcount);
                } else {
                    return new FDBigInteger(new int[] { pow5 << bitcount, pow5 >>> (32 - bitcount) }, wordcount);
                }
            } else {
                return big5pow(p5).leftShift(p2);
            }
        } else {
            return valueOfPow2(p2);
        }
    }

    public static FDBigInteger valueOfMulPow52(long value, int p5, int p2) {
        assert p5 >= 0 : p5;
        assert p2 >= 0 : p2;
        int v0 = (int) value;
        int v1 = (int) (value >>> 32);
        int wordcount = p2 >> 5;
        int bitcount = p2 & 0x1f;
        if (p5 != 0) {
            if (p5 < SMALL_5_POW.length) {
                long pow5 = SMALL_5_POW[p5] & LONG_MASK;
                long carry = (v0 & LONG_MASK) * pow5;
                v0 = (int) carry;
                carry >>>= 32;
                carry = (v1 & LONG_MASK) * pow5 + carry;
                v1 = (int) carry;
                int v2 = (int) (carry >>> 32);
                if (bitcount == 0) {
                    return new FDBigInteger(new int[] { v0, v1, v2 }, wordcount);
                } else {
                    return new FDBigInteger(new int[] { v0 << bitcount, (v1 << bitcount) | (v0 >>> (32 - bitcount)), (v2 << bitcount) | (v1 >>> (32 - bitcount)), v2 >>> (32 - bitcount) }, wordcount);
                }
            } else {
                FDBigInteger pow5 = big5pow(p5);
                int[] r;
                if (v1 == 0) {
                    r = new int[pow5.nWords + 1 + ((p2 != 0) ? 1 : 0)];
                    mult(pow5.data, pow5.nWords, v0, r);
                } else {
                    r = new int[pow5.nWords + 2 + ((p2 != 0) ? 1 : 0)];
                    mult(pow5.data, pow5.nWords, v0, v1, r);
                }
                return (new FDBigInteger(r, pow5.offset)).leftShift(p2);
            }
        } else if (p2 != 0) {
            if (bitcount == 0) {
                return new FDBigInteger(new int[] { v0, v1 }, wordcount);
            } else {
                return new FDBigInteger(new int[] { v0 << bitcount, (v1 << bitcount) | (v0 >>> (32 - bitcount)), v1 >>> (32 - bitcount) }, wordcount);
            }
        }
        return new FDBigInteger(new int[] { v0, v1 }, 0);
    }

    private static FDBigInteger valueOfPow2(int p2) {
        int wordcount = p2 >> 5;
        int bitcount = p2 & 0x1f;
        return new FDBigInteger(new int[] { 1 << bitcount }, wordcount);
    }

    private void trimLeadingZeros() {
        int i = nWords;
        if (i > 0 && (data[--i] == 0)) {
            while (i > 0 && data[i - 1] == 0) {
                i--;
            }
            this.nWords = i;
            if (i == 0) {
                this.offset = 0;
            }
        }
    }

    public int getNormalizationBias() {
        if (nWords == 0) {
            throw new IllegalArgumentException("Zero value cannot be normalized");
        }
        int zeros = Integer.numberOfLeadingZeros(data[nWords - 1]);
        return (zeros < 4) ? 28 + zeros : zeros - 4;
    }

    private static void leftShift(int[] src, int idx, int[] result, int bitcount, int anticount, int prev) {
        for (; idx > 0; idx--) {
            int v = (prev << bitcount);
            prev = src[idx - 1];
            v |= (prev >>> anticount);
            result[idx] = v;
        }
        int v = prev << bitcount;
        result[0] = v;
    }

    public FDBigInteger leftShift(int shift) {
        if (shift == 0 || nWords == 0) {
            return this;
        }
        int wordcount = shift >> 5;
        int bitcount = shift & 0x1f;
        if (this.isImmutable) {
            if (bitcount == 0) {
                return new FDBigInteger(Arrays.copyOf(data, nWords), offset + wordcount);
            } else {
                int anticount = 32 - bitcount;
                int idx = nWords - 1;
                int prev = data[idx];
                int hi = prev >>> anticount;
                int[] result;
                if (hi != 0) {
                    result = new int[nWords + 1];
                    result[nWords] = hi;
                } else {
                    result = new int[nWords];
                }
                leftShift(data, idx, result, bitcount, anticount, prev);
                return new FDBigInteger(result, offset + wordcount);
            }
        } else {
            if (bitcount != 0) {
                int anticount = 32 - bitcount;
                if ((data[0] << bitcount) == 0) {
                    int idx = 0;
                    int prev = data[idx];
                    for (; idx < nWords - 1; idx++) {
                        int v = (prev >>> anticount);
                        prev = data[idx + 1];
                        v |= (prev << bitcount);
                        data[idx] = v;
                    }
                    int v = prev >>> anticount;
                    data[idx] = v;
                    if (v == 0) {
                        nWords--;
                    }
                    offset++;
                } else {
                    int idx = nWords - 1;
                    int prev = data[idx];
                    int hi = prev >>> anticount;
                    int[] result = data;
                    int[] src = data;
                    if (hi != 0) {
                        if (nWords == data.length) {
                            data = result = new int[nWords + 1];
                        }
                        result[nWords++] = hi;
                    }
                    leftShift(src, idx, result, bitcount, anticount, prev);
                }
            }
            offset += wordcount;
            return this;
        }
    }

    private int size() {
        return nWords + offset;
    }

    public int quoRemIteration(FDBigInteger S) throws IllegalArgumentException {
        assert !this.isImmutable : "cannot modify immutable value";
        int thSize = this.size();
        int sSize = S.size();
        if (thSize < sSize) {
            int p = multAndCarryBy10(this.data, this.nWords, this.data);
            if (p != 0) {
                this.data[nWords++] = p;
            } else {
                trimLeadingZeros();
            }
            return 0;
        } else if (thSize > sSize) {
            throw new IllegalArgumentException("disparate values");
        }
        long q = (this.data[this.nWords - 1] & LONG_MASK) / (S.data[S.nWords - 1] & LONG_MASK);
        long diff = multDiffMe(q, S);
        if (diff != 0L) {
            long sum = 0L;
            int tStart = S.offset - this.offset;
            int[] sd = S.data;
            int[] td = this.data;
            while (sum == 0L) {
                for (int sIndex = 0, tIndex = tStart; tIndex < this.nWords; sIndex++, tIndex++) {
                    sum += (td[tIndex] & LONG_MASK) + (sd[sIndex] & LONG_MASK);
                    td[tIndex] = (int) sum;
                    sum >>>= 32;
                }
                assert sum == 0 || sum == 1 : sum;
                q -= 1;
            }
        }
        int p = multAndCarryBy10(this.data, this.nWords, this.data);
        assert p == 0 : p;
        trimLeadingZeros();
        return (int) q;
    }

    public FDBigInteger multBy10() {
        if (nWords == 0) {
            return this;
        }
        if (isImmutable) {
            int[] res = new int[nWords + 1];
            res[nWords] = multAndCarryBy10(data, nWords, res);
            return new FDBigInteger(res, offset);
        } else {
            int p = multAndCarryBy10(this.data, this.nWords, this.data);
            if (p != 0) {
                if (nWords == data.length) {
                    if (data[0] == 0) {
                        System.arraycopy(data, 1, data, 0, --nWords);
                        offset++;
                    } else {
                        data = Arrays.copyOf(data, data.length + 1);
                    }
                }
                data[nWords++] = p;
            } else {
                trimLeadingZeros();
            }
            return this;
        }
    }

    public FDBigInteger multByPow52(int p5, int p2) {
        if (this.nWords == 0) {
            return this;
        }
        FDBigInteger res = this;
        if (p5 != 0) {
            int[] r;
            int extraSize = (p2 != 0) ? 1 : 0;
            if (p5 < SMALL_5_POW.length) {
                r = new int[this.nWords + 1 + extraSize];
                mult(this.data, this.nWords, SMALL_5_POW[p5], r);
                res = new FDBigInteger(r, this.offset);
            } else {
                FDBigInteger pow5 = big5pow(p5);
                r = new int[this.nWords + pow5.size() + extraSize];
                mult(this.data, this.nWords, pow5.data, pow5.nWords, r);
                res = new FDBigInteger(r, this.offset + pow5.offset);
            }
        }
        return res.leftShift(p2);
    }

    private static void mult(int[] s1, int s1Len, int[] s2, int s2Len, int[] dst) {
        for (int i = 0; i < s1Len; i++) {
            long v = s1[i] & LONG_MASK;
            long p = 0L;
            for (int j = 0; j < s2Len; j++) {
                p += (dst[i + j] & LONG_MASK) + v * (s2[j] & LONG_MASK);
                dst[i + j] = (int) p;
                p >>>= 32;
            }
            dst[i + s2Len] = (int) p;
        }
    }

    public FDBigInteger leftInplaceSub(FDBigInteger subtrahend) {
        assert this.size() >= subtrahend.size() : "result should be positive";
        FDBigInteger minuend;
        if (this.isImmutable) {
            minuend = new FDBigInteger(this.data, this.offset);
        } else {
            minuend = this;
        }
        int offsetDiff = subtrahend.offset - minuend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            int rLen = minLen - offsetDiff;
            if (rLen < mData.length) {
                System.arraycopy(mData, 0, mData, -offsetDiff, minLen);
                Arrays.fill(mData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy(mData, 0, r, -offsetDiff, minLen);
                minuend.data = mData = r;
            }
            minuend.offset = subtrahend.offset;
            minuend.nWords = minLen = rLen;
            offsetDiff = 0;
        }
        long borrow = 0L;
        int mIndex = offsetDiff;
        for (int sIndex = 0; sIndex < subLen && mIndex < minLen; sIndex++, mIndex++) {
            long diff = (mData[mIndex] & LONG_MASK) - (sData[sIndex] & LONG_MASK) + borrow;
            mData[mIndex] = (int) diff;
            borrow = diff >> 32;
        }
        for (; borrow != 0 && mIndex < minLen; mIndex++) {
            long diff = (mData[mIndex] & LONG_MASK) + borrow;
            mData[mIndex] = (int) diff;
            borrow = diff >> 32;
        }
        assert borrow == 0L : borrow;
        minuend.trimLeadingZeros();
        return minuend;
    }

    public FDBigInteger rightInplaceSub(FDBigInteger subtrahend) {
        assert this.size() >= subtrahend.size() : "result should be positive";
        FDBigInteger minuend = this;
        if (subtrahend.isImmutable) {
            subtrahend = new FDBigInteger(subtrahend.data, subtrahend.offset);
        }
        int offsetDiff = minuend.offset - subtrahend.offset;
        int[] sData = subtrahend.data;
        int[] mData = minuend.data;
        int subLen = subtrahend.nWords;
        int minLen = minuend.nWords;
        if (offsetDiff < 0) {
            int rLen = minLen;
            if (rLen < sData.length) {
                System.arraycopy(sData, 0, sData, -offsetDiff, subLen);
                Arrays.fill(sData, 0, -offsetDiff, 0);
            } else {
                int[] r = new int[rLen];
                System.arraycopy(sData, 0, r, -offsetDiff, subLen);
                subtrahend.data = sData = r;
            }
            subtrahend.offset = minuend.offset;
            subLen -= offsetDiff;
            offsetDiff = 0;
        } else {
            int rLen = minLen + offsetDiff;
            if (rLen >= sData.length) {
                subtrahend.data = sData = Arrays.copyOf(sData, rLen);
            }
        }
        int sIndex = 0;
        long borrow = 0L;
        for (; sIndex < offsetDiff; sIndex++) {
            long diff = 0L - (sData[sIndex] & LONG_MASK) + borrow;
            sData[sIndex] = (int) diff;
            borrow = diff >> 32;
        }
        for (int mIndex = 0; mIndex < minLen; sIndex++, mIndex++) {
            long diff = (mData[mIndex] & LONG_MASK) - (sData[sIndex] & LONG_MASK) + borrow;
            sData[sIndex] = (int) diff;
            borrow = diff >> 32;
        }
        assert borrow == 0L : borrow;
        subtrahend.nWords = sIndex;
        subtrahend.trimLeadingZeros();
        return subtrahend;
    }

    private static int checkZeroTail(int[] a, int from) {
        while (from > 0) {
            if (a[--from] != 0) {
                return 1;
            }
        }
        return 0;
    }

    public int cmp(FDBigInteger other) {
        int aSize = nWords + offset;
        int bSize = other.nWords + other.offset;
        if (aSize > bSize) {
            return 1;
        } else if (aSize < bSize) {
            return -1;
        }
        int aLen = nWords;
        int bLen = other.nWords;
        while (aLen > 0 && bLen > 0) {
            int a = data[--aLen];
            int b = other.data[--bLen];
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
        }
        if (aLen > 0) {
            return checkZeroTail(data, aLen);
        }
        if (bLen > 0) {
            return -checkZeroTail(other.data, bLen);
        }
        return 0;
    }

    public int cmpPow52(int p5, int p2) {
        if (p5 == 0) {
            int wordcount = p2 >> 5;
            int bitcount = p2 & 0x1f;
            int size = this.nWords + this.offset;
            if (size > wordcount + 1) {
                return 1;
            } else if (size < wordcount + 1) {
                return -1;
            }
            int a = this.data[this.nWords - 1];
            int b = 1 << bitcount;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
            }
            return checkZeroTail(this.data, this.nWords - 1);
        }
        return this.cmp(big5pow(p5).leftShift(p2));
    }

    public int addAndCmp(FDBigInteger x, FDBigInteger y) {
        FDBigInteger big;
        FDBigInteger small;
        int xSize = x.size();
        int ySize = y.size();
        int bSize;
        int sSize;
        if (xSize >= ySize) {
            big = x;
            small = y;
            bSize = xSize;
            sSize = ySize;
        } else {
            big = y;
            small = x;
            bSize = ySize;
            sSize = xSize;
        }
        int thSize = this.size();
        if (bSize == 0) {
            return thSize == 0 ? 0 : 1;
        }
        if (sSize == 0) {
            return this.cmp(big);
        }
        if (bSize > thSize) {
            return -1;
        }
        if (bSize + 1 < thSize) {
            return 1;
        }
        long top = (big.data[big.nWords - 1] & LONG_MASK);
        if (sSize == bSize) {
            top += (small.data[small.nWords - 1] & LONG_MASK);
        }
        if ((top >>> 32) == 0) {
            if (((top + 1) >>> 32) == 0) {
                if (bSize < thSize) {
                    return 1;
                }
                long v = (this.data[this.nWords - 1] & LONG_MASK);
                if (v < top) {
                    return -1;
                }
                if (v > top + 1) {
                    return 1;
                }
            }
        } else {
            if (bSize + 1 > thSize) {
                return -1;
            }
            top >>>= 32;
            long v = (this.data[this.nWords - 1] & LONG_MASK);
            if (v < top) {
                return -1;
            }
            if (v > top + 1) {
                return 1;
            }
        }
        return this.cmp(big.add(small));
    }

    public void makeImmutable() {
        this.isImmutable = true;
    }

    private FDBigInteger mult(int i) {
        if (this.nWords == 0) {
            return this;
        }
        int[] r = new int[nWords + 1];
        mult(data, nWords, i, r);
        return new FDBigInteger(r, offset);
    }

    private FDBigInteger mult(FDBigInteger other) {
        if (this.nWords == 0) {
            return this;
        }
        if (this.size() == 1) {
            return other.mult(data[0]);
        }
        if (other.nWords == 0) {
            return other;
        }
        if (other.size() == 1) {
            return this.mult(other.data[0]);
        }
        int[] r = new int[nWords + other.nWords];
        mult(this.data, this.nWords, other.data, other.nWords, r);
        return new FDBigInteger(r, this.offset + other.offset);
    }

    private FDBigInteger add(FDBigInteger other) {
        FDBigInteger big, small;
        int bigLen, smallLen;
        int tSize = this.size();
        int oSize = other.size();
        if (tSize >= oSize) {
            big = this;
            bigLen = tSize;
            small = other;
            smallLen = oSize;
        } else {
            big = other;
            bigLen = oSize;
            small = this;
            smallLen = tSize;
        }
        int[] r = new int[bigLen + 1];
        int i = 0;
        long carry = 0L;
        for (; i < smallLen; i++) {
            carry += (i < big.offset ? 0L : (big.data[i - big.offset] & LONG_MASK)) + ((i < small.offset ? 0L : (small.data[i - small.offset] & LONG_MASK)));
            r[i] = (int) carry;
            carry >>= 32;
        }
        for (; i < bigLen; i++) {
            carry += (i < big.offset ? 0L : (big.data[i - big.offset] & LONG_MASK));
            r[i] = (int) carry;
            carry >>= 32;
        }
        r[bigLen] = (int) carry;
        return new FDBigInteger(r, 0);
    }

    private void multAddMe(int iv, int addend) {
        long v = iv & LONG_MASK;
        long p = v * (data[0] & LONG_MASK) + (addend & LONG_MASK);
        data[0] = (int) p;
        p >>>= 32;
        for (int i = 1; i < nWords; i++) {
            p += v * (data[i] & LONG_MASK);
            data[i] = (int) p;
            p >>>= 32;
        }
        if (p != 0L) {
            data[nWords++] = (int) p;
        }
    }

    private long multDiffMe(long q, FDBigInteger S) {
        long diff = 0L;
        if (q != 0) {
            int deltaSize = S.offset - this.offset;
            if (deltaSize >= 0) {
                int[] sd = S.data;
                int[] td = this.data;
                for (int sIndex = 0, tIndex = deltaSize; sIndex < S.nWords; sIndex++, tIndex++) {
                    diff += (td[tIndex] & LONG_MASK) - q * (sd[sIndex] & LONG_MASK);
                    td[tIndex] = (int) diff;
                    diff >>= 32;
                }
            } else {
                deltaSize = -deltaSize;
                int[] rd = new int[nWords + deltaSize];
                int sIndex = 0;
                int rIndex = 0;
                int[] sd = S.data;
                for (; rIndex < deltaSize && sIndex < S.nWords; sIndex++, rIndex++) {
                    diff -= q * (sd[sIndex] & LONG_MASK);
                    rd[rIndex] = (int) diff;
                    diff >>= 32;
                }
                int tIndex = 0;
                int[] td = this.data;
                for (; sIndex < S.nWords; sIndex++, tIndex++, rIndex++) {
                    diff += (td[tIndex] & LONG_MASK) - q * (sd[sIndex] & LONG_MASK);
                    rd[rIndex] = (int) diff;
                    diff >>= 32;
                }
                this.nWords += deltaSize;
                this.offset -= deltaSize;
                this.data = rd;
            }
        }
        return diff;
    }

    private static int multAndCarryBy10(int[] src, int srcLen, int[] dst) {
        long carry = 0;
        for (int i = 0; i < srcLen; i++) {
            long product = (src[i] & LONG_MASK) * 10L + carry;
            dst[i] = (int) product;
            carry = product >>> 32;
        }
        return (int) carry;
    }

    private static void mult(int[] src, int srcLen, int value, int[] dst) {
        long val = value & LONG_MASK;
        long carry = 0;
        for (int i = 0; i < srcLen; i++) {
            long product = (src[i] & LONG_MASK) * val + carry;
            dst[i] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int) carry;
    }

    private static void mult(int[] src, int srcLen, int v0, int v1, int[] dst) {
        long v = v0 & LONG_MASK;
        long carry = 0;
        for (int j = 0; j < srcLen; j++) {
            long product = v * (src[j] & LONG_MASK) + carry;
            dst[j] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen] = (int) carry;
        v = v1 & LONG_MASK;
        carry = 0;
        for (int j = 0; j < srcLen; j++) {
            long product = (dst[j + 1] & LONG_MASK) + v * (src[j] & LONG_MASK) + carry;
            dst[j + 1] = (int) product;
            carry = product >>> 32;
        }
        dst[srcLen + 1] = (int) carry;
    }

    private static FDBigInteger big5pow(int p) {
        assert p >= 0 : p;
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        return big5powRec(p);
    }

    private static FDBigInteger big5powRec(int p) {
        if (p < MAX_FIVE_POW) {
            return POW_5_CACHE[p];
        }
        int q, r;
        q = p >> 1;
        r = p - q;
        FDBigInteger bigq = big5powRec(q);
        if (r < SMALL_5_POW.length) {
            return bigq.mult(SMALL_5_POW[r]);
        } else {
            return bigq.mult(big5powRec(r));
        }
    }

    public String toHexString() {
        if (nWords == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder((nWords + offset) * 8);
        for (int i = nWords - 1; i >= 0; i--) {
            String subStr = Integer.toHexString(data[i]);
            for (int j = subStr.length(); j < 8; j++) {
                sb.append('0');
            }
            sb.append(subStr);
        }
        for (int i = offset; i > 0; i--) {
            sb.append("00000000");
        }
        return sb.toString();
    }

    public BigInteger toBigInteger() {
        byte[] magnitude = new byte[nWords * 4 + 1];
        for (int i = 0; i < nWords; i++) {
            int w = data[i];
            magnitude[magnitude.length - 4 * i - 1] = (byte) w;
            magnitude[magnitude.length - 4 * i - 2] = (byte) (w >> 8);
            magnitude[magnitude.length - 4 * i - 3] = (byte) (w >> 16);
            magnitude[magnitude.length - 4 * i - 4] = (byte) (w >> 24);
        }
        return new BigInteger(magnitude).shiftLeft(offset * 32);
    }

    @Override
    public String toString() {
        return toBigInteger().toString();
    }
}
