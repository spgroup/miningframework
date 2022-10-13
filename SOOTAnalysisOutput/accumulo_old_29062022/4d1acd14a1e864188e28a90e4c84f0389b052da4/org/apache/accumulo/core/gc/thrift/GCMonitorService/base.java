package org.apache.accumulo.core.gc.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
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

@SuppressWarnings("all")
public class GCMonitorService {

    public interface Iface {

        public GCStatus getStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException;
    }

    public interface AsyncIface {

        public void getStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException;
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

        public GCStatus getStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            send_getStatus(tinfo, credentials);
            return recv_getStatus();
        }

        public void send_getStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) throws org.apache.thrift.TException {
            getStatus_args args = new getStatus_args();
            args.setTinfo(tinfo);
            args.setCredentials(credentials);
            sendBase("getStatus", args);
        }

        public GCStatus recv_getStatus() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
            getStatus_result result = new getStatus_result();
            receiveBase(result, "getStatus");
            if (result.isSetSuccess()) {
                return result.success;
            }
            if (result.sec != null) {
                throw result.sec;
            }
            throw new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.MISSING_RESULT, "getStatus failed: unknown result");
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

        public void getStatus(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler) throws org.apache.thrift.TException {
            checkReady();
            getStatus_call method_call = new getStatus_call(tinfo, credentials, resultHandler, this, ___protocolFactory, ___transport);
            this.___currentMethod = method_call;
            ___manager.call(method_call);
        }

        public static class getStatus_call extends org.apache.thrift.async.TAsyncMethodCall {

            private org.apache.accumulo.trace.thrift.TInfo tinfo;

            private org.apache.accumulo.core.security.thrift.TCredentials credentials;

            public getStatus_call(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials, org.apache.thrift.async.AsyncMethodCallback resultHandler, org.apache.thrift.async.TAsyncClient client, org.apache.thrift.protocol.TProtocolFactory protocolFactory, org.apache.thrift.transport.TNonblockingTransport transport) throws org.apache.thrift.TException {
                super(client, protocolFactory, transport, resultHandler, false);
                this.tinfo = tinfo;
                this.credentials = credentials;
            }

            public void write_args(org.apache.thrift.protocol.TProtocol prot) throws org.apache.thrift.TException {
                prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("getStatus", org.apache.thrift.protocol.TMessageType.CALL, 0));
                getStatus_args args = new getStatus_args();
                args.setTinfo(tinfo);
                args.setCredentials(credentials);
                args.write(prot);
                prot.writeMessageEnd();
            }

            public GCStatus getResult() throws org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException, org.apache.thrift.TException {
                if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
                    throw new IllegalStateException("Method call not finished!");
                }
                org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
                org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
                return (new Client(prot)).recv_getStatus();
            }
        }
    }

    public static class Processor<I extends Iface> extends org.apache.thrift.TBaseProcessor<I> implements org.apache.thrift.TProcessor {

        private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class.getName());

        public Processor(I iface) {
            super(iface, getProcessMap(new HashMap<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>>()));
        }

        protected Processor(I iface, Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends Iface> Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> getProcessMap(Map<String, org.apache.thrift.ProcessFunction<I, ? extends org.apache.thrift.TBase>> processMap) {
            processMap.put("getStatus", new getStatus());
            return processMap;
        }

        public static class getStatus<I extends Iface> extends org.apache.thrift.ProcessFunction<I, getStatus_args> {

            public getStatus() {
                super("getStatus");
            }

            public getStatus_args getEmptyArgsInstance() {
                return new getStatus_args();
            }

            protected boolean isOneway() {
                return false;
            }

            public getStatus_result getResult(I iface, getStatus_args args) throws org.apache.thrift.TException {
                getStatus_result result = new getStatus_result();
                try {
                    result.success = iface.getStatus(args.tinfo, args.credentials);
                } catch (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
                    result.sec = sec;
                }
                return result;
            }
        }
    }

    public static class AsyncProcessor<I extends AsyncIface> extends org.apache.thrift.TBaseAsyncProcessor<I> {

        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProcessor.class.getName());

        public AsyncProcessor(I iface) {
            super(iface, getProcessMap(new HashMap<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>>()));
        }

        protected AsyncProcessor(I iface, Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            super(iface, getProcessMap(processMap));
        }

        private static <I extends AsyncIface> Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> getProcessMap(Map<String, org.apache.thrift.AsyncProcessFunction<I, ? extends org.apache.thrift.TBase, ?>> processMap) {
            processMap.put("getStatus", new getStatus());
            return processMap;
        }

        public static class getStatus<I extends AsyncIface> extends org.apache.thrift.AsyncProcessFunction<I, getStatus_args, GCStatus> {

            public getStatus() {
                super("getStatus");
            }

            public getStatus_args getEmptyArgsInstance() {
                return new getStatus_args();
            }

            public AsyncMethodCallback<GCStatus> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
                final org.apache.thrift.AsyncProcessFunction fcall = this;
                return new AsyncMethodCallback<GCStatus>() {

                    public void onComplete(GCStatus o) {
                        getStatus_result result = new getStatus_result();
                        result.success = o;
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
                        getStatus_result result = new getStatus_result();
                        if (e instanceof org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) {
                            result.sec = (org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) e;
                            result.setSecIsSet(true);
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

            protected boolean isOneway() {
                return false;
            }

            public void start(I iface, getStatus_args args, org.apache.thrift.async.AsyncMethodCallback<GCStatus> resultHandler) throws TException {
                iface.getStatus(args.tinfo, args.credentials, resultHandler);
            }
        }
    }

    public static class getStatus_args implements org.apache.thrift.TBase<getStatus_args, getStatus_args._Fields>, java.io.Serializable, Cloneable, Comparable<getStatus_args> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getStatus_args");

        private static final org.apache.thrift.protocol.TField TINFO_FIELD_DESC = new org.apache.thrift.protocol.TField("tinfo", org.apache.thrift.protocol.TType.STRUCT, (short) 2);

        private static final org.apache.thrift.protocol.TField CREDENTIALS_FIELD_DESC = new org.apache.thrift.protocol.TField("credentials", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getStatus_argsStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getStatus_argsTupleSchemeFactory());
        }

        public org.apache.accumulo.trace.thrift.TInfo tinfo;

        public org.apache.accumulo.core.security.thrift.TCredentials credentials;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            TINFO((short) 2, "tinfo"), CREDENTIALS((short) 1, "credentials");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 2:
                        return TINFO;
                    case 1:
                        return CREDENTIALS;
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

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.TINFO, new org.apache.thrift.meta_data.FieldMetaData("tinfo", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.trace.thrift.TInfo.class)));
            tmpMap.put(_Fields.CREDENTIALS, new org.apache.thrift.meta_data.FieldMetaData("credentials", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, org.apache.accumulo.core.security.thrift.TCredentials.class)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getStatus_args.class, metaDataMap);
        }

        public getStatus_args() {
        }

        public getStatus_args(org.apache.accumulo.trace.thrift.TInfo tinfo, org.apache.accumulo.core.security.thrift.TCredentials credentials) {
            this();
            this.tinfo = tinfo;
            this.credentials = credentials;
        }

        public getStatus_args(getStatus_args other) {
            if (other.isSetTinfo()) {
                this.tinfo = new org.apache.accumulo.trace.thrift.TInfo(other.tinfo);
            }
            if (other.isSetCredentials()) {
                this.credentials = new org.apache.accumulo.core.security.thrift.TCredentials(other.credentials);
            }
        }

        public getStatus_args deepCopy() {
            return new getStatus_args(this);
        }

        @Override
        public void clear() {
            this.tinfo = null;
            this.credentials = null;
        }

        public org.apache.accumulo.trace.thrift.TInfo getTinfo() {
            return this.tinfo;
        }

        public getStatus_args setTinfo(org.apache.accumulo.trace.thrift.TInfo tinfo) {
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

        public org.apache.accumulo.core.security.thrift.TCredentials getCredentials() {
            return this.credentials;
        }

        public getStatus_args setCredentials(org.apache.accumulo.core.security.thrift.TCredentials credentials) {
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

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case TINFO:
                    if (value == null) {
                        unsetTinfo();
                    } else {
                        setTinfo((org.apache.accumulo.trace.thrift.TInfo) value);
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

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case TINFO:
                    return getTinfo();
                case CREDENTIALS:
                    return getCredentials();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case TINFO:
                    return isSetTinfo();
                case CREDENTIALS:
                    return isSetCredentials();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getStatus_args)
                return this.equals((getStatus_args) that);
            return false;
        }

        public boolean equals(getStatus_args that) {
            if (that == null)
                return false;
            boolean this_present_tinfo = true && this.isSetTinfo();
            boolean that_present_tinfo = true && that.isSetTinfo();
            if (this_present_tinfo || that_present_tinfo) {
                if (!(this_present_tinfo && that_present_tinfo))
                    return false;
                if (!this.tinfo.equals(that.tinfo))
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
            return 0;
        }

        @Override
        public int compareTo(getStatus_args other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetTinfo()).compareTo(other.isSetTinfo());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetTinfo()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.tinfo, other.tinfo);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetCredentials()).compareTo(other.isSetCredentials());
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
            StringBuilder sb = new StringBuilder("getStatus_args(");
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
            if (tinfo != null) {
                tinfo.validate();
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

        private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            try {
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class getStatus_argsStandardSchemeFactory implements SchemeFactory {

            public getStatus_argsStandardScheme getScheme() {
                return new getStatus_argsStandardScheme();
            }
        }

        private static class getStatus_argsStandardScheme extends StandardScheme<getStatus_args> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getStatus_args struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 2:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                                struct.tinfo.read(iprot);
                                struct.setTinfoIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getStatus_args struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.credentials != null) {
                    oprot.writeFieldBegin(CREDENTIALS_FIELD_DESC);
                    struct.credentials.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.tinfo != null) {
                    oprot.writeFieldBegin(TINFO_FIELD_DESC);
                    struct.tinfo.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getStatus_argsTupleSchemeFactory implements SchemeFactory {

            public getStatus_argsTupleScheme getScheme() {
                return new getStatus_argsTupleScheme();
            }
        }

        private static class getStatus_argsTupleScheme extends TupleScheme<getStatus_args> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getStatus_args struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetTinfo()) {
                    optionals.set(0);
                }
                if (struct.isSetCredentials()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetTinfo()) {
                    struct.tinfo.write(oprot);
                }
                if (struct.isSetCredentials()) {
                    struct.credentials.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getStatus_args struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.tinfo = new org.apache.accumulo.trace.thrift.TInfo();
                    struct.tinfo.read(iprot);
                    struct.setTinfoIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.credentials = new org.apache.accumulo.core.security.thrift.TCredentials();
                    struct.credentials.read(iprot);
                    struct.setCredentialsIsSet(true);
                }
            }
        }
    }

    public static class getStatus_result implements org.apache.thrift.TBase<getStatus_result, getStatus_result._Fields>, java.io.Serializable, Cloneable, Comparable<getStatus_result> {

        private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("getStatus_result");

        private static final org.apache.thrift.protocol.TField SUCCESS_FIELD_DESC = new org.apache.thrift.protocol.TField("success", org.apache.thrift.protocol.TType.STRUCT, (short) 0);

        private static final org.apache.thrift.protocol.TField SEC_FIELD_DESC = new org.apache.thrift.protocol.TField("sec", org.apache.thrift.protocol.TType.STRUCT, (short) 1);

        private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();

        static {
            schemes.put(StandardScheme.class, new getStatus_resultStandardSchemeFactory());
            schemes.put(TupleScheme.class, new getStatus_resultTupleSchemeFactory());
        }

        public GCStatus success;

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec;

        @SuppressWarnings("all")
        public enum _Fields implements org.apache.thrift.TFieldIdEnum {

            SUCCESS((short) 0, "success"), SEC((short) 1, "sec");

            private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

            static {
                for (_Fields field : EnumSet.allOf(_Fields.class)) {
                    byName.put(field.getFieldName(), field);
                }
            }

            public static _Fields findByThriftId(int fieldId) {
                switch(fieldId) {
                    case 0:
                        return SUCCESS;
                    case 1:
                        return SEC;
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

        public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

        static {
            Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
            tmpMap.put(_Fields.SUCCESS, new org.apache.thrift.meta_data.FieldMetaData("success", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, GCStatus.class)));
            tmpMap.put(_Fields.SEC, new org.apache.thrift.meta_data.FieldMetaData("sec", org.apache.thrift.TFieldRequirementType.DEFAULT, new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT)));
            metaDataMap = Collections.unmodifiableMap(tmpMap);
            org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(getStatus_result.class, metaDataMap);
        }

        public getStatus_result() {
        }

        public getStatus_result(GCStatus success, org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this();
            this.success = success;
            this.sec = sec;
        }

        public getStatus_result(getStatus_result other) {
            if (other.isSetSuccess()) {
                this.success = new GCStatus(other.success);
            }
            if (other.isSetSec()) {
                this.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException(other.sec);
            }
        }

        public getStatus_result deepCopy() {
            return new getStatus_result(this);
        }

        @Override
        public void clear() {
            this.success = null;
            this.sec = null;
        }

        public GCStatus getSuccess() {
            return this.success;
        }

        public getStatus_result setSuccess(GCStatus success) {
            this.success = success;
            return this;
        }

        public void unsetSuccess() {
            this.success = null;
        }

        public boolean isSetSuccess() {
            return this.success != null;
        }

        public void setSuccessIsSet(boolean value) {
            if (!value) {
                this.success = null;
            }
        }

        public org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException getSec() {
            return this.sec;
        }

        public getStatus_result setSec(org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException sec) {
            this.sec = sec;
            return this;
        }

        public void unsetSec() {
            this.sec = null;
        }

        public boolean isSetSec() {
            return this.sec != null;
        }

        public void setSecIsSet(boolean value) {
            if (!value) {
                this.sec = null;
            }
        }

        public void setFieldValue(_Fields field, Object value) {
            switch(field) {
                case SUCCESS:
                    if (value == null) {
                        unsetSuccess();
                    } else {
                        setSuccess((GCStatus) value);
                    }
                    break;
                case SEC:
                    if (value == null) {
                        unsetSec();
                    } else {
                        setSec((org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException) value);
                    }
                    break;
            }
        }

        public Object getFieldValue(_Fields field) {
            switch(field) {
                case SUCCESS:
                    return getSuccess();
                case SEC:
                    return getSec();
            }
            throw new IllegalStateException();
        }

        public boolean isSet(_Fields field) {
            if (field == null) {
                throw new IllegalArgumentException();
            }
            switch(field) {
                case SUCCESS:
                    return isSetSuccess();
                case SEC:
                    return isSetSec();
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that instanceof getStatus_result)
                return this.equals((getStatus_result) that);
            return false;
        }

        public boolean equals(getStatus_result that) {
            if (that == null)
                return false;
            boolean this_present_success = true && this.isSetSuccess();
            boolean that_present_success = true && that.isSetSuccess();
            if (this_present_success || that_present_success) {
                if (!(this_present_success && that_present_success))
                    return false;
                if (!this.success.equals(that.success))
                    return false;
            }
            boolean this_present_sec = true && this.isSetSec();
            boolean that_present_sec = true && that.isSetSec();
            if (this_present_sec || that_present_sec) {
                if (!(this_present_sec && that_present_sec))
                    return false;
                if (!this.sec.equals(that.sec))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public int compareTo(getStatus_result other) {
            if (!getClass().equals(other.getClass())) {
                return getClass().getName().compareTo(other.getClass().getName());
            }
            int lastComparison = 0;
            lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(other.isSetSuccess());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSuccess()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.success, other.success);
                if (lastComparison != 0) {
                    return lastComparison;
                }
            }
            lastComparison = Boolean.valueOf(isSetSec()).compareTo(other.isSetSec());
            if (lastComparison != 0) {
                return lastComparison;
            }
            if (isSetSec()) {
                lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sec, other.sec);
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
            StringBuilder sb = new StringBuilder("getStatus_result(");
            boolean first = true;
            sb.append("success:");
            if (this.success == null) {
                sb.append("null");
            } else {
                sb.append(this.success);
            }
            first = false;
            if (!first)
                sb.append(", ");
            sb.append("sec:");
            if (this.sec == null) {
                sb.append("null");
            } else {
                sb.append(this.sec);
            }
            first = false;
            sb.append(")");
            return sb.toString();
        }

        public void validate() throws org.apache.thrift.TException {
            if (success != null) {
                success.validate();
            }
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
                read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
            } catch (org.apache.thrift.TException te) {
                throw new java.io.IOException(te);
            }
        }

        private static class getStatus_resultStandardSchemeFactory implements SchemeFactory {

            public getStatus_resultStandardScheme getScheme() {
                return new getStatus_resultStandardScheme();
            }
        }

        private static class getStatus_resultStandardScheme extends StandardScheme<getStatus_result> {

            public void read(org.apache.thrift.protocol.TProtocol iprot, getStatus_result struct) throws org.apache.thrift.TException {
                org.apache.thrift.protocol.TField schemeField;
                iprot.readStructBegin();
                while (true) {
                    schemeField = iprot.readFieldBegin();
                    if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                        break;
                    }
                    switch(schemeField.id) {
                        case 0:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.success = new GCStatus();
                                struct.success.read(iprot);
                                struct.setSuccessIsSet(true);
                            } else {
                                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                            }
                            break;
                        case 1:
                            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                                struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                                struct.sec.read(iprot);
                                struct.setSecIsSet(true);
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

            public void write(org.apache.thrift.protocol.TProtocol oprot, getStatus_result struct) throws org.apache.thrift.TException {
                struct.validate();
                oprot.writeStructBegin(STRUCT_DESC);
                if (struct.success != null) {
                    oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
                    struct.success.write(oprot);
                    oprot.writeFieldEnd();
                }
                if (struct.sec != null) {
                    oprot.writeFieldBegin(SEC_FIELD_DESC);
                    struct.sec.write(oprot);
                    oprot.writeFieldEnd();
                }
                oprot.writeFieldStop();
                oprot.writeStructEnd();
            }
        }

        private static class getStatus_resultTupleSchemeFactory implements SchemeFactory {

            public getStatus_resultTupleScheme getScheme() {
                return new getStatus_resultTupleScheme();
            }
        }

        private static class getStatus_resultTupleScheme extends TupleScheme<getStatus_result> {

            @Override
            public void write(org.apache.thrift.protocol.TProtocol prot, getStatus_result struct) throws org.apache.thrift.TException {
                TTupleProtocol oprot = (TTupleProtocol) prot;
                BitSet optionals = new BitSet();
                if (struct.isSetSuccess()) {
                    optionals.set(0);
                }
                if (struct.isSetSec()) {
                    optionals.set(1);
                }
                oprot.writeBitSet(optionals, 2);
                if (struct.isSetSuccess()) {
                    struct.success.write(oprot);
                }
                if (struct.isSetSec()) {
                    struct.sec.write(oprot);
                }
            }

            @Override
            public void read(org.apache.thrift.protocol.TProtocol prot, getStatus_result struct) throws org.apache.thrift.TException {
                TTupleProtocol iprot = (TTupleProtocol) prot;
                BitSet incoming = iprot.readBitSet(2);
                if (incoming.get(0)) {
                    struct.success = new GCStatus();
                    struct.success.read(iprot);
                    struct.setSuccessIsSet(true);
                }
                if (incoming.get(1)) {
                    struct.sec = new org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException();
                    struct.sec.read(iprot);
                    struct.setSecIsSet(true);
                }
            }
        }
    }
}