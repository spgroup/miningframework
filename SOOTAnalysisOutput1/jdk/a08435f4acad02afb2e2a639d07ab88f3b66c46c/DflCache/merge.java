package sun.security.krb5.internal.rcache;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.AccessController;
import java.util.*;
import sun.security.action.GetPropertyAction;
import sun.security.krb5.internal.KerberosTime;
import sun.security.krb5.internal.Krb5;
import sun.security.krb5.internal.KrbApErrException;
import sun.security.krb5.internal.ReplayCache;

public class DflCache extends ReplayCache {

    private static final int KRB5_RV_VNO = 0x501;

    private static final int EXCESSREPS = 30;

    private final String source;

    private static int uid;

    static {
        try {
            Class<?> clazz = Class.forName("com.sun.security.auth.module.UnixSystem");
            uid = (int) (long) (Long) clazz.getMethod("getUid").invoke(clazz.newInstance());
        } catch (Exception e) {
            uid = -1;
        }
    }

    public DflCache(String source) {
        this.source = source;
    }

    private static String defaultPath() {
        return AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir"));
    }

    private static String defaultFile(String server) {
        int slash = server.indexOf('/');
        if (slash == -1) {
            slash = server.indexOf('@');
        }
        if (slash != -1) {
            server = server.substring(0, slash);
        }
        if (uid != -1) {
            server += "_" + uid;
        }
        return server;
    }

    private static Path getFileName(String source, String server) {
        String path, file;
        if (source.equals("dfl")) {
            path = defaultPath();
            file = defaultFile(server);
        } else if (source.startsWith("dfl:")) {
            source = source.substring(4);
            int pos = source.lastIndexOf('/');
            int pos1 = source.lastIndexOf('\\');
            if (pos1 > pos)
                pos = pos1;
            if (pos == -1) {
                path = defaultPath();
                file = source;
            } else if (new File(source).isDirectory()) {
                path = source;
                file = defaultFile(server);
            } else {
                path = null;
                file = source;
            }
        } else {
            throw new IllegalArgumentException();
        }
        return new File(path, file).toPath();
    }

    @Override
    public void checkAndStore(KerberosTime currTime, AuthTimeWithHash time) throws KrbApErrException {
        try {
            checkAndStore0(currTime, time);
        } catch (IOException ioe) {
            KrbApErrException ke = new KrbApErrException(Krb5.KRB_ERR_GENERIC);
            ke.initCause(ioe);
            throw ke;
        }
    }

    private synchronized void checkAndStore0(KerberosTime currTime, AuthTimeWithHash time) throws IOException, KrbApErrException {
        Path p = getFileName(source, time.server);
        int missed = 0;
        try (Storage s = new Storage()) {
            try {
                missed = s.loadAndCheck(p, time, currTime);
            } catch (IOException ioe) {
                Storage.create(p);
                missed = s.loadAndCheck(p, time, currTime);
            }
            s.append(time);
        }
        if (missed > EXCESSREPS) {
            Storage.expunge(p, currTime);
        }
    }

    private static class Storage implements Closeable {

        @SuppressWarnings("try")
        private static void create(Path p) throws IOException {
            try (SeekableByteChannel newChan = createNoClose(p)) {
            }
            makeMine(p);
        }

        private static void makeMine(Path p) throws IOException {
            try {
                Set<PosixFilePermission> attrs = new HashSet<>();
                attrs.add(PosixFilePermission.OWNER_READ);
                attrs.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(p, attrs);
            } catch (UnsupportedOperationException uoe) {
            }
        }

        private static SeekableByteChannel createNoClose(Path p) throws IOException {
            SeekableByteChannel newChan = Files.newByteChannel(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            ByteBuffer buffer = ByteBuffer.allocate(6);
            buffer.putShort((short) KRB5_RV_VNO);
            buffer.order(ByteOrder.nativeOrder());
            buffer.putInt(KerberosTime.getDefaultSkew());
            buffer.flip();
            newChan.write(buffer);
            return newChan;
        }

        private static void expunge(Path p, KerberosTime currTime) throws IOException {
            Path p2 = Files.createTempFile(p.getParent(), "rcache", null);
            try (SeekableByteChannel oldChan = Files.newByteChannel(p);
                SeekableByteChannel newChan = createNoClose(p2)) {
                long timeLimit = currTime.getSeconds() - readHeader(oldChan);
                while (true) {
                    try {
                        AuthTime at = AuthTime.readFrom(oldChan);
                        if (at.ctime > timeLimit) {
                            ByteBuffer bb = ByteBuffer.wrap(at.encode(true));
                            newChan.write(bb);
                        }
                    } catch (BufferUnderflowException e) {
                        break;
                    }
                }
            }
            makeMine(p2);
            Files.move(p2, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }

        SeekableByteChannel chan;

        private int loadAndCheck(Path p, AuthTimeWithHash time, KerberosTime currTime) throws IOException, KrbApErrException {
            int missed = 0;
            if (Files.isSymbolicLink(p)) {
                throw new IOException("Symlink not accepted");
            }
            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(p);
                if (uid != -1 && (Integer) Files.getAttribute(p, "unix:uid") != uid) {
                    throw new IOException("Not mine");
                }
                if (perms.contains(PosixFilePermission.GROUP_READ) || perms.contains(PosixFilePermission.GROUP_WRITE) || perms.contains(PosixFilePermission.GROUP_EXECUTE) || perms.contains(PosixFilePermission.OTHERS_READ) || perms.contains(PosixFilePermission.OTHERS_WRITE) || perms.contains(PosixFilePermission.OTHERS_EXECUTE)) {
                    throw new IOException("Accessible by someone else");
                }
            } catch (UnsupportedOperationException uoe) {
            }
            chan = Files.newByteChannel(p, StandardOpenOption.WRITE, StandardOpenOption.READ);
            long timeLimit = currTime.getSeconds() - readHeader(chan);
            long pos = 0;
            boolean seeNewButNotSame = false;
            while (true) {
                try {
                    pos = chan.position();
                    AuthTime a = AuthTime.readFrom(chan);
                    if (a instanceof AuthTimeWithHash) {
                        if (time.equals(a)) {
                            throw new KrbApErrException(Krb5.KRB_AP_ERR_REPEAT);
                        } else if (time.isSameIgnoresHash(a)) {
                            seeNewButNotSame = true;
                        }
                    } else {
                        if (time.isSameIgnoresHash(a)) {
                            if (!seeNewButNotSame) {
                                throw new KrbApErrException(Krb5.KRB_AP_ERR_REPEAT);
                            }
                        }
                    }
                    if (a.ctime < timeLimit) {
                        missed++;
                    } else {
                        missed--;
                    }
                } catch (BufferUnderflowException e) {
                    chan.position(pos);
                    break;
                }
            }
            return missed;
        }

        private static int readHeader(SeekableByteChannel chan) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(6);
            chan.read(bb);
            if (bb.getShort(0) != KRB5_RV_VNO) {
                throw new IOException("Not correct rcache version");
            }
            bb.order(ByteOrder.nativeOrder());
            return bb.getInt(2);
        }

        private void append(AuthTimeWithHash at) throws IOException {
            ByteBuffer bb;
            bb = ByteBuffer.wrap(at.encode(true));
            chan.write(bb);
            bb = ByteBuffer.wrap(at.encode(false));
            chan.write(bb);
        }

        @Override
        public void close() throws IOException {
            if (chan != null)
                chan.close();
            chan = null;
        }
    }
}
