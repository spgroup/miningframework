package com.orientechnologies.orient.server.distributed.sql;

import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLAbstract;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ODistributedCommandExecutorSQLFactory implements OCommandExecutorSQLFactory {

    private static final Map<String, Class<? extends OCommandExecutorSQLAbstract>> COMMANDS;

    static {
        final Map<String, Class<? extends OCommandExecutorSQLAbstract>> commands = new HashMap<String, Class<? extends OCommandExecutorSQLAbstract>>();
        commands.put(OCommandExecutorSQLSyncDatabase.NAME, OCommandExecutorSQLSyncDatabase.class);
        commands.put(OCommandExecutorSQLSyncCluster.NAME, OCommandExecutorSQLSyncCluster.class);
        COMMANDS = Collections.unmodifiableMap(commands);
    }

    public Set<String> getCommandNames() {
        return COMMANDS.keySet();
    }

    public OCommandExecutorSQLAbstract createCommand(final String name) throws OCommandExecutionException {
        final Class<? extends OCommandExecutorSQLAbstract> clazz = COMMANDS.get(name);
        if (clazz == null) {
            throw new OCommandExecutionException("Unknown command name :" + name);
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new OCommandExecutionException("Error in creation of command " + name + "(). Probably there is not an empty constructor or the constructor generates errors", e);
        }
    }
}