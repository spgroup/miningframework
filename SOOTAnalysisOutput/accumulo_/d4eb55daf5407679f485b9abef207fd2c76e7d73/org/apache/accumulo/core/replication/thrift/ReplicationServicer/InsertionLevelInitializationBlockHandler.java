package org.apache.accumulo.core.replication.thrift;

@SuppressWarnings({ "cast", "rawtypes", "serial", "unchecked", "unused" })
public class ReplicationServicer {

    public interface Iface {

<<<<<<< MINE
public long replicateLog(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException {
            send_replicateLog(remoteTableId, data, credentials);
            return recv_replicateLog();
        }
=======
public long replicateLog(String remoteTableId, WalEdits data, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException;
>>>>>>> YOURS


<<<<<<< MINE
public long replicateKeyValues(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException {
            send_replicateKeyValues(remoteTableId, data, credentials);
            return recv_replicateKeyValues();
        }
=======
public long replicateKeyValues(String remoteTableId, KeyValues data, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException;
>>>>>>> YOURS

    }

    public interface AsyncIface {

<<<<<<< MINE
public void replicateLog(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            replicateLog_call method_call = new replicateLog_call(remoteTableId, data, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }
=======
public void replicateLog(String remoteTableId, WalEdits data, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;
>>>>>>> YOURS


<<<<<<< MINE
public void replicateKeyValues(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            replicateKeyValues_call method_call = new replicateKeyValues_call(remoteTableId, data, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }
=======
public void replicateKeyValues(String remoteTableId, KeyValues data, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;
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

        public long replicateLog(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException {
            send_replicateLog(remoteTableId, data, credentials);
            return recv_replicateLog();
        }

        public void send_replicateLog(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws org.apache.thrift.TException {
            replicateLog_args args = new replicateLog_args();
            args.setRemoteTableId(remoteTableId);
            args.setData(data);
            args.setCredentials(credentials);
            sendBase("replicateLog", args);
        }

        public long recv_replicateLog() throws RemoteReplicationException, org.apache.thrift.TException {
            replicateLog_result result = new replicateLog_result();
            receiveBase(result, "replicateLog");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.e != null) {
                throw result.e;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "replicateLog failed: unknown result");
        }

        public long replicateKeyValues(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws RemoteReplicationException, org.apache.thrift.TException {
            send_replicateKeyValues(remoteTableId, data, credentials);
            return recv_replicateKeyValues();
        }

        public void send_replicateKeyValues(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) throws org.apache.thrift.TException {
            replicateKeyValues_args args = new replicateKeyValues_args();
            args.setRemoteTableId(remoteTableId);
            args.setData(data);
            args.setCredentials(credentials);
            sendBase("replicateKeyValues", args);
        }

        public long recv_replicateKeyValues() throws RemoteReplicationException, org.apache.thrift.TException {
            replicateKeyValues_result result = new replicateKeyValues_result();
            receiveBase(result, "replicateKeyValues");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.e != null) {
                throw result.e;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "replicateKeyValues failed: unknown result");
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

        public void replicateLog(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            replicateLog_call method_call = new replicateLog_call(remoteTableId, data, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class replicateLog_call extends org.apache.thrift.async.TAsyncMethodCall<java.lang.Long> {

            private java.lang.String remoteTableId;

            private WalEdits data;

            private org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials;

            public replicateLog_call(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.remoteTableId = remoteTableId;
                this.data = data;
                this.credentials = credentials;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("replicateLog", org.apache.thrift.protocol.TMessageType.CALL, 0));
                replicateLog_args args = new replicateLog_args();
                args.setRemoteTableId(remoteTableId);
                args.setData(data);
                args.setCredentials(credentials);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public java.lang.Long getResult() throws RemoteReplicationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new java.lang.IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_replicateLog();
            }
        }

        public void replicateKeyValues(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
            checkReady();
            replicateKeyValues_call method_call = new replicateKeyValues_call(remoteTableId, data, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class replicateKeyValues_call extends org.apache.thrift.async.TAsyncMethodCall<java.lang.Long> {

            private java.lang.String remoteTableId;

            private KeyValues data;

            private org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials;

            public replicateKeyValues_call(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.remoteTableId = remoteTableId;
                this.data = data;
                this.credentials = credentials;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("replicateKeyValues", org.apache.thrift.protocol.TMessageType.CALL, 0));
                replicateKeyValues_args args = new replicateKeyValues_args();
                args.setRemoteTableId(remoteTableId);
                args.setData(data);
                args.setCredentials(credentials);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public java.lang.Long getResult() throws RemoteReplicationException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new java.lang.IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_replicateKeyValues();
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
            processMap.put("replicateLog", new replicateLog());
            processMap.put("replicateKeyValues", new replicateKeyValues());
            return processMap;
        }

        public static class replicateLog<I extends Iface> extends org.apache.thrift.ProcessFunction<I, replicateLog_args> {

            public replicateLog() {
                super("replicateLog");
            }

            public replicateLog_args getEmptyArgsInstance() {
                return new replicateLog_args();
            }

            protected boolean isOneway() {
                return false;
            }

            @Override
            protected boolean rethrowUnhandledExceptions() {
                return false;
            }

            public replicateLog_result getResult(I iface, replicateLog_args args) throws org.apache.thrift.TException {
                replicateLog_result result = new replicateLog_result();
                try {
                    result.success = iface.replicateLog(args.remoteTableId, args.data, args.credentials);
                    result.setSuccessIsSet(true);
                } catch (RemoteReplicationException e) {
                    result.e = e;
                }
                return result;
            }
        }

        public static class replicateKeyValues<I extends Iface> extends org.apache.thrift.ProcessFunction<I, replicateKeyValues_args> {

            public replicateKeyValues() {
                super("replicateKeyValues");
            }

            public replicateKeyValues_args getEmptyArgsInstance() {
                return new replicateKeyValues_args();
            }

            protected boolean isOneway() {
                return false;
            }

            @Override
            protected boolean rethrowUnhandledExceptions() {
                return false;
            }

            public replicateKeyValues_result getResult(I iface, replicateKeyValues_args args) throws org.apache.thrift.TException {
                replicateKeyValues_result result = new replicateKeyValues_result();
                try {
                    result.success = iface.replicateKeyValues(args.remoteTableId, args.data, args.credentials);
                    result.setSuccessIsSet(true);
                } catch (RemoteReplicationException e) {
                    result.e = e;
                }
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
            processMap.put("replicateLog", new replicateLog());
            processMap.put("replicateKeyValues", new replicateKeyValues());
            return processMap;
        }

        public static class replicateLog<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, replicateLog_args, java.lang.Long> {

            public replicateLog() {
                super("replicateLog");
            }

            public replicateLog_args getEmptyArgsInstance() {
                return new replicateLog_args();
            }

            public org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> getResultHandler(final org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new org.apache.thrift.async.AsyncMethodCallback<java.lang.Long>() {

                    public void onComplete(java.lang.Long o) {
                        replicateLog_result result = new replicateLog_result();
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
                        replicateLog_result result = new replicateLog_result();
                        if (e instanceof RemoteReplicationException) {
                            result.e = (RemoteReplicationException) e;
                            result.setEIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.thrift.transport.TTransportException) {
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
            }<<<<<<< MINE
=======
public AsyncMethodCallback<Long> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<Long>() {

                    public void onComplete(Long o) {
                        replicateLog_result result = new replicateLog_result();
                        result.success = o;
                        result.setSuccessIsSet(true);
                        try {
                            fcall.sendResponse(fb, result, org.apache.thrift.protocol.TMessageType.REPLY, seqid);
                            return;
                        } catch (Exception e) {
                            LOGGER.error("Exception writing to internal frame buffer", e);
                        }
                        fb.close();
                    }

                    public void onError(Exception e) {
                        byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
                        org.apache.thrift.TBase msg;
                        replicateLog_result result = new replicateLog_result();
                        if (e instanceof RemoteReplicationException) {
                            result.e = (RemoteReplicationException) e;
                            result.setEIsSet(true);
                            msg = result;
                        } else {
                            msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
                            msg = (org.apache.thrift.TBase) new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
                        }
                        try {
                            fcall.sendResponse(fb, msg, msgType, seqid);
                            return;
                        } catch (Exception ex) {
                            LOGGER.error("Exception writing to internal frame buffer", ex);
                        }
                        fb.close();
                    }
                };
            }
>>>>>>> YOURS


            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, replicateLog_args args, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
                iface.replicateLog(args.remoteTableId, args.data, args.credentials, resultHandler);
            }
        }

        public static class replicateKeyValues<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, replicateKeyValues_args, java.lang.Long> {

            public replicateKeyValues() {
                super("replicateKeyValues");
            }

            public replicateKeyValues_args getEmptyArgsInstance() {
                return new replicateKeyValues_args();
            }

            public org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> getResultHandler(final org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new org.apache.thrift.async.AsyncMethodCallback<java.lang.Long>() {

                    public void onComplete(java.lang.Long o) {
                        replicateKeyValues_result result = new replicateKeyValues_result();
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
                        replicateKeyValues_result result = new replicateKeyValues_result();
                        if (e instanceof RemoteReplicationException) {
                            result.e = (RemoteReplicationException) e;
                            result.setEIsSet(true);
                            msg = result;
                        } else if (e instanceof org.apache.thrift.transport.TTransportException) {
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

            public void start(I iface, replicateKeyValues_args args, org.apache.thrift.async.AsyncMethodCallback<java.lang.Long> resultHandler) throws org.apache.thrift.TException {
                iface.replicateKeyValues(args.remoteTableId, args.data, args.credentials, resultHandler);
            }
        }
    }

    public static class replicateLog_args implements org.apache.thrift.TBase<replicateLog_args, replicateLog_args._Fields>, java.io.Serializable, Cloneable, Comparable<replicateLog_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("replicateLog_args");

        private static final org.apache.thrift.protocol.TField REMOTE_TABLE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("remoteTableId", org.apache.thrift.protocol.TType.STRING, (short) 1);

        private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new replicateLog_argsStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new replicateLog_argsTupleSchemeFactory();

        @org.apache.thrift.annotation.Nullable
        public java.lang.String remoteTableId;

        @org.apache.thrift.annotation.Nullable
public WalEdits data;

        @org.apache.thrift.annotation.Nullable
        public org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            REMOTE_TABLE_ID((short) 1, "remoteTableId"), DATA((short) 2, "data"), CREDENTIALS((short) 3, "credentials");

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
                        return REMOTE_TABLE_ID;
                    case 2:
                        return DATA;
                    case 3:
                        return CREDENTIALS;
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
            tmpMap.put(_Fields.REMOTE_TABLE_ID, new org.apache.thrift.meta_data.FieldMetaData("remoteTableId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, WalEdits.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.securityImpl.thrift.TCredentials.class)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(replicateLog_args.class, metaDataMap);
        }

        public replicateLog_args() {
        }

        public replicateLog_args(java.lang.String remoteTableId, WalEdits data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) {
            this();
            this.remoteTableId = remoteTableId;
            this.data = data;
            this.credentials = credentials;
        }

        public replicateLog_args(replicateLog_args other) {
            if (other.isSetRemoteTableId()) {
                this.remoteTableId = other.remoteTableId;
            }
            if (other.isSetData()) {
                this.data = new WalEdits(other.data);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials(other.credentials);
            }
        }

        public replicateLog_args deepCopy() {
            return new replicateLog_args(this);
        }

        @Override
        public void clear() {
            this.remoteTableId = null;
            this.data = null;
            this.credentials = null;
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.String getRemoteTableId() {
            return this.remoteTableId;
        }

<<<<<<< MINE
public replicateLog_args setRemoteTableId(@org.apache.thrift.annotation.Nullable java.lang.String remoteTableId) {
=======
public replicateLog_args setRemoteTableId(String remoteTableId) {
>>>>>>> YOURS
            this.remoteTableId = remoteTableId;
            return this;
        }

        public void unsetRemoteTableId() {
            this.remoteTableId = null;
        }

        public boolean isSetRemoteTableId() {
            return this.remoteTableId != null;
        }

        public void setRemoteTableIdIsSet(boolean value) {
            if (!value) {
                this.remoteTableId = null;
            }
        }

        @org.apache.thrift.annotation.Nullable
public WalEdits getData() {
            return this.data;
        }

        public replicateLog_args setData(@org.apache.thrift.annotation.Nullable WalEdits data) {
            this.data = data;
            return this;
        }

        public void unsetData() {
            this.data = null;
        }

        public boolean isSetData() {
            return this.data != null;
        }

        public void setDataIsSet(boolean value) {
            if (!value) {
                this.data = null;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public org.apache.accumulo.core.securityImpl.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

<<<<<<< MINE
public replicateLog_args setCredentials(@org.apache.thrift.annotation.Nullable org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) {
=======
public replicateLog_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
>>>>>>> YOURS
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case REMOTE_TABLE_ID:
                    if (value == null) {
                        unsetRemoteTableId();
                    } else {
<<<<<<< MINE
                        setSuccess((java.lang.Long) value);
=======
                        setRemoteTableId((String) value);
>>>>>>> YOURS
                    }
                    break;
                case DATA:
                    if (value == null) {
                        unsetData();
                    } else {
                        setData((WalEdits) value);
                    }
                    break;
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.security.thrift.TCredentials) value);
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case REMOTE_TABLE_ID:
                    return getRemoteTableId();
                case DATA:
                    return getData();
                case CREDENTIALS:
                    return getCredentials();
            }
            throw new java.lang.IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new java.lang.IllegalArgumentException();
            }
            switch(field) {
                case REMOTE_TABLE_ID:
                    return isSetRemoteTableId();
                case DATA:
                    return isSetData();
                case CREDENTIALS:
                    return isSetCredentials();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof replicateLog_args)
                return this.equals((replicateLog_args) that);
            return false;
        }

        public boolean equals(replicateLog_args that) {
            if (that == null)
                return false;
            if (this == that)
                return true;
            boolean this_present_remoteTableId = true && this.isSetRemoteTableId();
            boolean that_present_remoteTableId = true && that.isSetRemoteTableId();
            if (this_present_remoteTableId || that_present_remoteTableId) {
                if (!(this_present_remoteTableId && that_present_remoteTableId))
                    return false;
                if (!this.remoteTableId.equals(that.remoteTableId))
                    return false;
            }
            boolean this_present_data = true && this.isSetData();
            boolean that_present_data = true && that.isSetData();
            if (this_present_data || that_present_data) {
                if (!(this_present_data && that_present_data))
                    return false;
                if (!this.data.equals(that.data))
                    return false;
            }
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + ((isSetRemoteTableId()) ? 131071 : 524287);
            if (isSetRemoteTableId())
                hashCode = hashCode * 8191 + remoteTableId.hashCode();
            hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
            if (isSetData())
                hashCode = hashCode * 8191 + data.hashCode();
            hashCode = hashCode * 8191 + ((isSetCredentials()) ? 131071 : 524287);
            if (isSetCredentials())
                hashCode = hashCode * 8191 + credentials.hashCode();
            return hashCode;
        }

        @Override
        public int compareTo(replicateLog_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = java.lang.Boolean.valueOf(isSetRemoteTableId()).compareTo(other.isSetRemoteTableId());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetRemoteTableId()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.remoteTableId, other.remoteTableId);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = java.lang.Boolean.valueOf(isSetData()).compareTo(other.isSetData());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetData()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = java.lang.Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
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
            java.lang.StringBuilder sb = new java.lang.StringBuilder("replicateLog_args(");
            boolean first = true;
            sb.append("remoteTableId:");
            if (this.remoteTableId == null) {
                sb.append("null");
            } else {
                sb.append(this.remoteTableId);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("data:");
            if (this.data == null) {
                sb.append("null");
            } else {
                sb.append(this.data);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (data != null) {
                data.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class replicateLog_argsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateLog_argsStandardScheme getScheme() {
                return new replicateLog_argsStandardScheme();
            }
        }

        private static class replicateLog_argsStandardScheme extends org.apache.thrift.scheme.StandardScheme<replicateLog_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, replicateLog_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.remoteTableId = iprot.readString();
                                struct.setRemoteTableIdIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.data = new WalEdits();
                                struct.data.read(iprot);
                                struct.setDataIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, replicateLog_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.remoteTableId != null) {
                    oprot.writeFieldBegin(REMOTE_TABLE_ID_FIELD_DESC);
                    oprot.writeString(struct.remoteTableId);
                    oprot.writeFieldEnd();
                }
                if (struct.data != null) {
                    oprot.writeFieldBegin(DATA_FIELD_DESC);
                    struct.data.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class replicateLog_argsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateLog_argsTupleScheme getScheme() {
                return new replicateLog_argsTupleScheme();
            }
        }

        private static class replicateLog_argsTupleScheme extends org.apache.thrift.scheme.TupleScheme<replicateLog_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, replicateLog_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetRemoteTableId()) {
                    optionals.set(0);
                }
                if (struct.isSetData()) {
                    optionals.set(1);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetRemoteTableId()) {
                    oprot.writeString(struct.remoteTableId);
                }
                if (struct.isSetData()) {
                    struct.data.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, replicateLog_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.remoteTableId = iprot.readString();
                    struct.setRemoteTableIdIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.data = new WalEdits();
                    struct.data.read(iprot);
                    struct.setDataIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
            }
        }

        private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
            return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
        }
    }

    public static class replicateLog_result implements org.apache.thrift.TBase<replicateLog_result, replicateLog_result._Fields>, java.io.Serializable, Cloneable, Comparable<replicateLog_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("replicateLog_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.I64, (short) 0);

        private static final org.apache.thrift.protocol.TField E_FIELD_DESC = new org.apache.thrift.protocol.TField("e", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new replicateLog_resultStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new replicateLog_resultTupleSchemeFactory();

        public long success;

        @org.apache.thrift.annotation.Nullable
public RemoteReplicationException e;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), E((short) 1, "e");

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
                    case 1:
                        return E;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            tmpMap.put(_Fields.E, new org.apache.thrift.meta_data.FieldMetaData("e", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, RemoteReplicationException.class)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(replicateLog_result.class, metaDataMap);
        }

        public replicateLog_result() {
        }

        public replicateLog_result(long success, RemoteReplicationException e) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.e = e;
        }

        public replicateLog_result(replicateLog_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetE()) {
                this.e = new RemoteReplicationException(other.e);
            }
        }

        public replicateLog_result deepCopy() {
            return new replicateLog_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = 0;
            this.e = null;
        }

        public long getSuccess() {
            return this.success;
        }

        public replicateLog_result setSuccess(long success) {
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

        @org.apache.thrift.annotation.Nullable
public RemoteReplicationException getE() {
            return this.e;
        }

        public replicateLog_result setE(@org.apache.thrift.annotation.Nullable RemoteReplicationException e) {
            this.e = e;
            return this;
        }

        public void unsetE() {
            this.e = null;
        }

        public boolean isSetE() {
            return this.e != null;
        }

        public void setEIsSet(boolean value) {
            if (!value) {
                this.e = null;
            }
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((java.lang.Long) value);
                    }
                    break;
                case E:
                    if (value == null) {
                        unsetE();
                    } else {
                        setE((RemoteReplicationException) value);
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case E:
                    return getE();
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
                case E:
                    return isSetE();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof replicateLog_result)
                return this.equals((replicateLog_result) that);
            return false;
        }

        public boolean equals(replicateLog_result that) {
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
            boolean this_present_e = true && this.isSetE();
            boolean that_present_e = true && that.isSetE();
            if (this_present_e || that_present_e) {
                if (!(this_present_e && that_present_e))
                    return false;
                if (!this.e.equals(that.e))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(success);
            hashCode = hashCode * 8191 + ((isSetE()) ? 131071 : 524287);
            if (isSetE())
                hashCode = hashCode * 8191 + e.hashCode();
            return hashCode;
        }

        @Override
        public int compareTo(replicateLog_result other) {
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
            lastComparison = java.lang.Boolean.valueOf(isSetE()).compareTo(other.isSetE());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetE()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.e, other.e);
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
            java.lang.StringBuilder sb = new java.lang.StringBuilder("replicateLog_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("e:");
            if (this.e == null) {
                sb.append("null");
            } else {
                sb.append(this.e);
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

        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
            try {
                __isset_bitfield = 0;
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class replicateLog_resultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateLog_resultStandardScheme getScheme() {
                return new replicateLog_resultStandardScheme();
            }
        }

        private static class replicateLog_resultStandardScheme extends org.apache.thrift.scheme.StandardScheme<replicateLog_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, replicateLog_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.success = iprot.readI64();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.e = new RemoteReplicationException();
                                struct.e.read(iprot);
                                struct.setEIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, replicateLog_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeI64(struct.success);
                    oprot.writeFieldEnd();
                }
                if (struct.e != null) {
                    oprot.writeFieldBegin(E_FIELD_DESC);
                    struct.e.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class replicateLog_resultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateLog_resultTupleScheme getScheme() {
                return new replicateLog_resultTupleScheme();
            }
        }

        private static class replicateLog_resultTupleScheme extends org.apache.thrift.scheme.TupleScheme<replicateLog_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, replicateLog_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetE()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    oprot.writeI64(struct.success);
                }
                if (struct.isSetE()) {
                    struct.e.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, replicateLog_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = iprot.readI64();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.e = new RemoteReplicationException();
                    struct.e.read(iprot);
                    struct.setEIsSet(true);
                }
            }
        }

        private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
            return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
        }
    }

    public static class replicateKeyValues_args implements org.apache.thrift.TBase<replicateKeyValues_args, replicateKeyValues_args._Fields>, java.io.Serializable, Cloneable, Comparable<replicateKeyValues_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("replicateKeyValues_args");

        private static final org.apache.thrift.protocol.TField REMOTE_TABLE_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("remoteTableId", org.apache.thrift.protocol.TType.STRING, (short) 1);

        private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 3);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new replicateKeyValues_argsStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new replicateKeyValues_argsTupleSchemeFactory();

        @org.apache.thrift.annotation.Nullable
        public java.lang.String remoteTableId;

        @org.apache.thrift.annotation.Nullable
public KeyValues data;

        @org.apache.thrift.annotation.Nullable
        public org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            REMOTE_TABLE_ID((short) 1, "remoteTableId"), DATA((short) 2, "data"), CREDENTIALS((short) 3, "credentials");

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
                    case 1:
                        return REMOTE_TABLE_ID;
                    case 2:
                        return DATA;
                    case 3:
                        return CREDENTIALS;
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
            tmpMap.put(_Fields.REMOTE_TABLE_ID, new org.apache.thrift.meta_data.FieldMetaData("remoteTableId", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
            tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, KeyValues.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.securityImpl.thrift.TCredentials.class)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(replicateKeyValues_args.class, metaDataMap);
        }

        public replicateKeyValues_args() {
        }

        public replicateKeyValues_args(java.lang.String remoteTableId, KeyValues data, org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) {
            this();
            this.remoteTableId = remoteTableId;
            this.data = data;
            this.credentials = credentials;
        }

        public replicateKeyValues_args(replicateKeyValues_args other) {
            if (other.isSetRemoteTableId()) {
                this.remoteTableId = other.remoteTableId;
            }
            if (other.isSetData()) {
                this.data = new KeyValues(other.data);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials(other.credentials);
            }
        }

        public replicateKeyValues_args deepCopy() {
            return new replicateKeyValues_args(this);
        }

        @Override
        public void clear() {
            this.remoteTableId = null;
            this.data = null;
            this.credentials = null;
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.String getRemoteTableId() {
            return this.remoteTableId;
        }

        public replicateKeyValues_args setRemoteTableId(@org.apache.thrift.annotation.Nullable java.lang.String remoteTableId) {
            this.remoteTableId = remoteTableId;
            return this;
        }

        public void unsetRemoteTableId() {
            this.remoteTableId = null;
        }

        public boolean isSetRemoteTableId() {
            return this.remoteTableId != null;
        }

        public void setRemoteTableIdIsSet(boolean value) {
            if (!value) {
                this.remoteTableId = null;
            }
        }

        @org.apache.thrift.annotation.Nullable
public KeyValues getData() {
            return this.data;
        }

        public replicateKeyValues_args setData(@org.apache.thrift.annotation.Nullable KeyValues data) {
            this.data = data;
            return this;
        }

        public void unsetData() {
            this.data = null;
        }

        public boolean isSetData() {
            return this.data != null;
        }

        public void setDataIsSet(boolean value) {
            if (!value) {
                this.data = null;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public org.apache.accumulo.core.securityImpl.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public replicateKeyValues_args setCredentials(@org.apache.thrift.annotation.Nullable org.apache.accumulo.core.securityImpl.thrift.TCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public void unsetCredentials() {
            this.credentials = null;
        }

        public boolean isSetCredentials() {
            return this.credentials != null;
        }

        public void setCredentialsIsSet(boolean value) {
            if (!value) {
                this.credentials = null;
            }
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case REMOTE_TABLE_ID:
                    if (value == null) {
                        unsetRemoteTableId();
                    } else {
                        setRemoteTableId((java.lang.String) value);
                    }
                    break;
                case DATA:
                    if (value == null) {
                        unsetData();
                    } else {
                        setData((KeyValues) value);
                    }
                    break;
                case CREDENTIALS:
                    if (value == null) {
                        unsetCredentials();
                    } else {
                        setCredentials((org.apache.accumulo.core.securityImpl.thrift.TCredentials) value);
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case REMOTE_TABLE_ID:
                    return getRemoteTableId();
                case DATA:
                    return getData();
                case CREDENTIALS:
                    return getCredentials();
            }
            throw new java.lang.IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new java.lang.IllegalArgumentException();
            }
            switch(field) {
                case REMOTE_TABLE_ID:
                    return isSetRemoteTableId();
                case DATA:
                    return isSetData();
                case CREDENTIALS:
                    return isSetCredentials();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof replicateKeyValues_args)
                return this.equals((replicateKeyValues_args) that);
            return false;
        }

        public boolean equals(replicateKeyValues_args that) {
            if (that == null)
                return false;
            if (this == that)
                return true;
            boolean this_present_remoteTableId = true && this.isSetRemoteTableId();
            boolean that_present_remoteTableId = true && that.isSetRemoteTableId();
            if (this_present_remoteTableId || that_present_remoteTableId) {
                if (!(this_present_remoteTableId && that_present_remoteTableId))
                    return false;
                if (!this.remoteTableId.equals(that.remoteTableId))
                    return false;
            }
            boolean this_present_data = true && this.isSetData();
            boolean that_present_data = true && that.isSetData();
            if (this_present_data || that_present_data) {
                if (!(this_present_data && that_present_data))
                    return false;
                if (!this.data.equals(that.data))
                    return false;
            }
            boolean this_present_credentials = true && this.isSetCredentials();
            boolean that_present_credentials = true && that.isSetCredentials();
            if (this_present_credentials || that_present_credentials) {
                if (!(this_present_credentials && that_present_credentials))
                    return false;
                if (!this.credentials.equals(that.credentials))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + ((isSetRemoteTableId()) ? 131071 : 524287);
            if (isSetRemoteTableId())
                hashCode = hashCode * 8191 + remoteTableId.hashCode();
            hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
            if (isSetData())
                hashCode = hashCode * 8191 + data.hashCode();
            hashCode = hashCode * 8191 + ((isSetCredentials()) ? 131071 : 524287);
            if (isSetCredentials())
                hashCode = hashCode * 8191 + credentials.hashCode();
            return hashCode;
        }

        @Override
        public int compareTo(replicateKeyValues_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = java.lang.Boolean.valueOf(isSetRemoteTableId()).compareTo(other.isSetRemoteTableId());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetRemoteTableId()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.remoteTableId, other.remoteTableId);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = java.lang.Boolean.valueOf(isSetData()).compareTo(other.isSetData());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetData()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = java.lang.Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetCredentials()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.credentials, other.credentials);
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
            java.lang.StringBuilder sb = new java.lang.StringBuilder("replicateKeyValues_args(");
            boolean first = true;
            sb.append("remoteTableId:");
            if (this.remoteTableId == null) {
                sb.append("null");
            } else {
                sb.append(this.remoteTableId);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("data:");
            if (this.data == null) {
                sb.append("null");
            } else {
                sb.append(this.data);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("credentials:");
            if (this.credentials == null) {
                sb.append("null");
            } else {
                sb.append(this.credentials);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (data != null) {
                data.validate();
            }
            if (credentials != null) {
                credentials.validate();
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

        private static class replicateKeyValues_argsStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateKeyValues_argsStandardScheme getScheme() {
                return new replicateKeyValues_argsStandardScheme();
            }
        }

        private static class replicateKeyValues_argsStandardScheme extends org.apache.thrift.scheme.StandardScheme<replicateKeyValues_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, replicateKeyValues_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                                struct.remoteTableId = iprot.readString();
                                struct.setRemoteTableIdIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.data = new KeyValues();
                                struct.data.read(iprot);
                                struct.setDataIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 3:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials();
                                struct.credentials.read(iprot);
                                struct.setCredentialsIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, replicateKeyValues_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.remoteTableId != null) {
                    oprot.writeFieldBegin(REMOTE_TABLE_ID_FIELD_DESC);
                    oprot.writeString(struct.remoteTableId);
                    oprot.writeFieldEnd();
                }
                if (struct.data != null) {
                    oprot.writeFieldBegin(DATA_FIELD_DESC);
                    struct.data.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class replicateKeyValues_argsTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateKeyValues_argsTupleScheme getScheme() {
                return new replicateKeyValues_argsTupleScheme();
            }
        }

        private static class replicateKeyValues_argsTupleScheme extends org.apache.thrift.scheme.TupleScheme<replicateKeyValues_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, replicateKeyValues_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetRemoteTableId()) {
                    optionals.set(0);
                }
                if (struct.isSetData()) {
                    optionals.set(1);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(2);
                }
                oprot.writeBitSet(optionals, 3);
                if (struct.isSetRemoteTableId()) {
                    oprot.writeString(struct.remoteTableId);
                }
                if (struct.isSetData()) {
                    struct.data.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, replicateKeyValues_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(3);
                if (incoming.get(0)) {
                    struct.remoteTableId = iprot.readString();
                    struct.setRemoteTableIdIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.data = new KeyValues();
                    struct.data.read(iprot);
                    struct.setDataIsSet(true);
                }
                if (incoming.get(2)) {
                    struct.credentials = new org.apache.accumulo.core.securityImpl.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
            }
        }

        private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
            return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
        }
    }

    public static class replicateKeyValues_result implements org.apache.thrift.TBase<replicateKeyValues_result, replicateKeyValues_result._Fields>, java.io.Serializable, Cloneable, Comparable<replicateKeyValues_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("replicateKeyValues_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.I64, (short) 0);

        private static final org.apache.thrift.protocol.TField E_FIELD_DESC = new org.apache.thrift.protocol.TField("e", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new replicateKeyValues_resultStandardSchemeFactory();

        private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new replicateKeyValues_resultTupleSchemeFactory();

        public long success;

        @org.apache.thrift.annotation.Nullable
public RemoteReplicationException e;

        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), E((short) 1, "e");

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
                    case 1:
                        return E;
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
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
            tmpMap.put(_Fields.E, new org.apache.thrift.meta_data.FieldMetaData("e", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, RemoteReplicationException.class)));
            metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(replicateKeyValues_result.class, metaDataMap);
        }

        public replicateKeyValues_result() {
        }

        public replicateKeyValues_result(long success, RemoteReplicationException e) {
            this();
            this.success = success;
            setSuccessIsSet(true);
            this.e = e;
        }

        public replicateKeyValues_result(replicateKeyValues_result other) {
            __isset_bitfield = other.__isset_bitfield;
            this.success = other.success;
            if (other.isSetE()) {
                this.e = new RemoteReplicationException(other.e);
            }
        }

        public replicateKeyValues_result deepCopy() {
            return new replicateKeyValues_result(this);
        }

        @Override
        public void clear() {
            setSuccessIsSet(false);
            this.success = 0;
            this.e = null;
        }

        public long getSuccess() {
            return this.success;
        }

        public replicateKeyValues_result setSuccess(long success) {
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

        @org.apache.thrift.annotation.Nullable
public RemoteReplicationException getE() {
            return this.e;
        }

        public replicateKeyValues_result setE(@org.apache.thrift.annotation.Nullable RemoteReplicationException e) {
            this.e = e;
            return this;
        }

        public void unsetE() {
            this.e = null;
        }

        public boolean isSetE() {
            return this.e != null;
        }

        public void setEIsSet(boolean value) {
            if (!value) {
                this.e = null;
            }
        }

        public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((java.lang.Long) value);
                    }
                    break;
                case E:
                    if (value == null) {
                        unsetE();
                    } else {
                        setE((RemoteReplicationException) value);
                    }
                    break;
            }
        }

        @org.apache.thrift.annotation.Nullable
        public java.lang.Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case E:
                    return getE();
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
                case E:
                    return isSetE();
            }
            throw new java.lang.IllegalStateException();
        }

        @Override
        public boolean equals(java.lang.Object that) {
            if (that == null)
                return false;
            if (that instanceof replicateKeyValues_result)
                return this.equals((replicateKeyValues_result) that);
            return false;
        }

        public boolean equals(replicateKeyValues_result that) {
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
            boolean this_present_e = true && this.isSetE();
            boolean that_present_e = true && that.isSetE();
            if (this_present_e || that_present_e) {
                if (!(this_present_e && that_present_e))
                    return false;
                if (!this.e.equals(that.e))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(success);
            hashCode = hashCode * 8191 + ((isSetE()) ? 131071 : 524287);
            if (isSetE())
                hashCode = hashCode * 8191 + e.hashCode();
            return hashCode;
        }

        @Override
        public int compareTo(replicateKeyValues_result other) {
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
            lastComparison = java.lang.Boolean.valueOf(isSetE()).compareTo(other.isSetE());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetE()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.e, other.e);
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
            java.lang.StringBuilder sb = new java.lang.StringBuilder("replicateKeyValues_result(");
            boolean first = true;
            sb.append("success:");
            sb.append(this.success);
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("e:");
            if (this.e == null) {
                sb.append("null");
            } else {
                sb.append(this.e);
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

        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
            try {
                __isset_bitfield = 0;
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class replicateKeyValues_resultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateKeyValues_resultStandardScheme getScheme() {
                return new replicateKeyValues_resultStandardScheme();
            }
        }

        private static class replicateKeyValues_resultStandardScheme extends org.apache.thrift.scheme.StandardScheme<replicateKeyValues_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, replicateKeyValues_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                                struct.success = iprot.readI64();
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.e = new RemoteReplicationException();
                                struct.e.read(iprot);
                                struct.setEIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, replicateKeyValues_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.isSetSuccess()) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    oprot.writeI64(struct.success);
                    oprot.writeFieldEnd();
                }
                if (struct.e != null) {
                    oprot.writeFieldBegin(E_FIELD_DESC);
                    struct.e.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class replicateKeyValues_resultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {

            public replicateKeyValues_resultTupleScheme getScheme() {
                return new replicateKeyValues_resultTupleScheme();
            }
        }

        private static class replicateKeyValues_resultTupleScheme extends org.apache.thrift.scheme.TupleScheme<replicateKeyValues_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, replicateKeyValues_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet optionals = new java.util.BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetE()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    oprot.writeI64(struct.success);
                }
                if (struct.isSetE()) {
                    struct.e.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, replicateKeyValues_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
                java.util.BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = iprot.readI64();
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.e = new RemoteReplicationException();
                    struct.e.read(iprot);
                    struct.setEIsSet(true);
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