package java.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import sun.misc.JavaSecurityProtectionDomainAccess;
import static sun.misc.JavaSecurityProtectionDomainAccess.ProtectionDomainCache;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;

public class ProtectionDomain {

    static {
        SharedSecrets.setJavaSecurityAccess(new JavaSecurityAccess() {

            public <T> T doIntersectionPrivilege(PrivilegedAction<T> action, final AccessControlContext stack, final AccessControlContext context) {
                if (action == null) {
                    throw new NullPointerException();
                }
                return AccessController.doPrivileged(action, new AccessControlContext(stack.getContext(), context).optimize());
            }

            public <T> T doIntersectionPrivilege(PrivilegedAction<T> action, AccessControlContext context) {
                return doIntersectionPrivilege(action, AccessController.getContext(), context);
            }
        });
    }

    private CodeSource codesource;

    private ClassLoader classloader;

    private Principal[] principals;

    private PermissionCollection permissions;

    private boolean hasAllPerm = false;

    private boolean staticPermissions;

    final Key key = new Key();

    private static final Debug debug = Debug.getInstance("domain");

    public ProtectionDomain(CodeSource codesource, PermissionCollection permissions) {
        this.codesource = codesource;
        if (permissions != null) {
            this.permissions = permissions;
            this.permissions.setReadOnly();
            if (permissions instanceof Permissions && ((Permissions) permissions).allPermission != null) {
                hasAllPerm = true;
            }
        }
        this.classloader = null;
        this.principals = new Principal[0];
        staticPermissions = true;
    }

    public ProtectionDomain(CodeSource codesource, PermissionCollection permissions, ClassLoader classloader, Principal[] principals) {
        this.codesource = codesource;
        if (permissions != null) {
            this.permissions = permissions;
            this.permissions.setReadOnly();
            if (permissions instanceof Permissions && ((Permissions) permissions).allPermission != null) {
                hasAllPerm = true;
            }
        }
        this.classloader = classloader;
        this.principals = (principals != null ? principals.clone() : new Principal[0]);
        staticPermissions = false;
    }

    public final CodeSource getCodeSource() {
        return this.codesource;
    }

    public final ClassLoader getClassLoader() {
        return this.classloader;
    }

    public final Principal[] getPrincipals() {
        return this.principals.clone();
    }

    public final PermissionCollection getPermissions() {
        return permissions;
    }

    public boolean implies(Permission permission) {
        if (hasAllPerm) {
            return true;
        }
        if (!staticPermissions && Policy.getPolicyNoCheck().implies(this, permission))
            return true;
        if (permissions != null)
            return permissions.implies(permission);
        return false;
    }

    boolean impliesCreateAccessControlContext() {
        return implies(SecurityConstants.CREATE_ACC_PERMISSION);
    }

    @Override
    public String toString() {
        String pals = "<no principals>";
        if (principals != null && principals.length > 0) {
            StringBuilder palBuf = new StringBuilder("(principals ");
            for (int i = 0; i < principals.length; i++) {
                palBuf.append(principals[i].getClass().getName() + " \"" + principals[i].getName() + "\"");
                if (i < principals.length - 1)
                    palBuf.append(",\n");
                else
                    palBuf.append(")\n");
            }
            pals = palBuf.toString();
        }
        PermissionCollection pc = Policy.isSet() && seeAllp() ? mergePermissions() : getPermissions();
        return "ProtectionDomain " + " " + codesource + "\n" + " " + classloader + "\n" + " " + pals + "\n" + " " + pc + "\n";
    }

    private static boolean seeAllp() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return true;
        } else {
            if (debug != null) {
                if (sm.getClass().getClassLoader() == null && Policy.getPolicyNoCheck().getClass().getClassLoader() == null) {
                    return true;
                }
            } else {
                try {
                    sm.checkPermission(SecurityConstants.GET_POLICY_PERMISSION);
                    return true;
                } catch (SecurityException se) {
                }
            }
        }
        return false;
    }

    private PermissionCollection mergePermissions() {
        if (staticPermissions)
            return permissions;
        PermissionCollection perms = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<PermissionCollection>() {

            public PermissionCollection run() {
                Policy p = Policy.getPolicyNoCheck();
                return p.getPermissions(ProtectionDomain.this);
            }
        });
        Permissions mergedPerms = new Permissions();
        int swag = 32;
        int vcap = 8;
        Enumeration<Permission> e;
        List<Permission> pdVector = new ArrayList<>(vcap);
        List<Permission> plVector = new ArrayList<>(swag);
        if (permissions != null) {
            synchronized (permissions) {
                e = permissions.elements();
                while (e.hasMoreElements()) {
                    pdVector.add(e.nextElement());
                }
            }
        }
        if (perms != null) {
            synchronized (perms) {
                e = perms.elements();
                while (e.hasMoreElements()) {
                    plVector.add(e.nextElement());
                    vcap++;
                }
            }
        }
        if (perms != null && permissions != null) {
            synchronized (permissions) {
                e = permissions.elements();
                while (e.hasMoreElements()) {
                    Permission pdp = e.nextElement();
                    Class<?> pdpClass = pdp.getClass();
                    String pdpActions = pdp.getActions();
                    String pdpName = pdp.getName();
                    for (int i = 0; i < plVector.size(); i++) {
                        Permission pp = plVector.get(i);
                        if (pdpClass.isInstance(pp)) {
                            if (pdpName.equals(pp.getName()) && pdpActions.equals(pp.getActions())) {
                                plVector.remove(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (perms != null) {
            for (int i = plVector.size() - 1; i >= 0; i--) {
                mergedPerms.add(plVector.get(i));
            }
        }
        if (permissions != null) {
            for (int i = pdVector.size() - 1; i >= 0; i--) {
                mergedPerms.add(pdVector.get(i));
            }
        }
        return mergedPerms;
    }

    final class Key {
    }

    static {
        SharedSecrets.setJavaSecurityProtectionDomainAccess(new JavaSecurityProtectionDomainAccess() {

            public ProtectionDomainCache getProtectionDomainCache() {
                return new ProtectionDomainCache() {

                    private final Map<Key, PermissionCollection> map = Collections.synchronizedMap(new WeakHashMap<Key, PermissionCollection>());

                    public void put(ProtectionDomain pd, PermissionCollection pc) {
                        map.put((pd == null ? null : pd.key), pc);
                    }

                    public PermissionCollection get(ProtectionDomain pd) {
                        return pd == null ? map.get(null) : map.get(pd.key);
                    }
                };
            }
        });
    }
}
