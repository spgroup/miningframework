package org.apache.accumulo.tracer.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class TestService {

    public interface Iface {

<<<<<<< MINE
public boolean checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message) throws org.apache.thrift.TException {
            send_checkTrace(tinfo, message);
            return recv_checkTrace();
        }
=======
public boolean checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, String message) throws org.apache.thrift.TException;
>>>>>>> YOURS

    }

    public interface AsyncIface {

<<<<<<< MINE
public void checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message, org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            checkTrace_call method_call = new checkTrace_call(tinfo, message, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }
=======
public void checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, String message, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;
>>>>>>> YOURS

    }

    public static class Client extends org.apache.thrift.TServiceClient implements Iface {

        public static class Factory implements org.apache.thrift.TServiceClientFactory<Client> {

            public Factory() {
            }

            public Client getClient(org.apache.thrift.protocol.TProtocol prot) {
                return new Client(prot);
            }

            public Client getClient(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
                return new Client(iprot, oprot);
            }
        }

        public Client(org.apache.thrift.protocol.TProtocol prot) {
            super(prot, prot);
        }

        public Client(org.apache.thrift.protocol.TProtocol iprot, org.apache.thrift.protocol.TProtocol oprot) {
            super(iprot, oprot);
        }

        public boolean checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message) throws org.apache.thrift.TException {
            send_checkTrace(tinfo, message);
            return recv_checkTrace();
        }

        public void send_checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message) throws org.apache.thrift.TException {
            checkTrace_args args = new checkTrace_args();
            args.setTinfo(tinfo);
            args.setMessage(message);
            sendBase("checkTrace", args);
        }

        public boolean recv_checkTrace() throws org.apache.thrift.TException {
            checkTrace_result result = new checkTrace_result();
            receiveBase(result, "checkTrace");
            if (result.isSetSuccess()) {
                return result.success;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "checkTrace failed: unknown result");
        }
    }

    public static class AsyncClient extends org.apache.thrift.async.TAsyncClient implements AsyncIface {

        public static class Factory implements org.apache.thrift.async.TAsyncClientFactory<AsyncClient> {

            private org.apache.thrift.async.TAsyncClientManager clientManager;

            private org.apache.thrift.protocol.TProtocolFactory protocolFactory;

            public Factory(org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.protocol.TProtocolFactory protocolFactory) {
                this.clientManager = clientManager;
                this.protocolFactory = protocolFactory;
            }

            public AsyncClient getAsyncClient(org.apache.thrift.transport.TNonblockingTransport transport) {
                return new AsyncClient(protocolFactory, clientManager, transport);
            }
        }

        public AsyncClient(org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.async.TAsyncClientManager clientManager, org.apache.thrift.transport.TNonblockingTransport transport) {
            super(protocolFactory, clientManager, transport);
        }

        public void checkTrace(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message, org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            checkTrace_call method_call = new checkTrace_call(tinfo, message, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class checkTrace_call extends org.apache.thrift.async.TAsyncMethodCall<java.lang.Boolean> {

            private org.apache.accumulo.core.trace.thrift.TInfo tinfo;

            private java.lang.String message;

            public checkTrace_call(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message, org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.message = message;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("checkTrace", org.apache.thrift.protocol.TMessageType.CALL, 0));
                checkTrace_args args = new checkTrace_args();
                args.setTinfo(tinfo);
                args.setMessage(message);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public java.lang.Boolean getResult() throws org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new java.lang.IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_checkTrace();
            }
        }
    }

    public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {

        private static final org.slf4j.Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(Processor.class.getName());

        public Processor(I iface) {
            super(iface, getProcessMap(new java.util.HashMap<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
        }

        protected Processor(I iface, java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends Iface> java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(java.util.Map<java.lang.String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            processMap.put("checkTrace", new checkTrace());
            return processMap;
        }

        public static class checkTrace<I extends Iface> extends org.apache.thrift.ProcessFunction<I, checkTrace_args> {

            public checkTrace() {
                super("checkTrace");
            }

            public checkTrace_args getEmptyArgsInstance() {
                return new checkTrace_args();
            }

            protected boolean isOneway() {
                return false;
            }

            @Override
            protected boolean rethrowUnhandledExceptions() {
                return false;
            }

            public checkTrace_result getResult(I iface, checkTrace_args args) throws org.apache.thrift.TException {
                checkTrace_result result = new checkTrace_result();
                result.success = iface.checkTrace(args.tinfo, args.message);
                result.setSuccessIsSet(true);
                return result;
            }
        }
    }

    public static class AsyncProcessor<I extends AsyncIface> extends org.apache.thrift.TBaseAsyncProcessor<I> {

        private static final org.slf4j.Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(AsyncProcessor.class.getName());

        public AsyncProcessor(I iface) {
            super(iface, getProcessMap(new java.util.HashMap<java.lang.String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>>()));
        }

        protected AsyncProcessor(I iface, java.util.Map<java.lang.String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends AsyncIface> java.util.Map<java.lang.String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> getProcessMap(java.util.Map<java.lang.String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            processMap.put("checkTrace", new checkTrace());
            return processMap;
        }

        public static class checkTrace<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, checkTrace_args, java.lang.Boolean> {

            public checkTrace() {
                super("checkTrace");
            }

            public checkTrace_args getEmptyArgsInstance() {
                return new checkTrace_args();
            }

            public org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> getResultHandler(final org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean>() {

                    public void onComplete(java.lang.Boolean o) {
                        checkTrace_result result = new checkTrace_result();
                        result.success = o;
                        result.setSuccessIsSet(true);
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                        } catch (org.apache.thrift.transport.TTransportException e) {
                            _LOGGER.error("TTransportException writing to internal frame buffer", e);
                            fb.close();
                        } catch (java.lang.Exception e) {
                            _LOGGER.error("Exception writing to internal frame buffer", e);
                            onError(e);
                        }
                    }

                    public void onError(java.lang.Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TSerializable msg;
                        checkTrace_result result = new checkTrace_result();
                        if (e instanceof org.apache.thrift.transport.TTransportException) {
                            _LOGGER.error("TTransportException inside handler", e);
                            fb.close();
                            return;
                        } else if (e instanceof org.apache.thrift.TApplicationException) {
                            _LOGGER.error("TApplicationException inside handler", e);
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TApplicationException) e;
                        } else {
                            _LOGGER.error("Exception inside handler", e);
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                        } catch (java.lang.Exception ex) {
                            _LOGGER.error("Exception writing to internal frame buffer", ex);
                            fb.close();
                        }
                    }
                };
            }

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, checkTrace_args args, org.apache.thrift.async.AsyncMethodCallback<java.lang.Boolean> resultHandler) throws org.apache.thrift.TException {
                iface.checkTrace(args.tinfo, args.message, resultHandler);
            }
        }
    }

    public static class checkTrace_args implements org.apache.thrift.TBase<checkTrace_args, checkTrace_args._Fields>, java.io.Serializable, Cloneable, Comparable<checkTrace_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("checkTrace_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.protocol.TField MESSAGE_FIELD_DESC = new org.apache.thrift.protocol.TField("message", org.apache.thrift.protocol.TType.STRING, (short) 2);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new checkTrace_argsStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new checkTrace_argsTupleSchemeFactory();

        @org.apache.thrift.annotation.Nullable
public org.apache.accumulo.core.trace.thrift.TInfo tinfo;

        @org.apache.thrift.annotation.Nullable
        public java.lang.String message;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 1, "tinfo"), MESSAGE((short) 2, "message");

            private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

            static {
                for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            @org.apache.thrift.annotation.Nullable
public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 1:
                        return TINFO;
                    case 2:
                        return MESSAGE;
                    default:
                        return null;
                }
            }

            public static _Fields findByThriftIdOrThrow(int fieldId) {
                _Fields fields = findByThriftId(fieldId);
                if (fields == null)
                    throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
                return fields;
            }

            @org.apache.thrift.annotation.Nullable
            public static _Fields findByName(java.lang.String name) {
                return byName.get(name);
            }

            private final short _thriftId;

            private final java.lang.String _fieldName;

            _Fields(short thriftId, java.lang.String fieldName) {
                _thriftId = thriftId;
                _fieldName = fieldName;
            }

            public short getThriftFieldId() {
                return _thriftId;
            }

            public java.lang.String getFieldName() {
                return _fieldName;
            }
        }

        public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.MESSAGE, new org.apache.thrift.meta_data.FieldMetaData("message", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(checkTrace_args.class, metaDataMap);
        }

        public checkTrace_args() {
        }

        public checkTrace_args(org.apache.accumulo.core.trace.thrift.TInfo tinfo, java.lang.String message) {
            this();
            this.tinfo = tinfo;
            this.message = message;
        }

        public checkTrace_args(checkTrace_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.core.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetMessage()) {
                this.message = other.message;
            }
        }

        public checkTrace_args deepCopy() {
            return new checkTrace_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.message = null;
        }

        @org.apache.thrift.annotation.Nullable
public org.apache.accumulo.core.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public checkTrace_args setTinfo(@org.apache.thrift.annotation.Nullable org.apache.accumulo.core.trace.thrift.TInfo tinfo) {
            this.tinfo = tinfo;
            return this;
        }

        public void unsetTinfo() {
            this.tinfo = null;
        }

        public boolean isSetTinfo() {
            return this.tinfo != null;
        }

        public void setTinfoIsSet(boolean value) {
            if (!value) {
                this.tinfo = null;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.String getMessage() {
            return this.message;
        }

        public checkTrace_args setMessage(@org.apache.thrift.annotation.Nullable java.lang.String message) {
            this.message = message;
            return this;
        }

        public void unsetMessage() {
            this.message = null;
        }

        public boolean isSetMessage() {
            return this.message != null;
        }

        public void setMessageIsSet(boolean value) {
            if (!value) {
                this.message = null;
            }
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
<<<<<<< MINE
                        setSuccess((java.lang.Boolean) value);
=======
                        setTinfo((org.apache.accumulo.core.trace.thrift.TInfo) value);
                    }
                    break;
                case MESSAGE:
                    if (value == null) {
                        unsetMessage();
                    } else {
                        setMessage((String) value);
>>>>>>> YOURS
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case MESSAGE:
                    return getMessage();
            }
            throw new java.lang.IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new java.lang.IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case MESSAGE:
                    return isSetMessage();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof checkTrace_args)
                return this.equals((checkTrace_args) that);
            return false;
        }

        public boolean equals(checkTrace_args that) {
            if (that == null)
                return false;
            if (this == that)
                return true;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
                    return false;
            }
            boolean this_present_message = true && this.isSetMessage();
            boolean that_present_message = true && that.isSetMessage();
            if (this_present_message || that_present_message) {
                if (!(this_present_message && that_present_message))
                    return false;
                if (!this.message.equals(that.message))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + ((isSetTinfo()) ? 131071 : 524287);
            if (isSetTinfo())
                hashCode = hashCode * 8191 + tinfo.hashCode();
            hashCode = hashCode * 8191 + ((isSetMessage()) ? 131071 : 524287);
            if (isSetMessage())
                hashCode = hashCode * 8191 + message.hashCode();
            return hashCode;
        }

        @Override
        public int compareTo(checkTrace_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = java.lang.Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = java.lang.Boolean.valueOf(isSetMessage()).compareTo(other.isSetMessage());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetMessage()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.message, other.message);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            return 0;
        }

        @org.apache.thrift.annotation.Nullable
public _Fields fieldForId(int fieldId) {
            return _Fields.findByThriftId(fieldId);
        }

        public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
            scheme(iprot).read(iprot, this);
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
            scheme(oprot).write(oprot, this);
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("checkTrace_args(");
            boolean first = true;
            sb.append("tinfo:");
            if (this.tinfo == null) {
                sb.append("null");
            } else {
                sb.append(this.tinfo);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("message:");
            if (this.message == null) {
                sb.append("null");
            } else {
                sb.append(this.message);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (tinfo != null) {
                tinfo.validate();
            }
        }

        private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
            try {
                write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
            try {
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class checkTrace_argsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public checkTrace_argsStandardScheme getScheme() {
                return new checkTrace_argsStandardScheme();
            }
        }

        private static class checkTrace_argsStandardScheme extends org.apache.thrift.scheme.StandardScheme<checkTrace_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, checkTrace_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.core.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.message = iprot.readString();
                                struct.setMessageIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, checkTrace_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.message != null) {
                    oprot.writeFieldBegin(MESSAGE_FIELD_DESC);
                    oprot.writeString(struct.message);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class checkTrace_argsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public checkTrace_argsTupleScheme getScheme() {
                return new checkTrace_argsTupleScheme();
            }
        }

        private static class checkTrace_argsTupleScheme extends org.apache.thrift.scheme.TupleScheme<checkTrace_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, checkTrace_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetMessage()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetMessage()) {
                    oprot.writeString(struct.message);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, checkTrace_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.core.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.message = iprot.readString();
                    struct.setMessageIsSet(true);
                }
            }
        }

        private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
            return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
        }
    }

    public static class checkTrace_result implements org.apache.thrift.TBase<checkTrace_result, checkTrace_result._Fields>, java.io.Serializable, Cloneable, Comparable<checkTrace_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("checkTrace_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.BOOL, (short) 0);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new checkTrace_resultStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new checkTrace_resultTupleSchemeFactory();

        public boolean success;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success");

            private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

            static {
                for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            @org.apache.thrift.annotation.Nullable
public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    default:
                        return null;
                }
            }

            public static _Fields findByThriftIdOrThrow(int fieldId) {
                _Fields fields = findByThriftId(fieldId);
                if (fields == null)
                    throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
                return fields;
            }

            @org.apache.thrift.annotation.Nullable
            public static _Fields findByName(java.lang.String name) {
                return byName.get(name);
            }

            private final short _thriftId;

            private final java.lang.String _fieldName;

            _Fields(short thriftId, java.lang.String fieldName) {
                _thriftId = thriftId;
                _fieldName = fieldName;
            }

            public short getThriftFieldId() {
                return _thriftId;
            }

            public java.lang.String getFieldName() {
                return _fieldName;
            }
        }

        private static final int __SUCCESS_ISSET_ID = 0;

        private byte __isset_bitfield = 0;

        public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(checkTrace_result.class, metaDataMap);
        }

        public checkTrace_result() {
        }

        public checkTrace_result(boolean success) {
            this();
            this.success = success;
            setSuccessIsSet(true);
        }

        public checkTrace_result(checkTrace_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
        }

        public checkTrace_result deepCopy() {
            return new checkTrace_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = false;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public checkTrace_result setSuccess(boolean success) {
            this.success = success;
            setSuccessIsSet(true);
            return this;
        }

        public void unsetSuccess() {
            __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public boolean isSetSuccess() {
            return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SUCCESS_ISSET_ID);
        }

        public void setSuccessIsSet(boolean value) {
            __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SUCCESS_ISSET_ID, value);
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((java.lang.Boolean) value);
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return isSuccess();
            }
            throw new java.lang.IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new java.lang.IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof checkTrace_result)
                return this.equals((checkTrace_result) that);
            return false;
        }

        public boolean equals(checkTrace_result that) {
            if (that == null)
                return false;
            if (this == that)
                return true;
            boolean this_present_success = true;
            boolean that_present_success = true;
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (this.success != that.success)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + ((success) ? 131071 : 524287);
            return hashCode;
        }

        @Override
        public int compareTo(checkTrace_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = java.lang.Boolean.valueOf(isSetSuccess()).compareTo(other.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, other.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            return 0;
        }

        @org.apache.thrift.annotation.Nullable
public _Fields fieldForId(int fieldId) {
            return _Fields.findByThriftId(fieldId);
        }

        public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
            scheme(iprot).read(iprot, this);
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
            scheme(oprot).write(oprot, this);
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder sb = new java.lang.StringBuilder("checkTrace_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
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

        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
            try {
                __isset_bitfield = 0;
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class checkTrace_resultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public checkTrace_resultStandardScheme getScheme() {
                return new checkTrace_resultStandardScheme();
            }
        }

        private static class checkTrace_resultStandardScheme extends org.apache.thrift.scheme.StandardScheme<checkTrace_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, checkTrace_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
                                struct.success = iprot.readBool();
                                struct.setSuccessIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, checkTrace_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeBool(struct.success);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class checkTrace_resultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public checkTrace_resultTupleScheme getScheme() {
                return new checkTrace_resultTupleScheme();
            }
        }

        private static class checkTrace_resultTupleScheme extends org.apache.thrift.scheme.TupleScheme<checkTrace_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, checkTrace_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                oprot.writeBitSet(optionals, 1);
                if (struct.isSetSuccess()) {
                    oprot.writeBool(struct.success);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, checkTrace_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(1);
                if (incoming.get(0)) {
                    struct.success = iprot.readBool();
                    struct.setSuccessIsSet(true);
                }
            }
        }

        private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
            return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
        }
    }

    private static void unusedMethod() {
    }
}