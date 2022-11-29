package com.sun.xml.internal.bind.v2.model.impl;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import com.sun.xml.internal.bind.util.Which;
import com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.internal.bind.v2.model.annotation.ClassLocatable;
import com.sun.xml.internal.bind.v2.model.annotation.Locatable;
import com.sun.xml.internal.bind.v2.model.core.ClassInfo;
import com.sun.xml.internal.bind.v2.model.core.ErrorHandler;
import com.sun.xml.internal.bind.v2.model.core.LeafInfo;
import com.sun.xml.internal.bind.v2.model.core.NonElement;
import com.sun.xml.internal.bind.v2.model.core.PropertyInfo;
import com.sun.xml.internal.bind.v2.model.core.PropertyKind;
import com.sun.xml.internal.bind.v2.model.core.Ref;
import com.sun.xml.internal.bind.v2.model.core.RegistryInfo;
import com.sun.xml.internal.bind.v2.model.core.TypeInfo;
import com.sun.xml.internal.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.internal.bind.v2.model.nav.Navigator;
import com.sun.xml.internal.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.internal.bind.WhiteSpaceProcessor;

public class ModelBuilder<T, C, F, M> {

    final TypeInfoSetImpl<T, C, F, M> typeInfoSet;

    public final AnnotationReader<T, C, F, M> reader;

    public final Navigator<T, C, F, M> nav;

    private final Map<QName, TypeInfo> typeNames = new HashMap<QName, TypeInfo>();

    public final String defaultNsUri;

    final Map<String, RegistryInfoImpl<T, C, F, M>> registries = new HashMap<String, RegistryInfoImpl<T, C, F, M>>();

    private final Map<C, C> subclassReplacements;

    private ErrorHandler errorHandler;

    private boolean hadError;

    public boolean hasSwaRef;

    private final ErrorHandler proxyErrorHandler = new ErrorHandler() {

        public void error(IllegalAnnotationException e) {
            reportError(e);
        }
    };

    public ModelBuilder(AnnotationReader<T, C, F, M> reader, Navigator<T, C, F, M> navigator, Map<C, C> subclassReplacements, String defaultNamespaceRemap) {
        this.reader = reader;
        this.nav = navigator;
        this.subclassReplacements = subclassReplacements;
        if (defaultNamespaceRemap == null)
            defaultNamespaceRemap = "";
        this.defaultNsUri = defaultNamespaceRemap;
        reader.setErrorHandler(proxyErrorHandler);
        typeInfoSet = createTypeInfoSet();
    }

    static {
        try {
            XmlSchema s = null;
            s.location();
        } catch (NullPointerException e) {
        } catch (NoSuchMethodError e) {
            Messages res;
            if (SecureLoader.getClassClassLoader(XmlSchema.class) == null) {
                res = Messages.INCOMPATIBLE_API_VERSION_MUSTANG;
            } else {
                res = Messages.INCOMPATIBLE_API_VERSION;
            }
            throw new LinkageError(res.format(Which.which(XmlSchema.class), Which.which(ModelBuilder.class)));
        }
    }

    static {
        try {
            WhiteSpaceProcessor.isWhiteSpace("xyz");
        } catch (NoSuchMethodError e) {
            throw new LinkageError(Messages.RUNNING_WITH_1_0_RUNTIME.format(Which.which(WhiteSpaceProcessor.class), Which.which(ModelBuilder.class)));
        }
    }

    protected TypeInfoSetImpl<T, C, F, M> createTypeInfoSet() {
        return new TypeInfoSetImpl<T, C, F, M>(nav, reader, BuiltinLeafInfoImpl.createLeaves(nav));
    }

    public NonElement<T, C> getClassInfo(C clazz, Locatable upstream) {
        return getClassInfo(clazz, false, upstream);
    }

    public NonElement<T, C> getClassInfo(C clazz, boolean searchForSuperClass, Locatable upstream) {
        assert clazz != null;
        NonElement<T, C> r = typeInfoSet.getClassInfo(clazz);
        if (r != null)
            return r;
        if (nav.isEnum(clazz)) {
            EnumLeafInfoImpl<T, C, F, M> li = createEnumLeafInfo(clazz, upstream);
            typeInfoSet.add(li);
            r = li;
            addTypeName(r);
        } else {
            boolean isReplaced = subclassReplacements.containsKey(clazz);
            if (isReplaced && !searchForSuperClass) {
                r = getClassInfo(subclassReplacements.get(clazz), upstream);
            } else if (reader.hasClassAnnotation(clazz, XmlTransient.class) || isReplaced) {
                r = getClassInfo(nav.getSuperClass(clazz), searchForSuperClass, new ClassLocatable<C>(upstream, clazz, nav));
            } else {
                ClassInfoImpl<T, C, F, M> ci = createClassInfo(clazz, upstream);
                typeInfoSet.add(ci);
                for (PropertyInfo<T, C> p : ci.getProperties()) {
                    if (p.kind() == PropertyKind.REFERENCE) {
                        String pkg = nav.getPackageName(ci.getClazz());
                        if (!registries.containsKey(pkg)) {
                            C c = nav.findClass(pkg + ".ObjectFactory", ci.getClazz());
                            if (c != null)
                                addRegistry(c, (Locatable) p);
                        }
                    }
                    for (TypeInfo<T, C> t : p.ref()) ;
                }
                ci.getBaseClass();
                r = ci;
                addTypeName(r);
            }
        }
        XmlSeeAlso sa = reader.getClassAnnotation(XmlSeeAlso.class, clazz, upstream);
        if (sa != null) {
            for (T t : reader.getClassArrayValue(sa, "value")) {
                getTypeInfo(t, (Locatable) sa);
            }
        }
        return r;
    }

    private void addTypeName(NonElement<T, C> r) {
        QName t = r.getTypeName();
        if (t == null)
            return;
        TypeInfo old = typeNames.put(t, r);
        if (old != null) {
            reportError(new IllegalAnnotationException(Messages.CONFLICTING_XML_TYPE_MAPPING.format(r.getTypeName()), old, r));
        }
    }

    public NonElement<T, C> getTypeInfo(T t, Locatable upstream) {
        NonElement<T, C> r = typeInfoSet.getTypeInfo(t);
        if (r != null)
            return r;
        if (nav.isArray(t)) {
            ArrayInfoImpl<T, C, F, M> ai = createArrayInfo(upstream, t);
            addTypeName(ai);
            typeInfoSet.add(ai);
            return ai;
        }
        C c = nav.asDecl(t);
        assert c != null : t.toString() + " must be a leaf, but we failed to recognize it.";
        return getClassInfo(c, upstream);
    }

    public NonElement<T, C> getTypeInfo(Ref<T, C> ref) {
        assert !ref.valueList;
        C c = nav.asDecl(ref.type);
        if (c != null && reader.getClassAnnotation(XmlRegistry.class, c, null) != null) {
            if (!registries.containsKey(nav.getPackageName(c)))
                addRegistry(c, null);
            return null;
        } else
            return getTypeInfo(ref.type, null);
    }

    protected EnumLeafInfoImpl<T, C, F, M> createEnumLeafInfo(C clazz, Locatable upstream) {
        return new EnumLeafInfoImpl<T, C, F, M>(this, upstream, clazz, nav.use(clazz));
    }

    protected ClassInfoImpl<T, C, F, M> createClassInfo(C clazz, Locatable upstream) {
        return new ClassInfoImpl<T, C, F, M>(this, upstream, clazz);
    }

    protected ElementInfoImpl<T, C, F, M> createElementInfo(RegistryInfoImpl<T, C, F, M> registryInfo, M m) throws IllegalAnnotationException {
        return new ElementInfoImpl<T, C, F, M>(this, registryInfo, m);
    }

    protected ArrayInfoImpl<T, C, F, M> createArrayInfo(Locatable upstream, T arrayType) {
        return new ArrayInfoImpl<T, C, F, M>(this, upstream, arrayType);
    }

    public RegistryInfo<T, C> addRegistry(C registryClass, Locatable upstream) {
        return new RegistryInfoImpl<T, C, F, M>(this, upstream, registryClass);
    }

    public RegistryInfo<T, C> getRegistry(String packageName) {
        return registries.get(packageName);
    }

    private boolean linked;

    public TypeInfoSet<T, C, F, M> link() {
        assert !linked;
        linked = true;
        for (ElementInfoImpl ei : typeInfoSet.getAllElements()) ei.link();
        for (ClassInfoImpl ci : typeInfoSet.beans().values()) ci.link();
        for (EnumLeafInfoImpl li : typeInfoSet.enums().values()) li.link();
        if (hadError)
            return null;
        else
            return typeInfoSet;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public final void reportError(IllegalAnnotationException e) {
        hadError = true;
        if (errorHandler != null)
            errorHandler.error(e);
    }

    public boolean isReplaced(C sc) {
        return subclassReplacements.containsKey(sc);
    }
}
