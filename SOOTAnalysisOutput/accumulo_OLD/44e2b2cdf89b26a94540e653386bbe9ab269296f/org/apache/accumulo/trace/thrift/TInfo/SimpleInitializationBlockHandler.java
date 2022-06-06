package org.apache.accumulo.trace.thrift;

@SuppressWarnings("serial")
@Deprecated
public class TInfo extends org.apache.accumulo.core.trace.thrift.TInfo {

    static {
        schemes.put(StandardScheme.class, new TInfoStandardSchemeFactory());
        schemes.put(TupleScheme.class, new TInfoTupleSchemeFactory());
    }

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.TRACE_ID, new org.apache.thrift.meta_data.FieldMetaData("traceId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.PARENT_ID, new org.apache.thrift.meta_data.FieldMetaData("parentId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TInfo.class, metaDataMap);
    }
}