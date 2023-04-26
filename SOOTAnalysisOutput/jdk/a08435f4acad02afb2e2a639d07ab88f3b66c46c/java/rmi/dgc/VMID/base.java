package java.rmi.dgc;

import java.io.*;
import java.net.*;
import java.rmi.server.UID;
import java.security.*;

public final class VMID implements java.io.Serializable {

    private static byte[] localAddr = computeAddressHash();

    private byte[] addr;

    private UID uid;

    private static final long serialVersionUID = -538642295484486218L;

    public VMID() {
        addr = localAddr;
        uid = new UID();
    }

    @Deprecated
    public static boolean isUnique() {
        return true;
    }

    public int hashCode() {
        return uid.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof VMID) {
            VMID vmid = (VMID) obj;
            if (!uid.equals(vmid.uid))
                return false;
            if ((addr == null) ^ (vmid.addr == null))
                return false;
            if (addr != null) {
                if (addr.length != vmid.addr.length)
                    return false;
                for (int i = 0; i < addr.length; ++i) if (addr[i] != vmid.addr[i])
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        if (addr != null)
            for (int i = 0; i < addr.length; ++i) {
                int x = addr[i] & 0xFF;
                result.append((x < 0x10 ? "0" : "") + Integer.toString(x, 16));
            }
        result.append(':');
        result.append(uid.toString());
        return result.toString();
    }

    private static byte[] computeAddressHash() {
        byte[] addr = java.security.AccessController.doPrivileged(new PrivilegedAction<byte[]>() {

            public byte[] run() {
                try {
                    return InetAddress.getLocalHost().getAddress();
                } catch (Exception e) {
                }
                return new byte[] { 0, 0, 0, 0 };
            }
        });
        byte[] addrHash;
        final int ADDR_HASH_LENGTH = 8;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            ByteArrayOutputStream sink = new ByteArrayOutputStream(64);
            DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, md));
            out.write(addr, 0, addr.length);
            out.flush();
            byte[] digest = md.digest();
            int hashlength = Math.min(ADDR_HASH_LENGTH, digest.length);
            addrHash = new byte[hashlength];
            System.arraycopy(digest, 0, addrHash, 0, hashlength);
        } catch (IOException ignore) {
            addrHash = new byte[0];
        } catch (NoSuchAlgorithmException complain) {
            throw new InternalError(complain.toString(), complain);
        }
        return addrHash;
    }
}
