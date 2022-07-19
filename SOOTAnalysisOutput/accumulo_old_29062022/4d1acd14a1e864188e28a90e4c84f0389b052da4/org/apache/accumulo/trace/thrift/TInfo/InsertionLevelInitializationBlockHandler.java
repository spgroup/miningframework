package org.apache.accumulo.trace.thrift;

<<<<<<< MINE
@SuppressWarnings("serial")
@Deprecated
=======
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
>>>>>>> YOURS
public class TInfo extends org.apache.accumulo.core.trace.thrift.TInfo {<<<<<<< MINE

=======


  public enum _Fields implements org.apache.thrift.TFieldIdEnum {

    TRACE_ID((short) 1, "traceId"), PARENT_ID((short) 2, "parentId");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1:
          return TRACE_ID;
        case 2:
          return PARENT_ID;
        default:
          return null;
      }
    }

    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null)
        throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;

    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }
>>>>>>> YOURS
}