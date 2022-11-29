package java.rmi.activation;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;
import java.rmi.server.UID;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

public class ActivationID implements Serializable {

    private transient Activator activator;

    private transient UID uid = new UID();

    private static final long serialVersionUID = -4608673054848209235L;

    private static final AccessControlContext NOPERMS_ACC;

    static {
        Permissions perms = new Permissions();
        ProtectionDomain[] pd = { new ProtectionDomain(null, perms) };
        NOPERMS_ACC = new AccessControlContext(pd);
    }

    public ActivationID(Activator activator) {
        this.activator = activator;
    }

    public Remote activate(boolean force) throws ActivationException, UnknownObjectException, RemoteException {
        try {
            MarshalledObject<? extends Remote> mobj = activator.activate(this, force);
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Remote>() {

                public Remote run() throws IOException, ClassNotFoundException {
                    return mobj.get();
                }
            }, NOPERMS_ACC);
        } catch (PrivilegedActionException pae) {
            Exception ex = pae.getException();
            if (ex instanceof RemoteException) {
                throw (RemoteException) ex;
            } else {
                throw new UnmarshalException("activation failed", ex);
            }
        }
    }

    public int hashCode() {
        return uid.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ActivationID) {
            ActivationID id = (ActivationID) obj;
            return (uid.equals(id.uid) && activator.equals(id.activator));
        } else {
            return false;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
        out.writeObject(uid);
        RemoteRef ref;
        if (activator instanceof RemoteObject) {
            ref = ((RemoteObject) activator).getRef();
        } else if (Proxy.isProxyClass(activator.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(activator);
            if (!(handler instanceof RemoteObjectInvocationHandler)) {
                throw new InvalidObjectException("unexpected invocation handler");
            }
            ref = ((RemoteObjectInvocationHandler) handler).getRef();
        } else {
            throw new InvalidObjectException("unexpected activator type");
        }
        out.writeUTF(ref.getRefClass(out));
        ref.writeExternal(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        uid = (UID) in.readObject();
        try {
            Class<? extends RemoteRef> refClass = Class.forName(RemoteRef.packagePrefix + "." + in.readUTF()).asSubclass(RemoteRef.class);
            @SuppressWarnings("deprecation")
            RemoteRef ref = refClass.newInstance();
            ref.readExternal(in);
            activator = (Activator) Proxy.newProxyInstance(Activator.class.getClassLoader(), new Class<?>[] { Activator.class }, new RemoteObjectInvocationHandler(ref));
        } catch (InstantiationException e) {
            throw (IOException) new InvalidObjectException("Unable to create remote reference").initCause(e);
        } catch (IllegalAccessException e) {
            throw (IOException) new InvalidObjectException("Unable to create remote reference").initCause(e);
        }
    }
}
