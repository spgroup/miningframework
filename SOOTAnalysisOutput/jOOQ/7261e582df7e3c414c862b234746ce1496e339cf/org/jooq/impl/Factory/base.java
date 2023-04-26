package org.jooq.impl;

import static org.jooq.SQLDialect.ASE;
import static org.jooq.SQLDialect.CUBRID;
import static org.jooq.SQLDialect.DB2;
import static org.jooq.SQLDialect.DERBY;
import static org.jooq.SQLDialect.H2;
import static org.jooq.SQLDialect.HSQLDB;
import static org.jooq.SQLDialect.INGRES;
import static org.jooq.SQLDialect.MYSQL;
import static org.jooq.SQLDialect.ORACLE;
import static org.jooq.SQLDialect.POSTGRES;
import static org.jooq.SQLDialect.SQLITE;
import static org.jooq.SQLDialect.SQLSERVER;
import static org.jooq.SQLDialect.SYBASE;
import static org.jooq.conf.SettingsTools.getRenderMapping;
import static org.jooq.impl.Util.combine;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXB;
import org.jooq.AggregateFunction;
import org.jooq.ArrayRecord;
import org.jooq.Attachable;
import org.jooq.Batch;
import org.jooq.BatchBindStep;
import org.jooq.BindContext;
import org.jooq.Case;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.Cursor;
import org.jooq.DataType;
import org.jooq.DatePart;
import org.jooq.DeleteQuery;
import org.jooq.DeleteWhereStep;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.FactoryOperations;
import org.jooq.Field;
import org.jooq.FieldProvider;
import org.jooq.GroupConcatOrderByStep;
import org.jooq.Insert;
import org.jooq.InsertQuery;
import org.jooq.InsertSetStep;
import org.jooq.InsertValuesStep;
import org.jooq.LoaderOptionsStep;
import org.jooq.MergeUsingStep;
import org.jooq.OrderedAggregateFunction;
import org.jooq.Param;
import org.jooq.Query;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.RenderContext;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.SelectSelectStep;
import org.jooq.Sequence;
import org.jooq.SimpleSelectQuery;
import org.jooq.SimpleSelectWhereStep;
import org.jooq.Support;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.TableRecord;
import org.jooq.Truncate;
import org.jooq.UDT;
import org.jooq.UDTRecord;
import org.jooq.UpdatableRecord;
import org.jooq.UpdateQuery;
import org.jooq.UpdateSetStep;
import org.jooq.WindowIgnoreNullsStep;
import org.jooq.WindowOverStep;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.conf.SettingsTools;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.InvalidResultException;
import org.jooq.exception.SQLDialectNotSupportedException;
import org.jooq.tools.JooqLogger;
import org.jooq.types.DayToSecond;

public class Factory implements FactoryOperations {

    private static final long serialVersionUID = 2681360188806309513L;

    private static final JooqLogger log = JooqLogger.getLogger(Factory.class);

    private static final Factory[] DEFAULT_INSTANCES = new Factory[SQLDialect.values().length];

    private transient Connection connection;

    private final SQLDialect dialect;

    @SuppressWarnings("deprecation")
    private final org.jooq.SchemaMapping mapping;

    private final Settings settings;

    private final Map<String, Object> data;

    public Factory(Connection connection, SQLDialect dialect) {
        this(connection, dialect, null, null, null);
    }

    public Factory(SQLDialect dialect) {
        this(null, dialect, null, null, null);
    }

    @Deprecated
    public Factory(Connection connection, SQLDialect dialect, org.jooq.SchemaMapping mapping) {
        this(connection, dialect, null, null, null);
    }

    @SuppressWarnings("deprecation")
    public Factory(Connection connection, SQLDialect dialect, Settings settings) {
        this(connection, dialect, settings, new org.jooq.SchemaMapping(settings), null);
    }

    @SuppressWarnings("deprecation")
    public Factory(SQLDialect dialect, Settings settings) {
        this(null, dialect, settings, new org.jooq.SchemaMapping(settings), null);
    }

    @SuppressWarnings("deprecation")
    private Factory(Connection connection, SQLDialect dialect, Settings settings, org.jooq.SchemaMapping mapping, Map<String, Object> data) {
        this.connection = connection;
        this.dialect = dialect;
        this.settings = settings != null ? settings : SettingsTools.defaultSettings();
        this.mapping = mapping != null ? mapping : new org.jooq.SchemaMapping(this.settings);
        this.data = data != null ? data : new HashMap<String, Object>();
    }

    @Override
    public final SQLDialect getDialect() {
        return dialect;
    }

    @Override
    public final Connection getConnection() {
        if (connection == null) {
            return null;
        } else if (connection.getClass() == ConnectionProxy.class) {
            return connection;
        } else {
            return new ConnectionProxy(connection, settings);
        }
    }

    @Override
    public final void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    @Deprecated
    public final org.jooq.SchemaMapping getSchemaMapping() {
        return mapping;
    }

    @Override
    public final Settings getSettings() {
        return settings;
    }

    @Override
    public final Map<String, Object> getData() {
        return data;
    }

    @Override
    public final Object getData(String key) {
        return data.get(key);
    }

    @Override
    public final Object setData(String key, Object value) {
        return data.put(key, value);
    }

    public final RenderContext renderContext() {
        return new DefaultRenderContext(this);
    }

    @Override
    public final String render(QueryPart part) {
        return renderContext().render(part);
    }

    @Override
    public final String renderNamedParams(QueryPart part) {
        return renderContext().namedParams(true).render(part);
    }

    @Override
    public final String renderInlined(QueryPart part) {
        return renderContext().inline(true).render(part);
    }

    public final BindContext bindContext(PreparedStatement stmt) {
        return new DefaultBindContext(this, stmt);
    }

    public final int bind(QueryPart part, PreparedStatement stmt) {
        return bindContext(stmt).bind(part).peekIndex();
    }

    @Override
    public final void attach(Attachable... attachables) {
        attach(Arrays.asList(attachables));
    }

    @Override
    public final void attach(Collection<Attachable> attachables) {
        for (Attachable attachable : attachables) {
            attachable.attach(this);
        }
    }

    @Override
    public final <R extends TableRecord<R>> LoaderOptionsStep<R> loadInto(Table<R> table) {
        return new LoaderImpl<R>(this, table);
    }

    public static <R extends Record> Table<R> table(Select<R> select) {
        return select.asTable();
    }

    @Support
    public static Table<?> table(List<?> list) {
        return table(list.toArray());
    }

    @Support
    public static Table<?> table(Object[] array) {
        return table(val(array));
    }

    @Support(ORACLE)
    public static Table<?> table(ArrayRecord<?> array) {
        return table(val(array));
    }

    @Support({ H2, HSQLDB, POSTGRES, ORACLE })
    public static Table<?> table(Field<?> cursor) {
        return unnest(cursor);
    }

    @Support
    public static Table<?> unnest(List<?> list) {
        return unnest(list.toArray());
    }

    @Support
    public static Table<?> unnest(Object[] array) {
        return unnest(val(array));
    }

    @Support(ORACLE)
    public static Table<?> unnest(ArrayRecord<?> array) {
        return unnest(val(array));
    }

    @Support({ H2, HSQLDB, POSTGRES, ORACLE })
    public static Table<?> unnest(Field<?> cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException();
        } else if (cursor.getType() == Result.class) {
            return new FunctionTable<Record>(cursor);
        } else if (ArrayConstant.class.isAssignableFrom(cursor.getClass())) {
            return new ArrayTable(cursor);
        } else if (ArrayRecord.class.isAssignableFrom(cursor.getDataType().getType())) {
            return new ArrayTable(cursor);
        } else if (cursor.getType().isArray() && cursor.getType() != byte[].class) {
            return new ArrayTable(cursor);
        }
        throw new SQLDialectNotSupportedException("Converting arbitrary types into array tables is currently not supported");
    }

    @Support
    public static Table<Record> table(String sql) {
        return table(sql, new Object[0]);
    }

    @Support
    public static Table<Record> table(String sql, Object... bindings) {
        return new SQLTable(sql, bindings);
    }

    @Support
    public static Table<Record> tableByName(String... tableName) {
        return new QualifiedTable(tableName);
    }

    public static Field<Object> field(String sql, QueryPart... parts) {
        return new SQLClause<Object>(sql, SQLDataType.OTHER, parts);
    }

    public static <T> Field<T> field(String sql, Class<T> type, QueryPart... parts) {
        return new SQLClause<T>(sql, getDataType(type), parts);
    }

    public static <T> Field<T> field(String sql, DataType<T> type, QueryPart... parts) {
        return new SQLClause<T>(sql, type, parts);
    }

    @Support
    public static Field<Object> field(String sql) {
        return field(sql, new Object[0]);
    }

    @Support
    public static Field<Object> field(String sql, Object... bindings) {
        return field(sql, Object.class, bindings);
    }

    @Support
    public static <T> Field<T> field(String sql, Class<T> type) {
        return field(sql, type, new Object[0]);
    }

    @Support
    public static <T> Field<T> field(String sql, Class<T> type, Object... bindings) {
        return field(sql, getDataType(type), bindings);
    }

    @Support
    public static <T> Field<T> field(String sql, DataType<T> type) {
        return field(sql, type, new Object[0]);
    }

    @Support
    public static <T> Field<T> field(String sql, DataType<T> type, Object... bindings) {
        return new SQLField<T>(sql, type, bindings);
    }

    @Support
    public static Field<Object> fieldByName(String... fieldName) {
        return fieldByName(Object.class, fieldName);
    }

    @Support
    public static <T> Field<T> fieldByName(Class<T> type, String... fieldName) {
        return fieldByName(getDataType(type), fieldName);
    }

    @Support
    public static <T> Field<T> fieldByName(DataType<T> type, String... fieldName) {
        return new QualifiedField<T>(type, fieldName);
    }

    @Support
    public static <T> Field<T> function(String name, Class<T> type, Field<?>... arguments) {
        return function(name, getDataType(type), nullSafe(arguments));
    }

    @Support
    public static <T> Field<T> function(String name, DataType<T> type, Field<?>... arguments) {
        return new Function<T>(name, type, nullSafe(arguments));
    }

    @Support
    public static Condition condition(String sql) {
        return condition(sql, new Object[0]);
    }

    @Support
    public static Condition condition(String sql, Object... bindings) {
        return new SQLCondition(sql, bindings);
    }

    @Override
    public final Query query(String sql) {
        return query(sql, new Object[0]);
    }

    @Override
    public final Query query(String sql, Object... bindings) {
        return new SQLQuery(this, sql, bindings);
    }

    @Override
    public final Result<Record> fetch(String sql) {
        return resultQuery(sql).fetch();
    }

    @Override
    public final Result<Record> fetch(String sql, Object... bindings) {
        return resultQuery(sql, bindings).fetch();
    }

    @Override
    public final Cursor<Record> fetchLazy(String sql) throws DataAccessException {
        return resultQuery(sql).fetchLazy();
    }

    @Override
    public final Cursor<Record> fetchLazy(String sql, Object... bindings) throws DataAccessException {
        return resultQuery(sql, bindings).fetchLazy();
    }

    @Override
    public final List<Result<Record>> fetchMany(String sql) {
        return resultQuery(sql).fetchMany();
    }

    @Override
    public final List<Result<Record>> fetchMany(String sql, Object... bindings) {
        return resultQuery(sql, bindings).fetchMany();
    }

    @Override
    public final Record fetchOne(String sql) {
        return resultQuery(sql).fetchOne();
    }

    @Override
    public final Record fetchOne(String sql, Object... bindings) {
        return resultQuery(sql, bindings).fetchOne();
    }

    @Override
    public final int execute(String sql) throws DataAccessException {
        return query(sql).execute();
    }

    @Override
    public final int execute(String sql, Object... bindings) throws DataAccessException {
        return query(sql, bindings).execute();
    }

    @Override
    public final ResultQuery<Record> resultQuery(String sql) throws DataAccessException {
        return resultQuery(sql, new Object[0]);
    }

    @Override
    public final ResultQuery<Record> resultQuery(String sql, Object... bindings) throws DataAccessException {
        return new SQLResultQuery(this, sql, bindings);
    }

    @Override
    public final Result<Record> fetch(ResultSet rs) {
        ExecuteContext ctx = new DefaultExecuteContext(this);
        ExecuteListener listener = new ExecuteListeners(ctx);
        try {
            FieldProvider fields = new MetaDataFieldProvider(this, rs.getMetaData());
            ctx.resultSet(rs);
            return new CursorImpl<Record>(ctx, listener, fields).fetch();
        } catch (SQLException e) {
            ctx.sqlException(e);
            listener.exception(ctx);
            throw ctx.exception();
        }
    }

    @Support
    public static Condition trueCondition() {
        return new TrueCondition();
    }

    @Support
    public static Condition falseCondition() {
        return new FalseCondition();
    }

    @Support
    public static Condition exists(Select<?> query) {
        return new SelectQueryAsExistsCondition(query, ExistsOperator.EXISTS);
    }

    @Support
    public static Condition notExists(Select<?> query) {
        return new SelectQueryAsExistsCondition(query, ExistsOperator.NOT_EXISTS);
    }

    @Override
    public final <R extends Record> SimpleSelectWhereStep<R> selectFrom(Table<R> table) {
        return new SimpleSelectImpl<R>(this, table);
    }

    @Override
    public final SelectSelectStep select(Field<?>... fields) {
        return new SelectImpl(this).select(fields);
    }

    @Override
    public final SelectSelectStep selectZero() {
        return new SelectImpl(this).select(zero());
    }

    @Override
    public final SelectSelectStep selectOne() {
        return new SelectImpl(this).select(one());
    }

    @Override
    public final SelectSelectStep selectCount() {
        return new SelectImpl(this).select(count());
    }

    @Override
    public final SelectSelectStep selectDistinct(Field<?>... fields) {
        return new SelectImpl(this, true).select(fields);
    }

    @Override
    public final SelectSelectStep select(Collection<? extends Field<?>> fields) {
        return new SelectImpl(this).select(fields);
    }

    @Override
    public final SelectSelectStep selectDistinct(Collection<? extends Field<?>> fields) {
        return new SelectImpl(this, true).select(fields);
    }

    @Override
    public final SelectQuery selectQuery() {
        return new SelectQueryImpl(this);
    }

    @Override
    public final <R extends Record> SimpleSelectQuery<R> selectQuery(TableLike<R> table) {
        return new SimpleSelectQueryImpl<R>(this, table);
    }

    @Override
    public final <R extends Record> InsertQuery<R> insertQuery(Table<R> into) {
        return new InsertQueryImpl<R>(this, into);
    }

    @Override
    public final <R extends Record> InsertSetStep<R> insertInto(Table<R> into) {
        return new InsertImpl<R>(this, into, Collections.<Field<?>>emptyList());
    }

    @Override
    public final <R extends Record> InsertValuesStep<R> insertInto(Table<R> into, Field<?>... fields) {
        return new InsertImpl<R>(this, into, Arrays.asList(fields));
    }

    @Override
    public final <R extends Record> InsertValuesStep<R> insertInto(Table<R> into, Collection<? extends Field<?>> fields) {
        return new InsertImpl<R>(this, into, fields);
    }

    @Override
    @Deprecated
    public final <R extends Record> Insert<R> insertInto(Table<R> into, Select<?> select) {
        return new InsertSelectQueryImpl<R>(this, into, into.getFields(), select);
    }

    @Override
    public final <R extends Record> UpdateQuery<R> updateQuery(Table<R> table) {
        return new UpdateQueryImpl<R>(this, table);
    }

    @Override
    public final <R extends Record> UpdateSetStep<R> update(Table<R> table) {
        return new UpdateImpl<R>(this, table);
    }

    @Override
    public final <R extends Record> MergeUsingStep<R> mergeInto(Table<R> table) {
        return new MergeImpl<R>(this, table);
    }

    @Override
    public final <R extends Record> DeleteQuery<R> deleteQuery(Table<R> table) {
        return new DeleteQueryImpl<R>(this, table);
    }

    @Override
    public final <R extends Record> DeleteWhereStep<R> delete(Table<R> table) {
        return new DeleteImpl<R>(this, table);
    }

    @Override
    public final Batch batch(Query... queries) {
        return new BatchMultiple(this, queries);
    }

    @Override
    public final Batch batch(Collection<? extends Query> queries) {
        return batch(queries.toArray(new Query[queries.size()]));
    }

    @Override
    public final BatchBindStep batch(Query query) {
        return new BatchSingle(this, query);
    }

    @Override
    public final Batch batchStore(UpdatableRecord<?>... records) {
        return new BatchStore(this, records);
    }

    @Override
    public final <R extends Record> Truncate<R> truncate(Table<R> table) {
        return new TruncateImpl<R>(this, table);
    }

    @Override
    public final BigInteger lastID() {
        switch(getDialect()) {
            case DERBY:
                {
                    Field<BigInteger> field = field("identity_val_local()", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            case H2:
            case HSQLDB:
                {
                    Field<BigInteger> field = field("identity()", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            case INGRES:
                {
                    Field<BigInteger> field = field("last_identity()", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            case CUBRID:
            case MYSQL:
                {
                    Field<BigInteger> field = field("last_insert_id()", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            case SQLITE:
                {
                    Field<BigInteger> field = field("last_insert_rowid()", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            case ASE:
            case SQLSERVER:
            case SYBASE:
                {
                    Field<BigInteger> field = field("@@identity", BigInteger.class);
                    return select(field).fetchOne(field);
                }
            default:
                throw new SQLDialectNotSupportedException("identity functionality not supported by " + getDialect());
        }
    }

    @Override
    public final <T extends Number> T nextval(Sequence<T> sequence) {
        Field<T> nextval = sequence.nextval();
        return select(nextval).fetchOne(nextval);
    }

    @Override
    public final <T extends Number> T currval(Sequence<T> sequence) {
        Field<T> currval = sequence.currval();
        return select(currval).fetchOne(currval);
    }

    @SuppressWarnings("deprecation")
    @Override
    public final int use(Schema schema) {
        int result = 0;
        try {
            String schemaName = render(schema);
            switch(dialect) {
                case DB2:
                case DERBY:
                case H2:
                case HSQLDB:
                    result = query("set schema " + schemaName).execute();
                    break;
                case ASE:
                case MYSQL:
                case SYBASE:
                    result = query("use " + schemaName).execute();
                    break;
                case ORACLE:
                    result = query("alter session set current_schema = " + schemaName).execute();
                    break;
                case POSTGRES:
                    result = query("set search_path = " + schemaName).execute();
                    break;
                case SQLSERVER:
                    break;
                case CUBRID:
                case SQLITE:
                    break;
            }
        } finally {
            getRenderMapping(settings).setDefaultSchema(schema.getName());
            mapping.use(schema);
        }
        return result;
    }

    @Override
    public final int use(String schema) {
        return use(new SchemaImpl(schema));
    }

    @Override
    public final <R extends UDTRecord<R>> R newRecord(UDT<R> type) {
        return Util.newRecord(type, this);
    }

    @Override
    public final <R extends TableRecord<R>> R newRecord(Table<R> table) {
        return Util.newRecord(table, this);
    }

    @Override
    public final <R extends TableRecord<R>> R newRecord(Table<R> table, Object source) {
        R result = newRecord(table);
        result.from(source);
        return result;
    }

    @Support
    public static Case decode() {
        return new CaseImpl();
    }

    @Support
    public static <Z, T> Field<Z> decode(T value, T search, Z result) {
        return decode(value, search, result, new Object[0]);
    }

    @Support
    public static <Z, T> Field<Z> decode(T value, T search, Z result, Object... more) {
        return decode(val(value), val(search), val(result), vals(more).toArray(new Field[0]));
    }

    @Support
    public static <Z, T> Field<Z> decode(Field<T> value, Field<T> search, Field<Z> result) {
        return decode(nullSafe(value), nullSafe(search), nullSafe(result), new Field[0]);
    }

    @Support
    public static <Z, T> Field<Z> decode(Field<T> value, Field<T> search, Field<Z> result, Field<?>... more) {
        return new Decode<T, Z>(nullSafe(value), nullSafe(search), nullSafe(result), nullSafe(more));
    }

    @Support
    public static <T> Field<T> cast(Object value, Field<T> as) {
        return val(value, as).cast(as);
    }

    @Support
    public static <T> Field<T> castNull(Field<T> as) {
        return NULL().cast(as);
    }

    @Support
    public static <T> Field<T> cast(Object value, Class<? extends T> type) {
        return val(value, type).cast(type);
    }

    @Support
    public static <T> Field<T> castNull(DataType<T> type) {
        return NULL().cast(type);
    }

    @Support
    public static <T> Field<T> cast(Object value, DataType<T> type) {
        return val(value, type).cast(type);
    }

    @Support
    public static <T> Field<T> castNull(Class<? extends T> type) {
        return NULL().cast(type);
    }

    @SuppressWarnings("unchecked")
    static <T> Field<T>[] castAll(Class<? extends T> type, Field<?>... fields) {
        Field<?>[] castFields = new Field<?>[fields.length];
        for (int i = 0; i < fields.length; i++) {
            castFields[i] = fields[i].cast(type);
        }
        return (Field<T>[]) castFields;
    }

    @Support
    public static <T> Field<T> coalesce(T value, T... values) {
        return coalesce(val(value), vals(values).toArray(new Field[0]));
    }

    @Support
    public static <T> Field<T> coalesce(Field<T> field, Field<?>... fields) {
        return function("coalesce", nullSafeDataType(field), nullSafe(combine(field, fields)));
    }

    @Support
    public static <T> Field<T> nvl(T value, T defaultValue) {
        return nvl(val(value), val(defaultValue));
    }

    @Support
    public static <T> Field<T> nvl(T value, Field<T> defaultValue) {
        return nvl(val(value), nullSafe(defaultValue));
    }

    @Support
    public static <T> Field<T> nvl(Field<T> value, T defaultValue) {
        return nvl(nullSafe(value), val(defaultValue));
    }

    @Support
    public static <T> Field<T> nvl(Field<T> value, Field<T> defaultValue) {
        return new Nvl<T>(nullSafe(value), nullSafe(defaultValue));
    }

    @Support
    public static <Z> Field<Z> nvl2(Field<?> value, Z valueIfNotNull, Z valueIfNull) {
        return nvl2(nullSafe(value), val(valueIfNotNull), val(valueIfNull));
    }

    @Support
    public static <Z> Field<Z> nvl2(Field<?> value, Z valueIfNotNull, Field<Z> valueIfNull) {
        return nvl2(nullSafe(value), val(valueIfNotNull), nullSafe(valueIfNull));
    }

    @Support
    public static <Z> Field<Z> nvl2(Field<?> value, Field<Z> valueIfNotNull, Z valueIfNull) {
        return nvl2(nullSafe(value), nullSafe(valueIfNotNull), val(valueIfNull));
    }

    @Support
    public static <Z> Field<Z> nvl2(Field<?> value, Field<Z> valueIfNotNull, Field<Z> valueIfNull) {
        return new Nvl2<Z>(nullSafe(value), nullSafe(valueIfNotNull), nullSafe(valueIfNull));
    }

    @Support
    public static <T> Field<T> nullif(T value, T other) {
        return nullif(val(value), val(other));
    }

    @Support
    public static <T> Field<T> nullif(T value, Field<T> other) {
        return nullif(val(value), nullSafe(other));
    }

    @Support
    public static <T> Field<T> nullif(Field<T> value, T other) {
        return nullif(nullSafe(value), val(other));
    }

    @Support
    public static <T> Field<T> nullif(Field<T> value, Field<T> other) {
        return function("nullif", nullSafeDataType(value), nullSafe(value), nullSafe(other));
    }

    @Support
    public static Field<String> upper(String value) {
        return upper(val(value));
    }

    @Support
    public static Field<String> upper(Field<String> field) {
        return function("upper", SQLDataType.VARCHAR, nullSafe(field));
    }

    @Support
    public static Field<String> lower(String value) {
        return lower(val(value, String.class));
    }

    @Support
    public static Field<String> lower(Field<String> value) {
        return function("lower", SQLDataType.VARCHAR, nullSafe(value));
    }

    @Support
    public static Field<String> trim(String value) {
        return trim(val(value, String.class));
    }

    @Support
    public static Field<String> trim(Field<String> field) {
        return new Trim(nullSafe(field));
    }

    @Support
    public static Field<String> rtrim(String value) {
        return rtrim(val(value));
    }

    @Support
    public static Field<String> rtrim(Field<String> field) {
        return function("rtrim", SQLDataType.VARCHAR, nullSafe(field));
    }

    @Support
    public static Field<String> ltrim(String value) {
        return ltrim(val(value, String.class));
    }

    @Support
    public static Field<String> ltrim(Field<String> value) {
        return function("ltrim", SQLDataType.VARCHAR, nullSafe(value));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> rpad(Field<String> field, int length) {
        return rpad(nullSafe(field), val(length));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> rpad(Field<String> field, Field<? extends Number> length) {
        return new Rpad(nullSafe(field), nullSafe(length));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> rpad(Field<String> field, int length, char character) {
        return rpad(field, length, Character.toString(character));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> rpad(Field<String> field, int length, String character) {
        return rpad(nullSafe(field), val(length), val(character, String.class));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> rpad(Field<String> field, Field<? extends Number> length, Field<String> character) {
        return new Rpad(nullSafe(field), nullSafe(length), nullSafe(character));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> lpad(Field<String> field, int length) {
        return lpad(nullSafe(field), val(length));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> lpad(Field<String> field, Field<? extends Number> length) {
        return new Lpad(nullSafe(field), nullSafe(length));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> lpad(Field<String> field, int length, char character) {
        return lpad(field, length, Character.toString(character));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> lpad(Field<String> field, int length, String character) {
        return lpad(nullSafe(field), val(length), val(character, String.class));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> lpad(Field<String> field, Field<? extends Number> length, Field<String> character) {
        return new Lpad(nullSafe(field), nullSafe(length), nullSafe(character));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> repeat(String field, int count) {
        return repeat(val(field, String.class), val(count));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> repeat(String field, Field<? extends Number> count) {
        return repeat(val(field, String.class), nullSafe(count));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> repeat(Field<String> field, int count) {
        return repeat(nullSafe(field), val(count));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> repeat(Field<String> field, Field<? extends Number> count) {
        return new Repeat(nullSafe(field), nullSafe(count));
    }

    @Support
    public static String escape(String value, char escape) {
        String esc = "" + escape;
        return value.replace(esc, esc + esc).replace("%", esc + "%").replace("_", esc + "_");
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static Field<String> escape(Field<String> field, char escape) {
        Field<String> replace = field;
        String esc = "" + escape;
        replace = replace(replace, inline(esc), inline(esc + esc));
        replace = replace(replace, inline("%"), inline(esc + "%"));
        replace = replace(replace, inline("_"), inline(esc + "_"));
        return replace;
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static Field<String> replace(Field<String> field, String search) {
        return replace(nullSafe(field), val(search, String.class));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static Field<String> replace(Field<String> field, Field<String> search) {
        return new Replace(nullSafe(field), nullSafe(search));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static Field<String> replace(Field<String> field, String search, String replace) {
        return replace(nullSafe(field), val(search, String.class), val(replace, String.class));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static Field<String> replace(Field<String> field, Field<String> search, Field<String> replace) {
        return new Replace(nullSafe(field), nullSafe(search), nullSafe(replace));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> position(String in, String search) {
        return position(val(in, String.class), val(search, String.class));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> position(String in, Field<String> search) {
        return position(val(in, String.class), nullSafe(search));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> position(Field<String> in, String search) {
        return position(nullSafe(in), val(search, String.class));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> position(Field<String> in, Field<String> search) {
        return new Position(nullSafe(search), nullSafe(in));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> ascii(String field) {
        return ascii(val(field, String.class));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<Integer> ascii(Field<String> field) {
        return new Ascii(nullSafe(field));
    }

    @Support
    public static Field<String> concat(String... values) {
        return concat(vals((Object[]) values).toArray(new Field[0]));
    }

    @Support
    public static Field<String> concat(Field<?>... fields) {
        return new Concat(nullSafe(fields));
    }

    @Support
    public static Field<String> substring(Field<String> field, int startingPosition) {
        return substring(nullSafe(field), val(startingPosition));
    }

    @Support
    public static Field<String> substring(Field<String> field, Field<? extends Number> startingPosition) {
        return new Substring(nullSafe(field), nullSafe(startingPosition));
    }

    @Support
    public static Field<String> substring(Field<String> field, int startingPosition, int length) {
        return substring(nullSafe(field), val(startingPosition), val(length));
    }

    @Support
    public static Field<String> substring(Field<String> field, Field<? extends Number> startingPosition, Field<? extends Number> length) {
        return new Substring(nullSafe(field), nullSafe(startingPosition), nullSafe(length));
    }

    @Support
    public static Field<Integer> length(String value) {
        return length(val(value, String.class));
    }

    @Support
    public static Field<Integer> length(Field<String> field) {
        return charLength(field);
    }

    @Support
    public static Field<Integer> charLength(String value) {
        return charLength(val(value));
    }

    @Support
    public static Field<Integer> charLength(Field<String> field) {
        return new Function<Integer>(Term.CHAR_LENGTH, SQLDataType.INTEGER, nullSafe(field));
    }

    @Support
    public static Field<Integer> bitLength(String value) {
        return bitLength(val(value));
    }

    @Support
    public static Field<Integer> bitLength(Field<String> field) {
        return new Function<Integer>(Term.BIT_LENGTH, SQLDataType.INTEGER, nullSafe(field));
    }

    @Support
    public static Field<Integer> octetLength(String value) {
        return octetLength(val(value, String.class));
    }

    @Support
    public static Field<Integer> octetLength(Field<String> field) {
        return new Function<Integer>(Term.OCTET_LENGTH, SQLDataType.INTEGER, nullSafe(field));
    }

    @Support
    public static Field<Date> currentDate() {
        return new CurrentDate();
    }

    @Support
    public static Field<Time> currentTime() {
        return new CurrentTime();
    }

    @Support
    public static Field<Timestamp> currentTimestamp() {
        return new CurrentTimestamp();
    }

    @Support
    public static Field<Integer> dateDiff(Date date1, Date date2) {
        return dateDiff(val(date1), val(date2));
    }

    @Support
    public static Field<Integer> dateDiff(Field<Date> date1, Date date2) {
        return dateDiff(nullSafe(date1), val(date2));
    }

    @Support
    public static Field<Date> dateAdd(Date date, Number interval) {
        return dateAdd(val(date), val(interval));
    }

    @Support
    public static Field<Date> dateAdd(Field<Date> date, Field<? extends Number> interval) {
        return nullSafe(date).add(interval);
    }

    @Support
    public static Field<Integer> dateDiff(Date date1, Field<Date> date2) {
        return dateDiff(val(date1), nullSafe(date2));
    }

    @Support
    public static Field<Integer> dateDiff(Field<Date> date1, Field<Date> date2) {
        return new DateDiff(nullSafe(date1), nullSafe(date2));
    }

    @Support
    public static Field<Timestamp> timestampAdd(Timestamp timestamp, Number interval) {
        return timestampAdd(val(timestamp), val(interval));
    }

    @Support
    public static Field<Timestamp> timestampAdd(Field<Timestamp> timestamp, Field<? extends Number> interval) {
        return nullSafe(timestamp).add(interval);
    }

    @Support
    public static Field<DayToSecond> timestampDiff(Timestamp timestamp1, Timestamp timestamp2) {
        return timestampDiff(val(timestamp1), val(timestamp2));
    }

    @Support
    public static Field<DayToSecond> timestampDiff(Field<Timestamp> timestamp1, Timestamp timestamp2) {
        return timestampDiff(nullSafe(timestamp1), val(timestamp2));
    }

    @Support
    public static Field<DayToSecond> timestampDiff(Timestamp timestamp1, Field<Timestamp> timestamp2) {
        return timestampDiff(val(timestamp1), nullSafe(timestamp2));
    }

    @Support
    public static Field<DayToSecond> timestampDiff(Field<Timestamp> timestamp1, Field<Timestamp> timestamp2) {
        return new TimestampDiff(nullSafe(timestamp1), nullSafe(timestamp2));
    }

    static Field<Date> trunc(Date date) {
        return trunc(date, DatePart.DAY);
    }

    static Field<Date> trunc(Date date, DatePart part) {
        return trunc(val(date), part);
    }

    static Field<Timestamp> trunc(Timestamp timestamp) {
        return trunc(timestamp, DatePart.DAY);
    }

    static Field<Timestamp> trunc(Timestamp timestamp, DatePart part) {
        return trunc(val(timestamp), part);
    }

    static <T extends java.util.Date> Field<T> trunc(Field<T> date) {
        return trunc(date, DatePart.DAY);
    }

    static <T extends java.util.Date> Field<T> trunc(Field<T> date, DatePart part) {
        throw new UnsupportedOperationException("This is not yet implemented");
    }

    @Support
    public static Field<Integer> extract(java.util.Date value, DatePart datePart) {
        return extract(val(value), datePart);
    }

    @Support
    public static Field<Integer> extract(Field<? extends java.util.Date> field, DatePart datePart) {
        return new Extract(nullSafe(field), datePart);
    }

    @Support
    public static Field<Integer> year(java.util.Date value) {
        return extract(value, DatePart.YEAR);
    }

    @Support
    public static Field<Integer> year(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.YEAR);
    }

    @Support
    public static Field<Integer> month(java.util.Date value) {
        return extract(value, DatePart.MONTH);
    }

    @Support
    public static Field<Integer> month(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.MONTH);
    }

    @Support
    public static Field<Integer> day(java.util.Date value) {
        return extract(value, DatePart.DAY);
    }

    @Support
    public static Field<Integer> day(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.DAY);
    }

    @Support
    public static Field<Integer> hour(java.util.Date value) {
        return extract(value, DatePart.HOUR);
    }

    @Support
    public static Field<Integer> hour(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.HOUR);
    }

    @Support
    public static Field<Integer> minute(java.util.Date value) {
        return extract(value, DatePart.MINUTE);
    }

    @Support
    public static Field<Integer> minute(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.MINUTE);
    }

    @Support
    public static Field<Integer> second(java.util.Date value) {
        return extract(value, DatePart.SECOND);
    }

    @Support
    public static Field<Integer> second(Field<? extends java.util.Date> field) {
        return extract(field, DatePart.SECOND);
    }

    @Support({ CUBRID, DB2, MYSQL, ORACLE, SQLSERVER, SYBASE })
    public static Field<?> rollup(Field<?>... fields) {
        return new Rollup(nullSafe(fields));
    }

    @Support({ DB2, ORACLE, SQLSERVER, SYBASE })
    public static Field<?> cube(Field<?>... fields) {
        return function("cube", Object.class, nullSafe(fields));
    }

    @SuppressWarnings("unchecked")
    @Support({ DB2, ORACLE, SQLSERVER, SYBASE })
    public static Field<?> groupingSets(Field<?>... fields) {
        List<Field<?>>[] array = new List[fields.length];
        for (int i = 0; i < fields.length; i++) {
            array[i] = Arrays.<Field<?>>asList(fields[i]);
        }
        return groupingSets(array);
    }

    @SuppressWarnings("unchecked")
    @Support({ DB2, ORACLE, SQLSERVER, SYBASE })
    public static Field<?> groupingSets(Field<?>[]... fieldSets) {
        List<Field<?>>[] array = new List[fieldSets.length];
        for (int i = 0; i < fieldSets.length; i++) {
            array[i] = Arrays.asList(fieldSets[i]);
        }
        return groupingSets(array);
    }

    @Support({ DB2, ORACLE, SQLSERVER, SYBASE })
    public static Field<?> groupingSets(Collection<Field<?>>... fieldSets) {
        WrappedList[] array = new WrappedList[fieldSets.length];
        for (int i = 0; i < fieldSets.length; i++) {
            array[i] = new WrappedList(new FieldList(fieldSets[i]));
        }
        return new Function<Object>("grouping sets", SQLDataType.OTHER, array);
    }

    @Support({ DB2, ORACLE, SQLSERVER, SYBASE })
    public static Field<Integer> grouping(Field<?> field) {
        return function("grouping", Integer.class, nullSafe(field));
    }

    @Support({ ORACLE, SQLSERVER })
    public static Field<Integer> groupingId(Field<?>... fields) {
        return function("grouping_id", Integer.class, nullSafe(fields));
    }

    @Support({ CUBRID, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SYBASE, SQLITE })
    public static Field<Integer> bitCount(Number value) {
        return bitCount(val(value));
    }

    @Support({ CUBRID, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SYBASE, SQLITE })
    public static Field<Integer> bitCount(Field<? extends Number> field) {
        return new BitCount(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNot(T value) {
        return bitNot(val(value));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNot(Field<T> field) {
        return new Neg<T>(nullSafe(field), ExpressionOperator.BIT_NOT);
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitAnd(T value1, T value2) {
        return bitAnd(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitAnd(T value1, Field<T> value2) {
        return bitAnd(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitAnd(Field<T> value1, T value2) {
        return bitAnd(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitAnd(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_AND, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNand(T value1, T value2) {
        return bitNand(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNand(T value1, Field<T> value2) {
        return bitNand(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNand(Field<T> value1, T value2) {
        return bitNand(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNand(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_NAND, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitOr(T value1, T value2) {
        return bitOr(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitOr(T value1, Field<T> value2) {
        return bitOr(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitOr(Field<T> value1, T value2) {
        return bitOr(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitOr(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_OR, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNor(T value1, T value2) {
        return bitNor(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNor(T value1, Field<T> value2) {
        return bitNor(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNor(Field<T> value1, T value2) {
        return bitNor(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitNor(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_NOR, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXor(T value1, T value2) {
        return bitXor(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXor(T value1, Field<T> value2) {
        return bitXor(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXor(Field<T> value1, T value2) {
        return bitXor(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXor(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_XOR, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXNor(T value1, T value2) {
        return bitXNor(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXNor(T value1, Field<T> value2) {
        return bitXNor(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXNor(Field<T> value1, T value2) {
        return bitXNor(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> bitXNor(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.BIT_XNOR, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shl(T value1, T value2) {
        return shl(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shl(T value1, Field<T> value2) {
        return shl(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shl(Field<T> value1, T value2) {
        return shl(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shl(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.SHL, nullSafe(field1), nullSafe(field2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shr(T value1, T value2) {
        return shr(val(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shr(T value1, Field<T> value2) {
        return shr(val(value1), nullSafe(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shr(Field<T> value1, T value2) {
        return shr(nullSafe(value1), val(value2));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE, SQLITE })
    public static <T extends Number> Field<T> shr(Field<T> field1, Field<T> field2) {
        return new Expression<T>(ExpressionOperator.SHR, nullSafe(field1), nullSafe(field2));
    }

    @Support
    public static <T> Field<T> greatest(T value, T... values) {
        return greatest(val(value), vals(values).toArray(new Field[0]));
    }

    @Support
    public static <T> Field<T> greatest(Field<T> field, Field<?>... others) {
        return new Greatest<T>(nullSafeDataType(field), nullSafe(combine(field, others)));
    }

    @Support
    public static <T> Field<T> least(T value, T... values) {
        return least(val(value), vals(values).toArray(new Field[0]));
    }

    @Support
    public static <T> Field<T> least(Field<T> field, Field<?>... others) {
        return new Least<T>(nullSafeDataType(field), nullSafe(combine(field, others)));
    }

    @Support
    public static Field<Integer> sign(Number value) {
        return sign(val(value));
    }

    @Support
    public static Field<Integer> sign(Field<? extends Number> field) {
        return new Sign(nullSafe(field));
    }

    @Support
    public static <T extends Number> Field<T> abs(T value) {
        return abs(val(value));
    }

    @Support
    public static <T extends Number> Field<T> abs(Field<T> field) {
        return function("abs", nullSafeDataType(field), nullSafe(field));
    }

    @Support
    public static <T extends Number> Field<T> round(T value) {
        return round(val(value));
    }

    @Support
    public static <T extends Number> Field<T> round(Field<T> field) {
        return new Round<T>(nullSafe(field));
    }

    @Support
    public static <T extends Number> Field<T> round(T value, int decimals) {
        return round(val(value), decimals);
    }

    @Support
    public static <T extends Number> Field<T> round(Field<T> field, int decimals) {
        return new Round<T>(nullSafe(field), decimals);
    }

    @Support
    public static <T extends Number> Field<T> floor(T value) {
        return floor(val(value));
    }

    @Support
    public static <T extends Number> Field<T> floor(Field<T> field) {
        return new Floor<T>(nullSafe(field));
    }

    @Support
    public static <T extends Number> Field<T> ceil(T value) {
        return ceil(val(value));
    }

    @Support
    public static <T extends Number> Field<T> ceil(Field<T> field) {
        return new Ceil<T>(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static <T extends Number> Field<T> trunc(T number) {
        return trunc(val(number), inline(0));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static <T extends Number> Field<T> trunc(T number, int decimals) {
        return trunc(val(number), inline(decimals));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static <T extends Number> Field<T> trunc(Field<T> number, int decimals) {
        return trunc(nullSafe(number), inline(decimals));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static <T extends Number> Field<T> trunc(T number, Field<Integer> decimals) {
        return trunc(val(number), nullSafe(decimals));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static <T extends Number> Field<T> trunc(Field<T> number, Field<Integer> decimals) {
        return new Trunc<T>(nullSafe(number), nullSafe(decimals));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sqrt(Number value) {
        return sqrt(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sqrt(Field<? extends Number> field) {
        return new Sqrt(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> exp(Number value) {
        return exp(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> exp(Field<? extends Number> field) {
        return function("exp", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> ln(Number value) {
        return ln(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> ln(Field<? extends Number> field) {
        return new Ln(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> log(Number value, int base) {
        return log(val(value), base);
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> log(Field<? extends Number> field, int base) {
        return new Ln(nullSafe(field), base);
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> power(Number value, Number exponent) {
        return power(val(value), val(exponent));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> power(Field<? extends Number> field, Number exponent) {
        return power(nullSafe(field), val(exponent));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> power(Number value, Field<? extends Number> exponent) {
        return power(val(value), nullSafe(exponent));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> power(Field<? extends Number> field, Field<? extends Number> exponent) {
        return new Power(nullSafe(field), nullSafe(exponent));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> acos(Number value) {
        return acos(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> acos(Field<? extends Number> field) {
        return function("acos", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> asin(Number value) {
        return asin(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> asin(Field<? extends Number> field) {
        return function("asin", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan(Number value) {
        return atan(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan(Field<? extends Number> field) {
        return function("atan", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan2(Number x, Number y) {
        return atan2(val(x), val(y));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan2(Number x, Field<? extends Number> y) {
        return atan2(val(x), nullSafe(y));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan2(Field<? extends Number> x, Number y) {
        return atan2(nullSafe(x), val(y));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> atan2(Field<? extends Number> x, Field<? extends Number> y) {
        return new Function<BigDecimal>(Term.ATAN2, SQLDataType.NUMERIC, nullSafe(x), nullSafe(y));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cos(Number value) {
        return cos(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cos(Field<? extends Number> field) {
        return function("cos", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sin(Number value) {
        return sin(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sin(Field<? extends Number> field) {
        return function("sin", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> tan(Number value) {
        return tan(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> tan(Field<? extends Number> field) {
        return function("tan", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cot(Number value) {
        return cot(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cot(Field<? extends Number> field) {
        return new Cot(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sinh(Number value) {
        return sinh(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> sinh(Field<? extends Number> field) {
        return new Sinh(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cosh(Number value) {
        return cosh(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> cosh(Field<? extends Number> field) {
        return new Cosh(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> tanh(Number value) {
        return tanh(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> tanh(Field<? extends Number> field) {
        return new Tanh(nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> coth(Number value) {
        return coth(val(value));
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<BigDecimal> coth(Field<? extends Number> field) {
        field = nullSafe(field);
        return exp(field.mul(2)).add(1).div(exp(field.mul(2)).sub(1));
    }

    @Support
    public static Field<BigDecimal> deg(Number value) {
        return deg(val(value));
    }

    @Support
    public static Field<BigDecimal> deg(Field<? extends Number> field) {
        return new Degrees(nullSafe(field));
    }

    @Support
    public static Field<BigDecimal> rad(Number value) {
        return rad(val(value));
    }

    @Support
    public static Field<BigDecimal> rad(Field<? extends Number> field) {
        return new Radians(nullSafe(field));
    }

    @Support({ CUBRID, ORACLE })
    public static Field<Integer> level() {
        return field("level", Integer.class);
    }

    @Support({ CUBRID, ORACLE })
    public static Field<Boolean> connectByIsCycle() {
        return field("connect_by_iscycle", Boolean.class);
    }

    @Support({ CUBRID, ORACLE })
    public static Field<Boolean> connectByIsLeaf() {
        return field("connect_by_isleaf", Boolean.class);
    }

    @Support({ CUBRID, ORACLE })
    public static Field<String> sysConnectByPath(Field<?> field, String separator) {
        String escaped = "'" + separator.replace("'", "''") + "'";
        return function("sys_connect_by_path", String.class, field, literal(escaped));
    }

    @Support({ CUBRID, ORACLE })
    public static <T> Field<T> prior(Field<T> field) {
        return field("{prior} {0}", nullSafe(field).getDataType(), field);
    }

    @Support
    public static AggregateFunction<Integer> count() {
        return count(field("*", Integer.class));
    }

    @Support
    public static AggregateFunction<Integer> count(Field<?> field) {
        return new Function<Integer>("count", SQLDataType.INTEGER, nullSafe(field));
    }

    @Support
    public static AggregateFunction<Integer> countDistinct(Field<?> field) {
        return new Function<Integer>("count", true, SQLDataType.INTEGER, nullSafe(field));
    }

    @Support
    public static <T> AggregateFunction<T> max(Field<T> field) {
        return new Function<T>("max", nullSafeDataType(field), nullSafe(field));
    }

    @Support
    public static <T> AggregateFunction<T> maxDistinct(Field<T> field) {
        return new Function<T>("max", true, nullSafeDataType(field), nullSafe(field));
    }

    @Support
    public static <T> AggregateFunction<T> min(Field<T> field) {
        return new Function<T>("min", nullSafeDataType(field), nullSafe(field));
    }

    @Support
    public static <T> AggregateFunction<T> minDistinct(Field<T> field) {
        return new Function<T>("min", true, nullSafeDataType(field), nullSafe(field));
    }

    @Support
    public static AggregateFunction<BigDecimal> sum(Field<? extends Number> field) {
        return new Function<BigDecimal>("sum", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support
    public static AggregateFunction<BigDecimal> sumDistinct(Field<? extends Number> field) {
        return new Function<BigDecimal>("sum", true, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support
    public static AggregateFunction<BigDecimal> avg(Field<? extends Number> field) {
        return new Function<BigDecimal>("avg", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support
    public static AggregateFunction<BigDecimal> avgDistinct(Field<? extends Number> field) {
        return new Function<BigDecimal>("avg", true, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ HSQLDB, ORACLE, SYBASE })
    public static AggregateFunction<BigDecimal> median(Field<? extends Number> field) {
        return new Function<BigDecimal>("median", SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static AggregateFunction<BigDecimal> stddevPop(Field<? extends Number> field) {
        return new Function<BigDecimal>(Term.STDDEV_POP, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static AggregateFunction<BigDecimal> stddevSamp(Field<? extends Number> field) {
        return new Function<BigDecimal>(Term.STDDEV_SAMP, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static AggregateFunction<BigDecimal> varPop(Field<? extends Number> field) {
        return new Function<BigDecimal>(Term.VAR_POP, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ ASE, CUBRID, DB2, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static AggregateFunction<BigDecimal> varSamp(Field<? extends Number> field) {
        return new Function<BigDecimal>(Term.VAR_SAMP, SQLDataType.NUMERIC, nullSafe(field));
    }

    @Support({ CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SYBASE })
    public static OrderedAggregateFunction<String> listAgg(Field<?> field) {
        return new Function<String>(Term.LIST_AGG, SQLDataType.VARCHAR, nullSafe(field));
    }

    @Support({ CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SYBASE })
    public static OrderedAggregateFunction<String> listAgg(Field<?> field, String separator) {
        Field<String> literal = literal("'" + separator.replace("'", "''") + "'");
        return new Function<String>(Term.LIST_AGG, SQLDataType.VARCHAR, nullSafe(field), literal);
    }

    @Support({ CUBRID, DB2, H2, HSQLDB, MYSQL, ORACLE, POSTGRES, SYBASE })
    public static GroupConcatOrderByStep groupConcat(Field<?> field) {
        return new GroupConcat(nullSafe(field));
    }

    @Support({ CUBRID, H2, HSQLDB, MYSQL, POSTGRES, SYBASE })
    public static GroupConcatOrderByStep groupConcatDistinct(Field<?> field) {
        return new GroupConcat(nullSafe(field), true);
    }

    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER, SYBASE })
    public static WindowOverStep<Integer> rowNumber() {
        return new Function<Integer>("row_number", SQLDataType.INTEGER);
    }

    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER, SYBASE })
    public static WindowOverStep<Integer> rank() {
        return new Function<Integer>("rank", SQLDataType.INTEGER);
    }

    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER, SYBASE })
    public static WindowOverStep<Integer> denseRank() {
        return new Function<Integer>("dense_rank", SQLDataType.INTEGER);
    }

    @Support({ POSTGRES, ORACLE, SYBASE })
    public static WindowOverStep<BigDecimal> percentRank() {
        return new Function<BigDecimal>("percent_rank", SQLDataType.NUMERIC);
    }

    @Support({ POSTGRES, ORACLE, SYBASE })
    public static WindowOverStep<BigDecimal> cumeDist() {
        return new Function<BigDecimal>("cume_dist", SQLDataType.NUMERIC);
    }

    @Support({ POSTGRES, ORACLE, SQLSERVER })
    public static WindowOverStep<Integer> ntile(int number) {
        return new Function<Integer>("ntile", SQLDataType.INTEGER, field("" + number, Integer.class));
    }

    @Support({ DB2, POSTGRES, ORACLE, SYBASE })
    public static <T> WindowIgnoreNullsStep<T> firstValue(Field<T> field) {
        return new Function<T>("first_value", nullSafeDataType(field), nullSafe(field));
    }

    @Support({ DB2, POSTGRES, ORACLE, SYBASE })
    public static <T> WindowIgnoreNullsStep<T> lastValue(Field<T> field) {
        return new Function<T>("last_value", nullSafeDataType(field), nullSafe(field));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lead(Field<T> field) {
        return new Function<T>("lead", nullSafeDataType(field), nullSafe(field));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lead(Field<T> field, int offset) {
        return new Function<T>("lead", nullSafeDataType(field), nullSafe(field), literal(offset));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lead(Field<T> field, int offset, T defaultValue) {
        return lead(nullSafe(field), offset, val(defaultValue));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lead(Field<T> field, int offset, Field<T> defaultValue) {
        return new Function<T>("lead", nullSafeDataType(field), nullSafe(field), literal(offset), nullSafe(defaultValue));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lag(Field<T> field) {
        return new Function<T>("lag", nullSafeDataType(field), nullSafe(field));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lag(Field<T> field, int offset) {
        return new Function<T>("lag", nullSafeDataType(field), nullSafe(field), literal(offset));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lag(Field<T> field, int offset, T defaultValue) {
        return lag(nullSafe(field), offset, val(defaultValue));
    }

    @Support({ DB2, POSTGRES, ORACLE })
    public static <T> WindowIgnoreNullsStep<T> lag(Field<T> field, int offset, Field<T> defaultValue) {
        return new Function<T>("lag", nullSafeDataType(field), nullSafe(field), literal(offset), nullSafe(defaultValue));
    }

    @Support
    public static Param<Object> param(String name) {
        return param(name, Object.class);
    }

    @Support
    public static <T> Param<T> param(String name, Class<? extends T> type) {
        return param(name, SQLDataType.getDataType(null, type));
    }

    @Support
    public static <T> Param<T> param(String name, DataType<T> type) {
        return new Val<T>(null, type, name);
    }

    @Support
    public static <T> Param<T> param(String name, T value) {
        return new Val<T>(value, val(value).getDataType(), name);
    }

    @Support
    public static <T> Field<T> value(T value) {
        return val(value);
    }

    @Support
    public static <T> Field<T> value(Object value, Class<? extends T> type) {
        return val(value, type);
    }

    @Support
    public static <T> Field<T> value(Object value, Field<T> field) {
        return val(value, field);
    }

    @Support
    public static <T> Field<T> value(Object value, DataType<T> type) {
        return val(value, type);
    }

    @Support
    public static <T> Param<T> inline(T value) {
        Param<T> val = (Param<T>) val(value);
        val.setInline(true);
        return val;
    }

    @Support
    public static Param<String> inline(char character) {
        return inline("" + character);
    }

    @Support
    public static Param<String> inline(Character character) {
        return inline((character == null) ? null : ("" + character));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Support
    public static Param<String> inline(CharSequence character) {
        return (Param) inline((Object) ((character == null) ? null : ("" + character)));
    }

    @Support
    public static <T> Param<T> inline(Object value, Class<? extends T> type) {
        Param<T> val = (Param<T>) val(value, type);
        val.setInline(true);
        return val;
    }

    @Support
    public static <T> Param<T> inline(Object value, Field<T> field) {
        Param<T> val = (Param<T>) val(value, field);
        val.setInline(true);
        return val;
    }

    @Support
    public static <T> Param<T> inline(Object value, DataType<T> type) {
        Param<T> val = (Param<T>) val(value, type);
        val.setInline(true);
        return val;
    }

    @SuppressWarnings("unchecked")
    @Support
    public static <T> Field<T> val(T value) {
        if (value instanceof Field<?>) {
            return (Field<T>) value;
        } else {
            Class<?> type = (value == null) ? Object.class : value.getClass();
            return (Field<T>) val(value, getDataType(type));
        }
    }

    @Support
    public static <T> Field<T> val(Object value, Class<? extends T> type) {
        return val(value, getDataType(type));
    }

    @Support
    public static <T> Field<T> val(Object value, Field<T> field) {
        return val(value, nullSafeDataType(field));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Support
    public static <T> Field<T> val(Object value, DataType<T> type) {
        if (value instanceof Field<?>) {
            return (Field<T>) value;
        } else if (value instanceof UDTRecord) {
            return new UDTConstant((UDTRecord) value);
        } else if (value instanceof ArrayRecord) {
            return new ArrayConstant((ArrayRecord) value);
        } else {
            return new Val<T>(type.convert(value), type);
        }
    }

    @Support
    public static List<Field<?>> vals(Object... values) {
        FieldList result = new FieldList();
        if (values != null) {
            for (Object value : values) {
                if (value instanceof Field<?>) {
                    result.add((Field<?>) value);
                } else {
                    result.add(val(value));
                }
            }
        }
        return result;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    @Support
    public static <T> Field<T> literal(T literal) {
        if (literal == null) {
            return (Field<T>) NULL();
        } else {
            return literal(literal, (Class<T>) literal.getClass());
        }
    }

    @Deprecated
    @Support
    public static <T> Field<T> literal(Object literal, Class<T> type) {
        return literal(literal, getDataType(type));
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    @Support
    public static <T> Field<T> literal(Object literal, DataType<T> type) {
        if (literal == null) {
            return (Field<T>) NULL();
        } else {
            return field(literal.toString(), type);
        }
    }

    static Field<?> NULL() {
        return field("null");
    }

    static <T> Field<T> nullSafe(Field<T> field) {
        return field == null ? val((T) null) : field;
    }

    static Field<?>[] nullSafe(Field<?>... fields) {
        Field<?>[] result = new Field<?>[fields.length];
        for (int i = 0; i < fields.length; i++) {
            result[i] = nullSafe(fields[i]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static <T> DataType<T> nullSafeDataType(Field<T> field) {
        return (DataType<T>) (field == null ? SQLDataType.OTHER : field.getDataType());
    }

    @Support
    public static Field<Integer> zero() {
        return inline(0);
    }

    @Support
    public static Field<Integer> one() {
        return inline(1);
    }

    @Support
    public static Field<Integer> two() {
        return inline(2);
    }

    @Support
    public static Field<BigDecimal> pi() {
        return new Pi();
    }

    @Support
    public static Field<BigDecimal> e() {
        return new Euler();
    }

    @Support({ ASE, CUBRID, DB2, DERBY, H2, HSQLDB, INGRES, MYSQL, ORACLE, POSTGRES, SQLSERVER, SYBASE })
    public static Field<String> currentUser() {
        return new CurrentUser();
    }

    @Support
    public static Field<BigDecimal> rand() {
        return new Rand();
    }

    @Override
    public final <R extends Record> Result<R> fetch(Table<R> table) {
        return fetch(table, trueCondition());
    }

    @Override
    public final <R extends Record> Result<R> fetch(Table<R> table, Condition condition) {
        return selectFrom(table).where(condition).fetch();
    }

    @Override
    public final <R extends Record> R fetchOne(Table<R> table) {
        return filterOne(fetch(table));
    }

    @Override
    public final <R extends Record> R fetchOne(Table<R> table, Condition condition) {
        return filterOne(fetch(table, condition));
    }

    @Override
    public final <R extends Record> R fetchAny(Table<R> table) {
        return filterOne(selectFrom(table).limit(1).fetch());
    }

    @Override
    public final <R extends TableRecord<R>> int executeInsert(Table<R> table, R record) {
        InsertQuery<R> insert = insertQuery(table);
        insert.setRecord(record);
        return insert.execute();
    }

    @Override
    public final <R extends TableRecord<R>> int executeUpdate(Table<R> table, R record) {
        return executeUpdate(table, record, trueCondition());
    }

    @Override
    public final <R extends TableRecord<R>, T> int executeUpdate(Table<R> table, R record, Condition condition) {
        UpdateQuery<R> update = updateQuery(table);
        update.addConditions(condition);
        update.setRecord(record);
        return update.execute();
    }

    @Override
    public final <R extends TableRecord<R>> int executeUpdateOne(Table<R> table, R record) {
        return filterUpdateOne(executeUpdate(table, record));
    }

    @Override
    public final <R extends TableRecord<R>, T> int executeUpdateOne(Table<R> table, R record, Condition condition) {
        return filterUpdateOne(executeUpdate(table, record, condition));
    }

    @Override
    public final <R extends TableRecord<R>> int executeDelete(Table<R> table) {
        return executeDelete(table, trueCondition());
    }

    @Override
    public final <R extends TableRecord<R>, T> int executeDelete(Table<R> table, Condition condition) {
        DeleteQuery<R> delete = deleteQuery(table);
        delete.addConditions(condition);
        return delete.execute();
    }

    @Override
    public final <R extends TableRecord<R>> int executeDeleteOne(Table<R> table) {
        return executeDeleteOne(table, trueCondition());
    }

    @Override
    public final <R extends TableRecord<R>, T> int executeDeleteOne(Table<R> table, Condition condition) {
        DeleteQuery<R> delete = deleteQuery(table);
        delete.addConditions(condition);
        return filterDeleteOne(delete.execute());
    }

    @SuppressWarnings("deprecation")
    @Support
    public static <T> DataType<T> getDataType(Class<? extends T> type) {
        return FieldTypeHelper.getDataType(SQLDialect.SQL99, type);
    }

    static {
        try {
            Class.forName(SQLDataType.class.getName());
        } catch (Exception ignore) {
        }
    }

    private static int filterDeleteOne(int i) {
        return filterOne(i, "deleted");
    }

    private static int filterUpdateOne(int i) {
        return filterOne(i, "updated");
    }

    private static int filterOne(int i, String action) {
        if (i <= 1) {
            return i;
        } else {
            throw new InvalidResultException("Too many rows " + action + " : " + i);
        }
    }

    private static <R extends Record> R filterOne(List<R> list) {
        if (filterOne(list.size(), "selected") == 1) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        JAXB.marshal(settings, writer);
        return "Factory [\n\tconnected=" + (connection != null) + ",\n\tdialect=" + dialect + ",\n\tdata=" + data + ",\n\tsettings=\n\t\t" + writer.toString().trim().replace("\n", "\n\t\t") + "\n]";
    }

    static {
        for (SQLDialect dialect : SQLDialect.values()) {
            Factory.DEFAULT_INSTANCES[dialect.ordinal()] = new Factory(null, dialect);
        }
    }

    final static Factory getNewFactory(SQLDialect dialect) {
        return getNewFactory(DEFAULT_INSTANCES[dialect.ordinal()]);
    }

    final static Factory getStaticFactory(SQLDialect dialect) {
        return DEFAULT_INSTANCES[dialect.ordinal()];
    }

    @SuppressWarnings("deprecation")
    final static Factory getNewFactory(Configuration configuration) {
        if (configuration == null) {
            return getNewFactory(DefaultConfiguration.DEFAULT_CONFIGURATION);
        } else {
            return new Factory(configuration.getConnection(), configuration.getDialect(), configuration.getSettings(), configuration.getSchemaMapping(), configuration.getData());
        }
    }

    final static boolean isStaticFactory(Configuration configuration) {
        if (configuration == null) {
            return false;
        } else if (configuration instanceof DefaultConfiguration) {
            return true;
        } else {
            return getStaticFactory(configuration.getDialect()) == configuration;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @SuppressWarnings("deprecation")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (log.isDebugEnabled()) {
            log.debug("Deserialising", this);
        }
        Configuration registered = org.jooq.ConfigurationRegistry.provideFor(this);
        if (registered != null) {
            connection = registered.getConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Deserialised", this);
        }
    }
}
