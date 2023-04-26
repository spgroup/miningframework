package java.math;

import java.util.Random;
import java.io.*;

public class BigInteger extends Number implements Comparable<BigInteger> {

    final int signum;

    final int[] mag;

    @Deprecated
    private int bitCount;

    @Deprecated
    private int bitLength;

    @Deprecated
    private int lowestSetBit;

    @Deprecated
    private int firstNonzeroIntNum;

    final static long LONG_MASK = 0xffffffffL;

    public BigInteger(byte[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");
        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = stripLeadingZeroBytes(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
    }

    private BigInteger(int[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");
        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = trustedStripLeadingZeroInts(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
    }

    public BigInteger(int signum, byte[] magnitude) {
        this.mag = stripLeadingZeroBytes(magnitude);
        if (signum < -1 || signum > 1)
            throw (new NumberFormatException("Invalid signum value"));
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw (new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
    }

    private BigInteger(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);
        if (signum < -1 || signum > 1)
            throw (new NumberFormatException("Invalid signum value"));
        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw (new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
    }

    public BigInteger(String val, int radix) {
        int cursor = 0, numDigits;
        final int len = val.length();
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("Radix out of range");
        if (len == 0)
            throw new NumberFormatException("Zero length BigInteger");
        int sign = 1;
        int index1 = val.lastIndexOf('-');
        int index2 = val.lastIndexOf('+');
        if ((index1 + index2) <= -1) {
            if (index1 == 0 || index2 == 0) {
                cursor = 1;
                if (len == 1)
                    throw new NumberFormatException("Zero length BigInteger");
            }
            if (index1 == 0)
                sign = -1;
        } else
            throw new NumberFormatException("Illegal embedded sign character");
        while (cursor < len && Character.digit(val.charAt(cursor), radix) == 0) cursor++;
        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }
        numDigits = len - cursor;
        signum = sign;
        int numBits = (int) (((numDigits * bitsPerDigit[radix]) >>> 10) + 1);
        int numWords = (numBits + 31) >>> 5;
        int[] magnitude = new int[numWords];
        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[radix];
        String group = val.substring(cursor, cursor += firstGroupLen);
        magnitude[numWords - 1] = Integer.parseInt(group, radix);
        if (magnitude[numWords - 1] < 0)
            throw new NumberFormatException("Illegal digit");
        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            group = val.substring(cursor, cursor += digitsPerInt[radix]);
            groupVal = Integer.parseInt(group, radix);
            if (groupVal < 0)
                throw new NumberFormatException("Illegal digit");
            destructiveMulAdd(magnitude, superRadix, groupVal);
        }
        mag = trustedStripLeadingZeroInts(magnitude);
    }

    BigInteger(char[] val) {
        int cursor = 0, numDigits;
        int len = val.length;
        int sign = 1;
        if (val[0] == '-') {
            if (len == 1)
                throw new NumberFormatException("Zero length BigInteger");
            sign = -1;
            cursor = 1;
        } else if (val[0] == '+') {
            if (len == 1)
                throw new NumberFormatException("Zero length BigInteger");
            cursor = 1;
        }
        while (cursor < len && Character.digit(val[cursor], 10) == 0) cursor++;
        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }
        numDigits = len - cursor;
        signum = sign;
        int numWords;
        if (len < 10) {
            numWords = 1;
        } else {
            int numBits = (int) (((numDigits * bitsPerDigit[10]) >>> 10) + 1);
            numWords = (numBits + 31) >>> 5;
        }
        int[] magnitude = new int[numWords];
        int firstGroupLen = numDigits % digitsPerInt[10];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[10];
        magnitude[numWords - 1] = parseInt(val, cursor, cursor += firstGroupLen);
        while (cursor < len) {
            int groupVal = parseInt(val, cursor, cursor += digitsPerInt[10]);
            destructiveMulAdd(magnitude, intRadix[10], groupVal);
        }
        mag = trustedStripLeadingZeroInts(magnitude);
    }

    private int parseInt(char[] source, int start, int end) {
        int result = Character.digit(source[start++], 10);
        if (result == -1)
            throw new NumberFormatException(new String(source));
        for (int index = start; index < end; index++) {
            int nextVal = Character.digit(source[index], 10);
            if (nextVal == -1)
                throw new NumberFormatException(new String(source));
            result = 10 * result + nextVal;
        }
        return result;
    }

    private static long[] bitsPerDigit = { 0, 0, 1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672, 3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633, 4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210, 5253, 5295 };

    private static void destructiveMulAdd(int[] x, int y, int z) {
        long ylong = y & LONG_MASK;
        long zlong = z & LONG_MASK;
        int len = x.length;
        long product = 0;
        long carry = 0;
        for (int i = len - 1; i >= 0; i--) {
            product = ylong * (x[i] & LONG_MASK) + carry;
            x[i] = (int) product;
            carry = product >>> 32;
        }
        long sum = (x[len - 1] & LONG_MASK) + zlong;
        x[len - 1] = (int) sum;
        carry = sum >>> 32;
        for (int i = len - 2; i >= 0; i--) {
            sum = (x[i] & LONG_MASK) + carry;
            x[i] = (int) sum;
            carry = sum >>> 32;
        }
    }

    public BigInteger(String val) {
        this(val, 10);
    }

    public BigInteger(int numBits, Random rnd) {
        this(1, randomBits(numBits, rnd));
    }

    private static byte[] randomBits(int numBits, Random rnd) {
        if (numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
        int numBytes = (int) (((long) numBits + 7) / 8);
        byte[] randomBits = new byte[numBytes];
        if (numBytes > 0) {
            rnd.nextBytes(randomBits);
            int excessBits = 8 * numBytes - numBits;
            randomBits[0] &= (1 << (8 - excessBits)) - 1;
        }
        return randomBits;
    }

    public BigInteger(int bitLength, int certainty, Random rnd) {
        BigInteger prime;
        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");
        prime = (bitLength < 95 ? smallPrime(bitLength, certainty, rnd) : largePrime(bitLength, certainty, rnd));
        signum = 1;
        mag = prime.mag;
    }

    private static final int SMALL_PRIME_THRESHOLD = 95;

    private static final int DEFAULT_PRIME_CERTAINTY = 100;

    public static BigInteger probablePrime(int bitLength, Random rnd) {
        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");
        return (bitLength < SMALL_PRIME_THRESHOLD ? smallPrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd) : largePrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd));
    }

    private static BigInteger smallPrime(int bitLength, int certainty, Random rnd) {
        int magLen = (bitLength + 31) >>> 5;
        int[] temp = new int[magLen];
        int highBit = 1 << ((bitLength + 31) & 0x1f);
        int highMask = (highBit << 1) - 1;
        while (true) {
            for (int i = 0; i < magLen; i++) temp[i] = rnd.nextInt();
            temp[0] = (temp[0] & highMask) | highBit;
            if (bitLength > 2)
                temp[magLen - 1] |= 1;
            BigInteger p = new BigInteger(temp, 1);
            if (bitLength > 6) {
                long r = p.remainder(SMALL_PRIME_PRODUCT).longValue();
                if ((r % 3 == 0) || (r % 5 == 0) || (r % 7 == 0) || (r % 11 == 0) || (r % 13 == 0) || (r % 17 == 0) || (r % 19 == 0) || (r % 23 == 0) || (r % 29 == 0) || (r % 31 == 0) || (r % 37 == 0) || (r % 41 == 0))
                    continue;
            }
            if (bitLength < 4)
                return p;
            if (p.primeToCertainty(certainty, rnd))
                return p;
        }
    }

    private static final BigInteger SMALL_PRIME_PRODUCT = valueOf(3L * 5 * 7 * 11 * 13 * 17 * 19 * 23 * 29 * 31 * 37 * 41);

    private static BigInteger largePrime(int bitLength, int certainty, Random rnd) {
        BigInteger p;
        p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);
        p.mag[p.mag.length - 1] &= 0xfffffffe;
        int searchLen = (bitLength / 20) * 64;
        BitSieve searchSieve = new BitSieve(p, searchLen);
        BigInteger candidate = searchSieve.retrieve(p, certainty, rnd);
        while ((candidate == null) || (candidate.bitLength() != bitLength)) {
            p = p.add(BigInteger.valueOf(2 * searchLen));
            if (p.bitLength() != bitLength)
                p = new BigInteger(bitLength, rnd).setBit(bitLength - 1);
            p.mag[p.mag.length - 1] &= 0xfffffffe;
            searchSieve = new BitSieve(p, searchLen);
            candidate = searchSieve.retrieve(p, certainty, rnd);
        }
        return candidate;
    }

    public BigInteger nextProbablePrime() {
        if (this.signum < 0)
            throw new ArithmeticException("start < 0: " + this);
        if ((this.signum == 0) || this.equals(ONE))
            return TWO;
        BigInteger result = this.add(ONE);
        if (result.bitLength() < SMALL_PRIME_THRESHOLD) {
            if (!result.testBit(0))
                result = result.add(ONE);
            while (true) {
                if (result.bitLength() > 6) {
                    long r = result.remainder(SMALL_PRIME_PRODUCT).longValue();
                    if ((r % 3 == 0) || (r % 5 == 0) || (r % 7 == 0) || (r % 11 == 0) || (r % 13 == 0) || (r % 17 == 0) || (r % 19 == 0) || (r % 23 == 0) || (r % 29 == 0) || (r % 31 == 0) || (r % 37 == 0) || (r % 41 == 0)) {
                        result = result.add(TWO);
                        continue;
                    }
                }
                if (result.bitLength() < 4)
                    return result;
                if (result.primeToCertainty(DEFAULT_PRIME_CERTAINTY, null))
                    return result;
                result = result.add(TWO);
            }
        }
        if (result.testBit(0))
            result = result.subtract(ONE);
        int searchLen = (result.bitLength() / 20) * 64;
        while (true) {
            BitSieve searchSieve = new BitSieve(result, searchLen);
            BigInteger candidate = searchSieve.retrieve(result, DEFAULT_PRIME_CERTAINTY, null);
            if (candidate != null)
                return candidate;
            result = result.add(BigInteger.valueOf(2 * searchLen));
        }
    }

    boolean primeToCertainty(int certainty, Random random) {
        int rounds = 0;
        int n = (Math.min(certainty, Integer.MAX_VALUE - 1) + 1) / 2;
        int sizeInBits = this.bitLength();
        if (sizeInBits < 100) {
            rounds = 50;
            rounds = n < rounds ? n : rounds;
            return passesMillerRabin(rounds, random);
        }
        if (sizeInBits < 256) {
            rounds = 27;
        } else if (sizeInBits < 512) {
            rounds = 15;
        } else if (sizeInBits < 768) {
            rounds = 8;
        } else if (sizeInBits < 1024) {
            rounds = 4;
        } else {
            rounds = 2;
        }
        rounds = n < rounds ? n : rounds;
        return passesMillerRabin(rounds, random) && passesLucasLehmer();
    }

    private boolean passesLucasLehmer() {
        BigInteger thisPlusOne = this.add(ONE);
        int d = 5;
        while (jacobiSymbol(d, this) != -1) {
            d = (d < 0) ? Math.abs(d) + 2 : -(d + 2);
        }
        BigInteger u = lucasLehmerSequence(d, thisPlusOne, this);
        return u.mod(this).equals(ZERO);
    }

    private static int jacobiSymbol(int p, BigInteger n) {
        if (p == 0)
            return 0;
        int j = 1;
        int u = n.mag[n.mag.length - 1];
        if (p < 0) {
            p = -p;
            int n8 = u & 7;
            if ((n8 == 3) || (n8 == 7))
                j = -j;
        }
        while ((p & 3) == 0) p >>= 2;
        if ((p & 1) == 0) {
            p >>= 1;
            if (((u ^ (u >> 1)) & 2) != 0)
                j = -j;
        }
        if (p == 1)
            return j;
        if ((p & u & 2) != 0)
            j = -j;
        u = n.mod(BigInteger.valueOf(p)).intValue();
        while (u != 0) {
            while ((u & 3) == 0) u >>= 2;
            if ((u & 1) == 0) {
                u >>= 1;
                if (((p ^ (p >> 1)) & 2) != 0)
                    j = -j;
            }
            if (u == 1)
                return j;
            assert (u < p);
            int t = u;
            u = p;
            p = t;
            if ((u & p & 2) != 0)
                j = -j;
            u %= p;
        }
        return 0;
    }

    private static BigInteger lucasLehmerSequence(int z, BigInteger k, BigInteger n) {
        BigInteger d = BigInteger.valueOf(z);
        BigInteger u = ONE;
        BigInteger u2;
        BigInteger v = ONE;
        BigInteger v2;
        for (int i = k.bitLength() - 2; i >= 0; i--) {
            u2 = u.multiply(v).mod(n);
            v2 = v.square().add(d.multiply(u.square())).mod(n);
            if (v2.testBit(0))
                v2 = v2.subtract(n);
            v2 = v2.shiftRight(1);
            u = u2;
            v = v2;
            if (k.testBit(i)) {
                u2 = u.add(v).mod(n);
                if (u2.testBit(0))
                    u2 = u2.subtract(n);
                u2 = u2.shiftRight(1);
                v2 = v.add(d.multiply(u)).mod(n);
                if (v2.testBit(0))
                    v2 = v2.subtract(n);
                v2 = v2.shiftRight(1);
                u = u2;
                v = v2;
            }
        }
        return u;
    }

    private static volatile Random staticRandom;

    private static Random getSecureRandom() {
        if (staticRandom == null) {
            staticRandom = new java.security.SecureRandom();
        }
        return staticRandom;
    }

    private boolean passesMillerRabin(int iterations, Random rnd) {
        BigInteger thisMinusOne = this.subtract(ONE);
        BigInteger m = thisMinusOne;
        int a = m.getLowestSetBit();
        m = m.shiftRight(a);
        if (rnd == null) {
            rnd = getSecureRandom();
        }
        for (int i = 0; i < iterations; i++) {
            BigInteger b;
            do {
                b = new BigInteger(this.bitLength(), rnd);
            } while (b.compareTo(ONE) <= 0 || b.compareTo(this) >= 0);
            int j = 0;
            BigInteger z = b.modPow(m, this);
            while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a)
                    return false;
                z = z.modPow(TWO, this);
            }
        }
        return true;
    }

    BigInteger(int[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = magnitude;
    }

    private BigInteger(byte[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = stripLeadingZeroBytes(magnitude);
    }

    public static BigInteger valueOf(long val) {
        if (val == 0)
            return ZERO;
        if (val > 0 && val <= MAX_CONSTANT)
            return posConst[(int) val];
        else if (val < 0 && val >= -MAX_CONSTANT)
            return negConst[(int) -val];
        return new BigInteger(val);
    }

    private BigInteger(long val) {
        if (val < 0) {
            val = -val;
            signum = -1;
        } else {
            signum = 1;
        }
        int highWord = (int) (val >>> 32);
        if (highWord == 0) {
            mag = new int[1];
            mag[0] = (int) val;
        } else {
            mag = new int[2];
            mag[0] = highWord;
            mag[1] = (int) val;
        }
    }

    private static BigInteger valueOf(int[] val) {
        return (val[0] > 0 ? new BigInteger(val, 1) : new BigInteger(val));
    }

    private final static int MAX_CONSTANT = 16;

    private static BigInteger[] posConst = new BigInteger[MAX_CONSTANT + 1];

    private static BigInteger[] negConst = new BigInteger[MAX_CONSTANT + 1];

    static {
        for (int i = 1; i <= MAX_CONSTANT; i++) {
            int[] magnitude = new int[1];
            magnitude[0] = i;
            posConst[i] = new BigInteger(magnitude, 1);
            negConst[i] = new BigInteger(magnitude, -1);
        }
    }

    public static final BigInteger ZERO = new BigInteger(new int[0], 0);

    public static final BigInteger ONE = valueOf(1);

    private static final BigInteger TWO = valueOf(2);

    public static final BigInteger TEN = valueOf(10);

    public BigInteger add(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val;
        if (val.signum == signum)
            return new BigInteger(add(mag, val.mag), signum);
        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag) : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    private static int[] add(int[] x, int[] y) {
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }
        int xIndex = x.length;
        int yIndex = y.length;
        int[] result = new int[xIndex];
        long sum = 0;
        while (yIndex > 0) {
            sum = (x[--xIndex] & LONG_MASK) + (y[--yIndex] & LONG_MASK) + (sum >>> 32);
            result[xIndex] = (int) sum;
        }
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry) carry = ((result[--xIndex] = x[xIndex] + 1) == 0);
        while (xIndex > 0) result[--xIndex] = x[xIndex];
        if (carry) {
            int[] bigger = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    public BigInteger subtract(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val.negate();
        if (val.signum != signum)
            return new BigInteger(add(mag, val.mag), signum);
        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag) : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int[] result = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0;
        while (littleIndex > 0) {
            difference = (big[--bigIndex] & LONG_MASK) - (little[--littleIndex] & LONG_MASK) + (difference >> 32);
            result[bigIndex] = (int) difference;
        }
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow) borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);
        while (bigIndex > 0) result[--bigIndex] = big[bigIndex];
        return result;
    }

    public BigInteger multiply(BigInteger val) {
        if (val.signum == 0 || signum == 0)
            return ZERO;
        int[] result = multiplyToLen(mag, mag.length, val.mag, val.mag.length, null);
        result = trustedStripLeadingZeroInts(result);
        return new BigInteger(result, signum == val.signum ? 1 : -1);
    }

    BigInteger multiply(long v) {
        if (v == 0 || signum == 0)
            return ZERO;
        if (v == BigDecimal.INFLATED)
            return multiply(BigInteger.valueOf(v));
        int rsign = (v > 0 ? signum : -signum);
        if (v < 0)
            v = -v;
        long dh = v >>> 32;
        long dl = v & LONG_MASK;
        int xlen = mag.length;
        int[] value = mag;
        int[] rmag = (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
        long carry = 0;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (value[i] & LONG_MASK) * dl + carry;
            rmag[rstart--] = (int) product;
            carry = product >>> 32;
        }
        rmag[rstart] = (int) carry;
        if (dh != 0L) {
            carry = 0;
            rstart = rmag.length - 2;
            for (int i = xlen - 1; i >= 0; i--) {
                long product = (value[i] & LONG_MASK) * dh + (rmag[rstart] & LONG_MASK) + carry;
                rmag[rstart--] = (int) product;
                carry = product >>> 32;
            }
            rmag[0] = (int) carry;
        }
        if (carry == 0L)
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        return new BigInteger(rmag, rsign);
    }

    private int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;
        if (z == null || z.length < (xlen + ylen))
            z = new int[xlen + ylen];
        long carry = 0;
        for (int j = ystart, k = ystart + 1 + xstart; j >= 0; j--, k--) {
            long product = (y[j] & LONG_MASK) * (x[xstart] & LONG_MASK) + carry;
            z[k] = (int) product;
            carry = product >>> 32;
        }
        z[xstart] = (int) carry;
        for (int i = xstart - 1; i >= 0; i--) {
            carry = 0;
            for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
                long product = (y[j] & LONG_MASK) * (x[i] & LONG_MASK) + (z[k] & LONG_MASK) + carry;
                z[k] = (int) product;
                carry = product >>> 32;
            }
            z[i] = (int) carry;
        }
        return z;
    }

    private BigInteger square() {
        if (signum == 0)
            return ZERO;
        int[] z = squareToLen(mag, mag.length, null);
        return new BigInteger(trustedStripLeadingZeroInts(z), 1);
    }

    private static final int[] squareToLen(int[] x, int len, int[] z) {
        int zlen = len << 1;
        if (z == null || z.length < zlen)
            z = new int[zlen];
        int lastProductLowWord = 0;
        for (int j = 0, i = 0; j < len; j++) {
            long piece = (x[j] & LONG_MASK);
            long product = piece * piece;
            z[i++] = (lastProductLowWord << 31) | (int) (product >>> 33);
            z[i++] = (int) (product >>> 1);
            lastProductLowWord = (int) product;
        }
        for (int i = len, offset = 1; i > 0; i--, offset += 2) {
            int t = x[i - 1];
            t = mulAdd(z, x, offset, i - 1, t);
            addOne(z, offset - 1, i, t);
        }
        primitiveLeftShift(z, zlen, 1);
        z[zlen - 1] |= x[len - 1] & 1;
        return z;
    }

    public BigInteger divide(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(), a = new MutableBigInteger(this.mag), b = new MutableBigInteger(val.mag);
        a.divide(b, q);
        return q.toBigInteger(this.signum == val.signum ? 1 : -1);
    }

    public BigInteger[] divideAndRemainder(BigInteger val) {
        BigInteger[] result = new BigInteger[2];
        MutableBigInteger q = new MutableBigInteger(), a = new MutableBigInteger(this.mag), b = new MutableBigInteger(val.mag);
        MutableBigInteger r = a.divide(b, q);
        result[0] = q.toBigInteger(this.signum == val.signum ? 1 : -1);
        result[1] = r.toBigInteger(this.signum);
        return result;
    }

    public BigInteger remainder(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(), a = new MutableBigInteger(this.mag), b = new MutableBigInteger(val.mag);
        return a.divide(b, q).toBigInteger(this.signum);
    }

    public BigInteger pow(int exponent) {
        if (exponent < 0)
            throw new ArithmeticException("Negative exponent");
        if (signum == 0)
            return (exponent == 0 ? ONE : this);
        int newSign = (signum < 0 && (exponent & 1) == 1 ? -1 : 1);
        int[] baseToPow2 = this.mag;
        int[] result = { 1 };
        while (exponent != 0) {
            if ((exponent & 1) == 1) {
                result = multiplyToLen(result, result.length, baseToPow2, baseToPow2.length, null);
                result = trustedStripLeadingZeroInts(result);
            }
            if ((exponent >>>= 1) != 0) {
                baseToPow2 = squareToLen(baseToPow2, baseToPow2.length, null);
                baseToPow2 = trustedStripLeadingZeroInts(baseToPow2);
            }
        }
        return new BigInteger(result, newSign);
    }

    public BigInteger gcd(BigInteger val) {
        if (val.signum == 0)
            return this.abs();
        else if (this.signum == 0)
            return val.abs();
        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger b = new MutableBigInteger(val);
        MutableBigInteger result = a.hybridGCD(b);
        return result.toBigInteger(1);
    }

    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        int bitsInHighWord = bitLengthForInt(a[0]);
        if (n <= (32 - bitsInHighWord)) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else {
            if (nBits <= (32 - bitsInHighWord)) {
                int[] result = new int[nInts + len];
                for (int i = 0; i < len; i++) result[i] = a[i];
                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                int[] result = new int[nInts + len + 1];
                for (int i = 0; i < len; i++) result[i] = a[i];
                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }

    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        for (int i = len - 1, c = a[i]; i > 0; i--) {
            int b = c;
            c = a[i - 1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] >>>= n;
    }

    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0)
            return;
        int n2 = 32 - n;
        for (int i = 0, c = a[i], m = i + len - 1; i < m; i++) {
            int b = c;
            c = a[i + 1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len - 1] <<= n;
    }

    private static int bitLength(int[] val, int len) {
        if (len == 0)
            return 0;
        return ((len - 1) << 5) + bitLengthForInt(val[0]);
    }

    public BigInteger abs() {
        return (signum >= 0 ? this : this.negate());
    }

    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }

    public int signum() {
        return this.signum;
    }

    public BigInteger mod(BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");
        BigInteger result = this.remainder(m);
        return (result.signum >= 0 ? result : result.add(m));
    }

    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");
        if (exponent.signum == 0)
            return (m.equals(ONE) ? ZERO : ONE);
        if (this.equals(ONE))
            return (m.equals(ONE) ? ZERO : ONE);
        if (this.equals(ZERO) && exponent.signum >= 0)
            return ZERO;
        if (this.equals(negConst[1]) && (!exponent.testBit(0)))
            return (m.equals(ONE) ? ZERO : ONE);
        boolean invertResult;
        if ((invertResult = (exponent.signum < 0)))
            exponent = exponent.negate();
        BigInteger base = (this.signum < 0 || this.compareTo(m) >= 0 ? this.mod(m) : this);
        BigInteger result;
        if (m.testBit(0)) {
            result = base.oddModPow(exponent, m);
        } else {
            int p = m.getLowestSetBit();
            BigInteger m1 = m.shiftRight(p);
            BigInteger m2 = ONE.shiftLeft(p);
            BigInteger base2 = (this.signum < 0 || this.compareTo(m1) >= 0 ? this.mod(m1) : this);
            BigInteger a1 = (m1.equals(ONE) ? ZERO : base2.oddModPow(exponent, m1));
            BigInteger a2 = base.modPow2(exponent, p);
            BigInteger y1 = m2.modInverse(m1);
            BigInteger y2 = m1.modInverse(m2);
            result = a1.multiply(m2).multiply(y1).add(a2.multiply(m1).multiply(y2)).mod(m);
        }
        return (invertResult ? result.modInverse(m) : result);
    }

    static int[] bnExpModThreshTable = { 7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE };

    private BigInteger oddModPow(BigInteger y, BigInteger z) {
        if (y.equals(ONE))
            return this;
        if (signum == 0)
            return ZERO;
        int[] base = mag.clone();
        int[] exp = y.mag;
        int[] mod = z.mag;
        int modLen = mod.length;
        int wbits = 0;
        int ebits = bitLength(exp, exp.length);
        if ((ebits != 17) || (exp[0] != 65537)) {
            while (ebits > bnExpModThreshTable[wbits]) {
                wbits++;
            }
        }
        int tblmask = 1 << wbits;
        int[][] table = new int[tblmask][];
        for (int i = 0; i < tblmask; i++) table[i] = new int[modLen];
        int inv = -MutableBigInteger.inverseMod32(mod[modLen - 1]);
        int[] a = leftShift(base, base.length, modLen << 5);
        MutableBigInteger q = new MutableBigInteger(), a2 = new MutableBigInteger(a), b2 = new MutableBigInteger(mod);
        MutableBigInteger r = a2.divide(b2, q);
        table[0] = r.toIntArray();
        if (table[0].length < modLen) {
            int offset = modLen - table[0].length;
            int[] t2 = new int[modLen];
            for (int i = 0; i < table[0].length; i++) t2[i + offset] = table[0][i];
            table[0] = t2;
        }
        int[] b = squareToLen(table[0], modLen, null);
        b = montReduce(b, mod, modLen, inv);
        int[] t = new int[modLen];
        for (int i = 0; i < modLen; i++) t[i] = b[i];
        for (int i = 1; i < tblmask; i++) {
            int[] prod = multiplyToLen(t, modLen, table[i - 1], modLen, null);
            table[i] = montReduce(prod, mod, modLen, inv);
        }
        int bitpos = 1 << ((ebits - 1) & (32 - 1));
        int buf = 0;
        int elen = exp.length;
        int eIndex = 0;
        for (int i = 0; i <= wbits; i++) {
            buf = (buf << 1) | (((exp[eIndex] & bitpos) != 0) ? 1 : 0);
            bitpos >>>= 1;
            if (bitpos == 0) {
                eIndex++;
                bitpos = 1 << (32 - 1);
                elen--;
            }
        }
        int multpos = ebits;
        ebits--;
        boolean isone = true;
        multpos = ebits - wbits;
        while ((buf & 1) == 0) {
            buf >>>= 1;
            multpos++;
        }
        int[] mult = table[buf >>> 1];
        buf = 0;
        if (multpos == ebits)
            isone = false;
        while (true) {
            ebits--;
            buf <<= 1;
            if (elen != 0) {
                buf |= ((exp[eIndex] & bitpos) != 0) ? 1 : 0;
                bitpos >>>= 1;
                if (bitpos == 0) {
                    eIndex++;
                    bitpos = 1 << (32 - 1);
                    elen--;
                }
            }
            if ((buf & tblmask) != 0) {
                multpos = ebits - wbits;
                while ((buf & 1) == 0) {
                    buf >>>= 1;
                    multpos++;
                }
                mult = table[buf >>> 1];
                buf = 0;
            }
            if (ebits == multpos) {
                if (isone) {
                    b = mult.clone();
                    isone = false;
                } else {
                    t = b;
                    a = multiplyToLen(t, modLen, mult, modLen, a);
                    a = montReduce(a, mod, modLen, inv);
                    t = a;
                    a = b;
                    b = t;
                }
            }
            if (ebits == 0)
                break;
            if (!isone) {
                t = b;
                a = squareToLen(t, modLen, a);
                a = montReduce(a, mod, modLen, inv);
                t = a;
                a = b;
                b = t;
            }
        }
        int[] t2 = new int[2 * modLen];
        for (int i = 0; i < modLen; i++) t2[i + modLen] = b[i];
        b = montReduce(t2, mod, modLen, inv);
        t2 = new int[modLen];
        for (int i = 0; i < modLen; i++) t2[i] = b[i];
        return new BigInteger(1, t2);
    }

    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c = 0;
        int len = mlen;
        int offset = 0;
        do {
            int nEnd = n[n.length - 1 - offset];
            int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += addOne(n, offset, mlen, carry);
            offset++;
        } while (--len > 0);
        while (c > 0) c += subN(n, mod, mlen);
        while (intArrayCmpToLen(n, mod, mlen) >= 0) subN(n, mod, mlen);
        return n;
    }

    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for (int i = 0; i < len; i++) {
            long b1 = arg1[i] & LONG_MASK;
            long b2 = arg2[i] & LONG_MASK;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    private static int subN(int[] a, int[] b, int len) {
        long sum = 0;
        while (--len >= 0) {
            sum = (a[len] & LONG_MASK) - (b[len] & LONG_MASK) + (sum >> 32);
            a[len] = (int) sum;
        }
        return (int) (sum >> 32);
    }

    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = k & LONG_MASK;
        long carry = 0;
        offset = out.length - offset - 1;
        for (int j = len - 1; j >= 0; j--) {
            long product = (in[j] & LONG_MASK) * kLong + (out[offset] & LONG_MASK) + carry;
            out[offset--] = (int) product;
            carry = product >>> 32;
        }
        return (int) carry;
    }

    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length - 1 - mlen - offset;
        long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);
        a[offset] = (int) t;
        if ((t >>> 32) == 0)
            return 0;
        while (--mlen >= 0) {
            if (--offset < 0) {
                return 1;
            } else {
                a[offset]++;
                if (a[offset] != 0)
                    return 0;
            }
        }
        return 1;
    }

    private BigInteger modPow2(BigInteger exponent, int p) {
        BigInteger result = valueOf(1);
        BigInteger baseToPow2 = this.mod2(p);
        int expOffset = 0;
        int limit = exponent.bitLength();
        if (this.testBit(0))
            limit = (p - 1) < limit ? (p - 1) : limit;
        while (expOffset < limit) {
            if (exponent.testBit(expOffset))
                result = result.multiply(baseToPow2).mod2(p);
            expOffset++;
            if (expOffset < limit)
                baseToPow2 = baseToPow2.square().mod2(p);
        }
        return result;
    }

    private BigInteger mod2(int p) {
        if (bitLength() <= p)
            return this;
        int numInts = (p + 31) >>> 5;
        int[] mag = new int[numInts];
        for (int i = 0; i < numInts; i++) mag[i] = this.mag[i + (this.mag.length - numInts)];
        int excessBits = (numInts << 5) - p;
        mag[0] &= (1L << (32 - excessBits)) - 1;
        return (mag[0] == 0 ? new BigInteger(1, mag) : new BigInteger(mag, 1));
    }

    public BigInteger modInverse(BigInteger m) {
        if (m.signum != 1)
            throw new ArithmeticException("BigInteger: modulus not positive");
        if (m.equals(ONE))
            return ZERO;
        BigInteger modVal = this;
        if (signum < 0 || (this.compareMagnitude(m) >= 0))
            modVal = this.mod(m);
        if (modVal.equals(ONE))
            return ONE;
        MutableBigInteger a = new MutableBigInteger(modVal);
        MutableBigInteger b = new MutableBigInteger(m);
        MutableBigInteger result = a.mutableModInverse(b);
        return result.toBigInteger(1);
    }

    public BigInteger shiftLeft(int n) {
        if (signum == 0)
            return ZERO;
        if (n == 0)
            return this;
        if (n < 0) {
            if (n == Integer.MIN_VALUE) {
                throw new ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.");
            } else {
                return shiftRight(-n);
            }
        }
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int[] newMag = null;
        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            for (int i = 0; i < magLen; i++) newMag[i] = mag[i];
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int j = 0;
            while (j < magLen - 1) newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
            newMag[i] = mag[j] << nBits;
        }
        return new BigInteger(newMag, signum);
    }

    public BigInteger shiftRight(int n) {
        if (n == 0)
            return this;
        if (n < 0) {
            if (n == Integer.MIN_VALUE) {
                throw new ArithmeticException("Shift distance of Integer.MIN_VALUE not supported.");
            } else {
                return shiftLeft(-n);
            }
        }
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int[] newMag = null;
        if (nInts >= magLen)
            return (signum >= 0 ? ZERO : negConst[1]);
        if (nBits == 0) {
            int newMagLen = magLen - nInts;
            newMag = new int[newMagLen];
            for (int i = 0; i < newMagLen; i++) newMag[i] = mag[i];
        } else {
            int i = 0;
            int highBits = mag[0] >>> nBits;
            if (highBits != 0) {
                newMag = new int[magLen - nInts];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen - nInts - 1];
            }
            int nBits2 = 32 - nBits;
            int j = 0;
            while (j < magLen - nInts - 1) newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
        }
        if (signum < 0) {
            boolean onesLost = false;
            for (int i = magLen - 1, j = magLen - nInts; i >= j && !onesLost; i--) onesLost = (mag[i] != 0);
            if (!onesLost && nBits != 0)
                onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);
            if (onesLost)
                newMag = javaIncrement(newMag);
        }
        return new BigInteger(newMag, signum);
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;
        for (int i = val.length - 1; i >= 0 && lastSum == 0; i--) lastSum = (val[i] += 1);
        if (lastSum == 0) {
            val = new int[val.length + 1];
            val[0] = 1;
        }
        return val;
    }

    public BigInteger and(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++) result[i] = (getInt(result.length - i - 1) & val.getInt(result.length - i - 1));
        return valueOf(result);
    }

    public BigInteger or(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++) result[i] = (getInt(result.length - i - 1) | val.getInt(result.length - i - 1));
        return valueOf(result);
    }

    public BigInteger xor(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++) result[i] = (getInt(result.length - i - 1) ^ val.getInt(result.length - i - 1));
        return valueOf(result);
    }

    public BigInteger not() {
        int[] result = new int[intLength()];
        for (int i = 0; i < result.length; i++) result[i] = ~getInt(result.length - i - 1);
        return valueOf(result);
    }

    public BigInteger andNot(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i = 0; i < result.length; i++) result[i] = (getInt(result.length - i - 1) & ~val.getInt(result.length - i - 1));
        return valueOf(result);
    }

    public boolean testBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");
        return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
    }

    public BigInteger setBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");
        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum + 2)];
        for (int i = 0; i < result.length; i++) result[result.length - i - 1] = getInt(i);
        result[result.length - intNum - 1] |= (1 << (n & 31));
        return valueOf(result);
    }

    public BigInteger clearBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");
        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), ((n + 1) >>> 5) + 1)];
        for (int i = 0; i < result.length; i++) result[result.length - i - 1] = getInt(i);
        result[result.length - intNum - 1] &= ~(1 << (n & 31));
        return valueOf(result);
    }

    public BigInteger flipBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");
        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum + 2)];
        for (int i = 0; i < result.length; i++) result[result.length - i - 1] = getInt(i);
        result[result.length - intNum - 1] ^= (1 << (n & 31));
        return valueOf(result);
    }

    public int getLowestSetBit() {
        @SuppressWarnings("deprecation")
        int lsb = lowestSetBit - 2;
        if (lsb == -2) {
            lsb = 0;
            if (signum == 0) {
                lsb -= 1;
            } else {
                int i, b;
                for (i = 0; (b = getInt(i)) == 0; i++) ;
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            lowestSetBit = lsb + 2;
        }
        return lsb;
    }

    public int bitLength() {
        @SuppressWarnings("deprecation")
        int n = bitLength - 1;
        if (n == -1) {
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0;
            } else {
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                if (signum < 0) {
                    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                    for (int i = 1; i < len && pow2; i++) pow2 = (mag[i] == 0);
                    n = (pow2 ? magBitLength - 1 : magBitLength);
                } else {
                    n = magBitLength;
                }
            }
            bitLength = n + 1;
        }
        return n;
    }

    public int bitCount() {
        @SuppressWarnings("deprecation")
        int bc = bitCount - 1;
        if (bc == -1) {
            bc = 0;
            for (int i = 0; i < mag.length; i++) bc += Integer.bitCount(mag[i]);
            if (signum < 0) {
                int magTrailingZeroCount = 0, j;
                for (j = mag.length - 1; mag[j] == 0; j--) magTrailingZeroCount += 32;
                magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j]);
                bc += magTrailingZeroCount - 1;
            }
            bitCount = bc + 1;
        }
        return bc;
    }

    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0)
            return true;
        BigInteger w = this.abs();
        if (w.equals(TWO))
            return true;
        if (!w.testBit(0) || w.equals(ONE))
            return false;
        return w.primeToCertainty(certainty, null);
    }

    public int compareTo(BigInteger val) {
        if (signum == val.signum) {
            switch(signum) {
                case 1:
                    return compareMagnitude(val);
                case -1:
                    return val.compareMagnitude(this);
                default:
                    return 0;
            }
        }
        return signum > val.signum ? 1 : -1;
    }

    final int compareMagnitude(BigInteger val) {
        int[] m1 = mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2)
            return -1;
        if (len1 > len2)
            return 1;
        for (int i = 0; i < len1; i++) {
            int a = m1[i];
            int b = m2[i];
            if (a != b)
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
        }
        return 0;
    }

    public boolean equals(Object x) {
        if (x == this)
            return true;
        if (!(x instanceof BigInteger))
            return false;
        BigInteger xInt = (BigInteger) x;
        if (xInt.signum != signum)
            return false;
        int[] m = mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length)
            return false;
        for (int i = 0; i < len; i++) if (xm[i] != m[i])
            return false;
        return true;
    }

    public BigInteger min(BigInteger val) {
        return (compareTo(val) < 0 ? this : val);
    }

    public BigInteger max(BigInteger val) {
        return (compareTo(val) > 0 ? this : val);
    }

    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i < mag.length; i++) hashCode = (int) (31 * hashCode + (mag[i] & LONG_MASK));
        return hashCode * signum;
    }

    public String toString(int radix) {
        if (signum == 0)
            return "0";
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
        int maxNumDigitGroups = (4 * mag.length + 6) / 7;
        String[] digitGroup = new String[maxNumDigitGroups];
        BigInteger tmp = this.abs();
        int numGroups = 0;
        while (tmp.signum != 0) {
            BigInteger d = longRadix[radix];
            MutableBigInteger q = new MutableBigInteger(), a = new MutableBigInteger(tmp.mag), b = new MutableBigInteger(d.mag);
            MutableBigInteger r = a.divide(b, q);
            BigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
            BigInteger r2 = r.toBigInteger(tmp.signum * d.signum);
            digitGroup[numGroups++] = Long.toString(r2.longValue(), radix);
            tmp = q2;
        }
        StringBuilder buf = new StringBuilder(numGroups * digitsPerLong[radix] + 1);
        if (signum < 0)
            buf.append('-');
        buf.append(digitGroup[numGroups - 1]);
        for (int i = numGroups - 2; i >= 0; i--) {
            int numLeadingZeros = digitsPerLong[radix] - digitGroup[i].length();
            if (numLeadingZeros != 0)
                buf.append(zeros[numLeadingZeros]);
            buf.append(digitGroup[i]);
        }
        return buf.toString();
    }

    private static String[] zeros = new String[64];

    static {
        zeros[63] = "000000000000000000000000000000000000000000000000000000000000000";
        for (int i = 0; i < 63; i++) zeros[i] = zeros[63].substring(0, i);
    }

    public String toString() {
        return toString(10);
    }

    public byte[] toByteArray() {
        int byteLen = bitLength() / 8 + 1;
        byte[] byteArray = new byte[byteLen];
        for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            byteArray[i] = (byte) nextInt;
        }
        return byteArray;
    }

    public int intValue() {
        int result = 0;
        result = getInt(0);
        return result;
    }

    public long longValue() {
        long result = 0;
        for (int i = 1; i >= 0; i--) result = (result << 32) + (getInt(i) & LONG_MASK);
        return result;
    }

    public float floatValue() {
        return Float.parseFloat(this.toString());
    }

    public double doubleValue() {
        return Double.parseDouble(this.toString());
    }

    private static int[] stripLeadingZeroInts(int[] val) {
        int vlen = val.length;
        int keep;
        for (keep = 0; keep < vlen && val[keep] == 0; keep++) ;
        return java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] trustedStripLeadingZeroInts(int[] val) {
        int vlen = val.length;
        int keep;
        for (keep = 0; keep < vlen && val[keep] == 0; keep++) ;
        return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    private static int[] stripLeadingZeroBytes(byte[] a) {
        int byteLength = a.length;
        int keep;
        for (keep = 0; keep < byteLength && a[keep] == 0; keep++) ;
        int intLength = ((byteLength - keep) + 3) >>> 2;
        int[] result = new int[intLength];
        int b = byteLength - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);
            for (int j = 8; j <= (bytesToTransfer << 3); j += 8) result[i] |= ((a[b--] & 0xff) << j);
        }
        return result;
    }

    private static int[] makePositive(byte[] a) {
        int keep, k;
        int byteLength = a.length;
        for (keep = 0; keep < byteLength && a[keep] == -1; keep++) ;
        for (k = keep; k < byteLength && a[k] == 0; k++) ;
        int extraByte = (k == byteLength) ? 1 : 0;
        int intLength = ((byteLength - keep + extraByte) + 3) / 4;
        int[] result = new int[intLength];
        int b = byteLength - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int numBytesToTransfer = Math.min(3, b - keep + 1);
            if (numBytesToTransfer < 0)
                numBytesToTransfer = 0;
            for (int j = 8; j <= 8 * numBytesToTransfer; j += 8) result[i] |= ((a[b--] & 0xff) << j);
            int mask = -1 >>> (8 * (3 - numBytesToTransfer));
            result[i] = ~result[i] & mask;
        }
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (int) ((result[i] & LONG_MASK) + 1);
            if (result[i] != 0)
                break;
        }
        return result;
    }

    private static int[] makePositive(int[] a) {
        int keep, j;
        for (keep = 0; keep < a.length && a[keep] == -1; keep++) ;
        for (j = keep; j < a.length && a[j] == 0; j++) ;
        int extraInt = (j == a.length ? 1 : 0);
        int[] result = new int[a.length - keep + extraInt];
        for (int i = keep; i < a.length; i++) result[i - keep + extraInt] = ~a[i];
        for (int i = result.length - 1; ++result[i] == 0; i--) ;
        return result;
    }

    private static int[] digitsPerLong = { 0, 0, 62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12 };

    private static BigInteger[] longRadix = { null, null, valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL), valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL), valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L), valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L), valueOf(0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L), valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL), valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L), valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L), valueOf(0x5da0e1e53c5c8000L), valueOf(0xb16a458ef403f19L), valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L), valueOf(0x5658597bcaa24000L), valueOf(0x6feb266931a75b7L), valueOf(0xc29e98000000000L), valueOf(0x14adf4b7320334b9L), valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL), valueOf(0x5a3c23e39c000000L), valueOf(0x4e900abb53e6b71L), valueOf(0x7600ec618141000L), valueOf(0xaee5720ee830681L), valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L), valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L), valueOf(0x41c21cb8e1000000L) };

    private static int[] digitsPerInt = { 0, 0, 30, 19, 15, 13, 11, 11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5 };

    private static int[] intRadix = { 0, 0, 0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800, 0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61, 0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f, 0x10000000, 0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d, 0x6c20a40, 0x8d2d931, 0xb640000, 0xe8d4a51, 0x1269ae40, 0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41, 0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400 };

    private int intLength() {
        return (bitLength() >>> 5) + 1;
    }

    private int signBit() {
        return signum < 0 ? 1 : 0;
    }

    private int signInt() {
        return signum < 0 ? -1 : 0;
    }

    private int getInt(int n) {
        if (n < 0)
            return 0;
        if (n >= mag.length)
            return signInt();
        int magInt = mag[mag.length - n - 1];
        return (signum >= 0 ? magInt : (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    private int firstNonzeroIntNum() {
        int fn = firstNonzeroIntNum - 2;
        if (fn == -2) {
            fn = 0;
            int i;
            int mlen = mag.length;
            for (i = mlen - 1; i >= 0 && mag[i] == 0; i--) ;
            fn = mlen - i - 1;
            firstNonzeroIntNum = fn + 2;
        }
        return fn;
    }

    private static final long serialVersionUID = -8287574255936472291L;

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("signum", Integer.TYPE), new ObjectStreamField("magnitude", byte[].class), new ObjectStreamField("bitCount", Integer.TYPE), new ObjectStreamField("bitLength", Integer.TYPE), new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE), new ObjectStreamField("lowestSetBit", Integer.TYPE) };

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        int sign = fields.get("signum", -2);
        byte[] magnitude = (byte[]) fields.get("magnitude", null);
        if (sign < -1 || sign > 1) {
            String message = "BigInteger: Invalid signum value";
            if (fields.defaulted("signum"))
                message = "BigInteger: Signum not present in stream";
            throw new java.io.StreamCorruptedException(message);
        }
        if ((magnitude.length == 0) != (sign == 0)) {
            String message = "BigInteger: signum-magnitude mismatch";
            if (fields.defaulted("magnitude"))
                message = "BigInteger: Magnitude not present in stream";
            throw new java.io.StreamCorruptedException(message);
        }
        unsafe.putIntVolatile(this, signumOffset, sign);
        unsafe.putObjectVolatile(this, magOffset, stripLeadingZeroBytes(magnitude));
    }

    private static final sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();

    private static final long signumOffset;

    private static final long magOffset;

    static {
        try {
            signumOffset = unsafe.objectFieldOffset(BigInteger.class.getDeclaredField("signum"));
            magOffset = unsafe.objectFieldOffset(BigInteger.class.getDeclaredField("mag"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", signum);
        fields.put("magnitude", magSerializedForm());
        fields.put("bitCount", -1);
        fields.put("bitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);
        s.writeFields();
    }

    private byte[] magSerializedForm() {
        int len = mag.length;
        int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];
        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte) nextInt;
        }
        return result;
    }
}
