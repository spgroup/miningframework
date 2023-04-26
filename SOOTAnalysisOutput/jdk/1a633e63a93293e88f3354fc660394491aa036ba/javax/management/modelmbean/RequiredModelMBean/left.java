package javax.management.modelmbean;

import static com.sun.jmx.defaults.JmxProperties.MODELMBEAN_LOGGER;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;
import javax.management.ServiceNotFoundException;
import javax.management.loading.ClassLoaderRepository;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;
import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

public class RequiredModelMBean implements ModelMBean, MBeanRegistration, NotificationEmitter {

    ModelMBeanInfo modelMBeanInfo;

    private NotificationBroadcasterSupport generalBroadcaster = null;

    private NotificationBroadcasterSupport attributeBroadcaster = null;

    private Object managedResource = null;

    private boolean registered = false;

    private transient MBeanServer server = null;

    private final static JavaSecurityAccess javaSecurityAccess = SharedSecrets.getJavaSecurityAccess();

    final private AccessControlContext acc = AccessController.getContext();

    public RequiredModelMBean() throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "RequiredModelMBean()", "Entry");
        }
        modelMBeanInfo = createDefaultModelMBeanInfo();
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "RequiredModelMBean()", "Exit");
        }
    }

    public RequiredModelMBean(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "RequiredModelMBean(MBeanInfo)", "Entry");
        }
        setModelMBeanInfo(mbi);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "RequiredModelMBean(MBeanInfo)", "Exit");
        }
    }

    public void setModelMBeanInfo(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "Entry");
        }
        if (mbi == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "ModelMBeanInfo is null: Raising exception.");
            }
            final RuntimeException x = new IllegalArgumentException("ModelMBeanInfo must not be null");
            final String exceptionText = "Exception occurred trying to initialize the " + "ModelMBeanInfo of the RequiredModelMBean";
            throw new RuntimeOperationsException(x, exceptionText);
        }
        if (registered) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "RequiredMBean is registered: Raising exception.");
            }
            final String exceptionText = "Exception occurred trying to set the " + "ModelMBeanInfo of the RequiredModelMBean";
            final RuntimeException x = new IllegalStateException("cannot call setModelMBeanInfo while ModelMBean is registered");
            throw new RuntimeOperationsException(x, exceptionText);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "Setting ModelMBeanInfo to " + printModelMBeanInfo(mbi));
            int noOfNotifications = 0;
            if (mbi.getNotifications() != null) {
                noOfNotifications = mbi.getNotifications().length;
            }
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "ModelMBeanInfo notifications has " + noOfNotifications + " elements");
        }
        modelMBeanInfo = (ModelMBeanInfo) mbi.clone();
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "set mbeanInfo to: " + printModelMBeanInfo(modelMBeanInfo));
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setModelMBeanInfo(ModelMBeanInfo)", "Exit");
        }
    }

    public void setManagedResource(Object mr, String mr_type) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setManagedResource(Object,String)", "Entry");
        }
        if ((mr_type == null) || (!mr_type.equalsIgnoreCase("objectReference"))) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setManagedResource(Object,String)", "Managed Resource Type is not supported: " + mr_type);
            }
            throw new InvalidTargetObjectTypeException(mr_type);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setManagedResource(Object,String)", "Managed Resource is valid");
        }
        managedResource = mr;
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setManagedResource(Object, String)", "Exit");
        }
    }

    public void load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
        final ServiceNotFoundException x = new ServiceNotFoundException("Persistence not supported for this MBean");
        throw new MBeanException(x, x.getMessage());
    }

    public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException {
        final ServiceNotFoundException x = new ServiceNotFoundException("Persistence not supported for this MBean");
        throw new MBeanException(x, x.getMessage());
    }

    private Object resolveForCacheValue(Descriptor descr) throws MBeanException, RuntimeOperationsException {
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        final String mth = "resolveForCacheValue(Descriptor)";
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        Object response = null;
        boolean resetValue = false, returnCachedValue = true;
        long currencyPeriod = 0;
        if (descr == null) {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Input Descriptor is null");
            }
            return response;
        }
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "descriptor is " + descr);
        }
        final Descriptor mmbDescr = modelMBeanInfo.getMBeanDescriptor();
        if (mmbDescr == null) {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "MBean Descriptor is null");
            }
        }
        Object objExpTime = descr.getFieldValue("currencyTimeLimit");
        String expTime;
        if (objExpTime != null) {
            expTime = objExpTime.toString();
        } else {
            expTime = null;
        }
        if ((expTime == null) && (mmbDescr != null)) {
            objExpTime = mmbDescr.getFieldValue("currencyTimeLimit");
            if (objExpTime != null) {
                expTime = objExpTime.toString();
            } else {
                expTime = null;
            }
        }
        if (expTime != null) {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "currencyTimeLimit: " + expTime);
            }
            currencyPeriod = ((new Long(expTime)).longValue()) * 1000;
            if (currencyPeriod < 0) {
                returnCachedValue = false;
                resetValue = true;
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, currencyPeriod + ": never Cached");
                }
            } else if (currencyPeriod == 0) {
                returnCachedValue = true;
                resetValue = false;
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "always valid Cache");
                }
            } else {
                Object objtStamp = descr.getFieldValue("lastUpdatedTimeStamp");
                String tStamp;
                if (objtStamp != null)
                    tStamp = objtStamp.toString();
                else
                    tStamp = null;
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "lastUpdatedTimeStamp: " + tStamp);
                }
                if (tStamp == null)
                    tStamp = "0";
                long lastTime = (new Long(tStamp)).longValue();
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "currencyPeriod:" + currencyPeriod + " lastUpdatedTimeStamp:" + lastTime);
                }
                long now = (new Date()).getTime();
                if (now < (lastTime + currencyPeriod)) {
                    returnCachedValue = true;
                    resetValue = false;
                    if (tracing) {
                        MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, " timed valid Cache for " + now + " < " + (lastTime + currencyPeriod));
                    }
                } else {
                    returnCachedValue = false;
                    resetValue = true;
                    if (tracing) {
                        MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "timed expired cache for " + now + " > " + (lastTime + currencyPeriod));
                    }
                }
            }
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "returnCachedValue:" + returnCachedValue + " resetValue: " + resetValue);
            }
            if (returnCachedValue == true) {
                Object currValue = descr.getFieldValue("value");
                if (currValue != null) {
                    response = currValue;
                    if (tracing) {
                        MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "valid Cache value: " + currValue);
                    }
                } else {
                    response = null;
                    if (tracing) {
                        MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "no Cached value");
                    }
                }
            }
            if (resetValue == true) {
                descr.removeField("lastUpdatedTimeStamp");
                descr.removeField("value");
                response = null;
                modelMBeanInfo.setDescriptor(descr, null);
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "reset cached value to null");
                }
            }
        }
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
        return response;
    }

    public MBeanInfo getMBeanInfo() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getMBeanInfo()", "Entry");
        }
        if (modelMBeanInfo == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getMBeanInfo()", "modelMBeanInfo is null");
            }
            modelMBeanInfo = createDefaultModelMBeanInfo();
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getMBeanInfo()", "ModelMBeanInfo is " + modelMBeanInfo.getClassName() + " for " + modelMBeanInfo.getDescription());
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getMBeanInfo()", printModelMBeanInfo(modelMBeanInfo));
        }
        return ((MBeanInfo) modelMBeanInfo.clone());
    }

    private String printModelMBeanInfo(ModelMBeanInfo info) {
        final StringBuilder retStr = new StringBuilder();
        if (info == null) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "printModelMBeanInfo(ModelMBeanInfo)", "ModelMBeanInfo to print is null, " + "printing local ModelMBeanInfo");
            }
            info = modelMBeanInfo;
        }
        retStr.append("\nMBeanInfo for ModelMBean is:");
        retStr.append("\nCLASSNAME: \t" + info.getClassName());
        retStr.append("\nDESCRIPTION: \t" + info.getDescription());
        try {
            retStr.append("\nMBEAN DESCRIPTOR: \t" + info.getMBeanDescriptor());
        } catch (Exception e) {
            retStr.append("\nMBEAN DESCRIPTOR: \t" + " is invalid");
        }
        retStr.append("\nATTRIBUTES");
        final MBeanAttributeInfo[] attrInfo = info.getAttributes();
        if ((attrInfo != null) && (attrInfo.length > 0)) {
            for (int i = 0; i < attrInfo.length; i++) {
                final ModelMBeanAttributeInfo attInfo = (ModelMBeanAttributeInfo) attrInfo[i];
                retStr.append(" ** NAME: \t" + attInfo.getName());
                retStr.append("    DESCR: \t" + attInfo.getDescription());
                retStr.append("    TYPE: \t" + attInfo.getType() + "    READ: \t" + attInfo.isReadable() + "    WRITE: \t" + attInfo.isWritable());
                retStr.append("    DESCRIPTOR: " + attInfo.getDescriptor().toString());
            }
        } else {
            retStr.append(" ** No attributes **");
        }
        retStr.append("\nCONSTRUCTORS");
        final MBeanConstructorInfo[] constrInfo = info.getConstructors();
        if ((constrInfo != null) && (constrInfo.length > 0)) {
            for (int i = 0; i < constrInfo.length; i++) {
                final ModelMBeanConstructorInfo ctorInfo = (ModelMBeanConstructorInfo) constrInfo[i];
                retStr.append(" ** NAME: \t" + ctorInfo.getName());
                retStr.append("    DESCR: \t" + ctorInfo.getDescription());
                retStr.append("    PARAM: \t" + ctorInfo.getSignature().length + " parameter(s)");
                retStr.append("    DESCRIPTOR: " + ctorInfo.getDescriptor().toString());
            }
        } else {
            retStr.append(" ** No Constructors **");
        }
        retStr.append("\nOPERATIONS");
        final MBeanOperationInfo[] opsInfo = info.getOperations();
        if ((opsInfo != null) && (opsInfo.length > 0)) {
            for (int i = 0; i < opsInfo.length; i++) {
                final ModelMBeanOperationInfo operInfo = (ModelMBeanOperationInfo) opsInfo[i];
                retStr.append(" ** NAME: \t" + operInfo.getName());
                retStr.append("    DESCR: \t" + operInfo.getDescription());
                retStr.append("    PARAM: \t" + operInfo.getSignature().length + " parameter(s)");
                retStr.append("    DESCRIPTOR: " + operInfo.getDescriptor().toString());
            }
        } else {
            retStr.append(" ** No operations ** ");
        }
        retStr.append("\nNOTIFICATIONS");
        MBeanNotificationInfo[] notifInfo = info.getNotifications();
        if ((notifInfo != null) && (notifInfo.length > 0)) {
            for (int i = 0; i < notifInfo.length; i++) {
                final ModelMBeanNotificationInfo nInfo = (ModelMBeanNotificationInfo) notifInfo[i];
                retStr.append(" ** NAME: \t" + nInfo.getName());
                retStr.append("    DESCR: \t" + nInfo.getDescription());
                retStr.append("    DESCRIPTOR: " + nInfo.getDescriptor().toString());
            }
        } else {
            retStr.append(" ** No notifications **");
        }
        retStr.append(" ** ModelMBean: End of MBeanInfo ** ");
        return retStr.toString();
    }

    public Object invoke(String opName, Object[] opArgs, String[] sig) throws MBeanException, ReflectionException {
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        final String mth = "invoke(String, Object[], String[])";
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (opName == null) {
            final RuntimeException x = new IllegalArgumentException("Method name must not be null");
            throw new RuntimeOperationsException(x, "An exception occurred while trying to " + "invoke a method on a RequiredModelMBean");
        }
        String opClassName = null;
        String opMethodName;
        int opSplitter = opName.lastIndexOf(".");
        if (opSplitter > 0) {
            opClassName = opName.substring(0, opSplitter);
            opMethodName = opName.substring(opSplitter + 1);
        } else
            opMethodName = opName;
        opSplitter = opMethodName.indexOf("(");
        if (opSplitter > 0)
            opMethodName = opMethodName.substring(0, opSplitter);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Finding operation " + opName + " as " + opMethodName);
        }
        ModelMBeanOperationInfo opInfo = modelMBeanInfo.getOperation(opMethodName);
        if (opInfo == null) {
            final String msg = "Operation " + opName + " not in ModelMBeanInfo";
            throw new MBeanException(new ServiceNotFoundException(msg), msg);
        }
        final Descriptor opDescr = opInfo.getDescriptor();
        if (opDescr == null) {
            final String msg = "Operation descriptor null";
            throw new MBeanException(new ServiceNotFoundException(msg), msg);
        }
        final Object cached = resolveForCacheValue(opDescr);
        if (cached != null) {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Returning cached value");
            }
            return cached;
        }
        if (opClassName == null)
            opClassName = (String) opDescr.getFieldValue("class");
        opMethodName = (String) opDescr.getFieldValue("name");
        if (opMethodName == null) {
            final String msg = "Method descriptor must include `name' field";
            throw new MBeanException(new ServiceNotFoundException(msg), msg);
        }
        final String targetTypeField = (String) opDescr.getFieldValue("targetType");
        if (targetTypeField != null && !targetTypeField.equalsIgnoreCase("objectReference")) {
            final String msg = "Target type must be objectReference: " + targetTypeField;
            throw new MBeanException(new InvalidTargetObjectTypeException(msg), msg);
        }
        final Object targetObjectField = opDescr.getFieldValue("targetObject");
        if (tracing && targetObjectField != null)
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Found target object in descriptor");
        Method method;
        Object targetObject;
        method = findRMMBMethod(opMethodName, targetObjectField, opClassName, sig);
        if (method != null)
            targetObject = this;
        else {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "looking for method in managedResource class");
            }
            if (targetObjectField != null)
                targetObject = targetObjectField;
            else {
                targetObject = managedResource;
                if (targetObject == null) {
                    final String msg = "managedResource for invoke " + opName + " is null";
                    Exception snfe = new ServiceNotFoundException(msg);
                    throw new MBeanException(snfe);
                }
            }
            final Class<?> targetClass;
            if (opClassName != null) {
                try {
                    AccessControlContext stack = AccessController.getContext();
                    final Object obj = targetObject;
                    final String className = opClassName;
                    final ClassNotFoundException[] caughtException = new ClassNotFoundException[1];
                    targetClass = javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Class<?>>() {

                        @Override
                        public Class<?> run() {
                            try {
                                ReflectUtil.checkPackageAccess(className);
                                final ClassLoader targetClassLoader = obj.getClass().getClassLoader();
                                return Class.forName(className, false, targetClassLoader);
                            } catch (ClassNotFoundException e) {
                                caughtException[0] = e;
                            }
                            return null;
                        }
                    }, stack, acc);
                    if (caughtException[0] != null) {
                        throw caughtException[0];
                    }
                } catch (ClassNotFoundException e) {
                    final String msg = "class for invoke " + opName + " not found";
                    throw new ReflectionException(e, msg);
                }
            } else
                targetClass = targetObject.getClass();
            method = resolveMethod(targetClass, opMethodName, sig);
        }
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "found " + opMethodName + ", now invoking");
        }
        final Object result = invokeMethod(opName, method, targetObject, opArgs);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "successfully invoked method");
        }
        if (result != null)
            cacheResult(opInfo, opDescr, result);
        return result;
    }

    private Method resolveMethod(Class<?> targetClass, String opMethodName, final String[] sig) throws ReflectionException {
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "resolveMethod", "resolving " + targetClass.getName() + "." + opMethodName);
        }
        final Class<?>[] argClasses;
        if (sig == null)
            argClasses = null;
        else {
            final AccessControlContext stack = AccessController.getContext();
            final ReflectionException[] caughtException = new ReflectionException[1];
            final ClassLoader targetClassLoader = targetClass.getClassLoader();
            argClasses = new Class<?>[sig.length];
            javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    for (int i = 0; i < sig.length; i++) {
                        if (tracing) {
                            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "resolveMethod", "resolve type " + sig[i]);
                        }
                        argClasses[i] = (Class<?>) primitiveClassMap.get(sig[i]);
                        if (argClasses[i] == null) {
                            try {
                                ReflectUtil.checkPackageAccess(sig[i]);
                                argClasses[i] = Class.forName(sig[i], false, targetClassLoader);
                            } catch (ClassNotFoundException e) {
                                if (tracing) {
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "resolveMethod", "class not found");
                                }
                                final String msg = "Parameter class not found";
                                caughtException[0] = new ReflectionException(e, msg);
                            }
                        }
                    }
                    return null;
                }
            }, stack, acc);
            if (caughtException[0] != null) {
                throw caughtException[0];
            }
        }
        try {
            return targetClass.getMethod(opMethodName, argClasses);
        } catch (NoSuchMethodException e) {
            final String msg = "Target method not found: " + targetClass.getName() + "." + opMethodName;
            throw new ReflectionException(e, msg);
        }
    }

    private static final Class<?>[] primitiveClasses = { int.class, long.class, boolean.class, double.class, float.class, short.class, byte.class, char.class };

    private static final Map<String, Class<?>> primitiveClassMap = new HashMap<String, Class<?>>();

    static {
        for (int i = 0; i < primitiveClasses.length; i++) {
            final Class<?> c = primitiveClasses[i];
            primitiveClassMap.put(c.getName(), c);
        }
    }

    private Method findRMMBMethod(String opMethodName, Object targetObjectField, String opClassName, String[] sig) {
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "invoke(String, Object[], String[])", "looking for method in RequiredModelMBean class");
        }
        if (!isRMMBMethodName(opMethodName))
            return null;
        if (targetObjectField != null)
            return null;
        final Class<RequiredModelMBean> rmmbClass = RequiredModelMBean.class;
        final Class<?> targetClass;
        if (opClassName == null)
            targetClass = rmmbClass;
        else {
            AccessControlContext stack = AccessController.getContext();
            final String className = opClassName;
            targetClass = javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Class<?>>() {

                @Override
                public Class<?> run() {
                    try {
                        ReflectUtil.checkPackageAccess(className);
                        final ClassLoader targetClassLoader = rmmbClass.getClassLoader();
                        Class clz = Class.forName(className, false, targetClassLoader);
                        if (!rmmbClass.isAssignableFrom(clz))
                            return null;
                        return clz;
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                }
            }, stack, acc);
        }
        try {
            return targetClass != null ? resolveMethod(targetClass, opMethodName, sig) : null;
        } catch (ReflectionException e) {
            return null;
        }
    }

    private Object invokeMethod(String opName, final Method method, final Object targetObject, final Object[] opArgs) throws MBeanException, ReflectionException {
        try {
            final Throwable[] caughtException = new Throwable[1];
            AccessControlContext stack = AccessController.getContext();
            Object rslt = javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Object>() {

                @Override
                public Object run() {
                    try {
                        ReflectUtil.checkPackageAccess(method.getDeclaringClass());
                        return MethodUtil.invoke(method, targetObject, opArgs);
                    } catch (InvocationTargetException e) {
                        caughtException[0] = e;
                    } catch (IllegalAccessException e) {
                        caughtException[0] = e;
                    }
                    return null;
                }
            }, stack, acc);
            if (caughtException[0] != null) {
                if (caughtException[0] instanceof Exception) {
                    throw (Exception) caughtException[0];
                } else if (caughtException[0] instanceof Error) {
                    throw (Error) caughtException[0];
                }
            }
            return rslt;
        } catch (RuntimeErrorException ree) {
            throw new RuntimeOperationsException(ree, "RuntimeException occurred in RequiredModelMBean " + "while trying to invoke operation " + opName);
        } catch (RuntimeException re) {
            throw new RuntimeOperationsException(re, "RuntimeException occurred in RequiredModelMBean " + "while trying to invoke operation " + opName);
        } catch (IllegalAccessException iae) {
            throw new ReflectionException(iae, "IllegalAccessException occurred in " + "RequiredModelMBean while trying to " + "invoke operation " + opName);
        } catch (InvocationTargetException ite) {
            Throwable mmbTargEx = ite.getTargetException();
            if (mmbTargEx instanceof RuntimeException) {
                throw new MBeanException((RuntimeException) mmbTargEx, "RuntimeException thrown in RequiredModelMBean " + "while trying to invoke operation " + opName);
            } else if (mmbTargEx instanceof Error) {
                throw new RuntimeErrorException((Error) mmbTargEx, "Error occurred in RequiredModelMBean while trying " + "to invoke operation " + opName);
            } else if (mmbTargEx instanceof ReflectionException) {
                throw (ReflectionException) mmbTargEx;
            } else {
                throw new MBeanException((Exception) mmbTargEx, "Exception thrown in RequiredModelMBean " + "while trying to invoke operation " + opName);
            }
        } catch (Error err) {
            throw new RuntimeErrorException(err, "Error occurred in RequiredModelMBean while trying " + "to invoke operation " + opName);
        } catch (Exception e) {
            throw new ReflectionException(e, "Exception occurred in RequiredModelMBean while " + "trying to invoke operation " + opName);
        }
    }

    private void cacheResult(ModelMBeanOperationInfo opInfo, Descriptor opDescr, Object result) throws MBeanException {
        Descriptor mmbDesc = modelMBeanInfo.getMBeanDescriptor();
        Object objctl = opDescr.getFieldValue("currencyTimeLimit");
        String ctl;
        if (objctl != null) {
            ctl = objctl.toString();
        } else {
            ctl = null;
        }
        if ((ctl == null) && (mmbDesc != null)) {
            objctl = mmbDesc.getFieldValue("currencyTimeLimit");
            if (objctl != null) {
                ctl = objctl.toString();
            } else {
                ctl = null;
            }
        }
        if ((ctl != null) && !(ctl.equals("-1"))) {
            opDescr.setField("value", result);
            opDescr.setField("lastUpdatedTimeStamp", String.valueOf((new Date()).getTime()));
            modelMBeanInfo.setDescriptor(opDescr, "operation");
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "invoke(String,Object[],Object[])", "new descriptor is " + opDescr);
            }
        }
    }

    private static Set<String> rmmbMethodNames;

    private static synchronized boolean isRMMBMethodName(String name) {
        if (rmmbMethodNames == null) {
            try {
                Set<String> names = new HashSet<String>();
                Method[] methods = RequiredModelMBean.class.getMethods();
                for (int i = 0; i < methods.length; i++) names.add(methods[i].getName());
                rmmbMethodNames = names;
            } catch (Exception e) {
                return true;
            }
        }
        return rmmbMethodNames.contains(name);
    }

    public Object getAttribute(String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attrName == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeName must not be null"), "Exception occurred trying to get attribute of a " + "RequiredModelMBean");
        final String mth = "getAttribute(String)";
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry with " + attrName);
        }
        Object response;
        try {
            if (modelMBeanInfo == null)
                throw new AttributeNotFoundException("getAttribute failed: ModelMBeanInfo not found for " + attrName);
            ModelMBeanAttributeInfo attrInfo = modelMBeanInfo.getAttribute(attrName);
            Descriptor mmbDesc = modelMBeanInfo.getMBeanDescriptor();
            if (attrInfo == null)
                throw new AttributeNotFoundException("getAttribute failed:" + " ModelMBeanAttributeInfo not found for " + attrName);
            Descriptor attrDescr = attrInfo.getDescriptor();
            if (attrDescr != null) {
                if (!attrInfo.isReadable())
                    throw new AttributeNotFoundException("getAttribute failed: " + attrName + " is not readable ");
                response = resolveForCacheValue(attrDescr);
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "*** cached value is " + response);
                }
                if (response == null) {
                    if (tracing) {
                        MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "**** cached value is null - getting getMethod");
                    }
                    String attrGetMethod = (String) (attrDescr.getFieldValue("getMethod"));
                    if (attrGetMethod != null) {
                        if (tracing) {
                            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "invoking a getMethod for " + attrName);
                        }
                        Object getResponse = invoke(attrGetMethod, new Object[] {}, new String[] {});
                        if (getResponse != null) {
                            if (tracing) {
                                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "got a non-null response " + "from getMethod\n");
                            }
                            response = getResponse;
                            Object objctl = attrDescr.getFieldValue("currencyTimeLimit");
                            String ctl;
                            if (objctl != null)
                                ctl = objctl.toString();
                            else
                                ctl = null;
                            if ((ctl == null) && (mmbDesc != null)) {
                                objctl = mmbDesc.getFieldValue("currencyTimeLimit");
                                if (objctl != null)
                                    ctl = objctl.toString();
                                else
                                    ctl = null;
                            }
                            if ((ctl != null) && !(ctl.equals("-1"))) {
                                if (tracing) {
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "setting cached value and " + "lastUpdatedTime in descriptor");
                                }
                                attrDescr.setField("value", response);
                                final String stamp = String.valueOf((new Date()).getTime());
                                attrDescr.setField("lastUpdatedTimeStamp", stamp);
                                attrInfo.setDescriptor(attrDescr);
                                modelMBeanInfo.setDescriptor(attrDescr, "attribute");
                                if (tracing) {
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "new descriptor is " + attrDescr);
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "AttributeInfo descriptor is " + attrInfo.getDescriptor());
                                    final String attStr = modelMBeanInfo.getDescriptor(attrName, "attribute").toString();
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "modelMBeanInfo: AttributeInfo " + "descriptor is " + attStr);
                                }
                            }
                        } else {
                            if (tracing) {
                                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "got a null response from getMethod\n");
                            }
                            response = null;
                        }
                    } else {
                        String qualifier = "";
                        response = attrDescr.getFieldValue("value");
                        if (response == null) {
                            qualifier = "default ";
                            response = attrDescr.getFieldValue("default");
                        }
                        if (tracing) {
                            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "could not find getMethod for " + attrName + ", returning descriptor " + qualifier + "value");
                        }
                    }
                }
                final String respType = attrInfo.getType();
                if (response != null) {
                    String responseClass = response.getClass().getName();
                    if (!respType.equals(responseClass)) {
                        boolean wrongType = false;
                        boolean primitiveType = false;
                        boolean correspondingTypes = false;
                        for (int i = 0; i < primitiveTypes.length; i++) {
                            if (respType.equals(primitiveTypes[i])) {
                                primitiveType = true;
                                if (responseClass.equals(primitiveWrappers[i]))
                                    correspondingTypes = true;
                                break;
                            }
                        }
                        if (primitiveType) {
                            if (!correspondingTypes)
                                wrongType = true;
                        } else {
                            boolean subtype;
                            try {
                                final Class respClass = response.getClass();
                                final Exception[] caughException = new Exception[1];
                                AccessControlContext stack = AccessController.getContext();
                                Class c = javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Class<?>>() {

                                    @Override
                                    public Class<?> run() {
                                        try {
                                            ReflectUtil.checkPackageAccess(respType);
                                            ClassLoader cl = respClass.getClassLoader();
                                            return Class.forName(respType, true, cl);
                                        } catch (Exception e) {
                                            caughException[0] = e;
                                        }
                                        return null;
                                    }
                                }, stack, acc);
                                if (caughException[0] != null) {
                                    throw caughException[0];
                                }
                                subtype = c.isInstance(response);
                            } catch (Exception e) {
                                subtype = false;
                                if (tracing) {
                                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exception: ", e);
                                }
                            }
                            if (!subtype)
                                wrongType = true;
                        }
                        if (wrongType) {
                            if (tracing) {
                                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Wrong response type '" + respType + "'");
                            }
                            throw new MBeanException(new InvalidAttributeValueException("Wrong value type received for get attribute"), "An exception occurred while trying to get an " + "attribute value through a RequiredModelMBean");
                        }
                    }
                }
            } else {
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "getMethod failed " + attrName + " not in attributeDescriptor\n");
                }
                throw new MBeanException(new InvalidAttributeValueException("Unable to resolve attribute value, " + "no getMethod defined in descriptor for attribute"), "An exception occurred while trying to get an " + "attribute value through a RequiredModelMBean");
            }
        } catch (MBeanException mbe) {
            throw mbe;
        } catch (AttributeNotFoundException t) {
            throw t;
        } catch (Exception e) {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "getMethod failed with " + e.getMessage() + " exception type " + (e.getClass()).toString());
            }
            throw new MBeanException(e, "An exception occurred while trying " + "to get an attribute value: " + e.getMessage());
        }
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
        return response;
    }

    public AttributeList getAttributes(String[] attrNames) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getAttributes(String[])", "Entry");
        }
        if (attrNames == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames must not be null"), "Exception occurred trying to get attributes of a " + "RequiredModelMBean");
        AttributeList responseList = new AttributeList();
        for (int i = 0; i < attrNames.length; i++) {
            try {
                responseList.add(new Attribute(attrNames[i], getAttribute(attrNames[i])));
            } catch (Exception e) {
                if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getAttributes(String[])", "Failed to get \"" + attrNames[i] + "\": ", e);
                }
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getAttributes(String[])", "Exit");
        }
        return responseList;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        final boolean tracing = MODELMBEAN_LOGGER.isLoggable(Level.FINER);
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute()", "Entry");
        }
        if (attribute == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("attribute must not be null"), "Exception occurred trying to set an attribute of a " + "RequiredModelMBean");
        String attrName = attribute.getName();
        Object attrValue = attribute.getValue();
        boolean updateDescriptor = false;
        ModelMBeanAttributeInfo attrInfo = modelMBeanInfo.getAttribute(attrName);
        if (attrInfo == null)
            throw new AttributeNotFoundException("setAttribute failed: " + attrName + " is not found ");
        Descriptor mmbDesc = modelMBeanInfo.getMBeanDescriptor();
        Descriptor attrDescr = attrInfo.getDescriptor();
        if (attrDescr != null) {
            if (!attrInfo.isWritable())
                throw new AttributeNotFoundException("setAttribute failed: " + attrName + " is not writable ");
            String attrSetMethod = (String) (attrDescr.getFieldValue("setMethod"));
            String attrGetMethod = (String) (attrDescr.getFieldValue("getMethod"));
            String attrType = attrInfo.getType();
            Object currValue = "Unknown";
            try {
                currValue = this.getAttribute(attrName);
            } catch (Throwable t) {
            }
            Attribute oldAttr = new Attribute(attrName, currValue);
            if (attrSetMethod == null) {
                if (attrValue != null) {
                    try {
                        final Class<?> clazz = loadClass(attrType);
                        if (!clazz.isInstance(attrValue))
                            throw new InvalidAttributeValueException(clazz.getName() + " expected, " + attrValue.getClass().getName() + " received.");
                    } catch (ClassNotFoundException x) {
                        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "Class " + attrType + " for attribute " + attrName + " not found: ", x);
                        }
                    }
                }
                updateDescriptor = true;
            } else {
                invoke(attrSetMethod, (new Object[] { attrValue }), (new String[] { attrType }));
            }
            Object objctl = attrDescr.getFieldValue("currencyTimeLimit");
            String ctl;
            if (objctl != null)
                ctl = objctl.toString();
            else
                ctl = null;
            if ((ctl == null) && (mmbDesc != null)) {
                objctl = mmbDesc.getFieldValue("currencyTimeLimit");
                if (objctl != null)
                    ctl = objctl.toString();
                else
                    ctl = null;
            }
            final boolean updateCache = ((ctl != null) && !(ctl.equals("-1")));
            if (attrSetMethod == null && !updateCache && attrGetMethod != null)
                throw new MBeanException(new ServiceNotFoundException("No " + "setMethod field is defined in the descriptor for " + attrName + " attribute and caching is not enabled " + "for it"));
            if (updateCache || updateDescriptor) {
                if (tracing) {
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "setting cached value of " + attrName + " to " + attrValue);
                }
                attrDescr.setField("value", attrValue);
                if (updateCache) {
                    final String currtime = String.valueOf((new Date()).getTime());
                    attrDescr.setField("lastUpdatedTimeStamp", currtime);
                }
                attrInfo.setDescriptor(attrDescr);
                modelMBeanInfo.setDescriptor(attrDescr, "attribute");
                if (tracing) {
                    final StringBuilder strb = new StringBuilder().append("new descriptor is ").append(attrDescr).append(". AttributeInfo descriptor is ").append(attrInfo.getDescriptor()).append(". AttributeInfo descriptor is ").append(modelMBeanInfo.getDescriptor(attrName, "attribute"));
                    MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", strb.toString());
                }
            }
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "sending sendAttributeNotification");
            }
            sendAttributeChangeNotification(oldAttr, attribute);
        } else {
            if (tracing) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "setMethod failed " + attrName + " not in attributeDescriptor\n");
            }
            throw new InvalidAttributeValueException("Unable to resolve attribute value, " + "no defined in descriptor for attribute");
        }
        if (tracing) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "Exit");
        }
    }

    public AttributeList setAttributes(AttributeList attributes) {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "setAttribute(Attribute)", "Entry");
        }
        if (attributes == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("attributes must not be null"), "Exception occurred trying to set attributes of a " + "RequiredModelMBean");
        final AttributeList responseList = new AttributeList();
        for (Attribute attr : attributes.asList()) {
            try {
                setAttribute(attr);
                responseList.add(attr);
            } catch (Exception excep) {
                responseList.remove(attr);
            }
        }
        return responseList;
    }

    private ModelMBeanInfo createDefaultModelMBeanInfo() {
        return (new ModelMBeanInfoSupport((this.getClass().getName()), "Default ModelMBean", null, null, null, null));
    }

    private synchronized void writeToLog(String logFileName, String logEntry) throws Exception {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "writeToLog(String, String)", "Notification Logging to " + logFileName + ": " + logEntry);
        }
        if ((logFileName == null) || (logEntry == null)) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "writeToLog(String, String)", "Bad input parameters, will not log this entry.");
            }
            return;
        }
        FileOutputStream fos = new FileOutputStream(logFileName, true);
        try {
            PrintStream logOut = new PrintStream(fos);
            logOut.println(logEntry);
            logOut.close();
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "writeToLog(String, String)", "Successfully opened log " + logFileName);
            }
        } catch (Exception e) {
            if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
                MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "writeToLog(String, String)", "Exception " + e.toString() + " trying to write to the Notification log file " + logFileName);
            }
            throw e;
        } finally {
            fos.close();
        }
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws java.lang.IllegalArgumentException {
        final String mth = "addNotificationListener(" + "NotificationListener, NotificationFilter, Object)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (listener == null)
            throw new IllegalArgumentException("notification listener must not be null");
        if (generalBroadcaster == null)
            generalBroadcaster = new NotificationBroadcasterSupport();
        generalBroadcaster.addNotificationListener(listener, filter, handback);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "NotificationListener added");
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        if (listener == null)
            throw new ListenerNotFoundException("Notification listener is null");
        final String mth = "removeNotificationListener(NotificationListener)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (generalBroadcaster == null)
            throw new ListenerNotFoundException("No notification listeners registered");
        generalBroadcaster.removeNotificationListener(listener);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        if (listener == null)
            throw new ListenerNotFoundException("Notification listener is null");
        final String mth = "removeNotificationListener(" + "NotificationListener, NotificationFilter, Object)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (generalBroadcaster == null)
            throw new ListenerNotFoundException("No notification listeners registered");
        generalBroadcaster.removeNotificationListener(listener, filter, handback);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void sendNotification(Notification ntfyObj) throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(Notification)", "Entry");
        }
        if (ntfyObj == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("notification object must not be " + "null"), "Exception occurred trying to send a notification from a " + "RequiredModelMBean");
        Descriptor ntfyDesc = modelMBeanInfo.getDescriptor(ntfyObj.getType(), "notification");
        Descriptor mmbDesc = modelMBeanInfo.getMBeanDescriptor();
        if (ntfyDesc != null) {
            String logging = (String) ntfyDesc.getFieldValue("log");
            if (logging == null) {
                if (mmbDesc != null)
                    logging = (String) mmbDesc.getFieldValue("log");
            }
            if ((logging != null) && (logging.equalsIgnoreCase("t") || logging.equalsIgnoreCase("true"))) {
                String logfile = (String) ntfyDesc.getFieldValue("logfile");
                if (logfile == null) {
                    if (mmbDesc != null)
                        logfile = (String) mmbDesc.getFieldValue("logfile");
                }
                if (logfile != null) {
                    try {
                        writeToLog(logfile, "LogMsg: " + ((new Date(ntfyObj.getTimeStamp())).toString()) + " " + ntfyObj.getType() + " " + ntfyObj.getMessage() + " Severity = " + (String) ntfyDesc.getFieldValue("severity"));
                    } catch (Exception e) {
                        if (MODELMBEAN_LOGGER.isLoggable(Level.FINE)) {
                            MODELMBEAN_LOGGER.logp(Level.FINE, RequiredModelMBean.class.getName(), "sendNotification(Notification)", "Failed to log " + ntfyObj.getType() + " notification: ", e);
                        }
                    }
                }
            }
        }
        if (generalBroadcaster != null) {
            generalBroadcaster.sendNotification(ntfyObj);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(Notification)", "sendNotification sent provided notification object");
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(Notification)", " Exit");
        }
    }

    public void sendNotification(String ntfyText) throws MBeanException, RuntimeOperationsException {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(String)", "Entry");
        }
        if (ntfyText == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("notification message must not " + "be null"), "Exception occurred trying to send a text notification " + "from a ModelMBean");
        Notification myNtfyObj = new Notification("jmx.modelmbean.generic", this, 1, ntfyText);
        sendNotification(myNtfyObj);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(String)", "Notification sent");
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "sendNotification(String)", "Exit");
        }
    }

    private static final boolean hasNotification(final ModelMBeanInfo info, final String notifName) {
        try {
            if (info == null)
                return false;
            else
                return (info.getNotification(notifName) != null);
        } catch (MBeanException x) {
            return false;
        } catch (RuntimeOperationsException r) {
            return false;
        }
    }

    private static final ModelMBeanNotificationInfo makeGenericInfo() {
        final Descriptor genericDescriptor = new DescriptorSupport(new String[] { "name=GENERIC", "descriptorType=notification", "log=T", "severity=6", "displayName=jmx.modelmbean.generic" });
        return new ModelMBeanNotificationInfo(new String[] { "jmx.modelmbean.generic" }, "GENERIC", "A text notification has been issued by the managed resource", genericDescriptor);
    }

    private static final ModelMBeanNotificationInfo makeAttributeChangeInfo() {
        final Descriptor attributeDescriptor = new DescriptorSupport(new String[] { "name=ATTRIBUTE_CHANGE", "descriptorType=notification", "log=T", "severity=6", "displayName=jmx.attribute.change" });
        return new ModelMBeanNotificationInfo(new String[] { "jmx.attribute.change" }, "ATTRIBUTE_CHANGE", "Signifies that an observed MBean attribute value has changed", attributeDescriptor);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getNotificationInfo()", "Entry");
        }
        final boolean hasGeneric = hasNotification(modelMBeanInfo, "GENERIC");
        final boolean hasAttributeChange = hasNotification(modelMBeanInfo, "ATTRIBUTE_CHANGE");
        final ModelMBeanNotificationInfo[] currInfo = (ModelMBeanNotificationInfo[]) modelMBeanInfo.getNotifications();
        final int len = ((currInfo == null ? 0 : currInfo.length) + (hasGeneric ? 0 : 1) + (hasAttributeChange ? 0 : 1));
        final ModelMBeanNotificationInfo[] respInfo = new ModelMBeanNotificationInfo[len];
        int inserted = 0;
        if (!hasGeneric)
            respInfo[inserted++] = makeGenericInfo();
        if (!hasAttributeChange)
            respInfo[inserted++] = makeAttributeChangeInfo();
        final int count = currInfo.length;
        final int offset = inserted;
        for (int j = 0; j < count; j++) {
            respInfo[offset + j] = currInfo[j];
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), "getNotificationInfo()", "Exit");
        }
        return respInfo;
    }

    public void addAttributeChangeNotificationListener(NotificationListener inlistener, String inAttributeName, Object inhandback) throws MBeanException, RuntimeOperationsException, IllegalArgumentException {
        final String mth = "addAttributeChangeNotificationListener(" + "NotificationListener, String, Object)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (inlistener == null)
            throw new IllegalArgumentException("Listener to be registered must not be null");
        if (attributeBroadcaster == null)
            attributeBroadcaster = new NotificationBroadcasterSupport();
        AttributeChangeNotificationFilter currFilter = new AttributeChangeNotificationFilter();
        MBeanAttributeInfo[] attrInfo = modelMBeanInfo.getAttributes();
        boolean found = false;
        if (inAttributeName == null) {
            if ((attrInfo != null) && (attrInfo.length > 0)) {
                for (int i = 0; i < attrInfo.length; i++) {
                    currFilter.enableAttribute(attrInfo[i].getName());
                }
            }
        } else {
            if ((attrInfo != null) && (attrInfo.length > 0)) {
                for (int i = 0; i < attrInfo.length; i++) {
                    if (inAttributeName.equals(attrInfo[i].getName())) {
                        found = true;
                        currFilter.enableAttribute(inAttributeName);
                        break;
                    }
                }
            }
            if (!found) {
                throw new RuntimeOperationsException(new IllegalArgumentException("The attribute name does not exist"), "Exception occurred trying to add an " + "AttributeChangeNotification listener");
            }
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            Vector<String> enabledAttrs = currFilter.getEnabledAttributes();
            String s = (enabledAttrs.size() > 1) ? "[" + enabledAttrs.firstElement() + ", ...]" : enabledAttrs.toString();
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Set attribute change filter to " + s);
        }
        attributeBroadcaster.addNotificationListener(inlistener, currFilter, inhandback);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Notification listener added for " + inAttributeName);
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void removeAttributeChangeNotificationListener(NotificationListener inlistener, String inAttributeName) throws MBeanException, RuntimeOperationsException, ListenerNotFoundException {
        if (inlistener == null)
            throw new ListenerNotFoundException("Notification listener is null");
        final String mth = "removeAttributeChangeNotificationListener(" + "NotificationListener, String)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (attributeBroadcaster == null)
            throw new ListenerNotFoundException("No attribute change notification listeners registered");
        MBeanAttributeInfo[] attrInfo = modelMBeanInfo.getAttributes();
        boolean found = false;
        if ((attrInfo != null) && (attrInfo.length > 0)) {
            for (int i = 0; i < attrInfo.length; i++) {
                if (attrInfo[i].getName().equals(inAttributeName)) {
                    found = true;
                    break;
                }
            }
        }
        if ((!found) && (inAttributeName != null)) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Invalid attribute name"), "Exception occurred trying to remove " + "attribute change notification listener");
        }
        attributeBroadcaster.removeNotificationListener(inlistener);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void sendAttributeChangeNotification(AttributeChangeNotification ntfyObj) throws MBeanException, RuntimeOperationsException {
        final String mth = "sendAttributeChangeNotification(" + "AttributeChangeNotification)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if (ntfyObj == null)
            throw new RuntimeOperationsException(new IllegalArgumentException("attribute change notification object must not be null"), "Exception occurred trying to send " + "attribute change notification of a ModelMBean");
        Object oldv = ntfyObj.getOldValue();
        Object newv = ntfyObj.getNewValue();
        if (oldv == null)
            oldv = "null";
        if (newv == null)
            newv = "null";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Sending AttributeChangeNotification with " + ntfyObj.getAttributeName() + ntfyObj.getAttributeType() + ntfyObj.getNewValue() + ntfyObj.getOldValue());
        }
        Descriptor ntfyDesc = modelMBeanInfo.getDescriptor(ntfyObj.getType(), "notification");
        Descriptor mmbDesc = modelMBeanInfo.getMBeanDescriptor();
        String logging, logfile;
        if (ntfyDesc != null) {
            logging = (String) ntfyDesc.getFieldValue("log");
            if (logging == null) {
                if (mmbDesc != null)
                    logging = (String) mmbDesc.getFieldValue("log");
            }
            if ((logging != null) && (logging.equalsIgnoreCase("t") || logging.equalsIgnoreCase("true"))) {
                logfile = (String) ntfyDesc.getFieldValue("logfile");
                if (logfile == null) {
                    if (mmbDesc != null)
                        logfile = (String) mmbDesc.getFieldValue("logfile");
                }
                if (logfile != null) {
                    try {
                        writeToLog(logfile, "LogMsg: " + ((new Date(ntfyObj.getTimeStamp())).toString()) + " " + ntfyObj.getType() + " " + ntfyObj.getMessage() + " Name = " + ntfyObj.getAttributeName() + " Old value = " + oldv + " New value = " + newv);
                    } catch (Exception e) {
                        if (MODELMBEAN_LOGGER.isLoggable(Level.FINE)) {
                            MODELMBEAN_LOGGER.logp(Level.FINE, RequiredModelMBean.class.getName(), mth, "Failed to log " + ntfyObj.getType() + " notification: ", e);
                        }
                    }
                }
            }
        } else if (mmbDesc != null) {
            logging = (String) mmbDesc.getFieldValue("log");
            if ((logging != null) && (logging.equalsIgnoreCase("t") || logging.equalsIgnoreCase("true"))) {
                logfile = (String) mmbDesc.getFieldValue("logfile");
                if (logfile != null) {
                    try {
                        writeToLog(logfile, "LogMsg: " + ((new Date(ntfyObj.getTimeStamp())).toString()) + " " + ntfyObj.getType() + " " + ntfyObj.getMessage() + " Name = " + ntfyObj.getAttributeName() + " Old value = " + oldv + " New value = " + newv);
                    } catch (Exception e) {
                        if (MODELMBEAN_LOGGER.isLoggable(Level.FINE)) {
                            MODELMBEAN_LOGGER.logp(Level.FINE, RequiredModelMBean.class.getName(), mth, "Failed to log " + ntfyObj.getType() + " notification: ", e);
                        }
                    }
                }
            }
        }
        if (attributeBroadcaster != null) {
            attributeBroadcaster.sendNotification(ntfyObj);
        }
        if (generalBroadcaster != null) {
            generalBroadcaster.sendNotification(ntfyObj);
        }
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "sent notification");
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    public void sendAttributeChangeNotification(Attribute inOldVal, Attribute inNewVal) throws MBeanException, RuntimeOperationsException {
        final String mth = "sendAttributeChangeNotification(Attribute, Attribute)";
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Entry");
        }
        if ((inOldVal == null) || (inNewVal == null))
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute object must not be null"), "Exception occurred trying to send " + "attribute change notification of a ModelMBean");
        if (!(inOldVal.getName().equals(inNewVal.getName())))
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute names are not the same"), "Exception occurred trying to send " + "attribute change notification of a ModelMBean");
        Object newVal = inNewVal.getValue();
        Object oldVal = inOldVal.getValue();
        String className = "unknown";
        if (newVal != null)
            className = newVal.getClass().getName();
        if (oldVal != null)
            className = oldVal.getClass().getName();
        AttributeChangeNotification myNtfyObj = new AttributeChangeNotification(this, 1, ((new Date()).getTime()), "AttributeChangeDetected", inOldVal.getName(), className, inOldVal.getValue(), inNewVal.getValue());
        sendAttributeChangeNotification(myNtfyObj);
        if (MODELMBEAN_LOGGER.isLoggable(Level.FINER)) {
            MODELMBEAN_LOGGER.logp(Level.FINER, RequiredModelMBean.class.getName(), mth, "Exit");
        }
    }

    protected ClassLoaderRepository getClassLoaderRepository() {
        return MBeanServerFactory.getClassLoaderRepository(server);
    }

    private Class<?> loadClass(final String className) throws ClassNotFoundException {
        AccessControlContext stack = AccessController.getContext();
        final ClassNotFoundException[] caughtException = new ClassNotFoundException[1];
        Class c = javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Class<?>>() {

            @Override
            public Class<?> run() {
                try {
                    ReflectUtil.checkPackageAccess(className);
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    final ClassLoaderRepository clr = getClassLoaderRepository();
                    try {
                        if (clr == null)
                            throw new ClassNotFoundException(className);
                        return clr.loadClass(className);
                    } catch (ClassNotFoundException ex) {
                        caughtException[0] = ex;
                    }
                }
                return null;
            }
        }, stack, acc);
        if (caughtException[0] != null) {
            throw caughtException[0];
        }
        return c;
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws java.lang.Exception {
        if (name == null)
            throw new NullPointerException("name of RequiredModelMBean to registered is null");
        this.server = server;
        return name;
    }

    public void postRegister(Boolean registrationDone) {
        registered = registrationDone.booleanValue();
    }

    public void preDeregister() throws java.lang.Exception {
    }

    public void postDeregister() {
        registered = false;
        this.server = null;
    }

    private static final String[] primitiveTypes;

    private static final String[] primitiveWrappers;

    static {
        primitiveTypes = new String[] { Boolean.TYPE.getName(), Byte.TYPE.getName(), Character.TYPE.getName(), Short.TYPE.getName(), Integer.TYPE.getName(), Long.TYPE.getName(), Float.TYPE.getName(), Double.TYPE.getName(), Void.TYPE.getName() };
        primitiveWrappers = new String[] { Boolean.class.getName(), Byte.class.getName(), Character.class.getName(), Short.class.getName(), Integer.class.getName(), Long.class.getName(), Float.class.getName(), Double.class.getName(), Void.class.getName() };
    }
}
