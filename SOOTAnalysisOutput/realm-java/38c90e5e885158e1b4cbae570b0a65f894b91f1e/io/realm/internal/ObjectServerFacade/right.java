package io.realm.internal;

import android.content.Context;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

public class ObjectServerFacade {

    private final static ObjectServerFacade nonSyncFacade = new ObjectServerFacade();

    private static ObjectServerFacade syncFacade = null;

    static {
        try {
            Class syncFacadeClass = Class.forName("io.realm.internal.SyncObjectServerFacade");
            syncFacade = (ObjectServerFacade) syncFacadeClass.newInstance();
        } catch (ClassNotFoundException ignored) {
        } catch (InstantiationException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        }
    }

    public void init(Context context) {
    }

    public void realmClosed(RealmConfiguration configuration) {
    }

    public String[] getUserAndServerUrl(RealmConfiguration config) {
        return new String[4];
    }

    public static ObjectServerFacade getFacade(boolean needSyncFacade) {
        if (needSyncFacade) {
            return syncFacade;
        }
        return nonSyncFacade;
    }

    public static ObjectServerFacade getSyncFacadeIfPossible() {
        if (syncFacade != null) {
            return syncFacade;
        }
        return nonSyncFacade;
    }

    public void wrapObjectStoreSessionIfRequired(RealmConfiguration config) {
    }
}
