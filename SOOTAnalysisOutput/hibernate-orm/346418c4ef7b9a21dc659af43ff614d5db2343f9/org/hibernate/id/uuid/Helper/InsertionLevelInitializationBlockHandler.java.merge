package org.hibernate.id.uuid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.hibernate.internal.util.BytesHelper;

public class Helper {

    private static final byte[] ADDRESS_BYTES;

    private static final int ADDRESS_INT;

    private static final String ADDRESS_HEX_STRING;

    static {
        byte[] address;
        try {
            address = InetAddress.getLocalHost().getAddress();
        } catch (Exception e) {
            address = new byte[4];
        }
        ADDRESS_BYTES = address;
        ADDRESS_INT = BytesHelper.toInt(ADDRESS_BYTES);
        ADDRESS_HEX_STRING = format(ADDRESS_INT);
    }

    public static byte[] getAddressBytes() {
        return ADDRESS_BYTES;
    }

    public static int getAddressInt() {
        return ADDRESS_INT;
    }

    public static String getAddressHexString() {
        return ADDRESS_HEX_STRING;
    }

    private static final byte[] JVM_IDENTIFIER_BYTES;

    private static final int JVM_IDENTIFIER_INT;

    private static final String JVM_IDENTIFIER_HEX_STRING;

    static {
        JVM_IDENTIFIER_INT = (int) (System.currentTimeMillis() >>> 8);
        JVM_IDENTIFIER_BYTES = BytesHelper.fromInt(JVM_IDENTIFIER_INT);
        JVM_IDENTIFIER_HEX_STRING = format(JVM_IDENTIFIER_INT);
    }

    public static byte[] getJvmIdentifierBytes() {
        return JVM_IDENTIFIER_BYTES;
    }

    public static int getJvmIdentifierInt() {
        return JVM_IDENTIFIER_INT;
    }

    public static String getJvmIdentifierHexString() {
        return JVM_IDENTIFIER_HEX_STRING;
    }

    private static short counter = (short) 0;

    public static short getCountShort() {
        synchronized (Helper.class) {
            if (counter < 0) {
                counter = 0;
            }
            return counter++;
        }
    }

    public static byte[] getCountBytes() {
        return BytesHelper.fromShort(getCountShort());
    }

    public static String format(int value) {
        final String formatted = Integer.toHexString(value);
        StringBuffer buf = new StringBuffer("00000000".intern());
        buf.replace(8 - formatted.length(), 8, formatted);
        return buf.toString();
    }

    public static String format(short value) {
        String formatted = Integer.toHexString(value);
        StringBuffer buf = new StringBuffer("0000");
        buf.replace(4 - formatted.length(), 4, formatted);
        return buf.toString();
    }

    public static void main(String[] args) throws UnknownHostException {
        byte[] addressBytes = InetAddress.getLocalHost().getAddress();
        System.out.println("Raw ip address bytes : " + addressBytes.toString());
        int addressInt = BytesHelper.toInt(addressBytes);
        System.out.println("ip address int : " + addressInt);
        String formatted = Integer.toHexString(addressInt);
        StringBuffer buf = new StringBuffer("00000000");
        buf.replace(8 - formatted.length(), 8, formatted);
        String addressHex = buf.toString();
        System.out.println("ip address hex : " + addressHex);
    }
}