package org.springframework.context.annotation;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jndi.support.SimpleJndiBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

@SuppressWarnings("serial")
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

<<<<<<< MINE
    private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);
=======
    private static final boolean jndiPresent = ClassUtils.isPresent("javax.naming.InitialContext", CommonAnnotationBeanPostProcessor.class.getClassLoader());

    private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);

    @Nullable
    private static final Class<? extends Annotation> webServiceRefClass;
>>>>>>> YOURS

    @Nullable
    private static final Class<? extends Annotation> ejbClass;

    static {
        resourceAnnotationTypes.add(Resource.class);
<<<<<<< MINE
        ejbClass = loadAnnotationType("jakarta.ejb.EJB");
=======
        webServiceRefClass = loadAnnotationType("javax.xml.ws.WebServiceRef");
        if (webServiceRefClass != null) {
            resourceAnnotationTypes.add(webServiceRefClass);
        }
        ejbClass = loadAnnotationType("javax.ejb.EJB");
>>>>>>> YOURS
        if (ejbClass != null) {
            resourceAnnotationTypes.add(ejbClass);
        }
    }

    private final Set<String> ignoredResourceTypes = new HashSet<>(1);

    private boolean fallbackToDefaultTypeMatch = true;

    private boolean alwaysUseJndiLookup = false;

    @Nullable
    private transient BeanFactory jndiFactory;

    @Nullable
    private transient BeanFactory resourceFactory;

    @Nullable
    private transient BeanFactory beanFactory;

    @Nullable
    private transient StringValueResolver embeddedValueResolver;

    private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    public CommonAnnotationBeanPostProcessor() {
        setOrder(Ordered.LOWEST_PRECEDENCE - 3);
        setInitAnnotationType(PostConstruct.class);
        setDestroyAnnotationType(PreDestroy.class);
<<<<<<< MINE
=======
        ignoreResourceType("javax.xml.ws.WebServiceContext");
        if (jndiPresent) {
            this.jndiFactory = new SimpleJndiBeanFactory();
        }
>>>>>>> YOURS
    }

    public void ignoreResourceType(String resourceType) {
        Assert.notNull(resourceType, "Ignored resource type must not be null");
        this.ignoredResourceTypes.add(resourceType);
    }

    public void setFallbackToDefaultTypeMatch(boolean fallbackToDefaultTypeMatch) {
        this.fallbackToDefaultTypeMatch = fallbackToDefaultTypeMatch;
    }

    public void setAlwaysUseJndiLookup(boolean alwaysUseJndiLookup) {
        this.alwaysUseJndiLookup = alwaysUseJndiLookup;
    }

    public void setJndiFactory(BeanFactory jndiFactory) {
        Assert.notNull(jndiFactory, "BeanFactory must not be null");
        this.jndiFactory = jndiFactory;
    }

    public void setResourceFactory(BeanFactory resourceFactory) {
        Assert.notNull(resourceFactory, "BeanFactory must not be null");
        this.resourceFactory = resourceFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
        if (this.resourceFactory == null) {
            this.resourceFactory = beanFactory;
        }
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName);
        InjectionMetadata metadata = findResourceMetadata(beanName, beanType, null);
        metadata.checkConfigMembers(beanDefinition);
    }

    @Override
    public void resetBeanDefinition(String beanName) {
        this.injectionMetadataCache.remove(beanName);
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        return null;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        return true;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        InjectionMetadata metadata = findResourceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
        }
        return pvs;
    }

    @Deprecated
    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) {
        return postProcessProperties(pvs, bean, beanName);
    }

    private InjectionMetadata findResourceMetadata(String beanName, final Class<?> clazz, @Nullable PropertyValues pvs) {
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildResourceMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildResourceMetadata(final Class<?> clazz) {
        if (!AnnotationUtils.isCandidateClass(clazz, resourceAnnotationTypes)) {
            return InjectionMetadata.EMPTY;
        }
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;
        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();
            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                if (ejbClass != null && field.isAnnotationPresent(ejbClass)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new IllegalStateException("@EJB annotation is not supported on static fields");
                    }
                    currElements.add(new EjbRefElement(field, field, null));
                } else if (field.isAnnotationPresent(Resource.class)) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new IllegalStateException("@Resource annotation is not supported on static fields");
                    }
                    if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
                        currElements.add(new ResourceElement(field, field, null));
                    }
                }
            });
            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (ejbClass != null && bridgedMethod.isAnnotationPresent(ejbClass)) {
                        if (Modifier.isStatic(method.getModifiers())) {
                            throw new IllegalStateException("@EJB annotation is not supported on static methods");
                        }
                        if (method.getParameterCount() != 1) {
                            throw new IllegalStateException("@EJB annotation requires a single-arg method: " + method);
                        }
                        PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                        currElements.add(new EjbRefElement(method, bridgedMethod, pd));
                    } else if (bridgedMethod.isAnnotationPresent(Resource.class)) {
                        if (Modifier.isStatic(method.getModifiers())) {
                            throw new IllegalStateException("@Resource annotation is not supported on static methods");
                        }
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes.length != 1) {
                            throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
                        }
                        if (!this.ignoredResourceTypes.contains(paramTypes[0].getName())) {
                            PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                            currElements.add(new ResourceElement(method, bridgedMethod, pd));
                        }
                    }
                }
            });
            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
        return InjectionMetadata.forElements(elements, clazz);
    }

    protected Object buildLazyResourceProxy(final LookupElement element, @Nullable final String requestingBeanName) {
        TargetSource ts = new TargetSource() {

            @Override
            public Class<?> getTargetClass() {
                return element.lookupType;
            }

            @Override
            public boolean isStatic() {
                return false;
            }

            @Override
            public Object getTarget() {
                return getResource(element, requestingBeanName);
            }

            @Override
            public void releaseTarget(Object target) {
            }
        };
        ProxyFactory pf = new ProxyFactory();
        pf.setTargetSource(ts);
        if (element.lookupType.isInterface()) {
            pf.addInterface(element.lookupType);
        }
        ClassLoader classLoader = (this.beanFactory instanceof ConfigurableBeanFactory ? ((ConfigurableBeanFactory) this.beanFactory).getBeanClassLoader() : null);
        return pf.getProxy(classLoader);
    }

    protected Object getResource(LookupElement element, @Nullable String requestingBeanName) throws NoSuchBeanDefinitionException {
        String jndiName = null;
        if (StringUtils.hasLength(element.mappedName)) {
            jndiName = element.mappedName;
        } else if (this.alwaysUseJndiLookup) {
            jndiName = element.name;
        }
        if (jndiName != null) {
            if (this.jndiFactory == null) {
                throw new NoSuchBeanDefinitionException(element.lookupType, "No JNDI factory configured - specify the 'jndiFactory' property");
            }
            return this.jndiFactory.getBean(jndiName, element.lookupType);
        }
        if (this.resourceFactory == null) {
            throw new NoSuchBeanDefinitionException(element.lookupType, "No resource factory configured - specify the 'resourceFactory' property");
        }
        return autowireResource(this.resourceFactory, element, requestingBeanName);
    }

    protected Object autowireResource(BeanFactory factory, LookupElement element, @Nullable String requestingBeanName) throws NoSuchBeanDefinitionException {
        Object resource;
        Set<String> autowiredBeanNames;
        String name = element.name;
        if (factory instanceof AutowireCapableBeanFactory) {
            AutowireCapableBeanFactory beanFactory = (AutowireCapableBeanFactory) factory;
            DependencyDescriptor descriptor = element.getDependencyDescriptor();
            if (this.fallbackToDefaultTypeMatch && element.isDefaultName && !factory.containsBean(name)) {
                autowiredBeanNames = new LinkedHashSet<>();
                resource = beanFactory.resolveDependency(descriptor, requestingBeanName, autowiredBeanNames, null);
                if (resource == null) {
                    throw new NoSuchBeanDefinitionException(element.getLookupType(), "No resolvable resource object");
                }
            } else {
                resource = beanFactory.resolveBeanByName(name, descriptor);
                autowiredBeanNames = Collections.singleton(name);
            }
        } else {
            resource = factory.getBean(name, element.lookupType);
            autowiredBeanNames = Collections.singleton(name);
        }
        if (factory instanceof ConfigurableBeanFactory) {
            ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) factory;
            for (String autowiredBeanName : autowiredBeanNames) {
                if (requestingBeanName != null && beanFactory.containsBean(autowiredBeanName)) {
                    beanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
                }
            }
        }
        return resource;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Class<? extends Annotation> loadAnnotationType(String name) {
        try {
            return (Class<? extends Annotation>) ClassUtils.forName(name, CommonAnnotationBeanPostProcessor.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    protected abstract static class LookupElement extends InjectionMetadata.InjectedElement {

        protected String name = "";

        protected boolean isDefaultName = false;

        protected Class<?> lookupType = Object.class;

        @Nullable
        protected String mappedName;

        public LookupElement(Member member, @Nullable PropertyDescriptor pd) {
            super(member, pd);
        }

        public final String getName() {
            return this.name;
        }

        public final Class<?> getLookupType() {
            return this.lookupType;
        }

        public final DependencyDescriptor getDependencyDescriptor() {
            if (this.isField) {
                return new LookupDependencyDescriptor((Field) this.member, this.lookupType);
            } else {
                return new LookupDependencyDescriptor((Method) this.member, this.lookupType);
            }
        }
    }

    private class ResourceElement extends LookupElement {

        private final boolean lazyLookup;

        public ResourceElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
            super(member, pd);
            Resource resource = ae.getAnnotation(Resource.class);
            String resourceName = resource.name();
            Class<?> resourceType = resource.type();
            this.isDefaultName = !StringUtils.hasLength(resourceName);
            if (this.isDefaultName) {
                resourceName = this.member.getName();
                if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
                    resourceName = Introspector.decapitalize(resourceName.substring(3));
                }
            } else if (embeddedValueResolver != null) {
                resourceName = embeddedValueResolver.resolveStringValue(resourceName);
            }
            if (Object.class != resourceType) {
                checkResourceType(resourceType);
            } else {
                resourceType = getResourceType();
            }
            this.name = (resourceName != null ? resourceName : "");
            this.lookupType = resourceType;
            String lookupValue = resource.lookup();
            this.mappedName = (StringUtils.hasLength(lookupValue) ? lookupValue : resource.mappedName());
            Lazy lazy = ae.getAnnotation(Lazy.class);
            this.lazyLookup = (lazy != null && lazy.value());
        }

        @Override
        protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
            return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName) : getResource(this, requestingBeanName));
        }
    }

    private class EjbRefElement extends LookupElement {

        private final String beanName;

        public EjbRefElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
            super(member, pd);
            EJB resource = ae.getAnnotation(EJB.class);
            String resourceBeanName = resource.beanName();
            String resourceName = resource.name();
            this.isDefaultName = !StringUtils.hasLength(resourceName);
            if (this.isDefaultName) {
                resourceName = this.member.getName();
                if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
                    resourceName = Introspector.decapitalize(resourceName.substring(3));
                }
            }
            Class<?> resourceType = resource.beanInterface();
            if (Object.class != resourceType) {
                checkResourceType(resourceType);
            } else {
                resourceType = getResourceType();
            }
            this.beanName = resourceBeanName;
            this.name = resourceName;
            this.lookupType = resourceType;
            this.mappedName = resource.mappedName();
        }

        @Override
        protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
            if (StringUtils.hasLength(this.beanName)) {
                if (beanFactory != null && beanFactory.containsBean(this.beanName)) {
                    Object bean = beanFactory.getBean(this.beanName, this.lookupType);
                    if (requestingBeanName != null && beanFactory instanceof ConfigurableBeanFactory) {
                        ((ConfigurableBeanFactory) beanFactory).registerDependentBean(this.beanName, requestingBeanName);
                    }
                    return bean;
                } else if (this.isDefaultName && !StringUtils.hasLength(this.mappedName)) {
                    throw new NoSuchBeanDefinitionException(this.beanName, "Cannot resolve 'beanName' in local BeanFactory. Consider specifying a general 'name' value instead.");
                }
            }
            return getResource(this, requestingBeanName);
        }
    }

    private static class LookupDependencyDescriptor extends DependencyDescriptor {

        private final Class<?> lookupType;

        public LookupDependencyDescriptor(Field field, Class<?> lookupType) {
            super(field, true);
            this.lookupType = lookupType;
        }

        public LookupDependencyDescriptor(Method method, Class<?> lookupType) {
            super(new MethodParameter(method, 0), true);
            this.lookupType = lookupType;
        }

        @Override
        public Class<?> getDependencyType() {
            return this.lookupType;
        }
    }
}
