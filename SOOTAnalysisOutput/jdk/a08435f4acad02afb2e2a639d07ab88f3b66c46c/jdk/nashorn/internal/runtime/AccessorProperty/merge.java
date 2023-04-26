package jdk.nashorn.internal.runtime;

import static jdk.nashorn.internal.codegen.ObjectClassGenerator.ACCESSOR_TYPES;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.DEBUG_FIELDS;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.LOG;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.OBJECT_FIELDS_ONLY;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.PRIMITIVE_TYPE;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.createGetter;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.createGuardBoxedPrimitiveSetter;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.createSetter;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.getAccessorType;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.getAccessorTypeIndex;
import static jdk.nashorn.internal.codegen.ObjectClassGenerator.getNumberOfAccessorTypes;
import static jdk.nashorn.internal.lookup.Lookup.MH;
import static jdk.nashorn.internal.lookup.MethodHandleFactory.stripName;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.nashorn.internal.codegen.ObjectClassGenerator;
import jdk.nashorn.internal.codegen.types.Type;
import jdk.nashorn.internal.lookup.Lookup;
import jdk.nashorn.internal.lookup.MethodHandleFactory;

public class AccessorProperty extends Property {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private static final MethodHandle REPLACE_MAP = findOwnMH("replaceMap", Object.class, Object.class, PropertyMap.class, String.class, Class.class, Class.class);

    private static final int NOOF_TYPES = getNumberOfAccessorTypes();

    private static ClassValue<GettersSetters> GETTERS_SETTERS = new ClassValue<GettersSetters>() {

        @Override
        protected GettersSetters computeValue(Class<?> structure) {
            return new GettersSetters(structure);
        }
    };

    private MethodHandle[] getters = new MethodHandle[NOOF_TYPES];

    private static final MethodType[] ACCESSOR_GETTER_TYPES = new MethodType[NOOF_TYPES];

    private static final MethodType[] ACCESSOR_SETTER_TYPES = new MethodType[NOOF_TYPES];

    private static final MethodHandle SPILL_ELEMENT_GETTER;

    private static final MethodHandle SPILL_ELEMENT_SETTER;

    private static final int SPILL_CACHE_SIZE = 8;

    private static final MethodHandle[] SPILL_ACCESSORS = new MethodHandle[SPILL_CACHE_SIZE * 2];

    static {
        for (int i = 0; i < NOOF_TYPES; i++) {
            final Type type = ACCESSOR_TYPES.get(i);
            ACCESSOR_GETTER_TYPES[i] = MH.type(type.getTypeClass(), Object.class);
            ACCESSOR_SETTER_TYPES[i] = MH.type(void.class, Object.class, type.getTypeClass());
        }
        final MethodHandle spillGetter = MH.getter(MethodHandles.lookup(), ScriptObject.class, "spill", Object[].class);
        SPILL_ELEMENT_GETTER = MH.filterArguments(MH.arrayElementGetter(Object[].class), 0, spillGetter);
        SPILL_ELEMENT_SETTER = MH.filterArguments(MH.arrayElementSetter(Object[].class), 0, spillGetter);
    }

    private MethodHandle primitiveGetter;

    private MethodHandle primitiveSetter;

    private MethodHandle objectGetter;

    private MethodHandle objectSetter;

    private Class<?> currentType;

    public AccessorProperty(final AccessorProperty property, final ScriptObject delegate) {
        super(property);
        this.primitiveGetter = bindTo(property.primitiveGetter, delegate);
        this.primitiveSetter = bindTo(property.primitiveSetter, delegate);
        this.objectGetter = bindTo(property.objectGetter, delegate);
        this.objectSetter = bindTo(property.objectSetter, delegate);
        setCurrentType(property.getCurrentType());
    }

    public AccessorProperty(final String key, final int flags, final int slot) {
        super(key, flags, slot);
        assert (flags & IS_SPILL) == IS_SPILL;
        setCurrentType(Object.class);
    }

    public AccessorProperty(final String key, final int flags, final int slot, final MethodHandle getter, final MethodHandle setter) {
        super(key, flags, slot);
        final Class<?> getterType = getter.type().returnType();
        final Class<?> setterType = setter == null ? null : setter.type().parameterType(1);
        assert setterType == null || setterType == getterType;
        if (getterType.isPrimitive()) {
            for (int i = 0; i < NOOF_TYPES; i++) {
                getters[i] = MH.asType(Lookup.filterReturnType(getter, getAccessorType(i).getTypeClass()), ACCESSOR_GETTER_TYPES[i]);
            }
        } else {
            objectGetter = getter;
            objectSetter = setter;
        }
        setCurrentType(getterType);
    }

    private static class GettersSetters {

        final MethodHandle[] getters;

        final MethodHandle[] setters;

        public GettersSetters(Class<?> structure) {
            final int fieldCount = ObjectClassGenerator.getFieldCount(structure);
            getters = new MethodHandle[fieldCount];
            setters = new MethodHandle[fieldCount];
            for (int i = 0; i < fieldCount; ++i) {
                final String fieldName = ObjectClassGenerator.getFieldName(i, Type.OBJECT);
                getters[i] = MH.getter(lookup, structure, fieldName, Type.OBJECT.getTypeClass());
                setters[i] = MH.setter(lookup, structure, fieldName, Type.OBJECT.getTypeClass());
            }
        }
    }

    public AccessorProperty(final String key, final int flags, final Class<?> structure, final int slot) {
        super(key, flags, slot);
        primitiveGetter = null;
        primitiveSetter = null;
        if (isParameter() && hasArguments()) {
            final MethodHandle arguments = MH.getter(lookup, structure, "arguments", Object.class);
            final MethodHandle argumentsSO = MH.asType(arguments, arguments.type().changeReturnType(ScriptObject.class));
            objectGetter = MH.insertArguments(MH.filterArguments(ScriptObject.GET_ARGUMENT.methodHandle(), 0, argumentsSO), 1, slot);
            objectSetter = MH.insertArguments(MH.filterArguments(ScriptObject.SET_ARGUMENT.methodHandle(), 0, argumentsSO), 1, slot);
        } else {
            final GettersSetters gs = GETTERS_SETTERS.get(structure);
            objectGetter = gs.getters[slot];
            objectSetter = gs.setters[slot];
            if (!OBJECT_FIELDS_ONLY) {
                final String fieldNamePrimitive = ObjectClassGenerator.getFieldName(slot, ObjectClassGenerator.PRIMITIVE_TYPE);
                primitiveGetter = MH.getter(lookup, structure, fieldNamePrimitive, PRIMITIVE_TYPE.getTypeClass());
                primitiveSetter = MH.setter(lookup, structure, fieldNamePrimitive, PRIMITIVE_TYPE.getTypeClass());
            }
        }
        Class<?> initialType = null;
        if (OBJECT_FIELDS_ONLY || isAlwaysObject()) {
            initialType = Object.class;
        } else if (!canBePrimitive()) {
            info(key + " cannot be primitive");
            initialType = Object.class;
        } else {
            info(key + " CAN be primitive");
            if (!canBeUndefined()) {
                info(key + " is always defined");
                initialType = int.class;
            }
        }
        setCurrentType(initialType);
    }

    protected AccessorProperty(final AccessorProperty property) {
        super(property);
        this.getters = property.getters;
        this.primitiveGetter = property.primitiveGetter;
        this.primitiveSetter = property.primitiveSetter;
        this.objectGetter = property.objectGetter;
        this.objectSetter = property.objectSetter;
        setCurrentType(property.getCurrentType());
    }

    private static MethodHandle bindTo(final MethodHandle mh, final Object receiver) {
        if (mh == null) {
            return null;
        }
        return MH.dropArguments(MH.bindTo(mh, receiver), 0, Object.class);
    }

    @Override
    protected Property copy() {
        return new AccessorProperty(this);
    }

    @Override
    public void setObjectValue(final ScriptObject self, final ScriptObject owner, final Object value, final boolean strict) {
        if (isSpill()) {
            self.spill[getSlot()] = value;
        } else {
            try {
                getSetter(Object.class, self.getMap()).invokeExact((Object) self, value);
            } catch (final Error | RuntimeException e) {
                throw e;
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Object getObjectValue(final ScriptObject self, final ScriptObject owner) {
        if (isSpill()) {
            return self.spill[getSlot()];
        }
        try {
            return getGetter(Object.class).invokeExact((Object) self);
        } catch (final Error | RuntimeException e) {
            throw e;
        } catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MethodHandle getGetter(final Class<?> type) {
        if (isSpill() && objectGetter == null) {
            objectGetter = getSpillGetter();
        }
        final int i = getAccessorTypeIndex(type);
        if (getters[i] == null) {
            getters[i] = debug(MH.asType(createGetter(currentType, type, primitiveGetter, objectGetter), ACCESSOR_GETTER_TYPES[i]), currentType, type, "get");
        }
        return getters[i];
    }

    private Property getWiderProperty(final Class<?> type) {
        final AccessorProperty newProperty = new AccessorProperty(this);
        newProperty.invalidate(type);
        return newProperty;
    }

    private PropertyMap getWiderMap(final PropertyMap oldMap, final Property newProperty) {
        final PropertyMap newMap = oldMap.replaceProperty(this, newProperty);
        assert oldMap.size() > 0;
        assert newMap.size() == oldMap.size();
        return newMap;
    }

    @SuppressWarnings("unused")
    private static Object replaceMap(final Object sobj, final PropertyMap newMap, final String key, final Class<?> oldType, final Class<?> newType) {
        if (DEBUG_FIELDS) {
            final PropertyMap oldMap = ((ScriptObject) sobj).getMap();
            info("Type change for '" + key + "' " + oldType + "=>" + newType);
            finest("setting map " + sobj + " from " + Debug.id(oldMap) + " to " + Debug.id(newMap) + " " + oldMap + " => " + newMap);
        }
        ((ScriptObject) sobj).setMap(newMap);
        return sobj;
    }

    private MethodHandle generateSetter(final Class<?> forType, final Class<?> type) {
        if (isSpill() && objectSetter == null) {
            objectSetter = getSpillSetter();
        }
        MethodHandle mh = createSetter(forType, type, primitiveSetter, objectSetter);
        mh = MH.asType(mh, ACCESSOR_SETTER_TYPES[getAccessorTypeIndex(type)]);
        mh = debug(mh, currentType, type, "set");
        return mh;
    }

    @Override
    public MethodHandle getSetter(final Class<?> type, final PropertyMap currentMap) {
        final int i = getAccessorTypeIndex(type);
        final int ci = currentType == null ? -1 : getAccessorTypeIndex(currentType);
        final Class<?> forType = currentType == null ? type : currentType;
        MethodHandle mh;
        if (needsInvalidator(i, ci)) {
            final Property newProperty = getWiderProperty(type);
            final PropertyMap newMap = getWiderMap(currentMap, newProperty);
            final MethodHandle widerSetter = newProperty.getSetter(type, newMap);
            final MethodHandle explodeTypeSetter = MH.filterArguments(widerSetter, 0, MH.insertArguments(REPLACE_MAP, 1, newMap, getKey(), currentType, type));
            if (currentType != null && currentType.isPrimitive() && type == Object.class) {
                mh = createGuardBoxedPrimitiveSetter(currentType, generateSetter(currentType, currentType), explodeTypeSetter);
            } else {
                mh = explodeTypeSetter;
            }
        } else {
            mh = generateSetter(forType, type);
        }
        return mh;
    }

    @Override
    public boolean canChangeType() {
        if (OBJECT_FIELDS_ONLY) {
            return false;
        }
        return currentType != Object.class && (isConfigurable() || isWritable());
    }

    private boolean needsInvalidator(final int ti, final int fti) {
        return canChangeType() && ti > fti;
    }

    private void invalidate(final Class<?> newType) {
        getters = new MethodHandle[NOOF_TYPES];
        setCurrentType(newType);
    }

    private MethodHandle getSpillGetter() {
        final int slot = getSlot();
        MethodHandle getter = slot < SPILL_CACHE_SIZE ? SPILL_ACCESSORS[slot * 2] : null;
        if (getter == null) {
            getter = MH.asType(MH.insertArguments(SPILL_ELEMENT_GETTER, 1, slot), Lookup.GET_OBJECT_TYPE);
            if (slot < SPILL_CACHE_SIZE) {
                SPILL_ACCESSORS[slot * 2] = getter;
            }
        }
        return getter;
    }

    private MethodHandle getSpillSetter() {
        final int slot = getSlot();
        MethodHandle setter = slot < SPILL_CACHE_SIZE ? SPILL_ACCESSORS[slot * 2 + 1] : null;
        if (setter == null) {
            setter = MH.asType(MH.insertArguments(SPILL_ELEMENT_SETTER, 1, slot), Lookup.SET_OBJECT_TYPE);
            if (slot < SPILL_CACHE_SIZE) {
                SPILL_ACCESSORS[slot * 2 + 1] = setter;
            }
        }
        return setter;
    }

    private static void finest(final String str) {
        if (DEBUG_FIELDS) {
            LOG.finest(str);
        }
    }

    private static void info(final String str) {
        if (DEBUG_FIELDS) {
            LOG.info(str);
        }
    }

    private MethodHandle debug(final MethodHandle mh, final Class<?> forType, final Class<?> type, final String tag) {
        if (DEBUG_FIELDS) {
            return MethodHandleFactory.addDebugPrintout(LOG, mh, tag + " '" + getKey() + "' (property=" + Debug.id(this) + ", forType=" + stripName(forType) + ", type=" + stripName(type) + ')');
        }
        return mh;
    }

    private void setCurrentType(final Class<?> currentType) {
        this.currentType = currentType;
    }

    @Override
    public Class<?> getCurrentType() {
        return currentType;
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(lookup, AccessorProperty.class, name, MH.type(rtype, types));
    }
}
