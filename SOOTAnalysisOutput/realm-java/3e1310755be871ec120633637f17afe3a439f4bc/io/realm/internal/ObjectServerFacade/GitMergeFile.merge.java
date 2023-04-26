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
<<<<<<< MINE
            @SuppressWarnings("LiteralClassName")
            Class syncFacadeClass = Class.forName("io.realm.internal.objectserver.SyncObjectServerFacade");
=======
            Class syncFacadeClass = Class.forName("io.realm.internal.SyncObjectServerFacade");
>>>>>>> YOURS
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
