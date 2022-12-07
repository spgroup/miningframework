package voldemort.client.protocol.pb;

public final class VAdminProto {

    private VAdminProto() {
    }

    public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
    }

    public enum AdminRequestType implements com.google.protobuf.ProtocolMessageEnum {

        GET_METADATA(0, 0),
        UPDATE_METADATA(1, 1),
        UPDATE_PARTITION_ENTRIES(2, 2),
        FETCH_PARTITION_ENTRIES(3, 3),
        DELETE_PARTITION_ENTRIES(4, 4),
        INITIATE_FETCH_AND_UPDATE(5, 5),
        ASYNC_OPERATION_STATUS(6, 6),
        INITIATE_REBALANCE_NODE(7, 7),
        ASYNC_OPERATION_STOP(8, 8),
        ASYNC_OPERATION_LIST(9, 9);

        public final int getNumber() {
            return value;
        }

        public static AdminRequestType valueOf(int value) {
            switch(value) {
                case 0:
                    return GET_METADATA;
                case 1:
                    return UPDATE_METADATA;
                case 2:
                    return UPDATE_PARTITION_ENTRIES;
                case 3:
                    return FETCH_PARTITION_ENTRIES;
                case 4:
                    return DELETE_PARTITION_ENTRIES;
                case 5:
                    return INITIATE_FETCH_AND_UPDATE;
                case 6:
                    return ASYNC_OPERATION_STATUS;
                case 7:
                    return INITIATE_REBALANCE_NODE;
                case 8:
                    return ASYNC_OPERATION_STOP;
                case 9:
                    return ASYNC_OPERATION_LIST;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<AdminRequestType> internalGetValueMap() {
            return internalValueMap;
        }

        private static com.google.protobuf.Internal.EnumLiteMap<AdminRequestType> internalValueMap = new com.google.protobuf.Internal.EnumLiteMap<AdminRequestType>() {

            public AdminRequestType findValueByNumber(int number) {
                return AdminRequestType.valueOf(number);
            }
        };

        public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
            return getDescriptor().getValues().get(index);
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
            return getDescriptor();
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.getDescriptor().getEnumTypes().get(0);
        }

        private static final AdminRequestType[] VALUES = { GET_METADATA, UPDATE_METADATA, UPDATE_PARTITION_ENTRIES, FETCH_PARTITION_ENTRIES, DELETE_PARTITION_ENTRIES, INITIATE_FETCH_AND_UPDATE, ASYNC_OPERATION_STATUS, INITIATE_REBALANCE_NODE, ASYNC_OPERATION_STOP, ASYNC_OPERATION_LIST };

        public static AdminRequestType valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
            }
            return VALUES[desc.getIndex()];
        }

        private final int index;

        private final int value;

        private AdminRequestType(int index, int value) {
            this.index = index;
            this.value = value;
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }
    }

    public static final class GetMetadataRequest extends com.google.protobuf.GeneratedMessage {

        private GetMetadataRequest() {
        }

        private static final GetMetadataRequest defaultInstance = new GetMetadataRequest();

        public static GetMetadataRequest getDefaultInstance() {
            return defaultInstance;
        }

        public GetMetadataRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_GetMetadataRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_GetMetadataRequest_fieldAccessorTable;
        }

        public static final int KEY_FIELD_NUMBER = 1;

        private boolean hasKey;

        private com.google.protobuf.ByteString key_ = com.google.protobuf.ByteString.EMPTY;

        public boolean hasKey() {
            return hasKey;
        }

        public com.google.protobuf.ByteString getKey() {
            return key_;
        }

        public final boolean isInitialized() {
            if (!hasKey)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasKey()) {
                output.writeBytes(1, getKey());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasKey()) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getKey());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDefaultInstance())
                    return this;
                if (other.hasKey()) {
                    setKey(other.getKey());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setKey(input.readBytes());
                                break;
                            }
                    }
                }
            }

            public boolean hasKey() {
                return result.hasKey();
            }

            public com.google.protobuf.ByteString getKey() {
                return result.getKey();
            }

            public Builder setKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasKey = true;
                result.key_ = value;
                return this;
            }

            public Builder clearKey() {
                result.hasKey = false;
                result.key_ = getDefaultInstance().getKey();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class GetMetadataResponse extends com.google.protobuf.GeneratedMessage {

        private GetMetadataResponse() {
        }

        private static final GetMetadataResponse defaultInstance = new GetMetadataResponse();

        public static GetMetadataResponse getDefaultInstance() {
            return defaultInstance;
        }

        public GetMetadataResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_GetMetadataResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_GetMetadataResponse_fieldAccessorTable;
        }

        public static final int VERSION_FIELD_NUMBER = 1;

        private boolean hasVersion;

        private voldemort.client.protocol.pb.VProto.Versioned version_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();

        public boolean hasVersion() {
            return hasVersion;
        }

        public voldemort.client.protocol.pb.VProto.Versioned getVersion() {
            return version_;
        }

        public static final int ERROR_FIELD_NUMBER = 2;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasVersion()) {
                if (!getVersion().isInitialized())
                    return false;
            }
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasVersion()) {
                output.writeMessage(1, getVersion());
            }
            if (hasError()) {
                output.writeMessage(2, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasVersion()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getVersion());
            }
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse.getDefaultInstance())
                    return this;
                if (other.hasVersion()) {
                    mergeVersion(other.getVersion());
                }
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                voldemort.client.protocol.pb.VProto.Versioned.Builder subBuilder = voldemort.client.protocol.pb.VProto.Versioned.newBuilder();
                                if (hasVersion()) {
                                    subBuilder.mergeFrom(getVersion());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setVersion(subBuilder.buildPartial());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasVersion() {
                return result.hasVersion();
            }

            public voldemort.client.protocol.pb.VProto.Versioned getVersion() {
                return result.getVersion();
            }

            public Builder setVersion(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasVersion = true;
                result.version_ = value;
                return this;
            }

            public Builder setVersion(voldemort.client.protocol.pb.VProto.Versioned.Builder builderForValue) {
                result.hasVersion = true;
                result.version_ = builderForValue.build();
                return this;
            }

            public Builder mergeVersion(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (result.hasVersion() && result.version_ != voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance()) {
                    result.version_ = voldemort.client.protocol.pb.VProto.Versioned.newBuilder(result.version_).mergeFrom(value).buildPartial();
                } else {
                    result.version_ = value;
                }
                result.hasVersion = true;
                return this;
            }

            public Builder clearVersion() {
                result.hasVersion = false;
                result.version_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();
                return this;
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class UpdateMetadataRequest extends com.google.protobuf.GeneratedMessage {

        private UpdateMetadataRequest() {
        }

        private static final UpdateMetadataRequest defaultInstance = new UpdateMetadataRequest();

        public static UpdateMetadataRequest getDefaultInstance() {
            return defaultInstance;
        }

        public UpdateMetadataRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdateMetadataRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdateMetadataRequest_fieldAccessorTable;
        }

        public static final int KEY_FIELD_NUMBER = 1;

        private boolean hasKey;

        private com.google.protobuf.ByteString key_ = com.google.protobuf.ByteString.EMPTY;

        public boolean hasKey() {
            return hasKey;
        }

        public com.google.protobuf.ByteString getKey() {
            return key_;
        }

        public static final int VERSIONED_FIELD_NUMBER = 2;

        private boolean hasVersioned;

        private voldemort.client.protocol.pb.VProto.Versioned versioned_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();

        public boolean hasVersioned() {
            return hasVersioned;
        }

        public voldemort.client.protocol.pb.VProto.Versioned getVersioned() {
            return versioned_;
        }

        public final boolean isInitialized() {
            if (!hasKey)
                return false;
            if (!hasVersioned)
                return false;
            if (!getVersioned().isInitialized())
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasKey()) {
                output.writeBytes(1, getKey());
            }
            if (hasVersioned()) {
                output.writeMessage(2, getVersioned());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasKey()) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getKey());
            }
            if (hasVersioned()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getVersioned());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDefaultInstance())
                    return this;
                if (other.hasKey()) {
                    setKey(other.getKey());
                }
                if (other.hasVersioned()) {
                    mergeVersioned(other.getVersioned());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setKey(input.readBytes());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VProto.Versioned.Builder subBuilder = voldemort.client.protocol.pb.VProto.Versioned.newBuilder();
                                if (hasVersioned()) {
                                    subBuilder.mergeFrom(getVersioned());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setVersioned(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasKey() {
                return result.hasKey();
            }

            public com.google.protobuf.ByteString getKey() {
                return result.getKey();
            }

            public Builder setKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasKey = true;
                result.key_ = value;
                return this;
            }

            public Builder clearKey() {
                result.hasKey = false;
                result.key_ = getDefaultInstance().getKey();
                return this;
            }

            public boolean hasVersioned() {
                return result.hasVersioned();
            }

            public voldemort.client.protocol.pb.VProto.Versioned getVersioned() {
                return result.getVersioned();
            }

            public Builder setVersioned(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasVersioned = true;
                result.versioned_ = value;
                return this;
            }

            public Builder setVersioned(voldemort.client.protocol.pb.VProto.Versioned.Builder builderForValue) {
                result.hasVersioned = true;
                result.versioned_ = builderForValue.build();
                return this;
            }

            public Builder mergeVersioned(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (result.hasVersioned() && result.versioned_ != voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance()) {
                    result.versioned_ = voldemort.client.protocol.pb.VProto.Versioned.newBuilder(result.versioned_).mergeFrom(value).buildPartial();
                } else {
                    result.versioned_ = value;
                }
                result.hasVersioned = true;
                return this;
            }

            public Builder clearVersioned() {
                result.hasVersioned = false;
                result.versioned_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class UpdateMetadataResponse extends com.google.protobuf.GeneratedMessage {

        private UpdateMetadataResponse() {
        }

        private static final UpdateMetadataResponse defaultInstance = new UpdateMetadataResponse();

        public static UpdateMetadataResponse getDefaultInstance() {
            return defaultInstance;
        }

        public UpdateMetadataResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdateMetadataResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdateMetadataResponse_fieldAccessorTable;
        }

        public static final int ERROR_FIELD_NUMBER = 1;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasError()) {
                output.writeMessage(1, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse.getDefaultInstance())
                    return this;
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class PartitionEntry extends com.google.protobuf.GeneratedMessage {

        private PartitionEntry() {
        }

        private static final PartitionEntry defaultInstance = new PartitionEntry();

        public static PartitionEntry getDefaultInstance() {
            return defaultInstance;
        }

        public PartitionEntry getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_PartitionEntry_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_PartitionEntry_fieldAccessorTable;
        }

        public static final int KEY_FIELD_NUMBER = 1;

        private boolean hasKey;

        private com.google.protobuf.ByteString key_ = com.google.protobuf.ByteString.EMPTY;

        public boolean hasKey() {
            return hasKey;
        }

        public com.google.protobuf.ByteString getKey() {
            return key_;
        }

        public static final int VERSIONED_FIELD_NUMBER = 2;

        private boolean hasVersioned;

        private voldemort.client.protocol.pb.VProto.Versioned versioned_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();

        public boolean hasVersioned() {
            return hasVersioned;
        }

        public voldemort.client.protocol.pb.VProto.Versioned getVersioned() {
            return versioned_;
        }

        public final boolean isInitialized() {
            if (!hasKey)
                return false;
            if (!hasVersioned)
                return false;
            if (!getVersioned().isInitialized())
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasKey()) {
                output.writeBytes(1, getKey());
            }
            if (hasVersioned()) {
                output.writeMessage(2, getVersioned());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasKey()) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(1, getKey());
            }
            if (hasVersioned()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getVersioned());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.PartitionEntry parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.PartitionEntry prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.PartitionEntry result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.PartitionEntry();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.PartitionEntry internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.PartitionEntry();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.PartitionEntry getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.PartitionEntry build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.PartitionEntry buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.PartitionEntry buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.PartitionEntry returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.PartitionEntry) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.PartitionEntry) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.PartitionEntry other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance())
                    return this;
                if (other.hasKey()) {
                    setKey(other.getKey());
                }
                if (other.hasVersioned()) {
                    mergeVersioned(other.getVersioned());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setKey(input.readBytes());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VProto.Versioned.Builder subBuilder = voldemort.client.protocol.pb.VProto.Versioned.newBuilder();
                                if (hasVersioned()) {
                                    subBuilder.mergeFrom(getVersioned());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setVersioned(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasKey() {
                return result.hasKey();
            }

            public com.google.protobuf.ByteString getKey() {
                return result.getKey();
            }

            public Builder setKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasKey = true;
                result.key_ = value;
                return this;
            }

            public Builder clearKey() {
                result.hasKey = false;
                result.key_ = getDefaultInstance().getKey();
                return this;
            }

            public boolean hasVersioned() {
                return result.hasVersioned();
            }

            public voldemort.client.protocol.pb.VProto.Versioned getVersioned() {
                return result.getVersioned();
            }

            public Builder setVersioned(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasVersioned = true;
                result.versioned_ = value;
                return this;
            }

            public Builder setVersioned(voldemort.client.protocol.pb.VProto.Versioned.Builder builderForValue) {
                result.hasVersioned = true;
                result.versioned_ = builderForValue.build();
                return this;
            }

            public Builder mergeVersioned(voldemort.client.protocol.pb.VProto.Versioned value) {
                if (result.hasVersioned() && result.versioned_ != voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance()) {
                    result.versioned_ = voldemort.client.protocol.pb.VProto.Versioned.newBuilder(result.versioned_).mergeFrom(value).buildPartial();
                } else {
                    result.versioned_ = value;
                }
                result.hasVersioned = true;
                return this;
            }

            public Builder clearVersioned() {
                result.hasVersioned = false;
                result.versioned_ = voldemort.client.protocol.pb.VProto.Versioned.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class UpdatePartitionEntriesRequest extends com.google.protobuf.GeneratedMessage {

        private UpdatePartitionEntriesRequest() {
        }

        private static final UpdatePartitionEntriesRequest defaultInstance = new UpdatePartitionEntriesRequest();

        public static UpdatePartitionEntriesRequest getDefaultInstance() {
            return defaultInstance;
        }

        public UpdatePartitionEntriesRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdatePartitionEntriesRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdatePartitionEntriesRequest_fieldAccessorTable;
        }

        public static final int STORE_FIELD_NUMBER = 1;

        private boolean hasStore;

        private java.lang.String store_ = "";

        public boolean hasStore() {
            return hasStore;
        }

        public java.lang.String getStore() {
            return store_;
        }

        public static final int PARTITION_ENTRY_FIELD_NUMBER = 2;

        private boolean hasPartitionEntry;

        private voldemort.client.protocol.pb.VAdminProto.PartitionEntry partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance();

        public boolean hasPartitionEntry() {
            return hasPartitionEntry;
        }

        public voldemort.client.protocol.pb.VAdminProto.PartitionEntry getPartitionEntry() {
            return partitionEntry_;
        }

        public static final int FILTER_FIELD_NUMBER = 3;

        private boolean hasFilter;

        private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();

        public boolean hasFilter() {
            return hasFilter;
        }

        public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
            return filter_;
        }

        public final boolean isInitialized() {
            if (!hasStore)
                return false;
            if (!hasPartitionEntry)
                return false;
            if (!getPartitionEntry().isInitialized())
                return false;
            if (hasFilter()) {
                if (!getFilter().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasStore()) {
                output.writeString(1, getStore());
            }
            if (hasPartitionEntry()) {
                output.writeMessage(2, getPartitionEntry());
            }
            if (hasFilter()) {
                output.writeMessage(3, getFilter());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasStore()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(1, getStore());
            }
            if (hasPartitionEntry()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getPartitionEntry());
            }
            if (hasFilter()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getFilter());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDefaultInstance())
                    return this;
                if (other.hasStore()) {
                    setStore(other.getStore());
                }
                if (other.hasPartitionEntry()) {
                    mergePartitionEntry(other.getPartitionEntry());
                }
                if (other.hasFilter()) {
                    mergeFilter(other.getFilter());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setStore(input.readString());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VAdminProto.PartitionEntry.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.newBuilder();
                                if (hasPartitionEntry()) {
                                    subBuilder.mergeFrom(getPartitionEntry());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setPartitionEntry(subBuilder.buildPartial());
                                break;
                            }
                        case 26:
                            {
                                voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder();
                                if (hasFilter()) {
                                    subBuilder.mergeFrom(getFilter());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setFilter(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasStore() {
                return result.hasStore();
            }

            public java.lang.String getStore() {
                return result.getStore();
            }

            public Builder setStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasStore = true;
                result.store_ = value;
                return this;
            }

            public Builder clearStore() {
                result.hasStore = false;
                result.store_ = getDefaultInstance().getStore();
                return this;
            }

            public boolean hasPartitionEntry() {
                return result.hasPartitionEntry();
            }

            public voldemort.client.protocol.pb.VAdminProto.PartitionEntry getPartitionEntry() {
                return result.getPartitionEntry();
            }

            public Builder setPartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasPartitionEntry = true;
                result.partitionEntry_ = value;
                return this;
            }

            public Builder setPartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry.Builder builderForValue) {
                result.hasPartitionEntry = true;
                result.partitionEntry_ = builderForValue.build();
                return this;
            }

            public Builder mergePartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry value) {
                if (result.hasPartitionEntry() && result.partitionEntry_ != voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance()) {
                    result.partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.newBuilder(result.partitionEntry_).mergeFrom(value).buildPartial();
                } else {
                    result.partitionEntry_ = value;
                }
                result.hasPartitionEntry = true;
                return this;
            }

            public Builder clearPartitionEntry() {
                result.hasPartitionEntry = false;
                result.partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance();
                return this;
            }

            public boolean hasFilter() {
                return result.hasFilter();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
                return result.getFilter();
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasFilter = true;
                result.filter_ = value;
                return this;
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder builderForValue) {
                result.hasFilter = true;
                result.filter_ = builderForValue.build();
                return this;
            }

            public Builder mergeFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (result.hasFilter() && result.filter_ != voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance()) {
                    result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder(result.filter_).mergeFrom(value).buildPartial();
                } else {
                    result.filter_ = value;
                }
                result.hasFilter = true;
                return this;
            }

            public Builder clearFilter() {
                result.hasFilter = false;
                result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class UpdatePartitionEntriesResponse extends com.google.protobuf.GeneratedMessage {

        private UpdatePartitionEntriesResponse() {
        }

        private static final UpdatePartitionEntriesResponse defaultInstance = new UpdatePartitionEntriesResponse();

        public static UpdatePartitionEntriesResponse getDefaultInstance() {
            return defaultInstance;
        }

        public UpdatePartitionEntriesResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdatePartitionEntriesResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_UpdatePartitionEntriesResponse_fieldAccessorTable;
        }

        public static final int ERROR_FIELD_NUMBER = 1;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasError()) {
                output.writeMessage(1, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse.getDefaultInstance())
                    return this;
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class VoldemortFilter extends com.google.protobuf.GeneratedMessage {

        private VoldemortFilter() {
        }

        private static final VoldemortFilter defaultInstance = new VoldemortFilter();

        public static VoldemortFilter getDefaultInstance() {
            return defaultInstance;
        }

        public VoldemortFilter getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_VoldemortFilter_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_VoldemortFilter_fieldAccessorTable;
        }

        public static final int NAME_FIELD_NUMBER = 1;

        private boolean hasName;

        private java.lang.String name_ = "";

        public boolean hasName() {
            return hasName;
        }

        public java.lang.String getName() {
            return name_;
        }

        public static final int DATA_FIELD_NUMBER = 2;

        private boolean hasData;

        private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

        public boolean hasData() {
            return hasData;
        }

        public com.google.protobuf.ByteString getData() {
            return data_;
        }

        public final boolean isInitialized() {
            if (!hasName)
                return false;
            if (!hasData)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasName()) {
                output.writeString(1, getName());
            }
            if (hasData()) {
                output.writeBytes(2, getData());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasName()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(1, getName());
            }
            if (hasData()) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getData());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortFilter parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.VoldemortFilter();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.VoldemortFilter internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.VoldemortFilter();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.VoldemortFilter returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.VoldemortFilter) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.VoldemortFilter) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance())
                    return this;
                if (other.hasName()) {
                    setName(other.getName());
                }
                if (other.hasData()) {
                    setData(other.getData());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setName(input.readString());
                                break;
                            }
                        case 18:
                            {
                                setData(input.readBytes());
                                break;
                            }
                    }
                }
            }

            public boolean hasName() {
                return result.hasName();
            }

            public java.lang.String getName() {
                return result.getName();
            }

            public Builder setName(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasName = true;
                result.name_ = value;
                return this;
            }

            public Builder clearName() {
                result.hasName = false;
                result.name_ = getDefaultInstance().getName();
                return this;
            }

            public boolean hasData() {
                return result.hasData();
            }

            public com.google.protobuf.ByteString getData() {
                return result.getData();
            }

            public Builder setData(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasData = true;
                result.data_ = value;
                return this;
            }

            public Builder clearData() {
                result.hasData = false;
                result.data_ = getDefaultInstance().getData();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class FetchPartitionEntriesRequest extends com.google.protobuf.GeneratedMessage {

        private FetchPartitionEntriesRequest() {
        }

        private static final FetchPartitionEntriesRequest defaultInstance = new FetchPartitionEntriesRequest();

        public static FetchPartitionEntriesRequest getDefaultInstance() {
            return defaultInstance;
        }

        public FetchPartitionEntriesRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_FetchPartitionEntriesRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_FetchPartitionEntriesRequest_fieldAccessorTable;
        }

        public static final int PARTITIONS_FIELD_NUMBER = 1;

        private java.util.List<java.lang.Integer> partitions_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.Integer> getPartitionsList() {
            return partitions_;
        }

        public int getPartitionsCount() {
            return partitions_.size();
        }

        public int getPartitions(int index) {
            return partitions_.get(index);
        }

        public static final int STORE_FIELD_NUMBER = 2;

        private boolean hasStore;

        private java.lang.String store_ = "";

        public boolean hasStore() {
            return hasStore;
        }

        public java.lang.String getStore() {
            return store_;
        }

        public static final int FILTER_FIELD_NUMBER = 3;

        private boolean hasFilter;

        private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();

        public boolean hasFilter() {
            return hasFilter;
        }

        public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
            return filter_;
        }

        public static final int FETCH_VALUES_FIELD_NUMBER = 4;

        private boolean hasFetchValues;

        private boolean fetchValues_ = false;

        public boolean hasFetchValues() {
            return hasFetchValues;
        }

        public boolean getFetchValues() {
            return fetchValues_;
        }

        public final boolean isInitialized() {
            if (!hasStore)
                return false;
            if (hasFilter()) {
                if (!getFilter().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            for (int element : getPartitionsList()) {
                output.writeInt32(1, element);
            }
            if (hasStore()) {
                output.writeString(2, getStore());
            }
            if (hasFilter()) {
                output.writeMessage(3, getFilter());
            }
            if (hasFetchValues()) {
                output.writeBool(4, getFetchValues());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            {
                int dataSize = 0;
                for (int element : getPartitionsList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getPartitionsList().size();
            }
            if (hasStore()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(2, getStore());
            }
            if (hasFilter()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getFilter());
            }
            if (hasFetchValues()) {
                size += com.google.protobuf.CodedOutputStream.computeBoolSize(4, getFetchValues());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                if (result.partitions_ != java.util.Collections.EMPTY_LIST) {
                    result.partitions_ = java.util.Collections.unmodifiableList(result.partitions_);
                }
                voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDefaultInstance())
                    return this;
                if (!other.partitions_.isEmpty()) {
                    if (result.partitions_.isEmpty()) {
                        result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                    }
                    result.partitions_.addAll(other.partitions_);
                }
                if (other.hasStore()) {
                    setStore(other.getStore());
                }
                if (other.hasFilter()) {
                    mergeFilter(other.getFilter());
                }
                if (other.hasFetchValues()) {
                    setFetchValues(other.getFetchValues());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                addPartitions(input.readInt32());
                                break;
                            }
                        case 18:
                            {
                                setStore(input.readString());
                                break;
                            }
                        case 26:
                            {
                                voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder();
                                if (hasFilter()) {
                                    subBuilder.mergeFrom(getFilter());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setFilter(subBuilder.buildPartial());
                                break;
                            }
                        case 32:
                            {
                                setFetchValues(input.readBool());
                                break;
                            }
                    }
                }
            }

            public java.util.List<java.lang.Integer> getPartitionsList() {
                return java.util.Collections.unmodifiableList(result.partitions_);
            }

            public int getPartitionsCount() {
                return result.getPartitionsCount();
            }

            public int getPartitions(int index) {
                return result.getPartitions(index);
            }

            public Builder setPartitions(int index, int value) {
                result.partitions_.set(index, value);
                return this;
            }

            public Builder addPartitions(int value) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                result.partitions_.add(value);
                return this;
            }

            public Builder addAllPartitions(java.lang.Iterable<? extends java.lang.Integer> values) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                super.addAll(values, result.partitions_);
                return this;
            }

            public Builder clearPartitions() {
                result.partitions_ = java.util.Collections.emptyList();
                return this;
            }

            public boolean hasStore() {
                return result.hasStore();
            }

            public java.lang.String getStore() {
                return result.getStore();
            }

            public Builder setStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasStore = true;
                result.store_ = value;
                return this;
            }

            public Builder clearStore() {
                result.hasStore = false;
                result.store_ = getDefaultInstance().getStore();
                return this;
            }

            public boolean hasFilter() {
                return result.hasFilter();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
                return result.getFilter();
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasFilter = true;
                result.filter_ = value;
                return this;
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder builderForValue) {
                result.hasFilter = true;
                result.filter_ = builderForValue.build();
                return this;
            }

            public Builder mergeFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (result.hasFilter() && result.filter_ != voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance()) {
                    result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder(result.filter_).mergeFrom(value).buildPartial();
                } else {
                    result.filter_ = value;
                }
                result.hasFilter = true;
                return this;
            }

            public Builder clearFilter() {
                result.hasFilter = false;
                result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();
                return this;
            }

            public boolean hasFetchValues() {
                return result.hasFetchValues();
            }

            public boolean getFetchValues() {
                return result.getFetchValues();
            }

            public Builder setFetchValues(boolean value) {
                result.hasFetchValues = true;
                result.fetchValues_ = value;
                return this;
            }

            public Builder clearFetchValues() {
                result.hasFetchValues = false;
                result.fetchValues_ = false;
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class FetchPartitionEntriesResponse extends com.google.protobuf.GeneratedMessage {

        private FetchPartitionEntriesResponse() {
        }

        private static final FetchPartitionEntriesResponse defaultInstance = new FetchPartitionEntriesResponse();

        public static FetchPartitionEntriesResponse getDefaultInstance() {
            return defaultInstance;
        }

        public FetchPartitionEntriesResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_FetchPartitionEntriesResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_FetchPartitionEntriesResponse_fieldAccessorTable;
        }

        public static final int PARTITION_ENTRY_FIELD_NUMBER = 1;

        private boolean hasPartitionEntry;

        private voldemort.client.protocol.pb.VAdminProto.PartitionEntry partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance();

        public boolean hasPartitionEntry() {
            return hasPartitionEntry;
        }

        public voldemort.client.protocol.pb.VAdminProto.PartitionEntry getPartitionEntry() {
            return partitionEntry_;
        }

        public static final int KEY_FIELD_NUMBER = 2;

        private boolean hasKey;

        private com.google.protobuf.ByteString key_ = com.google.protobuf.ByteString.EMPTY;

        public boolean hasKey() {
            return hasKey;
        }

        public com.google.protobuf.ByteString getKey() {
            return key_;
        }

        public static final int ERROR_FIELD_NUMBER = 3;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasPartitionEntry()) {
                if (!getPartitionEntry().isInitialized())
                    return false;
            }
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasPartitionEntry()) {
                output.writeMessage(1, getPartitionEntry());
            }
            if (hasKey()) {
                output.writeBytes(2, getKey());
            }
            if (hasError()) {
                output.writeMessage(3, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasPartitionEntry()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getPartitionEntry());
            }
            if (hasKey()) {
                size += com.google.protobuf.CodedOutputStream.computeBytesSize(2, getKey());
            }
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse.getDefaultInstance())
                    return this;
                if (other.hasPartitionEntry()) {
                    mergePartitionEntry(other.getPartitionEntry());
                }
                if (other.hasKey()) {
                    setKey(other.getKey());
                }
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                voldemort.client.protocol.pb.VAdminProto.PartitionEntry.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.newBuilder();
                                if (hasPartitionEntry()) {
                                    subBuilder.mergeFrom(getPartitionEntry());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setPartitionEntry(subBuilder.buildPartial());
                                break;
                            }
                        case 18:
                            {
                                setKey(input.readBytes());
                                break;
                            }
                        case 26:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasPartitionEntry() {
                return result.hasPartitionEntry();
            }

            public voldemort.client.protocol.pb.VAdminProto.PartitionEntry getPartitionEntry() {
                return result.getPartitionEntry();
            }

            public Builder setPartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasPartitionEntry = true;
                result.partitionEntry_ = value;
                return this;
            }

            public Builder setPartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry.Builder builderForValue) {
                result.hasPartitionEntry = true;
                result.partitionEntry_ = builderForValue.build();
                return this;
            }

            public Builder mergePartitionEntry(voldemort.client.protocol.pb.VAdminProto.PartitionEntry value) {
                if (result.hasPartitionEntry() && result.partitionEntry_ != voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance()) {
                    result.partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.newBuilder(result.partitionEntry_).mergeFrom(value).buildPartial();
                } else {
                    result.partitionEntry_ = value;
                }
                result.hasPartitionEntry = true;
                return this;
            }

            public Builder clearPartitionEntry() {
                result.hasPartitionEntry = false;
                result.partitionEntry_ = voldemort.client.protocol.pb.VAdminProto.PartitionEntry.getDefaultInstance();
                return this;
            }

            public boolean hasKey() {
                return result.hasKey();
            }

            public com.google.protobuf.ByteString getKey() {
                return result.getKey();
            }

            public Builder setKey(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasKey = true;
                result.key_ = value;
                return this;
            }

            public Builder clearKey() {
                result.hasKey = false;
                result.key_ = getDefaultInstance().getKey();
                return this;
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class DeletePartitionEntriesRequest extends com.google.protobuf.GeneratedMessage {

        private DeletePartitionEntriesRequest() {
        }

        private static final DeletePartitionEntriesRequest defaultInstance = new DeletePartitionEntriesRequest();

        public static DeletePartitionEntriesRequest getDefaultInstance() {
            return defaultInstance;
        }

        public DeletePartitionEntriesRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_DeletePartitionEntriesRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_DeletePartitionEntriesRequest_fieldAccessorTable;
        }

        public static final int STORE_FIELD_NUMBER = 1;

        private boolean hasStore;

        private java.lang.String store_ = "";

        public boolean hasStore() {
            return hasStore;
        }

        public java.lang.String getStore() {
            return store_;
        }

        public static final int PARTITIONS_FIELD_NUMBER = 2;

        private java.util.List<java.lang.Integer> partitions_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.Integer> getPartitionsList() {
            return partitions_;
        }

        public int getPartitionsCount() {
            return partitions_.size();
        }

        public int getPartitions(int index) {
            return partitions_.get(index);
        }

        public static final int FILTER_FIELD_NUMBER = 3;

        private boolean hasFilter;

        private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();

        public boolean hasFilter() {
            return hasFilter;
        }

        public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
            return filter_;
        }

        public final boolean isInitialized() {
            if (!hasStore)
                return false;
            if (hasFilter()) {
                if (!getFilter().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasStore()) {
                output.writeString(1, getStore());
            }
            for (int element : getPartitionsList()) {
                output.writeInt32(2, element);
            }
            if (hasFilter()) {
                output.writeMessage(3, getFilter());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasStore()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(1, getStore());
            }
            {
                int dataSize = 0;
                for (int element : getPartitionsList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getPartitionsList().size();
            }
            if (hasFilter()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getFilter());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                if (result.partitions_ != java.util.Collections.EMPTY_LIST) {
                    result.partitions_ = java.util.Collections.unmodifiableList(result.partitions_);
                }
                voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDefaultInstance())
                    return this;
                if (other.hasStore()) {
                    setStore(other.getStore());
                }
                if (!other.partitions_.isEmpty()) {
                    if (result.partitions_.isEmpty()) {
                        result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                    }
                    result.partitions_.addAll(other.partitions_);
                }
                if (other.hasFilter()) {
                    mergeFilter(other.getFilter());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setStore(input.readString());
                                break;
                            }
                        case 16:
                            {
                                addPartitions(input.readInt32());
                                break;
                            }
                        case 26:
                            {
                                voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder();
                                if (hasFilter()) {
                                    subBuilder.mergeFrom(getFilter());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setFilter(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasStore() {
                return result.hasStore();
            }

            public java.lang.String getStore() {
                return result.getStore();
            }

            public Builder setStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasStore = true;
                result.store_ = value;
                return this;
            }

            public Builder clearStore() {
                result.hasStore = false;
                result.store_ = getDefaultInstance().getStore();
                return this;
            }

            public java.util.List<java.lang.Integer> getPartitionsList() {
                return java.util.Collections.unmodifiableList(result.partitions_);
            }

            public int getPartitionsCount() {
                return result.getPartitionsCount();
            }

            public int getPartitions(int index) {
                return result.getPartitions(index);
            }

            public Builder setPartitions(int index, int value) {
                result.partitions_.set(index, value);
                return this;
            }

            public Builder addPartitions(int value) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                result.partitions_.add(value);
                return this;
            }

            public Builder addAllPartitions(java.lang.Iterable<? extends java.lang.Integer> values) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                super.addAll(values, result.partitions_);
                return this;
            }

            public Builder clearPartitions() {
                result.partitions_ = java.util.Collections.emptyList();
                return this;
            }

            public boolean hasFilter() {
                return result.hasFilter();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
                return result.getFilter();
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasFilter = true;
                result.filter_ = value;
                return this;
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder builderForValue) {
                result.hasFilter = true;
                result.filter_ = builderForValue.build();
                return this;
            }

            public Builder mergeFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (result.hasFilter() && result.filter_ != voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance()) {
                    result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder(result.filter_).mergeFrom(value).buildPartial();
                } else {
                    result.filter_ = value;
                }
                result.hasFilter = true;
                return this;
            }

            public Builder clearFilter() {
                result.hasFilter = false;
                result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class DeletePartitionEntriesResponse extends com.google.protobuf.GeneratedMessage {

        private DeletePartitionEntriesResponse() {
        }

        private static final DeletePartitionEntriesResponse defaultInstance = new DeletePartitionEntriesResponse();

        public static DeletePartitionEntriesResponse getDefaultInstance() {
            return defaultInstance;
        }

        public DeletePartitionEntriesResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_DeletePartitionEntriesResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_DeletePartitionEntriesResponse_fieldAccessorTable;
        }

        public static final int COUNT_FIELD_NUMBER = 1;

        private boolean hasCount;

        private int count_ = 0;

        public boolean hasCount() {
            return hasCount;
        }

        public int getCount() {
            return count_;
        }

        public static final int ERROR_FIELD_NUMBER = 2;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (!hasCount)
                return false;
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasCount()) {
                output.writeInt32(1, getCount());
            }
            if (hasError()) {
                output.writeMessage(2, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasCount()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getCount());
            }
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse.getDefaultInstance())
                    return this;
                if (other.hasCount()) {
                    setCount(other.getCount());
                }
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setCount(input.readInt32());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasCount() {
                return result.hasCount();
            }

            public int getCount() {
                return result.getCount();
            }

            public Builder setCount(int value) {
                result.hasCount = true;
                result.count_ = value;
                return this;
            }

            public Builder clearCount() {
                result.hasCount = false;
                result.count_ = 0;
                return this;
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class InitiateFetchAndUpdateRequest extends com.google.protobuf.GeneratedMessage {

        private InitiateFetchAndUpdateRequest() {
        }

        private static final InitiateFetchAndUpdateRequest defaultInstance = new InitiateFetchAndUpdateRequest();

        public static InitiateFetchAndUpdateRequest getDefaultInstance() {
            return defaultInstance;
        }

        public InitiateFetchAndUpdateRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_InitiateFetchAndUpdateRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_InitiateFetchAndUpdateRequest_fieldAccessorTable;
        }

        public static final int NODE_ID_FIELD_NUMBER = 1;

        private boolean hasNodeId;

        private int nodeId_ = 0;

        public boolean hasNodeId() {
            return hasNodeId;
        }

        public int getNodeId() {
            return nodeId_;
        }

        public static final int PARTITIONS_FIELD_NUMBER = 2;

        private java.util.List<java.lang.Integer> partitions_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.Integer> getPartitionsList() {
            return partitions_;
        }

        public int getPartitionsCount() {
            return partitions_.size();
        }

        public int getPartitions(int index) {
            return partitions_.get(index);
        }

        public static final int STORE_FIELD_NUMBER = 3;

        private boolean hasStore;

        private java.lang.String store_ = "";

        public boolean hasStore() {
            return hasStore;
        }

        public java.lang.String getStore() {
            return store_;
        }

        public static final int FILTER_FIELD_NUMBER = 4;

        private boolean hasFilter;

        private voldemort.client.protocol.pb.VAdminProto.VoldemortFilter filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();

        public boolean hasFilter() {
            return hasFilter;
        }

        public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
            return filter_;
        }

        public final boolean isInitialized() {
            if (!hasNodeId)
                return false;
            if (!hasStore)
                return false;
            if (hasFilter()) {
                if (!getFilter().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasNodeId()) {
                output.writeInt32(1, getNodeId());
            }
            for (int element : getPartitionsList()) {
                output.writeInt32(2, element);
            }
            if (hasStore()) {
                output.writeString(3, getStore());
            }
            if (hasFilter()) {
                output.writeMessage(4, getFilter());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasNodeId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getNodeId());
            }
            {
                int dataSize = 0;
                for (int element : getPartitionsList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getPartitionsList().size();
            }
            if (hasStore()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(3, getStore());
            }
            if (hasFilter()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, getFilter());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                if (result.partitions_ != java.util.Collections.EMPTY_LIST) {
                    result.partitions_ = java.util.Collections.unmodifiableList(result.partitions_);
                }
                voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDefaultInstance())
                    return this;
                if (other.hasNodeId()) {
                    setNodeId(other.getNodeId());
                }
                if (!other.partitions_.isEmpty()) {
                    if (result.partitions_.isEmpty()) {
                        result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                    }
                    result.partitions_.addAll(other.partitions_);
                }
                if (other.hasStore()) {
                    setStore(other.getStore());
                }
                if (other.hasFilter()) {
                    mergeFilter(other.getFilter());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setNodeId(input.readInt32());
                                break;
                            }
                        case 16:
                            {
                                addPartitions(input.readInt32());
                                break;
                            }
                        case 26:
                            {
                                setStore(input.readString());
                                break;
                            }
                        case 34:
                            {
                                voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder();
                                if (hasFilter()) {
                                    subBuilder.mergeFrom(getFilter());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setFilter(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasNodeId() {
                return result.hasNodeId();
            }

            public int getNodeId() {
                return result.getNodeId();
            }

            public Builder setNodeId(int value) {
                result.hasNodeId = true;
                result.nodeId_ = value;
                return this;
            }

            public Builder clearNodeId() {
                result.hasNodeId = false;
                result.nodeId_ = 0;
                return this;
            }

            public java.util.List<java.lang.Integer> getPartitionsList() {
                return java.util.Collections.unmodifiableList(result.partitions_);
            }

            public int getPartitionsCount() {
                return result.getPartitionsCount();
            }

            public int getPartitions(int index) {
                return result.getPartitions(index);
            }

            public Builder setPartitions(int index, int value) {
                result.partitions_.set(index, value);
                return this;
            }

            public Builder addPartitions(int value) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                result.partitions_.add(value);
                return this;
            }

            public Builder addAllPartitions(java.lang.Iterable<? extends java.lang.Integer> values) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                super.addAll(values, result.partitions_);
                return this;
            }

            public Builder clearPartitions() {
                result.partitions_ = java.util.Collections.emptyList();
                return this;
            }

            public boolean hasStore() {
                return result.hasStore();
            }

            public java.lang.String getStore() {
                return result.getStore();
            }

            public Builder setStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasStore = true;
                result.store_ = value;
                return this;
            }

            public Builder clearStore() {
                result.hasStore = false;
                result.store_ = getDefaultInstance().getStore();
                return this;
            }

            public boolean hasFilter() {
                return result.hasFilter();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortFilter getFilter() {
                return result.getFilter();
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasFilter = true;
                result.filter_ = value;
                return this;
            }

            public Builder setFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder builderForValue) {
                result.hasFilter = true;
                result.filter_ = builderForValue.build();
                return this;
            }

            public Builder mergeFilter(voldemort.client.protocol.pb.VAdminProto.VoldemortFilter value) {
                if (result.hasFilter() && result.filter_ != voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance()) {
                    result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.newBuilder(result.filter_).mergeFrom(value).buildPartial();
                } else {
                    result.filter_ = value;
                }
                result.hasFilter = true;
                return this;
            }

            public Builder clearFilter() {
                result.hasFilter = false;
                result.filter_ = voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationStatusRequest extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationStatusRequest() {
        }

        private static final AsyncOperationStatusRequest defaultInstance = new AsyncOperationStatusRequest();

        public static AsyncOperationStatusRequest getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationStatusRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStatusRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStatusRequest_fieldAccessorTable;
        }

        public static final int REQUEST_ID_FIELD_NUMBER = 1;

        private boolean hasRequestId;

        private int requestId_ = 0;

        public boolean hasRequestId() {
            return hasRequestId;
        }

        public int getRequestId() {
            return requestId_;
        }

        public final boolean isInitialized() {
            if (!hasRequestId)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasRequestId()) {
                output.writeInt32(1, getRequestId());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasRequestId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getRequestId());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDefaultInstance())
                    return this;
                if (other.hasRequestId()) {
                    setRequestId(other.getRequestId());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setRequestId(input.readInt32());
                                break;
                            }
                    }
                }
            }

            public boolean hasRequestId() {
                return result.hasRequestId();
            }

            public int getRequestId() {
                return result.getRequestId();
            }

            public Builder setRequestId(int value) {
                result.hasRequestId = true;
                result.requestId_ = value;
                return this;
            }

            public Builder clearRequestId() {
                result.hasRequestId = false;
                result.requestId_ = 0;
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationStopRequest extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationStopRequest() {
        }

        private static final AsyncOperationStopRequest defaultInstance = new AsyncOperationStopRequest();

        public static AsyncOperationStopRequest getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationStopRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStopRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStopRequest_fieldAccessorTable;
        }

        public static final int REQUEST_ID_FIELD_NUMBER = 1;

        private boolean hasRequestId;

        private int requestId_ = 0;

        public boolean hasRequestId() {
            return hasRequestId;
        }

        public int getRequestId() {
            return requestId_;
        }

        public final boolean isInitialized() {
            if (!hasRequestId)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasRequestId()) {
                output.writeInt32(1, getRequestId());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasRequestId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getRequestId());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDefaultInstance())
                    return this;
                if (other.hasRequestId()) {
                    setRequestId(other.getRequestId());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setRequestId(input.readInt32());
                                break;
                            }
                    }
                }
            }

            public boolean hasRequestId() {
                return result.hasRequestId();
            }

            public int getRequestId() {
                return result.getRequestId();
            }

            public Builder setRequestId(int value) {
                result.hasRequestId = true;
                result.requestId_ = value;
                return this;
            }

            public Builder clearRequestId() {
                result.hasRequestId = false;
                result.requestId_ = 0;
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationStopResponse extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationStopResponse() {
        }

        private static final AsyncOperationStopResponse defaultInstance = new AsyncOperationStopResponse();

        public static AsyncOperationStopResponse getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationStopResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStopResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStopResponse_fieldAccessorTable;
        }

        public static final int ERROR_FIELD_NUMBER = 1;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasError()) {
                output.writeMessage(1, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(1, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse.getDefaultInstance())
                    return this;
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationListRequest extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationListRequest() {
        }

        private static final AsyncOperationListRequest defaultInstance = new AsyncOperationListRequest();

        public static AsyncOperationListRequest getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationListRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationListRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationListRequest_fieldAccessorTable;
        }

        public static final int REQUEST_ID_FIELD_NUMBER = 1;

        private boolean hasRequestId;

        private int requestId_ = 0;

        public boolean hasRequestId() {
            return hasRequestId;
        }

        public int getRequestId() {
            return requestId_;
        }

        public static final int SHOW_COMPLETE_FIELD_NUMBER = 2;

        private boolean hasShowComplete;

        private boolean showComplete_ = false;

        public boolean hasShowComplete() {
            return hasShowComplete;
        }

        public boolean getShowComplete() {
            return showComplete_;
        }

        public final boolean isInitialized() {
            if (!hasRequestId)
                return false;
            if (!hasShowComplete)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasRequestId()) {
                output.writeInt32(1, getRequestId());
            }
            if (hasShowComplete()) {
                output.writeBool(2, getShowComplete());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasRequestId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getRequestId());
            }
            if (hasShowComplete()) {
                size += com.google.protobuf.CodedOutputStream.computeBoolSize(2, getShowComplete());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDefaultInstance())
                    return this;
                if (other.hasRequestId()) {
                    setRequestId(other.getRequestId());
                }
                if (other.hasShowComplete()) {
                    setShowComplete(other.getShowComplete());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setRequestId(input.readInt32());
                                break;
                            }
                        case 16:
                            {
                                setShowComplete(input.readBool());
                                break;
                            }
                    }
                }
            }

            public boolean hasRequestId() {
                return result.hasRequestId();
            }

            public int getRequestId() {
                return result.getRequestId();
            }

            public Builder setRequestId(int value) {
                result.hasRequestId = true;
                result.requestId_ = value;
                return this;
            }

            public Builder clearRequestId() {
                result.hasRequestId = false;
                result.requestId_ = 0;
                return this;
            }

            public boolean hasShowComplete() {
                return result.hasShowComplete();
            }

            public boolean getShowComplete() {
                return result.getShowComplete();
            }

            public Builder setShowComplete(boolean value) {
                result.hasShowComplete = true;
                result.showComplete_ = value;
                return this;
            }

            public Builder clearShowComplete() {
                result.hasShowComplete = false;
                result.showComplete_ = false;
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationListResponse extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationListResponse() {
        }

        private static final AsyncOperationListResponse defaultInstance = new AsyncOperationListResponse();

        public static AsyncOperationListResponse getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationListResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationListResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationListResponse_fieldAccessorTable;
        }

        public static final int REQUEST_IDS_FIELD_NUMBER = 1;

        private java.util.List<java.lang.Integer> requestIds_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.Integer> getRequestIdsList() {
            return requestIds_;
        }

        public int getRequestIdsCount() {
            return requestIds_.size();
        }

        public int getRequestIds(int index) {
            return requestIds_.get(index);
        }

        public static final int ERROR_FIELD_NUMBER = 2;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            for (int element : getRequestIdsList()) {
                output.writeInt32(1, element);
            }
            if (hasError()) {
                output.writeMessage(2, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            {
                int dataSize = 0;
                for (int element : getRequestIdsList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getRequestIdsList().size();
            }
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                if (result.requestIds_ != java.util.Collections.EMPTY_LIST) {
                    result.requestIds_ = java.util.Collections.unmodifiableList(result.requestIds_);
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse.getDefaultInstance())
                    return this;
                if (!other.requestIds_.isEmpty()) {
                    if (result.requestIds_.isEmpty()) {
                        result.requestIds_ = new java.util.ArrayList<java.lang.Integer>();
                    }
                    result.requestIds_.addAll(other.requestIds_);
                }
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                addRequestIds(input.readInt32());
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public java.util.List<java.lang.Integer> getRequestIdsList() {
                return java.util.Collections.unmodifiableList(result.requestIds_);
            }

            public int getRequestIdsCount() {
                return result.getRequestIdsCount();
            }

            public int getRequestIds(int index) {
                return result.getRequestIds(index);
            }

            public Builder setRequestIds(int index, int value) {
                result.requestIds_.set(index, value);
                return this;
            }

            public Builder addRequestIds(int value) {
                if (result.requestIds_.isEmpty()) {
                    result.requestIds_ = new java.util.ArrayList<java.lang.Integer>();
                }
                result.requestIds_.add(value);
                return this;
            }

            public Builder addAllRequestIds(java.lang.Iterable<? extends java.lang.Integer> values) {
                if (result.requestIds_.isEmpty()) {
                    result.requestIds_ = new java.util.ArrayList<java.lang.Integer>();
                }
                super.addAll(values, result.requestIds_);
                return this;
            }

            public Builder clearRequestIds() {
                result.requestIds_ = java.util.Collections.emptyList();
                return this;
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class InitiateRebalanceNodeRequest extends com.google.protobuf.GeneratedMessage {

        private InitiateRebalanceNodeRequest() {
        }

        private static final InitiateRebalanceNodeRequest defaultInstance = new InitiateRebalanceNodeRequest();

        public static InitiateRebalanceNodeRequest getDefaultInstance() {
            return defaultInstance;
        }

        public InitiateRebalanceNodeRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_InitiateRebalanceNodeRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_InitiateRebalanceNodeRequest_fieldAccessorTable;
        }

        public static final int CURRENT_STORE_FIELD_NUMBER = 1;

        private boolean hasCurrentStore;

        private java.lang.String currentStore_ = "";

        public boolean hasCurrentStore() {
            return hasCurrentStore;
        }

        public java.lang.String getCurrentStore() {
            return currentStore_;
        }

        public static final int STEALER_ID_FIELD_NUMBER = 2;

        private boolean hasStealerId;

        private int stealerId_ = 0;

        public boolean hasStealerId() {
            return hasStealerId;
        }

        public int getStealerId() {
            return stealerId_;
        }

        public static final int DONOR_ID_FIELD_NUMBER = 3;

        private boolean hasDonorId;

        private int donorId_ = 0;

        public boolean hasDonorId() {
            return hasDonorId;
        }

        public int getDonorId() {
            return donorId_;
        }

        public static final int PARTITIONS_FIELD_NUMBER = 4;

        private java.util.List<java.lang.Integer> partitions_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.Integer> getPartitionsList() {
            return partitions_;
        }

        public int getPartitionsCount() {
            return partitions_.size();
        }

        public int getPartitions(int index) {
            return partitions_.get(index);
        }

        public static final int ATTEMPT_FIELD_NUMBER = 5;

        private boolean hasAttempt;

        private int attempt_ = 0;

        public boolean hasAttempt() {
            return hasAttempt;
        }

        public int getAttempt() {
            return attempt_;
        }

        public static final int UNBALANCED_STORE_FIELD_NUMBER = 6;

        private java.util.List<java.lang.String> unbalancedStore_ = java.util.Collections.emptyList();

        public java.util.List<java.lang.String> getUnbalancedStoreList() {
            return unbalancedStore_;
        }

        public int getUnbalancedStoreCount() {
            return unbalancedStore_.size();
        }

        public java.lang.String getUnbalancedStore(int index) {
            return unbalancedStore_.get(index);
        }

        public final boolean isInitialized() {
            if (!hasCurrentStore)
                return false;
            if (!hasStealerId)
                return false;
            if (!hasDonorId)
                return false;
            if (!hasAttempt)
                return false;
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasCurrentStore()) {
                output.writeString(1, getCurrentStore());
            }
            if (hasStealerId()) {
                output.writeInt32(2, getStealerId());
            }
            if (hasDonorId()) {
                output.writeInt32(3, getDonorId());
            }
            for (int element : getPartitionsList()) {
                output.writeInt32(4, element);
            }
            if (hasAttempt()) {
                output.writeInt32(5, getAttempt());
            }
            for (java.lang.String element : getUnbalancedStoreList()) {
                output.writeString(6, element);
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasCurrentStore()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(1, getCurrentStore());
            }
            if (hasStealerId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(2, getStealerId());
            }
            if (hasDonorId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(3, getDonorId());
            }
            {
                int dataSize = 0;
                for (int element : getPartitionsList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeInt32SizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getPartitionsList().size();
            }
            if (hasAttempt()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(5, getAttempt());
            }
            {
                int dataSize = 0;
                for (java.lang.String element : getUnbalancedStoreList()) {
                    dataSize += com.google.protobuf.CodedOutputStream.computeStringSizeNoTag(element);
                }
                size += dataSize;
                size += 1 * getUnbalancedStoreList().size();
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                if (result.partitions_ != java.util.Collections.EMPTY_LIST) {
                    result.partitions_ = java.util.Collections.unmodifiableList(result.partitions_);
                }
                if (result.unbalancedStore_ != java.util.Collections.EMPTY_LIST) {
                    result.unbalancedStore_ = java.util.Collections.unmodifiableList(result.unbalancedStore_);
                }
                voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDefaultInstance())
                    return this;
                if (other.hasCurrentStore()) {
                    setCurrentStore(other.getCurrentStore());
                }
                if (other.hasStealerId()) {
                    setStealerId(other.getStealerId());
                }
                if (other.hasDonorId()) {
                    setDonorId(other.getDonorId());
                }
                if (!other.partitions_.isEmpty()) {
                    if (result.partitions_.isEmpty()) {
                        result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                    }
                    result.partitions_.addAll(other.partitions_);
                }
                if (other.hasAttempt()) {
                    setAttempt(other.getAttempt());
                }
                if (!other.unbalancedStore_.isEmpty()) {
                    if (result.unbalancedStore_.isEmpty()) {
                        result.unbalancedStore_ = new java.util.ArrayList<java.lang.String>();
                    }
                    result.unbalancedStore_.addAll(other.unbalancedStore_);
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 10:
                            {
                                setCurrentStore(input.readString());
                                break;
                            }
                        case 16:
                            {
                                setStealerId(input.readInt32());
                                break;
                            }
                        case 24:
                            {
                                setDonorId(input.readInt32());
                                break;
                            }
                        case 32:
                            {
                                addPartitions(input.readInt32());
                                break;
                            }
                        case 40:
                            {
                                setAttempt(input.readInt32());
                                break;
                            }
                        case 50:
                            {
                                addUnbalancedStore(input.readString());
                                break;
                            }
                    }
                }
            }

            public boolean hasCurrentStore() {
                return result.hasCurrentStore();
            }

            public java.lang.String getCurrentStore() {
                return result.getCurrentStore();
            }

            public Builder setCurrentStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasCurrentStore = true;
                result.currentStore_ = value;
                return this;
            }

            public Builder clearCurrentStore() {
                result.hasCurrentStore = false;
                result.currentStore_ = getDefaultInstance().getCurrentStore();
                return this;
            }

            public boolean hasStealerId() {
                return result.hasStealerId();
            }

            public int getStealerId() {
                return result.getStealerId();
            }

            public Builder setStealerId(int value) {
                result.hasStealerId = true;
                result.stealerId_ = value;
                return this;
            }

            public Builder clearStealerId() {
                result.hasStealerId = false;
                result.stealerId_ = 0;
                return this;
            }

            public boolean hasDonorId() {
                return result.hasDonorId();
            }

            public int getDonorId() {
                return result.getDonorId();
            }

            public Builder setDonorId(int value) {
                result.hasDonorId = true;
                result.donorId_ = value;
                return this;
            }

            public Builder clearDonorId() {
                result.hasDonorId = false;
                result.donorId_ = 0;
                return this;
            }

            public java.util.List<java.lang.Integer> getPartitionsList() {
                return java.util.Collections.unmodifiableList(result.partitions_);
            }

            public int getPartitionsCount() {
                return result.getPartitionsCount();
            }

            public int getPartitions(int index) {
                return result.getPartitions(index);
            }

            public Builder setPartitions(int index, int value) {
                result.partitions_.set(index, value);
                return this;
            }

            public Builder addPartitions(int value) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                result.partitions_.add(value);
                return this;
            }

            public Builder addAllPartitions(java.lang.Iterable<? extends java.lang.Integer> values) {
                if (result.partitions_.isEmpty()) {
                    result.partitions_ = new java.util.ArrayList<java.lang.Integer>();
                }
                super.addAll(values, result.partitions_);
                return this;
            }

            public Builder clearPartitions() {
                result.partitions_ = java.util.Collections.emptyList();
                return this;
            }

            public boolean hasAttempt() {
                return result.hasAttempt();
            }

            public int getAttempt() {
                return result.getAttempt();
            }

            public Builder setAttempt(int value) {
                result.hasAttempt = true;
                result.attempt_ = value;
                return this;
            }

            public Builder clearAttempt() {
                result.hasAttempt = false;
                result.attempt_ = 0;
                return this;
            }

            public java.util.List<java.lang.String> getUnbalancedStoreList() {
                return java.util.Collections.unmodifiableList(result.unbalancedStore_);
            }

            public int getUnbalancedStoreCount() {
                return result.getUnbalancedStoreCount();
            }

            public java.lang.String getUnbalancedStore(int index) {
                return result.getUnbalancedStore(index);
            }

            public Builder setUnbalancedStore(int index, java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.unbalancedStore_.set(index, value);
                return this;
            }

            public Builder addUnbalancedStore(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                if (result.unbalancedStore_.isEmpty()) {
                    result.unbalancedStore_ = new java.util.ArrayList<java.lang.String>();
                }
                result.unbalancedStore_.add(value);
                return this;
            }

            public Builder addAllUnbalancedStore(java.lang.Iterable<? extends java.lang.String> values) {
                if (result.unbalancedStore_.isEmpty()) {
                    result.unbalancedStore_ = new java.util.ArrayList<java.lang.String>();
                }
                super.addAll(values, result.unbalancedStore_);
                return this;
            }

            public Builder clearUnbalancedStore() {
                result.unbalancedStore_ = java.util.Collections.emptyList();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class AsyncOperationStatusResponse extends com.google.protobuf.GeneratedMessage {

        private AsyncOperationStatusResponse() {
        }

        private static final AsyncOperationStatusResponse defaultInstance = new AsyncOperationStatusResponse();

        public static AsyncOperationStatusResponse getDefaultInstance() {
            return defaultInstance;
        }

        public AsyncOperationStatusResponse getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStatusResponse_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_AsyncOperationStatusResponse_fieldAccessorTable;
        }

        public static final int REQUEST_ID_FIELD_NUMBER = 1;

        private boolean hasRequestId;

        private int requestId_ = 0;

        public boolean hasRequestId() {
            return hasRequestId;
        }

        public int getRequestId() {
            return requestId_;
        }

        public static final int DESCRIPTION_FIELD_NUMBER = 2;

        private boolean hasDescription;

        private java.lang.String description_ = "";

        public boolean hasDescription() {
            return hasDescription;
        }

        public java.lang.String getDescription() {
            return description_;
        }

        public static final int STATUS_FIELD_NUMBER = 3;

        private boolean hasStatus;

        private java.lang.String status_ = "";

        public boolean hasStatus() {
            return hasStatus;
        }

        public java.lang.String getStatus() {
            return status_;
        }

        public static final int COMPLETE_FIELD_NUMBER = 4;

        private boolean hasComplete;

        private boolean complete_ = false;

        public boolean hasComplete() {
            return hasComplete;
        }

        public boolean getComplete() {
            return complete_;
        }

        public static final int ERROR_FIELD_NUMBER = 5;

        private boolean hasError;

        private voldemort.client.protocol.pb.VProto.Error error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();

        public boolean hasError() {
            return hasError;
        }

        public voldemort.client.protocol.pb.VProto.Error getError() {
            return error_;
        }

        public final boolean isInitialized() {
            if (!hasRequestId)
                return false;
            if (!hasDescription)
                return false;
            if (!hasStatus)
                return false;
            if (!hasComplete)
                return false;
            if (hasError()) {
                if (!getError().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasRequestId()) {
                output.writeInt32(1, getRequestId());
            }
            if (hasDescription()) {
                output.writeString(2, getDescription());
            }
            if (hasStatus()) {
                output.writeString(3, getStatus());
            }
            if (hasComplete()) {
                output.writeBool(4, getComplete());
            }
            if (hasError()) {
                output.writeMessage(5, getError());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasRequestId()) {
                size += com.google.protobuf.CodedOutputStream.computeInt32Size(1, getRequestId());
            }
            if (hasDescription()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(2, getDescription());
            }
            if (hasStatus()) {
                size += com.google.protobuf.CodedOutputStream.computeStringSize(3, getStatus());
            }
            if (hasComplete()) {
                size += com.google.protobuf.CodedOutputStream.computeBoolSize(4, getComplete());
            }
            if (hasError()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, getError());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse.getDefaultInstance())
                    return this;
                if (other.hasRequestId()) {
                    setRequestId(other.getRequestId());
                }
                if (other.hasDescription()) {
                    setDescription(other.getDescription());
                }
                if (other.hasStatus()) {
                    setStatus(other.getStatus());
                }
                if (other.hasComplete()) {
                    setComplete(other.getComplete());
                }
                if (other.hasError()) {
                    mergeError(other.getError());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                setRequestId(input.readInt32());
                                break;
                            }
                        case 18:
                            {
                                setDescription(input.readString());
                                break;
                            }
                        case 26:
                            {
                                setStatus(input.readString());
                                break;
                            }
                        case 32:
                            {
                                setComplete(input.readBool());
                                break;
                            }
                        case 42:
                            {
                                voldemort.client.protocol.pb.VProto.Error.Builder subBuilder = voldemort.client.protocol.pb.VProto.Error.newBuilder();
                                if (hasError()) {
                                    subBuilder.mergeFrom(getError());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setError(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasRequestId() {
                return result.hasRequestId();
            }

            public int getRequestId() {
                return result.getRequestId();
            }

            public Builder setRequestId(int value) {
                result.hasRequestId = true;
                result.requestId_ = value;
                return this;
            }

            public Builder clearRequestId() {
                result.hasRequestId = false;
                result.requestId_ = 0;
                return this;
            }

            public boolean hasDescription() {
                return result.hasDescription();
            }

            public java.lang.String getDescription() {
                return result.getDescription();
            }

            public Builder setDescription(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasDescription = true;
                result.description_ = value;
                return this;
            }

            public Builder clearDescription() {
                result.hasDescription = false;
                result.description_ = getDefaultInstance().getDescription();
                return this;
            }

            public boolean hasStatus() {
                return result.hasStatus();
            }

            public java.lang.String getStatus() {
                return result.getStatus();
            }

            public Builder setStatus(java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasStatus = true;
                result.status_ = value;
                return this;
            }

            public Builder clearStatus() {
                result.hasStatus = false;
                result.status_ = getDefaultInstance().getStatus();
                return this;
            }

            public boolean hasComplete() {
                return result.hasComplete();
            }

            public boolean getComplete() {
                return result.getComplete();
            }

            public Builder setComplete(boolean value) {
                result.hasComplete = true;
                result.complete_ = value;
                return this;
            }

            public Builder clearComplete() {
                result.hasComplete = false;
                result.complete_ = false;
                return this;
            }

            public boolean hasError() {
                return result.hasError();
            }

            public voldemort.client.protocol.pb.VProto.Error getError() {
                return result.getError();
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasError = true;
                result.error_ = value;
                return this;
            }

            public Builder setError(voldemort.client.protocol.pb.VProto.Error.Builder builderForValue) {
                result.hasError = true;
                result.error_ = builderForValue.build();
                return this;
            }

            public Builder mergeError(voldemort.client.protocol.pb.VProto.Error value) {
                if (result.hasError() && result.error_ != voldemort.client.protocol.pb.VProto.Error.getDefaultInstance()) {
                    result.error_ = voldemort.client.protocol.pb.VProto.Error.newBuilder(result.error_).mergeFrom(value).buildPartial();
                } else {
                    result.error_ = value;
                }
                result.hasError = true;
                return this;
            }

            public Builder clearError() {
                result.hasError = false;
                result.error_ = voldemort.client.protocol.pb.VProto.Error.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    public static final class VoldemortAdminRequest extends com.google.protobuf.GeneratedMessage {

        private VoldemortAdminRequest() {
        }

        private static final VoldemortAdminRequest defaultInstance = new VoldemortAdminRequest();

        public static VoldemortAdminRequest getDefaultInstance() {
            return defaultInstance;
        }

        public VoldemortAdminRequest getDefaultInstanceForType() {
            return defaultInstance;
        }

        public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_VoldemortAdminRequest_descriptor;
        }

        protected com.google.protobuf.GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable() {
            return voldemort.client.protocol.pb.VAdminProto.internal_static_voldemort_VoldemortAdminRequest_fieldAccessorTable;
        }

        public static final int TYPE_FIELD_NUMBER = 1;

        private boolean hasType;

        private voldemort.client.protocol.pb.VAdminProto.AdminRequestType type_ = voldemort.client.protocol.pb.VAdminProto.AdminRequestType.GET_METADATA;

        public boolean hasType() {
            return hasType;
        }

        public voldemort.client.protocol.pb.VAdminProto.AdminRequestType getType() {
            return type_;
        }

        public static final int GET_METADATA_FIELD_NUMBER = 2;

        private boolean hasGetMetadata;

        private voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest getMetadata_ = voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDefaultInstance();

        public boolean hasGetMetadata() {
            return hasGetMetadata;
        }

        public voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest getGetMetadata() {
            return getMetadata_;
        }

        public static final int UPDATE_METADATA_FIELD_NUMBER = 3;

        private boolean hasUpdateMetadata;

        private voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest updateMetadata_ = voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDefaultInstance();

        public boolean hasUpdateMetadata() {
            return hasUpdateMetadata;
        }

        public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest getUpdateMetadata() {
            return updateMetadata_;
        }

        public static final int UPDATE_PARTITION_ENTRIES_FIELD_NUMBER = 4;

        private boolean hasUpdatePartitionEntries;

        private voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest updatePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDefaultInstance();

        public boolean hasUpdatePartitionEntries() {
            return hasUpdatePartitionEntries;
        }

        public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest getUpdatePartitionEntries() {
            return updatePartitionEntries_;
        }

        public static final int FETCH_PARTITION_ENTRIES_FIELD_NUMBER = 5;

        private boolean hasFetchPartitionEntries;

        private voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest fetchPartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDefaultInstance();

        public boolean hasFetchPartitionEntries() {
            return hasFetchPartitionEntries;
        }

        public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest getFetchPartitionEntries() {
            return fetchPartitionEntries_;
        }

        public static final int DELETE_PARTITION_ENTRIES_FIELD_NUMBER = 6;

        private boolean hasDeletePartitionEntries;

        private voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest deletePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDefaultInstance();

        public boolean hasDeletePartitionEntries() {
            return hasDeletePartitionEntries;
        }

        public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest getDeletePartitionEntries() {
            return deletePartitionEntries_;
        }

        public static final int INITIATE_FETCH_AND_UPDATE_FIELD_NUMBER = 7;

        private boolean hasInitiateFetchAndUpdate;

        private voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest initiateFetchAndUpdate_ = voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDefaultInstance();

        public boolean hasInitiateFetchAndUpdate() {
            return hasInitiateFetchAndUpdate;
        }

        public voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest getInitiateFetchAndUpdate() {
            return initiateFetchAndUpdate_;
        }

        public static final int ASYNC_OPERATION_STATUS_FIELD_NUMBER = 8;

        private boolean hasAsyncOperationStatus;

        private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest asyncOperationStatus_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDefaultInstance();

        public boolean hasAsyncOperationStatus() {
            return hasAsyncOperationStatus;
        }

        public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest getAsyncOperationStatus() {
            return asyncOperationStatus_;
        }

        public static final int INITIATE_REBALANCE_NODE_FIELD_NUMBER = 9;

        private boolean hasInitiateRebalanceNode;

        private voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest initiateRebalanceNode_ = voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDefaultInstance();

        public boolean hasInitiateRebalanceNode() {
            return hasInitiateRebalanceNode;
        }

        public voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest getInitiateRebalanceNode() {
            return initiateRebalanceNode_;
        }

        public static final int ASYNC_OPERATION_STOP_FIELD_NUMBER = 10;

        private boolean hasAsyncOperationStop;

        private voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest asyncOperationStop_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDefaultInstance();

        public boolean hasAsyncOperationStop() {
            return hasAsyncOperationStop;
        }

        public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest getAsyncOperationStop() {
            return asyncOperationStop_;
        }

        public static final int ASYNC_OPERATION_LIST_FIELD_NUMBER = 11;

        private boolean hasAsyncOperationList;

        private voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest asyncOperationList_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDefaultInstance();

        public boolean hasAsyncOperationList() {
            return hasAsyncOperationList;
        }

        public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest getAsyncOperationList() {
            return asyncOperationList_;
        }

        public final boolean isInitialized() {
            if (!hasType)
                return false;
            if (hasGetMetadata()) {
                if (!getGetMetadata().isInitialized())
                    return false;
            }
            if (hasUpdateMetadata()) {
                if (!getUpdateMetadata().isInitialized())
                    return false;
            }
            if (hasUpdatePartitionEntries()) {
                if (!getUpdatePartitionEntries().isInitialized())
                    return false;
            }
            if (hasFetchPartitionEntries()) {
                if (!getFetchPartitionEntries().isInitialized())
                    return false;
            }
            if (hasDeletePartitionEntries()) {
                if (!getDeletePartitionEntries().isInitialized())
                    return false;
            }
            if (hasInitiateFetchAndUpdate()) {
                if (!getInitiateFetchAndUpdate().isInitialized())
                    return false;
            }
            if (hasAsyncOperationStatus()) {
                if (!getAsyncOperationStatus().isInitialized())
                    return false;
            }
            if (hasInitiateRebalanceNode()) {
                if (!getInitiateRebalanceNode().isInitialized())
                    return false;
            }
            if (hasAsyncOperationStop()) {
                if (!getAsyncOperationStop().isInitialized())
                    return false;
            }
            if (hasAsyncOperationList()) {
                if (!getAsyncOperationList().isInitialized())
                    return false;
            }
            return true;
        }

        public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
            if (hasType()) {
                output.writeEnum(1, getType().getNumber());
            }
            if (hasGetMetadata()) {
                output.writeMessage(2, getGetMetadata());
            }
            if (hasUpdateMetadata()) {
                output.writeMessage(3, getUpdateMetadata());
            }
            if (hasUpdatePartitionEntries()) {
                output.writeMessage(4, getUpdatePartitionEntries());
            }
            if (hasFetchPartitionEntries()) {
                output.writeMessage(5, getFetchPartitionEntries());
            }
            if (hasDeletePartitionEntries()) {
                output.writeMessage(6, getDeletePartitionEntries());
            }
            if (hasInitiateFetchAndUpdate()) {
                output.writeMessage(7, getInitiateFetchAndUpdate());
            }
            if (hasAsyncOperationStatus()) {
                output.writeMessage(8, getAsyncOperationStatus());
            }
            if (hasInitiateRebalanceNode()) {
                output.writeMessage(9, getInitiateRebalanceNode());
            }
            if (hasAsyncOperationStop()) {
                output.writeMessage(10, getAsyncOperationStop());
            }
            if (hasAsyncOperationList()) {
                output.writeMessage(11, getAsyncOperationList());
            }
            getUnknownFields().writeTo(output);
        }

        private int memoizedSerializedSize = -1;

        public int getSerializedSize() {
            int size = memoizedSerializedSize;
            if (size != -1)
                return size;
            size = 0;
            if (hasType()) {
                size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, getType().getNumber());
            }
            if (hasGetMetadata()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(2, getGetMetadata());
            }
            if (hasUpdateMetadata()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(3, getUpdateMetadata());
            }
            if (hasUpdatePartitionEntries()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(4, getUpdatePartitionEntries());
            }
            if (hasFetchPartitionEntries()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(5, getFetchPartitionEntries());
            }
            if (hasDeletePartitionEntries()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(6, getDeletePartitionEntries());
            }
            if (hasInitiateFetchAndUpdate()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(7, getInitiateFetchAndUpdate());
            }
            if (hasAsyncOperationStatus()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(8, getAsyncOperationStatus());
            }
            if (hasInitiateRebalanceNode()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(9, getInitiateRebalanceNode());
            }
            if (hasAsyncOperationStop()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(10, getAsyncOperationStop());
            }
            if (hasAsyncOperationList()) {
                size += com.google.protobuf.CodedOutputStream.computeMessageSize(11, getAsyncOperationList());
            }
            size += getUnknownFields().getSerializedSize();
            memoizedSerializedSize = size;
            return size;
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
            return newBuilder().mergeFrom(data, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeDelimitedFrom(input, extensionRegistry).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
            return newBuilder().mergeFrom(input).buildParsed();
        }

        public static voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
            return newBuilder().mergeFrom(input, extensionRegistry).buildParsed();
        }

        public static Builder newBuilder() {
            return Builder.create();
        }

        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder(voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest prototype) {
            return newBuilder().mergeFrom(prototype);
        }

        public Builder toBuilder() {
            return newBuilder(this);
        }

        public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Builder> {

            private voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest result;

            private Builder() {
            }

            private static Builder create() {
                Builder builder = new Builder();
                builder.result = new voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest();
                return builder;
            }

            protected voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest internalGetResult() {
                return result;
            }

            public Builder clear() {
                if (result == null) {
                    throw new IllegalStateException("Cannot call clear() after build().");
                }
                result = new voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest();
                return this;
            }

            public Builder clone() {
                return create().mergeFrom(result);
            }

            public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
                return voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest.getDescriptor();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest getDefaultInstanceForType() {
                return voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest.getDefaultInstance();
            }

            public boolean isInitialized() {
                return result.isInitialized();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest build() {
                if (result != null && !isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return buildPartial();
            }

            private voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest buildParsed() throws com.google.protobuf.InvalidProtocolBufferException {
                if (!isInitialized()) {
                    throw newUninitializedMessageException(result).asInvalidProtocolBufferException();
                }
                return buildPartial();
            }

            public voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest buildPartial() {
                if (result == null) {
                    throw new IllegalStateException("build() has already been called on this Builder.");
                }
                voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest returnMe = result;
                result = null;
                return returnMe;
            }

            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest) {
                    return mergeFrom((voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest other) {
                if (other == voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest.getDefaultInstance())
                    return this;
                if (other.hasType()) {
                    setType(other.getType());
                }
                if (other.hasGetMetadata()) {
                    mergeGetMetadata(other.getGetMetadata());
                }
                if (other.hasUpdateMetadata()) {
                    mergeUpdateMetadata(other.getUpdateMetadata());
                }
                if (other.hasUpdatePartitionEntries()) {
                    mergeUpdatePartitionEntries(other.getUpdatePartitionEntries());
                }
                if (other.hasFetchPartitionEntries()) {
                    mergeFetchPartitionEntries(other.getFetchPartitionEntries());
                }
                if (other.hasDeletePartitionEntries()) {
                    mergeDeletePartitionEntries(other.getDeletePartitionEntries());
                }
                if (other.hasInitiateFetchAndUpdate()) {
                    mergeInitiateFetchAndUpdate(other.getInitiateFetchAndUpdate());
                }
                if (other.hasAsyncOperationStatus()) {
                    mergeAsyncOperationStatus(other.getAsyncOperationStatus());
                }
                if (other.hasInitiateRebalanceNode()) {
                    mergeInitiateRebalanceNode(other.getInitiateRebalanceNode());
                }
                if (other.hasAsyncOperationStop()) {
                    mergeAsyncOperationStop(other.getAsyncOperationStop());
                }
                if (other.hasAsyncOperationList()) {
                    mergeAsyncOperationList(other.getAsyncOperationList());
                }
                this.mergeUnknownFields(other.getUnknownFields());
                return this;
            }

            public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
                com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder(this.getUnknownFields());
                while (true) {
                    int tag = input.readTag();
                    switch(tag) {
                        case 0:
                            this.setUnknownFields(unknownFields.build());
                            return this;
                        default:
                            {
                                if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                                    this.setUnknownFields(unknownFields.build());
                                    return this;
                                }
                                break;
                            }
                        case 8:
                            {
                                int rawValue = input.readEnum();
                                voldemort.client.protocol.pb.VAdminProto.AdminRequestType value = voldemort.client.protocol.pb.VAdminProto.AdminRequestType.valueOf(rawValue);
                                if (value == null) {
                                    unknownFields.mergeVarintField(1, rawValue);
                                } else {
                                    setType(value);
                                }
                                break;
                            }
                        case 18:
                            {
                                voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.newBuilder();
                                if (hasGetMetadata()) {
                                    subBuilder.mergeFrom(getGetMetadata());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setGetMetadata(subBuilder.buildPartial());
                                break;
                            }
                        case 26:
                            {
                                voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.newBuilder();
                                if (hasUpdateMetadata()) {
                                    subBuilder.mergeFrom(getUpdateMetadata());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setUpdateMetadata(subBuilder.buildPartial());
                                break;
                            }
                        case 34:
                            {
                                voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.newBuilder();
                                if (hasUpdatePartitionEntries()) {
                                    subBuilder.mergeFrom(getUpdatePartitionEntries());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setUpdatePartitionEntries(subBuilder.buildPartial());
                                break;
                            }
                        case 42:
                            {
                                voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.newBuilder();
                                if (hasFetchPartitionEntries()) {
                                    subBuilder.mergeFrom(getFetchPartitionEntries());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setFetchPartitionEntries(subBuilder.buildPartial());
                                break;
                            }
                        case 50:
                            {
                                voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.newBuilder();
                                if (hasDeletePartitionEntries()) {
                                    subBuilder.mergeFrom(getDeletePartitionEntries());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setDeletePartitionEntries(subBuilder.buildPartial());
                                break;
                            }
                        case 58:
                            {
                                voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.newBuilder();
                                if (hasInitiateFetchAndUpdate()) {
                                    subBuilder.mergeFrom(getInitiateFetchAndUpdate());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setInitiateFetchAndUpdate(subBuilder.buildPartial());
                                break;
                            }
                        case 66:
                            {
                                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.newBuilder();
                                if (hasAsyncOperationStatus()) {
                                    subBuilder.mergeFrom(getAsyncOperationStatus());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setAsyncOperationStatus(subBuilder.buildPartial());
                                break;
                            }
                        case 74:
                            {
                                voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.newBuilder();
                                if (hasInitiateRebalanceNode()) {
                                    subBuilder.mergeFrom(getInitiateRebalanceNode());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setInitiateRebalanceNode(subBuilder.buildPartial());
                                break;
                            }
                        case 82:
                            {
                                voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.newBuilder();
                                if (hasAsyncOperationStop()) {
                                    subBuilder.mergeFrom(getAsyncOperationStop());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setAsyncOperationStop(subBuilder.buildPartial());
                                break;
                            }
                        case 90:
                            {
                                voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.Builder subBuilder = voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.newBuilder();
                                if (hasAsyncOperationList()) {
                                    subBuilder.mergeFrom(getAsyncOperationList());
                                }
                                input.readMessage(subBuilder, extensionRegistry);
                                setAsyncOperationList(subBuilder.buildPartial());
                                break;
                            }
                    }
                }
            }

            public boolean hasType() {
                return result.hasType();
            }

            public voldemort.client.protocol.pb.VAdminProto.AdminRequestType getType() {
                return result.getType();
            }

            public Builder setType(voldemort.client.protocol.pb.VAdminProto.AdminRequestType value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasType = true;
                result.type_ = value;
                return this;
            }

            public Builder clearType() {
                result.hasType = false;
                result.type_ = voldemort.client.protocol.pb.VAdminProto.AdminRequestType.GET_METADATA;
                return this;
            }

            public boolean hasGetMetadata() {
                return result.hasGetMetadata();
            }

            public voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest getGetMetadata() {
                return result.getGetMetadata();
            }

            public Builder setGetMetadata(voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasGetMetadata = true;
                result.getMetadata_ = value;
                return this;
            }

            public Builder setGetMetadata(voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.Builder builderForValue) {
                result.hasGetMetadata = true;
                result.getMetadata_ = builderForValue.build();
                return this;
            }

            public Builder mergeGetMetadata(voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest value) {
                if (result.hasGetMetadata() && result.getMetadata_ != voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDefaultInstance()) {
                    result.getMetadata_ = voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.newBuilder(result.getMetadata_).mergeFrom(value).buildPartial();
                } else {
                    result.getMetadata_ = value;
                }
                result.hasGetMetadata = true;
                return this;
            }

            public Builder clearGetMetadata() {
                result.hasGetMetadata = false;
                result.getMetadata_ = voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.getDefaultInstance();
                return this;
            }

            public boolean hasUpdateMetadata() {
                return result.hasUpdateMetadata();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest getUpdateMetadata() {
                return result.getUpdateMetadata();
            }

            public Builder setUpdateMetadata(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasUpdateMetadata = true;
                result.updateMetadata_ = value;
                return this;
            }

            public Builder setUpdateMetadata(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.Builder builderForValue) {
                result.hasUpdateMetadata = true;
                result.updateMetadata_ = builderForValue.build();
                return this;
            }

            public Builder mergeUpdateMetadata(voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest value) {
                if (result.hasUpdateMetadata() && result.updateMetadata_ != voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDefaultInstance()) {
                    result.updateMetadata_ = voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.newBuilder(result.updateMetadata_).mergeFrom(value).buildPartial();
                } else {
                    result.updateMetadata_ = value;
                }
                result.hasUpdateMetadata = true;
                return this;
            }

            public Builder clearUpdateMetadata() {
                result.hasUpdateMetadata = false;
                result.updateMetadata_ = voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.getDefaultInstance();
                return this;
            }

            public boolean hasUpdatePartitionEntries() {
                return result.hasUpdatePartitionEntries();
            }

            public voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest getUpdatePartitionEntries() {
                return result.getUpdatePartitionEntries();
            }

            public Builder setUpdatePartitionEntries(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasUpdatePartitionEntries = true;
                result.updatePartitionEntries_ = value;
                return this;
            }

            public Builder setUpdatePartitionEntries(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.Builder builderForValue) {
                result.hasUpdatePartitionEntries = true;
                result.updatePartitionEntries_ = builderForValue.build();
                return this;
            }

            public Builder mergeUpdatePartitionEntries(voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest value) {
                if (result.hasUpdatePartitionEntries() && result.updatePartitionEntries_ != voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDefaultInstance()) {
                    result.updatePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.newBuilder(result.updatePartitionEntries_).mergeFrom(value).buildPartial();
                } else {
                    result.updatePartitionEntries_ = value;
                }
                result.hasUpdatePartitionEntries = true;
                return this;
            }

            public Builder clearUpdatePartitionEntries() {
                result.hasUpdatePartitionEntries = false;
                result.updatePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.getDefaultInstance();
                return this;
            }

            public boolean hasFetchPartitionEntries() {
                return result.hasFetchPartitionEntries();
            }

            public voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest getFetchPartitionEntries() {
                return result.getFetchPartitionEntries();
            }

            public Builder setFetchPartitionEntries(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasFetchPartitionEntries = true;
                result.fetchPartitionEntries_ = value;
                return this;
            }

            public Builder setFetchPartitionEntries(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.Builder builderForValue) {
                result.hasFetchPartitionEntries = true;
                result.fetchPartitionEntries_ = builderForValue.build();
                return this;
            }

            public Builder mergeFetchPartitionEntries(voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest value) {
                if (result.hasFetchPartitionEntries() && result.fetchPartitionEntries_ != voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDefaultInstance()) {
                    result.fetchPartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.newBuilder(result.fetchPartitionEntries_).mergeFrom(value).buildPartial();
                } else {
                    result.fetchPartitionEntries_ = value;
                }
                result.hasFetchPartitionEntries = true;
                return this;
            }

            public Builder clearFetchPartitionEntries() {
                result.hasFetchPartitionEntries = false;
                result.fetchPartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.getDefaultInstance();
                return this;
            }

            public boolean hasDeletePartitionEntries() {
                return result.hasDeletePartitionEntries();
            }

            public voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest getDeletePartitionEntries() {
                return result.getDeletePartitionEntries();
            }

            public Builder setDeletePartitionEntries(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasDeletePartitionEntries = true;
                result.deletePartitionEntries_ = value;
                return this;
            }

            public Builder setDeletePartitionEntries(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.Builder builderForValue) {
                result.hasDeletePartitionEntries = true;
                result.deletePartitionEntries_ = builderForValue.build();
                return this;
            }

            public Builder mergeDeletePartitionEntries(voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest value) {
                if (result.hasDeletePartitionEntries() && result.deletePartitionEntries_ != voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDefaultInstance()) {
                    result.deletePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.newBuilder(result.deletePartitionEntries_).mergeFrom(value).buildPartial();
                } else {
                    result.deletePartitionEntries_ = value;
                }
                result.hasDeletePartitionEntries = true;
                return this;
            }

            public Builder clearDeletePartitionEntries() {
                result.hasDeletePartitionEntries = false;
                result.deletePartitionEntries_ = voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.getDefaultInstance();
                return this;
            }

            public boolean hasInitiateFetchAndUpdate() {
                return result.hasInitiateFetchAndUpdate();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest getInitiateFetchAndUpdate() {
                return result.getInitiateFetchAndUpdate();
            }

            public Builder setInitiateFetchAndUpdate(voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasInitiateFetchAndUpdate = true;
                result.initiateFetchAndUpdate_ = value;
                return this;
            }

            public Builder setInitiateFetchAndUpdate(voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.Builder builderForValue) {
                result.hasInitiateFetchAndUpdate = true;
                result.initiateFetchAndUpdate_ = builderForValue.build();
                return this;
            }

            public Builder mergeInitiateFetchAndUpdate(voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest value) {
                if (result.hasInitiateFetchAndUpdate() && result.initiateFetchAndUpdate_ != voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDefaultInstance()) {
                    result.initiateFetchAndUpdate_ = voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.newBuilder(result.initiateFetchAndUpdate_).mergeFrom(value).buildPartial();
                } else {
                    result.initiateFetchAndUpdate_ = value;
                }
                result.hasInitiateFetchAndUpdate = true;
                return this;
            }

            public Builder clearInitiateFetchAndUpdate() {
                result.hasInitiateFetchAndUpdate = false;
                result.initiateFetchAndUpdate_ = voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.getDefaultInstance();
                return this;
            }

            public boolean hasAsyncOperationStatus() {
                return result.hasAsyncOperationStatus();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest getAsyncOperationStatus() {
                return result.getAsyncOperationStatus();
            }

            public Builder setAsyncOperationStatus(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasAsyncOperationStatus = true;
                result.asyncOperationStatus_ = value;
                return this;
            }

            public Builder setAsyncOperationStatus(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.Builder builderForValue) {
                result.hasAsyncOperationStatus = true;
                result.asyncOperationStatus_ = builderForValue.build();
                return this;
            }

            public Builder mergeAsyncOperationStatus(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest value) {
                if (result.hasAsyncOperationStatus() && result.asyncOperationStatus_ != voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDefaultInstance()) {
                    result.asyncOperationStatus_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.newBuilder(result.asyncOperationStatus_).mergeFrom(value).buildPartial();
                } else {
                    result.asyncOperationStatus_ = value;
                }
                result.hasAsyncOperationStatus = true;
                return this;
            }

            public Builder clearAsyncOperationStatus() {
                result.hasAsyncOperationStatus = false;
                result.asyncOperationStatus_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.getDefaultInstance();
                return this;
            }

            public boolean hasInitiateRebalanceNode() {
                return result.hasInitiateRebalanceNode();
            }

            public voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest getInitiateRebalanceNode() {
                return result.getInitiateRebalanceNode();
            }

            public Builder setInitiateRebalanceNode(voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasInitiateRebalanceNode = true;
                result.initiateRebalanceNode_ = value;
                return this;
            }

            public Builder setInitiateRebalanceNode(voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.Builder builderForValue) {
                result.hasInitiateRebalanceNode = true;
                result.initiateRebalanceNode_ = builderForValue.build();
                return this;
            }

            public Builder mergeInitiateRebalanceNode(voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest value) {
                if (result.hasInitiateRebalanceNode() && result.initiateRebalanceNode_ != voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDefaultInstance()) {
                    result.initiateRebalanceNode_ = voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.newBuilder(result.initiateRebalanceNode_).mergeFrom(value).buildPartial();
                } else {
                    result.initiateRebalanceNode_ = value;
                }
                result.hasInitiateRebalanceNode = true;
                return this;
            }

            public Builder clearInitiateRebalanceNode() {
                result.hasInitiateRebalanceNode = false;
                result.initiateRebalanceNode_ = voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.getDefaultInstance();
                return this;
            }

            public boolean hasAsyncOperationStop() {
                return result.hasAsyncOperationStop();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest getAsyncOperationStop() {
                return result.getAsyncOperationStop();
            }

            public Builder setAsyncOperationStop(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasAsyncOperationStop = true;
                result.asyncOperationStop_ = value;
                return this;
            }

            public Builder setAsyncOperationStop(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.Builder builderForValue) {
                result.hasAsyncOperationStop = true;
                result.asyncOperationStop_ = builderForValue.build();
                return this;
            }

            public Builder mergeAsyncOperationStop(voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest value) {
                if (result.hasAsyncOperationStop() && result.asyncOperationStop_ != voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDefaultInstance()) {
                    result.asyncOperationStop_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.newBuilder(result.asyncOperationStop_).mergeFrom(value).buildPartial();
                } else {
                    result.asyncOperationStop_ = value;
                }
                result.hasAsyncOperationStop = true;
                return this;
            }

            public Builder clearAsyncOperationStop() {
                result.hasAsyncOperationStop = false;
                result.asyncOperationStop_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.getDefaultInstance();
                return this;
            }

            public boolean hasAsyncOperationList() {
                return result.hasAsyncOperationList();
            }

            public voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest getAsyncOperationList() {
                return result.getAsyncOperationList();
            }

            public Builder setAsyncOperationList(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                result.hasAsyncOperationList = true;
                result.asyncOperationList_ = value;
                return this;
            }

            public Builder setAsyncOperationList(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.Builder builderForValue) {
                result.hasAsyncOperationList = true;
                result.asyncOperationList_ = builderForValue.build();
                return this;
            }

            public Builder mergeAsyncOperationList(voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest value) {
                if (result.hasAsyncOperationList() && result.asyncOperationList_ != voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDefaultInstance()) {
                    result.asyncOperationList_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.newBuilder(result.asyncOperationList_).mergeFrom(value).buildPartial();
                } else {
                    result.asyncOperationList_ = value;
                }
                result.hasAsyncOperationList = true;
                return this;
            }

            public Builder clearAsyncOperationList() {
                result.hasAsyncOperationList = false;
                result.asyncOperationList_ = voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.getDefaultInstance();
                return this;
            }
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.getDescriptor();
        }

        static {
            voldemort.client.protocol.pb.VAdminProto.internalForceInit();
        }
    }

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_GetMetadataRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_GetMetadataRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_GetMetadataResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_GetMetadataResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_UpdateMetadataRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_UpdateMetadataRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_UpdateMetadataResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_UpdateMetadataResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_PartitionEntry_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_PartitionEntry_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_UpdatePartitionEntriesRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_UpdatePartitionEntriesRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_UpdatePartitionEntriesResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_UpdatePartitionEntriesResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_VoldemortFilter_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_VoldemortFilter_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_FetchPartitionEntriesRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_FetchPartitionEntriesRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_FetchPartitionEntriesResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_FetchPartitionEntriesResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_DeletePartitionEntriesRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_DeletePartitionEntriesRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_DeletePartitionEntriesResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_DeletePartitionEntriesResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_InitiateFetchAndUpdateRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_InitiateFetchAndUpdateRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationStatusRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationStatusRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationStopRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationStopRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationStopResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationStopResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationListRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationListRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationListResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationListResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_InitiateRebalanceNodeRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_InitiateRebalanceNodeRequest_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_AsyncOperationStatusResponse_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_AsyncOperationStatusResponse_fieldAccessorTable;

    private static com.google.protobuf.Descriptors.Descriptor internal_static_voldemort_VoldemortAdminRequest_descriptor;

    private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_voldemort_VoldemortAdminRequest_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;

    static {
        java.lang.String[] descriptorData = { "\n\025voldemort-admin.proto\022\tvoldemort\032\026vold" + "emort-client.proto\"!\n\022GetMetadataRequest" + "\022\013\n\003key\030\001 \002(\014\"]\n\023GetMetadataResponse\022%\n\007" + "version\030\001 \001(\0132\024.voldemort.Versioned\022\037\n\005e" + "rror\030\002 \001(\0132\020.voldemort.Error\"M\n\025UpdateMe" + "tadataRequest\022\013\n\003key\030\001 \002(\014\022\'\n\tversioned\030" + "\002 \002(\0132\024.voldemort.Versioned\"9\n\026UpdateMet" + "adataResponse\022\037\n\005error\030\001 \001(\0132\020.voldemort" + ".Error\"F\n\016PartitionEntry\022\013\n\003key\030\001 \002(\014\022\'\n" + "\tversioned\030\002 \002(\0132\024.voldemort.Versioned\"\216", "\001\n\035UpdatePartitionEntriesRequest\022\r\n\005stor" + "e\030\001 \002(\t\0222\n\017partition_entry\030\002 \002(\0132\031.volde" + "mort.PartitionEntry\022*\n\006filter\030\003 \001(\0132\032.vo" + "ldemort.VoldemortFilter\"A\n\036UpdatePartiti" + "onEntriesResponse\022\037\n\005error\030\001 \001(\0132\020.volde" + "mort.Error\"-\n\017VoldemortFilter\022\014\n\004name\030\001 " + "\002(\t\022\014\n\004data\030\002 \002(\014\"\203\001\n\034FetchPartitionEntr" + "iesRequest\022\022\n\npartitions\030\001 \003(\005\022\r\n\005store\030" + "\002 \002(\t\022*\n\006filter\030\003 \001(\0132\032.voldemort.Voldem" + "ortFilter\022\024\n\014fetch_values\030\004 \001(\010\"\201\001\n\035Fetc", "hPartitionEntriesResponse\0222\n\017partition_e" + "ntry\030\001 \001(\0132\031.voldemort.PartitionEntry\022\013\n" + "\003key\030\002 \001(\014\022\037\n\005error\030\003 \001(\0132\020.voldemort.Er" + "ror\"n\n\035DeletePartitionEntriesRequest\022\r\n\005" + "store\030\001 \002(\t\022\022\n\npartitions\030\002 \003(\005\022*\n\006filte" + "r\030\003 \001(\0132\032.voldemort.VoldemortFilter\"P\n\036D" + "eletePartitionEntriesResponse\022\r\n\005count\030\001" + " \002(\005\022\037\n\005error\030\002 \001(\0132\020.voldemort.Error\"\177\n" + "\035InitiateFetchAndUpdateRequest\022\017\n\007node_i" + "d\030\001 \002(\005\022\022\n\npartitions\030\002 \003(\005\022\r\n\005store\030\003 \002", "(\t\022*\n\006filter\030\004 \001(\0132\032.voldemort.Voldemort" + "Filter\"1\n\033AsyncOperationStatusRequest\022\022\n" + "\nrequest_id\030\001 \002(\005\"/\n\031AsyncOperationStopR" + "equest\022\022\n\nrequest_id\030\001 \002(\005\"=\n\032AsyncOpera" + "tionStopResponse\022\037\n\005error\030\001 \001(\0132\020.voldem" + "ort.Error\"M\n\031AsyncOperationListRequest\022\022" + "\n\nrequest_id\030\001 \002(\005\022\034\n\rshow_complete\030\002 \002(" + "\010:\005false\"R\n\032AsyncOperationListResponse\022\023" + "\n\013request_ids\030\001 \003(\005\022\037\n\005error\030\002 \001(\0132\020.vol" + "demort.Error\"\232\001\n\034InitiateRebalanceNodeRe", "quest\022\025\n\rcurrent_store\030\001 \002(\t\022\022\n\nstealer_" + "id\030\002 \002(\005\022\020\n\010donor_id\030\003 \002(\005\022\022\n\npartitions" + "\030\004 \003(\005\022\017\n\007attempt\030\005 \002(\005\022\030\n\020unbalanced_st" + "ore\030\006 \003(\t\"\212\001\n\034AsyncOperationStatusRespon" + "se\022\022\n\nrequest_id\030\001 \002(\005\022\023\n\013description\030\002 " + "\002(\t\022\016\n\006status\030\003 \002(\t\022\020\n\010complete\030\004 \002(\010\022\037\n" + "\005error\030\005 \001(\0132\020.voldemort.Error\"\373\005\n\025Volde" + "mortAdminRequest\022)\n\004type\030\001 \002(\0162\033.voldemo" + "rt.AdminRequestType\0223\n\014get_metadata\030\002 \001(" + "\0132\035.voldemort.GetMetadataRequest\0229\n\017upda", "te_metadata\030\003 \001(\0132 .voldemort.UpdateMeta" + "dataRequest\022J\n\030update_partition_entries\030" + "\004 \001(\0132(.voldemort.UpdatePartitionEntries" + "Request\022H\n\027fetch_partition_entries\030\005 \001(\013" + "2\'.voldemort.FetchPartitionEntriesReques" + "t\022J\n\030delete_partition_entries\030\006 \001(\0132(.vo" + "ldemort.DeletePartitionEntriesRequest\022K\n" + "\031initiate_fetch_and_update\030\007 \001(\0132(.volde" + "mort.InitiateFetchAndUpdateRequest\022F\n\026as" + "ync_operation_status\030\010 \001(\0132&.voldemort.A", "syncOperationStatusRequest\022H\n\027initiate_r" + "ebalance_node\030\t \001(\0132\'.voldemort.Initiate" + "RebalanceNodeRequest\022B\n\024async_operation_" + "stop\030\n \001(\0132$.voldemort.AsyncOperationSto" + "pRequest\022B\n\024async_operation_list\030\013 \001(\0132$" + ".voldemort.AsyncOperationListRequest*\236\002\n" + "\020AdminRequestType\022\020\n\014GET_METADATA\020\000\022\023\n\017U" + "PDATE_METADATA\020\001\022\034\n\030UPDATE_PARTITION_ENT" + "RIES\020\002\022\033\n\027FETCH_PARTITION_ENTRIES\020\003\022\034\n\030D" + "ELETE_PARTITION_ENTRIES\020\004\022\035\n\031INITIATE_FE", "TCH_AND_UPDATE\020\005\022\032\n\026ASYNC_OPERATION_STAT" + "US\020\006\022\033\n\027INITIATE_REBALANCE_NODE\020\007\022\030\n\024ASY" + "NC_OPERATION_STOP\020\010\022\030\n\024ASYNC_OPERATION_L" + "IST\020\tB-\n\034voldemort.client.protocol.pbB\013V" + "AdminProtoH\001" };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {

            public com.google.protobuf.ExtensionRegistry assignDescriptors(com.google.protobuf.Descriptors.FileDescriptor root) {
                descriptor = root;
                internal_static_voldemort_GetMetadataRequest_descriptor = getDescriptor().getMessageTypes().get(0);
                internal_static_voldemort_GetMetadataRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_GetMetadataRequest_descriptor, new java.lang.String[] { "Key" }, voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.class, voldemort.client.protocol.pb.VAdminProto.GetMetadataRequest.Builder.class);
                internal_static_voldemort_GetMetadataResponse_descriptor = getDescriptor().getMessageTypes().get(1);
                internal_static_voldemort_GetMetadataResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_GetMetadataResponse_descriptor, new java.lang.String[] { "Version", "Error" }, voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse.class, voldemort.client.protocol.pb.VAdminProto.GetMetadataResponse.Builder.class);
                internal_static_voldemort_UpdateMetadataRequest_descriptor = getDescriptor().getMessageTypes().get(2);
                internal_static_voldemort_UpdateMetadataRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_UpdateMetadataRequest_descriptor, new java.lang.String[] { "Key", "Versioned" }, voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.class, voldemort.client.protocol.pb.VAdminProto.UpdateMetadataRequest.Builder.class);
                internal_static_voldemort_UpdateMetadataResponse_descriptor = getDescriptor().getMessageTypes().get(3);
                internal_static_voldemort_UpdateMetadataResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_UpdateMetadataResponse_descriptor, new java.lang.String[] { "Error" }, voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse.class, voldemort.client.protocol.pb.VAdminProto.UpdateMetadataResponse.Builder.class);
                internal_static_voldemort_PartitionEntry_descriptor = getDescriptor().getMessageTypes().get(4);
                internal_static_voldemort_PartitionEntry_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_PartitionEntry_descriptor, new java.lang.String[] { "Key", "Versioned" }, voldemort.client.protocol.pb.VAdminProto.PartitionEntry.class, voldemort.client.protocol.pb.VAdminProto.PartitionEntry.Builder.class);
                internal_static_voldemort_UpdatePartitionEntriesRequest_descriptor = getDescriptor().getMessageTypes().get(5);
                internal_static_voldemort_UpdatePartitionEntriesRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_UpdatePartitionEntriesRequest_descriptor, new java.lang.String[] { "Store", "PartitionEntry", "Filter" }, voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.class, voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesRequest.Builder.class);
                internal_static_voldemort_UpdatePartitionEntriesResponse_descriptor = getDescriptor().getMessageTypes().get(6);
                internal_static_voldemort_UpdatePartitionEntriesResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_UpdatePartitionEntriesResponse_descriptor, new java.lang.String[] { "Error" }, voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse.class, voldemort.client.protocol.pb.VAdminProto.UpdatePartitionEntriesResponse.Builder.class);
                internal_static_voldemort_VoldemortFilter_descriptor = getDescriptor().getMessageTypes().get(7);
                internal_static_voldemort_VoldemortFilter_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_VoldemortFilter_descriptor, new java.lang.String[] { "Name", "Data" }, voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.class, voldemort.client.protocol.pb.VAdminProto.VoldemortFilter.Builder.class);
                internal_static_voldemort_FetchPartitionEntriesRequest_descriptor = getDescriptor().getMessageTypes().get(8);
                internal_static_voldemort_FetchPartitionEntriesRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_FetchPartitionEntriesRequest_descriptor, new java.lang.String[] { "Partitions", "Store", "Filter", "FetchValues" }, voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.class, voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesRequest.Builder.class);
                internal_static_voldemort_FetchPartitionEntriesResponse_descriptor = getDescriptor().getMessageTypes().get(9);
                internal_static_voldemort_FetchPartitionEntriesResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_FetchPartitionEntriesResponse_descriptor, new java.lang.String[] { "PartitionEntry", "Key", "Error" }, voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse.class, voldemort.client.protocol.pb.VAdminProto.FetchPartitionEntriesResponse.Builder.class);
                internal_static_voldemort_DeletePartitionEntriesRequest_descriptor = getDescriptor().getMessageTypes().get(10);
                internal_static_voldemort_DeletePartitionEntriesRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_DeletePartitionEntriesRequest_descriptor, new java.lang.String[] { "Store", "Partitions", "Filter" }, voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.class, voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesRequest.Builder.class);
                internal_static_voldemort_DeletePartitionEntriesResponse_descriptor = getDescriptor().getMessageTypes().get(11);
                internal_static_voldemort_DeletePartitionEntriesResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_DeletePartitionEntriesResponse_descriptor, new java.lang.String[] { "Count", "Error" }, voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse.class, voldemort.client.protocol.pb.VAdminProto.DeletePartitionEntriesResponse.Builder.class);
                internal_static_voldemort_InitiateFetchAndUpdateRequest_descriptor = getDescriptor().getMessageTypes().get(12);
                internal_static_voldemort_InitiateFetchAndUpdateRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_InitiateFetchAndUpdateRequest_descriptor, new java.lang.String[] { "NodeId", "Partitions", "Store", "Filter" }, voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.class, voldemort.client.protocol.pb.VAdminProto.InitiateFetchAndUpdateRequest.Builder.class);
                internal_static_voldemort_AsyncOperationStatusRequest_descriptor = getDescriptor().getMessageTypes().get(13);
                internal_static_voldemort_AsyncOperationStatusRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationStatusRequest_descriptor, new java.lang.String[] { "RequestId" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusRequest.Builder.class);
                internal_static_voldemort_AsyncOperationStopRequest_descriptor = getDescriptor().getMessageTypes().get(14);
                internal_static_voldemort_AsyncOperationStopRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationStopRequest_descriptor, new java.lang.String[] { "RequestId" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopRequest.Builder.class);
                internal_static_voldemort_AsyncOperationStopResponse_descriptor = getDescriptor().getMessageTypes().get(15);
                internal_static_voldemort_AsyncOperationStopResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationStopResponse_descriptor, new java.lang.String[] { "Error" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStopResponse.Builder.class);
                internal_static_voldemort_AsyncOperationListRequest_descriptor = getDescriptor().getMessageTypes().get(16);
                internal_static_voldemort_AsyncOperationListRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationListRequest_descriptor, new java.lang.String[] { "RequestId", "ShowComplete" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationListRequest.Builder.class);
                internal_static_voldemort_AsyncOperationListResponse_descriptor = getDescriptor().getMessageTypes().get(17);
                internal_static_voldemort_AsyncOperationListResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationListResponse_descriptor, new java.lang.String[] { "RequestIds", "Error" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationListResponse.Builder.class);
                internal_static_voldemort_InitiateRebalanceNodeRequest_descriptor = getDescriptor().getMessageTypes().get(18);
                internal_static_voldemort_InitiateRebalanceNodeRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_InitiateRebalanceNodeRequest_descriptor, new java.lang.String[] { "CurrentStore", "StealerId", "DonorId", "Partitions", "Attempt", "UnbalancedStore" }, voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.class, voldemort.client.protocol.pb.VAdminProto.InitiateRebalanceNodeRequest.Builder.class);
                internal_static_voldemort_AsyncOperationStatusResponse_descriptor = getDescriptor().getMessageTypes().get(19);
                internal_static_voldemort_AsyncOperationStatusResponse_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_AsyncOperationStatusResponse_descriptor, new java.lang.String[] { "RequestId", "Description", "Status", "Complete", "Error" }, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse.class, voldemort.client.protocol.pb.VAdminProto.AsyncOperationStatusResponse.Builder.class);
                internal_static_voldemort_VoldemortAdminRequest_descriptor = getDescriptor().getMessageTypes().get(20);
                internal_static_voldemort_VoldemortAdminRequest_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(internal_static_voldemort_VoldemortAdminRequest_descriptor, new java.lang.String[] { "Type", "GetMetadata", "UpdateMetadata", "UpdatePartitionEntries", "FetchPartitionEntries", "DeletePartitionEntries", "InitiateFetchAndUpdate", "AsyncOperationStatus", "InitiateRebalanceNode", "AsyncOperationStop", "AsyncOperationList" }, voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest.class, voldemort.client.protocol.pb.VAdminProto.VoldemortAdminRequest.Builder.class);
                return null;
            }
        };
        com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[] { voldemort.client.protocol.pb.VProto.getDescriptor() }, assigner);
    }

    public static void internalForceInit() {
    }
}
