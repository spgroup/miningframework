package org.apache.accumulo.server.master.state;

import java.util.HashMap;
import java.util.HashSet;

public enum TabletServerState {

    RESERVED((byte) (-1)),
    NEW((byte) 0),
    ONLINE((byte) 1),
    UNRESPONSIVE((byte) 2),
    DOWN((byte) 3),
    BAD_SYSTEM_PASSWORD((byte) 101),
    BAD_VERSION((byte) 102),
    BAD_INSTANCE((byte) 103),
    BAD_CONFIG((byte) 104),
    BAD_VERSION_AND_INSTANCE((byte) 105),
    BAD_VERSION_AND_CONFIG((byte) 106),
    BAD_VERSION_AND_INSTANCE_AND_CONFIG((byte) 107),
    BAD_INSTANCE_AND_CONFIG((byte) 108);

    private byte id;

    private static HashMap<Byte, TabletServerState> mapping;

    private static HashSet<TabletServerState> badStates;

    static {
        mapping = new HashMap<>(TabletServerState.values().length);
        badStates = new HashSet<>();
        for (TabletServerState state : TabletServerState.values()) {
            mapping.put(state.id, state);
            if (state.id > 99)
                badStates.add(state);
        }
    }

    private TabletServerState(byte id) {
        this.id = id;
    }

    public byte getId() {
        return this.id;
    }

    public static TabletServerState getStateById(byte id) {
        if (mapping.containsKey(id))
            return mapping.get(id);
        throw new IndexOutOfBoundsException("No such state");
    }
}