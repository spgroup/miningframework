package org.apache.hadoop.hive.serde2.thrift.test;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Complex implements org.apache.thrift.TBase<Complex, Complex._Fields>, java.io.Serializable, Cloneable {

    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Complex");

    private static final org.apache.thrift.protocol.TField AINT_FIELD_DESC = new org.apache.thrift.protocol.TField("aint", org.apache.thrift.protocol.TType.I32, (short) 1);

    private static final org.apache.thrift.protocol.TField A_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("aString", org.apache.thrift.protocol.TType.STRING, (short) 2);

    private static final org.apache.thrift.protocol.TField LINT_FIELD_DESC = new org.apache.thrift.protocol.TField("lint", org.apache.thrift.protocol.TType.LIST, (short) 3);

    private static final org.apache.thrift.protocol.TField L_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("lString", org.apache.thrift.protocol.TType.LIST, (short) 4);

    private static final org.apache.thrift.protocol.TField LINT_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("lintString", org.apache.thrift.protocol.TType.LIST, (short) 5);

    private static final org.apache.thrift.protocol.TField M_STRING_STRING_FIELD_DESC = new org.apache.thrift.protocol.TField("mStringString", org.apache.thrift.protocol.TType.MAP, (short) 6);

    private static final org.apache.thrift.protocol.TField ATTRIBUTES_FIELD_DESC = new org.apache.thrift.protocol.TField("attributes", org.apache.thrift.protocol.TType.MAP, (short) 7);

    private static final org.apache.thrift.protocol.TField UNION_FIELD1_FIELD_DESC = new org.apache.thrift.protocol.TField("unionField1", org.apache.thrift.protocol.TType.STRUCT, (short) 8);

    private static final org.apache.thrift.protocol.TField UNION_FIELD2_FIELD_DESC = new org.apache.thrift.protocol.TField("unionField2", org.apache.thrift.protocol.TType.STRUCT, (short) 9);

    private static final org.apache.thrift.protocol.TField UNION_FIELD3_FIELD_DESC = new org.apache.thrift.protocol.TField("unionField3", org.apache.thrift.protocol.TType.STRUCT, (short) 10);

    private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

    static {
        schemes.put(StandardScheme.class, new ComplexStandardSchemeFactory());
        schemes.put(TupleScheme.class, new ComplexTupleSchemeFactory());
    }

    private int aint;

    private String aString;

    private List<Integer> lint;

    private List<String> lString;

    private List<IntString> lintString;

    private Map<String, String> mStringString;

    private Map<String, Map<String, Map<String, PropValueUnion>>> attributes;

    private PropValueUnion unionField1;

    private PropValueUnion unionField2;

    private PropValueUnion unionField3;

    public enum _Fields implements org.apache.thrift.TFieldIdEnum {

        AINT((short) 1, "aint"),
        A_STRING((short) 2, "aString"),
        LINT((short) 3, "lint"),
        L_STRING((short) 4, "lString"),
        LINT_STRING((short) 5, "lintString"),
        M_STRING_STRING((short) 6, "mStringString"),
        ATTRIBUTES((short) 7, "attributes"),
        UNION_FIELD1((short) 8, "unionField1"),
        UNION_FIELD2((short) 9, "unionField2"),
        UNION_FIELD3((short) 10, "unionField3");

        private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

        static {
            for (_Fields field : EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        public static _Fields findByThriftId(int fieldId) {
            switch(fieldId) {
                case 1:
                    return AINT;
                case 2:
                    return A_STRING;
                case 3:
                    return LINT;
                case 4:
                    return L_STRING;
                case 5:
                    return LINT_STRING;
                case 6:
                    return M_STRING_STRING;
                case 7:
                    return ATTRIBUTES;
                case 8:
                    return UNION_FIELD1;
                case 9:
                    return UNION_FIELD2;
                case 10:
                    return UNION_FIELD3;
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

    private static final int __AINT_ISSET_ID = 0;

    private byte __isset_bitfield = 0;

    public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.AINT, new org.apache.thrift.meta_data.FieldMetaData("aint", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
        tmpMap.put(_Fields.A_STRING, new org.apache.thrift.meta_data.FieldMetaData("aString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        tmpMap.put(_Fields.LINT, new org.apache.thrift.meta_data.FieldMetaData("lint", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32))));
        tmpMap.put(_Fields.L_STRING, new org.apache.thrift.meta_data.FieldMetaData("lString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.LINT_STRING, new org.apache.thrift.meta_data.FieldMetaData("lintString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, IntString.class))));
        tmpMap.put(_Fields.M_STRING_STRING, new org.apache.thrift.meta_data.FieldMetaData("mStringString", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
        tmpMap.put(_Fields.ATTRIBUTES, new org.apache.thrift.meta_data.FieldMetaData("attributes", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PropValueUnion.class))))));
        tmpMap.put(_Fields.UNION_FIELD1, new org.apache.thrift.meta_data.FieldMetaData("unionField1", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PropValueUnion.class)));
        tmpMap.put(_Fields.UNION_FIELD2, new org.apache.thrift.meta_data.FieldMetaData("unionField2", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PropValueUnion.class)));
        tmpMap.put(_Fields.UNION_FIELD3, new org.apache.thrift.meta_data.FieldMetaData("unionField3", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, PropValueUnion.class)));
        metaDataMap = Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Complex.class, metaDataMap);
    }

    public Complex() {
    }

    public Complex(int aint, String aString, List<Integer> lint, List<String> lString, List<IntString> lintString, Map<String, String> mStringString, Map<String, Map<String, Map<String, PropValueUnion>>> attributes, PropValueUnion unionField1, PropValueUnion unionField2, PropValueUnion unionField3) {
        this();
        this.aint = aint;
        setAintIsSet(true);
        this.aString = aString;
        this.lint = lint;
        this.lString = lString;
        this.lintString = lintString;
        this.mStringString = mStringString;
        this.attributes = attributes;
        this.unionField1 = unionField1;
        this.unionField2 = unionField2;
        this.unionField3 = unionField3;
    }

    public Complex(Complex other) {
        __isset_bitfield = other.__isset_bitfield;
        this.aint = other.aint;
        if (other.isSetAString()) {
            this.aString = other.aString;
        }
        if (other.isSetLint()) {
            List<Integer> __this__lint = new ArrayList<Integer>();
            for (Integer other_element : other.lint) {
                __this__lint.add(other_element);
            }
            this.lint = __this__lint;
        }
        if (other.isSetLString()) {
            List<String> __this__lString = new ArrayList<String>();
            for (String other_element : other.lString) {
                __this__lString.add(other_element);
            }
            this.lString = __this__lString;
        }
        if (other.isSetLintString()) {
            List<IntString> __this__lintString = new ArrayList<IntString>();
            for (IntString other_element : other.lintString) {
                __this__lintString.add(new IntString(other_element));
            }
            this.lintString = __this__lintString;
        }
        if (other.isSetMStringString()) {
            Map<String, String> __this__mStringString = new HashMap<String, String>();
            for (Map.Entry<String, String> other_element : other.mStringString.entrySet()) {
                String other_element_key = other_element.getKey();
                String other_element_value = other_element.getValue();
                String __this__mStringString_copy_key = other_element_key;
                String __this__mStringString_copy_value = other_element_value;
                __this__mStringString.put(__this__mStringString_copy_key, __this__mStringString_copy_value);
            }
            this.mStringString = __this__mStringString;
        }
        if (other.isSetAttributes()) {
            Map<String, Map<String, Map<String, PropValueUnion>>> __this__attributes = new HashMap<String, Map<String, Map<String, PropValueUnion>>>();
            for (Map.Entry<String, Map<String, Map<String, PropValueUnion>>> other_element : other.attributes.entrySet()) {
                String other_element_key = other_element.getKey();
                Map<String, Map<String, PropValueUnion>> other_element_value = other_element.getValue();
                String __this__attributes_copy_key = other_element_key;
                Map<String, Map<String, PropValueUnion>> __this__attributes_copy_value = new HashMap<String, Map<String, PropValueUnion>>();
                for (Map.Entry<String, Map<String, PropValueUnion>> other_element_value_element : other_element_value.entrySet()) {
                    String other_element_value_element_key = other_element_value_element.getKey();
                    Map<String, PropValueUnion> other_element_value_element_value = other_element_value_element.getValue();
                    String __this__attributes_copy_value_copy_key = other_element_value_element_key;
                    Map<String, PropValueUnion> __this__attributes_copy_value_copy_value = new HashMap<String, PropValueUnion>();
                    for (Map.Entry<String, PropValueUnion> other_element_value_element_value_element : other_element_value_element_value.entrySet()) {
                        String other_element_value_element_value_element_key = other_element_value_element_value_element.getKey();
                        PropValueUnion other_element_value_element_value_element_value = other_element_value_element_value_element.getValue();
                        String __this__attributes_copy_value_copy_value_copy_key = other_element_value_element_value_element_key;
                        PropValueUnion __this__attributes_copy_value_copy_value_copy_value = new PropValueUnion(other_element_value_element_value_element_value);
                        __this__attributes_copy_value_copy_value.put(__this__attributes_copy_value_copy_value_copy_key, __this__attributes_copy_value_copy_value_copy_value);
                    }
                    __this__attributes_copy_value.put(__this__attributes_copy_value_copy_key, __this__attributes_copy_value_copy_value);
                }
                __this__attributes.put(__this__attributes_copy_key, __this__attributes_copy_value);
            }
            this.attributes = __this__attributes;
        }
        if (other.isSetUnionField1()) {
            this.unionField1 = new PropValueUnion(other.unionField1);
        }
        if (other.isSetUnionField2()) {
            this.unionField2 = new PropValueUnion(other.unionField2);
        }
        if (other.isSetUnionField3()) {
            this.unionField3 = new PropValueUnion(other.unionField3);
        }
    }

    public Complex deepCopy() {
        return new Complex(this);
    }

    @Override
    public void clear() {
        setAintIsSet(false);
        this.aint = 0;
        this.aString = null;
        this.lint = null;
        this.lString = null;
        this.lintString = null;
        this.mStringString = null;
        this.attributes = null;
        this.unionField1 = null;
        this.unionField2 = null;
        this.unionField3 = null;
    }

    public int getAint() {
        return this.aint;
    }

    public void setAint(int aint) {
        this.aint = aint;
        setAintIsSet(true);
    }

    public void unsetAint() {
        __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __AINT_ISSET_ID);
    }

    public boolean isSetAint() {
        return EncodingUtils.testBit(__isset_bitfield, __AINT_ISSET_ID);
    }

    public void setAintIsSet(boolean value) {
        __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __AINT_ISSET_ID, value);
    }

    public String getAString() {
        return this.aString;
    }

    public void setAString(String aString) {
        this.aString = aString;
    }

    public void unsetAString() {
        this.aString = null;
    }

    public boolean isSetAString() {
        return this.aString != null;
    }

    public void setAStringIsSet(boolean value) {
        if (!value) {
            this.aString = null;
        }
    }

    public int getLintSize() {
        return (this.lint == null) ? 0 : this.lint.size();
    }

    public java.util.Iterator<Integer> getLintIterator() {
        return (this.lint == null) ? null : this.lint.iterator();
    }

    public void addToLint(int elem) {
        if (this.lint == null) {
            this.lint = new ArrayList<Integer>();
        }
        this.lint.add(elem);
    }

    public List<Integer> getLint() {
        return this.lint;
    }

    public void setLint(List<Integer> lint) {
        this.lint = lint;
    }

    public void unsetLint() {
        this.lint = null;
    }

    public boolean isSetLint() {
        return this.lint != null;
    }

    public void setLintIsSet(boolean value) {
        if (!value) {
            this.lint = null;
        }
    }

    public int getLStringSize() {
        return (this.lString == null) ? 0 : this.lString.size();
    }

    public java.util.Iterator<String> getLStringIterator() {
        return (this.lString == null) ? null : this.lString.iterator();
    }

    public void addToLString(String elem) {
        if (this.lString == null) {
            this.lString = new ArrayList<String>();
        }
        this.lString.add(elem);
    }

    public List<String> getLString() {
        return this.lString;
    }

    public void setLString(List<String> lString) {
        this.lString = lString;
    }

    public void unsetLString() {
        this.lString = null;
    }

    public boolean isSetLString() {
        return this.lString != null;
    }

    public void setLStringIsSet(boolean value) {
        if (!value) {
            this.lString = null;
        }
    }

    public int getLintStringSize() {
        return (this.lintString == null) ? 0 : this.lintString.size();
    }

    public java.util.Iterator<IntString> getLintStringIterator() {
        return (this.lintString == null) ? null : this.lintString.iterator();
    }

    public void addToLintString(IntString elem) {
        if (this.lintString == null) {
            this.lintString = new ArrayList<IntString>();
        }
        this.lintString.add(elem);
    }

    public List<IntString> getLintString() {
        return this.lintString;
    }

    public void setLintString(List<IntString> lintString) {
        this.lintString = lintString;
    }

    public void unsetLintString() {
        this.lintString = null;
    }

    public boolean isSetLintString() {
        return this.lintString != null;
    }

    public void setLintStringIsSet(boolean value) {
        if (!value) {
            this.lintString = null;
        }
    }

    public int getMStringStringSize() {
        return (this.mStringString == null) ? 0 : this.mStringString.size();
    }

    public void putToMStringString(String key, String val) {
        if (this.mStringString == null) {
            this.mStringString = new HashMap<String, String>();
        }
        this.mStringString.put(key, val);
    }

    public Map<String, String> getMStringString() {
        return this.mStringString;
    }

    public void setMStringString(Map<String, String> mStringString) {
        this.mStringString = mStringString;
    }

    public void unsetMStringString() {
        this.mStringString = null;
    }

    public boolean isSetMStringString() {
        return this.mStringString != null;
    }

    public void setMStringStringIsSet(boolean value) {
        if (!value) {
            this.mStringString = null;
        }
    }

    public int getAttributesSize() {
        return (this.attributes == null) ? 0 : this.attributes.size();
    }

    public void putToAttributes(String key, Map<String, Map<String, PropValueUnion>> val) {
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Map<String, Map<String, PropValueUnion>>>();
        }
        this.attributes.put(key, val);
    }

    public Map<String, Map<String, Map<String, PropValueUnion>>> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, Map<String, Map<String, PropValueUnion>>> attributes) {
        this.attributes = attributes;
    }

    public void unsetAttributes() {
        this.attributes = null;
    }

    public boolean isSetAttributes() {
        return this.attributes != null;
    }

    public void setAttributesIsSet(boolean value) {
        if (!value) {
            this.attributes = null;
        }
    }

    public PropValueUnion getUnionField1() {
        return this.unionField1;
    }

    public void setUnionField1(PropValueUnion unionField1) {
        this.unionField1 = unionField1;
    }

    public void unsetUnionField1() {
        this.unionField1 = null;
    }

    public boolean isSetUnionField1() {
        return this.unionField1 != null;
    }

    public void setUnionField1IsSet(boolean value) {
        if (!value) {
            this.unionField1 = null;
        }
    }

    public PropValueUnion getUnionField2() {
        return this.unionField2;
    }

    public void setUnionField2(PropValueUnion unionField2) {
        this.unionField2 = unionField2;
    }

    public void unsetUnionField2() {
        this.unionField2 = null;
    }

    public boolean isSetUnionField2() {
        return this.unionField2 != null;
    }

    public void setUnionField2IsSet(boolean value) {
        if (!value) {
            this.unionField2 = null;
        }
    }

    public PropValueUnion getUnionField3() {
        return this.unionField3;
    }

    public void setUnionField3(PropValueUnion unionField3) {
        this.unionField3 = unionField3;
    }

    public void unsetUnionField3() {
        this.unionField3 = null;
    }

    public boolean isSetUnionField3() {
        return this.unionField3 != null;
    }

    public void setUnionField3IsSet(boolean value) {
        if (!value) {
            this.unionField3 = null;
        }
    }

    public void setFieldValue(_Fields field, Object value) {
        switch(field) {
            case AINT:
                if (value == null) {
                    unsetAint();
                } else {
                    setAint((Integer) value);
                }
                break;
            case A_STRING:
                if (value == null) {
                    unsetAString();
                } else {
                    setAString((String) value);
                }
                break;
            case LINT:
                if (value == null) {
                    unsetLint();
                } else {
                    setLint((List<Integer>) value);
                }
                break;
            case L_STRING:
                if (value == null) {
                    unsetLString();
                } else {
                    setLString((List<String>) value);
                }
                break;
            case LINT_STRING:
                if (value == null) {
                    unsetLintString();
                } else {
                    setLintString((List<IntString>) value);
                }
                break;
            case M_STRING_STRING:
                if (value == null) {
                    unsetMStringString();
                } else {
                    setMStringString((Map<String, String>) value);
                }
                break;
            case ATTRIBUTES:
                if (value == null) {
                    unsetAttributes();
                } else {
                    setAttributes((Map<String, Map<String, Map<String, PropValueUnion>>>) value);
                }
                break;
            case UNION_FIELD1:
                if (value == null) {
                    unsetUnionField1();
                } else {
                    setUnionField1((PropValueUnion) value);
                }
                break;
            case UNION_FIELD2:
                if (value == null) {
                    unsetUnionField2();
                } else {
                    setUnionField2((PropValueUnion) value);
                }
                break;
            case UNION_FIELD3:
                if (value == null) {
                    unsetUnionField3();
                } else {
                    setUnionField3((PropValueUnion) value);
                }
                break;
        }
    }

    public Object getFieldValue(_Fields field) {
        switch(field) {
            case AINT:
                return Integer.valueOf(getAint());
            case A_STRING:
                return getAString();
            case LINT:
                return getLint();
            case L_STRING:
                return getLString();
            case LINT_STRING:
                return getLintString();
            case M_STRING_STRING:
                return getMStringString();
            case ATTRIBUTES:
                return getAttributes();
            case UNION_FIELD1:
                return getUnionField1();
            case UNION_FIELD2:
                return getUnionField2();
            case UNION_FIELD3:
                return getUnionField3();
        }
        throw new IllegalStateException();
    }

    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        switch(field) {
            case AINT:
                return isSetAint();
            case A_STRING:
                return isSetAString();
            case LINT:
                return isSetLint();
            case L_STRING:
                return isSetLString();
            case LINT_STRING:
                return isSetLintString();
            case M_STRING_STRING:
                return isSetMStringString();
            case ATTRIBUTES:
                return isSetAttributes();
            case UNION_FIELD1:
                return isSetUnionField1();
            case UNION_FIELD2:
                return isSetUnionField2();
            case UNION_FIELD3:
                return isSetUnionField3();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
            return false;
        if (that instanceof Complex)
            return this.equals((Complex) that);
        return false;
    }

    public boolean equals(Complex that) {
        if (that == null)
            return false;
        boolean this_present_aint = true;
        boolean that_present_aint = true;
        if (this_present_aint || that_present_aint) {
            if (!(this_present_aint && that_present_aint))
                return false;
            if (this.aint != that.aint)
                return false;
        }
        boolean this_present_aString = true && this.isSetAString();
        boolean that_present_aString = true && that.isSetAString();
        if (this_present_aString || that_present_aString) {
            if (!(this_present_aString && that_present_aString))
                return false;
            if (!this.aString.equals(that.aString))
                return false;
        }
        boolean this_present_lint = true && this.isSetLint();
        boolean that_present_lint = true && that.isSetLint();
        if (this_present_lint || that_present_lint) {
            if (!(this_present_lint && that_present_lint))
                return false;
            if (!this.lint.equals(that.lint))
                return false;
        }
        boolean this_present_lString = true && this.isSetLString();
        boolean that_present_lString = true && that.isSetLString();
        if (this_present_lString || that_present_lString) {
            if (!(this_present_lString && that_present_lString))
                return false;
            if (!this.lString.equals(that.lString))
                return false;
        }
        boolean this_present_lintString = true && this.isSetLintString();
        boolean that_present_lintString = true && that.isSetLintString();
        if (this_present_lintString || that_present_lintString) {
            if (!(this_present_lintString && that_present_lintString))
                return false;
            if (!this.lintString.equals(that.lintString))
                return false;
        }
        boolean this_present_mStringString = true && this.isSetMStringString();
        boolean that_present_mStringString = true && that.isSetMStringString();
        if (this_present_mStringString || that_present_mStringString) {
            if (!(this_present_mStringString && that_present_mStringString))
                return false;
            if (!this.mStringString.equals(that.mStringString))
                return false;
        }
        boolean this_present_attributes = true && this.isSetAttributes();
        boolean that_present_attributes = true && that.isSetAttributes();
        if (this_present_attributes || that_present_attributes) {
            if (!(this_present_attributes && that_present_attributes))
                return false;
            if (!this.attributes.equals(that.attributes))
                return false;
        }
        boolean this_present_unionField1 = true && this.isSetUnionField1();
        boolean that_present_unionField1 = true && that.isSetUnionField1();
        if (this_present_unionField1 || that_present_unionField1) {
            if (!(this_present_unionField1 && that_present_unionField1))
                return false;
            if (!this.unionField1.equals(that.unionField1))
                return false;
        }
        boolean this_present_unionField2 = true && this.isSetUnionField2();
        boolean that_present_unionField2 = true && that.isSetUnionField2();
        if (this_present_unionField2 || that_present_unionField2) {
            if (!(this_present_unionField2 && that_present_unionField2))
                return false;
            if (!this.unionField2.equals(that.unionField2))
                return false;
        }
        boolean this_present_unionField3 = true && this.isSetUnionField3();
        boolean that_present_unionField3 = true && that.isSetUnionField3();
        if (this_present_unionField3 || that_present_unionField3) {
            if (!(this_present_unionField3 && that_present_unionField3))
                return false;
            if (!this.unionField3.equals(that.unionField3))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        boolean present_aint = true;
        builder.append(present_aint);
        if (present_aint)
            builder.append(aint);
        boolean present_aString = true && (isSetAString());
        builder.append(present_aString);
        if (present_aString)
            builder.append(aString);
        boolean present_lint = true && (isSetLint());
        builder.append(present_lint);
        if (present_lint)
            builder.append(lint);
        boolean present_lString = true && (isSetLString());
        builder.append(present_lString);
        if (present_lString)
            builder.append(lString);
        boolean present_lintString = true && (isSetLintString());
        builder.append(present_lintString);
        if (present_lintString)
            builder.append(lintString);
        boolean present_mStringString = true && (isSetMStringString());
        builder.append(present_mStringString);
        if (present_mStringString)
            builder.append(mStringString);
        boolean present_attributes = true && (isSetAttributes());
        builder.append(present_attributes);
        if (present_attributes)
            builder.append(attributes);
        boolean present_unionField1 = true && (isSetUnionField1());
        builder.append(present_unionField1);
        if (present_unionField1)
            builder.append(unionField1);
        boolean present_unionField2 = true && (isSetUnionField2());
        builder.append(present_unionField2);
        if (present_unionField2)
            builder.append(unionField2);
        boolean present_unionField3 = true && (isSetUnionField3());
        builder.append(present_unionField3);
        if (present_unionField3)
            builder.append(unionField3);
        return builder.toHashCode();
    }

    public int compareTo(Complex other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }
        int lastComparison = 0;
        Complex typedOther = (Complex) other;
        lastComparison = Boolean.valueOf(isSetAint()).compareTo(typedOther.isSetAint());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAint()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.aint, typedOther.aint);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAString()).compareTo(typedOther.isSetAString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.aString, typedOther.aString);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLint()).compareTo(typedOther.isSetLint());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLint()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lint, typedOther.lint);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLString()).compareTo(typedOther.isSetLString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lString, typedOther.lString);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetLintString()).compareTo(typedOther.isSetLintString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetLintString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.lintString, typedOther.lintString);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetMStringString()).compareTo(typedOther.isSetMStringString());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetMStringString()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mStringString, typedOther.mStringString);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetAttributes()).compareTo(typedOther.isSetAttributes());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetAttributes()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.attributes, typedOther.attributes);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUnionField1()).compareTo(typedOther.isSetUnionField1());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnionField1()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unionField1, typedOther.unionField1);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUnionField2()).compareTo(typedOther.isSetUnionField2());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnionField2()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unionField2, typedOther.unionField2);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = Boolean.valueOf(isSetUnionField3()).compareTo(typedOther.isSetUnionField3());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUnionField3()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.unionField3, typedOther.unionField3);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Complex(");
        boolean first = true;
        sb.append("aint:");
        sb.append(this.aint);
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("aString:");
        if (this.aString == null) {
            sb.append("null");
        } else {
            sb.append(this.aString);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lint:");
        if (this.lint == null) {
            sb.append("null");
        } else {
            sb.append(this.lint);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lString:");
        if (this.lString == null) {
            sb.append("null");
        } else {
            sb.append(this.lString);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("lintString:");
        if (this.lintString == null) {
            sb.append("null");
        } else {
            sb.append(this.lintString);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("mStringString:");
        if (this.mStringString == null) {
            sb.append("null");
        } else {
            sb.append(this.mStringString);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("attributes:");
        if (this.attributes == null) {
            sb.append("null");
        } else {
            sb.append(this.attributes);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("unionField1:");
        if (this.unionField1 == null) {
            sb.append("null");
        } else {
            sb.append(this.unionField1);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("unionField2:");
        if (this.unionField2 == null) {
            sb.append("null");
        } else {
            sb.append(this.unionField2);
        }
        first = false;
        if (!first)
            sb.append(", ");
        sb.append("unionField3:");
        if (this.unionField3 == null) {
            sb.append("null");
        } else {
            sb.append(this.unionField3);
        }
        first = false;
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        try {
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class ComplexStandardSchemeFactory implements SchemeFactory {

        public ComplexStandardScheme getScheme() {
            return new ComplexStandardScheme();
        }
    }

    private static class ComplexStandardScheme extends StandardScheme<Complex> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, Complex struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch(schemeField.id) {
                    case 1:
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.aint = iprot.readI32();
                            struct.setAintIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.aString = iprot.readString();
                            struct.setAStringIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list18 = iprot.readListBegin();
                                struct.lint = new ArrayList<Integer>(_list18.size);
                                for (int _i19 = 0; _i19 < _list18.size; ++_i19) {
                                    int _elem20;
                                    _elem20 = iprot.readI32();
                                    struct.lint.add(_elem20);
                                }
                                iprot.readListEnd();
                            }
                            struct.setLintIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 4:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list21 = iprot.readListBegin();
                                struct.lString = new ArrayList<String>(_list21.size);
                                for (int _i22 = 0; _i22 < _list21.size; ++_i22) {
                                    String _elem23;
                                    _elem23 = iprot.readString();
                                    struct.lString.add(_elem23);
                                }
                                iprot.readListEnd();
                            }
                            struct.setLStringIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 5:
                        if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
                            {
                                org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                                struct.lintString = new ArrayList<IntString>(_list24.size);
                                for (int _i25 = 0; _i25 < _list24.size; ++_i25) {
                                    IntString _elem26;
                                    _elem26 = new IntString();
                                    _elem26.read(iprot);
                                    struct.lintString.add(_elem26);
                                }
                                iprot.readListEnd();
                            }
                            struct.setLintStringIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 6:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map27 = iprot.readMapBegin();
                                struct.mStringString = new HashMap<String, String>(2 * _map27.size);
                                for (int _i28 = 0; _i28 < _map27.size; ++_i28) {
                                    String _key29;
                                    String _val30;
                                    _key29 = iprot.readString();
                                    _val30 = iprot.readString();
                                    struct.mStringString.put(_key29, _val30);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setMStringStringIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 7:
                        if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
                            {
                                org.apache.thrift.protocol.TMap _map31 = iprot.readMapBegin();
                                struct.attributes = new HashMap<String, Map<String, Map<String, PropValueUnion>>>(2 * _map31.size);
                                for (int _i32 = 0; _i32 < _map31.size; ++_i32) {
                                    String _key33;
                                    Map<String, Map<String, PropValueUnion>> _val34;
                                    _key33 = iprot.readString();
                                    {
                                        org.apache.thrift.protocol.TMap _map35 = iprot.readMapBegin();
                                        _val34 = new HashMap<String, Map<String, PropValueUnion>>(2 * _map35.size);
                                        for (int _i36 = 0; _i36 < _map35.size; ++_i36) {
                                            String _key37;
                                            Map<String, PropValueUnion> _val38;
                                            _key37 = iprot.readString();
                                            {
                                                org.apache.thrift.protocol.TMap _map39 = iprot.readMapBegin();
                                                _val38 = new HashMap<String, PropValueUnion>(2 * _map39.size);
                                                for (int _i40 = 0; _i40 < _map39.size; ++_i40) {
                                                    String _key41;
                                                    PropValueUnion _val42;
                                                    _key41 = iprot.readString();
                                                    _val42 = new PropValueUnion();
                                                    _val42.read(iprot);
                                                    _val38.put(_key41, _val42);
                                                }
                                                iprot.readMapEnd();
                                            }
                                            _val34.put(_key37, _val38);
                                        }
                                        iprot.readMapEnd();
                                    }
                                    struct.attributes.put(_key33, _val34);
                                }
                                iprot.readMapEnd();
                            }
                            struct.setAttributesIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 8:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.unionField1 = new PropValueUnion();
                            struct.unionField1.read(iprot);
                            struct.setUnionField1IsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 9:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.unionField2 = new PropValueUnion();
                            struct.unionField2.read(iprot);
                            struct.setUnionField2IsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 10:
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                            struct.unionField3 = new PropValueUnion();
                            struct.unionField3.read(iprot);
                            struct.setUnionField3IsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, Complex struct) throws org.apache.thrift.TException {
            struct.validate();
            oprot.writeStructBegin(STRUCT_DESC);
            oprot.writeFieldBegin(AINT_FIELD_DESC);
            oprot.writeI32(struct.aint);
            oprot.writeFieldEnd();
            if (struct.aString != null) {
                oprot.writeFieldBegin(A_STRING_FIELD_DESC);
                oprot.writeString(struct.aString);
                oprot.writeFieldEnd();
            }
            if (struct.lint != null) {
                oprot.writeFieldBegin(LINT_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.lint.size()));
                    for (int _iter43 : struct.lint) {
                        oprot.writeI32(_iter43);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.lString != null) {
                oprot.writeFieldBegin(L_STRING_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.lString.size()));
                    for (String _iter44 : struct.lString) {
                        oprot.writeString(_iter44);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.lintString != null) {
                oprot.writeFieldBegin(LINT_STRING_FIELD_DESC);
                {
                    oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.lintString.size()));
                    for (IntString _iter45 : struct.lintString) {
                        _iter45.write(oprot);
                    }
                    oprot.writeListEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.mStringString != null) {
                oprot.writeFieldBegin(M_STRING_STRING_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.mStringString.size()));
                    for (Map.Entry<String, String> _iter46 : struct.mStringString.entrySet()) {
                        oprot.writeString(_iter46.getKey());
                        oprot.writeString(_iter46.getValue());
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.attributes != null) {
                oprot.writeFieldBegin(ATTRIBUTES_FIELD_DESC);
                {
                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, struct.attributes.size()));
                    for (Map.Entry<String, Map<String, Map<String, PropValueUnion>>> _iter47 : struct.attributes.entrySet()) {
                        oprot.writeString(_iter47.getKey());
                        {
                            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, _iter47.getValue().size()));
                            for (Map.Entry<String, Map<String, PropValueUnion>> _iter48 : _iter47.getValue().entrySet()) {
                                oprot.writeString(_iter48.getKey());
                                {
                                    oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, _iter48.getValue().size()));
                                    for (Map.Entry<String, PropValueUnion> _iter49 : _iter48.getValue().entrySet()) {
                                        oprot.writeString(_iter49.getKey());
                                        _iter49.getValue().write(oprot);
                                    }
                                    oprot.writeMapEnd();
                                }
                            }
                            oprot.writeMapEnd();
                        }
                    }
                    oprot.writeMapEnd();
                }
                oprot.writeFieldEnd();
            }
            if (struct.unionField1 != null) {
                oprot.writeFieldBegin(UNION_FIELD1_FIELD_DESC);
                struct.unionField1.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.unionField2 != null) {
                oprot.writeFieldBegin(UNION_FIELD2_FIELD_DESC);
                struct.unionField2.write(oprot);
                oprot.writeFieldEnd();
            }
            if (struct.unionField3 != null) {
                oprot.writeFieldBegin(UNION_FIELD3_FIELD_DESC);
                struct.unionField3.write(oprot);
                oprot.writeFieldEnd();
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }
    }

    private static class ComplexTupleSchemeFactory implements SchemeFactory {

        public ComplexTupleScheme getScheme() {
            return new ComplexTupleScheme();
        }
    }

    private static class ComplexTupleScheme extends TupleScheme<Complex> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, Complex struct) throws org.apache.thrift.TException {
            TTupleProtocol oprot = (TTupleProtocol) prot;
            BitSet optionals = new BitSet();
            if (struct.isSetAint()) {
                optionals.set(0);
            }
            if (struct.isSetAString()) {
                optionals.set(1);
            }
            if (struct.isSetLint()) {
                optionals.set(2);
            }
            if (struct.isSetLString()) {
                optionals.set(3);
            }
            if (struct.isSetLintString()) {
                optionals.set(4);
            }
            if (struct.isSetMStringString()) {
                optionals.set(5);
            }
            if (struct.isSetAttributes()) {
                optionals.set(6);
            }
            if (struct.isSetUnionField1()) {
                optionals.set(7);
            }
            if (struct.isSetUnionField2()) {
                optionals.set(8);
            }
            if (struct.isSetUnionField3()) {
                optionals.set(9);
            }
            oprot.writeBitSet(optionals, 10);
            if (struct.isSetAint()) {
                oprot.writeI32(struct.aint);
            }
            if (struct.isSetAString()) {
                oprot.writeString(struct.aString);
            }
            if (struct.isSetLint()) {
                {
                    oprot.writeI32(struct.lint.size());
                    for (int _iter50 : struct.lint) {
                        oprot.writeI32(_iter50);
                    }
                }
            }
            if (struct.isSetLString()) {
                {
                    oprot.writeI32(struct.lString.size());
                    for (String _iter51 : struct.lString) {
                        oprot.writeString(_iter51);
                    }
                }
            }
            if (struct.isSetLintString()) {
                {
                    oprot.writeI32(struct.lintString.size());
                    for (IntString _iter52 : struct.lintString) {
                        _iter52.write(oprot);
                    }
                }
            }
            if (struct.isSetMStringString()) {
                {
                    oprot.writeI32(struct.mStringString.size());
                    for (Map.Entry<String, String> _iter53 : struct.mStringString.entrySet()) {
                        oprot.writeString(_iter53.getKey());
                        oprot.writeString(_iter53.getValue());
                    }
                }
            }
            if (struct.isSetAttributes()) {
                {
                    oprot.writeI32(struct.attributes.size());
                    for (Map.Entry<String, Map<String, Map<String, PropValueUnion>>> _iter54 : struct.attributes.entrySet()) {
                        oprot.writeString(_iter54.getKey());
                        {
                            oprot.writeI32(_iter54.getValue().size());
                            for (Map.Entry<String, Map<String, PropValueUnion>> _iter55 : _iter54.getValue().entrySet()) {
                                oprot.writeString(_iter55.getKey());
                                {
                                    oprot.writeI32(_iter55.getValue().size());
                                    for (Map.Entry<String, PropValueUnion> _iter56 : _iter55.getValue().entrySet()) {
                                        oprot.writeString(_iter56.getKey());
                                        _iter56.getValue().write(oprot);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (struct.isSetUnionField1()) {
                struct.unionField1.write(oprot);
            }
            if (struct.isSetUnionField2()) {
                struct.unionField2.write(oprot);
            }
            if (struct.isSetUnionField3()) {
                struct.unionField3.write(oprot);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, Complex struct) throws org.apache.thrift.TException {
            TTupleProtocol iprot = (TTupleProtocol) prot;
            BitSet incoming = iprot.readBitSet(10);
            if (incoming.get(0)) {
                struct.aint = iprot.readI32();
                struct.setAintIsSet(true);
            }
            if (incoming.get(1)) {
                struct.aString = iprot.readString();
                struct.setAStringIsSet(true);
            }
            if (incoming.get(2)) {
                {
                    org.apache.thrift.protocol.TList _list57 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
                    struct.lint = new ArrayList<Integer>(_list57.size);
                    for (int _i58 = 0; _i58 < _list57.size; ++_i58) {
                        int _elem59;
                        _elem59 = iprot.readI32();
                        struct.lint.add(_elem59);
                    }
                }
                struct.setLintIsSet(true);
            }
            if (incoming.get(3)) {
                {
                    org.apache.thrift.protocol.TList _list60 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.lString = new ArrayList<String>(_list60.size);
                    for (int _i61 = 0; _i61 < _list60.size; ++_i61) {
                        String _elem62;
                        _elem62 = iprot.readString();
                        struct.lString.add(_elem62);
                    }
                }
                struct.setLStringIsSet(true);
            }
            if (incoming.get(4)) {
                {
                    org.apache.thrift.protocol.TList _list63 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                    struct.lintString = new ArrayList<IntString>(_list63.size);
                    for (int _i64 = 0; _i64 < _list63.size; ++_i64) {
                        IntString _elem65;
                        _elem65 = new IntString();
                        _elem65.read(iprot);
                        struct.lintString.add(_elem65);
                    }
                }
                struct.setLintStringIsSet(true);
            }
            if (incoming.get(5)) {
                {
                    org.apache.thrift.protocol.TMap _map66 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
                    struct.mStringString = new HashMap<String, String>(2 * _map66.size);
                    for (int _i67 = 0; _i67 < _map66.size; ++_i67) {
                        String _key68;
                        String _val69;
                        _key68 = iprot.readString();
                        _val69 = iprot.readString();
                        struct.mStringString.put(_key68, _val69);
                    }
                }
                struct.setMStringStringIsSet(true);
            }
            if (incoming.get(6)) {
                {
                    org.apache.thrift.protocol.TMap _map70 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
                    struct.attributes = new HashMap<String, Map<String, Map<String, PropValueUnion>>>(2 * _map70.size);
                    for (int _i71 = 0; _i71 < _map70.size; ++_i71) {
                        String _key72;
                        Map<String, Map<String, PropValueUnion>> _val73;
                        _key72 = iprot.readString();
                        {
                            org.apache.thrift.protocol.TMap _map74 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.MAP, iprot.readI32());
                            _val73 = new HashMap<String, Map<String, PropValueUnion>>(2 * _map74.size);
                            for (int _i75 = 0; _i75 < _map74.size; ++_i75) {
                                String _key76;
                                Map<String, PropValueUnion> _val77;
                                _key76 = iprot.readString();
                                {
                                    org.apache.thrift.protocol.TMap _map78 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
                                    _val77 = new HashMap<String, PropValueUnion>(2 * _map78.size);
                                    for (int _i79 = 0; _i79 < _map78.size; ++_i79) {
                                        String _key80;
                                        PropValueUnion _val81;
                                        _key80 = iprot.readString();
                                        _val81 = new PropValueUnion();
                                        _val81.read(iprot);
                                        _val77.put(_key80, _val81);
                                    }
                                }
                                _val73.put(_key76, _val77);
                            }
                        }
                        struct.attributes.put(_key72, _val73);
                    }
                }
                struct.setAttributesIsSet(true);
            }
            if (incoming.get(7)) {
                struct.unionField1 = new PropValueUnion();
                struct.unionField1.read(iprot);
                struct.setUnionField1IsSet(true);
            }
            if (incoming.get(8)) {
                struct.unionField2 = new PropValueUnion();
                struct.unionField2.read(iprot);
                struct.setUnionField2IsSet(true);
            }
            if (incoming.get(9)) {
                struct.unionField3 = new PropValueUnion();
                struct.unionField3.read(iprot);
                struct.setUnionField3IsSet(true);
            }
        }
    }
}
