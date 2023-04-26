package org.apache.hadoop.hive.ql.exec;

import org.apache.hadoop.hive.ql.exec.vector.VectorAppMasterEventOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorExtractOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorFileSinkOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorFilterOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorGroupByOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorLimitOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorMapJoinOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorReduceSinkOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorSMBMapJoinOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorSelectOperator;
import org.apache.hadoop.hive.ql.exec.vector.VectorizationContext;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.plan.AppMasterEventDesc;
import org.apache.hadoop.hive.ql.plan.CollectDesc;
import org.apache.hadoop.hive.ql.plan.CommonMergeJoinDesc;
import org.apache.hadoop.hive.ql.plan.DemuxDesc;
import org.apache.hadoop.hive.ql.plan.DummyStoreDesc;
import org.apache.hadoop.hive.ql.plan.DynamicPruningEventDesc;
import org.apache.hadoop.hive.ql.plan.ExprNodeDesc;
import org.apache.hadoop.hive.ql.plan.ExtractDesc;
import org.apache.hadoop.hive.ql.plan.FileSinkDesc;
import org.apache.hadoop.hive.ql.plan.FilterDesc;
import org.apache.hadoop.hive.ql.plan.ForwardDesc;
import org.apache.hadoop.hive.ql.plan.GroupByDesc;
import org.apache.hadoop.hive.ql.plan.HashTableDummyDesc;
import org.apache.hadoop.hive.ql.plan.HashTableSinkDesc;
import org.apache.hadoop.hive.ql.plan.JoinDesc;
import org.apache.hadoop.hive.ql.plan.LateralViewForwardDesc;
import org.apache.hadoop.hive.ql.plan.LateralViewJoinDesc;
import org.apache.hadoop.hive.ql.plan.LimitDesc;
import org.apache.hadoop.hive.ql.plan.ListSinkDesc;
import org.apache.hadoop.hive.ql.plan.MapJoinDesc;
import org.apache.hadoop.hive.ql.plan.MuxDesc;
import org.apache.hadoop.hive.ql.plan.OperatorDesc;
import org.apache.hadoop.hive.ql.plan.OrcFileMergeDesc;
import org.apache.hadoop.hive.ql.plan.PTFDesc;
import org.apache.hadoop.hive.ql.plan.RCFileMergeDesc;
import org.apache.hadoop.hive.ql.plan.ReduceSinkDesc;
import org.apache.hadoop.hive.ql.plan.SMBJoinDesc;
import org.apache.hadoop.hive.ql.plan.ScriptDesc;
import org.apache.hadoop.hive.ql.plan.SelectDesc;
import org.apache.hadoop.hive.ql.plan.TableScanDesc;
import org.apache.hadoop.hive.ql.plan.UDTFDesc;
import org.apache.hadoop.hive.ql.plan.UnionDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class OperatorFactory {

    private static final List<OpTuple> opvec;

    private static final List<OpTuple> vectorOpvec;

    static {
        opvec = new ArrayList<OpTuple>();
        opvec.add(new OpTuple<FilterDesc>(FilterDesc.class, FilterOperator.class));
        opvec.add(new OpTuple<SelectDesc>(SelectDesc.class, SelectOperator.class));
        opvec.add(new OpTuple<ForwardDesc>(ForwardDesc.class, ForwardOperator.class));
        opvec.add(new OpTuple<FileSinkDesc>(FileSinkDesc.class, FileSinkOperator.class));
        opvec.add(new OpTuple<CollectDesc>(CollectDesc.class, CollectOperator.class));
        opvec.add(new OpTuple<ScriptDesc>(ScriptDesc.class, ScriptOperator.class));
        opvec.add(new OpTuple<PTFDesc>(PTFDesc.class, PTFOperator.class));
        opvec.add(new OpTuple<ReduceSinkDesc>(ReduceSinkDesc.class, ReduceSinkOperator.class));
        opvec.add(new OpTuple<ExtractDesc>(ExtractDesc.class, ExtractOperator.class));
        opvec.add(new OpTuple<GroupByDesc>(GroupByDesc.class, GroupByOperator.class));
        opvec.add(new OpTuple<JoinDesc>(JoinDesc.class, JoinOperator.class));
        opvec.add(new OpTuple<MapJoinDesc>(MapJoinDesc.class, MapJoinOperator.class));
        opvec.add(new OpTuple<SMBJoinDesc>(SMBJoinDesc.class, SMBMapJoinOperator.class));
        opvec.add(new OpTuple<LimitDesc>(LimitDesc.class, LimitOperator.class));
        opvec.add(new OpTuple<TableScanDesc>(TableScanDesc.class, TableScanOperator.class));
        opvec.add(new OpTuple<UnionDesc>(UnionDesc.class, UnionOperator.class));
        opvec.add(new OpTuple<UDTFDesc>(UDTFDesc.class, UDTFOperator.class));
        opvec.add(new OpTuple<LateralViewJoinDesc>(LateralViewJoinDesc.class, LateralViewJoinOperator.class));
        opvec.add(new OpTuple<LateralViewForwardDesc>(LateralViewForwardDesc.class, LateralViewForwardOperator.class));
        opvec.add(new OpTuple<HashTableDummyDesc>(HashTableDummyDesc.class, HashTableDummyOperator.class));
        opvec.add(new OpTuple<HashTableSinkDesc>(HashTableSinkDesc.class, HashTableSinkOperator.class));
        opvec.add(new OpTuple<DummyStoreDesc>(DummyStoreDesc.class, DummyStoreOperator.class));
        opvec.add(new OpTuple<DemuxDesc>(DemuxDesc.class, DemuxOperator.class));
        opvec.add(new OpTuple<MuxDesc>(MuxDesc.class, MuxOperator.class));
        opvec.add(new OpTuple<AppMasterEventDesc>(AppMasterEventDesc.class, AppMasterEventOperator.class));
        opvec.add(new OpTuple<DynamicPruningEventDesc>(DynamicPruningEventDesc.class, AppMasterEventOperator.class));
        opvec.add(new OpTuple<RCFileMergeDesc>(RCFileMergeDesc.class, RCFileMergeOperator.class));
        opvec.add(new OpTuple<OrcFileMergeDesc>(OrcFileMergeDesc.class, OrcFileMergeOperator.class));
        opvec.add(new OpTuple<CommonMergeJoinDesc>(CommonMergeJoinDesc.class, CommonMergeJoinOperator.class));
        opvec.add(new OpTuple<ListSinkDesc>(ListSinkDesc.class, ListSinkOperator.class));
    }

    static {
        vectorOpvec = new ArrayList<OpTuple>();
        vectorOpvec.add(new OpTuple<AppMasterEventDesc>(AppMasterEventDesc.class, VectorAppMasterEventOperator.class));
        vectorOpvec.add(new OpTuple<DynamicPruningEventDesc>(DynamicPruningEventDesc.class, VectorAppMasterEventOperator.class));
        vectorOpvec.add(new OpTuple<SelectDesc>(SelectDesc.class, VectorSelectOperator.class));
        vectorOpvec.add(new OpTuple<GroupByDesc>(GroupByDesc.class, VectorGroupByOperator.class));
        vectorOpvec.add(new OpTuple<MapJoinDesc>(MapJoinDesc.class, VectorMapJoinOperator.class));
        vectorOpvec.add(new OpTuple<SMBJoinDesc>(SMBJoinDesc.class, VectorSMBMapJoinOperator.class));
        vectorOpvec.add(new OpTuple<ReduceSinkDesc>(ReduceSinkDesc.class, VectorReduceSinkOperator.class));
        vectorOpvec.add(new OpTuple<FileSinkDesc>(FileSinkDesc.class, VectorFileSinkOperator.class));
        vectorOpvec.add(new OpTuple<FilterDesc>(FilterDesc.class, VectorFilterOperator.class));
        vectorOpvec.add(new OpTuple<LimitDesc>(LimitDesc.class, VectorLimitOperator.class));
        vectorOpvec.add(new OpTuple<ExtractDesc>(ExtractDesc.class, VectorExtractOperator.class));
    }

    private static final class OpTuple<T extends OperatorDesc> {

        private final Class<T> descClass;

        private final Class<? extends Operator<?>> opClass;

        public OpTuple(Class<T> descClass, Class<? extends Operator<?>> opClass) {
            this.descClass = descClass;
            this.opClass = opClass;
        }
    }

    public static <T extends OperatorDesc> Operator<T> getVectorOperator(T conf, VectorizationContext vContext) throws HiveException {
        Class<T> descClass = (Class<T>) conf.getClass();
        for (OpTuple o : vectorOpvec) {
            if (o.descClass == descClass) {
                try {
                    Operator<T> op = (Operator<T>) o.opClass.getDeclaredConstructor(VectorizationContext.class, OperatorDesc.class).newInstance(vContext, conf);
                    return op;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new HiveException(e);
                }
            }
        }
        throw new HiveException("No vector operator for descriptor class " + descClass.getName());
    }

    public static <T extends OperatorDesc> Operator<T> get(Class<T> opClass) {
        for (OpTuple o : opvec) {
            if (o.descClass == opClass) {
                try {
                    Operator<T> op = (Operator<T>) o.opClass.newInstance();
                    return op;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("No operator for descriptor class " + opClass.getName());
    }

    public static <T extends OperatorDesc> Operator<T> get(Class<T> opClass, RowSchema rwsch) {
        Operator<T> ret = get(opClass);
        ret.setSchema(rwsch);
        return ret;
    }

    public static <T extends OperatorDesc> Operator<T> get(T conf, Operator<? extends OperatorDesc>... oplist) {
        Operator<T> ret = get((Class<T>) conf.getClass());
        ret.setConf(conf);
        makeChild(ret, oplist);
        return (ret);
    }

    public static void makeChild(Operator<? extends OperatorDesc> ret, Operator<? extends OperatorDesc>... oplist) {
        if (oplist.length == 0) {
            return;
        }
        ArrayList<Operator<? extends OperatorDesc>> clist = new ArrayList<Operator<? extends OperatorDesc>>();
        for (Operator<? extends OperatorDesc> op : oplist) {
            clist.add(op);
        }
        ret.setChildOperators(clist);
        for (Operator<? extends OperatorDesc> op : oplist) {
            List<Operator<? extends OperatorDesc>> parents = op.getParentOperators();
            if (parents == null) {
                parents = new ArrayList<Operator<? extends OperatorDesc>>();
            }
            parents.add(ret);
            op.setParentOperators(parents);
        }
    }

    public static <T extends OperatorDesc> Operator<T> get(T conf, RowSchema rwsch, Operator... oplist) {
        Operator<T> ret = get(conf, oplist);
        ret.setSchema(rwsch);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, Operator... oplist) {
        Operator<T> ret = get((Class<T>) conf.getClass());
        ret.setConf(conf);
        if (oplist.length == 0) {
            return (ret);
        }
        for (Operator op : oplist) {
            List<Operator> children = op.getChildOperators();
            if (children == null) {
                children = new ArrayList<Operator>();
            }
            children.add(ret);
            op.setChildOperators(children);
        }
        List<Operator<? extends OperatorDesc>> parent = new ArrayList<Operator<? extends OperatorDesc>>();
        for (Operator op : oplist) {
            parent.add(op);
        }
        ret.setParentOperators(parent);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, List<Operator<? extends OperatorDesc>> oplist) {
        Operator<T> ret = get((Class<T>) conf.getClass());
        ret.setConf(conf);
        if (oplist.size() == 0) {
            return (ret);
        }
        for (Operator op : oplist) {
            List<Operator> children = op.getChildOperators();
            if (children == null) {
                children = new ArrayList<Operator>();
            }
            children.add(ret);
            op.setChildOperators(children);
        }
        List<Operator<? extends OperatorDesc>> parent = new ArrayList<Operator<? extends OperatorDesc>>();
        for (Operator op : oplist) {
            parent.add(op);
        }
        ret.setParentOperators(parent);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, RowSchema rwsch, Operator... oplist) {
        Operator<T> ret = getAndMakeChild(conf, oplist);
        ret.setSchema(rwsch);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, RowSchema rwsch, Map<String, ExprNodeDesc> colExprMap, Operator... oplist) {
        Operator<T> ret = getAndMakeChild(conf, rwsch, oplist);
        ret.setColumnExprMap(colExprMap);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, RowSchema rwsch, List<Operator<? extends OperatorDesc>> oplist) {
        Operator<T> ret = getAndMakeChild(conf, oplist);
        ret.setSchema(rwsch);
        return (ret);
    }

    public static <T extends OperatorDesc> Operator<T> getAndMakeChild(T conf, RowSchema rwsch, Map<String, ExprNodeDesc> colExprMap, List<Operator<? extends OperatorDesc>> oplist) {
        Operator<T> ret = getAndMakeChild(conf, rwsch, oplist);
        ret.setColumnExprMap(colExprMap);
        return (ret);
    }

    private OperatorFactory() {
    }
}
