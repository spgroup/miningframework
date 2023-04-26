package org.apache.accumulo.core.client.mock;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import org.apache.accumulo.core.client.BatchScanner;
import org.apache.accumulo.core.client.admin.TimeType;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.metadata.MetadataTable;
import org.apache.accumulo.core.metadata.RootTable;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.SystemPermission;
import org.apache.accumulo.core.security.TablePermission;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;

public class MockAccumulo {

    final Map<String, MockTable> tables = new HashMap<String, MockTable>();

    final Map<String, String> systemProperties = new HashMap<String, String>();

    Map<String, MockUser> users = new HashMap<String, MockUser>();

    final FileSystem fs;

    MockAccumulo(FileSystem fs) {
        this.fs = fs;
    }

    {
        MockUser root = new MockUser("root", new PasswordToken(new byte[0]), Authorizations.EMPTY);
        root.permissions.add(SystemPermission.SYSTEM);
        users.put(root.name, root);
        createTable("root", RootTable.NAME, true, TimeType.LOGICAL);
        createTable("root", MetadataTable.NAME, true, TimeType.LOGICAL);
    }

    public FileSystem getFileSystem() {
        return fs;
    }

    void setProperty(String key, String value) {
        systemProperties.put(key, value);
    }

    String removeProperty(String key) {
        return systemProperties.remove(key);
    }

    void createTable(String user, String table) {
        createTable(user, table, true, TimeType.MILLIS);
    }

    public void addMutation(String table, Mutation m) {
        MockTable t = tables.get(table);
        t.addMutation(m);
    }

    public BatchScanner createBatchScanner(String tableName, Authorizations authorizations) {
        return new MockBatchScanner(tables.get(tableName), authorizations);
    }

    public void createTable(String username, String tableName, boolean useVersions, TimeType timeType) {
        MockTable t = new MockTable(useVersions, timeType);
        t.userPermissions.put(username, EnumSet.allOf(TablePermission.class));
        tables.put(tableName, t);
    }

    public void addSplits(String tableName, SortedSet<Text> partitionKeys) {
        tables.get(tableName).addSplits(partitionKeys);
    }

    public Collection<Text> getSplits(String tableName) {
        return tables.get(tableName).getSplits();
    }

    public void merge(String tableName, Text start, Text end) {
        tables.get(tableName).merge(start, end);
    }
}