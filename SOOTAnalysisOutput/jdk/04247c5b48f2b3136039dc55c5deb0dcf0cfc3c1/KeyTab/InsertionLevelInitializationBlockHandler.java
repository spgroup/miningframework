package javax.security.auth.kerberos;

import java.io.File;
import java.util.Objects;
import sun.misc.SharedSecrets;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.PrincipalName;
import sun.security.krb5.RealmException;

public final class KeyTab {

    private final File file;

    static {
        SharedSecrets.setJavaxSecurityAuthKerberosAccess(new JavaxSecurityAuthKerberosAccessImpl());
    }

    private KeyTab(File file) {
        this.file = file;
    }

    public static KeyTab getInstance(File file) {
        if (file == null) {
            throw new NullPointerException("file must be non null");
        }
        return new KeyTab(file);
    }

    public static KeyTab getInstance() {
        return new KeyTab(null);
    }

    private sun.security.krb5.internal.ktab.KeyTab takeSnapshot() {
        return sun.security.krb5.internal.ktab.KeyTab.getInstance(file);
    }

    public KerberosKey[] getKeys(KerberosPrincipal principal) {
        try {
            EncryptionKey[] keys = takeSnapshot().readServiceKeys(new PrincipalName(principal.getName()));
            KerberosKey[] kks = new KerberosKey[keys.length];
            for (int i = 0; i < kks.length; i++) {
                Integer tmp = keys[i].getKeyVersionNumber();
                kks[i] = new KerberosKey(principal, keys[i].getBytes(), keys[i].getEType(), tmp == null ? 0 : tmp.intValue());
                keys[i].destroy();
            }
            return kks;
        } catch (RealmException re) {
            return new KerberosKey[0];
        }
    }

    EncryptionKey[] getEncryptionKeys(PrincipalName principal) {
        return takeSnapshot().readServiceKeys(principal);
    }

    public boolean exists() {
        return !takeSnapshot().isMissing();
    }

    public String toString() {
        return file == null ? "Default keytab" : file.toString();
    }

    public int hashCode() {
        return Objects.hash(file);
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof KeyTab)) {
            return false;
        }
        KeyTab otherKtab = (KeyTab) other;
        return Objects.equals(otherKtab.file, file);
    }
}