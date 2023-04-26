package com.sun.xml.internal.ws.fault;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.api.SOAPVersion;
import com.sun.xml.internal.ws.api.message.Message;
import com.sun.xml.internal.ws.api.model.CheckedException;
import com.sun.xml.internal.ws.api.model.ExceptionType;
import com.sun.xml.internal.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.internal.ws.encoding.soap.SOAPConstants;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import com.sun.xml.internal.ws.message.jaxb.JAXBMessage;
import com.sun.xml.internal.ws.message.FaultMessage;
import com.sun.xml.internal.ws.model.CheckedExceptionImpl;
import com.sun.xml.internal.ws.model.JavaMethodImpl;
import com.sun.xml.internal.ws.spi.db.XMLBridge;
import com.sun.xml.internal.ws.util.DOMUtil;
import com.sun.xml.internal.ws.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SOAPFaultBuilder {

    abstract DetailType getDetail();

    abstract void setDetail(DetailType detailType);

    @XmlTransient
    @Nullable
    public QName getFirstDetailEntryName() {
        DetailType dt = getDetail();
        if (dt != null) {
            Node entry = dt.getDetail(0);
            if (entry != null) {
                return new QName(entry.getNamespaceURI(), entry.getLocalName());
            }
        }
        return null;
    }

    abstract String getFaultString();

    public Throwable createException(Map<QName, CheckedExceptionImpl> exceptions) throws JAXBException {
        DetailType dt = getDetail();
        Node detail = null;
        if (dt != null)
            detail = dt.getDetail(0);
        if (detail == null || exceptions == null) {
            return attachServerException(getProtocolException());
        }
        QName detailName = new QName(detail.getNamespaceURI(), detail.getLocalName());
        CheckedExceptionImpl ce = exceptions.get(detailName);
        if (ce == null) {
            return attachServerException(getProtocolException());
        }
        if (ce.getExceptionType().equals(ExceptionType.UserDefined)) {
            return attachServerException(createUserDefinedException(ce));
        }
        Class exceptionClass = ce.getExceptionClass();
        try {
            Constructor constructor = exceptionClass.getConstructor(String.class, (Class) ce.getDetailType().type);
            Exception exception = (Exception) constructor.newInstance(getFaultString(), getJAXBObject(detail, ce));
            return attachServerException(exception);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    @NotNull
    public static Message createSOAPFaultMessage(@NotNull SOAPVersion soapVersion, @NotNull ProtocolException ex, @Nullable QName faultcode) {
        Object detail = getFaultDetail(null, ex);
        if (soapVersion == SOAPVersion.SOAP_12)
            return createSOAP12Fault(soapVersion, ex, detail, null, faultcode);
        return createSOAP11Fault(soapVersion, ex, detail, null, faultcode);
    }

    public static Message createSOAPFaultMessage(SOAPVersion soapVersion, CheckedExceptionImpl ceModel, Throwable ex) {
        return createSOAPFaultMessage(soapVersion, ceModel, ex, null);
    }

    public static Message createSOAPFaultMessage(SOAPVersion soapVersion, CheckedExceptionImpl ceModel, Throwable ex, QName faultCode) {
        Object detail = getFaultDetail(ceModel, ex);
        if (soapVersion == SOAPVersion.SOAP_12)
            return createSOAP12Fault(soapVersion, ex, detail, ceModel, faultCode);
        return createSOAP11Fault(soapVersion, ex, detail, ceModel, faultCode);
    }

    public static Message createSOAPFaultMessage(SOAPVersion soapVersion, String faultString, QName faultCode) {
        if (faultCode == null)
            faultCode = getDefaultFaultCode(soapVersion);
        return createSOAPFaultMessage(soapVersion, faultString, faultCode, null);
    }

    public static Message createSOAPFaultMessage(SOAPVersion soapVersion, SOAPFault fault) {
        switch(soapVersion) {
            case SOAP_11:
                return JAXBMessage.create(JAXB_CONTEXT, new SOAP11Fault(fault), soapVersion);
            case SOAP_12:
                return JAXBMessage.create(JAXB_CONTEXT, new SOAP12Fault(fault), soapVersion);
            default:
                throw new AssertionError();
        }
    }

    private static Message createSOAPFaultMessage(SOAPVersion soapVersion, String faultString, QName faultCode, Element detail) {
        switch(soapVersion) {
            case SOAP_11:
                return JAXBMessage.create(JAXB_CONTEXT, new SOAP11Fault(faultCode, faultString, null, detail), soapVersion);
            case SOAP_12:
                return JAXBMessage.create(JAXB_CONTEXT, new SOAP12Fault(faultCode, faultString, detail), soapVersion);
            default:
                throw new AssertionError();
        }
    }

    final void captureStackTrace(@Nullable Throwable t) {
        if (t == null)
            return;
        if (!captureStackTrace)
            return;
        try {
            Document d = DOMUtil.createDom();
            ExceptionBean.marshal(t, d);
            DetailType detail = getDetail();
            if (detail == null)
                setDetail(detail = new DetailType());
            detail.getDetails().add(d.getDocumentElement());
        } catch (JAXBException e) {
            logger.log(Level.WARNING, "Unable to capture the stack trace into XML", e);
        }
    }

    private <T extends Throwable> T attachServerException(T t) {
        DetailType detail = getDetail();
        if (detail == null)
            return t;
        for (Element n : detail.getDetails()) {
            if (ExceptionBean.isStackTraceXml(n)) {
                try {
                    t.initCause(ExceptionBean.unmarshal(n));
                } catch (JAXBException e) {
                    logger.log(Level.WARNING, "Unable to read the capture stack trace in the fault", e);
                }
                return t;
            }
        }
        return t;
    }

    abstract protected Throwable getProtocolException();

    private Object getJAXBObject(Node jaxbBean, CheckedExceptionImpl ce) throws JAXBException {
        XMLBridge bridge = ce.getBond();
        return bridge.unmarshal(jaxbBean, null);
    }

    private Exception createUserDefinedException(CheckedExceptionImpl ce) {
        Class exceptionClass = ce.getExceptionClass();
        Class detailBean = ce.getDetailBean();
        try {
            Node detailNode = getDetail().getDetails().get(0);
            Object jaxbDetail = getJAXBObject(detailNode, ce);
            Constructor exConstructor;
            try {
                exConstructor = exceptionClass.getConstructor(String.class, detailBean);
                return (Exception) exConstructor.newInstance(getFaultString(), jaxbDetail);
            } catch (NoSuchMethodException e) {
                exConstructor = exceptionClass.getConstructor(String.class);
                return (Exception) exConstructor.newInstance(getFaultString());
            }
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    private static String getWriteMethod(Field f) {
        return "set" + StringUtils.capitalize(f.getName());
    }

    private static Object getFaultDetail(CheckedExceptionImpl ce, Throwable exception) {
        if (ce == null)
            return null;
        if (ce.getExceptionType().equals(ExceptionType.UserDefined)) {
            return createDetailFromUserDefinedException(ce, exception);
        }
        try {
            Method m = exception.getClass().getMethod("getFaultInfo");
            return m.invoke(exception);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    private static Object createDetailFromUserDefinedException(CheckedExceptionImpl ce, Object exception) {
        Class detailBean = ce.getDetailBean();
        Field[] fields = detailBean.getDeclaredFields();
        try {
            Object detail = detailBean.newInstance();
            for (Field f : fields) {
                Method em = exception.getClass().getMethod(getReadMethod(f));
                try {
                    Method sm = detailBean.getMethod(getWriteMethod(f), em.getReturnType());
                    sm.invoke(detail, em.invoke(exception));
                } catch (NoSuchMethodException ne) {
                    Field sf = detailBean.getField(f.getName());
                    sf.set(detail, em.invoke(exception));
                }
            }
            return detail;
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    private static String getReadMethod(Field f) {
        if (f.getType().isAssignableFrom(boolean.class))
            return "is" + StringUtils.capitalize(f.getName());
        return "get" + StringUtils.capitalize(f.getName());
    }

    private static Message createSOAP11Fault(SOAPVersion soapVersion, Throwable e, Object detail, CheckedExceptionImpl ce, QName faultCode) {
        SOAPFaultException soapFaultException = null;
        String faultString = null;
        String faultActor = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException) e;
        } else if (cause != null && cause instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException) e.getCause();
        }
        if (soapFaultException != null) {
            QName soapFaultCode = soapFaultException.getFault().getFaultCodeAsQName();
            if (soapFaultCode != null)
                faultCode = soapFaultCode;
            faultString = soapFaultException.getFault().getFaultString();
            faultActor = soapFaultException.getFault().getFaultActor();
        }
        if (faultCode == null) {
            faultCode = getDefaultFaultCode(soapVersion);
        }
        if (faultString == null) {
            faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString();
            }
        }
        Element detailNode = null;
        QName firstEntry = null;
        if (detail == null && soapFaultException != null) {
            detailNode = soapFaultException.getFault().getDetail();
            firstEntry = getFirstDetailEntryName((Detail) detailNode);
        } else if (ce != null) {
            try {
                DOMResult dr = new DOMResult();
                ce.getBond().marshal(detail, dr);
                detailNode = (Element) dr.getNode().getFirstChild();
                firstEntry = getFirstDetailEntryName(detailNode);
            } catch (JAXBException e1) {
                faultString = e.getMessage();
                faultCode = getDefaultFaultCode(soapVersion);
            }
        }
        SOAP11Fault soap11Fault = new SOAP11Fault(faultCode, faultString, faultActor, detailNode);
        if (ce == null) {
            soap11Fault.captureStackTrace(e);
        }
        Message msg = JAXBMessage.create(JAXB_CONTEXT, soap11Fault, soapVersion);
        return new FaultMessage(msg, firstEntry);
    }

    @Nullable
    private static QName getFirstDetailEntryName(@Nullable Detail detail) {
        if (detail != null) {
            Iterator<DetailEntry> it = detail.getDetailEntries();
            if (it.hasNext()) {
                DetailEntry entry = it.next();
                return getFirstDetailEntryName(entry);
            }
        }
        return null;
    }

    @NotNull
    private static QName getFirstDetailEntryName(@NotNull Element entry) {
        return new QName(entry.getNamespaceURI(), entry.getLocalName());
    }

    private static Message createSOAP12Fault(SOAPVersion soapVersion, Throwable e, Object detail, CheckedExceptionImpl ce, QName faultCode) {
        SOAPFaultException soapFaultException = null;
        CodeType code = null;
        String faultString = null;
        String faultRole = null;
        String faultNode = null;
        Throwable cause = e.getCause();
        if (e instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException) e;
        } else if (cause != null && cause instanceof SOAPFaultException) {
            soapFaultException = (SOAPFaultException) e.getCause();
        }
        if (soapFaultException != null) {
            SOAPFault fault = soapFaultException.getFault();
            QName soapFaultCode = fault.getFaultCodeAsQName();
            if (soapFaultCode != null) {
                faultCode = soapFaultCode;
                code = new CodeType(faultCode);
                Iterator iter = fault.getFaultSubcodes();
                boolean first = true;
                SubcodeType subcode = null;
                while (iter.hasNext()) {
                    QName value = (QName) iter.next();
                    if (first) {
                        SubcodeType sct = new SubcodeType(value);
                        code.setSubcode(sct);
                        subcode = sct;
                        first = false;
                        continue;
                    }
                    subcode = fillSubcodes(subcode, value);
                }
            }
            faultString = soapFaultException.getFault().getFaultString();
            faultRole = soapFaultException.getFault().getFaultActor();
            faultNode = soapFaultException.getFault().getFaultNode();
        }
        if (faultCode == null) {
            faultCode = getDefaultFaultCode(soapVersion);
            code = new CodeType(faultCode);
        } else if (code == null) {
            code = new CodeType(faultCode);
        }
        if (faultString == null) {
            faultString = e.getMessage();
            if (faultString == null) {
                faultString = e.toString();
            }
        }
        ReasonType reason = new ReasonType(faultString);
        Element detailNode = null;
        QName firstEntry = null;
        if (detail == null && soapFaultException != null) {
            detailNode = soapFaultException.getFault().getDetail();
            firstEntry = getFirstDetailEntryName((Detail) detailNode);
        } else if (detail != null) {
            try {
                DOMResult dr = new DOMResult();
                ce.getBond().marshal(detail, dr);
                detailNode = (Element) dr.getNode().getFirstChild();
                firstEntry = getFirstDetailEntryName(detailNode);
            } catch (JAXBException e1) {
                faultString = e.getMessage();
                faultCode = getDefaultFaultCode(soapVersion);
            }
        }
        SOAP12Fault soap12Fault = new SOAP12Fault(code, reason, faultNode, faultRole, detailNode);
        if (ce == null) {
            soap12Fault.captureStackTrace(e);
        }
        Message msg = JAXBMessage.create(JAXB_CONTEXT, soap12Fault, soapVersion);
        return new FaultMessage(msg, firstEntry);
    }

    private static SubcodeType fillSubcodes(SubcodeType parent, QName value) {
        SubcodeType newCode = new SubcodeType(value);
        parent.setSubcode(newCode);
        return newCode;
    }

    private static QName getDefaultFaultCode(SOAPVersion soapVersion) {
        return soapVersion.faultCodeServer;
    }

    public static SOAPFaultBuilder create(Message msg) throws JAXBException {
        return msg.readPayloadAsJAXB(JAXB_CONTEXT.createUnmarshaller());
    }

    private static final JAXBContext JAXB_CONTEXT;

    private static final Logger logger = Logger.getLogger(SOAPFaultBuilder.class.getName());

    public static boolean captureStackTrace;

    static final String CAPTURE_STACK_TRACE_PROPERTY = SOAPFaultBuilder.class.getName() + ".captureStackTrace";

    static {
        try {
            captureStackTrace = Boolean.getBoolean(CAPTURE_STACK_TRACE_PROPERTY);
        } catch (SecurityException e) {
        }
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(SOAP11Fault.class, SOAP12Fault.class);
        } catch (JAXBException e) {
            throw new Error(e);
        }
    }
}
