package io.realm.internal;

import android.content.Context;
import java.lang.reflect.InvocationTargetException;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;

public class ObjectServerFacade {

    private final static ObjectServerFacade nonSyncFacade = new ObjectServerFacade();

    private static ObjectServerFacade syncFacade = null;

    static {
        try {
            @SuppressWarnings("LiteralClassName")
            Class syncFacadeClass = Class.forName("io.realm.internal.objectserver.SyncObjectServerFacade");
            syncFacade = (ObjectServerFacade) syncFacadeClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ignored) {
        } catch (InstantiationException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        } catch (IllegalAccessException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        } catch (NoSuchMethodException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e);
        } catch (InvocationTargetException e) {
            throw new RealmException("Failed to init SyncObjectServerFacade", e.getTargetException());
        }
    }

    public void init(Context context) {
    }

    public void notifyCommit(RealmConfiguration configuration, long lastSnapshotVersion) {
    }

    public void realmClosed(RealmConfiguration configuration) {
    }

    public void realmOpened(RealmConfiguration configuration) {
    }

    public String[] getUserAndServerUrl(RealmConfiguration config) {
        return new String[2];
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
}
