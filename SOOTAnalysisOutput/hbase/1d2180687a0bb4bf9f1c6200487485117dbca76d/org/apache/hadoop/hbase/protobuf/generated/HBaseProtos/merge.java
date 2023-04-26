package org.apache.hadoop.hbase.protobuf.generated;

public final class HBaseProtos {

    private HBaseProtos() {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    }

    public enum CompareType implements com.google.protobuf.ProtocolMessageEnum {

        LESS(0, 0),
        LESS_OR_EQUAL(1, 1),
        EQUAL(2, 2),
        NOT_EQUAL(3, 3),
        GREATER_OR_EQUAL(4, 4),
        GREATER(5, 5),
        NO_OP(6, 6);

        public static final int LESS_VALUE = 0;

        public static final int LESS_OR_EQUAL_VALUE = 1;

        public static final int EQUAL_VALUE = 2;

        public static final int NOT_EQUAL_VALUE = 3;

        public static final int GREATER_OR_EQUAL_VALUE = 4;

        public static final int GREATER_VALUE = 5;

        public static final int NO_OP_VALUE = 6;

        public final int getNumber() {
            return value;
        }

        public static CompareType valueOf(int value) {
            switch(value) {
                case 0:
                    return LESS;
                case 1:
                    return LESS_OR_EQUAL;
                case 2:
                    return EQUAL;
                case 3:
                    return NOT_EQUAL;
                case 4:
                    return GREATER_OR_EQUAL;
                case 5:
                    return GREATER;
                case 6:
                    return NO_OP;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<CompareType> internalGetValueMap() {
            return internalValueMap;
        }

        private static com.google.protobuf.Internal.EnumLiteMap<CompareType> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<CompareType>() {

            public CompareType findValueByNumber(int number) {
                return CompareType.valueOf(number);
            }
        };

        public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.getDescriptor().getEnumTypes().get(0);
        }

        private static final CompareType[] VALUES = { LESS, LESS_OR_EQUAL, EQUAL, NOT_EQUAL, GREATER_OR_EQUAL, GREATER, NO_OP };

        public static CompareType valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private final int index;

        private final int value;

        private CompareType(int index, int value) {
            this.index = index;
            this.value = value;
        }
    }

    public enum KeyType implements com.google.protobuf.ProtocolMessageEnum {

        MINIMUM(0, 0),
        PUT(1, 4),
        DELETE(2, 8),
        DELETE_COLUMN(3, 12),
        DELETE_FAMILY(4, 14),
        MAXIMUM(5, 255);

        public static final int MINIMUM_VALUE = 0;

        public static final int PUT_VALUE = 4;

        public static final int DELETE_VALUE = 8;

        public static final int DELETE_COLUMN_VALUE = 12;

        public static final int DELETE_FAMILY_VALUE = 14;

        public static final int MAXIMUM_VALUE = 255;

        public final int getNumber() {
            return value;
        }

        public static KeyType valueOf(int value) {
            switch(value) {
                case 0:
                    return MINIMUM;
                case 4:
                    return PUT;
                case 8:
                    return DELETE;
                case 12:
                    return DELETE_COLUMN;
                case 14:
                    return DELETE_FAMILY;
                case 255:
                    return MAXIMUM;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<KeyType> internalGetValueMap() {
            return internalValueMap;
        }

        private static com.google.protobuf.Internal.EnumLiteMap<KeyType> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<KeyType>() {

            public KeyType findValueByNumber(int number) {
                return KeyType.valueOf(number);
            }
        };

        public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.getDescriptor().getEnumTypes().get(1);
        }

        private static final KeyType[] VALUES = { MINIMUM, PUT, DELETE, DELETE_COLUMN, DELETE_FAMILY, MAXIMUM };

        public static KeyType valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private final int index;

        private final int value;

        private KeyType(int index, int value) {
            this.index = index;
            this.value = value;
        }
    }

    public interface TableSchemaOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        com.google.protobuf.ByteString getName();

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index);

        int getAttributesCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index);

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> getColumnFamiliesList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema getColumnFamilies(int index);

        int getColumnFamiliesCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder> getColumnFamiliesOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder getColumnFamiliesOrBuilder(int index);

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index);

        int getConfigurationCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index);
    }

    public static final class TableSchema extends com.google.protobuf.GeneratedMessage implements TableSchemaOrBuilder {

        private TableSchema(Builder builder) {
            super(builder);
        }

        private TableSchema(boolean noInit) {
        }

        private static final TableSchema defaultInstance;

        public static TableSchema getDefaultInstance() {
            return defaultInstance;
        }

        public TableSchema getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TableSchema_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TableSchema_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getName() {
            return name_;
        }

        public static final int ATTRIBUTES_FIELD_NUMBER = 2;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> attributes_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList() {
            return attributes_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList() {
            return attributes_;
        }

        public int getAttributesCount() {
            return attributes_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index) {
            return attributes_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index) {
            return attributes_.get(index);
        }

        public static final int COLUMNFAMILIES_FIELD_NUMBER = 3;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> columnFamilies_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> getColumnFamiliesList() {
            return columnFamilies_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder> getColumnFamiliesOrBuilderList() {
            return columnFamilies_;
        }

        public int getColumnFamiliesCount() {
            return columnFamilies_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema getColumnFamilies(int index) {
            return columnFamilies_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder getColumnFamiliesOrBuilder(int index) {
            return columnFamilies_.get(index);
        }

        public static final int CONFIGURATION_FIELD_NUMBER = 4;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> configuration_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList() {
            return configuration_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList() {
            return configuration_;
        }

        public int getConfigurationCount() {
            return configuration_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index) {
            return configuration_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index) {
            return configuration_.get(index);
        }

        private void initFields() {
            name_ = com.google.protobuf.ByteString.EMPTY;
            attributes_ = java.util.Collections.emptyList();
            columnFamilies_ = java.util.Collections.emptyList();
            configuration_ = java.util.Collections.emptyList();
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            for (int i = 0; i < getAttributesCount(); i++) {
                if (!getAttributes(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            for (int i = 0; i < getColumnFamiliesCount(); i++) {
                if (!getColumnFamilies(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            for (int i = 0; i < getConfigurationCount(); i++) {
                if (!getConfiguration(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, name_);
            }
            for (int i = 0; i < attributes_.size(); i++) {
                output.writeMessage(2, attributes_.get(i));
            }
            for (int i = 0; i < columnFamilies_.size(); i++) {
                output.writeMessage(3, columnFamilies_.get(i));
            }
            for (int i = 0; i < configuration_.size(); i++) {
                output.writeMessage(4, configuration_.get(i));
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, name_);
            }
            for (int i = 0; i < attributes_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, attributes_.get(i));
            }
            for (int i = 0; i < columnFamilies_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, columnFamilies_.get(i));
            }
            for (int i = 0; i < configuration_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, configuration_.get(i));
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && getAttributesList().equals(other.getAttributesList());
            result = result && getColumnFamiliesList().equals(other.getColumnFamiliesList());
            result = result && getConfigurationList().equals(other.getConfigurationList());
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (getAttributesCount() > 0) {
                hash = (37 * hash) + ATTRIBUTES_FIELD_NUMBER;
                hash = (53 * hash) + getAttributesList().hashCode();
            }
            if (getColumnFamiliesCount() > 0) {
                hash = (37 * hash) + COLUMNFAMILIES_FIELD_NUMBER;
                hash = (53 * hash) + getColumnFamiliesList().hashCode();
            }
            if (getConfigurationCount() > 0) {
                hash = (37 * hash) + CONFIGURATION_FIELD_NUMBER;
                hash = (53 * hash) + getConfigurationList().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchemaOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TableSchema_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TableSchema_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getAttributesFieldBuilder();
                    getColumnFamiliesFieldBuilder();
                    getConfigurationFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                if (attributesBuilder_ == null) {
                    attributes_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                } else {
                    attributesBuilder_.clear();
                }
                if (columnFamiliesBuilder_ == null) {
                    columnFamilies_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000004);
                } else {
                    columnFamiliesBuilder_.clear();
                }
                if (configurationBuilder_ == null) {
                    configuration_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000008);
                } else {
                    configurationBuilder_.clear();
                }
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (attributesBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) == 0x00000002)) {
                        attributes_ = java.util.Collections.unmodifiableList(attributes_);
                        bitField0_ = (bitField0_ & ~0x00000002);
                    }
                    result.attributes_ = attributes_;
                } else {
                    result.attributes_ = attributesBuilder_.build();
                }
                if (columnFamiliesBuilder_ == null) {
                    if (((bitField0_ & 0x00000004) == 0x00000004)) {
                        columnFamilies_ = java.util.Collections.unmodifiableList(columnFamilies_);
                        bitField0_ = (bitField0_ & ~0x00000004);
                    }
                    result.columnFamilies_ = columnFamilies_;
                } else {
                    result.columnFamilies_ = columnFamiliesBuilder_.build();
                }
                if (configurationBuilder_ == null) {
                    if (((bitField0_ & 0x00000008) == 0x00000008)) {
                        configuration_ = java.util.Collections.unmodifiableList(configuration_);
                        bitField0_ = (bitField0_ & ~0x00000008);
                    }
                    result.configuration_ = configuration_;
                } else {
                    result.configuration_ = configurationBuilder_.build();
                }
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (attributesBuilder_ == null) {
                    if (!other.attributes_.isEmpty()) {
                        if (attributes_.isEmpty()) {
                            attributes_ = other.attributes_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                        } else {
                            ensureAttributesIsMutable();
                            attributes_.addAll(other.attributes_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.attributes_.isEmpty()) {
                        if (attributesBuilder_.isEmpty()) {
                            attributesBuilder_.dispose();
                            attributesBuilder_ = null;
                            attributes_ = other.attributes_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                            attributesBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getAttributesFieldBuilder() : null;
                        } else {
                            attributesBuilder_.addAllMessages(other.attributes_);
                        }
                    }
                }
                if (columnFamiliesBuilder_ == null) {
                    if (!other.columnFamilies_.isEmpty()) {
                        if (columnFamilies_.isEmpty()) {
                            columnFamilies_ = other.columnFamilies_;
                            bitField0_ = (bitField0_ & ~0x00000004);
                        } else {
                            ensureColumnFamiliesIsMutable();
                            columnFamilies_.addAll(other.columnFamilies_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.columnFamilies_.isEmpty()) {
                        if (columnFamiliesBuilder_.isEmpty()) {
                            columnFamiliesBuilder_.dispose();
                            columnFamiliesBuilder_ = null;
                            columnFamilies_ = other.columnFamilies_;
                            bitField0_ = (bitField0_ & ~0x00000004);
                            columnFamiliesBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getColumnFamiliesFieldBuilder() : null;
                        } else {
                            columnFamiliesBuilder_.addAllMessages(other.columnFamilies_);
                        }
                    }
                }
                if (configurationBuilder_ == null) {
                    if (!other.configuration_.isEmpty()) {
                        if (configuration_.isEmpty()) {
                            configuration_ = other.configuration_;
                            bitField0_ = (bitField0_ & ~0x00000008);
                        } else {
                            ensureConfigurationIsMutable();
                            configuration_.addAll(other.configuration_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.configuration_.isEmpty()) {
                        if (configurationBuilder_.isEmpty()) {
                            configurationBuilder_.dispose();
                            configurationBuilder_ = null;
                            configuration_ = other.configuration_;
                            bitField0_ = (bitField0_ & ~0x00000008);
                            configurationBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getConfigurationFieldBuilder() : null;
                        } else {
                            configurationBuilder_.addAllMessages(other.configuration_);
                        }
                    }
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                for (int i = 0; i < getAttributesCount(); i++) {
                    if (!getAttributes(i).isInitialized()) {
                        return false;
                    }
                }
                for (int i = 0; i < getColumnFamiliesCount(); i++) {
                    if (!getColumnFamilies(i).isInitialized()) {
                        return false;
                    }
                }
                for (int i = 0; i < getConfigurationCount(); i++) {
                    if (!getConfiguration(i).isInitialized()) {
                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addAttributes(subBuilder.buildPartial());
                                break;
                            }
                        case 26:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addColumnFamilies(subBuilder.buildPartial());
                                break;
                            }
                        case 34:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addConfiguration(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private com.google.protobuf.ByteString name_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getName() {
                return name_;
            }

            public Builder setName(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> attributes_ = java.util.Collections.emptyList();

            private void ensureAttributesIsMutable() {
                if (!((bitField0_ & 0x00000002) == 0x00000002)) {
                    attributes_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair>(attributes_);
                    bitField0_ |= 0x00000002;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> attributesBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList() {
                if (attributesBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(attributes_);
                } else {
                    return attributesBuilder_.getMessageList();
                }
            }

            public int getAttributesCount() {
                if (attributesBuilder_ == null) {
                    return attributes_.size();
                } else {
                    return attributesBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index) {
                if (attributesBuilder_ == null) {
                    return attributes_.get(index);
                } else {
                    return attributesBuilder_.getMessage(index);
                }
            }

            public Builder setAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.set(index, value);
                    onChanged();
                } else {
                    attributesBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAttributes(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.add(value);
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.add(index, value);
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addAttributes(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.add(builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllAttributes(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> values) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    super.addAll(values, attributes_);
                    onChanged();
                } else {
                    attributesBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearAttributes() {
                if (attributesBuilder_ == null) {
                    attributes_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                    onChanged();
                } else {
                    attributesBuilder_.clear();
                }
                return this;
            }

            public Builder removeAttributes(int index) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.remove(index);
                    onChanged();
                } else {
                    attributesBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder getAttributesBuilder(int index) {
                return getAttributesFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index) {
                if (attributesBuilder_ == null) {
                    return attributes_.get(index);
                } else {
                    return attributesBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList() {
                if (attributesBuilder_ != null) {
                    return attributesBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(attributes_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder addAttributesBuilder() {
                return getAttributesFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder addAttributesBuilder(int index) {
                return getAttributesFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder> getAttributesBuilderList() {
                return getAttributesFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesFieldBuilder() {
                if (attributesBuilder_ == null) {
                    attributesBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder>(attributes_, ((bitField0_ & 0x00000002) == 0x00000002), getParentForChildren(), isClean());
                    attributes_ = null;
                }
                return attributesBuilder_;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> columnFamilies_ = java.util.Collections.emptyList();

            private void ensureColumnFamiliesIsMutable() {
                if (!((bitField0_ & 0x00000004) == 0x00000004)) {
                    columnFamilies_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema>(columnFamilies_);
                    bitField0_ |= 0x00000004;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder> columnFamiliesBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> getColumnFamiliesList() {
                if (columnFamiliesBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(columnFamilies_);
                } else {
                    return columnFamiliesBuilder_.getMessageList();
                }
            }

            public int getColumnFamiliesCount() {
                if (columnFamiliesBuilder_ == null) {
                    return columnFamilies_.size();
                } else {
                    return columnFamiliesBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema getColumnFamilies(int index) {
                if (columnFamiliesBuilder_ == null) {
                    return columnFamilies_.get(index);
                } else {
                    return columnFamiliesBuilder_.getMessage(index);
                }
            }

            public Builder setColumnFamilies(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema value) {
                if (columnFamiliesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.set(index, value);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setColumnFamilies(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder builderForValue) {
                if (columnFamiliesBuilder_ == null) {
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    columnFamiliesBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addColumnFamilies(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema value) {
                if (columnFamiliesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.add(value);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addColumnFamilies(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema value) {
                if (columnFamiliesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.add(index, value);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addColumnFamilies(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder builderForValue) {
                if (columnFamiliesBuilder_ == null) {
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.add(builderForValue.build());
                    onChanged();
                } else {
                    columnFamiliesBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addColumnFamilies(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder builderForValue) {
                if (columnFamiliesBuilder_ == null) {
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    columnFamiliesBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllColumnFamilies(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema> values) {
                if (columnFamiliesBuilder_ == null) {
                    ensureColumnFamiliesIsMutable();
                    super.addAll(values, columnFamilies_);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearColumnFamilies() {
                if (columnFamiliesBuilder_ == null) {
                    columnFamilies_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000004);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.clear();
                }
                return this;
            }

            public Builder removeColumnFamilies(int index) {
                if (columnFamiliesBuilder_ == null) {
                    ensureColumnFamiliesIsMutable();
                    columnFamilies_.remove(index);
                    onChanged();
                } else {
                    columnFamiliesBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder getColumnFamiliesBuilder(int index) {
                return getColumnFamiliesFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder getColumnFamiliesOrBuilder(int index) {
                if (columnFamiliesBuilder_ == null) {
                    return columnFamilies_.get(index);
                } else {
                    return columnFamiliesBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder> getColumnFamiliesOrBuilderList() {
                if (columnFamiliesBuilder_ != null) {
                    return columnFamiliesBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(columnFamilies_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder addColumnFamiliesBuilder() {
                return getColumnFamiliesFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder addColumnFamiliesBuilder(int index) {
                return getColumnFamiliesFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder> getColumnFamiliesBuilderList() {
                return getColumnFamiliesFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder> getColumnFamiliesFieldBuilder() {
                if (columnFamiliesBuilder_ == null) {
                    columnFamiliesBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder>(columnFamilies_, ((bitField0_ & 0x00000004) == 0x00000004), getParentForChildren(), isClean());
                    columnFamilies_ = null;
                }
                return columnFamiliesBuilder_;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> configuration_ = java.util.Collections.emptyList();

            private void ensureConfigurationIsMutable() {
                if (!((bitField0_ & 0x00000008) == 0x00000008)) {
                    configuration_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair>(configuration_);
                    bitField0_ |= 0x00000008;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> configurationBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList() {
                if (configurationBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(configuration_);
                } else {
                    return configurationBuilder_.getMessageList();
                }
            }

            public int getConfigurationCount() {
                if (configurationBuilder_ == null) {
                    return configuration_.size();
                } else {
                    return configurationBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index) {
                if (configurationBuilder_ == null) {
                    return configuration_.get(index);
                } else {
                    return configurationBuilder_.getMessage(index);
                }
            }

            public Builder setConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.set(index, value);
                    onChanged();
                } else {
                    configurationBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addConfiguration(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.add(value);
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.add(index, value);
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addConfiguration(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.add(builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllConfiguration(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> values) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    super.addAll(values, configuration_);
                    onChanged();
                } else {
                    configurationBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearConfiguration() {
                if (configurationBuilder_ == null) {
                    configuration_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000008);
                    onChanged();
                } else {
                    configurationBuilder_.clear();
                }
                return this;
            }

            public Builder removeConfiguration(int index) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.remove(index);
                    onChanged();
                } else {
                    configurationBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder getConfigurationBuilder(int index) {
                return getConfigurationFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index) {
                if (configurationBuilder_ == null) {
                    return configuration_.get(index);
                } else {
                    return configurationBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList() {
                if (configurationBuilder_ != null) {
                    return configurationBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(configuration_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder addConfigurationBuilder() {
                return getConfigurationFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder addConfigurationBuilder(int index) {
                return getConfigurationFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder> getConfigurationBuilderList() {
                return getConfigurationFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationFieldBuilder() {
                if (configurationBuilder_ == null) {
                    configurationBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder>(configuration_, ((bitField0_ & 0x00000008) == 0x00000008), getParentForChildren(), isClean());
                    configuration_ = null;
                }
                return configurationBuilder_;
            }
        }

        static {
            defaultInstance = new TableSchema(true);
            defaultInstance.initFields();
        }
    }

    public interface ColumnFamilySchemaOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        com.google.protobuf.ByteString getName();

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index);

        int getAttributesCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index);

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index);

        int getConfigurationCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index);
    }

    public static final class ColumnFamilySchema extends com.google.protobuf.GeneratedMessage implements ColumnFamilySchemaOrBuilder {

        private ColumnFamilySchema(Builder builder) {
            super(builder);
        }

        private ColumnFamilySchema(boolean noInit) {
        }

        private static final ColumnFamilySchema defaultInstance;

        public static ColumnFamilySchema getDefaultInstance() {
            return defaultInstance;
        }

        public ColumnFamilySchema getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ColumnFamilySchema_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ColumnFamilySchema_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getName() {
            return name_;
        }

        public static final int ATTRIBUTES_FIELD_NUMBER = 2;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> attributes_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList() {
            return attributes_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList() {
            return attributes_;
        }

        public int getAttributesCount() {
            return attributes_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index) {
            return attributes_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index) {
            return attributes_.get(index);
        }

        public static final int CONFIGURATION_FIELD_NUMBER = 3;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> configuration_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList() {
            return configuration_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList() {
            return configuration_;
        }

        public int getConfigurationCount() {
            return configuration_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index) {
            return configuration_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index) {
            return configuration_.get(index);
        }

        private void initFields() {
            name_ = com.google.protobuf.ByteString.EMPTY;
            attributes_ = java.util.Collections.emptyList();
            configuration_ = java.util.Collections.emptyList();
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            for (int i = 0; i < getAttributesCount(); i++) {
                if (!getAttributes(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            for (int i = 0; i < getConfigurationCount(); i++) {
                if (!getConfiguration(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, name_);
            }
            for (int i = 0; i < attributes_.size(); i++) {
                output.writeMessage(2, attributes_.get(i));
            }
            for (int i = 0; i < configuration_.size(); i++) {
                output.writeMessage(3, configuration_.get(i));
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, name_);
            }
            for (int i = 0; i < attributes_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, attributes_.get(i));
            }
            for (int i = 0; i < configuration_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, configuration_.get(i));
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && getAttributesList().equals(other.getAttributesList());
            result = result && getConfigurationList().equals(other.getConfigurationList());
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (getAttributesCount() > 0) {
                hash = (37 * hash) + ATTRIBUTES_FIELD_NUMBER;
                hash = (53 * hash) + getAttributesList().hashCode();
            }
            if (getConfigurationCount() > 0) {
                hash = (37 * hash) + CONFIGURATION_FIELD_NUMBER;
                hash = (53 * hash) + getConfigurationList().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchemaOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ColumnFamilySchema_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ColumnFamilySchema_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getAttributesFieldBuilder();
                    getConfigurationFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                if (attributesBuilder_ == null) {
                    attributes_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                } else {
                    attributesBuilder_.clear();
                }
                if (configurationBuilder_ == null) {
                    configuration_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000004);
                } else {
                    configurationBuilder_.clear();
                }
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (attributesBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) == 0x00000002)) {
                        attributes_ = java.util.Collections.unmodifiableList(attributes_);
                        bitField0_ = (bitField0_ & ~0x00000002);
                    }
                    result.attributes_ = attributes_;
                } else {
                    result.attributes_ = attributesBuilder_.build();
                }
                if (configurationBuilder_ == null) {
                    if (((bitField0_ & 0x00000004) == 0x00000004)) {
                        configuration_ = java.util.Collections.unmodifiableList(configuration_);
                        bitField0_ = (bitField0_ & ~0x00000004);
                    }
                    result.configuration_ = configuration_;
                } else {
                    result.configuration_ = configurationBuilder_.build();
                }
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (attributesBuilder_ == null) {
                    if (!other.attributes_.isEmpty()) {
                        if (attributes_.isEmpty()) {
                            attributes_ = other.attributes_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                        } else {
                            ensureAttributesIsMutable();
                            attributes_.addAll(other.attributes_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.attributes_.isEmpty()) {
                        if (attributesBuilder_.isEmpty()) {
                            attributesBuilder_.dispose();
                            attributesBuilder_ = null;
                            attributes_ = other.attributes_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                            attributesBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getAttributesFieldBuilder() : null;
                        } else {
                            attributesBuilder_.addAllMessages(other.attributes_);
                        }
                    }
                }
                if (configurationBuilder_ == null) {
                    if (!other.configuration_.isEmpty()) {
                        if (configuration_.isEmpty()) {
                            configuration_ = other.configuration_;
                            bitField0_ = (bitField0_ & ~0x00000004);
                        } else {
                            ensureConfigurationIsMutable();
                            configuration_.addAll(other.configuration_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.configuration_.isEmpty()) {
                        if (configurationBuilder_.isEmpty()) {
                            configurationBuilder_.dispose();
                            configurationBuilder_ = null;
                            configuration_ = other.configuration_;
                            bitField0_ = (bitField0_ & ~0x00000004);
                            configurationBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getConfigurationFieldBuilder() : null;
                        } else {
                            configurationBuilder_.addAllMessages(other.configuration_);
                        }
                    }
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                for (int i = 0; i < getAttributesCount(); i++) {
                    if (!getAttributes(i).isInitialized()) {
                        return false;
                    }
                }
                for (int i = 0; i < getConfigurationCount(); i++) {
                    if (!getConfiguration(i).isInitialized()) {
                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addAttributes(subBuilder.buildPartial());
                                break;
                            }
                        case 26:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addConfiguration(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private com.google.protobuf.ByteString name_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getName() {
                return name_;
            }

            public Builder setName(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> attributes_ = java.util.Collections.emptyList();

            private void ensureAttributesIsMutable() {
                if (!((bitField0_ & 0x00000002) == 0x00000002)) {
                    attributes_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair>(attributes_);
                    bitField0_ |= 0x00000002;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> attributesBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> getAttributesList() {
                if (attributesBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(attributes_);
                } else {
                    return attributesBuilder_.getMessageList();
                }
            }

            public int getAttributesCount() {
                if (attributesBuilder_ == null) {
                    return attributes_.size();
                } else {
                    return attributesBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getAttributes(int index) {
                if (attributesBuilder_ == null) {
                    return attributes_.get(index);
                } else {
                    return attributesBuilder_.getMessage(index);
                }
            }

            public Builder setAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.set(index, value);
                    onChanged();
                } else {
                    attributesBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAttributes(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.add(value);
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair value) {
                if (attributesBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureAttributesIsMutable();
                    attributes_.add(index, value);
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addAttributes(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.add(builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addAttributes(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder builderForValue) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    attributesBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllAttributes(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair> values) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    super.addAll(values, attributes_);
                    onChanged();
                } else {
                    attributesBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearAttributes() {
                if (attributesBuilder_ == null) {
                    attributes_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                    onChanged();
                } else {
                    attributesBuilder_.clear();
                }
                return this;
            }

            public Builder removeAttributes(int index) {
                if (attributesBuilder_ == null) {
                    ensureAttributesIsMutable();
                    attributes_.remove(index);
                    onChanged();
                } else {
                    attributesBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder getAttributesBuilder(int index) {
                return getAttributesFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder getAttributesOrBuilder(int index) {
                if (attributesBuilder_ == null) {
                    return attributes_.get(index);
                } else {
                    return attributesBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesOrBuilderList() {
                if (attributesBuilder_ != null) {
                    return attributesBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(attributes_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder addAttributesBuilder() {
                return getAttributesFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder addAttributesBuilder(int index) {
                return getAttributesFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder> getAttributesBuilderList() {
                return getAttributesFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder> getAttributesFieldBuilder() {
                if (attributesBuilder_ == null) {
                    attributesBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder>(attributes_, ((bitField0_ & 0x00000002) == 0x00000002), getParentForChildren(), isClean());
                    attributes_ = null;
                }
                return attributesBuilder_;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> configuration_ = java.util.Collections.emptyList();

            private void ensureConfigurationIsMutable() {
                if (!((bitField0_ & 0x00000004) == 0x00000004)) {
                    configuration_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair>(configuration_);
                    bitField0_ |= 0x00000004;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> configurationBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> getConfigurationList() {
                if (configurationBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(configuration_);
                } else {
                    return configurationBuilder_.getMessageList();
                }
            }

            public int getConfigurationCount() {
                if (configurationBuilder_ == null) {
                    return configuration_.size();
                } else {
                    return configurationBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getConfiguration(int index) {
                if (configurationBuilder_ == null) {
                    return configuration_.get(index);
                } else {
                    return configurationBuilder_.getMessage(index);
                }
            }

            public Builder setConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.set(index, value);
                    onChanged();
                } else {
                    configurationBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addConfiguration(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.add(value);
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair value) {
                if (configurationBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureConfigurationIsMutable();
                    configuration_.add(index, value);
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addConfiguration(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.add(builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addConfiguration(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder builderForValue) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    configurationBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllConfiguration(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair> values) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    super.addAll(values, configuration_);
                    onChanged();
                } else {
                    configurationBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearConfiguration() {
                if (configurationBuilder_ == null) {
                    configuration_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000004);
                    onChanged();
                } else {
                    configurationBuilder_.clear();
                }
                return this;
            }

            public Builder removeConfiguration(int index) {
                if (configurationBuilder_ == null) {
                    ensureConfigurationIsMutable();
                    configuration_.remove(index);
                    onChanged();
                } else {
                    configurationBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder getConfigurationBuilder(int index) {
                return getConfigurationFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder getConfigurationOrBuilder(int index) {
                if (configurationBuilder_ == null) {
                    return configuration_.get(index);
                } else {
                    return configurationBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationOrBuilderList() {
                if (configurationBuilder_ != null) {
                    return configurationBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(configuration_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder addConfigurationBuilder() {
                return getConfigurationFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder addConfigurationBuilder(int index) {
                return getConfigurationFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder> getConfigurationBuilderList() {
                return getConfigurationFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder> getConfigurationFieldBuilder() {
                if (configurationBuilder_ == null) {
                    configurationBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder>(configuration_, ((bitField0_ & 0x00000004) == 0x00000004), getParentForChildren(), isClean());
                    configuration_ = null;
                }
                return configurationBuilder_;
            }
        }

        static {
            defaultInstance = new ColumnFamilySchema(true);
            defaultInstance.initFields();
        }
    }

    public interface RegionInfoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasRegionId();

        long getRegionId();

        boolean hasTableName();

        com.google.protobuf.ByteString getTableName();

        boolean hasStartKey();

        com.google.protobuf.ByteString getStartKey();

        boolean hasEndKey();

        com.google.protobuf.ByteString getEndKey();

        boolean hasOffline();

        boolean getOffline();

        boolean hasSplit();

        boolean getSplit();
    }

    public static final class RegionInfo extends com.google.protobuf.GeneratedMessage implements RegionInfoOrBuilder {

        private RegionInfo(Builder builder) {
            super(builder);
        }

        private RegionInfo(boolean noInit) {
        }

        private static final RegionInfo defaultInstance;

        public static RegionInfo getDefaultInstance() {
            return defaultInstance;
        }

        public RegionInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionInfo_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionInfo_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int REGIONID_FIELD_NUMBER = 1;

        private long regionId_;

        public boolean hasRegionId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public long getRegionId() {
            return regionId_;
        }

        public static final int TABLENAME_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString tableName_;

        public boolean hasTableName() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getTableName() {
            return tableName_;
        }

        public static final int STARTKEY_FIELD_NUMBER = 3;

        private com.google.protobuf.ByteString startKey_;

        public boolean hasStartKey() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public com.google.protobuf.ByteString getStartKey() {
            return startKey_;
        }

        public static final int ENDKEY_FIELD_NUMBER = 4;

        private com.google.protobuf.ByteString endKey_;

        public boolean hasEndKey() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public com.google.protobuf.ByteString getEndKey() {
            return endKey_;
        }

        public static final int OFFLINE_FIELD_NUMBER = 5;

        private boolean offline_;

        public boolean hasOffline() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public boolean getOffline() {
            return offline_;
        }

        public static final int SPLIT_FIELD_NUMBER = 6;

        private boolean split_;

        public boolean hasSplit() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public boolean getSplit() {
            return split_;
        }

        private void initFields() {
            regionId_ = 0L;
            tableName_ = com.google.protobuf.ByteString.EMPTY;
            startKey_ = com.google.protobuf.ByteString.EMPTY;
            endKey_ = com.google.protobuf.ByteString.EMPTY;
            offline_ = false;
            split_ = false;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasRegionId()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasTableName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeUInt64(1, regionId_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, tableName_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeBytes(3, startKey_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBytes(4, endKey_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeBool(5, offline_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeBool(6, split_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(1, regionId_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, tableName_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, startKey_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(4, endKey_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeBoolSize(5, offline_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeBoolSize(6, split_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo) obj;
            boolean result = true;
            result = result && (hasRegionId() == other.hasRegionId());
            if (hasRegionId()) {
                result = result && (getRegionId() == other.getRegionId());
            }
            result = result && (hasTableName() == other.hasTableName());
            if (hasTableName()) {
                result = result && getTableName().equals(other.getTableName());
            }
            result = result && (hasStartKey() == other.hasStartKey());
            if (hasStartKey()) {
                result = result && getStartKey().equals(other.getStartKey());
            }
            result = result && (hasEndKey() == other.hasEndKey());
            if (hasEndKey()) {
                result = result && getEndKey().equals(other.getEndKey());
            }
            result = result && (hasOffline() == other.hasOffline());
            if (hasOffline()) {
                result = result && (getOffline() == other.getOffline());
            }
            result = result && (hasSplit() == other.hasSplit());
            if (hasSplit()) {
                result = result && (getSplit() == other.getSplit());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasRegionId()) {
                hash = (37 * hash) + REGIONID_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getRegionId());
            }
            if (hasTableName()) {
                hash = (37 * hash) + TABLENAME_FIELD_NUMBER;
                hash = (53 * hash) + getTableName().hashCode();
            }
            if (hasStartKey()) {
                hash = (37 * hash) + STARTKEY_FIELD_NUMBER;
                hash = (53 * hash) + getStartKey().hashCode();
            }
            if (hasEndKey()) {
                hash = (37 * hash) + ENDKEY_FIELD_NUMBER;
                hash = (53 * hash) + getEndKey().hashCode();
            }
            if (hasOffline()) {
                hash = (37 * hash) + OFFLINE_FIELD_NUMBER;
                hash = (53 * hash) + hashBoolean(getOffline());
            }
            if (hasSplit()) {
                hash = (37 * hash) + SPLIT_FIELD_NUMBER;
                hash = (53 * hash) + hashBoolean(getSplit());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionInfo_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionInfo_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                regionId_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                tableName_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                startKey_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000004);
                endKey_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000008);
                offline_ = false;
                bitField0_ = (bitField0_ & ~0x00000010);
                split_ = false;
                bitField0_ = (bitField0_ & ~0x00000020);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.regionId_ = regionId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.tableName_ = tableName_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.startKey_ = startKey_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.endKey_ = endKey_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.offline_ = offline_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.split_ = split_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo.getDefaultInstance())
                    return this;
                if (other.hasRegionId()) {
                    setRegionId(other.getRegionId());
                }
                if (other.hasTableName()) {
                    setTableName(other.getTableName());
                }
                if (other.hasStartKey()) {
                    setStartKey(other.getStartKey());
                }
                if (other.hasEndKey()) {
                    setEndKey(other.getEndKey());
                }
                if (other.hasOffline()) {
                    setOffline(other.getOffline());
                }
                if (other.hasSplit()) {
                    setSplit(other.getSplit());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasRegionId()) {
                    return false;
                }
                if (!hasTableName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                bitField0_ |= 0x00000001;
                                regionId_ = input.readUInt64();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                tableName_ = input.readBytes();
                                break;
                            }
                        case 26:
                            {
                                bitField0_ |= 0x00000004;
                                startKey_ = input.readBytes();
                                break;
                            }
                        case 34:
                            {
                                bitField0_ |= 0x00000008;
                                endKey_ = input.readBytes();
                                break;
                            }
                        case 40:
                            {
                                bitField0_ |= 0x00000010;
                                offline_ = input.readBool();
                                break;
                            }
                        case 48:
                            {
                                bitField0_ |= 0x00000020;
                                split_ = input.readBool();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private long regionId_;

            public boolean hasRegionId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public long getRegionId() {
                return regionId_;
            }

            public Builder setRegionId(long value) {
                bitField0_ |= 0x00000001;
                regionId_ = value;
                onChanged();
                return this;
            }

            public Builder clearRegionId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                regionId_ = 0L;
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString tableName_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasTableName() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getTableName() {
                return tableName_;
            }

            public Builder setTableName(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                tableName_ = value;
                onChanged();
                return this;
            }

            public Builder clearTableName() {
                bitField0_ = (bitField0_ & ~0x00000002);
                tableName_ = getDefaultInstance().getTableName();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString startKey_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasStartKey() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public com.google.protobuf.ByteString getStartKey() {
                return startKey_;
            }

            public Builder setStartKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                startKey_ = value;
                onChanged();
                return this;
            }

            public Builder clearStartKey() {
                bitField0_ = (bitField0_ & ~0x00000004);
                startKey_ = getDefaultInstance().getStartKey();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString endKey_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasEndKey() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public com.google.protobuf.ByteString getEndKey() {
                return endKey_;
            }

            public Builder setEndKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                endKey_ = value;
                onChanged();
                return this;
            }

            public Builder clearEndKey() {
                bitField0_ = (bitField0_ & ~0x00000008);
                endKey_ = getDefaultInstance().getEndKey();
                onChanged();
                return this;
            }

            private boolean offline_;

            public boolean hasOffline() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public boolean getOffline() {
                return offline_;
            }

            public Builder setOffline(boolean value) {
                bitField0_ |= 0x00000010;
                offline_ = value;
                onChanged();
                return this;
            }

            public Builder clearOffline() {
                bitField0_ = (bitField0_ & ~0x00000010);
                offline_ = false;
                onChanged();
                return this;
            }

            private boolean split_;

            public boolean hasSplit() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }

            public boolean getSplit() {
                return split_;
            }

            public Builder setSplit(boolean value) {
                bitField0_ |= 0x00000020;
                split_ = value;
                onChanged();
                return this;
            }

            public Builder clearSplit() {
                bitField0_ = (bitField0_ & ~0x00000020);
                split_ = false;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new RegionInfo(true);
            defaultInstance.initFields();
        }
    }

    public interface RegionSpecifierOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasType();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType getType();

        boolean hasValue();

        com.google.protobuf.ByteString getValue();
    }

    public static final class RegionSpecifier extends com.google.protobuf.GeneratedMessage implements RegionSpecifierOrBuilder {

        private RegionSpecifier(Builder builder) {
            super(builder);
        }

        private RegionSpecifier(boolean noInit) {
        }

        private static final RegionSpecifier defaultInstance;

        public static RegionSpecifier getDefaultInstance() {
            return defaultInstance;
        }

        public RegionSpecifier getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionSpecifier_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionSpecifier_fieldAccessorTable;
        }

        public enum RegionSpecifierType implements com.google.protobuf.ProtocolMessageEnum {

            REGION_NAME(0, 1), ENCODED_REGION_NAME(1, 2);

            public static final int REGION_NAME_VALUE = 1;

            public static final int ENCODED_REGION_NAME_VALUE = 2;

            public final int getNumber() {
                return value;
            }

            public static RegionSpecifierType valueOf(int value) {
                switch(value) {
                    case 1:
                        return REGION_NAME;
                    case 2:
                        return ENCODED_REGION_NAME;
                    default:
                        return null;
                }
            }

            public static com.google.protobuf.Internal.EnumLiteMap<RegionSpecifierType> internalGetValueMap() {
                return internalValueMap;
            }

            private static com.google.protobuf.Internal.EnumLiteMap<RegionSpecifierType> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<RegionSpecifierType>() {

                public RegionSpecifierType findValueByNumber(int number) {
                    return RegionSpecifierType.valueOf(number);
                }
            };

            public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
                return getDescriptor().getValues().get(index);
            }

            public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
                return getDescriptor();
            }

            public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDescriptor().getEnumTypes().get(0);
            }

            private static final RegionSpecifierType[] VALUES = { REGION_NAME, ENCODED_REGION_NAME };

            public static RegionSpecifierType valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
                if (desc.getType() != getDescriptor()) {
                    throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
                }
                return VALUES[desc.getIndex()];
            }

            private final int index;

            private final int value;

            private RegionSpecifierType(int index, int value) {
                this.index = index;
                this.value = value;
            }
        }

        private int bitField0_;

        public static final int TYPE_FIELD_NUMBER = 1;

        private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType type_;

        public boolean hasType() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType getType() {
            return type_;
        }

        public static final int VALUE_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString value_;

        public boolean hasValue() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getValue() {
            return value_;
        }

        private void initFields() {
            type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.REGION_NAME;
            value_ = com.google.protobuf.ByteString.EMPTY;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasType()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasValue()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeEnum(1, type_.getNumber());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, value_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, type_.getNumber());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, value_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier) obj;
            boolean result = true;
            result = result && (hasType() == other.hasType());
            if (hasType()) {
                result = result && (getType() == other.getType());
            }
            result = result && (hasValue() == other.hasValue());
            if (hasValue()) {
                result = result && getValue().equals(other.getValue());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasType()) {
                hash = (37 * hash) + TYPE_FIELD_NUMBER;
                hash = (53 * hash) + hashEnum(getType());
            }
            if (hasValue()) {
                hash = (37 * hash) + VALUE_FIELD_NUMBER;
                hash = (53 * hash) + getValue().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionSpecifier_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionSpecifier_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.REGION_NAME;
                bitField0_ = (bitField0_ & ~0x00000001);
                value_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.type_ = type_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.value_ = value_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance())
                    return this;
                if (other.hasType()) {
                    setType(other.getType());
                }
                if (other.hasValue()) {
                    setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasType()) {
                    return false;
                }
                if (!hasValue()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                int rawValue = input.readEnum();
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType value = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(1, rawValue);
                                } else {
                                    bitField0_ |= 0x00000001;
                                    type_ = value;
                                }
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                value_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.REGION_NAME;

            public boolean hasType() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType getType() {
                return type_;
            }

            public Builder setType(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                type_ = value;
                onChanged();
                return this;
            }

            public Builder clearType() {
                bitField0_ = (bitField0_ & ~0x00000001);
                type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.RegionSpecifierType.REGION_NAME;
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasValue() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getValue() {
                return value_;
            }

            public Builder setValue(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                value_ = value;
                onChanged();
                return this;
            }

            public Builder clearValue() {
                bitField0_ = (bitField0_ & ~0x00000002);
                value_ = getDefaultInstance().getValue();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new RegionSpecifier(true);
            defaultInstance.initFields();
        }
    }

    public interface RegionLoadOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasRegionSpecifier();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier getRegionSpecifier();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder getRegionSpecifierOrBuilder();

        boolean hasStores();

        int getStores();

        boolean hasStorefiles();

        int getStorefiles();

        boolean hasStoreUncompressedSizeMB();

        int getStoreUncompressedSizeMB();

        boolean hasStorefileSizeMB();

        int getStorefileSizeMB();

        boolean hasMemstoreSizeMB();

        int getMemstoreSizeMB();

        boolean hasStorefileIndexSizeMB();

        int getStorefileIndexSizeMB();

        boolean hasReadRequestsCount();

        long getReadRequestsCount();

        boolean hasWriteRequestsCount();

        long getWriteRequestsCount();

        boolean hasTotalCompactingKVs();

        long getTotalCompactingKVs();

        boolean hasCurrentCompactedKVs();

        long getCurrentCompactedKVs();

        boolean hasRootIndexSizeKB();

        int getRootIndexSizeKB();

        boolean hasTotalStaticIndexSizeKB();

        int getTotalStaticIndexSizeKB();

        boolean hasTotalStaticBloomSizeKB();

        int getTotalStaticBloomSizeKB();

        boolean hasCompleteSequenceId();

        long getCompleteSequenceId();
    }

    public static final class RegionLoad extends com.google.protobuf.GeneratedMessage implements RegionLoadOrBuilder {

        private RegionLoad(Builder builder) {
            super(builder);
        }

        private RegionLoad(boolean noInit) {
        }

        private static final RegionLoad defaultInstance;

        public static RegionLoad getDefaultInstance() {
            return defaultInstance;
        }

        public RegionLoad getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionLoad_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionLoad_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int REGIONSPECIFIER_FIELD_NUMBER = 1;

        private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier regionSpecifier_;

        public boolean hasRegionSpecifier() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier getRegionSpecifier() {
            return regionSpecifier_;
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder getRegionSpecifierOrBuilder() {
            return regionSpecifier_;
        }

        public static final int STORES_FIELD_NUMBER = 2;

        private int stores_;

        public boolean hasStores() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getStores() {
            return stores_;
        }

        public static final int STOREFILES_FIELD_NUMBER = 3;

        private int storefiles_;

        public boolean hasStorefiles() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getStorefiles() {
            return storefiles_;
        }

        public static final int STOREUNCOMPRESSEDSIZEMB_FIELD_NUMBER = 4;

        private int storeUncompressedSizeMB_;

        public boolean hasStoreUncompressedSizeMB() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public int getStoreUncompressedSizeMB() {
            return storeUncompressedSizeMB_;
        }

        public static final int STOREFILESIZEMB_FIELD_NUMBER = 5;

        private int storefileSizeMB_;

        public boolean hasStorefileSizeMB() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public int getStorefileSizeMB() {
            return storefileSizeMB_;
        }

        public static final int MEMSTORESIZEMB_FIELD_NUMBER = 6;

        private int memstoreSizeMB_;

        public boolean hasMemstoreSizeMB() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public int getMemstoreSizeMB() {
            return memstoreSizeMB_;
        }

        public static final int STOREFILEINDEXSIZEMB_FIELD_NUMBER = 7;

        private int storefileIndexSizeMB_;

        public boolean hasStorefileIndexSizeMB() {
            return ((bitField0_ & 0x00000040) == 0x00000040);
        }

        public int getStorefileIndexSizeMB() {
            return storefileIndexSizeMB_;
        }

        public static final int READREQUESTSCOUNT_FIELD_NUMBER = 8;

        private long readRequestsCount_;

        public boolean hasReadRequestsCount() {
            return ((bitField0_ & 0x00000080) == 0x00000080);
        }

        public long getReadRequestsCount() {
            return readRequestsCount_;
        }

        public static final int WRITEREQUESTSCOUNT_FIELD_NUMBER = 9;

        private long writeRequestsCount_;

        public boolean hasWriteRequestsCount() {
            return ((bitField0_ & 0x00000100) == 0x00000100);
        }

        public long getWriteRequestsCount() {
            return writeRequestsCount_;
        }

        public static final int TOTALCOMPACTINGKVS_FIELD_NUMBER = 10;

        private long totalCompactingKVs_;

        public boolean hasTotalCompactingKVs() {
            return ((bitField0_ & 0x00000200) == 0x00000200);
        }

        public long getTotalCompactingKVs() {
            return totalCompactingKVs_;
        }

        public static final int CURRENTCOMPACTEDKVS_FIELD_NUMBER = 11;

        private long currentCompactedKVs_;

        public boolean hasCurrentCompactedKVs() {
            return ((bitField0_ & 0x00000400) == 0x00000400);
        }

        public long getCurrentCompactedKVs() {
            return currentCompactedKVs_;
        }

        public static final int ROOTINDEXSIZEKB_FIELD_NUMBER = 12;

        private int rootIndexSizeKB_;

        public boolean hasRootIndexSizeKB() {
            return ((bitField0_ & 0x00000800) == 0x00000800);
        }

        public int getRootIndexSizeKB() {
            return rootIndexSizeKB_;
        }

        public static final int TOTALSTATICINDEXSIZEKB_FIELD_NUMBER = 13;

        private int totalStaticIndexSizeKB_;

        public boolean hasTotalStaticIndexSizeKB() {
            return ((bitField0_ & 0x00001000) == 0x00001000);
        }

        public int getTotalStaticIndexSizeKB() {
            return totalStaticIndexSizeKB_;
        }

        public static final int TOTALSTATICBLOOMSIZEKB_FIELD_NUMBER = 14;

        private int totalStaticBloomSizeKB_;

        public boolean hasTotalStaticBloomSizeKB() {
            return ((bitField0_ & 0x00002000) == 0x00002000);
        }

        public int getTotalStaticBloomSizeKB() {
            return totalStaticBloomSizeKB_;
        }

        public static final int COMPLETESEQUENCEID_FIELD_NUMBER = 15;

        private long completeSequenceId_;

        public boolean hasCompleteSequenceId() {
            return ((bitField0_ & 0x00004000) == 0x00004000);
        }

        public long getCompleteSequenceId() {
            return completeSequenceId_;
        }

        private void initFields() {
            regionSpecifier_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance();
            stores_ = 0;
            storefiles_ = 0;
            storeUncompressedSizeMB_ = 0;
            storefileSizeMB_ = 0;
            memstoreSizeMB_ = 0;
            storefileIndexSizeMB_ = 0;
            readRequestsCount_ = 0L;
            writeRequestsCount_ = 0L;
            totalCompactingKVs_ = 0L;
            currentCompactedKVs_ = 0L;
            rootIndexSizeKB_ = 0;
            totalStaticIndexSizeKB_ = 0;
            totalStaticBloomSizeKB_ = 0;
            completeSequenceId_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasRegionSpecifier()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!getRegionSpecifier().isInitialized()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeMessage(1, regionSpecifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeUInt32(2, stores_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeUInt32(3, storefiles_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeUInt32(4, storeUncompressedSizeMB_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeUInt32(5, storefileSizeMB_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeUInt32(6, memstoreSizeMB_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                output.writeUInt32(7, storefileIndexSizeMB_);
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                output.writeUInt64(8, readRequestsCount_);
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                output.writeUInt64(9, writeRequestsCount_);
            }
            if (((bitField0_ & 0x00000200) == 0x00000200)) {
                output.writeUInt64(10, totalCompactingKVs_);
            }
            if (((bitField0_ & 0x00000400) == 0x00000400)) {
                output.writeUInt64(11, currentCompactedKVs_);
            }
            if (((bitField0_ & 0x00000800) == 0x00000800)) {
                output.writeUInt32(12, rootIndexSizeKB_);
            }
            if (((bitField0_ & 0x00001000) == 0x00001000)) {
                output.writeUInt32(13, totalStaticIndexSizeKB_);
            }
            if (((bitField0_ & 0x00002000) == 0x00002000)) {
                output.writeUInt32(14, totalStaticBloomSizeKB_);
            }
            if (((bitField0_ & 0x00004000) == 0x00004000)) {
                output.writeUInt64(15, completeSequenceId_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, regionSpecifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, stores_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(3, storefiles_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(4, storeUncompressedSizeMB_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(5, storefileSizeMB_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(6, memstoreSizeMB_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(7, storefileIndexSizeMB_);
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(8, readRequestsCount_);
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(9, writeRequestsCount_);
            }
            if (((bitField0_ & 0x00000200) == 0x00000200)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(10, totalCompactingKVs_);
            }
            if (((bitField0_ & 0x00000400) == 0x00000400)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(11, currentCompactedKVs_);
            }
            if (((bitField0_ & 0x00000800) == 0x00000800)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(12, rootIndexSizeKB_);
            }
            if (((bitField0_ & 0x00001000) == 0x00001000)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(13, totalStaticIndexSizeKB_);
            }
            if (((bitField0_ & 0x00002000) == 0x00002000)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(14, totalStaticBloomSizeKB_);
            }
            if (((bitField0_ & 0x00004000) == 0x00004000)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(15, completeSequenceId_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad) obj;
            boolean result = true;
            result = result && (hasRegionSpecifier() == other.hasRegionSpecifier());
            if (hasRegionSpecifier()) {
                result = result && getRegionSpecifier().equals(other.getRegionSpecifier());
            }
            result = result && (hasStores() == other.hasStores());
            if (hasStores()) {
                result = result && (getStores() == other.getStores());
            }
            result = result && (hasStorefiles() == other.hasStorefiles());
            if (hasStorefiles()) {
                result = result && (getStorefiles() == other.getStorefiles());
            }
            result = result && (hasStoreUncompressedSizeMB() == other.hasStoreUncompressedSizeMB());
            if (hasStoreUncompressedSizeMB()) {
                result = result && (getStoreUncompressedSizeMB() == other.getStoreUncompressedSizeMB());
            }
            result = result && (hasStorefileSizeMB() == other.hasStorefileSizeMB());
            if (hasStorefileSizeMB()) {
                result = result && (getStorefileSizeMB() == other.getStorefileSizeMB());
            }
            result = result && (hasMemstoreSizeMB() == other.hasMemstoreSizeMB());
            if (hasMemstoreSizeMB()) {
                result = result && (getMemstoreSizeMB() == other.getMemstoreSizeMB());
            }
            result = result && (hasStorefileIndexSizeMB() == other.hasStorefileIndexSizeMB());
            if (hasStorefileIndexSizeMB()) {
                result = result && (getStorefileIndexSizeMB() == other.getStorefileIndexSizeMB());
            }
            result = result && (hasReadRequestsCount() == other.hasReadRequestsCount());
            if (hasReadRequestsCount()) {
                result = result && (getReadRequestsCount() == other.getReadRequestsCount());
            }
            result = result && (hasWriteRequestsCount() == other.hasWriteRequestsCount());
            if (hasWriteRequestsCount()) {
                result = result && (getWriteRequestsCount() == other.getWriteRequestsCount());
            }
            result = result && (hasTotalCompactingKVs() == other.hasTotalCompactingKVs());
            if (hasTotalCompactingKVs()) {
                result = result && (getTotalCompactingKVs() == other.getTotalCompactingKVs());
            }
            result = result && (hasCurrentCompactedKVs() == other.hasCurrentCompactedKVs());
            if (hasCurrentCompactedKVs()) {
                result = result && (getCurrentCompactedKVs() == other.getCurrentCompactedKVs());
            }
            result = result && (hasRootIndexSizeKB() == other.hasRootIndexSizeKB());
            if (hasRootIndexSizeKB()) {
                result = result && (getRootIndexSizeKB() == other.getRootIndexSizeKB());
            }
            result = result && (hasTotalStaticIndexSizeKB() == other.hasTotalStaticIndexSizeKB());
            if (hasTotalStaticIndexSizeKB()) {
                result = result && (getTotalStaticIndexSizeKB() == other.getTotalStaticIndexSizeKB());
            }
            result = result && (hasTotalStaticBloomSizeKB() == other.hasTotalStaticBloomSizeKB());
            if (hasTotalStaticBloomSizeKB()) {
                result = result && (getTotalStaticBloomSizeKB() == other.getTotalStaticBloomSizeKB());
            }
            result = result && (hasCompleteSequenceId() == other.hasCompleteSequenceId());
            if (hasCompleteSequenceId()) {
                result = result && (getCompleteSequenceId() == other.getCompleteSequenceId());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasRegionSpecifier()) {
                hash = (37 * hash) + REGIONSPECIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getRegionSpecifier().hashCode();
            }
            if (hasStores()) {
                hash = (37 * hash) + STORES_FIELD_NUMBER;
                hash = (53 * hash) + getStores();
            }
            if (hasStorefiles()) {
                hash = (37 * hash) + STOREFILES_FIELD_NUMBER;
                hash = (53 * hash) + getStorefiles();
            }
            if (hasStoreUncompressedSizeMB()) {
                hash = (37 * hash) + STOREUNCOMPRESSEDSIZEMB_FIELD_NUMBER;
                hash = (53 * hash) + getStoreUncompressedSizeMB();
            }
            if (hasStorefileSizeMB()) {
                hash = (37 * hash) + STOREFILESIZEMB_FIELD_NUMBER;
                hash = (53 * hash) + getStorefileSizeMB();
            }
            if (hasMemstoreSizeMB()) {
                hash = (37 * hash) + MEMSTORESIZEMB_FIELD_NUMBER;
                hash = (53 * hash) + getMemstoreSizeMB();
            }
            if (hasStorefileIndexSizeMB()) {
                hash = (37 * hash) + STOREFILEINDEXSIZEMB_FIELD_NUMBER;
                hash = (53 * hash) + getStorefileIndexSizeMB();
            }
            if (hasReadRequestsCount()) {
                hash = (37 * hash) + READREQUESTSCOUNT_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getReadRequestsCount());
            }
            if (hasWriteRequestsCount()) {
                hash = (37 * hash) + WRITEREQUESTSCOUNT_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getWriteRequestsCount());
            }
            if (hasTotalCompactingKVs()) {
                hash = (37 * hash) + TOTALCOMPACTINGKVS_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getTotalCompactingKVs());
            }
            if (hasCurrentCompactedKVs()) {
                hash = (37 * hash) + CURRENTCOMPACTEDKVS_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getCurrentCompactedKVs());
            }
            if (hasRootIndexSizeKB()) {
                hash = (37 * hash) + ROOTINDEXSIZEKB_FIELD_NUMBER;
                hash = (53 * hash) + getRootIndexSizeKB();
            }
            if (hasTotalStaticIndexSizeKB()) {
                hash = (37 * hash) + TOTALSTATICINDEXSIZEKB_FIELD_NUMBER;
                hash = (53 * hash) + getTotalStaticIndexSizeKB();
            }
            if (hasTotalStaticBloomSizeKB()) {
                hash = (37 * hash) + TOTALSTATICBLOOMSIZEKB_FIELD_NUMBER;
                hash = (53 * hash) + getTotalStaticBloomSizeKB();
            }
            if (hasCompleteSequenceId()) {
                hash = (37 * hash) + COMPLETESEQUENCEID_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getCompleteSequenceId());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionLoad_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_RegionLoad_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getRegionSpecifierFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                if (regionSpecifierBuilder_ == null) {
                    regionSpecifier_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance();
                } else {
                    regionSpecifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                stores_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                storefiles_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                storeUncompressedSizeMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000008);
                storefileSizeMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000010);
                memstoreSizeMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000020);
                storefileIndexSizeMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000040);
                readRequestsCount_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000080);
                writeRequestsCount_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000100);
                totalCompactingKVs_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000200);
                currentCompactedKVs_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000400);
                rootIndexSizeKB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000800);
                totalStaticIndexSizeKB_ = 0;
                bitField0_ = (bitField0_ & ~0x00001000);
                totalStaticBloomSizeKB_ = 0;
                bitField0_ = (bitField0_ & ~0x00002000);
                completeSequenceId_ = 0L;
                bitField0_ = (bitField0_ & ~0x00004000);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                if (regionSpecifierBuilder_ == null) {
                    result.regionSpecifier_ = regionSpecifier_;
                } else {
                    result.regionSpecifier_ = regionSpecifierBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.stores_ = stores_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.storefiles_ = storefiles_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.storeUncompressedSizeMB_ = storeUncompressedSizeMB_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.storefileSizeMB_ = storefileSizeMB_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.memstoreSizeMB_ = memstoreSizeMB_;
                if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
                    to_bitField0_ |= 0x00000040;
                }
                result.storefileIndexSizeMB_ = storefileIndexSizeMB_;
                if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
                    to_bitField0_ |= 0x00000080;
                }
                result.readRequestsCount_ = readRequestsCount_;
                if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
                    to_bitField0_ |= 0x00000100;
                }
                result.writeRequestsCount_ = writeRequestsCount_;
                if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
                    to_bitField0_ |= 0x00000200;
                }
                result.totalCompactingKVs_ = totalCompactingKVs_;
                if (((from_bitField0_ & 0x00000400) == 0x00000400)) {
                    to_bitField0_ |= 0x00000400;
                }
                result.currentCompactedKVs_ = currentCompactedKVs_;
                if (((from_bitField0_ & 0x00000800) == 0x00000800)) {
                    to_bitField0_ |= 0x00000800;
                }
                result.rootIndexSizeKB_ = rootIndexSizeKB_;
                if (((from_bitField0_ & 0x00001000) == 0x00001000)) {
                    to_bitField0_ |= 0x00001000;
                }
                result.totalStaticIndexSizeKB_ = totalStaticIndexSizeKB_;
                if (((from_bitField0_ & 0x00002000) == 0x00002000)) {
                    to_bitField0_ |= 0x00002000;
                }
                result.totalStaticBloomSizeKB_ = totalStaticBloomSizeKB_;
                if (((from_bitField0_ & 0x00004000) == 0x00004000)) {
                    to_bitField0_ |= 0x00004000;
                }
                result.completeSequenceId_ = completeSequenceId_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.getDefaultInstance())
                    return this;
                if (other.hasRegionSpecifier()) {
                    mergeRegionSpecifier(other.getRegionSpecifier());
                }
                if (other.hasStores()) {
                    setStores(other.getStores());
                }
                if (other.hasStorefiles()) {
                    setStorefiles(other.getStorefiles());
                }
                if (other.hasStoreUncompressedSizeMB()) {
                    setStoreUncompressedSizeMB(other.getStoreUncompressedSizeMB());
                }
                if (other.hasStorefileSizeMB()) {
                    setStorefileSizeMB(other.getStorefileSizeMB());
                }
                if (other.hasMemstoreSizeMB()) {
                    setMemstoreSizeMB(other.getMemstoreSizeMB());
                }
                if (other.hasStorefileIndexSizeMB()) {
                    setStorefileIndexSizeMB(other.getStorefileIndexSizeMB());
                }
                if (other.hasReadRequestsCount()) {
                    setReadRequestsCount(other.getReadRequestsCount());
                }
                if (other.hasWriteRequestsCount()) {
                    setWriteRequestsCount(other.getWriteRequestsCount());
                }
                if (other.hasTotalCompactingKVs()) {
                    setTotalCompactingKVs(other.getTotalCompactingKVs());
                }
                if (other.hasCurrentCompactedKVs()) {
                    setCurrentCompactedKVs(other.getCurrentCompactedKVs());
                }
                if (other.hasRootIndexSizeKB()) {
                    setRootIndexSizeKB(other.getRootIndexSizeKB());
                }
                if (other.hasTotalStaticIndexSizeKB()) {
                    setTotalStaticIndexSizeKB(other.getTotalStaticIndexSizeKB());
                }
                if (other.hasTotalStaticBloomSizeKB()) {
                    setTotalStaticBloomSizeKB(other.getTotalStaticBloomSizeKB());
                }
                if (other.hasCompleteSequenceId()) {
                    setCompleteSequenceId(other.getCompleteSequenceId());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasRegionSpecifier()) {
                    return false;
                }
                if (!getRegionSpecifier().isInitialized()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.newBuilder();
                                if (hasRegionSpecifier()) {
                                    subBuilder.mergeFrom(getRegionSpecifier());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setRegionSpecifier(subBuilder.buildPartial());
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                stores_ = input.readUInt32();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                storefiles_ = input.readUInt32();
                                break;
                            }
                        case 32:
                            {
                                bitField0_ |= 0x00000008;
                                storeUncompressedSizeMB_ = input.readUInt32();
                                break;
                            }
                        case 40:
                            {
                                bitField0_ |= 0x00000010;
                                storefileSizeMB_ = input.readUInt32();
                                break;
                            }
                        case 48:
                            {
                                bitField0_ |= 0x00000020;
                                memstoreSizeMB_ = input.readUInt32();
                                break;
                            }
                        case 56:
                            {
                                bitField0_ |= 0x00000040;
                                storefileIndexSizeMB_ = input.readUInt32();
                                break;
                            }
                        case 64:
                            {
                                bitField0_ |= 0x00000080;
                                readRequestsCount_ = input.readUInt64();
                                break;
                            }
                        case 72:
                            {
                                bitField0_ |= 0x00000100;
                                writeRequestsCount_ = input.readUInt64();
                                break;
                            }
                        case 80:
                            {
                                bitField0_ |= 0x00000200;
                                totalCompactingKVs_ = input.readUInt64();
                                break;
                            }
                        case 88:
                            {
                                bitField0_ |= 0x00000400;
                                currentCompactedKVs_ = input.readUInt64();
                                break;
                            }
                        case 96:
                            {
                                bitField0_ |= 0x00000800;
                                rootIndexSizeKB_ = input.readUInt32();
                                break;
                            }
                        case 104:
                            {
                                bitField0_ |= 0x00001000;
                                totalStaticIndexSizeKB_ = input.readUInt32();
                                break;
                            }
                        case 112:
                            {
                                bitField0_ |= 0x00002000;
                                totalStaticBloomSizeKB_ = input.readUInt32();
                                break;
                            }
                        case 120:
                            {
                                bitField0_ |= 0x00004000;
                                completeSequenceId_ = input.readUInt64();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier regionSpecifier_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder> regionSpecifierBuilder_;

            public boolean hasRegionSpecifier() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier getRegionSpecifier() {
                if (regionSpecifierBuilder_ == null) {
                    return regionSpecifier_;
                } else {
                    return regionSpecifierBuilder_.getMessage();
                }
            }

            public Builder setRegionSpecifier(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier value) {
                if (regionSpecifierBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    regionSpecifier_ = value;
                    onChanged();
                } else {
                    regionSpecifierBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder setRegionSpecifier(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder builderForValue) {
                if (regionSpecifierBuilder_ == null) {
                    regionSpecifier_ = builderForValue.build();
                    onChanged();
                } else {
                    regionSpecifierBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder mergeRegionSpecifier(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier value) {
                if (regionSpecifierBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) == 0x00000001) && regionSpecifier_ != org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance()) {
                        regionSpecifier_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.newBuilder(regionSpecifier_).mergeFrom(value).buildPartial();
                    } else {
                        regionSpecifier_ = value;
                    }
                    onChanged();
                } else {
                    regionSpecifierBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder clearRegionSpecifier() {
                if (regionSpecifierBuilder_ == null) {
                    regionSpecifier_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.getDefaultInstance();
                    onChanged();
                } else {
                    regionSpecifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder getRegionSpecifierBuilder() {
                bitField0_ |= 0x00000001;
                onChanged();
                return getRegionSpecifierFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder getRegionSpecifierOrBuilder() {
                if (regionSpecifierBuilder_ != null) {
                    return regionSpecifierBuilder_.getMessageOrBuilder();
                } else {
                    return regionSpecifier_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder> getRegionSpecifierFieldBuilder() {
                if (regionSpecifierBuilder_ == null) {
                    regionSpecifierBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifierOrBuilder>(regionSpecifier_, getParentForChildren(), isClean());
                    regionSpecifier_ = null;
                }
                return regionSpecifierBuilder_;
            }

            private int stores_;

            public boolean hasStores() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getStores() {
                return stores_;
            }

            public Builder setStores(int value) {
                bitField0_ |= 0x00000002;
                stores_ = value;
                onChanged();
                return this;
            }

            public Builder clearStores() {
                bitField0_ = (bitField0_ & ~0x00000002);
                stores_ = 0;
                onChanged();
                return this;
            }

            private int storefiles_;

            public boolean hasStorefiles() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getStorefiles() {
                return storefiles_;
            }

            public Builder setStorefiles(int value) {
                bitField0_ |= 0x00000004;
                storefiles_ = value;
                onChanged();
                return this;
            }

            public Builder clearStorefiles() {
                bitField0_ = (bitField0_ & ~0x00000004);
                storefiles_ = 0;
                onChanged();
                return this;
            }

            private int storeUncompressedSizeMB_;

            public boolean hasStoreUncompressedSizeMB() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public int getStoreUncompressedSizeMB() {
                return storeUncompressedSizeMB_;
            }

            public Builder setStoreUncompressedSizeMB(int value) {
                bitField0_ |= 0x00000008;
                storeUncompressedSizeMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearStoreUncompressedSizeMB() {
                bitField0_ = (bitField0_ & ~0x00000008);
                storeUncompressedSizeMB_ = 0;
                onChanged();
                return this;
            }

            private int storefileSizeMB_;

            public boolean hasStorefileSizeMB() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public int getStorefileSizeMB() {
                return storefileSizeMB_;
            }

            public Builder setStorefileSizeMB(int value) {
                bitField0_ |= 0x00000010;
                storefileSizeMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearStorefileSizeMB() {
                bitField0_ = (bitField0_ & ~0x00000010);
                storefileSizeMB_ = 0;
                onChanged();
                return this;
            }

            private int memstoreSizeMB_;

            public boolean hasMemstoreSizeMB() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }

            public int getMemstoreSizeMB() {
                return memstoreSizeMB_;
            }

            public Builder setMemstoreSizeMB(int value) {
                bitField0_ |= 0x00000020;
                memstoreSizeMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearMemstoreSizeMB() {
                bitField0_ = (bitField0_ & ~0x00000020);
                memstoreSizeMB_ = 0;
                onChanged();
                return this;
            }

            private int storefileIndexSizeMB_;

            public boolean hasStorefileIndexSizeMB() {
                return ((bitField0_ & 0x00000040) == 0x00000040);
            }

            public int getStorefileIndexSizeMB() {
                return storefileIndexSizeMB_;
            }

            public Builder setStorefileIndexSizeMB(int value) {
                bitField0_ |= 0x00000040;
                storefileIndexSizeMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearStorefileIndexSizeMB() {
                bitField0_ = (bitField0_ & ~0x00000040);
                storefileIndexSizeMB_ = 0;
                onChanged();
                return this;
            }

            private long readRequestsCount_;

            public boolean hasReadRequestsCount() {
                return ((bitField0_ & 0x00000080) == 0x00000080);
            }

            public long getReadRequestsCount() {
                return readRequestsCount_;
            }

            public Builder setReadRequestsCount(long value) {
                bitField0_ |= 0x00000080;
                readRequestsCount_ = value;
                onChanged();
                return this;
            }

            public Builder clearReadRequestsCount() {
                bitField0_ = (bitField0_ & ~0x00000080);
                readRequestsCount_ = 0L;
                onChanged();
                return this;
            }

            private long writeRequestsCount_;

            public boolean hasWriteRequestsCount() {
                return ((bitField0_ & 0x00000100) == 0x00000100);
            }

            public long getWriteRequestsCount() {
                return writeRequestsCount_;
            }

            public Builder setWriteRequestsCount(long value) {
                bitField0_ |= 0x00000100;
                writeRequestsCount_ = value;
                onChanged();
                return this;
            }

            public Builder clearWriteRequestsCount() {
                bitField0_ = (bitField0_ & ~0x00000100);
                writeRequestsCount_ = 0L;
                onChanged();
                return this;
            }

            private long totalCompactingKVs_;

            public boolean hasTotalCompactingKVs() {
                return ((bitField0_ & 0x00000200) == 0x00000200);
            }

            public long getTotalCompactingKVs() {
                return totalCompactingKVs_;
            }

            public Builder setTotalCompactingKVs(long value) {
                bitField0_ |= 0x00000200;
                totalCompactingKVs_ = value;
                onChanged();
                return this;
            }

            public Builder clearTotalCompactingKVs() {
                bitField0_ = (bitField0_ & ~0x00000200);
                totalCompactingKVs_ = 0L;
                onChanged();
                return this;
            }

            private long currentCompactedKVs_;

            public boolean hasCurrentCompactedKVs() {
                return ((bitField0_ & 0x00000400) == 0x00000400);
            }

            public long getCurrentCompactedKVs() {
                return currentCompactedKVs_;
            }

            public Builder setCurrentCompactedKVs(long value) {
                bitField0_ |= 0x00000400;
                currentCompactedKVs_ = value;
                onChanged();
                return this;
            }

            public Builder clearCurrentCompactedKVs() {
                bitField0_ = (bitField0_ & ~0x00000400);
                currentCompactedKVs_ = 0L;
                onChanged();
                return this;
            }

            private int rootIndexSizeKB_;

            public boolean hasRootIndexSizeKB() {
                return ((bitField0_ & 0x00000800) == 0x00000800);
            }

            public int getRootIndexSizeKB() {
                return rootIndexSizeKB_;
            }

            public Builder setRootIndexSizeKB(int value) {
                bitField0_ |= 0x00000800;
                rootIndexSizeKB_ = value;
                onChanged();
                return this;
            }

            public Builder clearRootIndexSizeKB() {
                bitField0_ = (bitField0_ & ~0x00000800);
                rootIndexSizeKB_ = 0;
                onChanged();
                return this;
            }

            private int totalStaticIndexSizeKB_;

            public boolean hasTotalStaticIndexSizeKB() {
                return ((bitField0_ & 0x00001000) == 0x00001000);
            }

            public int getTotalStaticIndexSizeKB() {
                return totalStaticIndexSizeKB_;
            }

            public Builder setTotalStaticIndexSizeKB(int value) {
                bitField0_ |= 0x00001000;
                totalStaticIndexSizeKB_ = value;
                onChanged();
                return this;
            }

            public Builder clearTotalStaticIndexSizeKB() {
                bitField0_ = (bitField0_ & ~0x00001000);
                totalStaticIndexSizeKB_ = 0;
                onChanged();
                return this;
            }

            private int totalStaticBloomSizeKB_;

            public boolean hasTotalStaticBloomSizeKB() {
                return ((bitField0_ & 0x00002000) == 0x00002000);
            }

            public int getTotalStaticBloomSizeKB() {
                return totalStaticBloomSizeKB_;
            }

            public Builder setTotalStaticBloomSizeKB(int value) {
                bitField0_ |= 0x00002000;
                totalStaticBloomSizeKB_ = value;
                onChanged();
                return this;
            }

            public Builder clearTotalStaticBloomSizeKB() {
                bitField0_ = (bitField0_ & ~0x00002000);
                totalStaticBloomSizeKB_ = 0;
                onChanged();
                return this;
            }

            private long completeSequenceId_;

            public boolean hasCompleteSequenceId() {
                return ((bitField0_ & 0x00004000) == 0x00004000);
            }

            public long getCompleteSequenceId() {
                return completeSequenceId_;
            }

            public Builder setCompleteSequenceId(long value) {
                bitField0_ |= 0x00004000;
                completeSequenceId_ = value;
                onChanged();
                return this;
            }

            public Builder clearCompleteSequenceId() {
                bitField0_ = (bitField0_ & ~0x00004000);
                completeSequenceId_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new RegionLoad(true);
            defaultInstance.initFields();
        }
    }

    public interface ServerLoadOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasNumberOfRequests();

        int getNumberOfRequests();

        boolean hasTotalNumberOfRequests();

        int getTotalNumberOfRequests();

        boolean hasUsedHeapMB();

        int getUsedHeapMB();

        boolean hasMaxHeapMB();

        int getMaxHeapMB();

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> getRegionLoadsList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad getRegionLoads(int index);

        int getRegionLoadsCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder> getRegionLoadsOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder getRegionLoadsOrBuilder(int index);

        java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> getCoprocessorsList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor getCoprocessors(int index);

        int getCoprocessorsCount();

        java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder> getCoprocessorsOrBuilderList();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder getCoprocessorsOrBuilder(int index);

        boolean hasReportStartTime();

        long getReportStartTime();

        boolean hasReportEndTime();

        long getReportEndTime();

        boolean hasInfoServerPort();

        int getInfoServerPort();
    }

    public static final class ServerLoad extends com.google.protobuf.GeneratedMessage implements ServerLoadOrBuilder {

        private ServerLoad(Builder builder) {
            super(builder);
        }

        private ServerLoad(boolean noInit) {
        }

        private static final ServerLoad defaultInstance;

        public static ServerLoad getDefaultInstance() {
            return defaultInstance;
        }

        public ServerLoad getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerLoad_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerLoad_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NUMBEROFREQUESTS_FIELD_NUMBER = 1;

        private int numberOfRequests_;

        public boolean hasNumberOfRequests() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public int getNumberOfRequests() {
            return numberOfRequests_;
        }

        public static final int TOTALNUMBEROFREQUESTS_FIELD_NUMBER = 2;

        private int totalNumberOfRequests_;

        public boolean hasTotalNumberOfRequests() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getTotalNumberOfRequests() {
            return totalNumberOfRequests_;
        }

        public static final int USEDHEAPMB_FIELD_NUMBER = 3;

        private int usedHeapMB_;

        public boolean hasUsedHeapMB() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getUsedHeapMB() {
            return usedHeapMB_;
        }

        public static final int MAXHEAPMB_FIELD_NUMBER = 4;

        private int maxHeapMB_;

        public boolean hasMaxHeapMB() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public int getMaxHeapMB() {
            return maxHeapMB_;
        }

        public static final int REGIONLOADS_FIELD_NUMBER = 5;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> regionLoads_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> getRegionLoadsList() {
            return regionLoads_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder> getRegionLoadsOrBuilderList() {
            return regionLoads_;
        }

        public int getRegionLoadsCount() {
            return regionLoads_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad getRegionLoads(int index) {
            return regionLoads_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder getRegionLoadsOrBuilder(int index) {
            return regionLoads_.get(index);
        }

        public static final int COPROCESSORS_FIELD_NUMBER = 6;

        private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> coprocessors_;

        public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> getCoprocessorsList() {
            return coprocessors_;
        }

        public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder> getCoprocessorsOrBuilderList() {
            return coprocessors_;
        }

        public int getCoprocessorsCount() {
            return coprocessors_.size();
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor getCoprocessors(int index) {
            return coprocessors_.get(index);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder getCoprocessorsOrBuilder(int index) {
            return coprocessors_.get(index);
        }

        public static final int REPORTSTARTTIME_FIELD_NUMBER = 7;

        private long reportStartTime_;

        public boolean hasReportStartTime() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public long getReportStartTime() {
            return reportStartTime_;
        }

        public static final int REPORTENDTIME_FIELD_NUMBER = 8;

        private long reportEndTime_;

        public boolean hasReportEndTime() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public long getReportEndTime() {
            return reportEndTime_;
        }

        public static final int INFOSERVERPORT_FIELD_NUMBER = 9;

        private int infoServerPort_;

        public boolean hasInfoServerPort() {
            return ((bitField0_ & 0x00000040) == 0x00000040);
        }

        public int getInfoServerPort() {
            return infoServerPort_;
        }

        private void initFields() {
            numberOfRequests_ = 0;
            totalNumberOfRequests_ = 0;
            usedHeapMB_ = 0;
            maxHeapMB_ = 0;
            regionLoads_ = java.util.Collections.emptyList();
            coprocessors_ = java.util.Collections.emptyList();
            reportStartTime_ = 0L;
            reportEndTime_ = 0L;
            infoServerPort_ = 0;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            for (int i = 0; i < getRegionLoadsCount(); i++) {
                if (!getRegionLoads(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            for (int i = 0; i < getCoprocessorsCount(); i++) {
                if (!getCoprocessors(i).isInitialized()) {
                    memoizedIsInitialized = 0;
                    return false;
                }
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeUInt32(1, numberOfRequests_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeUInt32(2, totalNumberOfRequests_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeUInt32(3, usedHeapMB_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeUInt32(4, maxHeapMB_);
            }
            for (int i = 0; i < regionLoads_.size(); i++) {
                output.writeMessage(5, regionLoads_.get(i));
            }
            for (int i = 0; i < coprocessors_.size(); i++) {
                output.writeMessage(6, coprocessors_.get(i));
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeUInt64(7, reportStartTime_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeUInt64(8, reportEndTime_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                output.writeUInt32(9, infoServerPort_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(1, numberOfRequests_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, totalNumberOfRequests_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(3, usedHeapMB_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(4, maxHeapMB_);
            }
            for (int i = 0; i < regionLoads_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, regionLoads_.get(i));
            }
            for (int i = 0; i < coprocessors_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, coprocessors_.get(i));
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(7, reportStartTime_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(8, reportEndTime_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(9, infoServerPort_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad) obj;
            boolean result = true;
            result = result && (hasNumberOfRequests() == other.hasNumberOfRequests());
            if (hasNumberOfRequests()) {
                result = result && (getNumberOfRequests() == other.getNumberOfRequests());
            }
            result = result && (hasTotalNumberOfRequests() == other.hasTotalNumberOfRequests());
            if (hasTotalNumberOfRequests()) {
                result = result && (getTotalNumberOfRequests() == other.getTotalNumberOfRequests());
            }
            result = result && (hasUsedHeapMB() == other.hasUsedHeapMB());
            if (hasUsedHeapMB()) {
                result = result && (getUsedHeapMB() == other.getUsedHeapMB());
            }
            result = result && (hasMaxHeapMB() == other.hasMaxHeapMB());
            if (hasMaxHeapMB()) {
                result = result && (getMaxHeapMB() == other.getMaxHeapMB());
            }
            result = result && getRegionLoadsList().equals(other.getRegionLoadsList());
            result = result && getCoprocessorsList().equals(other.getCoprocessorsList());
            result = result && (hasReportStartTime() == other.hasReportStartTime());
            if (hasReportStartTime()) {
                result = result && (getReportStartTime() == other.getReportStartTime());
            }
            result = result && (hasReportEndTime() == other.hasReportEndTime());
            if (hasReportEndTime()) {
                result = result && (getReportEndTime() == other.getReportEndTime());
            }
            result = result && (hasInfoServerPort() == other.hasInfoServerPort());
            if (hasInfoServerPort()) {
                result = result && (getInfoServerPort() == other.getInfoServerPort());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasNumberOfRequests()) {
                hash = (37 * hash) + NUMBEROFREQUESTS_FIELD_NUMBER;
                hash = (53 * hash) + getNumberOfRequests();
            }
            if (hasTotalNumberOfRequests()) {
                hash = (37 * hash) + TOTALNUMBEROFREQUESTS_FIELD_NUMBER;
                hash = (53 * hash) + getTotalNumberOfRequests();
            }
            if (hasUsedHeapMB()) {
                hash = (37 * hash) + USEDHEAPMB_FIELD_NUMBER;
                hash = (53 * hash) + getUsedHeapMB();
            }
            if (hasMaxHeapMB()) {
                hash = (37 * hash) + MAXHEAPMB_FIELD_NUMBER;
                hash = (53 * hash) + getMaxHeapMB();
            }
            if (getRegionLoadsCount() > 0) {
                hash = (37 * hash) + REGIONLOADS_FIELD_NUMBER;
                hash = (53 * hash) + getRegionLoadsList().hashCode();
            }
            if (getCoprocessorsCount() > 0) {
                hash = (37 * hash) + COPROCESSORS_FIELD_NUMBER;
                hash = (53 * hash) + getCoprocessorsList().hashCode();
            }
            if (hasReportStartTime()) {
                hash = (37 * hash) + REPORTSTARTTIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getReportStartTime());
            }
            if (hasReportEndTime()) {
                hash = (37 * hash) + REPORTENDTIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getReportEndTime());
            }
            if (hasInfoServerPort()) {
                hash = (37 * hash) + INFOSERVERPORT_FIELD_NUMBER;
                hash = (53 * hash) + getInfoServerPort();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoadOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerLoad_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerLoad_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getRegionLoadsFieldBuilder();
                    getCoprocessorsFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                numberOfRequests_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                totalNumberOfRequests_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                usedHeapMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                maxHeapMB_ = 0;
                bitField0_ = (bitField0_ & ~0x00000008);
                if (regionLoadsBuilder_ == null) {
                    regionLoads_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000010);
                } else {
                    regionLoadsBuilder_.clear();
                }
                if (coprocessorsBuilder_ == null) {
                    coprocessors_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000020);
                } else {
                    coprocessorsBuilder_.clear();
                }
                reportStartTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000040);
                reportEndTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000080);
                infoServerPort_ = 0;
                bitField0_ = (bitField0_ & ~0x00000100);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.numberOfRequests_ = numberOfRequests_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.totalNumberOfRequests_ = totalNumberOfRequests_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.usedHeapMB_ = usedHeapMB_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.maxHeapMB_ = maxHeapMB_;
                if (regionLoadsBuilder_ == null) {
                    if (((bitField0_ & 0x00000010) == 0x00000010)) {
                        regionLoads_ = java.util.Collections.unmodifiableList(regionLoads_);
                        bitField0_ = (bitField0_ & ~0x00000010);
                    }
                    result.regionLoads_ = regionLoads_;
                } else {
                    result.regionLoads_ = regionLoadsBuilder_.build();
                }
                if (coprocessorsBuilder_ == null) {
                    if (((bitField0_ & 0x00000020) == 0x00000020)) {
                        coprocessors_ = java.util.Collections.unmodifiableList(coprocessors_);
                        bitField0_ = (bitField0_ & ~0x00000020);
                    }
                    result.coprocessors_ = coprocessors_;
                } else {
                    result.coprocessors_ = coprocessorsBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.reportStartTime_ = reportStartTime_;
                if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.reportEndTime_ = reportEndTime_;
                if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
                    to_bitField0_ |= 0x00000040;
                }
                result.infoServerPort_ = infoServerPort_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad.getDefaultInstance())
                    return this;
                if (other.hasNumberOfRequests()) {
                    setNumberOfRequests(other.getNumberOfRequests());
                }
                if (other.hasTotalNumberOfRequests()) {
                    setTotalNumberOfRequests(other.getTotalNumberOfRequests());
                }
                if (other.hasUsedHeapMB()) {
                    setUsedHeapMB(other.getUsedHeapMB());
                }
                if (other.hasMaxHeapMB()) {
                    setMaxHeapMB(other.getMaxHeapMB());
                }
                if (regionLoadsBuilder_ == null) {
                    if (!other.regionLoads_.isEmpty()) {
                        if (regionLoads_.isEmpty()) {
                            regionLoads_ = other.regionLoads_;
                            bitField0_ = (bitField0_ & ~0x00000010);
                        } else {
                            ensureRegionLoadsIsMutable();
                            regionLoads_.addAll(other.regionLoads_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.regionLoads_.isEmpty()) {
                        if (regionLoadsBuilder_.isEmpty()) {
                            regionLoadsBuilder_.dispose();
                            regionLoadsBuilder_ = null;
                            regionLoads_ = other.regionLoads_;
                            bitField0_ = (bitField0_ & ~0x00000010);
                            regionLoadsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getRegionLoadsFieldBuilder() : null;
                        } else {
                            regionLoadsBuilder_.addAllMessages(other.regionLoads_);
                        }
                    }
                }
                if (coprocessorsBuilder_ == null) {
                    if (!other.coprocessors_.isEmpty()) {
                        if (coprocessors_.isEmpty()) {
                            coprocessors_ = other.coprocessors_;
                            bitField0_ = (bitField0_ & ~0x00000020);
                        } else {
                            ensureCoprocessorsIsMutable();
                            coprocessors_.addAll(other.coprocessors_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.coprocessors_.isEmpty()) {
                        if (coprocessorsBuilder_.isEmpty()) {
                            coprocessorsBuilder_.dispose();
                            coprocessorsBuilder_ = null;
                            coprocessors_ = other.coprocessors_;
                            bitField0_ = (bitField0_ & ~0x00000020);
                            coprocessorsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getCoprocessorsFieldBuilder() : null;
                        } else {
                            coprocessorsBuilder_.addAllMessages(other.coprocessors_);
                        }
                    }
                }
                if (other.hasReportStartTime()) {
                    setReportStartTime(other.getReportStartTime());
                }
                if (other.hasReportEndTime()) {
                    setReportEndTime(other.getReportEndTime());
                }
                if (other.hasInfoServerPort()) {
                    setInfoServerPort(other.getInfoServerPort());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                for (int i = 0; i < getRegionLoadsCount(); i++) {
                    if (!getRegionLoads(i).isInitialized()) {
                        return false;
                    }
                }
                for (int i = 0; i < getCoprocessorsCount(); i++) {
                    if (!getCoprocessors(i).isInitialized()) {
                        return false;
                    }
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                bitField0_ |= 0x00000001;
                                numberOfRequests_ = input.readUInt32();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                totalNumberOfRequests_ = input.readUInt32();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                usedHeapMB_ = input.readUInt32();
                                break;
                            }
                        case 32:
                            {
                                bitField0_ |= 0x00000008;
                                maxHeapMB_ = input.readUInt32();
                                break;
                            }
                        case 42:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addRegionLoads(subBuilder.buildPartial());
                                break;
                            }
                        case 50:
                            {
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder subBuilder = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.newBuilder();
                                input.readMessage(subBuilder, extensionRegistry);
                                addCoprocessors(subBuilder.buildPartial());
                                break;
                            }
                        case 56:
                            {
                                bitField0_ |= 0x00000040;
                                reportStartTime_ = input.readUInt64();
                                break;
                            }
                        case 64:
                            {
                                bitField0_ |= 0x00000080;
                                reportEndTime_ = input.readUInt64();
                                break;
                            }
                        case 72:
                            {
                                bitField0_ |= 0x00000100;
                                infoServerPort_ = input.readUInt32();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private int numberOfRequests_;

            public boolean hasNumberOfRequests() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public int getNumberOfRequests() {
                return numberOfRequests_;
            }

            public Builder setNumberOfRequests(int value) {
                bitField0_ |= 0x00000001;
                numberOfRequests_ = value;
                onChanged();
                return this;
            }

            public Builder clearNumberOfRequests() {
                bitField0_ = (bitField0_ & ~0x00000001);
                numberOfRequests_ = 0;
                onChanged();
                return this;
            }

            private int totalNumberOfRequests_;

            public boolean hasTotalNumberOfRequests() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getTotalNumberOfRequests() {
                return totalNumberOfRequests_;
            }

            public Builder setTotalNumberOfRequests(int value) {
                bitField0_ |= 0x00000002;
                totalNumberOfRequests_ = value;
                onChanged();
                return this;
            }

            public Builder clearTotalNumberOfRequests() {
                bitField0_ = (bitField0_ & ~0x00000002);
                totalNumberOfRequests_ = 0;
                onChanged();
                return this;
            }

            private int usedHeapMB_;

            public boolean hasUsedHeapMB() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getUsedHeapMB() {
                return usedHeapMB_;
            }

            public Builder setUsedHeapMB(int value) {
                bitField0_ |= 0x00000004;
                usedHeapMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearUsedHeapMB() {
                bitField0_ = (bitField0_ & ~0x00000004);
                usedHeapMB_ = 0;
                onChanged();
                return this;
            }

            private int maxHeapMB_;

            public boolean hasMaxHeapMB() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public int getMaxHeapMB() {
                return maxHeapMB_;
            }

            public Builder setMaxHeapMB(int value) {
                bitField0_ |= 0x00000008;
                maxHeapMB_ = value;
                onChanged();
                return this;
            }

            public Builder clearMaxHeapMB() {
                bitField0_ = (bitField0_ & ~0x00000008);
                maxHeapMB_ = 0;
                onChanged();
                return this;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> regionLoads_ = java.util.Collections.emptyList();

            private void ensureRegionLoadsIsMutable() {
                if (!((bitField0_ & 0x00000010) == 0x00000010)) {
                    regionLoads_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad>(regionLoads_);
                    bitField0_ |= 0x00000010;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder> regionLoadsBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> getRegionLoadsList() {
                if (regionLoadsBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(regionLoads_);
                } else {
                    return regionLoadsBuilder_.getMessageList();
                }
            }

            public int getRegionLoadsCount() {
                if (regionLoadsBuilder_ == null) {
                    return regionLoads_.size();
                } else {
                    return regionLoadsBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad getRegionLoads(int index) {
                if (regionLoadsBuilder_ == null) {
                    return regionLoads_.get(index);
                } else {
                    return regionLoadsBuilder_.getMessage(index);
                }
            }

            public Builder setRegionLoads(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad value) {
                if (regionLoadsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureRegionLoadsIsMutable();
                    regionLoads_.set(index, value);
                    onChanged();
                } else {
                    regionLoadsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setRegionLoads(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder builderForValue) {
                if (regionLoadsBuilder_ == null) {
                    ensureRegionLoadsIsMutable();
                    regionLoads_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    regionLoadsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addRegionLoads(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad value) {
                if (regionLoadsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureRegionLoadsIsMutable();
                    regionLoads_.add(value);
                    onChanged();
                } else {
                    regionLoadsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addRegionLoads(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad value) {
                if (regionLoadsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureRegionLoadsIsMutable();
                    regionLoads_.add(index, value);
                    onChanged();
                } else {
                    regionLoadsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addRegionLoads(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder builderForValue) {
                if (regionLoadsBuilder_ == null) {
                    ensureRegionLoadsIsMutable();
                    regionLoads_.add(builderForValue.build());
                    onChanged();
                } else {
                    regionLoadsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addRegionLoads(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder builderForValue) {
                if (regionLoadsBuilder_ == null) {
                    ensureRegionLoadsIsMutable();
                    regionLoads_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    regionLoadsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllRegionLoads(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad> values) {
                if (regionLoadsBuilder_ == null) {
                    ensureRegionLoadsIsMutable();
                    super.addAll(values, regionLoads_);
                    onChanged();
                } else {
                    regionLoadsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearRegionLoads() {
                if (regionLoadsBuilder_ == null) {
                    regionLoads_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000010);
                    onChanged();
                } else {
                    regionLoadsBuilder_.clear();
                }
                return this;
            }

            public Builder removeRegionLoads(int index) {
                if (regionLoadsBuilder_ == null) {
                    ensureRegionLoadsIsMutable();
                    regionLoads_.remove(index);
                    onChanged();
                } else {
                    regionLoadsBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder getRegionLoadsBuilder(int index) {
                return getRegionLoadsFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder getRegionLoadsOrBuilder(int index) {
                if (regionLoadsBuilder_ == null) {
                    return regionLoads_.get(index);
                } else {
                    return regionLoadsBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder> getRegionLoadsOrBuilderList() {
                if (regionLoadsBuilder_ != null) {
                    return regionLoadsBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(regionLoads_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder addRegionLoadsBuilder() {
                return getRegionLoadsFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder addRegionLoadsBuilder(int index) {
                return getRegionLoadsFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder> getRegionLoadsBuilderList() {
                return getRegionLoadsFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder> getRegionLoadsFieldBuilder() {
                if (regionLoadsBuilder_ == null) {
                    regionLoadsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoadOrBuilder>(regionLoads_, ((bitField0_ & 0x00000010) == 0x00000010), getParentForChildren(), isClean());
                    regionLoads_ = null;
                }
                return regionLoadsBuilder_;
            }

            private java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> coprocessors_ = java.util.Collections.emptyList();

            private void ensureCoprocessorsIsMutable() {
                if (!((bitField0_ & 0x00000020) == 0x00000020)) {
                    coprocessors_ = new java.util.ArrayList<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor>(coprocessors_);
                    bitField0_ |= 0x00000020;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder> coprocessorsBuilder_;

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> getCoprocessorsList() {
                if (coprocessorsBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(coprocessors_);
                } else {
                    return coprocessorsBuilder_.getMessageList();
                }
            }

            public int getCoprocessorsCount() {
                if (coprocessorsBuilder_ == null) {
                    return coprocessors_.size();
                } else {
                    return coprocessorsBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor getCoprocessors(int index) {
                if (coprocessorsBuilder_ == null) {
                    return coprocessors_.get(index);
                } else {
                    return coprocessorsBuilder_.getMessage(index);
                }
            }

            public Builder setCoprocessors(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor value) {
                if (coprocessorsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureCoprocessorsIsMutable();
                    coprocessors_.set(index, value);
                    onChanged();
                } else {
                    coprocessorsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setCoprocessors(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder builderForValue) {
                if (coprocessorsBuilder_ == null) {
                    ensureCoprocessorsIsMutable();
                    coprocessors_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    coprocessorsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addCoprocessors(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor value) {
                if (coprocessorsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureCoprocessorsIsMutable();
                    coprocessors_.add(value);
                    onChanged();
                } else {
                    coprocessorsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addCoprocessors(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor value) {
                if (coprocessorsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureCoprocessorsIsMutable();
                    coprocessors_.add(index, value);
                    onChanged();
                } else {
                    coprocessorsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addCoprocessors(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder builderForValue) {
                if (coprocessorsBuilder_ == null) {
                    ensureCoprocessorsIsMutable();
                    coprocessors_.add(builderForValue.build());
                    onChanged();
                } else {
                    coprocessorsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addCoprocessors(int index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder builderForValue) {
                if (coprocessorsBuilder_ == null) {
                    ensureCoprocessorsIsMutable();
                    coprocessors_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    coprocessorsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllCoprocessors(java.lang.Iterable<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor> values) {
                if (coprocessorsBuilder_ == null) {
                    ensureCoprocessorsIsMutable();
                    super.addAll(values, coprocessors_);
                    onChanged();
                } else {
                    coprocessorsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearCoprocessors() {
                if (coprocessorsBuilder_ == null) {
                    coprocessors_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000020);
                    onChanged();
                } else {
                    coprocessorsBuilder_.clear();
                }
                return this;
            }

            public Builder removeCoprocessors(int index) {
                if (coprocessorsBuilder_ == null) {
                    ensureCoprocessorsIsMutable();
                    coprocessors_.remove(index);
                    onChanged();
                } else {
                    coprocessorsBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder getCoprocessorsBuilder(int index) {
                return getCoprocessorsFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder getCoprocessorsOrBuilder(int index) {
                if (coprocessorsBuilder_ == null) {
                    return coprocessors_.get(index);
                } else {
                    return coprocessorsBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder> getCoprocessorsOrBuilderList() {
                if (coprocessorsBuilder_ != null) {
                    return coprocessorsBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(coprocessors_);
                }
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder addCoprocessorsBuilder() {
                return getCoprocessorsFieldBuilder().addBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.getDefaultInstance());
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder addCoprocessorsBuilder(int index) {
                return getCoprocessorsFieldBuilder().addBuilder(index, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder> getCoprocessorsBuilderList() {
                return getCoprocessorsFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder> getCoprocessorsFieldBuilder() {
                if (coprocessorsBuilder_ == null) {
                    coprocessorsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder>(coprocessors_, ((bitField0_ & 0x00000020) == 0x00000020), getParentForChildren(), isClean());
                    coprocessors_ = null;
                }
                return coprocessorsBuilder_;
            }

            private long reportStartTime_;

            public boolean hasReportStartTime() {
                return ((bitField0_ & 0x00000040) == 0x00000040);
            }

            public long getReportStartTime() {
                return reportStartTime_;
            }

            public Builder setReportStartTime(long value) {
                bitField0_ |= 0x00000040;
                reportStartTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearReportStartTime() {
                bitField0_ = (bitField0_ & ~0x00000040);
                reportStartTime_ = 0L;
                onChanged();
                return this;
            }

            private long reportEndTime_;

            public boolean hasReportEndTime() {
                return ((bitField0_ & 0x00000080) == 0x00000080);
            }

            public long getReportEndTime() {
                return reportEndTime_;
            }

            public Builder setReportEndTime(long value) {
                bitField0_ |= 0x00000080;
                reportEndTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearReportEndTime() {
                bitField0_ = (bitField0_ & ~0x00000080);
                reportEndTime_ = 0L;
                onChanged();
                return this;
            }

            private int infoServerPort_;

            public boolean hasInfoServerPort() {
                return ((bitField0_ & 0x00000100) == 0x00000100);
            }

            public int getInfoServerPort() {
                return infoServerPort_;
            }

            public Builder setInfoServerPort(int value) {
                bitField0_ |= 0x00000100;
                infoServerPort_ = value;
                onChanged();
                return this;
            }

            public Builder clearInfoServerPort() {
                bitField0_ = (bitField0_ & ~0x00000100);
                infoServerPort_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new ServerLoad(true);
            defaultInstance.initFields();
        }
    }

    public interface TimeRangeOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasFrom();

        long getFrom();

        boolean hasTo();

        long getTo();
    }

    public static final class TimeRange extends com.google.protobuf.GeneratedMessage implements TimeRangeOrBuilder {

        private TimeRange(Builder builder) {
            super(builder);
        }

        private TimeRange(boolean noInit) {
        }

        private static final TimeRange defaultInstance;

        public static TimeRange getDefaultInstance() {
            return defaultInstance;
        }

        public TimeRange getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TimeRange_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TimeRange_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int FROM_FIELD_NUMBER = 1;

        private long from_;

        public boolean hasFrom() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public long getFrom() {
            return from_;
        }

        public static final int TO_FIELD_NUMBER = 2;

        private long to_;

        public boolean hasTo() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public long getTo() {
            return to_;
        }

        private void initFields() {
            from_ = 0L;
            to_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeUInt64(1, from_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeUInt64(2, to_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(1, from_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(2, to_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange) obj;
            boolean result = true;
            result = result && (hasFrom() == other.hasFrom());
            if (hasFrom()) {
                result = result && (getFrom() == other.getFrom());
            }
            result = result && (hasTo() == other.hasTo());
            if (hasTo()) {
                result = result && (getTo() == other.getTo());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasFrom()) {
                hash = (37 * hash) + FROM_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getFrom());
            }
            if (hasTo()) {
                hash = (37 * hash) + TO_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getTo());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRangeOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TimeRange_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_TimeRange_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                from_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                to_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.from_ = from_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.to_ = to_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange.getDefaultInstance())
                    return this;
                if (other.hasFrom()) {
                    setFrom(other.getFrom());
                }
                if (other.hasTo()) {
                    setTo(other.getTo());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                bitField0_ |= 0x00000001;
                                from_ = input.readUInt64();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                to_ = input.readUInt64();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private long from_;

            public boolean hasFrom() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public long getFrom() {
                return from_;
            }

            public Builder setFrom(long value) {
                bitField0_ |= 0x00000001;
                from_ = value;
                onChanged();
                return this;
            }

            public Builder clearFrom() {
                bitField0_ = (bitField0_ & ~0x00000001);
                from_ = 0L;
                onChanged();
                return this;
            }

            private long to_;

            public boolean hasTo() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public long getTo() {
                return to_;
            }

            public Builder setTo(long value) {
                bitField0_ |= 0x00000002;
                to_ = value;
                onChanged();
                return this;
            }

            public Builder clearTo() {
                bitField0_ = (bitField0_ & ~0x00000002);
                to_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new TimeRange(true);
            defaultInstance.initFields();
        }
    }

    public interface FilterOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();

        boolean hasSerializedFilter();

        com.google.protobuf.ByteString getSerializedFilter();
    }

    public static final class Filter extends com.google.protobuf.GeneratedMessage implements FilterOrBuilder {

        private Filter(Builder builder) {
            super(builder);
        }

        private Filter(boolean noInit) {
        }

        private static final Filter defaultInstance;

        public static Filter getDefaultInstance() {
            return defaultInstance;
        }

        public Filter getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Filter_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Filter_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int SERIALIZEDFILTER_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString serializedFilter_;

        public boolean hasSerializedFilter() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getSerializedFilter() {
            return serializedFilter_;
        }

        private void initFields() {
            name_ = "";
            serializedFilter_ = com.google.protobuf.ByteString.EMPTY;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, serializedFilter_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, serializedFilter_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && (hasSerializedFilter() == other.hasSerializedFilter());
            if (hasSerializedFilter()) {
                result = result && getSerializedFilter().equals(other.getSerializedFilter());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (hasSerializedFilter()) {
                hash = (37 * hash) + SERIALIZEDFILTER_FIELD_NUMBER;
                hash = (53 * hash) + getSerializedFilter().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.FilterOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Filter_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Filter_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                serializedFilter_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.serializedFilter_ = serializedFilter_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasSerializedFilter()) {
                    setSerializedFilter(other.getSerializedFilter());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                serializedFilter_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }

            private com.google.protobuf.ByteString serializedFilter_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasSerializedFilter() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getSerializedFilter() {
                return serializedFilter_;
            }

            public Builder setSerializedFilter(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                serializedFilter_ = value;
                onChanged();
                return this;
            }

            public Builder clearSerializedFilter() {
                bitField0_ = (bitField0_ & ~0x00000002);
                serializedFilter_ = getDefaultInstance().getSerializedFilter();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new Filter(true);
            defaultInstance.initFields();
        }
    }

    public interface KeyValueOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasRow();

        com.google.protobuf.ByteString getRow();

        boolean hasFamily();

        com.google.protobuf.ByteString getFamily();

        boolean hasQualifier();

        com.google.protobuf.ByteString getQualifier();

        boolean hasTimestamp();

        long getTimestamp();

        boolean hasKeyType();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType getKeyType();

        boolean hasValue();

        com.google.protobuf.ByteString getValue();
    }

    public static final class KeyValue extends com.google.protobuf.GeneratedMessage implements KeyValueOrBuilder {

        private KeyValue(Builder builder) {
            super(builder);
        }

        private KeyValue(boolean noInit) {
        }

        private static final KeyValue defaultInstance;

        public static KeyValue getDefaultInstance() {
            return defaultInstance;
        }

        public KeyValue getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_KeyValue_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_KeyValue_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int ROW_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString row_;

        public boolean hasRow() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getRow() {
            return row_;
        }

        public static final int FAMILY_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString family_;

        public boolean hasFamily() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getFamily() {
            return family_;
        }

        public static final int QUALIFIER_FIELD_NUMBER = 3;

        private com.google.protobuf.ByteString qualifier_;

        public boolean hasQualifier() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public com.google.protobuf.ByteString getQualifier() {
            return qualifier_;
        }

        public static final int TIMESTAMP_FIELD_NUMBER = 4;

        private long timestamp_;

        public boolean hasTimestamp() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public long getTimestamp() {
            return timestamp_;
        }

        public static final int KEYTYPE_FIELD_NUMBER = 5;

        private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType keyType_;

        public boolean hasKeyType() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType getKeyType() {
            return keyType_;
        }

        public static final int VALUE_FIELD_NUMBER = 6;

        private com.google.protobuf.ByteString value_;

        public boolean hasValue() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public com.google.protobuf.ByteString getValue() {
            return value_;
        }

        private void initFields() {
            row_ = com.google.protobuf.ByteString.EMPTY;
            family_ = com.google.protobuf.ByteString.EMPTY;
            qualifier_ = com.google.protobuf.ByteString.EMPTY;
            timestamp_ = 0L;
            keyType_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType.MINIMUM;
            value_ = com.google.protobuf.ByteString.EMPTY;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasRow()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasFamily()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasQualifier()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, row_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, family_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeBytes(3, qualifier_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeUInt64(4, timestamp_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeEnum(5, keyType_.getNumber());
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeBytes(6, value_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, row_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, family_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, qualifier_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(4, timestamp_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(5, keyType_.getNumber());
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(6, value_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue) obj;
            boolean result = true;
            result = result && (hasRow() == other.hasRow());
            if (hasRow()) {
                result = result && getRow().equals(other.getRow());
            }
            result = result && (hasFamily() == other.hasFamily());
            if (hasFamily()) {
                result = result && getFamily().equals(other.getFamily());
            }
            result = result && (hasQualifier() == other.hasQualifier());
            if (hasQualifier()) {
                result = result && getQualifier().equals(other.getQualifier());
            }
            result = result && (hasTimestamp() == other.hasTimestamp());
            if (hasTimestamp()) {
                result = result && (getTimestamp() == other.getTimestamp());
            }
            result = result && (hasKeyType() == other.hasKeyType());
            if (hasKeyType()) {
                result = result && (getKeyType() == other.getKeyType());
            }
            result = result && (hasValue() == other.hasValue());
            if (hasValue()) {
                result = result && getValue().equals(other.getValue());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasRow()) {
                hash = (37 * hash) + ROW_FIELD_NUMBER;
                hash = (53 * hash) + getRow().hashCode();
            }
            if (hasFamily()) {
                hash = (37 * hash) + FAMILY_FIELD_NUMBER;
                hash = (53 * hash) + getFamily().hashCode();
            }
            if (hasQualifier()) {
                hash = (37 * hash) + QUALIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getQualifier().hashCode();
            }
            if (hasTimestamp()) {
                hash = (37 * hash) + TIMESTAMP_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getTimestamp());
            }
            if (hasKeyType()) {
                hash = (37 * hash) + KEYTYPE_FIELD_NUMBER;
                hash = (53 * hash) + hashEnum(getKeyType());
            }
            if (hasValue()) {
                hash = (37 * hash) + VALUE_FIELD_NUMBER;
                hash = (53 * hash) + getValue().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValueOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_KeyValue_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_KeyValue_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                row_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                family_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                qualifier_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000004);
                timestamp_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000008);
                keyType_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType.MINIMUM;
                bitField0_ = (bitField0_ & ~0x00000010);
                value_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000020);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.row_ = row_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.family_ = family_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.qualifier_ = qualifier_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.timestamp_ = timestamp_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.keyType_ = keyType_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.value_ = value_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue.getDefaultInstance())
                    return this;
                if (other.hasRow()) {
                    setRow(other.getRow());
                }
                if (other.hasFamily()) {
                    setFamily(other.getFamily());
                }
                if (other.hasQualifier()) {
                    setQualifier(other.getQualifier());
                }
                if (other.hasTimestamp()) {
                    setTimestamp(other.getTimestamp());
                }
                if (other.hasKeyType()) {
                    setKeyType(other.getKeyType());
                }
                if (other.hasValue()) {
                    setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasRow()) {
                    return false;
                }
                if (!hasFamily()) {
                    return false;
                }
                if (!hasQualifier()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                row_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                family_ = input.readBytes();
                                break;
                            }
                        case 26:
                            {
                                bitField0_ |= 0x00000004;
                                qualifier_ = input.readBytes();
                                break;
                            }
                        case 32:
                            {
                                bitField0_ |= 0x00000008;
                                timestamp_ = input.readUInt64();
                                break;
                            }
                        case 40:
                            {
                                int rawValue = input.readEnum();
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType value = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(5, rawValue);
                                } else {
                                    bitField0_ |= 0x00000010;
                                    keyType_ = value;
                                }
                                break;
                            }
                        case 50:
                            {
                                bitField0_ |= 0x00000020;
                                value_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private com.google.protobuf.ByteString row_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasRow() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getRow() {
                return row_;
            }

            public Builder setRow(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                row_ = value;
                onChanged();
                return this;
            }

            public Builder clearRow() {
                bitField0_ = (bitField0_ & ~0x00000001);
                row_ = getDefaultInstance().getRow();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString family_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasFamily() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getFamily() {
                return family_;
            }

            public Builder setFamily(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                family_ = value;
                onChanged();
                return this;
            }

            public Builder clearFamily() {
                bitField0_ = (bitField0_ & ~0x00000002);
                family_ = getDefaultInstance().getFamily();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString qualifier_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasQualifier() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public com.google.protobuf.ByteString getQualifier() {
                return qualifier_;
            }

            public Builder setQualifier(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                qualifier_ = value;
                onChanged();
                return this;
            }

            public Builder clearQualifier() {
                bitField0_ = (bitField0_ & ~0x00000004);
                qualifier_ = getDefaultInstance().getQualifier();
                onChanged();
                return this;
            }

            private long timestamp_;

            public boolean hasTimestamp() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public long getTimestamp() {
                return timestamp_;
            }

            public Builder setTimestamp(long value) {
                bitField0_ |= 0x00000008;
                timestamp_ = value;
                onChanged();
                return this;
            }

            public Builder clearTimestamp() {
                bitField0_ = (bitField0_ & ~0x00000008);
                timestamp_ = 0L;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType keyType_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType.MINIMUM;

            public boolean hasKeyType() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType getKeyType() {
                return keyType_;
            }

            public Builder setKeyType(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000010;
                keyType_ = value;
                onChanged();
                return this;
            }

            public Builder clearKeyType() {
                bitField0_ = (bitField0_ & ~0x00000010);
                keyType_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyType.MINIMUM;
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasValue() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }

            public com.google.protobuf.ByteString getValue() {
                return value_;
            }

            public Builder setValue(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000020;
                value_ = value;
                onChanged();
                return this;
            }

            public Builder clearValue() {
                bitField0_ = (bitField0_ & ~0x00000020);
                value_ = getDefaultInstance().getValue();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new KeyValue(true);
            defaultInstance.initFields();
        }
    }

    public interface ServerNameOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasHostName();

        String getHostName();

        boolean hasPort();

        int getPort();

        boolean hasStartCode();

        long getStartCode();
    }

    public static final class ServerName extends com.google.protobuf.GeneratedMessage implements ServerNameOrBuilder {

        private ServerName(Builder builder) {
            super(builder);
        }

        private ServerName(boolean noInit) {
        }

        private static final ServerName defaultInstance;

        public static ServerName getDefaultInstance() {
            return defaultInstance;
        }

        public ServerName getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerName_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerName_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int HOSTNAME_FIELD_NUMBER = 1;

        private java.lang.Object hostName_;

        public boolean hasHostName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getHostName() {
            java.lang.Object ref = hostName_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    hostName_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getHostNameBytes() {
            java.lang.Object ref = hostName_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                hostName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int PORT_FIELD_NUMBER = 2;

        private int port_;

        public boolean hasPort() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getPort() {
            return port_;
        }

        public static final int STARTCODE_FIELD_NUMBER = 3;

        private long startCode_;

        public boolean hasStartCode() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public long getStartCode() {
            return startCode_;
        }

        private void initFields() {
            hostName_ = "";
            port_ = 0;
            startCode_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasHostName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getHostNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeUInt32(2, port_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeUInt64(3, startCode_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getHostNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, port_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeUInt64Size(3, startCode_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName) obj;
            boolean result = true;
            result = result && (hasHostName() == other.hasHostName());
            if (hasHostName()) {
                result = result && getHostName().equals(other.getHostName());
            }
            result = result && (hasPort() == other.hasPort());
            if (hasPort()) {
                result = result && (getPort() == other.getPort());
            }
            result = result && (hasStartCode() == other.hasStartCode());
            if (hasStartCode()) {
                result = result && (getStartCode() == other.getStartCode());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasHostName()) {
                hash = (37 * hash) + HOSTNAME_FIELD_NUMBER;
                hash = (53 * hash) + getHostName().hashCode();
            }
            if (hasPort()) {
                hash = (37 * hash) + PORT_FIELD_NUMBER;
                hash = (53 * hash) + getPort();
            }
            if (hasStartCode()) {
                hash = (37 * hash) + STARTCODE_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getStartCode());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerNameOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerName_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_ServerName_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                hostName_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                port_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                startCode_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.hostName_ = hostName_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.port_ = port_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.startCode_ = startCode_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName.getDefaultInstance())
                    return this;
                if (other.hasHostName()) {
                    setHostName(other.getHostName());
                }
                if (other.hasPort()) {
                    setPort(other.getPort());
                }
                if (other.hasStartCode()) {
                    setStartCode(other.getStartCode());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasHostName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                hostName_ = input.readBytes();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                port_ = input.readUInt32();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                startCode_ = input.readUInt64();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object hostName_ = "";

            public boolean hasHostName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getHostName() {
                java.lang.Object ref = hostName_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    hostName_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setHostName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                hostName_ = value;
                onChanged();
                return this;
            }

            public Builder clearHostName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                hostName_ = getDefaultInstance().getHostName();
                onChanged();
                return this;
            }

            void setHostName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                hostName_ = value;
                onChanged();
            }

            private int port_;

            public boolean hasPort() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getPort() {
                return port_;
            }

            public Builder setPort(int value) {
                bitField0_ |= 0x00000002;
                port_ = value;
                onChanged();
                return this;
            }

            public Builder clearPort() {
                bitField0_ = (bitField0_ & ~0x00000002);
                port_ = 0;
                onChanged();
                return this;
            }

            private long startCode_;

            public boolean hasStartCode() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public long getStartCode() {
                return startCode_;
            }

            public Builder setStartCode(long value) {
                bitField0_ |= 0x00000004;
                startCode_ = value;
                onChanged();
                return this;
            }

            public Builder clearStartCode() {
                bitField0_ = (bitField0_ & ~0x00000004);
                startCode_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new ServerName(true);
            defaultInstance.initFields();
        }
    }

    public interface CoprocessorOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();
    }

    public static final class Coprocessor extends com.google.protobuf.GeneratedMessage implements CoprocessorOrBuilder {

        private Coprocessor(Builder builder) {
            super(builder);
        }

        private Coprocessor(boolean noInit) {
        }

        private static final Coprocessor defaultInstance;

        public static Coprocessor getDefaultInstance() {
            return defaultInstance;
        }

        public Coprocessor getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Coprocessor_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Coprocessor_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            name_ = "";
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.CoprocessorOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Coprocessor_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_Coprocessor_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }
        }

        static {
            defaultInstance = new Coprocessor(true);
            defaultInstance.initFields();
        }
    }

    public interface NameStringPairOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();

        boolean hasValue();

        String getValue();
    }

    public static final class NameStringPair extends com.google.protobuf.GeneratedMessage implements NameStringPairOrBuilder {

        private NameStringPair(Builder builder) {
            super(builder);
        }

        private NameStringPair(boolean noInit) {
        }

        private static final NameStringPair defaultInstance;

        public static NameStringPair getDefaultInstance() {
            return defaultInstance;
        }

        public NameStringPair getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameStringPair_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameStringPair_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int VALUE_FIELD_NUMBER = 2;

        private java.lang.Object value_;

        public boolean hasValue() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public String getValue() {
            java.lang.Object ref = value_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    value_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getValueBytes() {
            java.lang.Object ref = value_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                value_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            name_ = "";
            value_ = "";
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasValue()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getValueBytes());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getValueBytes());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && (hasValue() == other.hasValue());
            if (hasValue()) {
                result = result && getValue().equals(other.getValue());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (hasValue()) {
                hash = (37 * hash) + VALUE_FIELD_NUMBER;
                hash = (53 * hash) + getValue().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPairOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameStringPair_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameStringPair_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                value_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.value_ = value_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasValue()) {
                    setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                if (!hasValue()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                value_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }

            private java.lang.Object value_ = "";

            public boolean hasValue() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public String getValue() {
                java.lang.Object ref = value_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    value_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setValue(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                value_ = value;
                onChanged();
                return this;
            }

            public Builder clearValue() {
                bitField0_ = (bitField0_ & ~0x00000002);
                value_ = getDefaultInstance().getValue();
                onChanged();
                return this;
            }

            void setValue(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000002;
                value_ = value;
                onChanged();
            }
        }

        static {
            defaultInstance = new NameStringPair(true);
            defaultInstance.initFields();
        }
    }

    public interface NameBytesPairOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();

        boolean hasValue();

        com.google.protobuf.ByteString getValue();
    }

    public static final class NameBytesPair extends com.google.protobuf.GeneratedMessage implements NameBytesPairOrBuilder {

        private NameBytesPair(Builder builder) {
            super(builder);
        }

        private NameBytesPair(boolean noInit) {
        }

        private static final NameBytesPair defaultInstance;

        public static NameBytesPair getDefaultInstance() {
            return defaultInstance;
        }

        public NameBytesPair getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameBytesPair_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameBytesPair_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int VALUE_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString value_;

        public boolean hasValue() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getValue() {
            return value_;
        }

        private void initFields() {
            name_ = "";
            value_ = com.google.protobuf.ByteString.EMPTY;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, value_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, value_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && (hasValue() == other.hasValue());
            if (hasValue()) {
                result = result && getValue().equals(other.getValue());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (hasValue()) {
                hash = (37 * hash) + VALUE_FIELD_NUMBER;
                hash = (53 * hash) + getValue().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPairOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameBytesPair_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameBytesPair_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                value_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.value_ = value_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasValue()) {
                    setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                value_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }

            private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasValue() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getValue() {
                return value_;
            }

            public Builder setValue(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                value_ = value;
                onChanged();
                return this;
            }

            public Builder clearValue() {
                bitField0_ = (bitField0_ & ~0x00000002);
                value_ = getDefaultInstance().getValue();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new NameBytesPair(true);
            defaultInstance.initFields();
        }
    }

    public interface BytesBytesPairOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasFirst();

        com.google.protobuf.ByteString getFirst();

        boolean hasSecond();

        com.google.protobuf.ByteString getSecond();
    }

    public static final class BytesBytesPair extends com.google.protobuf.GeneratedMessage implements BytesBytesPairOrBuilder {

        private BytesBytesPair(Builder builder) {
            super(builder);
        }

        private BytesBytesPair(boolean noInit) {
        }

        private static final BytesBytesPair defaultInstance;

        public static BytesBytesPair getDefaultInstance() {
            return defaultInstance;
        }

        public BytesBytesPair getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_BytesBytesPair_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_BytesBytesPair_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int FIRST_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString first_;

        public boolean hasFirst() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getFirst() {
            return first_;
        }

        public static final int SECOND_FIELD_NUMBER = 2;

        private com.google.protobuf.ByteString second_;

        public boolean hasSecond() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public com.google.protobuf.ByteString getSecond() {
            return second_;
        }

        private void initFields() {
            first_ = com.google.protobuf.ByteString.EMPTY;
            second_ = com.google.protobuf.ByteString.EMPTY;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasFirst()) {
                memoizedIsInitialized = 0;
                return false;
            }
            if (!hasSecond()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, first_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, second_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, first_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, second_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair) obj;
            boolean result = true;
            result = result && (hasFirst() == other.hasFirst());
            if (hasFirst()) {
                result = result && getFirst().equals(other.getFirst());
            }
            result = result && (hasSecond() == other.hasSecond());
            if (hasSecond()) {
                result = result && getSecond().equals(other.getSecond());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasFirst()) {
                hash = (37 * hash) + FIRST_FIELD_NUMBER;
                hash = (53 * hash) + getFirst().hashCode();
            }
            if (hasSecond()) {
                hash = (37 * hash) + SECOND_FIELD_NUMBER;
                hash = (53 * hash) + getSecond().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPairOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_BytesBytesPair_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_BytesBytesPair_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                first_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                second_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.first_ = first_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.second_ = second_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.getDefaultInstance())
                    return this;
                if (other.hasFirst()) {
                    setFirst(other.getFirst());
                }
                if (other.hasSecond()) {
                    setSecond(other.getSecond());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasFirst()) {
                    return false;
                }
                if (!hasSecond()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                first_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                second_ = input.readBytes();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private com.google.protobuf.ByteString first_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasFirst() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getFirst() {
                return first_;
            }

            public Builder setFirst(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                first_ = value;
                onChanged();
                return this;
            }

            public Builder clearFirst() {
                bitField0_ = (bitField0_ & ~0x00000001);
                first_ = getDefaultInstance().getFirst();
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString second_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasSecond() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public com.google.protobuf.ByteString getSecond() {
                return second_;
            }

            public Builder setSecond(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                second_ = value;
                onChanged();
                return this;
            }

            public Builder clearSecond() {
                bitField0_ = (bitField0_ & ~0x00000002);
                second_ = getDefaultInstance().getSecond();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new BytesBytesPair(true);
            defaultInstance.initFields();
        }
    }

    public interface NameInt64PairOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();

        boolean hasValue();

        long getValue();
    }

    public static final class NameInt64Pair extends com.google.protobuf.GeneratedMessage implements NameInt64PairOrBuilder {

        private NameInt64Pair(Builder builder) {
            super(builder);
        }

        private NameInt64Pair(boolean noInit) {
        }

        private static final NameInt64Pair defaultInstance;

        public static NameInt64Pair getDefaultInstance() {
            return defaultInstance;
        }

        public NameInt64Pair getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameInt64Pair_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameInt64Pair_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int VALUE_FIELD_NUMBER = 2;

        private long value_;

        public boolean hasValue() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public long getValue() {
            return value_;
        }

        private void initFields() {
            name_ = "";
            value_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt64(2, value_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(2, value_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && (hasValue() == other.hasValue());
            if (hasValue()) {
                result = result && (getValue() == other.getValue());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (hasValue()) {
                hash = (37 * hash) + VALUE_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getValue());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64PairOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameInt64Pair_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_NameInt64Pair_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                value_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.value_ = value_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasValue()) {
                    setValue(other.getValue());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                value_ = input.readInt64();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }

            private long value_;

            public boolean hasValue() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public long getValue() {
                return value_;
            }

            public Builder setValue(long value) {
                bitField0_ |= 0x00000002;
                value_ = value;
                onChanged();
                return this;
            }

            public Builder clearValue() {
                bitField0_ = (bitField0_ & ~0x00000002);
                value_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new NameInt64Pair(true);
            defaultInstance.initFields();
        }
    }

    public interface SnapshotDescriptionOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasName();

        String getName();

        boolean hasTable();

        String getTable();

        boolean hasCreationTime();

        long getCreationTime();

        boolean hasType();

        org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type getType();

        boolean hasVersion();

        int getVersion();
    }

    public static final class SnapshotDescription extends com.google.protobuf.GeneratedMessage implements SnapshotDescriptionOrBuilder {

        private SnapshotDescription(Builder builder) {
            super(builder);
        }

        private SnapshotDescription(boolean noInit) {
        }

        private static final SnapshotDescription defaultInstance;

        public static SnapshotDescription getDefaultInstance() {
            return defaultInstance;
        }

        public SnapshotDescription getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_SnapshotDescription_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_SnapshotDescription_fieldAccessorTable;
        }

        public enum Type implements com.google.protobuf.ProtocolMessageEnum {

            DISABLED(0, 0), FLUSH(1, 1);

            public static final int DISABLED_VALUE = 0;

            public static final int FLUSH_VALUE = 1;

            public final int getNumber() {
                return value;
            }

            public static Type valueOf(int value) {
                switch(value) {
                    case 0:
                        return DISABLED;
                    case 1:
                        return FLUSH;
                    default:
                        return null;
                }
            }

            public static com.google.protobuf.Internal.EnumLiteMap<Type> internalGetValueMap() {
                return internalValueMap;
            }

            private static com.google.protobuf.Internal.EnumLiteMap<Type> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<Type>() {

                public Type findValueByNumber(int number) {
                    return Type.valueOf(number);
                }
            };

            public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
                return getDescriptor().getValues().get(index);
            }

            public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
                return getDescriptor();
            }

            public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.getDescriptor().getEnumTypes().get(0);
            }

            private static final Type[] VALUES = { DISABLED, FLUSH };

            public static Type valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
                if (desc.getType() != getDescriptor()) {
                    throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
                }
                return VALUES[desc.getIndex()];
            }

            private final int index;

            private final int value;

            private Type(int index, int value) {
                this.index = index;
                this.value = value;
            }
        }

        private int bitField0_;

        public static final int NAME_FIELD_NUMBER = 1;

        private java.lang.Object name_;

        public boolean hasName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public String getName() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    name_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getNameBytes() {
            java.lang.Object ref = name_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                name_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int TABLE_FIELD_NUMBER = 2;

        private java.lang.Object table_;

        public boolean hasTable() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public String getTable() {
            java.lang.Object ref = table_;
            if (ref instanceof String) {
                return (String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                if (com.google.protobuf.Internal.isValidUtf8(bs)) {
                    table_ = s;
                }
                return s;
            }
        }

        private com.google.protobuf.ByteString getTableBytes() {
            java.lang.Object ref = table_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((String) ref);
                table_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int CREATIONTIME_FIELD_NUMBER = 3;

        private long creationTime_;

        public boolean hasCreationTime() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public long getCreationTime() {
            return creationTime_;
        }

        public static final int TYPE_FIELD_NUMBER = 4;

        private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type type_;

        public boolean hasType() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type getType() {
            return type_;
        }

        public static final int VERSION_FIELD_NUMBER = 5;

        private int version_;

        public boolean hasVersion() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public int getVersion() {
            return version_;
        }

        private void initFields() {
            name_ = "";
            table_ = "";
            creationTime_ = 0L;
            type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type.FLUSH;
            version_ = 0;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasName()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeBytes(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getTableBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt64(3, creationTime_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeEnum(4, type_.getNumber());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeInt32(5, version_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getTableBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(3, creationTime_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(4, type_.getNumber());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(5, version_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription) obj;
            boolean result = true;
            result = result && (hasName() == other.hasName());
            if (hasName()) {
                result = result && getName().equals(other.getName());
            }
            result = result && (hasTable() == other.hasTable());
            if (hasTable()) {
                result = result && getTable().equals(other.getTable());
            }
            result = result && (hasCreationTime() == other.hasCreationTime());
            if (hasCreationTime()) {
                result = result && (getCreationTime() == other.getCreationTime());
            }
            result = result && (hasType() == other.hasType());
            if (hasType()) {
                result = result && (getType() == other.getType());
            }
            result = result && (hasVersion() == other.hasVersion());
            if (hasVersion()) {
                result = result && (getVersion() == other.getVersion());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasName()) {
                hash = (37 * hash) + NAME_FIELD_NUMBER;
                hash = (53 * hash) + getName().hashCode();
            }
            if (hasTable()) {
                hash = (37 * hash) + TABLE_FIELD_NUMBER;
                hash = (53 * hash) + getTable().hashCode();
            }
            if (hasCreationTime()) {
                hash = (37 * hash) + CREATIONTIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getCreationTime());
            }
            if (hasType()) {
                hash = (37 * hash) + TYPE_FIELD_NUMBER;
                hash = (53 * hash) + hashEnum(getType());
            }
            if (hasVersion()) {
                hash = (37 * hash) + VERSION_FIELD_NUMBER;
                hash = (53 * hash) + getVersion();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescriptionOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_SnapshotDescription_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_SnapshotDescription_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                name_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                table_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                creationTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000004);
                type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type.FLUSH;
                bitField0_ = (bitField0_ & ~0x00000008);
                version_ = 0;
                bitField0_ = (bitField0_ & ~0x00000010);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.name_ = name_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.table_ = table_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.creationTime_ = creationTime_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.type_ = type_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.version_ = version_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasTable()) {
                    setTable(other.getTable());
                }
                if (other.hasCreationTime()) {
                    setCreationTime(other.getCreationTime());
                }
                if (other.hasType()) {
                    setType(other.getType());
                }
                if (other.hasVersion()) {
                    setVersion(other.getVersion());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasName()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                name_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                table_ = input.readBytes();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                creationTime_ = input.readInt64();
                                break;
                            }
                        case 32:
                            {
                                int rawValue = input.readEnum();
                                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type value = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(4, rawValue);
                                } else {
                                    bitField0_ |= 0x00000008;
                                    type_ = value;
                                }
                                break;
                            }
                        case 40:
                            {
                                bitField0_ |= 0x00000010;
                                version_ = input.readInt32();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private java.lang.Object name_ = "";

            public boolean hasName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public String getName() {
                java.lang.Object ref = name_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    name_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setName(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
                return this;
            }

            public Builder clearName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                name_ = getDefaultInstance().getName();
                onChanged();
                return this;
            }

            void setName(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000001;
                name_ = value;
                onChanged();
            }

            private java.lang.Object table_ = "";

            public boolean hasTable() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public String getTable() {
                java.lang.Object ref = table_;
                if (!(ref instanceof String)) {
                    String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    table_ = s;
                    return s;
                } else {
                    return (String) ref;
                }
            }

            public Builder setTable(String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                table_ = value;
                onChanged();
                return this;
            }

            public Builder clearTable() {
                bitField0_ = (bitField0_ & ~0x00000002);
                table_ = getDefaultInstance().getTable();
                onChanged();
                return this;
            }

            void setTable(com.google.protobuf.ByteString value) {
                bitField0_ |= 0x00000002;
                table_ = value;
                onChanged();
            }

            private long creationTime_;

            public boolean hasCreationTime() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public long getCreationTime() {
                return creationTime_;
            }

            public Builder setCreationTime(long value) {
                bitField0_ |= 0x00000004;
                creationTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearCreationTime() {
                bitField0_ = (bitField0_ & ~0x00000004);
                creationTime_ = 0L;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type.FLUSH;

            public boolean hasType() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type getType() {
                return type_;
            }

            public Builder setType(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                type_ = value;
                onChanged();
                return this;
            }

            public Builder clearType() {
                bitField0_ = (bitField0_ & ~0x00000008);
                type_ = org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Type.FLUSH;
                onChanged();
                return this;
            }

            private int version_;

            public boolean hasVersion() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public int getVersion() {
                return version_;
            }

            public Builder setVersion(int value) {
                bitField0_ |= 0x00000010;
                version_ = value;
                onChanged();
                return this;
            }

            public Builder clearVersion() {
                bitField0_ = (bitField0_ & ~0x00000010);
                version_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new SnapshotDescription(true);
            defaultInstance.initFields();
        }
    }

    public interface EmptyMsgOrBuilder extends com.google.protobuf.MessageOrBuilder {
    }

    public static final class EmptyMsg extends com.google.protobuf.GeneratedMessage implements EmptyMsgOrBuilder {

        private EmptyMsg(Builder builder) {
            super(builder);
        }

        private EmptyMsg(boolean noInit) {
        }

        private static final EmptyMsg defaultInstance;

        public static EmptyMsg getDefaultInstance() {
            return defaultInstance;
        }

        public EmptyMsg getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_EmptyMsg_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_EmptyMsg_fieldAccessorTable;
        }

        private void initFields() {
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg) obj;
            boolean result = true;
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsgOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_EmptyMsg_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_EmptyMsg_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg(this);
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg.getDefaultInstance())
                    return this;
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                    }
                }
            }
        }

        static {
            defaultInstance = new EmptyMsg(true);
            defaultInstance.initFields();
        }
    }

    public interface LongMsgOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasLongMsg();

        long getLongMsg();
    }

    public static final class LongMsg extends com.google.protobuf.GeneratedMessage implements LongMsgOrBuilder {

        private LongMsg(Builder builder) {
            super(builder);
        }

        private LongMsg(boolean noInit) {
        }

        private static final LongMsg defaultInstance;

        public static LongMsg getDefaultInstance() {
            return defaultInstance;
        }

        public LongMsg getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_LongMsg_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_LongMsg_fieldAccessorTable;
        }

        private int bitField0_;

        public static final int LONGMSG_FIELD_NUMBER = 1;

        private long longMsg_;

        public boolean hasLongMsg() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public long getLongMsg() {
            return longMsg_;
        }

        private void initFields() {
            longMsg_ = 0L;
        }

        private byte memoizedIsInitialized = -1;

        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized != -1)
                return isInitialized == 1;
            if (!hasLongMsg()) {
                memoizedIsInitialized = 0;
                return false;
            }
            memoizedIsInitialized = 1;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            getSerializedSize();
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                output.writeInt64(1, longMsg_);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(1, longMsg_);
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        private static final long serialVersionUID = 0L;

        @java.lang.Override
        protected java.lang.Object writeReplace() throws java.io.ObjectStreamException {
            return super.writeReplace();
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg other = (org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg) obj;
            boolean result = true;
            result = result && (hasLongMsg() == other.hasLongMsg());
            if (hasLongMsg()) {
                result = result && (getLongMsg() == other.getLongMsg());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        @java.lang.Override
        public int hashCode() {
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasLongMsg()) {
                hash = (37 * hash) + LONGMSG_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getLongMsg());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            return hash;
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            Builder builder = newBuilder();
            if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
                return builder.buildParsed();
            } else {
                return null;
            }
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsgOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_LongMsg_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.internal_static_LongMsg_fieldAccessorTable;
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                longMsg_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg.getDescriptor();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg getDefaultInstanceForType() {
                return org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg.getDefaultInstance();
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg build() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            private org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return result;
            }

            public org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg buildPartial() {
                org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg result = new org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.longMsg_ = longMsg_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg) {
                    return mergeFrom((org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg other) {
                if (other == org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg.getDefaultInstance())
                    return this;
                if (other.hasLongMsg()) {
                    setLongMsg(other.getLongMsg());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                if (!hasLongMsg()) {
                    return false;
                }
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            onChanged();
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    onChanged();
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                bitField0_ |= 0x00000001;
                                longMsg_ = input.readInt64();
                                break;
                            }
                    }
                }
            }

            private int bitField0_;

            private long longMsg_;

            public boolean hasLongMsg() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public long getLongMsg() {
                return longMsg_;
            }

            public Builder setLongMsg(long value) {
                bitField0_ |= 0x00000001;
                longMsg_ = value;
                onChanged();
                return this;
            }

            public Builder clearLongMsg() {
                bitField0_ = (bitField0_ & ~0x00000001);
                longMsg_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new LongMsg(true);
            defaultInstance.initFields();
        }
    }

    private static com.google.protobuf.Descriptors.Descriptor internal_static_TableSchema_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_TableSchema_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_ColumnFamilySchema_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_ColumnFamilySchema_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_RegionInfo_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_RegionInfo_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_RegionSpecifier_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_RegionSpecifier_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_RegionLoad_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_RegionLoad_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_ServerLoad_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_ServerLoad_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_TimeRange_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_TimeRange_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_Filter_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_Filter_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_KeyValue_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_KeyValue_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_ServerName_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_ServerName_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_Coprocessor_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_Coprocessor_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_NameStringPair_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_NameStringPair_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_NameBytesPair_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_NameBytesPair_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_BytesBytesPair_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_BytesBytesPair_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_NameInt64Pair_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_NameInt64Pair_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_SnapshotDescription_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_SnapshotDescription_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_EmptyMsg_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_EmptyMsg_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_LongMsg_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_LongMsg_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = { "\n\013hbase.proto\"\225\001\n\013TableSchema\022\014\n\004name\030\001 " + "\001(\014\022#\n\nattributes\030\002 \003(\0132\017.BytesBytesPair" + "\022+\n\016columnFamilies\030\003 \003(\0132\023.ColumnFamilyS" + "chema\022&\n\rconfiguration\030\004 \003(\0132\017.NameStrin" + "gPair\"o\n\022ColumnFamilySchema\022\014\n\004name\030\001 \002(" + "\014\022#\n\nattributes\030\002 \003(\0132\017.BytesBytesPair\022&" + "\n\rconfiguration\030\003 \003(\0132\017.NameStringPair\"s" + "\n\nRegionInfo\022\020\n\010regionId\030\001 \002(\004\022\021\n\ttableN" + "ame\030\002 \002(\014\022\020\n\010startKey\030\003 \001(\014\022\016\n\006endKey\030\004 " + "\001(\014\022\017\n\007offline\030\005 \001(\010\022\r\n\005split\030\006 \001(\010\"\225\001\n\017", "RegionSpecifier\0222\n\004type\030\001 \002(\0162$.RegionSp" + "ecifier.RegionSpecifierType\022\r\n\005value\030\002 \002" + "(\014\"?\n\023RegionSpecifierType\022\017\n\013REGION_NAME" + "\020\001\022\027\n\023ENCODED_REGION_NAME\020\002\"\260\003\n\nRegionLo" + "ad\022)\n\017regionSpecifier\030\001 \002(\0132\020.RegionSpec" + "ifier\022\016\n\006stores\030\002 \001(\r\022\022\n\nstorefiles\030\003 \001(" + "\r\022\037\n\027storeUncompressedSizeMB\030\004 \001(\r\022\027\n\017st" + "orefileSizeMB\030\005 \001(\r\022\026\n\016memstoreSizeMB\030\006 " + "\001(\r\022\034\n\024storefileIndexSizeMB\030\007 \001(\r\022\031\n\021rea" + "dRequestsCount\030\010 \001(\004\022\032\n\022writeRequestsCou", "nt\030\t \001(\004\022\032\n\022totalCompactingKVs\030\n \001(\004\022\033\n\023" + "currentCompactedKVs\030\013 \001(\004\022\027\n\017rootIndexSi" + "zeKB\030\014 \001(\r\022\036\n\026totalStaticIndexSizeKB\030\r \001" + "(\r\022\036\n\026totalStaticBloomSizeKB\030\016 \001(\r\022\032\n\022co" + "mpleteSequenceId\030\017 \001(\004\"\372\001\n\nServerLoad\022\030\n" + "\020numberOfRequests\030\001 \001(\r\022\035\n\025totalNumberOf" + "Requests\030\002 \001(\r\022\022\n\nusedHeapMB\030\003 \001(\r\022\021\n\tma" + "xHeapMB\030\004 \001(\r\022 \n\013regionLoads\030\005 \003(\0132\013.Reg" + "ionLoad\022\"\n\014coprocessors\030\006 \003(\0132\014.Coproces" + "sor\022\027\n\017reportStartTime\030\007 \001(\004\022\025\n\rreportEn", "dTime\030\010 \001(\004\022\026\n\016infoServerPort\030\t \001(\r\"%\n\tT" + "imeRange\022\014\n\004from\030\001 \001(\004\022\n\n\002to\030\002 \001(\004\"0\n\006Fi" + "lter\022\014\n\004name\030\001 \002(\t\022\030\n\020serializedFilter\030\002" + " \001(\014\"w\n\010KeyValue\022\013\n\003row\030\001 \002(\014\022\016\n\006family\030" + "\002 \002(\014\022\021\n\tqualifier\030\003 \002(\014\022\021\n\ttimestamp\030\004 " + "\001(\004\022\031\n\007keyType\030\005 \001(\0162\010.KeyType\022\r\n\005value\030" + "\006 \001(\014\"?\n\nServerName\022\020\n\010hostName\030\001 \002(\t\022\014\n" + "\004port\030\002 \001(\r\022\021\n\tstartCode\030\003 \001(\004\"\033\n\013Coproc" + "essor\022\014\n\004name\030\001 \002(\t\"-\n\016NameStringPair\022\014\n" + "\004name\030\001 \002(\t\022\r\n\005value\030\002 \002(\t\",\n\rNameBytesP", "air\022\014\n\004name\030\001 \002(\t\022\r\n\005value\030\002 \001(\014\"/\n\016Byte" + "sBytesPair\022\r\n\005first\030\001 \002(\014\022\016\n\006second\030\002 \002(" + "\014\",\n\rNameInt64Pair\022\014\n\004name\030\001 \001(\t\022\r\n\005valu" + "e\030\002 \001(\003\"\255\001\n\023SnapshotDescription\022\014\n\004name\030" + "\001 \002(\t\022\r\n\005table\030\002 \001(\t\022\027\n\014creationTime\030\003 \001" + "(\003:\0010\022.\n\004type\030\004 \001(\0162\031.SnapshotDescriptio" + "n.Type:\005FLUSH\022\017\n\007version\030\005 \001(\005\"\037\n\004Type\022\014" + "\n\010DISABLED\020\000\022\t\n\005FLUSH\020\001\"\n\n\010EmptyMsg\"\032\n\007L" + "ongMsg\022\017\n\007longMsg\030\001 \002(\003*r\n\013CompareType\022\010" + "\n\004LESS\020\000\022\021\n\rLESS_OR_EQUAL\020\001\022\t\n\005EQUAL\020\002\022\r", "\n\tNOT_EQUAL\020\003\022\024\n\020GREATER_OR_EQUAL\020\004\022\013\n\007G" + "REATER\020\005\022\t\n\005NO_OP\020\006*_\n\007KeyType\022\013\n\007MINIMU" + "M\020\000\022\007\n\003PUT\020\004\022\n\n\006DELETE\020\010\022\021\n\rDELETE_COLUM" + "N\020\014\022\021\n\rDELETE_FAMILY\020\016\022\014\n\007MAXIMUM\020\377\001B>\n*" + "org.apache.hadoop.hbase.protobuf.generat" + "edB\013HBaseProtosH\001\240\001\001" };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {

            public com.google.protobuf.ExtensionRegistry assignDescriptors(com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                internal_static_TableSchema_descriptor = getDescriptor().getMessageTypes().get(0);
                internal_static_TableSchema_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_TableSchema_descriptor, new java.lang.String[] { "Name", "Attributes", "ColumnFamilies", "Configuration" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TableSchema.Builder.class);
                internal_static_ColumnFamilySchema_descriptor = getDescriptor().getMessageTypes().get(1);
                internal_static_ColumnFamilySchema_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_ColumnFamilySchema_descriptor, new java.lang.String[] { "Name", "Attributes", "Configuration" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ColumnFamilySchema.Builder.class);
                internal_static_RegionInfo_descriptor = getDescriptor().getMessageTypes().get(2);
                internal_static_RegionInfo_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_RegionInfo_descriptor, new java.lang.String[] { "RegionId", "TableName", "StartKey", "EndKey", "Offline", "Split" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionInfo.Builder.class);
                internal_static_RegionSpecifier_descriptor = getDescriptor().getMessageTypes().get(3);
                internal_static_RegionSpecifier_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_RegionSpecifier_descriptor, new java.lang.String[] { "Type", "Value" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionSpecifier.Builder.class);
                internal_static_RegionLoad_descriptor = getDescriptor().getMessageTypes().get(4);
                internal_static_RegionLoad_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_RegionLoad_descriptor, new java.lang.String[] { "RegionSpecifier", "Stores", "Storefiles", "StoreUncompressedSizeMB", "StorefileSizeMB", "MemstoreSizeMB", "StorefileIndexSizeMB", "ReadRequestsCount", "WriteRequestsCount", "TotalCompactingKVs", "CurrentCompactedKVs", "RootIndexSizeKB", "TotalStaticIndexSizeKB", "TotalStaticBloomSizeKB", "CompleteSequenceId" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.RegionLoad.Builder.class);
                internal_static_ServerLoad_descriptor = getDescriptor().getMessageTypes().get(5);
                internal_static_ServerLoad_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_ServerLoad_descriptor, new java.lang.String[] { "NumberOfRequests", "TotalNumberOfRequests", "UsedHeapMB", "MaxHeapMB", "RegionLoads", "Coprocessors", "ReportStartTime", "ReportEndTime", "InfoServerPort" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerLoad.Builder.class);
                internal_static_TimeRange_descriptor = getDescriptor().getMessageTypes().get(6);
                internal_static_TimeRange_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_TimeRange_descriptor, new java.lang.String[] { "From", "To" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.TimeRange.Builder.class);
                internal_static_Filter_descriptor = getDescriptor().getMessageTypes().get(7);
                internal_static_Filter_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_Filter_descriptor, new java.lang.String[] { "Name", "SerializedFilter" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Filter.Builder.class);
                internal_static_KeyValue_descriptor = getDescriptor().getMessageTypes().get(8);
                internal_static_KeyValue_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_KeyValue_descriptor, new java.lang.String[] { "Row", "Family", "Qualifier", "Timestamp", "KeyType", "Value" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.KeyValue.Builder.class);
                internal_static_ServerName_descriptor = getDescriptor().getMessageTypes().get(9);
                internal_static_ServerName_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_ServerName_descriptor, new java.lang.String[] { "HostName", "Port", "StartCode" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.ServerName.Builder.class);
                internal_static_Coprocessor_descriptor = getDescriptor().getMessageTypes().get(10);
                internal_static_Coprocessor_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_Coprocessor_descriptor, new java.lang.String[] { "Name" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.Coprocessor.Builder.class);
                internal_static_NameStringPair_descriptor = getDescriptor().getMessageTypes().get(11);
                internal_static_NameStringPair_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_NameStringPair_descriptor, new java.lang.String[] { "Name", "Value" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameStringPair.Builder.class);
                internal_static_NameBytesPair_descriptor = getDescriptor().getMessageTypes().get(12);
                internal_static_NameBytesPair_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_NameBytesPair_descriptor, new java.lang.String[] { "Name", "Value" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameBytesPair.Builder.class);
                internal_static_BytesBytesPair_descriptor = getDescriptor().getMessageTypes().get(13);
                internal_static_BytesBytesPair_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_BytesBytesPair_descriptor, new java.lang.String[] { "First", "Second" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.BytesBytesPair.Builder.class);
                internal_static_NameInt64Pair_descriptor = getDescriptor().getMessageTypes().get(14);
                internal_static_NameInt64Pair_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_NameInt64Pair_descriptor, new java.lang.String[] { "Name", "Value" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.NameInt64Pair.Builder.class);
                internal_static_SnapshotDescription_descriptor = getDescriptor().getMessageTypes().get(15);
                internal_static_SnapshotDescription_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_SnapshotDescription_descriptor, new java.lang.String[] { "Name", "Table", "CreationTime", "Type", "Version" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.SnapshotDescription.Builder.class);
                internal_static_EmptyMsg_descriptor = getDescriptor().getMessageTypes().get(16);
                internal_static_EmptyMsg_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_EmptyMsg_descriptor, new java.lang.String[] {}, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg.Builder.class);
                internal_static_LongMsg_descriptor = getDescriptor().getMessageTypes().get(17);
                internal_static_LongMsg_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_LongMsg_descriptor, new java.lang.String[] { "LongMsg" }, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg.class, org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg.Builder.class);
                return null;
            }
        };
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] {}, assigner);
    }
}
