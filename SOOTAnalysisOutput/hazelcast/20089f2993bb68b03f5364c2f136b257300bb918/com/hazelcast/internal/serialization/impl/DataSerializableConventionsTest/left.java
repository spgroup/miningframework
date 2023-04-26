package com.hazelcast.internal.serialization.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hazelcast.internal.serialization.DataSerializerHook;
import com.hazelcast.map.impl.wan.WanMapEntryView;
import com.hazelcast.nio.serialization.BinaryInterface;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.nio.serialization.SerializableByConvention;
import com.hazelcast.query.impl.SkipIndexPredicate;
import com.hazelcast.spi.AbstractLocalOperation;
import com.hazelcast.spi.annotation.PrivateApi;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import static com.hazelcast.test.ReflectionsHelper.REFLECTIONS;
import static com.hazelcast.test.ReflectionsHelper.filterNonConcreteClasses;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(HazelcastParallelClassRunner.class)
@Category({ QuickTest.class })
public class DataSerializableConventionsTest {

    private final Set<Class> classWhiteList;

    public DataSerializableConventionsTest() {
        classWhiteList = Collections.unmodifiableSet(getWhitelistedClasses());
    }

    @Test
    public void test_dataSerializableClasses_areIdentifiedDataSerializable() {
        Set<Class<? extends DataSerializable>> dataSerializableClasses = REFLECTIONS.getSubTypesOf(DataSerializable.class);
        Set<Class<? extends IdentifiedDataSerializable>> allIdDataSerializableClasses = REFLECTIONS.getSubTypesOf(IdentifiedDataSerializable.class);
        dataSerializableClasses.removeAll(allIdDataSerializableClasses);
        dataSerializableClasses.remove(IdentifiedDataSerializable.class);
        filterNonConcreteClasses(dataSerializableClasses);
        Set<?> allAnnotatedClasses = REFLECTIONS.getTypesAnnotatedWith(BinaryInterface.class, true);
        dataSerializableClasses.removeAll(allAnnotatedClasses);
        Set<?> serializableByConventions = REFLECTIONS.getTypesAnnotatedWith(SerializableByConvention.class, true);
        dataSerializableClasses.removeAll(serializableByConventions);
        if (dataSerializableClasses.size() > 0) {
            SortedSet<String> nonCompliantClassNames = new TreeSet<String>();
            for (Object o : dataSerializableClasses) {
                nonCompliantClassNames.add(o.toString());
            }
            System.out.println("The following classes are DataSerializable while they should be IdentifiedDataSerializable:");
            for (String s : nonCompliantClassNames) {
                System.out.println(s);
            }
            fail("There are " + dataSerializableClasses.size() + " classes which are DataSerializable, not @BinaryInterface-" + "annotated and are not IdentifiedDataSerializable.");
        }
    }

    @Test
    public void test_serializableClasses_areIdentifiedDataSerializable() {
        Set<Class<? extends Serializable>> serializableClasses = REFLECTIONS.getSubTypesOf(Serializable.class);
        Set<Class<? extends IdentifiedDataSerializable>> allIdDataSerializableClasses = REFLECTIONS.getSubTypesOf(IdentifiedDataSerializable.class);
        serializableClasses.removeAll(allIdDataSerializableClasses);
        filterNonConcreteClasses(serializableClasses);
        Set<?> allAnnotatedClasses = REFLECTIONS.getTypesAnnotatedWith(BinaryInterface.class, true);
        serializableClasses.removeAll(allAnnotatedClasses);
        Set<?> serializableByConventions = REFLECTIONS.getTypesAnnotatedWith(SerializableByConvention.class, true);
        serializableClasses.removeAll(serializableByConventions);
        if (serializableClasses.size() > 0) {
            SortedSet<String> nonCompliantClassNames = new TreeSet<String>();
            for (Object o : serializableClasses) {
                if (!inheritsFromWhiteListedClass((Class) o)) {
                    nonCompliantClassNames.add(o.toString());
                }
            }
            if (!nonCompliantClassNames.isEmpty()) {
                System.out.println("The following classes are Serializable and should be IdentifiedDataSerializable:");
                for (String s : nonCompliantClassNames) {
                    System.out.println(s);
                }
                fail("There are " + nonCompliantClassNames.size() + " classes which are Serializable, not @BinaryInterface-" + "annotated and are not IdentifiedDataSerializable.");
            }
        }
    }

    @Test
    public void test_identifiedDataSerializables_haveUniqueFactoryAndTypeId() throws Exception {
        Set<String> classesWithInstantiationProblems = new TreeSet<String>();
        Set<String> classesThrowingUnsupportedOperationException = new TreeSet<String>();
        Multimap<Integer, Integer> factoryToTypeId = HashMultimap.create();
        Set<Class<? extends IdentifiedDataSerializable>> identifiedDataSerializables = getIDSConcreteClasses();
        for (Class<? extends IdentifiedDataSerializable> klass : identifiedDataSerializables) {
            if (!AbstractLocalOperation.class.isAssignableFrom(klass) && !isReadOnlyConfig(klass)) {
                try {
                    Constructor<? extends IdentifiedDataSerializable> ctor = klass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    IdentifiedDataSerializable instance = ctor.newInstance();
                    int factoryId = instance.getFactoryId();
                    int typeId = instance.getId();
                    if (factoryToTypeId.containsEntry(factoryId, typeId)) {
                        fail("Factory-Type ID pair {" + factoryId + ", " + typeId + "} from " + klass.toString() + " is already" + " registered in another type.");
                    } else {
                        factoryToTypeId.put(factoryId, typeId);
                    }
                } catch (UnsupportedOperationException e) {
                    classesThrowingUnsupportedOperationException.add(klass.getName());
                } catch (InstantiationException e) {
                    classesWithInstantiationProblems.add(klass.getName() + " failed with " + e.getMessage());
                } catch (NoSuchMethodException e) {
                    classesWithInstantiationProblems.add(klass.getName() + " failed with " + e.getMessage());
                }
            }
        }
        if (!classesThrowingUnsupportedOperationException.isEmpty()) {
            System.out.println("INFO: " + classesThrowingUnsupportedOperationException.size() + " classes threw" + " UnsupportedOperationException in getFactoryId/getId invocation:");
            for (String className : classesThrowingUnsupportedOperationException) {
                System.out.println(className);
            }
        }
        if (!classesWithInstantiationProblems.isEmpty()) {
            System.out.println("There are " + classesWithInstantiationProblems.size() + " classes which threw an exception while" + " attempting to invoke a default no-args constructor. See console output for exception details." + " List of problematic classes:");
            for (String className : classesWithInstantiationProblems) {
                System.out.println(className);
            }
            fail("There are " + classesWithInstantiationProblems.size() + " classes which threw an exception while" + " attempting to invoke a default no-args constructor. See test output for exception details.");
        }
    }

    @Test
    public void test_identifiedDataSerializables_areInstancesOfSameClass_whenConstructedFromFactory() throws Exception {
        Set<Class<? extends DataSerializerHook>> dsHooks = REFLECTIONS.getSubTypesOf(DataSerializerHook.class);
        Map<Integer, DataSerializableFactory> factories = new HashMap<Integer, DataSerializableFactory>();
        for (Class<? extends DataSerializerHook> hookClass : dsHooks) {
            DataSerializerHook dsHook = hookClass.newInstance();
            DataSerializableFactory factory = dsHook.createFactory();
            factories.put(dsHook.getFactoryId(), factory);
        }
        Set<Class<? extends IdentifiedDataSerializable>> identifiedDataSerializables = getIDSConcreteClasses();
        for (Class<? extends IdentifiedDataSerializable> klass : identifiedDataSerializables) {
            if (AbstractLocalOperation.class.isAssignableFrom(klass)) {
                continue;
            }
            if (isReadOnlyConfig(klass)) {
                continue;
            }
            try {
                Constructor<? extends IdentifiedDataSerializable> ctor = klass.getDeclaredConstructor();
                ctor.setAccessible(true);
                IdentifiedDataSerializable instance = ctor.newInstance();
                int factoryId = instance.getFactoryId();
                int typeId = instance.getId();
                if (!factories.containsKey(factoryId)) {
                    fail("Factory with ID " + factoryId + " declared in " + klass + " not found." + " Is such a factory ID registered?");
                }
                IdentifiedDataSerializable instanceFromFactory = factories.get(factoryId).create(typeId);
                assertNotNull("Factory with ID " + factoryId + " returned null for type with ID " + typeId, instanceFromFactory);
                assertTrue("Factory with ID " + factoryId + " instantiated an object of " + instanceFromFactory.getClass() + " while expected type was " + instance.getClass(), instanceFromFactory.getClass().equals(instance.getClass()));
            } catch (UnsupportedOperationException ignored) {
            }
        }
    }

    private boolean isReadOnlyConfig(Class<? extends IdentifiedDataSerializable> klass) {
        String className = klass.getName();
        return className.endsWith("ReadOnly") && className.contains("Config");
    }

    private Set<Class<? extends IdentifiedDataSerializable>> getIDSConcreteClasses() {
        Set<Class<? extends IdentifiedDataSerializable>> identifiedDataSerializables = REFLECTIONS.getSubTypesOf(IdentifiedDataSerializable.class);
        filterNonConcreteClasses(identifiedDataSerializables);
        identifiedDataSerializables.removeAll(classWhiteList);
        return identifiedDataSerializables;
    }

    private boolean inheritsClassFromPublicClass(Class klass, Class inheritedClass) {
        Class[] interfaces = klass.getInterfaces();
        if (interfaces != null) {
            for (Class implementedInterface : interfaces) {
                if (implementedInterface.equals(inheritedClass)) {
                    return false;
                } else if (inheritedClass.isAssignableFrom(implementedInterface) && isPublicClass(implementedInterface)) {
                    return true;
                }
            }
        }
        Class hierarchyIteratingClass = klass;
        while (hierarchyIteratingClass.getSuperclass() != null) {
            if (hierarchyIteratingClass.getSuperclass().equals(inheritedClass)) {
                return true;
            }
            if (inheritedClass.isAssignableFrom(hierarchyIteratingClass.getSuperclass()) && isPublicClass(hierarchyIteratingClass.getSuperclass())) {
                return true;
            }
            hierarchyIteratingClass = hierarchyIteratingClass.getSuperclass();
        }
        return false;
    }

    private boolean isPublicClass(Class klass) {
        return !klass.getName().contains(".impl.") && !klass.getName().contains(".internal.") && klass.getAnnotation(PrivateApi.class) == null;
    }

    private boolean inheritsFromWhiteListedClass(Class klass) {
        for (Class superclass : classWhiteList) {
            if (superclass.isAssignableFrom(klass)) {
                return true;
            }
        }
        return false;
    }

    protected Set<Class> getWhitelistedClasses() {
        Set<Class> whiteList = new HashSet<Class>();
        whiteList.add(EventObject.class);
        whiteList.add(Throwable.class);
        whiteList.add(Permission.class);
        whiteList.add(PermissionCollection.class);
        whiteList.add(WanMapEntryView.class);
        whiteList.add(SkipIndexPredicate.class);
        return whiteList;
    }
}
