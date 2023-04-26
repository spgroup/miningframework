package sun.nio.ch.sctp;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.spi.SelectorProvider;
import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.IllegalReceiveException;
import com.sun.nio.sctp.InvalidStreamException;
import com.sun.nio.sctp.IllegalUnbindException;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpMultiChannel;
import com.sun.nio.sctp.SctpSocketOption;
import sun.nio.ch.DirectBuffer;
import sun.nio.ch.NativeThread;
import sun.nio.ch.IOStatus;
import sun.nio.ch.IOUtil;
import sun.nio.ch.Net;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.Util;
import static com.sun.nio.sctp.SctpStandardSocketOptions.*;
import static sun.nio.ch.sctp.ResultContainer.*;

public class SctpMultiChannelImpl extends SctpMultiChannel implements SelChImpl {

    private final FileDescriptor fd;

    private final int fdVal;

    private volatile long receiverThread = 0;

    private volatile long senderThread = 0;

    private final Object receiveLock = new Object();

    private final Object sendLock = new Object();

    private final Object stateLock = new Object();

    private enum ChannelState {

        UNINITIALIZED, KILLPENDING, KILLED
    }

    private ChannelState state = ChannelState.UNINITIALIZED;

    int port = -1;

    private HashSet<InetSocketAddress> localAddresses = new HashSet<InetSocketAddress>();

    private boolean wildcard;

    private HashMap<SocketAddress, Association> addressMap = new HashMap<SocketAddress, Association>();

    private HashMap<Association, Set<SocketAddress>> associationMap = new HashMap<Association, Set<SocketAddress>>();

    private final ThreadLocal<Association> associationToRemove = new ThreadLocal<Association>() {

        @Override
        protected Association initialValue() {
            return null;
        }
    };

    private final ThreadLocal<Boolean> receiveInvoked = new ThreadLocal<Boolean>() {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public SctpMultiChannelImpl(SelectorProvider provider) throws IOException {
        super(provider);
        this.fd = SctpNet.socket(false);
        this.fdVal = IOUtil.fdVal(fd);
    }

    @Override
    public SctpMultiChannel bind(SocketAddress local, int backlog) throws IOException {
        synchronized (receiveLock) {
            synchronized (sendLock) {
                synchronized (stateLock) {
                    ensureOpen();
                    if (isBound())
                        SctpNet.throwAlreadyBoundException();
                    InetSocketAddress isa = (local == null) ? new InetSocketAddress(0) : Net.checkAddress(local);
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null)
                        sm.checkListen(isa.getPort());
                    Net.bind(fd, isa.getAddress(), isa.getPort());
                    InetSocketAddress boundIsa = Net.localAddress(fd);
                    port = boundIsa.getPort();
                    localAddresses.add(isa);
                    if (isa.getAddress().isAnyLocalAddress())
                        wildcard = true;
                    SctpNet.listen(fdVal, backlog < 1 ? 50 : backlog);
                }
            }
        }
        return this;
    }

    @Override
    public SctpMultiChannel bindAddress(InetAddress address) throws IOException {
        return bindUnbindAddress(address, true);
    }

    @Override
    public SctpMultiChannel unbindAddress(InetAddress address) throws IOException {
        return bindUnbindAddress(address, false);
    }

    private SctpMultiChannel bindUnbindAddress(InetAddress address, boolean add) throws IOException {
        if (address == null)
            throw new IllegalArgumentException();
        synchronized (receiveLock) {
            synchronized (sendLock) {
                synchronized (stateLock) {
                    if (!isOpen())
                        throw new ClosedChannelException();
                    if (!isBound())
                        throw new NotYetBoundException();
                    if (wildcard)
                        throw new IllegalStateException("Cannot add or remove addresses from a channel that is bound to the wildcard address");
                    if (address.isAnyLocalAddress())
                        throw new IllegalArgumentException("Cannot add or remove the wildcard address");
                    if (add) {
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                SctpNet.throwAlreadyBoundException();
                            }
                        }
                    } else {
                        if (localAddresses.size() <= 1)
                            throw new IllegalUnbindException("Cannot remove address from a channel with only one address bound");
                        boolean foundAddress = false;
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                foundAddress = true;
                                break;
                            }
                        }
                        if (!foundAddress)
                            throw new IllegalUnbindException("Cannot remove address from a channel that is not bound to that address");
                    }
                    SctpNet.bindx(fdVal, new InetAddress[] { address }, port, add);
                    if (add)
                        localAddresses.add(new InetSocketAddress(address, port));
                    else {
                        for (InetSocketAddress addr : localAddresses) {
                            if (addr.getAddress().equals(address)) {
                                localAddresses.remove(addr);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    @Override
    public Set<Association> associations() throws ClosedChannelException, NotYetBoundException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isBound())
                throw new NotYetBoundException();
            return Collections.unmodifiableSet(associationMap.keySet());
        }
    }

    private boolean isBound() {
        synchronized (stateLock) {
            return port == -1 ? false : true;
        }
    }

    private void ensureOpen() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
        }
    }

    private void receiverCleanup() throws IOException {
        synchronized (stateLock) {
            receiverThread = 0;
            if (state == ChannelState.KILLPENDING)
                kill();
        }
    }

    private void senderCleanup() throws IOException {
        synchronized (stateLock) {
            senderThread = 0;
            if (state == ChannelState.KILLPENDING)
                kill();
        }
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(fd, block);
    }

    @Override
    public void implCloseSelectableChannel() throws IOException {
        synchronized (stateLock) {
            SctpNet.preClose(fdVal);
            if (receiverThread != 0)
                NativeThread.signal(receiverThread);
            if (senderThread != 0)
                NativeThread.signal(senderThread);
            if (!isRegistered())
                kill();
        }
    }

    @Override
    public FileDescriptor getFD() {
        return fd;
    }

    @Override
    public int getFDVal() {
        return fdVal;
    }

    private boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps();
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;
        if ((ops & Net.POLLNVAL) != 0) {
            return false;
        }
        if ((ops & (Net.POLLERR | Net.POLLHUP)) != 0) {
            newOps = intOps;
            sk.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }
        if (((ops & Net.POLLIN) != 0) && ((intOps & SelectionKey.OP_READ) != 0))
            newOps |= SelectionKey.OP_READ;
        if (((ops & Net.POLLOUT) != 0) && ((intOps & SelectionKey.OP_WRITE) != 0))
            newOps |= SelectionKey.OP_WRITE;
        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    @Override
    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    @Override
    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    @Override
    public int translateInterestOps(int ops) {
        int newOps = 0;
        if ((ops & SelectionKey.OP_READ) != 0)
            newOps |= Net.POLLIN;
        if ((ops & SelectionKey.OP_WRITE) != 0)
            newOps |= Net.POLLOUT;
        return newOps;
    }

    @Override
    public void kill() throws IOException {
        synchronized (stateLock) {
            if (state == ChannelState.KILLED)
                return;
            if (state == ChannelState.UNINITIALIZED) {
                state = ChannelState.KILLED;
                return;
            }
            assert !isOpen() && !isRegistered();
            if (receiverThread == 0 && senderThread == 0) {
                SctpNet.close(fdVal);
                state = ChannelState.KILLED;
            } else {
                state = ChannelState.KILLPENDING;
            }
        }
    }

    @Override
    public <T> SctpMultiChannel setOption(SctpSocketOption<T> name, T value, Association association) throws IOException {
        if (name == null)
            throw new NullPointerException();
        if (!(supportedOptions().contains(name)))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (association != null && (name.equals(SCTP_PRIMARY_ADDR) || name.equals(SCTP_SET_PEER_PRIMARY_ADDR))) {
                checkAssociation(association);
            }
            if (!isOpen())
                throw new ClosedChannelException();
            int assocId = association == null ? 0 : association.associationID();
            SctpNet.setSocketOption(fdVal, name, value, assocId);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(SctpSocketOption<T> name, Association association) throws IOException {
        if (name == null)
            throw new NullPointerException();
        if (!supportedOptions().contains(name))
            throw new UnsupportedOperationException("'" + name + "' not supported");
        synchronized (stateLock) {
            if (association != null && (name.equals(SCTP_PRIMARY_ADDR) || name.equals(SCTP_SET_PEER_PRIMARY_ADDR))) {
                checkAssociation(association);
            }
            if (!isOpen())
                throw new ClosedChannelException();
            int assocId = association == null ? 0 : association.associationID();
            return (T) SctpNet.getSocketOption(fdVal, name, assocId);
        }
    }

    private static class DefaultOptionsHolder {

        static final Set<SctpSocketOption<?>> defaultOptions = defaultOptions();

        private static Set<SctpSocketOption<?>> defaultOptions() {
            HashSet<SctpSocketOption<?>> set = new HashSet<SctpSocketOption<?>>(10);
            set.add(SCTP_DISABLE_FRAGMENTS);
            set.add(SCTP_EXPLICIT_COMPLETE);
            set.add(SCTP_FRAGMENT_INTERLEAVE);
            set.add(SCTP_INIT_MAXSTREAMS);
            set.add(SCTP_NODELAY);
            set.add(SCTP_PRIMARY_ADDR);
            set.add(SCTP_SET_PEER_PRIMARY_ADDR);
            set.add(SO_SNDBUF);
            set.add(SO_RCVBUF);
            set.add(SO_LINGER);
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    public final Set<SctpSocketOption<?>> supportedOptions() {
        return DefaultOptionsHolder.defaultOptions;
    }

    @Override
    public <T> MessageInfo receive(ByteBuffer buffer, T attachment, NotificationHandler<T> handler) throws IOException {
        if (buffer == null)
            throw new IllegalArgumentException("buffer cannot be null");
        if (buffer.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        if (receiveInvoked.get())
            throw new IllegalReceiveException("cannot invoke receive from handler");
        receiveInvoked.set(Boolean.TRUE);
        try {
            ResultContainer resultContainer = new ResultContainer();
            do {
                resultContainer.clear();
                synchronized (receiveLock) {
                    ensureOpen();
                    if (!isBound())
                        throw new NotYetBoundException();
                    int n = 0;
                    try {
                        begin();
                        synchronized (stateLock) {
                            if (!isOpen())
                                return null;
                            receiverThread = NativeThread.current();
                        }
                        do {
                            n = receive(fdVal, buffer, resultContainer);
                        } while ((n == IOStatus.INTERRUPTED) && isOpen());
                    } finally {
                        receiverCleanup();
                        end((n > 0) || (n == IOStatus.UNAVAILABLE));
                        assert IOStatus.check(n);
                    }
                    if (!resultContainer.isNotification()) {
                        if (resultContainer.hasSomething()) {
                            MessageInfoImpl info = resultContainer.getMessageInfo();
                            info.setAssociation(lookupAssociation(info.associationID()));
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null) {
                                InetSocketAddress isa = (InetSocketAddress) info.address();
                                if (!addressMap.containsKey(isa)) {
                                    try {
                                        sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
                                    } catch (SecurityException se) {
                                        buffer.clear();
                                        throw se;
                                    }
                                }
                            }
                            assert info.association() != null;
                            return info;
                        } else {
                            return null;
                        }
                    } else {
                        synchronized (stateLock) {
                            handleNotificationInternal(resultContainer);
                        }
                    }
                }
            } while (handler == null ? true : (invokeNotificationHandler(resultContainer, handler, attachment) == HandlerResult.CONTINUE));
        } finally {
            receiveInvoked.set(Boolean.FALSE);
        }
        return null;
    }

    private int receive(int fd, ByteBuffer dst, ResultContainer resultContainer) throws IOException {
        int pos = dst.position();
        int lim = dst.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (dst instanceof DirectBuffer && rem > 0)
            return receiveIntoNativeBuffer(fd, resultContainer, dst, rem, pos);
        int newSize = Math.max(rem, 1);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(newSize);
        try {
            int n = receiveIntoNativeBuffer(fd, resultContainer, bb, newSize, 0);
            bb.flip();
            if (n > 0 && rem > 0)
                dst.put(bb);
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int receiveIntoNativeBuffer(int fd, ResultContainer resultContainer, ByteBuffer bb, int rem, int pos) throws IOException {
        int n = receive0(fd, resultContainer, ((DirectBuffer) bb).address() + pos, rem);
        if (n > 0)
            bb.position(pos + n);
        return n;
    }

    private InternalNotificationHandler internalNotificationHandler = new InternalNotificationHandler();

    private void handleNotificationInternal(ResultContainer resultContainer) {
        invokeNotificationHandler(resultContainer, internalNotificationHandler, null);
    }

    private class InternalNotificationHandler extends AbstractNotificationHandler<Object> {

        @Override
        public HandlerResult handleNotification(AssociationChangeNotification not, Object unused) {
            AssociationChange sac = (AssociationChange) not;
            switch(not.event()) {
                case COMM_UP:
                    Association newAssociation = new AssociationImpl(sac.assocId(), sac.maxInStreams(), sac.maxOutStreams());
                    addAssociation(newAssociation);
                    break;
                case SHUTDOWN:
                case COMM_LOST:
                    associationToRemove.set(lookupAssociation(sac.assocId()));
            }
            return HandlerResult.CONTINUE;
        }
    }

    private <T> HandlerResult invokeNotificationHandler(ResultContainer resultContainer, NotificationHandler<T> handler, T attachment) {
        HandlerResult result;
        SctpNotification notification = resultContainer.notification();
        notification.setAssociation(lookupAssociation(notification.assocId()));
        if (!(handler instanceof AbstractNotificationHandler)) {
            result = handler.handleNotification(notification, attachment);
        } else {
            AbstractNotificationHandler<T> absHandler = (AbstractNotificationHandler<T>) handler;
            switch(resultContainer.type()) {
                case ASSOCIATION_CHANGED:
                    result = absHandler.handleNotification(resultContainer.getAssociationChanged(), attachment);
                    break;
                case PEER_ADDRESS_CHANGED:
                    result = absHandler.handleNotification(resultContainer.getPeerAddressChanged(), attachment);
                    break;
                case SEND_FAILED:
                    result = absHandler.handleNotification(resultContainer.getSendFailed(), attachment);
                    break;
                case SHUTDOWN:
                    result = absHandler.handleNotification(resultContainer.getShutdown(), attachment);
                    break;
                default:
                    result = absHandler.handleNotification(resultContainer.notification(), attachment);
            }
        }
        if (!(handler instanceof InternalNotificationHandler)) {
            Association assoc = associationToRemove.get();
            if (assoc != null) {
                removeAssociation(assoc);
                associationToRemove.set(null);
            }
        }
        return result;
    }

    private Association lookupAssociation(int assocId) {
        synchronized (stateLock) {
            Set<Association> assocs = associationMap.keySet();
            for (Association a : assocs) {
                if (a.associationID() == assocId) {
                    return a;
                }
            }
        }
        return null;
    }

    private void addAssociation(Association association) {
        synchronized (stateLock) {
            int assocId = association.associationID();
            Set<SocketAddress> addresses = null;
            try {
                addresses = SctpNet.getRemoteAddresses(fdVal, assocId);
            } catch (IOException unused) {
            }
            associationMap.put(association, addresses);
            if (addresses != null) {
                for (SocketAddress addr : addresses) addressMap.put(addr, association);
            }
        }
    }

    private void removeAssociation(Association association) {
        synchronized (stateLock) {
            int assocId = association.associationID();
            Set<SocketAddress> addresses = null;
            try {
                addresses = SctpNet.getRemoteAddresses(fdVal, assocId);
            } catch (IOException unused) {
            }
            Set<Association> assocs = associationMap.keySet();
            for (Association a : assocs) {
                if (a.associationID() == assocId) {
                    associationMap.remove(a);
                    break;
                }
            }
            if (addresses != null) {
                for (SocketAddress addr : addresses) addressMap.remove(addr);
            } else {
                Set<java.util.Map.Entry<SocketAddress, Association>> addrAssocs = addressMap.entrySet();
                Iterator<Entry<SocketAddress, Association>> iterator = addrAssocs.iterator();
                while (iterator.hasNext()) {
                    Entry<SocketAddress, Association> entry = iterator.next();
                    if (entry.getValue().equals(association)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private boolean checkAssociation(Association messageAssoc) {
        synchronized (stateLock) {
            for (Association association : associationMap.keySet()) {
                if (messageAssoc.equals(association)) {
                    return true;
                }
            }
        }
        throw new IllegalArgumentException("Given Association is not controlled by this channel");
    }

    private void checkStreamNumber(Association assoc, int streamNumber) {
        synchronized (stateLock) {
            if (streamNumber < 0 || streamNumber >= assoc.maxOutboundStreams())
                throw new InvalidStreamException();
        }
    }

    @Override
    public int send(ByteBuffer buffer, MessageInfo messageInfo) throws IOException {
        if (buffer == null)
            throw new IllegalArgumentException("buffer cannot be null");
        if (messageInfo == null)
            throw new IllegalArgumentException("messageInfo cannot be null");
        synchronized (sendLock) {
            ensureOpen();
            if (!isBound())
                bind(null, 0);
            int n = 0;
            try {
                int assocId = -1;
                SocketAddress address = null;
                begin();
                synchronized (stateLock) {
                    if (!isOpen())
                        return 0;
                    senderThread = NativeThread.current();
                    Association assoc = messageInfo.association();
                    InetSocketAddress addr = (InetSocketAddress) messageInfo.address();
                    if (assoc != null) {
                        checkAssociation(assoc);
                        checkStreamNumber(assoc, messageInfo.streamNumber());
                        assocId = assoc.associationID();
                        if (addr != null) {
                            if (!assoc.equals(addressMap.get(addr)))
                                throw new IllegalArgumentException("given preferred address is not part of this association");
                            address = addr;
                        }
                    } else if (addr != null) {
                        address = addr;
                        Association association = addressMap.get(addr);
                        if (association != null) {
                            checkStreamNumber(association, messageInfo.streamNumber());
                            assocId = association.associationID();
                        } else {
                            SecurityManager sm = System.getSecurityManager();
                            if (sm != null)
                                sm.checkConnect(addr.getAddress().getHostAddress(), addr.getPort());
                        }
                    } else {
                        throw new AssertionError("Both association and address cannot be null");
                    }
                }
                do {
                    n = send(fdVal, buffer, assocId, address, messageInfo);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                senderCleanup();
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }

    private int send(int fd, ByteBuffer src, int assocId, SocketAddress target, MessageInfo messageInfo) throws IOException {
        int streamNumber = messageInfo.streamNumber();
        boolean unordered = messageInfo.isUnordered();
        int ppid = messageInfo.payloadProtocolID();
        if (src instanceof DirectBuffer)
            return sendFromNativeBuffer(fd, src, target, assocId, streamNumber, unordered, ppid);
        int pos = src.position();
        int lim = src.limit();
        assert (pos <= lim && streamNumber >= 0);
        int rem = (pos <= lim ? lim - pos : 0);
        ByteBuffer bb = Util.getTemporaryDirectBuffer(rem);
        try {
            bb.put(src);
            bb.flip();
            src.position(pos);
            int n = sendFromNativeBuffer(fd, bb, target, assocId, streamNumber, unordered, ppid);
            if (n > 0) {
                src.position(pos + n);
            }
            return n;
        } finally {
            Util.releaseTemporaryDirectBuffer(bb);
        }
    }

    private int sendFromNativeBuffer(int fd, ByteBuffer bb, SocketAddress target, int assocId, int streamNumber, boolean unordered, int ppid) throws IOException {
        InetAddress addr = null;
        int port = 0;
        if (target != null) {
            InetSocketAddress isa = Net.checkAddress(target);
            addr = isa.getAddress();
            port = isa.getPort();
        }
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int written = send0(fd, ((DirectBuffer) bb).address() + pos, rem, addr, port, assocId, streamNumber, unordered, ppid);
        if (written > 0)
            bb.position(pos + written);
        return written;
    }

    @Override
    public SctpMultiChannel shutdown(Association association) throws IOException {
        synchronized (stateLock) {
            checkAssociation(association);
            if (!isOpen())
                throw new ClosedChannelException();
            SctpNet.shutdown(fdVal, association.associationID());
        }
        return this;
    }

    @Override
    public Set<SocketAddress> getAllLocalAddresses() throws IOException {
        synchronized (stateLock) {
            if (!isOpen())
                throw new ClosedChannelException();
            if (!isBound())
                return Collections.emptySet();
            return SctpNet.getLocalAddresses(fdVal);
        }
    }

    @Override
    public Set<SocketAddress> getRemoteAddresses(Association association) throws IOException {
        synchronized (stateLock) {
            checkAssociation(association);
            if (!isOpen())
                throw new ClosedChannelException();
            try {
                return SctpNet.getRemoteAddresses(fdVal, association.associationID());
            } catch (SocketException se) {
                Set<SocketAddress> addrs = associationMap.get(association);
                return addrs != null ? addrs : Collections.<SocketAddress>emptySet();
            }
        }
    }

    @Override
    public SctpChannel branch(Association association) throws IOException {
        synchronized (stateLock) {
            checkAssociation(association);
            if (!isOpen())
                throw new ClosedChannelException();
            FileDescriptor bFd = SctpNet.branch(fdVal, association.associationID());
            removeAssociation(association);
            return new SctpChannelImpl(provider(), bFd, association);
        }
    }

    private static int receive0(int fd, ResultContainer resultContainer, long address, int length) throws IOException {
        return SctpChannelImpl.receive0(fd, resultContainer, address, length, false);
    }

    private static int send0(int fd, long address, int length, InetAddress addr, int port, int assocId, int streamNumber, boolean unordered, int ppid) throws IOException {
        return SctpChannelImpl.send0(fd, address, length, addr, port, assocId, streamNumber, unordered, ppid);
    }

    static {
        IOUtil.load();
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("sctp");
                return null;
            }
        });
    }
}
