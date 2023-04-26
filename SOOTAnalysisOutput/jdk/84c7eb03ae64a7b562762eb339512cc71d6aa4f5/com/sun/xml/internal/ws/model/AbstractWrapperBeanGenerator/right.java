package com.sun.xml.internal.ws.model;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.internal.bind.v2.model.nav.Navigator;
import com.sun.xml.internal.ws.spi.db.BindingHelper;
import com.sun.xml.internal.ws.util.StringUtils;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.ws.WebServiceException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractWrapperBeanGenerator<T, C, M, A extends Comparable> {

    private static final Logger LOGGER = Logger.getLogger(AbstractWrapperBeanGenerator.class.getName());

    private static final String RETURN = "return";

    private static final String EMTPY_NAMESPACE_ID = "";

    private static final Class[] jaxbAnns = new Class[] { XmlAttachmentRef.class, XmlMimeType.class, XmlJavaTypeAdapter.class, XmlList.class, XmlElement.class };

    private static final Set<String> skipProperties = new HashSet<String>();

    static {
        skipProperties.add("getCause");
        skipProperties.add("getLocalizedMessage");
        skipProperties.add("getClass");
        skipProperties.add("getStackTrace");
        skipProperties.add("getSuppressed");
    }

    private final AnnotationReader<T, C, ?, M> annReader;

    private final Navigator<T, C, ?, M> nav;

    private final BeanMemberFactory<T, A> factory;

    protected AbstractWrapperBeanGenerator(AnnotationReader<T, C, ?, M> annReader, Navigator<T, C, ?, M> nav, BeanMemberFactory<T, A> factory) {
        this.annReader = annReader;
        this.nav = nav;
        this.factory = factory;
    }

    public static interface BeanMemberFactory<T, A> {

        A createWrapperBeanMember(T paramType, String paramName, List<Annotation> jaxbAnnotations);
    }

    private List<Annotation> collectJAXBAnnotations(M method) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for (Class jaxbClass : jaxbAnns) {
            Annotation ann = annReader.getMethodAnnotation(jaxbClass, method, null);
            if (ann != null) {
                jaxbAnnotation.add(ann);
            }
        }
        return jaxbAnnotation;
    }

    private List<Annotation> collectJAXBAnnotations(M method, int paramIndex) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for (Class jaxbClass : jaxbAnns) {
            Annotation ann = annReader.getMethodParameterAnnotation(jaxbClass, method, paramIndex, null);
            if (ann != null) {
                jaxbAnnotation.add(ann);
            }
        }
        return jaxbAnnotation;
    }

    protected abstract T getSafeType(T type);

    protected abstract T getHolderValueType(T type);

    protected abstract boolean isVoidType(T type);

    public List<A> collectRequestBeanMembers(M method) {
        List<A> requestMembers = new ArrayList<A>();
        int paramIndex = -1;
        for (T param : nav.getMethodParameters(method)) {
            paramIndex++;
            WebParam webParam = annReader.getMethodParameterAnnotation(WebParam.class, method, paramIndex, null);
            if (webParam != null && (webParam.header() || webParam.mode().equals(WebParam.Mode.OUT))) {
                continue;
            }
            T holderType = getHolderValueType(param);
            T paramType = (holderType != null) ? holderType : getSafeType(param);
            String paramName = (webParam != null && webParam.name().length() > 0) ? webParam.name() : "arg" + paramIndex;
            String paramNamespace = (webParam != null && webParam.targetNamespace().length() > 0) ? webParam.targetNamespace() : EMTPY_NAMESPACE_ID;
            List<Annotation> jaxbAnnotation = collectJAXBAnnotations(method, paramIndex);
            processXmlElement(jaxbAnnotation, paramName, paramNamespace, paramType);
            A member = factory.createWrapperBeanMember(paramType, getPropertyName(paramName), jaxbAnnotation);
            requestMembers.add(member);
        }
        return requestMembers;
    }

    public List<A> collectResponseBeanMembers(M method) {
        List<A> responseMembers = new ArrayList<A>();
        String responseElementName = RETURN;
        String responseNamespace = EMTPY_NAMESPACE_ID;
        boolean isResultHeader = false;
        WebResult webResult = annReader.getMethodAnnotation(WebResult.class, method, null);
        if (webResult != null) {
            if (webResult.name().length() > 0) {
                responseElementName = webResult.name();
            }
            if (webResult.targetNamespace().length() > 0) {
                responseNamespace = webResult.targetNamespace();
            }
            isResultHeader = webResult.header();
        }
        T returnType = getSafeType(nav.getReturnType(method));
        if (!isVoidType(returnType) && !isResultHeader) {
            List<Annotation> jaxbRespAnnotations = collectJAXBAnnotations(method);
            processXmlElement(jaxbRespAnnotations, responseElementName, responseNamespace, returnType);
            responseMembers.add(factory.createWrapperBeanMember(returnType, getPropertyName(responseElementName), jaxbRespAnnotations));
        }
        int paramIndex = -1;
        for (T param : nav.getMethodParameters(method)) {
            paramIndex++;
            T paramType = getHolderValueType(param);
            WebParam webParam = annReader.getMethodParameterAnnotation(WebParam.class, method, paramIndex, null);
            if (paramType == null || (webParam != null && webParam.header())) {
                continue;
            }
            String paramName = (webParam != null && webParam.name().length() > 0) ? webParam.name() : "arg" + paramIndex;
            String paramNamespace = (webParam != null && webParam.targetNamespace().length() > 0) ? webParam.targetNamespace() : EMTPY_NAMESPACE_ID;
            List<Annotation> jaxbAnnotation = collectJAXBAnnotations(method, paramIndex);
            processXmlElement(jaxbAnnotation, paramName, paramNamespace, paramType);
            A member = factory.createWrapperBeanMember(paramType, getPropertyName(paramName), jaxbAnnotation);
            responseMembers.add(member);
        }
        return responseMembers;
    }

    private void processXmlElement(List<Annotation> jaxb, String elemName, String elemNS, T type) {
        XmlElement elemAnn = null;
        for (Annotation a : jaxb) {
            if (a.annotationType() == XmlElement.class) {
                elemAnn = (XmlElement) a;
                jaxb.remove(a);
                break;
            }
        }
        String name = (elemAnn != null && !elemAnn.name().equals("##default")) ? elemAnn.name() : elemName;
        String ns = (elemAnn != null && !elemAnn.namespace().equals("##default")) ? elemAnn.namespace() : elemNS;
        boolean nillable = nav.isArray(type) || (elemAnn != null && elemAnn.nillable());
        boolean required = elemAnn != null && elemAnn.required();
        XmlElementHandler handler = new XmlElementHandler(name, ns, nillable, required);
        XmlElement elem = (XmlElement) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { XmlElement.class }, handler);
        jaxb.add(elem);
    }

    private static class XmlElementHandler implements InvocationHandler {

        private String name;

        private String namespace;

        private boolean nillable;

        private boolean required;

        XmlElementHandler(String name, String namespace, boolean nillable, boolean required) {
            this.name = name;
            this.namespace = namespace;
            this.nillable = nillable;
            this.required = required;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("name")) {
                return name;
            } else if (methodName.equals("namespace")) {
                return namespace;
            } else if (methodName.equals("nillable")) {
                return nillable;
            } else if (methodName.equals("required")) {
                return required;
            } else {
                throw new WebServiceException("Not handling " + methodName);
            }
        }
    }

    public Collection<A> collectExceptionBeanMembers(C exception) {
        return collectExceptionBeanMembers(exception, true);
    }

    public Collection<A> collectExceptionBeanMembers(C exception, boolean decapitalize) {
        TreeMap<String, A> fields = new TreeMap<String, A>();
        getExceptionProperties(exception, fields, decapitalize);
        XmlType xmlType = annReader.getClassAnnotation(XmlType.class, exception, null);
        if (xmlType != null) {
            String[] propOrder = xmlType.propOrder();
            if (propOrder.length > 0 && propOrder[0].length() != 0) {
                List<A> list = new ArrayList<A>();
                for (String prop : propOrder) {
                    A a = fields.get(prop);
                    if (a != null) {
                        list.add(a);
                    } else {
                        throw new WebServiceException("Exception " + exception + " has @XmlType and its propOrder contains unknown property " + prop);
                    }
                }
                return list;
            }
        }
        return fields.values();
    }

    private void getExceptionProperties(C exception, TreeMap<String, A> fields, boolean decapitalize) {
        C sc = nav.getSuperClass(exception);
        if (sc != null) {
            getExceptionProperties(sc, fields, decapitalize);
        }
        Collection<? extends M> methods = nav.getDeclaredMethods(exception);
        for (M method : methods) {
            if (!nav.isPublicMethod(method) || (nav.isStaticMethod(method) && nav.isFinalMethod(method))) {
                continue;
            }
            if (!nav.isPublicMethod(method)) {
                continue;
            }
            String name = nav.getMethodName(method);
            if (!(name.startsWith("get") || name.startsWith("is")) || skipProperties.contains(name) || name.equals("get") || name.equals("is")) {
                continue;
            }
            T returnType = getSafeType(nav.getReturnType(method));
            if (nav.getMethodParameters(method).length == 0) {
                String fieldName = name.startsWith("get") ? name.substring(3) : name.substring(2);
                if (decapitalize)
                    fieldName = StringUtils.decapitalize(fieldName);
                fields.put(fieldName, factory.createWrapperBeanMember(returnType, fieldName, Collections.<Annotation>emptyList()));
            }
        }
    }

    private static String getPropertyName(String name) {
        String propertyName = BindingHelper.mangleNameToVariableName(name);
        return getJavaReservedVarialbeName(propertyName);
    }

    @NotNull
    private static String getJavaReservedVarialbeName(@NotNull String name) {
        String reservedName = reservedWords.get(name);
        return reservedName == null ? name : reservedName;
    }

    private static final Map<String, String> reservedWords;

    static {
        reservedWords = new HashMap<String, String>();
        reservedWords.put("abstract", "_abstract");
        reservedWords.put("assert", "_assert");
        reservedWords.put("boolean", "_boolean");
        reservedWords.put("break", "_break");
        reservedWords.put("byte", "_byte");
        reservedWords.put("case", "_case");
        reservedWords.put("catch", "_catch");
        reservedWords.put("char", "_char");
        reservedWords.put("class", "_class");
        reservedWords.put("const", "_const");
        reservedWords.put("continue", "_continue");
        reservedWords.put("default", "_default");
        reservedWords.put("do", "_do");
        reservedWords.put("double", "_double");
        reservedWords.put("else", "_else");
        reservedWords.put("extends", "_extends");
        reservedWords.put("false", "_false");
        reservedWords.put("final", "_final");
        reservedWords.put("finally", "_finally");
        reservedWords.put("float", "_float");
        reservedWords.put("for", "_for");
        reservedWords.put("goto", "_goto");
        reservedWords.put("if", "_if");
        reservedWords.put("implements", "_implements");
        reservedWords.put("import", "_import");
        reservedWords.put("instanceof", "_instanceof");
        reservedWords.put("int", "_int");
        reservedWords.put("interface", "_interface");
        reservedWords.put("long", "_long");
        reservedWords.put("native", "_native");
        reservedWords.put("new", "_new");
        reservedWords.put("null", "_null");
        reservedWords.put("package", "_package");
        reservedWords.put("private", "_private");
        reservedWords.put("protected", "_protected");
        reservedWords.put("public", "_public");
        reservedWords.put("return", "_return");
        reservedWords.put("short", "_short");
        reservedWords.put("static", "_static");
        reservedWords.put("strictfp", "_strictfp");
        reservedWords.put("super", "_super");
        reservedWords.put("switch", "_switch");
        reservedWords.put("synchronized", "_synchronized");
        reservedWords.put("this", "_this");
        reservedWords.put("throw", "_throw");
        reservedWords.put("throws", "_throws");
        reservedWords.put("transient", "_transient");
        reservedWords.put("true", "_true");
        reservedWords.put("try", "_try");
        reservedWords.put("void", "_void");
        reservedWords.put("volatile", "_volatile");
        reservedWords.put("while", "_while");
        reservedWords.put("enum", "_enum");
    }
}
