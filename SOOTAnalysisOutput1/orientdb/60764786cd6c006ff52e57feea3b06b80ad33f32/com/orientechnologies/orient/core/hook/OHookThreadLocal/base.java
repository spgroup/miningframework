package com.orientechnologies.orient.core.hook;

import com.orientechnologies.orient.core.OOrientListenerAbstract;
import com.orientechnologies.orient.core.OOrientShutdownListener;
import com.orientechnologies.orient.core.OOrientStartupListener;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import java.util.HashSet;
import java.util.Set;

public class OHookThreadLocal extends ThreadLocal<Set<OIdentifiable>> {

    public static volatile OHookThreadLocal INSTANCE = new OHookThreadLocal();

    static {
        Orient.instance().registerListener(new OOrientListenerAbstract() {

            @Override
            public void onStartup() {
                if (INSTANCE == null)
                    INSTANCE = new OHookThreadLocal();
            }

            @Override
            public void onShutdown() {
                INSTANCE = null;
            }
        });
    }

    public boolean push(final OIdentifiable iRecord) {
        final Set<OIdentifiable> set = get();
        if (set.contains(iRecord))
            return false;
        set.add(iRecord);
        return true;
    }

    public boolean pop(final OIdentifiable iRecord) {
        return get().remove(iRecord);
    }

    @Override
    protected Set<OIdentifiable> initialValue() {
        return new HashSet<OIdentifiable>();
    }
}
