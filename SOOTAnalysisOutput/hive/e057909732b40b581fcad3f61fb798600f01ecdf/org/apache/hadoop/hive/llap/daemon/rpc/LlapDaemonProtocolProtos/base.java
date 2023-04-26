package org.apache.hadoop.hive.llap.daemon.rpc;

public final class LlapDaemonProtocolProtos {

    private LlapDaemonProtocolProtos() {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    }

    public enum SourceStateProto implements com.google.protobuf.ProtocolMessageEnum {

        S_SUCCEEDED(0, 1), S_RUNNING(1, 2);

        public static final int S_SUCCEEDED_VALUE = 1;

        public static final int S_RUNNING_VALUE = 2;

        public final int getNumber() {
            return value;
        }

        public static SourceStateProto valueOf(int value) {
            switch(value) {
                case 1:
                    return S_SUCCEEDED;
                case 2:
                    return S_RUNNING;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<SourceStateProto> internalGetValueMap() {
            return internalValueMap;
        }

        private static com.google.protobuf.Internal.EnumLiteMap<SourceStateProto> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<SourceStateProto>() {

            public SourceStateProto findValueByNumber(int number) {
                return SourceStateProto.valueOf(number);
            }
        };

        public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.getDescriptor().getEnumTypes().get(0);
        }

        private static final SourceStateProto[] VALUES = values();

        public static SourceStateProto valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private final int index;

        private final int value;

        private SourceStateProto(int index, int value) {
            this.index = index;
            this.value = value;
        }
    }

    public enum SubmissionStateProto implements com.google.protobuf.ProtocolMessageEnum {

        ACCEPTED(0, 1), REJECTED(1, 2), EVICTED_OTHER(2, 3);

        public static final int ACCEPTED_VALUE = 1;

        public static final int REJECTED_VALUE = 2;

        public static final int EVICTED_OTHER_VALUE = 3;

        public final int getNumber() {
            return value;
        }

        public static SubmissionStateProto valueOf(int value) {
            switch(value) {
                case 1:
                    return ACCEPTED;
                case 2:
                    return REJECTED;
                case 3:
                    return EVICTED_OTHER;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<SubmissionStateProto> internalGetValueMap() {
            return internalValueMap;
        }

        private static com.google.protobuf.Internal.EnumLiteMap<SubmissionStateProto> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<SubmissionStateProto>() {

            public SubmissionStateProto findValueByNumber(int number) {
                return SubmissionStateProto.valueOf(number);
            }
        };

        public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.getDescriptor().getEnumTypes().get(1);
        }

        private static final SubmissionStateProto[] VALUES = values();

        public static SubmissionStateProto valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private final int index;

        private final int value;

        private SubmissionStateProto(int index, int value) {
            this.index = index;
            this.value = value;
        }
    }

    public interface UserPayloadProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasUserPayload();

        com.google.protobuf.ByteString getUserPayload();

        boolean hasVersion();

        int getVersion();
    }

    public static final class UserPayloadProto extends com.google.protobuf.GeneratedMessage implements UserPayloadProtoOrBuilder {

        private UserPayloadProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private UserPayloadProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final UserPayloadProto defaultInstance;

        public static UserPayloadProto getDefaultInstance() {
            return defaultInstance;
        }

        public UserPayloadProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private UserPayloadProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                userPayload_ = input.readBytes();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                version_ = input.readInt32();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_UserPayloadProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_UserPayloadProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder.class);
        }

        public static com.google.protobuf.Parser<UserPayloadProto> PARSER = new com.google.protobuf.AbstractParser<UserPayloadProto>() {

            public UserPayloadProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new UserPayloadProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<UserPayloadProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int USER_PAYLOAD_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString userPayload_;

        public boolean hasUserPayload() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getUserPayload() {
            return userPayload_;
        }

        public static final int VERSION_FIELD_NUMBER = 2;

        private int version_;

        public boolean hasVersion() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getVersion() {
            return version_;
        }

        private void initFields() {
            userPayload_ = com.google.protobuf.ByteString.EMPTY;
            version_ = 0;
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
                output.writeBytes(1, userPayload_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt32(2, version_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, userPayload_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, version_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto) obj;
            boolean result = true;
            result = result && (hasUserPayload() == other.hasUserPayload());
            if (hasUserPayload()) {
                result = result && getUserPayload().equals(other.getUserPayload());
            }
            result = result && (hasVersion() == other.hasVersion());
            if (hasVersion()) {
                result = result && (getVersion() == other.getVersion());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasUserPayload()) {
                hash = (37 * hash) + USER_PAYLOAD_FIELD_NUMBER;
                hash = (53 * hash) + getUserPayload().hashCode();
            }
            if (hasVersion()) {
                hash = (37 * hash) + VERSION_FIELD_NUMBER;
                hash = (53 * hash) + getVersion();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_UserPayloadProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_UserPayloadProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                userPayload_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                version_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_UserPayloadProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.userPayload_ = userPayload_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.version_ = version_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance())
                    return this;
                if (other.hasUserPayload()) {
                    setUserPayload(other.getUserPayload());
                }
                if (other.hasVersion()) {
                    setVersion(other.getVersion());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private com.google.protobuf.ByteString userPayload_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasUserPayload() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getUserPayload() {
                return userPayload_;
            }

            public Builder setUserPayload(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                userPayload_ = value;
                onChanged();
                return this;
            }

            public Builder clearUserPayload() {
                bitField0_ = (bitField0_ & ~0x00000001);
                userPayload_ = getDefaultInstance().getUserPayload();
                onChanged();
                return this;
            }

            private int version_;

            public boolean hasVersion() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getVersion() {
                return version_;
            }

            public Builder setVersion(int value) {
                bitField0_ |= 0x00000002;
                version_ = value;
                onChanged();
                return this;
            }

            public Builder clearVersion() {
                bitField0_ = (bitField0_ & ~0x00000002);
                version_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new UserPayloadProto(true);
            defaultInstance.initFields();
        }
    }

    public interface EntityDescriptorProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasClassName();

        java.lang.String getClassName();

        com.google.protobuf.ByteString getClassNameBytes();

        boolean hasUserPayload();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto getUserPayload();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder getUserPayloadOrBuilder();

        boolean hasHistoryText();

        com.google.protobuf.ByteString getHistoryText();
    }

    public static final class EntityDescriptorProto extends com.google.protobuf.GeneratedMessage implements EntityDescriptorProtoOrBuilder {

        private EntityDescriptorProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private EntityDescriptorProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final EntityDescriptorProto defaultInstance;

        public static EntityDescriptorProto getDefaultInstance() {
            return defaultInstance;
        }

        public EntityDescriptorProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private EntityDescriptorProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                className_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000002) == 0x00000002)) {
                                    subBuilder = userPayload_.toBuilder();
                                }
                                userPayload_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(userPayload_);
                                    userPayload_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000002;
                                break;
                            }
                        case 26:
                            {
                                bitField0_ |= 0x00000004;
                                historyText_ = input.readBytes();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_EntityDescriptorProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_EntityDescriptorProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder.class);
        }

        public static com.google.protobuf.Parser<EntityDescriptorProto> PARSER = new com.google.protobuf.AbstractParser<EntityDescriptorProto>() {

            public EntityDescriptorProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new EntityDescriptorProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<EntityDescriptorProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int CLASS_NAME_FIELD_NUMBER = 1;

        private java.lang.Object className_;

        public boolean hasClassName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getClassName() {
            java.lang.Object ref = className_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    className_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getClassNameBytes() {
            java.lang.Object ref = className_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                className_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int USER_PAYLOAD_FIELD_NUMBER = 2;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto userPayload_;

        public boolean hasUserPayload() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto getUserPayload() {
            return userPayload_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder getUserPayloadOrBuilder() {
            return userPayload_;
        }

        public static final int HISTORY_TEXT_FIELD_NUMBER = 3;

        private com.google.protobuf.ByteString historyText_;

        public boolean hasHistoryText() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public com.google.protobuf.ByteString getHistoryText() {
            return historyText_;
        }

        private void initFields() {
            className_ = "";
            userPayload_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance();
            historyText_ = com.google.protobuf.ByteString.EMPTY;
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
                output.writeBytes(1, getClassNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeMessage(2, userPayload_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeBytes(3, historyText_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getClassNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, userPayload_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, historyText_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto) obj;
            boolean result = true;
            result = result && (hasClassName() == other.hasClassName());
            if (hasClassName()) {
                result = result && getClassName().equals(other.getClassName());
            }
            result = result && (hasUserPayload() == other.hasUserPayload());
            if (hasUserPayload()) {
                result = result && getUserPayload().equals(other.getUserPayload());
            }
            result = result && (hasHistoryText() == other.hasHistoryText());
            if (hasHistoryText()) {
                result = result && getHistoryText().equals(other.getHistoryText());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasClassName()) {
                hash = (37 * hash) + CLASS_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getClassName().hashCode();
            }
            if (hasUserPayload()) {
                hash = (37 * hash) + USER_PAYLOAD_FIELD_NUMBER;
                hash = (53 * hash) + getUserPayload().hashCode();
            }
            if (hasHistoryText()) {
                hash = (37 * hash) + HISTORY_TEXT_FIELD_NUMBER;
                hash = (53 * hash) + getHistoryText().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_EntityDescriptorProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_EntityDescriptorProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getUserPayloadFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                className_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                if (userPayloadBuilder_ == null) {
                    userPayload_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance();
                } else {
                    userPayloadBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                historyText_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_EntityDescriptorProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.className_ = className_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                if (userPayloadBuilder_ == null) {
                    result.userPayload_ = userPayload_;
                } else {
                    result.userPayload_ = userPayloadBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.historyText_ = historyText_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance())
                    return this;
                if (other.hasClassName()) {
                    bitField0_ |= 0x00000001;
                    className_ = other.className_;
                    onChanged();
                }
                if (other.hasUserPayload()) {
                    mergeUserPayload(other.getUserPayload());
                }
                if (other.hasHistoryText()) {
                    setHistoryText(other.getHistoryText());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object className_ = "";

            public boolean hasClassName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getClassName() {
                java.lang.Object ref = className_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    className_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getClassNameBytes() {
                java.lang.Object ref = className_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    className_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setClassName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                className_ = value;
                onChanged();
                return this;
            }

            public Builder clearClassName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                className_ = getDefaultInstance().getClassName();
                onChanged();
                return this;
            }

            public Builder setClassNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                className_ = value;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto userPayload_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder> userPayloadBuilder_;

            public boolean hasUserPayload() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto getUserPayload() {
                if (userPayloadBuilder_ == null) {
                    return userPayload_;
                } else {
                    return userPayloadBuilder_.getMessage();
                }
            }

            public Builder setUserPayload(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto value) {
                if (userPayloadBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    userPayload_ = value;
                    onChanged();
                } else {
                    userPayloadBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder setUserPayload(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder builderForValue) {
                if (userPayloadBuilder_ == null) {
                    userPayload_ = builderForValue.build();
                    onChanged();
                } else {
                    userPayloadBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder mergeUserPayload(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto value) {
                if (userPayloadBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) == 0x00000002) && userPayload_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance()) {
                        userPayload_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.newBuilder(userPayload_).mergeFrom(value).buildPartial();
                    } else {
                        userPayload_ = value;
                    }
                    onChanged();
                } else {
                    userPayloadBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder clearUserPayload() {
                if (userPayloadBuilder_ == null) {
                    userPayload_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.getDefaultInstance();
                    onChanged();
                } else {
                    userPayloadBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder getUserPayloadBuilder() {
                bitField0_ |= 0x00000002;
                onChanged();
                return getUserPayloadFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder getUserPayloadOrBuilder() {
                if (userPayloadBuilder_ != null) {
                    return userPayloadBuilder_.getMessageOrBuilder();
                } else {
                    return userPayload_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder> getUserPayloadFieldBuilder() {
                if (userPayloadBuilder_ == null) {
                    userPayloadBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.UserPayloadProtoOrBuilder>(userPayload_, getParentForChildren(), isClean());
                    userPayload_ = null;
                }
                return userPayloadBuilder_;
            }

            private com.google.protobuf.ByteString historyText_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasHistoryText() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public com.google.protobuf.ByteString getHistoryText() {
                return historyText_;
            }

            public Builder setHistoryText(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                historyText_ = value;
                onChanged();
                return this;
            }

            public Builder clearHistoryText() {
                bitField0_ = (bitField0_ & ~0x00000004);
                historyText_ = getDefaultInstance().getHistoryText();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new EntityDescriptorProto(true);
            defaultInstance.initFields();
        }
    }

    public interface IOSpecProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasConnectedVertexName();

        java.lang.String getConnectedVertexName();

        com.google.protobuf.ByteString getConnectedVertexNameBytes();

        boolean hasIoDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getIoDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getIoDescriptorOrBuilder();

        boolean hasPhysicalEdgeCount();

        int getPhysicalEdgeCount();
    }

    public static final class IOSpecProto extends com.google.protobuf.GeneratedMessage implements IOSpecProtoOrBuilder {

        private IOSpecProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private IOSpecProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final IOSpecProto defaultInstance;

        public static IOSpecProto getDefaultInstance() {
            return defaultInstance;
        }

        public IOSpecProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private IOSpecProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                connectedVertexName_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000002) == 0x00000002)) {
                                    subBuilder = ioDescriptor_.toBuilder();
                                }
                                ioDescriptor_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(ioDescriptor_);
                                    ioDescriptor_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000002;
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                physicalEdgeCount_ = input.readInt32();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_IOSpecProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_IOSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder.class);
        }

        public static com.google.protobuf.Parser<IOSpecProto> PARSER = new com.google.protobuf.AbstractParser<IOSpecProto>() {

            public IOSpecProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new IOSpecProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<IOSpecProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int CONNECTED_VERTEX_NAME_FIELD_NUMBER = 1;

        private java.lang.Object connectedVertexName_;

        public boolean hasConnectedVertexName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getConnectedVertexName() {
            java.lang.Object ref = connectedVertexName_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    connectedVertexName_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getConnectedVertexNameBytes() {
            java.lang.Object ref = connectedVertexName_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                connectedVertexName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int IO_DESCRIPTOR_FIELD_NUMBER = 2;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto ioDescriptor_;

        public boolean hasIoDescriptor() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getIoDescriptor() {
            return ioDescriptor_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getIoDescriptorOrBuilder() {
            return ioDescriptor_;
        }

        public static final int PHYSICAL_EDGE_COUNT_FIELD_NUMBER = 3;

        private int physicalEdgeCount_;

        public boolean hasPhysicalEdgeCount() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getPhysicalEdgeCount() {
            return physicalEdgeCount_;
        }

        private void initFields() {
            connectedVertexName_ = "";
            ioDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
            physicalEdgeCount_ = 0;
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
                output.writeBytes(1, getConnectedVertexNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeMessage(2, ioDescriptor_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(3, physicalEdgeCount_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getConnectedVertexNameBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, ioDescriptor_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, physicalEdgeCount_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto) obj;
            boolean result = true;
            result = result && (hasConnectedVertexName() == other.hasConnectedVertexName());
            if (hasConnectedVertexName()) {
                result = result && getConnectedVertexName().equals(other.getConnectedVertexName());
            }
            result = result && (hasIoDescriptor() == other.hasIoDescriptor());
            if (hasIoDescriptor()) {
                result = result && getIoDescriptor().equals(other.getIoDescriptor());
            }
            result = result && (hasPhysicalEdgeCount() == other.hasPhysicalEdgeCount());
            if (hasPhysicalEdgeCount()) {
                result = result && (getPhysicalEdgeCount() == other.getPhysicalEdgeCount());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasConnectedVertexName()) {
                hash = (37 * hash) + CONNECTED_VERTEX_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getConnectedVertexName().hashCode();
            }
            if (hasIoDescriptor()) {
                hash = (37 * hash) + IO_DESCRIPTOR_FIELD_NUMBER;
                hash = (53 * hash) + getIoDescriptor().hashCode();
            }
            if (hasPhysicalEdgeCount()) {
                hash = (37 * hash) + PHYSICAL_EDGE_COUNT_FIELD_NUMBER;
                hash = (53 * hash) + getPhysicalEdgeCount();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_IOSpecProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_IOSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getIoDescriptorFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                connectedVertexName_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                if (ioDescriptorBuilder_ == null) {
                    ioDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                } else {
                    ioDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                physicalEdgeCount_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_IOSpecProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.connectedVertexName_ = connectedVertexName_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                if (ioDescriptorBuilder_ == null) {
                    result.ioDescriptor_ = ioDescriptor_;
                } else {
                    result.ioDescriptor_ = ioDescriptorBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.physicalEdgeCount_ = physicalEdgeCount_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance())
                    return this;
                if (other.hasConnectedVertexName()) {
                    bitField0_ |= 0x00000001;
                    connectedVertexName_ = other.connectedVertexName_;
                    onChanged();
                }
                if (other.hasIoDescriptor()) {
                    mergeIoDescriptor(other.getIoDescriptor());
                }
                if (other.hasPhysicalEdgeCount()) {
                    setPhysicalEdgeCount(other.getPhysicalEdgeCount());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object connectedVertexName_ = "";

            public boolean hasConnectedVertexName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getConnectedVertexName() {
                java.lang.Object ref = connectedVertexName_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    connectedVertexName_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getConnectedVertexNameBytes() {
                java.lang.Object ref = connectedVertexName_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    connectedVertexName_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setConnectedVertexName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                connectedVertexName_ = value;
                onChanged();
                return this;
            }

            public Builder clearConnectedVertexName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                connectedVertexName_ = getDefaultInstance().getConnectedVertexName();
                onChanged();
                return this;
            }

            public Builder setConnectedVertexNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                connectedVertexName_ = value;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto ioDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> ioDescriptorBuilder_;

            public boolean hasIoDescriptor() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getIoDescriptor() {
                if (ioDescriptorBuilder_ == null) {
                    return ioDescriptor_;
                } else {
                    return ioDescriptorBuilder_.getMessage();
                }
            }

            public Builder setIoDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (ioDescriptorBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ioDescriptor_ = value;
                    onChanged();
                } else {
                    ioDescriptorBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder setIoDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder builderForValue) {
                if (ioDescriptorBuilder_ == null) {
                    ioDescriptor_ = builderForValue.build();
                    onChanged();
                } else {
                    ioDescriptorBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder mergeIoDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (ioDescriptorBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) == 0x00000002) && ioDescriptor_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance()) {
                        ioDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.newBuilder(ioDescriptor_).mergeFrom(value).buildPartial();
                    } else {
                        ioDescriptor_ = value;
                    }
                    onChanged();
                } else {
                    ioDescriptorBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder clearIoDescriptor() {
                if (ioDescriptorBuilder_ == null) {
                    ioDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                    onChanged();
                } else {
                    ioDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder getIoDescriptorBuilder() {
                bitField0_ |= 0x00000002;
                onChanged();
                return getIoDescriptorFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getIoDescriptorOrBuilder() {
                if (ioDescriptorBuilder_ != null) {
                    return ioDescriptorBuilder_.getMessageOrBuilder();
                } else {
                    return ioDescriptor_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> getIoDescriptorFieldBuilder() {
                if (ioDescriptorBuilder_ == null) {
                    ioDescriptorBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder>(ioDescriptor_, getParentForChildren(), isClean());
                    ioDescriptor_ = null;
                }
                return ioDescriptorBuilder_;
            }

            private int physicalEdgeCount_;

            public boolean hasPhysicalEdgeCount() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getPhysicalEdgeCount() {
                return physicalEdgeCount_;
            }

            public Builder setPhysicalEdgeCount(int value) {
                bitField0_ |= 0x00000004;
                physicalEdgeCount_ = value;
                onChanged();
                return this;
            }

            public Builder clearPhysicalEdgeCount() {
                bitField0_ = (bitField0_ & ~0x00000004);
                physicalEdgeCount_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new IOSpecProto(true);
            defaultInstance.initFields();
        }
    }

    public interface GroupInputSpecProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasGroupName();

        java.lang.String getGroupName();

        com.google.protobuf.ByteString getGroupNameBytes();

        java.util.List<java.lang.String> getGroupVerticesList();

        int getGroupVerticesCount();

        java.lang.String getGroupVertices(int index);

        com.google.protobuf.ByteString getGroupVerticesBytes(int index);

        boolean hasMergedInputDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getMergedInputDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getMergedInputDescriptorOrBuilder();
    }

    public static final class GroupInputSpecProto extends com.google.protobuf.GeneratedMessage implements GroupInputSpecProtoOrBuilder {

        private GroupInputSpecProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private GroupInputSpecProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final GroupInputSpecProto defaultInstance;

        public static GroupInputSpecProto getDefaultInstance() {
            return defaultInstance;
        }

        public GroupInputSpecProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private GroupInputSpecProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                groupName_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                                    groupVertices_ = new com.google.protobuf.LazyStringArrayList();
                                    mutable_bitField0_ |= 0x00000002;
                                }
                                groupVertices_.add(input.readBytes());
                                break;
                            }
                        case 26:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000002) == 0x00000002)) {
                                    subBuilder = mergedInputDescriptor_.toBuilder();
                                }
                                mergedInputDescriptor_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(mergedInputDescriptor_);
                                    mergedInputDescriptor_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000002;
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                    groupVertices_ = new com.google.protobuf.UnmodifiableLazyStringList(groupVertices_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GroupInputSpecProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GroupInputSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder.class);
        }

        public static com.google.protobuf.Parser<GroupInputSpecProto> PARSER = new com.google.protobuf.AbstractParser<GroupInputSpecProto>() {

            public GroupInputSpecProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new GroupInputSpecProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<GroupInputSpecProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int GROUP_NAME_FIELD_NUMBER = 1;

        private java.lang.Object groupName_;

        public boolean hasGroupName() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getGroupName() {
            java.lang.Object ref = groupName_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    groupName_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getGroupNameBytes() {
            java.lang.Object ref = groupName_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                groupName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int GROUP_VERTICES_FIELD_NUMBER = 2;

        private com.google.protobuf.LazyStringList groupVertices_;

        public java.util.List<java.lang.String> getGroupVerticesList() {
            return groupVertices_;
        }

        public int getGroupVerticesCount() {
            return groupVertices_.size();
        }

        public java.lang.String getGroupVertices(int index) {
            return groupVertices_.get(index);
        }

        public com.google.protobuf.ByteString getGroupVerticesBytes(int index) {
            return groupVertices_.getByteString(index);
        }

        public static final int MERGED_INPUT_DESCRIPTOR_FIELD_NUMBER = 3;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto mergedInputDescriptor_;

        public boolean hasMergedInputDescriptor() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getMergedInputDescriptor() {
            return mergedInputDescriptor_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getMergedInputDescriptorOrBuilder() {
            return mergedInputDescriptor_;
        }

        private void initFields() {
            groupName_ = "";
            groupVertices_ = com.google.protobuf.LazyStringArrayList.EMPTY;
            mergedInputDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
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
                output.writeBytes(1, getGroupNameBytes());
            }
            for (int i = 0; i < groupVertices_.size(); i++) {
                output.writeBytes(2, groupVertices_.getByteString(i));
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeMessage(3, mergedInputDescriptor_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getGroupNameBytes());
            }
            {
                int dataSize = 0;
                for (int i = 0; i < groupVertices_.size(); i++) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeBytesSizeNoTag(groupVertices_.getByteString(i));
                }
                size += dataSize;
                size += 1 * getGroupVerticesList().size();
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, mergedInputDescriptor_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto) obj;
            boolean result = true;
            result = result && (hasGroupName() == other.hasGroupName());
            if (hasGroupName()) {
                result = result && getGroupName().equals(other.getGroupName());
            }
            result = result && getGroupVerticesList().equals(other.getGroupVerticesList());
            result = result && (hasMergedInputDescriptor() == other.hasMergedInputDescriptor());
            if (hasMergedInputDescriptor()) {
                result = result && getMergedInputDescriptor().equals(other.getMergedInputDescriptor());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasGroupName()) {
                hash = (37 * hash) + GROUP_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getGroupName().hashCode();
            }
            if (getGroupVerticesCount() > 0) {
                hash = (37 * hash) + GROUP_VERTICES_FIELD_NUMBER;
                hash = (53 * hash) + getGroupVerticesList().hashCode();
            }
            if (hasMergedInputDescriptor()) {
                hash = (37 * hash) + MERGED_INPUT_DESCRIPTOR_FIELD_NUMBER;
                hash = (53 * hash) + getMergedInputDescriptor().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GroupInputSpecProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GroupInputSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getMergedInputDescriptorFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                groupName_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                groupVertices_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                if (mergedInputDescriptorBuilder_ == null) {
                    mergedInputDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                } else {
                    mergedInputDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GroupInputSpecProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.groupName_ = groupName_;
                if (((bitField0_ & 0x00000002) == 0x00000002)) {
                    groupVertices_ = new com.google.protobuf.UnmodifiableLazyStringList(groupVertices_);
                    bitField0_ = (bitField0_ & ~0x00000002);
                }
                result.groupVertices_ = groupVertices_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000002;
                }
                if (mergedInputDescriptorBuilder_ == null) {
                    result.mergedInputDescriptor_ = mergedInputDescriptor_;
                } else {
                    result.mergedInputDescriptor_ = mergedInputDescriptorBuilder_.build();
                }
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.getDefaultInstance())
                    return this;
                if (other.hasGroupName()) {
                    bitField0_ |= 0x00000001;
                    groupName_ = other.groupName_;
                    onChanged();
                }
                if (!other.groupVertices_.isEmpty()) {
                    if (groupVertices_.isEmpty()) {
                        groupVertices_ = other.groupVertices_;
                        bitField0_ = (bitField0_ & ~0x00000002);
                    } else {
                        ensureGroupVerticesIsMutable();
                        groupVertices_.addAll(other.groupVertices_);
                    }
                    onChanged();
                }
                if (other.hasMergedInputDescriptor()) {
                    mergeMergedInputDescriptor(other.getMergedInputDescriptor());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object groupName_ = "";

            public boolean hasGroupName() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getGroupName() {
                java.lang.Object ref = groupName_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    groupName_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getGroupNameBytes() {
                java.lang.Object ref = groupName_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    groupName_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setGroupName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                groupName_ = value;
                onChanged();
                return this;
            }

            public Builder clearGroupName() {
                bitField0_ = (bitField0_ & ~0x00000001);
                groupName_ = getDefaultInstance().getGroupName();
                onChanged();
                return this;
            }

            public Builder setGroupNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                groupName_ = value;
                onChanged();
                return this;
            }

            private com.google.protobuf.LazyStringList groupVertices_ = com.google.protobuf.LazyStringArrayList.EMPTY;

            private void ensureGroupVerticesIsMutable() {
                if (!((bitField0_ & 0x00000002) == 0x00000002)) {
                    groupVertices_ = new com.google.protobuf.LazyStringArrayList(groupVertices_);
                    bitField0_ |= 0x00000002;
                }
            }

            public java.util.List<java.lang.String> getGroupVerticesList() {
                return java.util.Collections.unmodifiableList(groupVertices_);
            }

            public int getGroupVerticesCount() {
                return groupVertices_.size();
            }

            public java.lang.String getGroupVertices(int index) {
                return groupVertices_.get(index);
            }

            public com.google.protobuf.ByteString getGroupVerticesBytes(int index) {
                return groupVertices_.getByteString(index);
            }

            public Builder setGroupVertices(int index, java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGroupVerticesIsMutable();
                groupVertices_.set(index, value);
                onChanged();
                return this;
            }

            public Builder addGroupVertices(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGroupVerticesIsMutable();
                groupVertices_.add(value);
                onChanged();
                return this;
            }

            public Builder addAllGroupVertices(java.lang.Iterable<java.lang.String> values) {
                ensureGroupVerticesIsMutable();
                super.addAll(values, groupVertices_);
                onChanged();
                return this;
            }

            public Builder clearGroupVertices() {
                groupVertices_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                onChanged();
                return this;
            }

            public Builder addGroupVerticesBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureGroupVerticesIsMutable();
                groupVertices_.add(value);
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto mergedInputDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> mergedInputDescriptorBuilder_;

            public boolean hasMergedInputDescriptor() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getMergedInputDescriptor() {
                if (mergedInputDescriptorBuilder_ == null) {
                    return mergedInputDescriptor_;
                } else {
                    return mergedInputDescriptorBuilder_.getMessage();
                }
            }

            public Builder setMergedInputDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (mergedInputDescriptorBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    mergedInputDescriptor_ = value;
                    onChanged();
                } else {
                    mergedInputDescriptorBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            public Builder setMergedInputDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder builderForValue) {
                if (mergedInputDescriptorBuilder_ == null) {
                    mergedInputDescriptor_ = builderForValue.build();
                    onChanged();
                } else {
                    mergedInputDescriptorBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            public Builder mergeMergedInputDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (mergedInputDescriptorBuilder_ == null) {
                    if (((bitField0_ & 0x00000004) == 0x00000004) && mergedInputDescriptor_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance()) {
                        mergedInputDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.newBuilder(mergedInputDescriptor_).mergeFrom(value).buildPartial();
                    } else {
                        mergedInputDescriptor_ = value;
                    }
                    onChanged();
                } else {
                    mergedInputDescriptorBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            public Builder clearMergedInputDescriptor() {
                if (mergedInputDescriptorBuilder_ == null) {
                    mergedInputDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                    onChanged();
                } else {
                    mergedInputDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder getMergedInputDescriptorBuilder() {
                bitField0_ |= 0x00000004;
                onChanged();
                return getMergedInputDescriptorFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getMergedInputDescriptorOrBuilder() {
                if (mergedInputDescriptorBuilder_ != null) {
                    return mergedInputDescriptorBuilder_.getMessageOrBuilder();
                } else {
                    return mergedInputDescriptor_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> getMergedInputDescriptorFieldBuilder() {
                if (mergedInputDescriptorBuilder_ == null) {
                    mergedInputDescriptorBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder>(mergedInputDescriptor_, getParentForChildren(), isClean());
                    mergedInputDescriptor_ = null;
                }
                return mergedInputDescriptorBuilder_;
            }
        }

        static {
            defaultInstance = new GroupInputSpecProto(true);
            defaultInstance.initFields();
        }
    }

    public interface FragmentSpecProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasFragmentIdentifierString();

        java.lang.String getFragmentIdentifierString();

        com.google.protobuf.ByteString getFragmentIdentifierStringBytes();

        boolean hasDagName();

        java.lang.String getDagName();

        com.google.protobuf.ByteString getDagNameBytes();

        boolean hasDagId();

        int getDagId();

        boolean hasVertexName();

        java.lang.String getVertexName();

        com.google.protobuf.ByteString getVertexNameBytes();

        boolean hasProcessorDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getProcessorDescriptor();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getProcessorDescriptorOrBuilder();

        java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getInputSpecsList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getInputSpecs(int index);

        int getInputSpecsCount();

        java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getInputSpecsOrBuilderList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getInputSpecsOrBuilder(int index);

        java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getOutputSpecsList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getOutputSpecs(int index);

        int getOutputSpecsCount();

        java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getOutputSpecsOrBuilderList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getOutputSpecsOrBuilder(int index);

        java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> getGroupedInputSpecsList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto getGroupedInputSpecs(int index);

        int getGroupedInputSpecsCount();

        java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder> getGroupedInputSpecsOrBuilderList();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder getGroupedInputSpecsOrBuilder(int index);

        boolean hasVertexParallelism();

        int getVertexParallelism();

        boolean hasFragmentNumber();

        int getFragmentNumber();

        boolean hasAttemptNumber();

        int getAttemptNumber();
    }

    public static final class FragmentSpecProto extends com.google.protobuf.GeneratedMessage implements FragmentSpecProtoOrBuilder {

        private FragmentSpecProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private FragmentSpecProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final FragmentSpecProto defaultInstance;

        public static FragmentSpecProto getDefaultInstance() {
            return defaultInstance;
        }

        public FragmentSpecProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private FragmentSpecProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                fragmentIdentifierString_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                dagName_ = input.readBytes();
                                break;
                            }
                        case 26:
                            {
                                bitField0_ |= 0x00000008;
                                vertexName_ = input.readBytes();
                                break;
                            }
                        case 34:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000010) == 0x00000010)) {
                                    subBuilder = processorDescriptor_.toBuilder();
                                }
                                processorDescriptor_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(processorDescriptor_);
                                    processorDescriptor_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000010;
                                break;
                            }
                        case 42:
                            {
                                if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
                                    inputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto>();
                                    mutable_bitField0_ |= 0x00000020;
                                }
                                inputSpecs_.add(input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.PARSER, extensionRegistry));
                                break;
                            }
                        case 50:
                            {
                                if (!((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                                    outputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto>();
                                    mutable_bitField0_ |= 0x00000040;
                                }
                                outputSpecs_.add(input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.PARSER, extensionRegistry));
                                break;
                            }
                        case 58:
                            {
                                if (!((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
                                    groupedInputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto>();
                                    mutable_bitField0_ |= 0x00000080;
                                }
                                groupedInputSpecs_.add(input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.PARSER, extensionRegistry));
                                break;
                            }
                        case 64:
                            {
                                bitField0_ |= 0x00000020;
                                vertexParallelism_ = input.readInt32();
                                break;
                            }
                        case 72:
                            {
                                bitField0_ |= 0x00000040;
                                fragmentNumber_ = input.readInt32();
                                break;
                            }
                        case 80:
                            {
                                bitField0_ |= 0x00000080;
                                attemptNumber_ = input.readInt32();
                                break;
                            }
                        case 88:
                            {
                                bitField0_ |= 0x00000004;
                                dagId_ = input.readInt32();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
                    inputSpecs_ = java.util.Collections.unmodifiableList(inputSpecs_);
                }
                if (((mutable_bitField0_ & 0x00000040) == 0x00000040)) {
                    outputSpecs_ = java.util.Collections.unmodifiableList(outputSpecs_);
                }
                if (((mutable_bitField0_ & 0x00000080) == 0x00000080)) {
                    groupedInputSpecs_ = java.util.Collections.unmodifiableList(groupedInputSpecs_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentSpecProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder.class);
        }

        public static com.google.protobuf.Parser<FragmentSpecProto> PARSER = new com.google.protobuf.AbstractParser<FragmentSpecProto>() {

            public FragmentSpecProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new FragmentSpecProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<FragmentSpecProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int FRAGMENT_IDENTIFIER_STRING_FIELD_NUMBER = 1;

        private java.lang.Object fragmentIdentifierString_;

        public boolean hasFragmentIdentifierString() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getFragmentIdentifierString() {
            java.lang.Object ref = fragmentIdentifierString_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    fragmentIdentifierString_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getFragmentIdentifierStringBytes() {
            java.lang.Object ref = fragmentIdentifierString_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                fragmentIdentifierString_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int DAG_NAME_FIELD_NUMBER = 2;

        private java.lang.Object dagName_;

        public boolean hasDagName() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public java.lang.String getDagName() {
            java.lang.Object ref = dagName_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    dagName_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getDagNameBytes() {
            java.lang.Object ref = dagName_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                dagName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int DAG_ID_FIELD_NUMBER = 11;

        private int dagId_;

        public boolean hasDagId() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getDagId() {
            return dagId_;
        }

        public static final int VERTEX_NAME_FIELD_NUMBER = 3;

        private java.lang.Object vertexName_;

        public boolean hasVertexName() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public java.lang.String getVertexName() {
            java.lang.Object ref = vertexName_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    vertexName_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getVertexNameBytes() {
            java.lang.Object ref = vertexName_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                vertexName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int PROCESSOR_DESCRIPTOR_FIELD_NUMBER = 4;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto processorDescriptor_;

        public boolean hasProcessorDescriptor() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getProcessorDescriptor() {
            return processorDescriptor_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getProcessorDescriptorOrBuilder() {
            return processorDescriptor_;
        }

        public static final int INPUT_SPECS_FIELD_NUMBER = 5;

        private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> inputSpecs_;

        public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getInputSpecsList() {
            return inputSpecs_;
        }

        public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getInputSpecsOrBuilderList() {
            return inputSpecs_;
        }

        public int getInputSpecsCount() {
            return inputSpecs_.size();
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getInputSpecs(int index) {
            return inputSpecs_.get(index);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getInputSpecsOrBuilder(int index) {
            return inputSpecs_.get(index);
        }

        public static final int OUTPUT_SPECS_FIELD_NUMBER = 6;

        private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> outputSpecs_;

        public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getOutputSpecsList() {
            return outputSpecs_;
        }

        public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getOutputSpecsOrBuilderList() {
            return outputSpecs_;
        }

        public int getOutputSpecsCount() {
            return outputSpecs_.size();
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getOutputSpecs(int index) {
            return outputSpecs_.get(index);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getOutputSpecsOrBuilder(int index) {
            return outputSpecs_.get(index);
        }

        public static final int GROUPED_INPUT_SPECS_FIELD_NUMBER = 7;

        private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> groupedInputSpecs_;

        public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> getGroupedInputSpecsList() {
            return groupedInputSpecs_;
        }

        public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder> getGroupedInputSpecsOrBuilderList() {
            return groupedInputSpecs_;
        }

        public int getGroupedInputSpecsCount() {
            return groupedInputSpecs_.size();
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto getGroupedInputSpecs(int index) {
            return groupedInputSpecs_.get(index);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder getGroupedInputSpecsOrBuilder(int index) {
            return groupedInputSpecs_.get(index);
        }

        public static final int VERTEX_PARALLELISM_FIELD_NUMBER = 8;

        private int vertexParallelism_;

        public boolean hasVertexParallelism() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public int getVertexParallelism() {
            return vertexParallelism_;
        }

        public static final int FRAGMENT_NUMBER_FIELD_NUMBER = 9;

        private int fragmentNumber_;

        public boolean hasFragmentNumber() {
            return ((bitField0_ & 0x00000040) == 0x00000040);
        }

        public int getFragmentNumber() {
            return fragmentNumber_;
        }

        public static final int ATTEMPT_NUMBER_FIELD_NUMBER = 10;

        private int attemptNumber_;

        public boolean hasAttemptNumber() {
            return ((bitField0_ & 0x00000080) == 0x00000080);
        }

        public int getAttemptNumber() {
            return attemptNumber_;
        }

        private void initFields() {
            fragmentIdentifierString_ = "";
            dagName_ = "";
            dagId_ = 0;
            vertexName_ = "";
            processorDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
            inputSpecs_ = java.util.Collections.emptyList();
            outputSpecs_ = java.util.Collections.emptyList();
            groupedInputSpecs_ = java.util.Collections.emptyList();
            vertexParallelism_ = 0;
            fragmentNumber_ = 0;
            attemptNumber_ = 0;
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
                output.writeBytes(1, getFragmentIdentifierStringBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getDagNameBytes());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBytes(3, getVertexNameBytes());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeMessage(4, processorDescriptor_);
            }
            for (int i = 0; i < inputSpecs_.size(); i++) {
                output.writeMessage(5, inputSpecs_.get(i));
            }
            for (int i = 0; i < outputSpecs_.size(); i++) {
                output.writeMessage(6, outputSpecs_.get(i));
            }
            for (int i = 0; i < groupedInputSpecs_.size(); i++) {
                output.writeMessage(7, groupedInputSpecs_.get(i));
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeInt32(8, vertexParallelism_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                output.writeInt32(9, fragmentNumber_);
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                output.writeInt32(10, attemptNumber_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(11, dagId_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getFragmentIdentifierStringBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getDagNameBytes());
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(3, getVertexNameBytes());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, processorDescriptor_);
            }
            for (int i = 0; i < inputSpecs_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, inputSpecs_.get(i));
            }
            for (int i = 0; i < outputSpecs_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, outputSpecs_.get(i));
            }
            for (int i = 0; i < groupedInputSpecs_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, groupedInputSpecs_.get(i));
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(8, vertexParallelism_);
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(9, fragmentNumber_);
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(10, attemptNumber_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(11, dagId_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto) obj;
            boolean result = true;
            result = result && (hasFragmentIdentifierString() == other.hasFragmentIdentifierString());
            if (hasFragmentIdentifierString()) {
                result = result && getFragmentIdentifierString().equals(other.getFragmentIdentifierString());
            }
            result = result && (hasDagName() == other.hasDagName());
            if (hasDagName()) {
                result = result && getDagName().equals(other.getDagName());
            }
            result = result && (hasDagId() == other.hasDagId());
            if (hasDagId()) {
                result = result && (getDagId() == other.getDagId());
            }
            result = result && (hasVertexName() == other.hasVertexName());
            if (hasVertexName()) {
                result = result && getVertexName().equals(other.getVertexName());
            }
            result = result && (hasProcessorDescriptor() == other.hasProcessorDescriptor());
            if (hasProcessorDescriptor()) {
                result = result && getProcessorDescriptor().equals(other.getProcessorDescriptor());
            }
            result = result && getInputSpecsList().equals(other.getInputSpecsList());
            result = result && getOutputSpecsList().equals(other.getOutputSpecsList());
            result = result && getGroupedInputSpecsList().equals(other.getGroupedInputSpecsList());
            result = result && (hasVertexParallelism() == other.hasVertexParallelism());
            if (hasVertexParallelism()) {
                result = result && (getVertexParallelism() == other.getVertexParallelism());
            }
            result = result && (hasFragmentNumber() == other.hasFragmentNumber());
            if (hasFragmentNumber()) {
                result = result && (getFragmentNumber() == other.getFragmentNumber());
            }
            result = result && (hasAttemptNumber() == other.hasAttemptNumber());
            if (hasAttemptNumber()) {
                result = result && (getAttemptNumber() == other.getAttemptNumber());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasFragmentIdentifierString()) {
                hash = (37 * hash) + FRAGMENT_IDENTIFIER_STRING_FIELD_NUMBER;
                hash = (53 * hash) + getFragmentIdentifierString().hashCode();
            }
            if (hasDagName()) {
                hash = (37 * hash) + DAG_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getDagName().hashCode();
            }
            if (hasDagId()) {
                hash = (37 * hash) + DAG_ID_FIELD_NUMBER;
                hash = (53 * hash) + getDagId();
            }
            if (hasVertexName()) {
                hash = (37 * hash) + VERTEX_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getVertexName().hashCode();
            }
            if (hasProcessorDescriptor()) {
                hash = (37 * hash) + PROCESSOR_DESCRIPTOR_FIELD_NUMBER;
                hash = (53 * hash) + getProcessorDescriptor().hashCode();
            }
            if (getInputSpecsCount() > 0) {
                hash = (37 * hash) + INPUT_SPECS_FIELD_NUMBER;
                hash = (53 * hash) + getInputSpecsList().hashCode();
            }
            if (getOutputSpecsCount() > 0) {
                hash = (37 * hash) + OUTPUT_SPECS_FIELD_NUMBER;
                hash = (53 * hash) + getOutputSpecsList().hashCode();
            }
            if (getGroupedInputSpecsCount() > 0) {
                hash = (37 * hash) + GROUPED_INPUT_SPECS_FIELD_NUMBER;
                hash = (53 * hash) + getGroupedInputSpecsList().hashCode();
            }
            if (hasVertexParallelism()) {
                hash = (37 * hash) + VERTEX_PARALLELISM_FIELD_NUMBER;
                hash = (53 * hash) + getVertexParallelism();
            }
            if (hasFragmentNumber()) {
                hash = (37 * hash) + FRAGMENT_NUMBER_FIELD_NUMBER;
                hash = (53 * hash) + getFragmentNumber();
            }
            if (hasAttemptNumber()) {
                hash = (37 * hash) + ATTEMPT_NUMBER_FIELD_NUMBER;
                hash = (53 * hash) + getAttemptNumber();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentSpecProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentSpecProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getProcessorDescriptorFieldBuilder();
                    getInputSpecsFieldBuilder();
                    getOutputSpecsFieldBuilder();
                    getGroupedInputSpecsFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                fragmentIdentifierString_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                dagName_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                dagId_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                vertexName_ = "";
                bitField0_ = (bitField0_ & ~0x00000008);
                if (processorDescriptorBuilder_ == null) {
                    processorDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                } else {
                    processorDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000010);
                if (inputSpecsBuilder_ == null) {
                    inputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000020);
                } else {
                    inputSpecsBuilder_.clear();
                }
                if (outputSpecsBuilder_ == null) {
                    outputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000040);
                } else {
                    outputSpecsBuilder_.clear();
                }
                if (groupedInputSpecsBuilder_ == null) {
                    groupedInputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000080);
                } else {
                    groupedInputSpecsBuilder_.clear();
                }
                vertexParallelism_ = 0;
                bitField0_ = (bitField0_ & ~0x00000100);
                fragmentNumber_ = 0;
                bitField0_ = (bitField0_ & ~0x00000200);
                attemptNumber_ = 0;
                bitField0_ = (bitField0_ & ~0x00000400);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentSpecProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.fragmentIdentifierString_ = fragmentIdentifierString_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.dagName_ = dagName_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.dagId_ = dagId_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.vertexName_ = vertexName_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                if (processorDescriptorBuilder_ == null) {
                    result.processorDescriptor_ = processorDescriptor_;
                } else {
                    result.processorDescriptor_ = processorDescriptorBuilder_.build();
                }
                if (inputSpecsBuilder_ == null) {
                    if (((bitField0_ & 0x00000020) == 0x00000020)) {
                        inputSpecs_ = java.util.Collections.unmodifiableList(inputSpecs_);
                        bitField0_ = (bitField0_ & ~0x00000020);
                    }
                    result.inputSpecs_ = inputSpecs_;
                } else {
                    result.inputSpecs_ = inputSpecsBuilder_.build();
                }
                if (outputSpecsBuilder_ == null) {
                    if (((bitField0_ & 0x00000040) == 0x00000040)) {
                        outputSpecs_ = java.util.Collections.unmodifiableList(outputSpecs_);
                        bitField0_ = (bitField0_ & ~0x00000040);
                    }
                    result.outputSpecs_ = outputSpecs_;
                } else {
                    result.outputSpecs_ = outputSpecsBuilder_.build();
                }
                if (groupedInputSpecsBuilder_ == null) {
                    if (((bitField0_ & 0x00000080) == 0x00000080)) {
                        groupedInputSpecs_ = java.util.Collections.unmodifiableList(groupedInputSpecs_);
                        bitField0_ = (bitField0_ & ~0x00000080);
                    }
                    result.groupedInputSpecs_ = groupedInputSpecs_;
                } else {
                    result.groupedInputSpecs_ = groupedInputSpecsBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.vertexParallelism_ = vertexParallelism_;
                if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
                    to_bitField0_ |= 0x00000040;
                }
                result.fragmentNumber_ = fragmentNumber_;
                if (((from_bitField0_ & 0x00000400) == 0x00000400)) {
                    to_bitField0_ |= 0x00000080;
                }
                result.attemptNumber_ = attemptNumber_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance())
                    return this;
                if (other.hasFragmentIdentifierString()) {
                    bitField0_ |= 0x00000001;
                    fragmentIdentifierString_ = other.fragmentIdentifierString_;
                    onChanged();
                }
                if (other.hasDagName()) {
                    bitField0_ |= 0x00000002;
                    dagName_ = other.dagName_;
                    onChanged();
                }
                if (other.hasDagId()) {
                    setDagId(other.getDagId());
                }
                if (other.hasVertexName()) {
                    bitField0_ |= 0x00000008;
                    vertexName_ = other.vertexName_;
                    onChanged();
                }
                if (other.hasProcessorDescriptor()) {
                    mergeProcessorDescriptor(other.getProcessorDescriptor());
                }
                if (inputSpecsBuilder_ == null) {
                    if (!other.inputSpecs_.isEmpty()) {
                        if (inputSpecs_.isEmpty()) {
                            inputSpecs_ = other.inputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000020);
                        } else {
                            ensureInputSpecsIsMutable();
                            inputSpecs_.addAll(other.inputSpecs_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.inputSpecs_.isEmpty()) {
                        if (inputSpecsBuilder_.isEmpty()) {
                            inputSpecsBuilder_.dispose();
                            inputSpecsBuilder_ = null;
                            inputSpecs_ = other.inputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000020);
                            inputSpecsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getInputSpecsFieldBuilder() : null;
                        } else {
                            inputSpecsBuilder_.addAllMessages(other.inputSpecs_);
                        }
                    }
                }
                if (outputSpecsBuilder_ == null) {
                    if (!other.outputSpecs_.isEmpty()) {
                        if (outputSpecs_.isEmpty()) {
                            outputSpecs_ = other.outputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000040);
                        } else {
                            ensureOutputSpecsIsMutable();
                            outputSpecs_.addAll(other.outputSpecs_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.outputSpecs_.isEmpty()) {
                        if (outputSpecsBuilder_.isEmpty()) {
                            outputSpecsBuilder_.dispose();
                            outputSpecsBuilder_ = null;
                            outputSpecs_ = other.outputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000040);
                            outputSpecsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getOutputSpecsFieldBuilder() : null;
                        } else {
                            outputSpecsBuilder_.addAllMessages(other.outputSpecs_);
                        }
                    }
                }
                if (groupedInputSpecsBuilder_ == null) {
                    if (!other.groupedInputSpecs_.isEmpty()) {
                        if (groupedInputSpecs_.isEmpty()) {
                            groupedInputSpecs_ = other.groupedInputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000080);
                        } else {
                            ensureGroupedInputSpecsIsMutable();
                            groupedInputSpecs_.addAll(other.groupedInputSpecs_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.groupedInputSpecs_.isEmpty()) {
                        if (groupedInputSpecsBuilder_.isEmpty()) {
                            groupedInputSpecsBuilder_.dispose();
                            groupedInputSpecsBuilder_ = null;
                            groupedInputSpecs_ = other.groupedInputSpecs_;
                            bitField0_ = (bitField0_ & ~0x00000080);
                            groupedInputSpecsBuilder_ = com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders ? getGroupedInputSpecsFieldBuilder() : null;
                        } else {
                            groupedInputSpecsBuilder_.addAllMessages(other.groupedInputSpecs_);
                        }
                    }
                }
                if (other.hasVertexParallelism()) {
                    setVertexParallelism(other.getVertexParallelism());
                }
                if (other.hasFragmentNumber()) {
                    setFragmentNumber(other.getFragmentNumber());
                }
                if (other.hasAttemptNumber()) {
                    setAttemptNumber(other.getAttemptNumber());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object fragmentIdentifierString_ = "";

            public boolean hasFragmentIdentifierString() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getFragmentIdentifierString() {
                java.lang.Object ref = fragmentIdentifierString_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    fragmentIdentifierString_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getFragmentIdentifierStringBytes() {
                java.lang.Object ref = fragmentIdentifierString_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    fragmentIdentifierString_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setFragmentIdentifierString(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                fragmentIdentifierString_ = value;
                onChanged();
                return this;
            }

            public Builder clearFragmentIdentifierString() {
                bitField0_ = (bitField0_ & ~0x00000001);
                fragmentIdentifierString_ = getDefaultInstance().getFragmentIdentifierString();
                onChanged();
                return this;
            }

            public Builder setFragmentIdentifierStringBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                fragmentIdentifierString_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object dagName_ = "";

            public boolean hasDagName() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public java.lang.String getDagName() {
                java.lang.Object ref = dagName_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    dagName_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getDagNameBytes() {
                java.lang.Object ref = dagName_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    dagName_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setDagName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                dagName_ = value;
                onChanged();
                return this;
            }

            public Builder clearDagName() {
                bitField0_ = (bitField0_ & ~0x00000002);
                dagName_ = getDefaultInstance().getDagName();
                onChanged();
                return this;
            }

            public Builder setDagNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                dagName_ = value;
                onChanged();
                return this;
            }

            private int dagId_;

            public boolean hasDagId() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getDagId() {
                return dagId_;
            }

            public Builder setDagId(int value) {
                bitField0_ |= 0x00000004;
                dagId_ = value;
                onChanged();
                return this;
            }

            public Builder clearDagId() {
                bitField0_ = (bitField0_ & ~0x00000004);
                dagId_ = 0;
                onChanged();
                return this;
            }

            private java.lang.Object vertexName_ = "";

            public boolean hasVertexName() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public java.lang.String getVertexName() {
                java.lang.Object ref = vertexName_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    vertexName_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getVertexNameBytes() {
                java.lang.Object ref = vertexName_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    vertexName_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setVertexName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                vertexName_ = value;
                onChanged();
                return this;
            }

            public Builder clearVertexName() {
                bitField0_ = (bitField0_ & ~0x00000008);
                vertexName_ = getDefaultInstance().getVertexName();
                onChanged();
                return this;
            }

            public Builder setVertexNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                vertexName_ = value;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto processorDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> processorDescriptorBuilder_;

            public boolean hasProcessorDescriptor() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto getProcessorDescriptor() {
                if (processorDescriptorBuilder_ == null) {
                    return processorDescriptor_;
                } else {
                    return processorDescriptorBuilder_.getMessage();
                }
            }

            public Builder setProcessorDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (processorDescriptorBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    processorDescriptor_ = value;
                    onChanged();
                } else {
                    processorDescriptorBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000010;
                return this;
            }

            public Builder setProcessorDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder builderForValue) {
                if (processorDescriptorBuilder_ == null) {
                    processorDescriptor_ = builderForValue.build();
                    onChanged();
                } else {
                    processorDescriptorBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000010;
                return this;
            }

            public Builder mergeProcessorDescriptor(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto value) {
                if (processorDescriptorBuilder_ == null) {
                    if (((bitField0_ & 0x00000010) == 0x00000010) && processorDescriptor_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance()) {
                        processorDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.newBuilder(processorDescriptor_).mergeFrom(value).buildPartial();
                    } else {
                        processorDescriptor_ = value;
                    }
                    onChanged();
                } else {
                    processorDescriptorBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000010;
                return this;
            }

            public Builder clearProcessorDescriptor() {
                if (processorDescriptorBuilder_ == null) {
                    processorDescriptor_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.getDefaultInstance();
                    onChanged();
                } else {
                    processorDescriptorBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000010);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder getProcessorDescriptorBuilder() {
                bitField0_ |= 0x00000010;
                onChanged();
                return getProcessorDescriptorFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder getProcessorDescriptorOrBuilder() {
                if (processorDescriptorBuilder_ != null) {
                    return processorDescriptorBuilder_.getMessageOrBuilder();
                } else {
                    return processorDescriptor_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder> getProcessorDescriptorFieldBuilder() {
                if (processorDescriptorBuilder_ == null) {
                    processorDescriptorBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.EntityDescriptorProtoOrBuilder>(processorDescriptor_, getParentForChildren(), isClean());
                    processorDescriptor_ = null;
                }
                return processorDescriptorBuilder_;
            }

            private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> inputSpecs_ = java.util.Collections.emptyList();

            private void ensureInputSpecsIsMutable() {
                if (!((bitField0_ & 0x00000020) == 0x00000020)) {
                    inputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto>(inputSpecs_);
                    bitField0_ |= 0x00000020;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> inputSpecsBuilder_;

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getInputSpecsList() {
                if (inputSpecsBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(inputSpecs_);
                } else {
                    return inputSpecsBuilder_.getMessageList();
                }
            }

            public int getInputSpecsCount() {
                if (inputSpecsBuilder_ == null) {
                    return inputSpecs_.size();
                } else {
                    return inputSpecsBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getInputSpecs(int index) {
                if (inputSpecsBuilder_ == null) {
                    return inputSpecs_.get(index);
                } else {
                    return inputSpecsBuilder_.getMessage(index);
                }
            }

            public Builder setInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (inputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInputSpecsIsMutable();
                    inputSpecs_.set(index, value);
                    onChanged();
                } else {
                    inputSpecsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (inputSpecsBuilder_ == null) {
                    ensureInputSpecsIsMutable();
                    inputSpecs_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    inputSpecsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addInputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (inputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInputSpecsIsMutable();
                    inputSpecs_.add(value);
                    onChanged();
                } else {
                    inputSpecsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (inputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureInputSpecsIsMutable();
                    inputSpecs_.add(index, value);
                    onChanged();
                } else {
                    inputSpecsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addInputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (inputSpecsBuilder_ == null) {
                    ensureInputSpecsIsMutable();
                    inputSpecs_.add(builderForValue.build());
                    onChanged();
                } else {
                    inputSpecsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (inputSpecsBuilder_ == null) {
                    ensureInputSpecsIsMutable();
                    inputSpecs_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    inputSpecsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllInputSpecs(java.lang.Iterable<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> values) {
                if (inputSpecsBuilder_ == null) {
                    ensureInputSpecsIsMutable();
                    super.addAll(values, inputSpecs_);
                    onChanged();
                } else {
                    inputSpecsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearInputSpecs() {
                if (inputSpecsBuilder_ == null) {
                    inputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000020);
                    onChanged();
                } else {
                    inputSpecsBuilder_.clear();
                }
                return this;
            }

            public Builder removeInputSpecs(int index) {
                if (inputSpecsBuilder_ == null) {
                    ensureInputSpecsIsMutable();
                    inputSpecs_.remove(index);
                    onChanged();
                } else {
                    inputSpecsBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder getInputSpecsBuilder(int index) {
                return getInputSpecsFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getInputSpecsOrBuilder(int index) {
                if (inputSpecsBuilder_ == null) {
                    return inputSpecs_.get(index);
                } else {
                    return inputSpecsBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getInputSpecsOrBuilderList() {
                if (inputSpecsBuilder_ != null) {
                    return inputSpecsBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(inputSpecs_);
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder addInputSpecsBuilder() {
                return getInputSpecsFieldBuilder().addBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder addInputSpecsBuilder(int index) {
                return getInputSpecsFieldBuilder().addBuilder(index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder> getInputSpecsBuilderList() {
                return getInputSpecsFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getInputSpecsFieldBuilder() {
                if (inputSpecsBuilder_ == null) {
                    inputSpecsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder>(inputSpecs_, ((bitField0_ & 0x00000020) == 0x00000020), getParentForChildren(), isClean());
                    inputSpecs_ = null;
                }
                return inputSpecsBuilder_;
            }

            private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> outputSpecs_ = java.util.Collections.emptyList();

            private void ensureOutputSpecsIsMutable() {
                if (!((bitField0_ & 0x00000040) == 0x00000040)) {
                    outputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto>(outputSpecs_);
                    bitField0_ |= 0x00000040;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> outputSpecsBuilder_;

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> getOutputSpecsList() {
                if (outputSpecsBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(outputSpecs_);
                } else {
                    return outputSpecsBuilder_.getMessageList();
                }
            }

            public int getOutputSpecsCount() {
                if (outputSpecsBuilder_ == null) {
                    return outputSpecs_.size();
                } else {
                    return outputSpecsBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto getOutputSpecs(int index) {
                if (outputSpecsBuilder_ == null) {
                    return outputSpecs_.get(index);
                } else {
                    return outputSpecsBuilder_.getMessage(index);
                }
            }

            public Builder setOutputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (outputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.set(index, value);
                    onChanged();
                } else {
                    outputSpecsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setOutputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (outputSpecsBuilder_ == null) {
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    outputSpecsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addOutputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (outputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.add(value);
                    onChanged();
                } else {
                    outputSpecsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addOutputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto value) {
                if (outputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.add(index, value);
                    onChanged();
                } else {
                    outputSpecsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addOutputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (outputSpecsBuilder_ == null) {
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.add(builderForValue.build());
                    onChanged();
                } else {
                    outputSpecsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addOutputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder builderForValue) {
                if (outputSpecsBuilder_ == null) {
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    outputSpecsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllOutputSpecs(java.lang.Iterable<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto> values) {
                if (outputSpecsBuilder_ == null) {
                    ensureOutputSpecsIsMutable();
                    super.addAll(values, outputSpecs_);
                    onChanged();
                } else {
                    outputSpecsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearOutputSpecs() {
                if (outputSpecsBuilder_ == null) {
                    outputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000040);
                    onChanged();
                } else {
                    outputSpecsBuilder_.clear();
                }
                return this;
            }

            public Builder removeOutputSpecs(int index) {
                if (outputSpecsBuilder_ == null) {
                    ensureOutputSpecsIsMutable();
                    outputSpecs_.remove(index);
                    onChanged();
                } else {
                    outputSpecsBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder getOutputSpecsBuilder(int index) {
                return getOutputSpecsFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder getOutputSpecsOrBuilder(int index) {
                if (outputSpecsBuilder_ == null) {
                    return outputSpecs_.get(index);
                } else {
                    return outputSpecsBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getOutputSpecsOrBuilderList() {
                if (outputSpecsBuilder_ != null) {
                    return outputSpecsBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(outputSpecs_);
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder addOutputSpecsBuilder() {
                return getOutputSpecsFieldBuilder().addBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder addOutputSpecsBuilder(int index) {
                return getOutputSpecsFieldBuilder().addBuilder(index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder> getOutputSpecsBuilderList() {
                return getOutputSpecsFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder> getOutputSpecsFieldBuilder() {
                if (outputSpecsBuilder_ == null) {
                    outputSpecsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.IOSpecProtoOrBuilder>(outputSpecs_, ((bitField0_ & 0x00000040) == 0x00000040), getParentForChildren(), isClean());
                    outputSpecs_ = null;
                }
                return outputSpecsBuilder_;
            }

            private java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> groupedInputSpecs_ = java.util.Collections.emptyList();

            private void ensureGroupedInputSpecsIsMutable() {
                if (!((bitField0_ & 0x00000080) == 0x00000080)) {
                    groupedInputSpecs_ = new java.util.ArrayList<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto>(groupedInputSpecs_);
                    bitField0_ |= 0x00000080;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder> groupedInputSpecsBuilder_;

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> getGroupedInputSpecsList() {
                if (groupedInputSpecsBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(groupedInputSpecs_);
                } else {
                    return groupedInputSpecsBuilder_.getMessageList();
                }
            }

            public int getGroupedInputSpecsCount() {
                if (groupedInputSpecsBuilder_ == null) {
                    return groupedInputSpecs_.size();
                } else {
                    return groupedInputSpecsBuilder_.getCount();
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto getGroupedInputSpecs(int index) {
                if (groupedInputSpecsBuilder_ == null) {
                    return groupedInputSpecs_.get(index);
                } else {
                    return groupedInputSpecsBuilder_.getMessage(index);
                }
            }

            public Builder setGroupedInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto value) {
                if (groupedInputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.set(index, value);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.setMessage(index, value);
                }
                return this;
            }

            public Builder setGroupedInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder builderForValue) {
                if (groupedInputSpecsBuilder_ == null) {
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addGroupedInputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto value) {
                if (groupedInputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.add(value);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.addMessage(value);
                }
                return this;
            }

            public Builder addGroupedInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto value) {
                if (groupedInputSpecsBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.add(index, value);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.addMessage(index, value);
                }
                return this;
            }

            public Builder addGroupedInputSpecs(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder builderForValue) {
                if (groupedInputSpecsBuilder_ == null) {
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.add(builderForValue.build());
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            public Builder addGroupedInputSpecs(int index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder builderForValue) {
                if (groupedInputSpecsBuilder_ == null) {
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            public Builder addAllGroupedInputSpecs(java.lang.Iterable<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto> values) {
                if (groupedInputSpecsBuilder_ == null) {
                    ensureGroupedInputSpecsIsMutable();
                    super.addAll(values, groupedInputSpecs_);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.addAllMessages(values);
                }
                return this;
            }

            public Builder clearGroupedInputSpecs() {
                if (groupedInputSpecsBuilder_ == null) {
                    groupedInputSpecs_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000080);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.clear();
                }
                return this;
            }

            public Builder removeGroupedInputSpecs(int index) {
                if (groupedInputSpecsBuilder_ == null) {
                    ensureGroupedInputSpecsIsMutable();
                    groupedInputSpecs_.remove(index);
                    onChanged();
                } else {
                    groupedInputSpecsBuilder_.remove(index);
                }
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder getGroupedInputSpecsBuilder(int index) {
                return getGroupedInputSpecsFieldBuilder().getBuilder(index);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder getGroupedInputSpecsOrBuilder(int index) {
                if (groupedInputSpecsBuilder_ == null) {
                    return groupedInputSpecs_.get(index);
                } else {
                    return groupedInputSpecsBuilder_.getMessageOrBuilder(index);
                }
            }

            public java.util.List<? extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder> getGroupedInputSpecsOrBuilderList() {
                if (groupedInputSpecsBuilder_ != null) {
                    return groupedInputSpecsBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(groupedInputSpecs_);
                }
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder addGroupedInputSpecsBuilder() {
                return getGroupedInputSpecsFieldBuilder().addBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder addGroupedInputSpecsBuilder(int index) {
                return getGroupedInputSpecsFieldBuilder().addBuilder(index, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.getDefaultInstance());
            }

            public java.util.List<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder> getGroupedInputSpecsBuilderList() {
                return getGroupedInputSpecsFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder> getGroupedInputSpecsFieldBuilder() {
                if (groupedInputSpecsBuilder_ == null) {
                    groupedInputSpecsBuilder_ = new com.google.protobuf.RepeatedFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GroupInputSpecProtoOrBuilder>(groupedInputSpecs_, ((bitField0_ & 0x00000080) == 0x00000080), getParentForChildren(), isClean());
                    groupedInputSpecs_ = null;
                }
                return groupedInputSpecsBuilder_;
            }

            private int vertexParallelism_;

            public boolean hasVertexParallelism() {
                return ((bitField0_ & 0x00000100) == 0x00000100);
            }

            public int getVertexParallelism() {
                return vertexParallelism_;
            }

            public Builder setVertexParallelism(int value) {
                bitField0_ |= 0x00000100;
                vertexParallelism_ = value;
                onChanged();
                return this;
            }

            public Builder clearVertexParallelism() {
                bitField0_ = (bitField0_ & ~0x00000100);
                vertexParallelism_ = 0;
                onChanged();
                return this;
            }

            private int fragmentNumber_;

            public boolean hasFragmentNumber() {
                return ((bitField0_ & 0x00000200) == 0x00000200);
            }

            public int getFragmentNumber() {
                return fragmentNumber_;
            }

            public Builder setFragmentNumber(int value) {
                bitField0_ |= 0x00000200;
                fragmentNumber_ = value;
                onChanged();
                return this;
            }

            public Builder clearFragmentNumber() {
                bitField0_ = (bitField0_ & ~0x00000200);
                fragmentNumber_ = 0;
                onChanged();
                return this;
            }

            private int attemptNumber_;

            public boolean hasAttemptNumber() {
                return ((bitField0_ & 0x00000400) == 0x00000400);
            }

            public int getAttemptNumber() {
                return attemptNumber_;
            }

            public Builder setAttemptNumber(int value) {
                bitField0_ |= 0x00000400;
                attemptNumber_ = value;
                onChanged();
                return this;
            }

            public Builder clearAttemptNumber() {
                bitField0_ = (bitField0_ & ~0x00000400);
                attemptNumber_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new FragmentSpecProto(true);
            defaultInstance.initFields();
        }
    }

    public interface FragmentRuntimeInfoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasNumSelfAndUpstreamTasks();

        int getNumSelfAndUpstreamTasks();

        boolean hasNumSelfAndUpstreamCompletedTasks();

        int getNumSelfAndUpstreamCompletedTasks();

        boolean hasWithinDagPriority();

        int getWithinDagPriority();

        boolean hasDagStartTime();

        long getDagStartTime();

        boolean hasFirstAttemptStartTime();

        long getFirstAttemptStartTime();

        boolean hasCurrentAttemptStartTime();

        long getCurrentAttemptStartTime();
    }

    public static final class FragmentRuntimeInfo extends com.google.protobuf.GeneratedMessage implements FragmentRuntimeInfoOrBuilder {

        private FragmentRuntimeInfo(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private FragmentRuntimeInfo(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final FragmentRuntimeInfo defaultInstance;

        public static FragmentRuntimeInfo getDefaultInstance() {
            return defaultInstance;
        }

        public FragmentRuntimeInfo getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private FragmentRuntimeInfo(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 8:
                            {
                                bitField0_ |= 0x00000001;
                                numSelfAndUpstreamTasks_ = input.readInt32();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                numSelfAndUpstreamCompletedTasks_ = input.readInt32();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                withinDagPriority_ = input.readInt32();
                                break;
                            }
                        case 32:
                            {
                                bitField0_ |= 0x00000008;
                                dagStartTime_ = input.readInt64();
                                break;
                            }
                        case 40:
                            {
                                bitField0_ |= 0x00000010;
                                firstAttemptStartTime_ = input.readInt64();
                                break;
                            }
                        case 48:
                            {
                                bitField0_ |= 0x00000020;
                                currentAttemptStartTime_ = input.readInt64();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentRuntimeInfo_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentRuntimeInfo_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder.class);
        }

        public static com.google.protobuf.Parser<FragmentRuntimeInfo> PARSER = new com.google.protobuf.AbstractParser<FragmentRuntimeInfo>() {

            public FragmentRuntimeInfo parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new FragmentRuntimeInfo(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<FragmentRuntimeInfo> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int NUM_SELF_AND_UPSTREAM_TASKS_FIELD_NUMBER = 1;

        private int numSelfAndUpstreamTasks_;

        public boolean hasNumSelfAndUpstreamTasks() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public int getNumSelfAndUpstreamTasks() {
            return numSelfAndUpstreamTasks_;
        }

        public static final int NUM_SELF_AND_UPSTREAM_COMPLETED_TASKS_FIELD_NUMBER = 2;

        private int numSelfAndUpstreamCompletedTasks_;

        public boolean hasNumSelfAndUpstreamCompletedTasks() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getNumSelfAndUpstreamCompletedTasks() {
            return numSelfAndUpstreamCompletedTasks_;
        }

        public static final int WITHIN_DAG_PRIORITY_FIELD_NUMBER = 3;

        private int withinDagPriority_;

        public boolean hasWithinDagPriority() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getWithinDagPriority() {
            return withinDagPriority_;
        }

        public static final int DAG_START_TIME_FIELD_NUMBER = 4;

        private long dagStartTime_;

        public boolean hasDagStartTime() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public long getDagStartTime() {
            return dagStartTime_;
        }

        public static final int FIRST_ATTEMPT_START_TIME_FIELD_NUMBER = 5;

        private long firstAttemptStartTime_;

        public boolean hasFirstAttemptStartTime() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public long getFirstAttemptStartTime() {
            return firstAttemptStartTime_;
        }

        public static final int CURRENT_ATTEMPT_START_TIME_FIELD_NUMBER = 6;

        private long currentAttemptStartTime_;

        public boolean hasCurrentAttemptStartTime() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public long getCurrentAttemptStartTime() {
            return currentAttemptStartTime_;
        }

        private void initFields() {
            numSelfAndUpstreamTasks_ = 0;
            numSelfAndUpstreamCompletedTasks_ = 0;
            withinDagPriority_ = 0;
            dagStartTime_ = 0L;
            firstAttemptStartTime_ = 0L;
            currentAttemptStartTime_ = 0L;
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
                output.writeInt32(1, numSelfAndUpstreamTasks_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt32(2, numSelfAndUpstreamCompletedTasks_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(3, withinDagPriority_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeInt64(4, dagStartTime_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeInt64(5, firstAttemptStartTime_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeInt64(6, currentAttemptStartTime_);
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
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, numSelfAndUpstreamTasks_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, numSelfAndUpstreamCompletedTasks_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, withinDagPriority_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(4, dagStartTime_);
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(5, firstAttemptStartTime_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(6, currentAttemptStartTime_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo) obj;
            boolean result = true;
            result = result && (hasNumSelfAndUpstreamTasks() == other.hasNumSelfAndUpstreamTasks());
            if (hasNumSelfAndUpstreamTasks()) {
                result = result && (getNumSelfAndUpstreamTasks() == other.getNumSelfAndUpstreamTasks());
            }
            result = result && (hasNumSelfAndUpstreamCompletedTasks() == other.hasNumSelfAndUpstreamCompletedTasks());
            if (hasNumSelfAndUpstreamCompletedTasks()) {
                result = result && (getNumSelfAndUpstreamCompletedTasks() == other.getNumSelfAndUpstreamCompletedTasks());
            }
            result = result && (hasWithinDagPriority() == other.hasWithinDagPriority());
            if (hasWithinDagPriority()) {
                result = result && (getWithinDagPriority() == other.getWithinDagPriority());
            }
            result = result && (hasDagStartTime() == other.hasDagStartTime());
            if (hasDagStartTime()) {
                result = result && (getDagStartTime() == other.getDagStartTime());
            }
            result = result && (hasFirstAttemptStartTime() == other.hasFirstAttemptStartTime());
            if (hasFirstAttemptStartTime()) {
                result = result && (getFirstAttemptStartTime() == other.getFirstAttemptStartTime());
            }
            result = result && (hasCurrentAttemptStartTime() == other.hasCurrentAttemptStartTime());
            if (hasCurrentAttemptStartTime()) {
                result = result && (getCurrentAttemptStartTime() == other.getCurrentAttemptStartTime());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasNumSelfAndUpstreamTasks()) {
                hash = (37 * hash) + NUM_SELF_AND_UPSTREAM_TASKS_FIELD_NUMBER;
                hash = (53 * hash) + getNumSelfAndUpstreamTasks();
            }
            if (hasNumSelfAndUpstreamCompletedTasks()) {
                hash = (37 * hash) + NUM_SELF_AND_UPSTREAM_COMPLETED_TASKS_FIELD_NUMBER;
                hash = (53 * hash) + getNumSelfAndUpstreamCompletedTasks();
            }
            if (hasWithinDagPriority()) {
                hash = (37 * hash) + WITHIN_DAG_PRIORITY_FIELD_NUMBER;
                hash = (53 * hash) + getWithinDagPriority();
            }
            if (hasDagStartTime()) {
                hash = (37 * hash) + DAG_START_TIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getDagStartTime());
            }
            if (hasFirstAttemptStartTime()) {
                hash = (37 * hash) + FIRST_ATTEMPT_START_TIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getFirstAttemptStartTime());
            }
            if (hasCurrentAttemptStartTime()) {
                hash = (37 * hash) + CURRENT_ATTEMPT_START_TIME_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getCurrentAttemptStartTime());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentRuntimeInfo_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentRuntimeInfo_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                numSelfAndUpstreamTasks_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                numSelfAndUpstreamCompletedTasks_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                withinDagPriority_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                dagStartTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000008);
                firstAttemptStartTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000010);
                currentAttemptStartTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000020);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_FragmentRuntimeInfo_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.numSelfAndUpstreamTasks_ = numSelfAndUpstreamTasks_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.numSelfAndUpstreamCompletedTasks_ = numSelfAndUpstreamCompletedTasks_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.withinDagPriority_ = withinDagPriority_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.dagStartTime_ = dagStartTime_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.firstAttemptStartTime_ = firstAttemptStartTime_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.currentAttemptStartTime_ = currentAttemptStartTime_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance())
                    return this;
                if (other.hasNumSelfAndUpstreamTasks()) {
                    setNumSelfAndUpstreamTasks(other.getNumSelfAndUpstreamTasks());
                }
                if (other.hasNumSelfAndUpstreamCompletedTasks()) {
                    setNumSelfAndUpstreamCompletedTasks(other.getNumSelfAndUpstreamCompletedTasks());
                }
                if (other.hasWithinDagPriority()) {
                    setWithinDagPriority(other.getWithinDagPriority());
                }
                if (other.hasDagStartTime()) {
                    setDagStartTime(other.getDagStartTime());
                }
                if (other.hasFirstAttemptStartTime()) {
                    setFirstAttemptStartTime(other.getFirstAttemptStartTime());
                }
                if (other.hasCurrentAttemptStartTime()) {
                    setCurrentAttemptStartTime(other.getCurrentAttemptStartTime());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private int numSelfAndUpstreamTasks_;

            public boolean hasNumSelfAndUpstreamTasks() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public int getNumSelfAndUpstreamTasks() {
                return numSelfAndUpstreamTasks_;
            }

            public Builder setNumSelfAndUpstreamTasks(int value) {
                bitField0_ |= 0x00000001;
                numSelfAndUpstreamTasks_ = value;
                onChanged();
                return this;
            }

            public Builder clearNumSelfAndUpstreamTasks() {
                bitField0_ = (bitField0_ & ~0x00000001);
                numSelfAndUpstreamTasks_ = 0;
                onChanged();
                return this;
            }

            private int numSelfAndUpstreamCompletedTasks_;

            public boolean hasNumSelfAndUpstreamCompletedTasks() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getNumSelfAndUpstreamCompletedTasks() {
                return numSelfAndUpstreamCompletedTasks_;
            }

            public Builder setNumSelfAndUpstreamCompletedTasks(int value) {
                bitField0_ |= 0x00000002;
                numSelfAndUpstreamCompletedTasks_ = value;
                onChanged();
                return this;
            }

            public Builder clearNumSelfAndUpstreamCompletedTasks() {
                bitField0_ = (bitField0_ & ~0x00000002);
                numSelfAndUpstreamCompletedTasks_ = 0;
                onChanged();
                return this;
            }

            private int withinDagPriority_;

            public boolean hasWithinDagPriority() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getWithinDagPriority() {
                return withinDagPriority_;
            }

            public Builder setWithinDagPriority(int value) {
                bitField0_ |= 0x00000004;
                withinDagPriority_ = value;
                onChanged();
                return this;
            }

            public Builder clearWithinDagPriority() {
                bitField0_ = (bitField0_ & ~0x00000004);
                withinDagPriority_ = 0;
                onChanged();
                return this;
            }

            private long dagStartTime_;

            public boolean hasDagStartTime() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public long getDagStartTime() {
                return dagStartTime_;
            }

            public Builder setDagStartTime(long value) {
                bitField0_ |= 0x00000008;
                dagStartTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearDagStartTime() {
                bitField0_ = (bitField0_ & ~0x00000008);
                dagStartTime_ = 0L;
                onChanged();
                return this;
            }

            private long firstAttemptStartTime_;

            public boolean hasFirstAttemptStartTime() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public long getFirstAttemptStartTime() {
                return firstAttemptStartTime_;
            }

            public Builder setFirstAttemptStartTime(long value) {
                bitField0_ |= 0x00000010;
                firstAttemptStartTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearFirstAttemptStartTime() {
                bitField0_ = (bitField0_ & ~0x00000010);
                firstAttemptStartTime_ = 0L;
                onChanged();
                return this;
            }

            private long currentAttemptStartTime_;

            public boolean hasCurrentAttemptStartTime() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }

            public long getCurrentAttemptStartTime() {
                return currentAttemptStartTime_;
            }

            public Builder setCurrentAttemptStartTime(long value) {
                bitField0_ |= 0x00000020;
                currentAttemptStartTime_ = value;
                onChanged();
                return this;
            }

            public Builder clearCurrentAttemptStartTime() {
                bitField0_ = (bitField0_ & ~0x00000020);
                currentAttemptStartTime_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new FragmentRuntimeInfo(true);
            defaultInstance.initFields();
        }
    }

    public interface QueryIdentifierProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasAppIdentifier();

        java.lang.String getAppIdentifier();

        com.google.protobuf.ByteString getAppIdentifierBytes();

        boolean hasDagIdentifier();

        int getDagIdentifier();
    }

    public static final class QueryIdentifierProto extends com.google.protobuf.GeneratedMessage implements QueryIdentifierProtoOrBuilder {

        private QueryIdentifierProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private QueryIdentifierProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final QueryIdentifierProto defaultInstance;

        public static QueryIdentifierProto getDefaultInstance() {
            return defaultInstance;
        }

        public QueryIdentifierProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private QueryIdentifierProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                appIdentifier_ = input.readBytes();
                                break;
                            }
                        case 16:
                            {
                                bitField0_ |= 0x00000002;
                                dagIdentifier_ = input.readInt32();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryIdentifierProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryIdentifierProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder.class);
        }

        public static com.google.protobuf.Parser<QueryIdentifierProto> PARSER = new com.google.protobuf.AbstractParser<QueryIdentifierProto>() {

            public QueryIdentifierProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new QueryIdentifierProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<QueryIdentifierProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int APP_IDENTIFIER_FIELD_NUMBER = 1;

        private java.lang.Object appIdentifier_;

        public boolean hasAppIdentifier() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getAppIdentifier() {
            java.lang.Object ref = appIdentifier_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    appIdentifier_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getAppIdentifierBytes() {
            java.lang.Object ref = appIdentifier_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                appIdentifier_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int DAG_IDENTIFIER_FIELD_NUMBER = 2;

        private int dagIdentifier_;

        public boolean hasDagIdentifier() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public int getDagIdentifier() {
            return dagIdentifier_;
        }

        private void initFields() {
            appIdentifier_ = "";
            dagIdentifier_ = 0;
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
                output.writeBytes(1, getAppIdentifierBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeInt32(2, dagIdentifier_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getAppIdentifierBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, dagIdentifier_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto) obj;
            boolean result = true;
            result = result && (hasAppIdentifier() == other.hasAppIdentifier());
            if (hasAppIdentifier()) {
                result = result && getAppIdentifier().equals(other.getAppIdentifier());
            }
            result = result && (hasDagIdentifier() == other.hasDagIdentifier());
            if (hasDagIdentifier()) {
                result = result && (getDagIdentifier() == other.getDagIdentifier());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasAppIdentifier()) {
                hash = (37 * hash) + APP_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getAppIdentifier().hashCode();
            }
            if (hasDagIdentifier()) {
                hash = (37 * hash) + DAG_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getDagIdentifier();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryIdentifierProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryIdentifierProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                appIdentifier_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                dagIdentifier_ = 0;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryIdentifierProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.appIdentifier_ = appIdentifier_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.dagIdentifier_ = dagIdentifier_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance())
                    return this;
                if (other.hasAppIdentifier()) {
                    bitField0_ |= 0x00000001;
                    appIdentifier_ = other.appIdentifier_;
                    onChanged();
                }
                if (other.hasDagIdentifier()) {
                    setDagIdentifier(other.getDagIdentifier());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object appIdentifier_ = "";

            public boolean hasAppIdentifier() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getAppIdentifier() {
                java.lang.Object ref = appIdentifier_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    appIdentifier_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getAppIdentifierBytes() {
                java.lang.Object ref = appIdentifier_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    appIdentifier_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setAppIdentifier(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                appIdentifier_ = value;
                onChanged();
                return this;
            }

            public Builder clearAppIdentifier() {
                bitField0_ = (bitField0_ & ~0x00000001);
                appIdentifier_ = getDefaultInstance().getAppIdentifier();
                onChanged();
                return this;
            }

            public Builder setAppIdentifierBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                appIdentifier_ = value;
                onChanged();
                return this;
            }

            private int dagIdentifier_;

            public boolean hasDagIdentifier() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public int getDagIdentifier() {
                return dagIdentifier_;
            }

            public Builder setDagIdentifier(int value) {
                bitField0_ |= 0x00000002;
                dagIdentifier_ = value;
                onChanged();
                return this;
            }

            public Builder clearDagIdentifier() {
                bitField0_ = (bitField0_ & ~0x00000002);
                dagIdentifier_ = 0;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new QueryIdentifierProto(true);
            defaultInstance.initFields();
        }
    }

    public interface SubmitWorkRequestProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasContainerIdString();

        java.lang.String getContainerIdString();

        com.google.protobuf.ByteString getContainerIdStringBytes();

        boolean hasAmHost();

        java.lang.String getAmHost();

        com.google.protobuf.ByteString getAmHostBytes();

        boolean hasAmPort();

        int getAmPort();

        boolean hasTokenIdentifier();

        java.lang.String getTokenIdentifier();

        com.google.protobuf.ByteString getTokenIdentifierBytes();

        boolean hasCredentialsBinary();

        com.google.protobuf.ByteString getCredentialsBinary();

        boolean hasUser();

        java.lang.String getUser();

        com.google.protobuf.ByteString getUserBytes();

        boolean hasApplicationIdString();

        java.lang.String getApplicationIdString();

        com.google.protobuf.ByteString getApplicationIdStringBytes();

        boolean hasAppAttemptNumber();

        int getAppAttemptNumber();

        boolean hasFragmentSpec();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto getFragmentSpec();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder getFragmentSpecOrBuilder();

        boolean hasFragmentRuntimeInfo();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo getFragmentRuntimeInfo();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder getFragmentRuntimeInfoOrBuilder();
    }

    public static final class SubmitWorkRequestProto extends com.google.protobuf.GeneratedMessage implements SubmitWorkRequestProtoOrBuilder {

        private SubmitWorkRequestProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private SubmitWorkRequestProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final SubmitWorkRequestProto defaultInstance;

        public static SubmitWorkRequestProto getDefaultInstance() {
            return defaultInstance;
        }

        public SubmitWorkRequestProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private SubmitWorkRequestProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                containerIdString_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                amHost_ = input.readBytes();
                                break;
                            }
                        case 24:
                            {
                                bitField0_ |= 0x00000004;
                                amPort_ = input.readInt32();
                                break;
                            }
                        case 34:
                            {
                                bitField0_ |= 0x00000008;
                                tokenIdentifier_ = input.readBytes();
                                break;
                            }
                        case 42:
                            {
                                bitField0_ |= 0x00000010;
                                credentialsBinary_ = input.readBytes();
                                break;
                            }
                        case 50:
                            {
                                bitField0_ |= 0x00000020;
                                user_ = input.readBytes();
                                break;
                            }
                        case 58:
                            {
                                bitField0_ |= 0x00000040;
                                applicationIdString_ = input.readBytes();
                                break;
                            }
                        case 64:
                            {
                                bitField0_ |= 0x00000080;
                                appAttemptNumber_ = input.readInt32();
                                break;
                            }
                        case 74:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000100) == 0x00000100)) {
                                    subBuilder = fragmentSpec_.toBuilder();
                                }
                                fragmentSpec_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(fragmentSpec_);
                                    fragmentSpec_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000100;
                                break;
                            }
                        case 82:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000200) == 0x00000200)) {
                                    subBuilder = fragmentRuntimeInfo_.toBuilder();
                                }
                                fragmentRuntimeInfo_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(fragmentRuntimeInfo_);
                                    fragmentRuntimeInfo_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000200;
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkRequestProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.Builder.class);
        }

        public static com.google.protobuf.Parser<SubmitWorkRequestProto> PARSER = new com.google.protobuf.AbstractParser<SubmitWorkRequestProto>() {

            public SubmitWorkRequestProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new SubmitWorkRequestProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<SubmitWorkRequestProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int CONTAINER_ID_STRING_FIELD_NUMBER = 1;

        private java.lang.Object containerIdString_;

        public boolean hasContainerIdString() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getContainerIdString() {
            java.lang.Object ref = containerIdString_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    containerIdString_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getContainerIdStringBytes() {
            java.lang.Object ref = containerIdString_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                containerIdString_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int AM_HOST_FIELD_NUMBER = 2;

        private java.lang.Object amHost_;

        public boolean hasAmHost() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public java.lang.String getAmHost() {
            java.lang.Object ref = amHost_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    amHost_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getAmHostBytes() {
            java.lang.Object ref = amHost_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                amHost_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int AM_PORT_FIELD_NUMBER = 3;

        private int amPort_;

        public boolean hasAmPort() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public int getAmPort() {
            return amPort_;
        }

        public static final int TOKEN_IDENTIFIER_FIELD_NUMBER = 4;

        private java.lang.Object tokenIdentifier_;

        public boolean hasTokenIdentifier() {
            return ((bitField0_ & 0x00000008) == 0x00000008);
        }

        public java.lang.String getTokenIdentifier() {
            java.lang.Object ref = tokenIdentifier_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    tokenIdentifier_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getTokenIdentifierBytes() {
            java.lang.Object ref = tokenIdentifier_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                tokenIdentifier_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int CREDENTIALS_BINARY_FIELD_NUMBER = 5;

        private com.google.protobuf.ByteString credentialsBinary_;

        public boolean hasCredentialsBinary() {
            return ((bitField0_ & 0x00000010) == 0x00000010);
        }

        public com.google.protobuf.ByteString getCredentialsBinary() {
            return credentialsBinary_;
        }

        public static final int USER_FIELD_NUMBER = 6;

        private java.lang.Object user_;

        public boolean hasUser() {
            return ((bitField0_ & 0x00000020) == 0x00000020);
        }

        public java.lang.String getUser() {
            java.lang.Object ref = user_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    user_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getUserBytes() {
            java.lang.Object ref = user_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                user_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int APPLICATION_ID_STRING_FIELD_NUMBER = 7;

        private java.lang.Object applicationIdString_;

        public boolean hasApplicationIdString() {
            return ((bitField0_ & 0x00000040) == 0x00000040);
        }

        public java.lang.String getApplicationIdString() {
            java.lang.Object ref = applicationIdString_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    applicationIdString_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getApplicationIdStringBytes() {
            java.lang.Object ref = applicationIdString_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                applicationIdString_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int APP_ATTEMPT_NUMBER_FIELD_NUMBER = 8;

        private int appAttemptNumber_;

        public boolean hasAppAttemptNumber() {
            return ((bitField0_ & 0x00000080) == 0x00000080);
        }

        public int getAppAttemptNumber() {
            return appAttemptNumber_;
        }

        public static final int FRAGMENT_SPEC_FIELD_NUMBER = 9;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto fragmentSpec_;

        public boolean hasFragmentSpec() {
            return ((bitField0_ & 0x00000100) == 0x00000100);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto getFragmentSpec() {
            return fragmentSpec_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder getFragmentSpecOrBuilder() {
            return fragmentSpec_;
        }

        public static final int FRAGMENT_RUNTIME_INFO_FIELD_NUMBER = 10;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo fragmentRuntimeInfo_;

        public boolean hasFragmentRuntimeInfo() {
            return ((bitField0_ & 0x00000200) == 0x00000200);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo getFragmentRuntimeInfo() {
            return fragmentRuntimeInfo_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder getFragmentRuntimeInfoOrBuilder() {
            return fragmentRuntimeInfo_;
        }

        private void initFields() {
            containerIdString_ = "";
            amHost_ = "";
            amPort_ = 0;
            tokenIdentifier_ = "";
            credentialsBinary_ = com.google.protobuf.ByteString.EMPTY;
            user_ = "";
            applicationIdString_ = "";
            appAttemptNumber_ = 0;
            fragmentSpec_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance();
            fragmentRuntimeInfo_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance();
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
                output.writeBytes(1, getContainerIdStringBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getAmHostBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt32(3, amPort_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                output.writeBytes(4, getTokenIdentifierBytes());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                output.writeBytes(5, credentialsBinary_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                output.writeBytes(6, getUserBytes());
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                output.writeBytes(7, getApplicationIdStringBytes());
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                output.writeInt32(8, appAttemptNumber_);
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                output.writeMessage(9, fragmentSpec_);
            }
            if (((bitField0_ & 0x00000200) == 0x00000200)) {
                output.writeMessage(10, fragmentRuntimeInfo_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getContainerIdStringBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getAmHostBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, amPort_);
            }
            if (((bitField0_ & 0x00000008) == 0x00000008)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(4, getTokenIdentifierBytes());
            }
            if (((bitField0_ & 0x00000010) == 0x00000010)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(5, credentialsBinary_);
            }
            if (((bitField0_ & 0x00000020) == 0x00000020)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(6, getUserBytes());
            }
            if (((bitField0_ & 0x00000040) == 0x00000040)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(7, getApplicationIdStringBytes());
            }
            if (((bitField0_ & 0x00000080) == 0x00000080)) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(8, appAttemptNumber_);
            }
            if (((bitField0_ & 0x00000100) == 0x00000100)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(9, fragmentSpec_);
            }
            if (((bitField0_ & 0x00000200) == 0x00000200)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(10, fragmentRuntimeInfo_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) obj;
            boolean result = true;
            result = result && (hasContainerIdString() == other.hasContainerIdString());
            if (hasContainerIdString()) {
                result = result && getContainerIdString().equals(other.getContainerIdString());
            }
            result = result && (hasAmHost() == other.hasAmHost());
            if (hasAmHost()) {
                result = result && getAmHost().equals(other.getAmHost());
            }
            result = result && (hasAmPort() == other.hasAmPort());
            if (hasAmPort()) {
                result = result && (getAmPort() == other.getAmPort());
            }
            result = result && (hasTokenIdentifier() == other.hasTokenIdentifier());
            if (hasTokenIdentifier()) {
                result = result && getTokenIdentifier().equals(other.getTokenIdentifier());
            }
            result = result && (hasCredentialsBinary() == other.hasCredentialsBinary());
            if (hasCredentialsBinary()) {
                result = result && getCredentialsBinary().equals(other.getCredentialsBinary());
            }
            result = result && (hasUser() == other.hasUser());
            if (hasUser()) {
                result = result && getUser().equals(other.getUser());
            }
            result = result && (hasApplicationIdString() == other.hasApplicationIdString());
            if (hasApplicationIdString()) {
                result = result && getApplicationIdString().equals(other.getApplicationIdString());
            }
            result = result && (hasAppAttemptNumber() == other.hasAppAttemptNumber());
            if (hasAppAttemptNumber()) {
                result = result && (getAppAttemptNumber() == other.getAppAttemptNumber());
            }
            result = result && (hasFragmentSpec() == other.hasFragmentSpec());
            if (hasFragmentSpec()) {
                result = result && getFragmentSpec().equals(other.getFragmentSpec());
            }
            result = result && (hasFragmentRuntimeInfo() == other.hasFragmentRuntimeInfo());
            if (hasFragmentRuntimeInfo()) {
                result = result && getFragmentRuntimeInfo().equals(other.getFragmentRuntimeInfo());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasContainerIdString()) {
                hash = (37 * hash) + CONTAINER_ID_STRING_FIELD_NUMBER;
                hash = (53 * hash) + getContainerIdString().hashCode();
            }
            if (hasAmHost()) {
                hash = (37 * hash) + AM_HOST_FIELD_NUMBER;
                hash = (53 * hash) + getAmHost().hashCode();
            }
            if (hasAmPort()) {
                hash = (37 * hash) + AM_PORT_FIELD_NUMBER;
                hash = (53 * hash) + getAmPort();
            }
            if (hasTokenIdentifier()) {
                hash = (37 * hash) + TOKEN_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getTokenIdentifier().hashCode();
            }
            if (hasCredentialsBinary()) {
                hash = (37 * hash) + CREDENTIALS_BINARY_FIELD_NUMBER;
                hash = (53 * hash) + getCredentialsBinary().hashCode();
            }
            if (hasUser()) {
                hash = (37 * hash) + USER_FIELD_NUMBER;
                hash = (53 * hash) + getUser().hashCode();
            }
            if (hasApplicationIdString()) {
                hash = (37 * hash) + APPLICATION_ID_STRING_FIELD_NUMBER;
                hash = (53 * hash) + getApplicationIdString().hashCode();
            }
            if (hasAppAttemptNumber()) {
                hash = (37 * hash) + APP_ATTEMPT_NUMBER_FIELD_NUMBER;
                hash = (53 * hash) + getAppAttemptNumber();
            }
            if (hasFragmentSpec()) {
                hash = (37 * hash) + FRAGMENT_SPEC_FIELD_NUMBER;
                hash = (53 * hash) + getFragmentSpec().hashCode();
            }
            if (hasFragmentRuntimeInfo()) {
                hash = (37 * hash) + FRAGMENT_RUNTIME_INFO_FIELD_NUMBER;
                hash = (53 * hash) + getFragmentRuntimeInfo().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkRequestProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getFragmentSpecFieldBuilder();
                    getFragmentRuntimeInfoFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                containerIdString_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                amHost_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                amPort_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                tokenIdentifier_ = "";
                bitField0_ = (bitField0_ & ~0x00000008);
                credentialsBinary_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000010);
                user_ = "";
                bitField0_ = (bitField0_ & ~0x00000020);
                applicationIdString_ = "";
                bitField0_ = (bitField0_ & ~0x00000040);
                appAttemptNumber_ = 0;
                bitField0_ = (bitField0_ & ~0x00000080);
                if (fragmentSpecBuilder_ == null) {
                    fragmentSpec_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance();
                } else {
                    fragmentSpecBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000100);
                if (fragmentRuntimeInfoBuilder_ == null) {
                    fragmentRuntimeInfo_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance();
                } else {
                    fragmentRuntimeInfoBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000200);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkRequestProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.containerIdString_ = containerIdString_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.amHost_ = amHost_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.amPort_ = amPort_;
                if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
                    to_bitField0_ |= 0x00000008;
                }
                result.tokenIdentifier_ = tokenIdentifier_;
                if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.credentialsBinary_ = credentialsBinary_;
                if (((from_bitField0_ & 0x00000020) == 0x00000020)) {
                    to_bitField0_ |= 0x00000020;
                }
                result.user_ = user_;
                if (((from_bitField0_ & 0x00000040) == 0x00000040)) {
                    to_bitField0_ |= 0x00000040;
                }
                result.applicationIdString_ = applicationIdString_;
                if (((from_bitField0_ & 0x00000080) == 0x00000080)) {
                    to_bitField0_ |= 0x00000080;
                }
                result.appAttemptNumber_ = appAttemptNumber_;
                if (((from_bitField0_ & 0x00000100) == 0x00000100)) {
                    to_bitField0_ |= 0x00000100;
                }
                if (fragmentSpecBuilder_ == null) {
                    result.fragmentSpec_ = fragmentSpec_;
                } else {
                    result.fragmentSpec_ = fragmentSpecBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000200) == 0x00000200)) {
                    to_bitField0_ |= 0x00000200;
                }
                if (fragmentRuntimeInfoBuilder_ == null) {
                    result.fragmentRuntimeInfo_ = fragmentRuntimeInfo_;
                } else {
                    result.fragmentRuntimeInfo_ = fragmentRuntimeInfoBuilder_.build();
                }
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.getDefaultInstance())
                    return this;
                if (other.hasContainerIdString()) {
                    bitField0_ |= 0x00000001;
                    containerIdString_ = other.containerIdString_;
                    onChanged();
                }
                if (other.hasAmHost()) {
                    bitField0_ |= 0x00000002;
                    amHost_ = other.amHost_;
                    onChanged();
                }
                if (other.hasAmPort()) {
                    setAmPort(other.getAmPort());
                }
                if (other.hasTokenIdentifier()) {
                    bitField0_ |= 0x00000008;
                    tokenIdentifier_ = other.tokenIdentifier_;
                    onChanged();
                }
                if (other.hasCredentialsBinary()) {
                    setCredentialsBinary(other.getCredentialsBinary());
                }
                if (other.hasUser()) {
                    bitField0_ |= 0x00000020;
                    user_ = other.user_;
                    onChanged();
                }
                if (other.hasApplicationIdString()) {
                    bitField0_ |= 0x00000040;
                    applicationIdString_ = other.applicationIdString_;
                    onChanged();
                }
                if (other.hasAppAttemptNumber()) {
                    setAppAttemptNumber(other.getAppAttemptNumber());
                }
                if (other.hasFragmentSpec()) {
                    mergeFragmentSpec(other.getFragmentSpec());
                }
                if (other.hasFragmentRuntimeInfo()) {
                    mergeFragmentRuntimeInfo(other.getFragmentRuntimeInfo());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object containerIdString_ = "";

            public boolean hasContainerIdString() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getContainerIdString() {
                java.lang.Object ref = containerIdString_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    containerIdString_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getContainerIdStringBytes() {
                java.lang.Object ref = containerIdString_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    containerIdString_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setContainerIdString(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                containerIdString_ = value;
                onChanged();
                return this;
            }

            public Builder clearContainerIdString() {
                bitField0_ = (bitField0_ & ~0x00000001);
                containerIdString_ = getDefaultInstance().getContainerIdString();
                onChanged();
                return this;
            }

            public Builder setContainerIdStringBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                containerIdString_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object amHost_ = "";

            public boolean hasAmHost() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public java.lang.String getAmHost() {
                java.lang.Object ref = amHost_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    amHost_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getAmHostBytes() {
                java.lang.Object ref = amHost_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    amHost_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setAmHost(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                amHost_ = value;
                onChanged();
                return this;
            }

            public Builder clearAmHost() {
                bitField0_ = (bitField0_ & ~0x00000002);
                amHost_ = getDefaultInstance().getAmHost();
                onChanged();
                return this;
            }

            public Builder setAmHostBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                amHost_ = value;
                onChanged();
                return this;
            }

            private int amPort_;

            public boolean hasAmPort() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public int getAmPort() {
                return amPort_;
            }

            public Builder setAmPort(int value) {
                bitField0_ |= 0x00000004;
                amPort_ = value;
                onChanged();
                return this;
            }

            public Builder clearAmPort() {
                bitField0_ = (bitField0_ & ~0x00000004);
                amPort_ = 0;
                onChanged();
                return this;
            }

            private java.lang.Object tokenIdentifier_ = "";

            public boolean hasTokenIdentifier() {
                return ((bitField0_ & 0x00000008) == 0x00000008);
            }

            public java.lang.String getTokenIdentifier() {
                java.lang.Object ref = tokenIdentifier_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    tokenIdentifier_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getTokenIdentifierBytes() {
                java.lang.Object ref = tokenIdentifier_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    tokenIdentifier_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setTokenIdentifier(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                tokenIdentifier_ = value;
                onChanged();
                return this;
            }

            public Builder clearTokenIdentifier() {
                bitField0_ = (bitField0_ & ~0x00000008);
                tokenIdentifier_ = getDefaultInstance().getTokenIdentifier();
                onChanged();
                return this;
            }

            public Builder setTokenIdentifierBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000008;
                tokenIdentifier_ = value;
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString credentialsBinary_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasCredentialsBinary() {
                return ((bitField0_ & 0x00000010) == 0x00000010);
            }

            public com.google.protobuf.ByteString getCredentialsBinary() {
                return credentialsBinary_;
            }

            public Builder setCredentialsBinary(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000010;
                credentialsBinary_ = value;
                onChanged();
                return this;
            }

            public Builder clearCredentialsBinary() {
                bitField0_ = (bitField0_ & ~0x00000010);
                credentialsBinary_ = getDefaultInstance().getCredentialsBinary();
                onChanged();
                return this;
            }

            private java.lang.Object user_ = "";

            public boolean hasUser() {
                return ((bitField0_ & 0x00000020) == 0x00000020);
            }

            public java.lang.String getUser() {
                java.lang.Object ref = user_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    user_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getUserBytes() {
                java.lang.Object ref = user_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    user_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setUser(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000020;
                user_ = value;
                onChanged();
                return this;
            }

            public Builder clearUser() {
                bitField0_ = (bitField0_ & ~0x00000020);
                user_ = getDefaultInstance().getUser();
                onChanged();
                return this;
            }

            public Builder setUserBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000020;
                user_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object applicationIdString_ = "";

            public boolean hasApplicationIdString() {
                return ((bitField0_ & 0x00000040) == 0x00000040);
            }

            public java.lang.String getApplicationIdString() {
                java.lang.Object ref = applicationIdString_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    applicationIdString_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getApplicationIdStringBytes() {
                java.lang.Object ref = applicationIdString_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    applicationIdString_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setApplicationIdString(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000040;
                applicationIdString_ = value;
                onChanged();
                return this;
            }

            public Builder clearApplicationIdString() {
                bitField0_ = (bitField0_ & ~0x00000040);
                applicationIdString_ = getDefaultInstance().getApplicationIdString();
                onChanged();
                return this;
            }

            public Builder setApplicationIdStringBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000040;
                applicationIdString_ = value;
                onChanged();
                return this;
            }

            private int appAttemptNumber_;

            public boolean hasAppAttemptNumber() {
                return ((bitField0_ & 0x00000080) == 0x00000080);
            }

            public int getAppAttemptNumber() {
                return appAttemptNumber_;
            }

            public Builder setAppAttemptNumber(int value) {
                bitField0_ |= 0x00000080;
                appAttemptNumber_ = value;
                onChanged();
                return this;
            }

            public Builder clearAppAttemptNumber() {
                bitField0_ = (bitField0_ & ~0x00000080);
                appAttemptNumber_ = 0;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto fragmentSpec_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder> fragmentSpecBuilder_;

            public boolean hasFragmentSpec() {
                return ((bitField0_ & 0x00000100) == 0x00000100);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto getFragmentSpec() {
                if (fragmentSpecBuilder_ == null) {
                    return fragmentSpec_;
                } else {
                    return fragmentSpecBuilder_.getMessage();
                }
            }

            public Builder setFragmentSpec(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto value) {
                if (fragmentSpecBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    fragmentSpec_ = value;
                    onChanged();
                } else {
                    fragmentSpecBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            public Builder setFragmentSpec(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder builderForValue) {
                if (fragmentSpecBuilder_ == null) {
                    fragmentSpec_ = builderForValue.build();
                    onChanged();
                } else {
                    fragmentSpecBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            public Builder mergeFragmentSpec(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto value) {
                if (fragmentSpecBuilder_ == null) {
                    if (((bitField0_ & 0x00000100) == 0x00000100) && fragmentSpec_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance()) {
                        fragmentSpec_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.newBuilder(fragmentSpec_).mergeFrom(value).buildPartial();
                    } else {
                        fragmentSpec_ = value;
                    }
                    onChanged();
                } else {
                    fragmentSpecBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            public Builder clearFragmentSpec() {
                if (fragmentSpecBuilder_ == null) {
                    fragmentSpec_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.getDefaultInstance();
                    onChanged();
                } else {
                    fragmentSpecBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000100);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder getFragmentSpecBuilder() {
                bitField0_ |= 0x00000100;
                onChanged();
                return getFragmentSpecFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder getFragmentSpecOrBuilder() {
                if (fragmentSpecBuilder_ != null) {
                    return fragmentSpecBuilder_.getMessageOrBuilder();
                } else {
                    return fragmentSpec_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder> getFragmentSpecFieldBuilder() {
                if (fragmentSpecBuilder_ == null) {
                    fragmentSpecBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentSpecProtoOrBuilder>(fragmentSpec_, getParentForChildren(), isClean());
                    fragmentSpec_ = null;
                }
                return fragmentSpecBuilder_;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo fragmentRuntimeInfo_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder> fragmentRuntimeInfoBuilder_;

            public boolean hasFragmentRuntimeInfo() {
                return ((bitField0_ & 0x00000200) == 0x00000200);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo getFragmentRuntimeInfo() {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    return fragmentRuntimeInfo_;
                } else {
                    return fragmentRuntimeInfoBuilder_.getMessage();
                }
            }

            public Builder setFragmentRuntimeInfo(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo value) {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    fragmentRuntimeInfo_ = value;
                    onChanged();
                } else {
                    fragmentRuntimeInfoBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000200;
                return this;
            }

            public Builder setFragmentRuntimeInfo(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder builderForValue) {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    fragmentRuntimeInfo_ = builderForValue.build();
                    onChanged();
                } else {
                    fragmentRuntimeInfoBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000200;
                return this;
            }

            public Builder mergeFragmentRuntimeInfo(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo value) {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    if (((bitField0_ & 0x00000200) == 0x00000200) && fragmentRuntimeInfo_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance()) {
                        fragmentRuntimeInfo_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.newBuilder(fragmentRuntimeInfo_).mergeFrom(value).buildPartial();
                    } else {
                        fragmentRuntimeInfo_ = value;
                    }
                    onChanged();
                } else {
                    fragmentRuntimeInfoBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000200;
                return this;
            }

            public Builder clearFragmentRuntimeInfo() {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    fragmentRuntimeInfo_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.getDefaultInstance();
                    onChanged();
                } else {
                    fragmentRuntimeInfoBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000200);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder getFragmentRuntimeInfoBuilder() {
                bitField0_ |= 0x00000200;
                onChanged();
                return getFragmentRuntimeInfoFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder getFragmentRuntimeInfoOrBuilder() {
                if (fragmentRuntimeInfoBuilder_ != null) {
                    return fragmentRuntimeInfoBuilder_.getMessageOrBuilder();
                } else {
                    return fragmentRuntimeInfo_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder> getFragmentRuntimeInfoFieldBuilder() {
                if (fragmentRuntimeInfoBuilder_ == null) {
                    fragmentRuntimeInfoBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfo.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.FragmentRuntimeInfoOrBuilder>(fragmentRuntimeInfo_, getParentForChildren(), isClean());
                    fragmentRuntimeInfo_ = null;
                }
                return fragmentRuntimeInfoBuilder_;
            }
        }

        static {
            defaultInstance = new SubmitWorkRequestProto(true);
            defaultInstance.initFields();
        }
    }

    public interface SubmitWorkResponseProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasSubmissionState();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto getSubmissionState();
    }

    public static final class SubmitWorkResponseProto extends com.google.protobuf.GeneratedMessage implements SubmitWorkResponseProtoOrBuilder {

        private SubmitWorkResponseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private SubmitWorkResponseProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final SubmitWorkResponseProto defaultInstance;

        public static SubmitWorkResponseProto getDefaultInstance() {
            return defaultInstance;
        }

        public SubmitWorkResponseProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private SubmitWorkResponseProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 8:
                            {
                                int rawValue = input.readEnum();
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto value = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(1, rawValue);
                                } else {
                                    bitField0_ |= 0x00000001;
                                    submissionState_ = value;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkResponseProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.Builder.class);
        }

        public static com.google.protobuf.Parser<SubmitWorkResponseProto> PARSER = new com.google.protobuf.AbstractParser<SubmitWorkResponseProto>() {

            public SubmitWorkResponseProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new SubmitWorkResponseProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<SubmitWorkResponseProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int SUBMISSION_STATE_FIELD_NUMBER = 1;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto submissionState_;

        public boolean hasSubmissionState() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto getSubmissionState() {
            return submissionState_;
        }

        private void initFields() {
            submissionState_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto.ACCEPTED;
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
                output.writeEnum(1, submissionState_.getNumber());
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
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, submissionState_.getNumber());
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto) obj;
            boolean result = true;
            result = result && (hasSubmissionState() == other.hasSubmissionState());
            if (hasSubmissionState()) {
                result = result && (getSubmissionState() == other.getSubmissionState());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasSubmissionState()) {
                hash = (37 * hash) + SUBMISSION_STATE_FIELD_NUMBER;
                hash = (53 * hash) + hashEnum(getSubmissionState());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkResponseProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                submissionState_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto.ACCEPTED;
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SubmitWorkResponseProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.submissionState_ = submissionState_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance())
                    return this;
                if (other.hasSubmissionState()) {
                    setSubmissionState(other.getSubmissionState());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto submissionState_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto.ACCEPTED;

            public boolean hasSubmissionState() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto getSubmissionState() {
                return submissionState_;
            }

            public Builder setSubmissionState(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                submissionState_ = value;
                onChanged();
                return this;
            }

            public Builder clearSubmissionState() {
                bitField0_ = (bitField0_ & ~0x00000001);
                submissionState_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmissionStateProto.ACCEPTED;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new SubmitWorkResponseProto(true);
            defaultInstance.initFields();
        }
    }

    public interface SourceStateUpdatedRequestProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder();

        boolean hasSrcName();

        java.lang.String getSrcName();

        com.google.protobuf.ByteString getSrcNameBytes();

        boolean hasState();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto getState();
    }

    public static final class SourceStateUpdatedRequestProto extends com.google.protobuf.GeneratedMessage implements SourceStateUpdatedRequestProtoOrBuilder {

        private SourceStateUpdatedRequestProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private SourceStateUpdatedRequestProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final SourceStateUpdatedRequestProto defaultInstance;

        public static SourceStateUpdatedRequestProto getDefaultInstance() {
            return defaultInstance;
        }

        public SourceStateUpdatedRequestProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private SourceStateUpdatedRequestProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                                    subBuilder = queryIdentifier_.toBuilder();
                                }
                                queryIdentifier_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(queryIdentifier_);
                                    queryIdentifier_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000001;
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                srcName_ = input.readBytes();
                                break;
                            }
                        case 24:
                            {
                                int rawValue = input.readEnum();
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto value = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(3, rawValue);
                                } else {
                                    bitField0_ |= 0x00000004;
                                    state_ = value;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedRequestProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.Builder.class);
        }

        public static com.google.protobuf.Parser<SourceStateUpdatedRequestProto> PARSER = new com.google.protobuf.AbstractParser<SourceStateUpdatedRequestProto>() {

            public SourceStateUpdatedRequestProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new SourceStateUpdatedRequestProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<SourceStateUpdatedRequestProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int QUERY_IDENTIFIER_FIELD_NUMBER = 1;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_;

        public boolean hasQueryIdentifier() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
            return queryIdentifier_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
            return queryIdentifier_;
        }

        public static final int SRC_NAME_FIELD_NUMBER = 2;

        private java.lang.Object srcName_;

        public boolean hasSrcName() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public java.lang.String getSrcName() {
            java.lang.Object ref = srcName_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    srcName_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getSrcNameBytes() {
            java.lang.Object ref = srcName_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                srcName_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int STATE_FIELD_NUMBER = 3;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto state_;

        public boolean hasState() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto getState() {
            return state_;
        }

        private void initFields() {
            queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
            srcName_ = "";
            state_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto.S_SUCCEEDED;
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
                output.writeMessage(1, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getSrcNameBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeEnum(3, state_.getNumber());
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
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getSrcNameBytes());
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(3, state_.getNumber());
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) obj;
            boolean result = true;
            result = result && (hasQueryIdentifier() == other.hasQueryIdentifier());
            if (hasQueryIdentifier()) {
                result = result && getQueryIdentifier().equals(other.getQueryIdentifier());
            }
            result = result && (hasSrcName() == other.hasSrcName());
            if (hasSrcName()) {
                result = result && getSrcName().equals(other.getSrcName());
            }
            result = result && (hasState() == other.hasState());
            if (hasState()) {
                result = result && (getState() == other.getState());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasQueryIdentifier()) {
                hash = (37 * hash) + QUERY_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getQueryIdentifier().hashCode();
            }
            if (hasSrcName()) {
                hash = (37 * hash) + SRC_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getSrcName().hashCode();
            }
            if (hasState()) {
                hash = (37 * hash) + STATE_FIELD_NUMBER;
                hash = (53 * hash) + hashEnum(getState());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedRequestProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getQueryIdentifierFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                srcName_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                state_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto.S_SUCCEEDED;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedRequestProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                if (queryIdentifierBuilder_ == null) {
                    result.queryIdentifier_ = queryIdentifier_;
                } else {
                    result.queryIdentifier_ = queryIdentifierBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.srcName_ = srcName_;
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.state_ = state_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.getDefaultInstance())
                    return this;
                if (other.hasQueryIdentifier()) {
                    mergeQueryIdentifier(other.getQueryIdentifier());
                }
                if (other.hasSrcName()) {
                    bitField0_ |= 0x00000002;
                    srcName_ = other.srcName_;
                    onChanged();
                }
                if (other.hasState()) {
                    setState(other.getState());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> queryIdentifierBuilder_;

            public boolean hasQueryIdentifier() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    return queryIdentifier_;
                } else {
                    return queryIdentifierBuilder_.getMessage();
                }
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    queryIdentifier_ = value;
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder builderForValue) {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = builderForValue.build();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder mergeQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) == 0x00000001) && queryIdentifier_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance()) {
                        queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.newBuilder(queryIdentifier_).mergeFrom(value).buildPartial();
                    } else {
                        queryIdentifier_ = value;
                    }
                    onChanged();
                } else {
                    queryIdentifierBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder clearQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder getQueryIdentifierBuilder() {
                bitField0_ |= 0x00000001;
                onChanged();
                return getQueryIdentifierFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
                if (queryIdentifierBuilder_ != null) {
                    return queryIdentifierBuilder_.getMessageOrBuilder();
                } else {
                    return queryIdentifier_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> getQueryIdentifierFieldBuilder() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifierBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder>(queryIdentifier_, getParentForChildren(), isClean());
                    queryIdentifier_ = null;
                }
                return queryIdentifierBuilder_;
            }

            private java.lang.Object srcName_ = "";

            public boolean hasSrcName() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public java.lang.String getSrcName() {
                java.lang.Object ref = srcName_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    srcName_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getSrcNameBytes() {
                java.lang.Object ref = srcName_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    srcName_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setSrcName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                srcName_ = value;
                onChanged();
                return this;
            }

            public Builder clearSrcName() {
                bitField0_ = (bitField0_ & ~0x00000002);
                srcName_ = getDefaultInstance().getSrcName();
                onChanged();
                return this;
            }

            public Builder setSrcNameBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                srcName_ = value;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto state_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto.S_SUCCEEDED;

            public boolean hasState() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto getState() {
                return state_;
            }

            public Builder setState(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                state_ = value;
                onChanged();
                return this;
            }

            public Builder clearState() {
                bitField0_ = (bitField0_ & ~0x00000004);
                state_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateProto.S_SUCCEEDED;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new SourceStateUpdatedRequestProto(true);
            defaultInstance.initFields();
        }
    }

    public interface SourceStateUpdatedResponseProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {
    }

    public static final class SourceStateUpdatedResponseProto extends com.google.protobuf.GeneratedMessage implements SourceStateUpdatedResponseProtoOrBuilder {

        private SourceStateUpdatedResponseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private SourceStateUpdatedResponseProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final SourceStateUpdatedResponseProto defaultInstance;

        public static SourceStateUpdatedResponseProto getDefaultInstance() {
            return defaultInstance;
        }

        public SourceStateUpdatedResponseProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private SourceStateUpdatedResponseProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedResponseProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.Builder.class);
        }

        public static com.google.protobuf.Parser<SourceStateUpdatedResponseProto> PARSER = new com.google.protobuf.AbstractParser<SourceStateUpdatedResponseProto>() {

            public SourceStateUpdatedResponseProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new SourceStateUpdatedResponseProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<SourceStateUpdatedResponseProto> getParserForType() {
            return PARSER;
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto) obj;
            boolean result = true;
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedResponseProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_SourceStateUpdatedResponseProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto(this);
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance())
                    return this;
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
        }

        static {
            defaultInstance = new SourceStateUpdatedResponseProto(true);
            defaultInstance.initFields();
        }
    }

    public interface QueryCompleteRequestProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasQueryId();

        java.lang.String getQueryId();

        com.google.protobuf.ByteString getQueryIdBytes();

        boolean hasQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder();

        boolean hasDeleteDelay();

        long getDeleteDelay();
    }

    public static final class QueryCompleteRequestProto extends com.google.protobuf.GeneratedMessage implements QueryCompleteRequestProtoOrBuilder {

        private QueryCompleteRequestProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private QueryCompleteRequestProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final QueryCompleteRequestProto defaultInstance;

        public static QueryCompleteRequestProto getDefaultInstance() {
            return defaultInstance;
        }

        public QueryCompleteRequestProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private QueryCompleteRequestProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                queryId_ = input.readBytes();
                                break;
                            }
                        case 18:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000002) == 0x00000002)) {
                                    subBuilder = queryIdentifier_.toBuilder();
                                }
                                queryIdentifier_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(queryIdentifier_);
                                    queryIdentifier_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000002;
                                break;
                            }
                        case 32:
                            {
                                bitField0_ |= 0x00000004;
                                deleteDelay_ = input.readInt64();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteRequestProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.Builder.class);
        }

        public static com.google.protobuf.Parser<QueryCompleteRequestProto> PARSER = new com.google.protobuf.AbstractParser<QueryCompleteRequestProto>() {

            public QueryCompleteRequestProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new QueryCompleteRequestProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<QueryCompleteRequestProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int QUERY_ID_FIELD_NUMBER = 1;

        private java.lang.Object queryId_;

        public boolean hasQueryId() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public java.lang.String getQueryId() {
            java.lang.Object ref = queryId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    queryId_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getQueryIdBytes() {
            java.lang.Object ref = queryId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                queryId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int QUERY_IDENTIFIER_FIELD_NUMBER = 2;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_;

        public boolean hasQueryIdentifier() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
            return queryIdentifier_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
            return queryIdentifier_;
        }

        public static final int DELETE_DELAY_FIELD_NUMBER = 4;

        private long deleteDelay_;

        public boolean hasDeleteDelay() {
            return ((bitField0_ & 0x00000004) == 0x00000004);
        }

        public long getDeleteDelay() {
            return deleteDelay_;
        }

        private void initFields() {
            queryId_ = "";
            queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
            deleteDelay_ = 0L;
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
                output.writeBytes(1, getQueryIdBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeMessage(2, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                output.writeInt64(4, deleteDelay_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getQueryIdBytes());
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000004) == 0x00000004)) {
                size += com.google.protobuf.CodedOutputStream.computeInt64Size(4, deleteDelay_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) obj;
            boolean result = true;
            result = result && (hasQueryId() == other.hasQueryId());
            if (hasQueryId()) {
                result = result && getQueryId().equals(other.getQueryId());
            }
            result = result && (hasQueryIdentifier() == other.hasQueryIdentifier());
            if (hasQueryIdentifier()) {
                result = result && getQueryIdentifier().equals(other.getQueryIdentifier());
            }
            result = result && (hasDeleteDelay() == other.hasDeleteDelay());
            if (hasDeleteDelay()) {
                result = result && (getDeleteDelay() == other.getDeleteDelay());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasQueryId()) {
                hash = (37 * hash) + QUERY_ID_FIELD_NUMBER;
                hash = (53 * hash) + getQueryId().hashCode();
            }
            if (hasQueryIdentifier()) {
                hash = (37 * hash) + QUERY_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getQueryIdentifier().hashCode();
            }
            if (hasDeleteDelay()) {
                hash = (37 * hash) + DELETE_DELAY_FIELD_NUMBER;
                hash = (53 * hash) + hashLong(getDeleteDelay());
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteRequestProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getQueryIdentifierFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                queryId_ = "";
                bitField0_ = (bitField0_ & ~0x00000001);
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                deleteDelay_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteRequestProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.queryId_ = queryId_;
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                if (queryIdentifierBuilder_ == null) {
                    result.queryIdentifier_ = queryIdentifier_;
                } else {
                    result.queryIdentifier_ = queryIdentifierBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.deleteDelay_ = deleteDelay_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.getDefaultInstance())
                    return this;
                if (other.hasQueryId()) {
                    bitField0_ |= 0x00000001;
                    queryId_ = other.queryId_;
                    onChanged();
                }
                if (other.hasQueryIdentifier()) {
                    mergeQueryIdentifier(other.getQueryIdentifier());
                }
                if (other.hasDeleteDelay()) {
                    setDeleteDelay(other.getDeleteDelay());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.lang.Object queryId_ = "";

            public boolean hasQueryId() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public java.lang.String getQueryId() {
                java.lang.Object ref = queryId_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    queryId_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getQueryIdBytes() {
                java.lang.Object ref = queryId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    queryId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setQueryId(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                queryId_ = value;
                onChanged();
                return this;
            }

            public Builder clearQueryId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                queryId_ = getDefaultInstance().getQueryId();
                onChanged();
                return this;
            }

            public Builder setQueryIdBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                queryId_ = value;
                onChanged();
                return this;
            }

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> queryIdentifierBuilder_;

            public boolean hasQueryIdentifier() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    return queryIdentifier_;
                } else {
                    return queryIdentifierBuilder_.getMessage();
                }
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    queryIdentifier_ = value;
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder builderForValue) {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = builderForValue.build();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder mergeQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) == 0x00000002) && queryIdentifier_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance()) {
                        queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.newBuilder(queryIdentifier_).mergeFrom(value).buildPartial();
                    } else {
                        queryIdentifier_ = value;
                    }
                    onChanged();
                } else {
                    queryIdentifierBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            public Builder clearQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder getQueryIdentifierBuilder() {
                bitField0_ |= 0x00000002;
                onChanged();
                return getQueryIdentifierFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
                if (queryIdentifierBuilder_ != null) {
                    return queryIdentifierBuilder_.getMessageOrBuilder();
                } else {
                    return queryIdentifier_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> getQueryIdentifierFieldBuilder() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifierBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder>(queryIdentifier_, getParentForChildren(), isClean());
                    queryIdentifier_ = null;
                }
                return queryIdentifierBuilder_;
            }

            private long deleteDelay_;

            public boolean hasDeleteDelay() {
                return ((bitField0_ & 0x00000004) == 0x00000004);
            }

            public long getDeleteDelay() {
                return deleteDelay_;
            }

            public Builder setDeleteDelay(long value) {
                bitField0_ |= 0x00000004;
                deleteDelay_ = value;
                onChanged();
                return this;
            }

            public Builder clearDeleteDelay() {
                bitField0_ = (bitField0_ & ~0x00000004);
                deleteDelay_ = 0L;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new QueryCompleteRequestProto(true);
            defaultInstance.initFields();
        }
    }

    public interface QueryCompleteResponseProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {
    }

    public static final class QueryCompleteResponseProto extends com.google.protobuf.GeneratedMessage implements QueryCompleteResponseProtoOrBuilder {

        private QueryCompleteResponseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private QueryCompleteResponseProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final QueryCompleteResponseProto defaultInstance;

        public static QueryCompleteResponseProto getDefaultInstance() {
            return defaultInstance;
        }

        public QueryCompleteResponseProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private QueryCompleteResponseProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteResponseProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.Builder.class);
        }

        public static com.google.protobuf.Parser<QueryCompleteResponseProto> PARSER = new com.google.protobuf.AbstractParser<QueryCompleteResponseProto>() {

            public QueryCompleteResponseProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new QueryCompleteResponseProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<QueryCompleteResponseProto> getParserForType() {
            return PARSER;
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto) obj;
            boolean result = true;
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteResponseProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_QueryCompleteResponseProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto(this);
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance())
                    return this;
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
        }

        static {
            defaultInstance = new QueryCompleteResponseProto(true);
            defaultInstance.initFields();
        }
    }

    public interface TerminateFragmentRequestProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier();

        org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder();

        boolean hasFragmentIdentifierString();

        java.lang.String getFragmentIdentifierString();

        com.google.protobuf.ByteString getFragmentIdentifierStringBytes();
    }

    public static final class TerminateFragmentRequestProto extends com.google.protobuf.GeneratedMessage implements TerminateFragmentRequestProtoOrBuilder {

        private TerminateFragmentRequestProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private TerminateFragmentRequestProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final TerminateFragmentRequestProto defaultInstance;

        public static TerminateFragmentRequestProto getDefaultInstance() {
            return defaultInstance;
        }

        public TerminateFragmentRequestProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private TerminateFragmentRequestProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder subBuilder = null;
                                if (((bitField0_ & 0x00000001) == 0x00000001)) {
                                    subBuilder = queryIdentifier_.toBuilder();
                                }
                                queryIdentifier_ = input.readMessage(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.PARSER, extensionRegistry);
                                if (subBuilder != null) {
                                    subBuilder.mergeFrom(queryIdentifier_);
                                    queryIdentifier_ = subBuilder.buildPartial();
                                }
                                bitField0_ |= 0x00000001;
                                break;
                            }
                        case 18:
                            {
                                bitField0_ |= 0x00000002;
                                fragmentIdentifierString_ = input.readBytes();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentRequestProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.Builder.class);
        }

        public static com.google.protobuf.Parser<TerminateFragmentRequestProto> PARSER = new com.google.protobuf.AbstractParser<TerminateFragmentRequestProto>() {

            public TerminateFragmentRequestProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new TerminateFragmentRequestProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<TerminateFragmentRequestProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int QUERY_IDENTIFIER_FIELD_NUMBER = 1;

        private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_;

        public boolean hasQueryIdentifier() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
            return queryIdentifier_;
        }

        public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
            return queryIdentifier_;
        }

        public static final int FRAGMENT_IDENTIFIER_STRING_FIELD_NUMBER = 2;

        private java.lang.Object fragmentIdentifierString_;

        public boolean hasFragmentIdentifierString() {
            return ((bitField0_ & 0x00000002) == 0x00000002);
        }

        public java.lang.String getFragmentIdentifierString() {
            java.lang.Object ref = fragmentIdentifierString_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                if (bs.isValidUtf8()) {
                    fragmentIdentifierString_ = s;
                }
                return s;
            }
        }

        public com.google.protobuf.ByteString getFragmentIdentifierStringBytes() {
            java.lang.Object ref = fragmentIdentifierString_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                fragmentIdentifierString_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        private void initFields() {
            queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
            fragmentIdentifierString_ = "";
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
                output.writeMessage(1, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                output.writeBytes(2, getFragmentIdentifierStringBytes());
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
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, queryIdentifier_);
            }
            if (((bitField0_ & 0x00000002) == 0x00000002)) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getFragmentIdentifierStringBytes());
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) obj;
            boolean result = true;
            result = result && (hasQueryIdentifier() == other.hasQueryIdentifier());
            if (hasQueryIdentifier()) {
                result = result && getQueryIdentifier().equals(other.getQueryIdentifier());
            }
            result = result && (hasFragmentIdentifierString() == other.hasFragmentIdentifierString());
            if (hasFragmentIdentifierString()) {
                result = result && getFragmentIdentifierString().equals(other.getFragmentIdentifierString());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasQueryIdentifier()) {
                hash = (37 * hash) + QUERY_IDENTIFIER_FIELD_NUMBER;
                hash = (53 * hash) + getQueryIdentifier().hashCode();
            }
            if (hasFragmentIdentifierString()) {
                hash = (37 * hash) + FRAGMENT_IDENTIFIER_STRING_FIELD_NUMBER;
                hash = (53 * hash) + getFragmentIdentifierString().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentRequestProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
                    getQueryIdentifierFieldBuilder();
                }
            }

            private static Builder create() {
                return new Builder();
            }

            public Builder clear() {
                super.clear();
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                fragmentIdentifierString_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentRequestProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                if (queryIdentifierBuilder_ == null) {
                    result.queryIdentifier_ = queryIdentifier_;
                } else {
                    result.queryIdentifier_ = queryIdentifierBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.fragmentIdentifierString_ = fragmentIdentifierString_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.getDefaultInstance())
                    return this;
                if (other.hasQueryIdentifier()) {
                    mergeQueryIdentifier(other.getQueryIdentifier());
                }
                if (other.hasFragmentIdentifierString()) {
                    bitField0_ |= 0x00000002;
                    fragmentIdentifierString_ = other.fragmentIdentifierString_;
                    onChanged();
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> queryIdentifierBuilder_;

            public boolean hasQueryIdentifier() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto getQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    return queryIdentifier_;
                } else {
                    return queryIdentifierBuilder_.getMessage();
                }
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    queryIdentifier_ = value;
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder setQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder builderForValue) {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = builderForValue.build();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder mergeQueryIdentifier(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto value) {
                if (queryIdentifierBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) == 0x00000001) && queryIdentifier_ != org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance()) {
                        queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.newBuilder(queryIdentifier_).mergeFrom(value).buildPartial();
                    } else {
                        queryIdentifier_ = value;
                    }
                    onChanged();
                } else {
                    queryIdentifierBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000001;
                return this;
            }

            public Builder clearQueryIdentifier() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifier_ = org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.getDefaultInstance();
                    onChanged();
                } else {
                    queryIdentifierBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder getQueryIdentifierBuilder() {
                bitField0_ |= 0x00000001;
                onChanged();
                return getQueryIdentifierFieldBuilder().getBuilder();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder getQueryIdentifierOrBuilder() {
                if (queryIdentifierBuilder_ != null) {
                    return queryIdentifierBuilder_.getMessageOrBuilder();
                } else {
                    return queryIdentifier_;
                }
            }

            private com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder> getQueryIdentifierFieldBuilder() {
                if (queryIdentifierBuilder_ == null) {
                    queryIdentifierBuilder_ = new com.google.protobuf.SingleFieldBuilder<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProto.Builder, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryIdentifierProtoOrBuilder>(queryIdentifier_, getParentForChildren(), isClean());
                    queryIdentifier_ = null;
                }
                return queryIdentifierBuilder_;
            }

            private java.lang.Object fragmentIdentifierString_ = "";

            public boolean hasFragmentIdentifierString() {
                return ((bitField0_ & 0x00000002) == 0x00000002);
            }

            public java.lang.String getFragmentIdentifierString() {
                java.lang.Object ref = fragmentIdentifierString_;
                if (!(ref instanceof java.lang.String)) {
                    java.lang.String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
                    fragmentIdentifierString_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            public com.google.protobuf.ByteString getFragmentIdentifierStringBytes() {
                java.lang.Object ref = fragmentIdentifierString_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
                    fragmentIdentifierString_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            public Builder setFragmentIdentifierString(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                fragmentIdentifierString_ = value;
                onChanged();
                return this;
            }

            public Builder clearFragmentIdentifierString() {
                bitField0_ = (bitField0_ & ~0x00000002);
                fragmentIdentifierString_ = getDefaultInstance().getFragmentIdentifierString();
                onChanged();
                return this;
            }

            public Builder setFragmentIdentifierStringBytes(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                fragmentIdentifierString_ = value;
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new TerminateFragmentRequestProto(true);
            defaultInstance.initFields();
        }
    }

    public interface TerminateFragmentResponseProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {
    }

    public static final class TerminateFragmentResponseProto extends com.google.protobuf.GeneratedMessage implements TerminateFragmentResponseProtoOrBuilder {

        private TerminateFragmentResponseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private TerminateFragmentResponseProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final TerminateFragmentResponseProto defaultInstance;

        public static TerminateFragmentResponseProto getDefaultInstance() {
            return defaultInstance;
        }

        public TerminateFragmentResponseProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private TerminateFragmentResponseProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentResponseProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.Builder.class);
        }

        public static com.google.protobuf.Parser<TerminateFragmentResponseProto> PARSER = new com.google.protobuf.AbstractParser<TerminateFragmentResponseProto>() {

            public TerminateFragmentResponseProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new TerminateFragmentResponseProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<TerminateFragmentResponseProto> getParserForType() {
            return PARSER;
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto) obj;
            boolean result = true;
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentResponseProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_TerminateFragmentResponseProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto(this);
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance())
                    return this;
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
        }

        static {
            defaultInstance = new TerminateFragmentResponseProto(true);
            defaultInstance.initFields();
        }
    }

    public interface GetTokenRequestProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {
    }

    public static final class GetTokenRequestProto extends com.google.protobuf.GeneratedMessage implements GetTokenRequestProtoOrBuilder {

        private GetTokenRequestProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private GetTokenRequestProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final GetTokenRequestProto defaultInstance;

        public static GetTokenRequestProto getDefaultInstance() {
            return defaultInstance;
        }

        public GetTokenRequestProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private GetTokenRequestProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenRequestProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.Builder.class);
        }

        public static com.google.protobuf.Parser<GetTokenRequestProto> PARSER = new com.google.protobuf.AbstractParser<GetTokenRequestProto>() {

            public GetTokenRequestProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new GetTokenRequestProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<GetTokenRequestProto> getParserForType() {
            return PARSER;
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) obj;
            boolean result = true;
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenRequestProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenRequestProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenRequestProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto(this);
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.getDefaultInstance())
                    return this;
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }
        }

        static {
            defaultInstance = new GetTokenRequestProto(true);
            defaultInstance.initFields();
        }
    }

    public interface GetTokenResponseProtoOrBuilder extends com.google.protobuf.MessageOrBuilder {

        boolean hasToken();

        com.google.protobuf.ByteString getToken();
    }

    public static final class GetTokenResponseProto extends com.google.protobuf.GeneratedMessage implements GetTokenResponseProtoOrBuilder {

        private GetTokenResponseProto(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
        }

        private GetTokenResponseProto(boolean noInit) {
            this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
        }

        private static final GetTokenResponseProto defaultInstance;

        public static GetTokenResponseProto getDefaultInstance() {
            return defaultInstance;
        }

        public GetTokenResponseProto getDefaultInstanceForType() {
            return defaultInstance;
        }

        private final com.google.protobuf.UnknownFieldSet unknownFields;

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
        }

        private GetTokenResponseProto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            initFields();
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            done = true;
                            break;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    done = true;
                                }
                                break;
                            }
                        case 10:
                            {
                                bitField0_ |= 0x00000001;
                                token_ = input.readBytes();
                                break;
                            }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(e.getMessage()).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenResponseProto_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.Builder.class);
        }

        public static com.google.protobuf.Parser<GetTokenResponseProto> PARSER = new com.google.protobuf.AbstractParser<GetTokenResponseProto>() {

            public GetTokenResponseProto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
                return new GetTokenResponseProto(input, extensionRegistry);
            }
        };

        @java.lang.Override
        public com.google.protobuf.Parser<GetTokenResponseProto> getParserForType() {
            return PARSER;
        }

        private int bitField0_;

        public static final int TOKEN_FIELD_NUMBER = 1;

        private com.google.protobuf.ByteString token_;

        public boolean hasToken() {
            return ((bitField0_ & 0x00000001) == 0x00000001);
        }

        public com.google.protobuf.ByteString getToken() {
            return token_;
        }

        private void initFields() {
            token_ = com.google.protobuf.ByteString.EMPTY;
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
                output.writeBytes(1, token_);
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
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, token_);
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
            if (!(obj instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto)) {
                return super.equals(obj);
            }
            org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto other = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto) obj;
            boolean result = true;
            result = result && (hasToken() == other.hasToken());
            if (hasToken()) {
                result = result && getToken().equals(other.getToken());
            }
            result = result && getUnknownFields().equals(other.getUnknownFields());
            return result;
        }

        private int memoizedHashCode = 0;

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptorForType().hashCode();
            if (hasToken()) {
                hash = (37 * hash) + TOKEN_FIELD_NUMBER;
                hash = (53 * hash) + getToken().hashCode();
            }
            hash = (29 * hash) + getUnknownFields().hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseDelimitedFrom(input, extensionRegistry);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return PARSER.parseFrom(input);
        }

        public static org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return PARSER.parseFrom(input, extensionRegistry);
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto prototype) {
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

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> implements org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProtoOrBuilder {

            public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenResponseProto_descriptor;
            }

            protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenResponseProto_fieldAccessorTable.ensureFieldAccessorsInitialized(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.Builder.class);
            }

            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(com.google.protobuf.GeneratedMessage.BuilderParent parent) {
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
                token_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(buildPartial());
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.internal_static_GetTokenResponseProto_descriptor;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto getDefaultInstanceForType() {
                return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance();
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto build() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto buildPartial() {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto result = new org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
                    to_bitField0_ |= 0x00000001;
                }
                result.token_ = token_;
                result.bitField0_ = to_bitField0_;
                onBuilt();
                return result;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto) {
                    return mergeFrom((org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto other) {
                if (other == org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance())
                    return this;
                if (other.hasToken()) {
                    setToken(other.getToken());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public final boolean isInitialized() {
                return true;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto) e.getUnfinishedMessage();
                    throw e;
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private com.google.protobuf.ByteString token_ = com.google.protobuf.ByteString.EMPTY;

            public boolean hasToken() {
                return ((bitField0_ & 0x00000001) == 0x00000001);
            }

            public com.google.protobuf.ByteString getToken() {
                return token_;
            }

            public Builder setToken(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000001;
                token_ = value;
                onChanged();
                return this;
            }

            public Builder clearToken() {
                bitField0_ = (bitField0_ & ~0x00000001);
                token_ = getDefaultInstance().getToken();
                onChanged();
                return this;
            }
        }

        static {
            defaultInstance = new GetTokenResponseProto(true);
            defaultInstance.initFields();
        }
    }

    public static abstract class LlapDaemonProtocol implements com.google.protobuf.Service {

        protected LlapDaemonProtocol() {
        }

        public interface Interface {

            public abstract void submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto> done);

            public abstract void sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto> done);

            public abstract void queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto> done);

            public abstract void terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto> done);
        }

        public static com.google.protobuf.Service newReflectiveService(final Interface impl) {
            return new LlapDaemonProtocol() {

                @java.lang.Override
                public void submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto> done) {
                    impl.submitWork(controller, request, done);
                }

                @java.lang.Override
                public void sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto> done) {
                    impl.sourceStateUpdated(controller, request, done);
                }

                @java.lang.Override
                public void queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto> done) {
                    impl.queryComplete(controller, request, done);
                }

                @java.lang.Override
                public void terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto> done) {
                    impl.terminateFragment(controller, request, done);
                }
            };
        }

        public static com.google.protobuf.BlockingService newReflectiveBlockingService(final BlockingInterface impl) {
            return new com.google.protobuf.BlockingService() {

                public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
                    return getDescriptor();
                }

                public final com.google.protobuf.Message callBlockingMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request) throws com.google.protobuf.ServiceException {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.callBlockingMethod() given method descriptor for " + "wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return impl.submitWork(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) request);
                        case 1:
                            return impl.sourceStateUpdated(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) request);
                        case 2:
                            return impl.queryComplete(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) request);
                        case 3:
                            return impl.terminateFragment(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) request);
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.getDefaultInstance();
                        case 1:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.getDefaultInstance();
                        case 2:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.getDefaultInstance();
                        case 3:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance();
                        case 1:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance();
                        case 2:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance();
                        case 3:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }
            };
        }

        public abstract void submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto> done);

        public abstract void sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto> done);

        public abstract void queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto> done);

        public abstract void terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto> done);

        public static final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.getDescriptor().getServices().get(0);
        }

        public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public final void callMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request, com.google.protobuf.RpcCallback<com.google.protobuf.Message> done) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.callMethod() given method descriptor for wrong " + "service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    this.submitWork(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto) request, com.google.protobuf.RpcUtil.<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto>specializeCallback(done));
                    return;
                case 1:
                    this.sourceStateUpdated(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto) request, com.google.protobuf.RpcUtil.<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto>specializeCallback(done));
                    return;
                case 2:
                    this.queryComplete(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto) request, com.google.protobuf.RpcUtil.<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto>specializeCallback(done));
                    return;
                case 3:
                    this.terminateFragment(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto) request, com.google.protobuf.RpcUtil.<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto>specializeCallback(done));
                    return;
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto.getDefaultInstance();
                case 1:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto.getDefaultInstance();
                case 2:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto.getDefaultInstance();
                case 3:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance();
                case 1:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance();
                case 2:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance();
                case 3:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public static Stub newStub(com.google.protobuf.RpcChannel channel) {
            return new Stub(channel);
        }

        public static final class Stub extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.LlapDaemonProtocol implements Interface {

            private Stub(com.google.protobuf.RpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.RpcChannel channel;

            public com.google.protobuf.RpcChannel getChannel() {
                return channel;
            }

            public void submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto> done) {
                channel.callMethod(getDescriptor().getMethods().get(0), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance()));
            }

            public void sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto> done) {
                channel.callMethod(getDescriptor().getMethods().get(1), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance()));
            }

            public void queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto> done) {
                channel.callMethod(getDescriptor().getMethods().get(2), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance()));
            }

            public void terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto> done) {
                channel.callMethod(getDescriptor().getMethods().get(3), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance()));
            }
        }

        public static BlockingInterface newBlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
            return new BlockingStub(channel);
        }

        public interface BlockingInterface {

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request) throws com.google.protobuf.ServiceException;

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request) throws com.google.protobuf.ServiceException;

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request) throws com.google.protobuf.ServiceException;

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request) throws com.google.protobuf.ServiceException;
        }

        private static final class BlockingStub implements BlockingInterface {

            private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.BlockingRpcChannel channel;

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto submitWork(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkRequestProto request) throws com.google.protobuf.ServiceException {
                return (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto) channel.callBlockingMethod(getDescriptor().getMethods().get(0), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SubmitWorkResponseProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto sourceStateUpdated(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedRequestProto request) throws com.google.protobuf.ServiceException {
                return (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto) channel.callBlockingMethod(getDescriptor().getMethods().get(1), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.SourceStateUpdatedResponseProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto queryComplete(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteRequestProto request) throws com.google.protobuf.ServiceException {
                return (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto) channel.callBlockingMethod(getDescriptor().getMethods().get(2), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.QueryCompleteResponseProto.getDefaultInstance());
            }

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto terminateFragment(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentRequestProto request) throws com.google.protobuf.ServiceException {
                return (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto) channel.callBlockingMethod(getDescriptor().getMethods().get(3), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.TerminateFragmentResponseProto.getDefaultInstance());
            }
        }
    }

    public static abstract class LlapManagementProtocol implements com.google.protobuf.Service {

        protected LlapManagementProtocol() {
        }

        public interface Interface {

            public abstract void getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto> done);
        }

        public static com.google.protobuf.Service newReflectiveService(final Interface impl) {
            return new LlapManagementProtocol() {

                @java.lang.Override
                public void getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto> done) {
                    impl.getDelegationToken(controller, request, done);
                }
            };
        }

        public static com.google.protobuf.BlockingService newReflectiveBlockingService(final BlockingInterface impl) {
            return new com.google.protobuf.BlockingService() {

                public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
                    return getDescriptor();
                }

                public final com.google.protobuf.Message callBlockingMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request) throws com.google.protobuf.ServiceException {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.callBlockingMethod() given method descriptor for " + "wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return impl.getDelegationToken(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) request);
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }

                public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
                    if (method.getService() != getDescriptor()) {
                        throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
                    }
                    switch(method.getIndex()) {
                        case 0:
                            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance();
                        default:
                            throw new java.lang.AssertionError("Can't get here.");
                    }
                }
            };
        }

        public abstract void getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto> done);

        public static final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptor() {
            return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.getDescriptor().getServices().get(1);
        }

        public final com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public final void callMethod(com.google.protobuf.Descriptors.MethodDescriptor method, com.google.protobuf.RpcController controller, com.google.protobuf.Message request, com.google.protobuf.RpcCallback<com.google.protobuf.Message> done) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.callMethod() given method descriptor for wrong " + "service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    this.getDelegationToken(controller, (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto) request, com.google.protobuf.RpcUtil.<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto>specializeCallback(done));
                    return;
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getRequestPrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getRequestPrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public final com.google.protobuf.Message getResponsePrototype(com.google.protobuf.Descriptors.MethodDescriptor method) {
            if (method.getService() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("Service.getResponsePrototype() given method " + "descriptor for wrong service type.");
            }
            switch(method.getIndex()) {
                case 0:
                    return org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance();
                default:
                    throw new java.lang.AssertionError("Can't get here.");
            }
        }

        public static Stub newStub(com.google.protobuf.RpcChannel channel) {
            return new Stub(channel);
        }

        public static final class Stub extends org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.LlapManagementProtocol implements Interface {

            private Stub(com.google.protobuf.RpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.RpcChannel channel;

            public com.google.protobuf.RpcChannel getChannel() {
                return channel;
            }

            public void getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request, com.google.protobuf.RpcCallback<org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto> done) {
                channel.callMethod(getDescriptor().getMethods().get(0), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance(), com.google.protobuf.RpcUtil.generalizeCallback(done, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.class, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance()));
            }
        }

        public static BlockingInterface newBlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
            return new BlockingStub(channel);
        }

        public interface BlockingInterface {

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request) throws com.google.protobuf.ServiceException;
        }

        private static final class BlockingStub implements BlockingInterface {

            private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
                this.channel = channel;
            }

            private final com.google.protobuf.BlockingRpcChannel channel;

            public org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto getDelegationToken(com.google.protobuf.RpcController controller, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenRequestProto request) throws com.google.protobuf.ServiceException {
                return (org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto) channel.callBlockingMethod(getDescriptor().getMethods().get(0), controller, request, org.apache.hadoop.hive.llap.daemon.rpc.LlapDaemonProtocolProtos.GetTokenResponseProto.getDefaultInstance());
            }
        }
    }

    private static com.google.protobuf.Descriptors.Descriptor internal_static_UserPayloadProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_UserPayloadProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_EntityDescriptorProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_EntityDescriptorProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_IOSpecProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_IOSpecProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_GroupInputSpecProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_GroupInputSpecProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_FragmentSpecProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_FragmentSpecProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_FragmentRuntimeInfo_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_FragmentRuntimeInfo_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_QueryIdentifierProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_QueryIdentifierProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_SubmitWorkRequestProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_SubmitWorkRequestProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_SubmitWorkResponseProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_SubmitWorkResponseProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_SourceStateUpdatedRequestProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_SourceStateUpdatedRequestProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_SourceStateUpdatedResponseProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_SourceStateUpdatedResponseProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_QueryCompleteRequestProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_QueryCompleteRequestProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_QueryCompleteResponseProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_QueryCompleteResponseProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_TerminateFragmentRequestProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_TerminateFragmentRequestProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_TerminateFragmentResponseProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_TerminateFragmentResponseProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_GetTokenRequestProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_GetTokenRequestProto_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_GetTokenResponseProto_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_GetTokenResponseProto_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = { "\n\030LlapDaemonProtocol.proto\"9\n\020UserPayloa" + "dProto\022\024\n\014user_payload\030\001 \001(\014\022\017\n\007version\030" + "\002 \001(\005\"j\n\025EntityDescriptorProto\022\022\n\nclass_" + "name\030\001 \001(\t\022\'\n\014user_payload\030\002 \001(\0132\021.UserP" + "ayloadProto\022\024\n\014history_text\030\003 \001(\014\"x\n\013IOS" + "pecProto\022\035\n\025connected_vertex_name\030\001 \001(\t\022" + "-\n\rio_descriptor\030\002 \001(\0132\026.EntityDescripto" + "rProto\022\033\n\023physical_edge_count\030\003 \001(\005\"z\n\023G" + "roupInputSpecProto\022\022\n\ngroup_name\030\001 \001(\t\022\026" + "\n\016group_vertices\030\002 \003(\t\0227\n\027merged_input_d", "escriptor\030\003 \001(\0132\026.EntityDescriptorProto\"" + "\353\002\n\021FragmentSpecProto\022\"\n\032fragment_identi" + "fier_string\030\001 \001(\t\022\020\n\010dag_name\030\002 \001(\t\022\016\n\006d" + "ag_id\030\013 \001(\005\022\023\n\013vertex_name\030\003 \001(\t\0224\n\024proc" + "essor_descriptor\030\004 \001(\0132\026.EntityDescripto" + "rProto\022!\n\013input_specs\030\005 \003(\0132\014.IOSpecProt" + "o\022\"\n\014output_specs\030\006 \003(\0132\014.IOSpecProto\0221\n" + "\023grouped_input_specs\030\007 \003(\0132\024.GroupInputS" + "pecProto\022\032\n\022vertex_parallelism\030\010 \001(\005\022\027\n\017" + "fragment_number\030\t \001(\005\022\026\n\016attempt_number\030", "\n \001(\005\"\344\001\n\023FragmentRuntimeInfo\022#\n\033num_sel" + "f_and_upstream_tasks\030\001 \001(\005\022-\n%num_self_a" + "nd_upstream_completed_tasks\030\002 \001(\005\022\033\n\023wit" + "hin_dag_priority\030\003 \001(\005\022\026\n\016dag_start_time" + "\030\004 \001(\003\022 \n\030first_attempt_start_time\030\005 \001(\003" + "\022\"\n\032current_attempt_start_time\030\006 \001(\003\"F\n\024" + "QueryIdentifierProto\022\026\n\016app_identifier\030\001" + " \001(\t\022\026\n\016dag_identifier\030\002 \001(\005\"\266\002\n\026SubmitW" + "orkRequestProto\022\033\n\023container_id_string\030\001" + " \001(\t\022\017\n\007am_host\030\002 \001(\t\022\017\n\007am_port\030\003 \001(\005\022\030", "\n\020token_identifier\030\004 \001(\t\022\032\n\022credentials_" + "binary\030\005 \001(\014\022\014\n\004user\030\006 \001(\t\022\035\n\025applicatio" + "n_id_string\030\007 \001(\t\022\032\n\022app_attempt_number\030" + "\010 \001(\005\022)\n\rfragment_spec\030\t \001(\0132\022.FragmentS" + "pecProto\0223\n\025fragment_runtime_info\030\n \001(\0132" + "\024.FragmentRuntimeInfo\"J\n\027SubmitWorkRespo" + "nseProto\022/\n\020submission_state\030\001 \001(\0162\025.Sub" + "missionStateProto\"\205\001\n\036SourceStateUpdated" + "RequestProto\022/\n\020query_identifier\030\001 \001(\0132\025" + ".QueryIdentifierProto\022\020\n\010src_name\030\002 \001(\t\022", " \n\005state\030\003 \001(\0162\021.SourceStateProto\"!\n\037Sou" + "rceStateUpdatedResponseProto\"w\n\031QueryCom" + "pleteRequestProto\022\020\n\010query_id\030\001 \001(\t\022/\n\020q" + "uery_identifier\030\002 \001(\0132\025.QueryIdentifierP" + "roto\022\027\n\014delete_delay\030\004 \001(\003:\0010\"\034\n\032QueryCo" + "mpleteResponseProto\"t\n\035TerminateFragment" + "RequestProto\022/\n\020query_identifier\030\001 \001(\0132\025" + ".QueryIdentifierProto\022\"\n\032fragment_identi" + "fier_string\030\002 \001(\t\" \n\036TerminateFragmentRe" + "sponseProto\"\026\n\024GetTokenRequestProto\"&\n\025G", "etTokenResponseProto\022\r\n\005token\030\001 \001(\014*2\n\020S" + "ourceStateProto\022\017\n\013S_SUCCEEDED\020\001\022\r\n\tS_RU" + "NNING\020\002*E\n\024SubmissionStateProto\022\014\n\010ACCEP" + "TED\020\001\022\014\n\010REJECTED\020\002\022\021\n\rEVICTED_OTHER\020\0032\316" + "\002\n\022LlapDaemonProtocol\022?\n\nsubmitWork\022\027.Su" + "bmitWorkRequestProto\032\030.SubmitWorkRespons" + "eProto\022W\n\022sourceStateUpdated\022\037.SourceSta" + "teUpdatedRequestProto\032 .SourceStateUpdat" + "edResponseProto\022H\n\rqueryComplete\022\032.Query" + "CompleteRequestProto\032\033.QueryCompleteResp", "onseProto\022T\n\021terminateFragment\022\036.Termina" + "teFragmentRequestProto\032\037.TerminateFragme" + "ntResponseProto2]\n\026LlapManagementProtoco" + "l\022C\n\022getDelegationToken\022\025.GetTokenReques" + "tProto\032\026.GetTokenResponseProtoBH\n&org.ap" + "ache.hadoop.hive.llap.daemon.rpcB\030LlapDa" + "emonProtocolProtos\210\001\001\240\001\001" };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {

            public com.google.protobuf.ExtensionRegistry assignDescriptors(com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                internal_static_UserPayloadProto_descriptor = getDescriptor().getMessageTypes().get(0);
                internal_static_UserPayloadProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_UserPayloadProto_descriptor, new java.lang.String[] { "UserPayload", "Version" });
                internal_static_EntityDescriptorProto_descriptor = getDescriptor().getMessageTypes().get(1);
                internal_static_EntityDescriptorProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_EntityDescriptorProto_descriptor, new java.lang.String[] { "ClassName", "UserPayload", "HistoryText" });
                internal_static_IOSpecProto_descriptor = getDescriptor().getMessageTypes().get(2);
                internal_static_IOSpecProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_IOSpecProto_descriptor, new java.lang.String[] { "ConnectedVertexName", "IoDescriptor", "PhysicalEdgeCount" });
                internal_static_GroupInputSpecProto_descriptor = getDescriptor().getMessageTypes().get(3);
                internal_static_GroupInputSpecProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_GroupInputSpecProto_descriptor, new java.lang.String[] { "GroupName", "GroupVertices", "MergedInputDescriptor" });
                internal_static_FragmentSpecProto_descriptor = getDescriptor().getMessageTypes().get(4);
                internal_static_FragmentSpecProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_FragmentSpecProto_descriptor, new java.lang.String[] { "FragmentIdentifierString", "DagName", "DagId", "VertexName", "ProcessorDescriptor", "InputSpecs", "OutputSpecs", "GroupedInputSpecs", "VertexParallelism", "FragmentNumber", "AttemptNumber" });
                internal_static_FragmentRuntimeInfo_descriptor = getDescriptor().getMessageTypes().get(5);
                internal_static_FragmentRuntimeInfo_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_FragmentRuntimeInfo_descriptor, new java.lang.String[] { "NumSelfAndUpstreamTasks", "NumSelfAndUpstreamCompletedTasks", "WithinDagPriority", "DagStartTime", "FirstAttemptStartTime", "CurrentAttemptStartTime" });
                internal_static_QueryIdentifierProto_descriptor = getDescriptor().getMessageTypes().get(6);
                internal_static_QueryIdentifierProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_QueryIdentifierProto_descriptor, new java.lang.String[] { "AppIdentifier", "DagIdentifier" });
                internal_static_SubmitWorkRequestProto_descriptor = getDescriptor().getMessageTypes().get(7);
                internal_static_SubmitWorkRequestProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_SubmitWorkRequestProto_descriptor, new java.lang.String[] { "ContainerIdString", "AmHost", "AmPort", "TokenIdentifier", "CredentialsBinary", "User", "ApplicationIdString", "AppAttemptNumber", "FragmentSpec", "FragmentRuntimeInfo" });
                internal_static_SubmitWorkResponseProto_descriptor = getDescriptor().getMessageTypes().get(8);
                internal_static_SubmitWorkResponseProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_SubmitWorkResponseProto_descriptor, new java.lang.String[] { "SubmissionState" });
                internal_static_SourceStateUpdatedRequestProto_descriptor = getDescriptor().getMessageTypes().get(9);
                internal_static_SourceStateUpdatedRequestProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_SourceStateUpdatedRequestProto_descriptor, new java.lang.String[] { "QueryIdentifier", "SrcName", "State" });
                internal_static_SourceStateUpdatedResponseProto_descriptor = getDescriptor().getMessageTypes().get(10);
                internal_static_SourceStateUpdatedResponseProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_SourceStateUpdatedResponseProto_descriptor, new java.lang.String[] {});
                internal_static_QueryCompleteRequestProto_descriptor = getDescriptor().getMessageTypes().get(11);
                internal_static_QueryCompleteRequestProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_QueryCompleteRequestProto_descriptor, new java.lang.String[] { "QueryId", "QueryIdentifier", "DeleteDelay" });
                internal_static_QueryCompleteResponseProto_descriptor = getDescriptor().getMessageTypes().get(12);
                internal_static_QueryCompleteResponseProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_QueryCompleteResponseProto_descriptor, new java.lang.String[] {});
                internal_static_TerminateFragmentRequestProto_descriptor = getDescriptor().getMessageTypes().get(13);
                internal_static_TerminateFragmentRequestProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_TerminateFragmentRequestProto_descriptor, new java.lang.String[] { "QueryIdentifier", "FragmentIdentifierString" });
                internal_static_TerminateFragmentResponseProto_descriptor = getDescriptor().getMessageTypes().get(14);
                internal_static_TerminateFragmentResponseProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_TerminateFragmentResponseProto_descriptor, new java.lang.String[] {});
                internal_static_GetTokenRequestProto_descriptor = getDescriptor().getMessageTypes().get(15);
                internal_static_GetTokenRequestProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_GetTokenRequestProto_descriptor, new java.lang.String[] {});
                internal_static_GetTokenResponseProto_descriptor = getDescriptor().getMessageTypes().get(16);
                internal_static_GetTokenResponseProto_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_GetTokenResponseProto_descriptor, new java.lang.String[] { "Token" });
                return null;
            }
        };
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] {}, assigner);
    }
}
