package jdk.nashorn.internal.codegen;

import static jdk.nashorn.internal.codegen.Compiler.SCRIPTS_PACKAGE;
import static jdk.nashorn.internal.codegen.CompilerConstants.ALLOCATE;
import static jdk.nashorn.internal.codegen.CompilerConstants.INIT_ARGUMENTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.INIT_MAP;
import static jdk.nashorn.internal.codegen.CompilerConstants.INIT_SCOPE;
import static jdk.nashorn.internal.codegen.CompilerConstants.JAVA_THIS;
import static jdk.nashorn.internal.codegen.CompilerConstants.JS_OBJECT_PREFIX;
import static jdk.nashorn.internal.codegen.CompilerConstants.className;
import static jdk.nashorn.internal.codegen.CompilerConstants.constructorNoLookup;
import static jdk.nashorn.internal.lookup.Lookup.MH;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import jdk.nashorn.internal.codegen.ClassEmitter.Flag;
import jdk.nashorn.internal.codegen.types.Type;
import jdk.nashorn.internal.runtime.AccessorProperty;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.DebugLogger;
import jdk.nashorn.internal.runtime.FunctionScope;
import jdk.nashorn.internal.runtime.JSType;
import jdk.nashorn.internal.runtime.PropertyMap;
import jdk.nashorn.internal.runtime.ScriptEnvironment;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.ScriptRuntime;
import jdk.nashorn.internal.runtime.options.Options;

public final class ObjectClassGenerator {

    static final String SCOPE_MARKER = "P";

    public static final DebugLogger LOG = new DebugLogger("fields", "nashorn.fields.debug");

    public static final boolean DEBUG_FIELDS = LOG.isEnabled();

    public static final boolean OBJECT_FIELDS_ONLY = !Options.getBooleanProperty("nashorn.fields.dual");

    private static final List<Type> FIELD_TYPES = new LinkedList<>();

    public static final Type PRIMITIVE_TYPE = Type.LONG;

    static {
        if (!OBJECT_FIELDS_ONLY) {
            System.err.println("WARNING!!! Running with primitive fields - there is untested functionality!");
            FIELD_TYPES.add(PRIMITIVE_TYPE);
        }
        FIELD_TYPES.add(Type.OBJECT);
    }

    private final Context context;

    public static final List<Type> ACCESSOR_TYPES = Collections.unmodifiableList(Arrays.asList(Type.INT, Type.LONG, Type.NUMBER, Type.OBJECT));

    private static final int TYPE_INT_INDEX = 0;

    private static final int TYPE_LONG_INDEX = 1;

    private static final int TYPE_DOUBLE_INDEX = 2;

    private static final int TYPE_OBJECT_INDEX = 3;

    public ObjectClassGenerator(final Context context) {
        this.context = context;
        assert context != null;
    }

    public static int getAccessorTypeIndex(final Type type) {
        return getAccessorTypeIndex(type.getTypeClass());
    }

    public static int getAccessorTypeIndex(final Class<?> type) {
        if (type == int.class) {
            return 0;
        } else if (type == long.class) {
            return 1;
        } else if (type == double.class) {
            return 2;
        } else if (!type.isPrimitive()) {
            return 3;
        }
        return -1;
    }

    public static int getNumberOfAccessorTypes() {
        return ACCESSOR_TYPES.size();
    }

    public static Type getAccessorType(final int index) {
        return ACCESSOR_TYPES.get(index);
    }

    public static String getClassName(final int fieldCount) {
        return fieldCount != 0 ? SCRIPTS_PACKAGE + '/' + JS_OBJECT_PREFIX.symbolName() + fieldCount : SCRIPTS_PACKAGE + '/' + JS_OBJECT_PREFIX.symbolName();
    }

    public static String getClassName(final int fieldCount, final int paramCount) {
        return SCRIPTS_PACKAGE + '/' + JS_OBJECT_PREFIX.symbolName() + fieldCount + SCOPE_MARKER + paramCount;
<<<<<<< MINE
=======
    }

    public static int getFieldCount(Class<?> clazz) {
        final String name = clazz.getSimpleName();
        final String prefix = JS_OBJECT_PREFIX.symbolName();
        if (prefix.equals(name)) {
            return 0;
        }
        final int scopeMarker = name.indexOf(SCOPE_MARKER);
        return Integer.parseInt(scopeMarker == -1 ? name.substring(prefix.length()) : name.substring(prefix.length(), scopeMarker));
>>>>>>> YOURS
    }

    public static String getFieldName(final int fieldIndex, final Type type) {
        return type.getDescriptor().substring(0, 1) + fieldIndex;
    }

    private static void initializeToUndefined(final MethodEmitter init, final String className, final List<String> fieldNames) {
        if (fieldNames.isEmpty()) {
            return;
        }
        init.load(Type.OBJECT, JAVA_THIS.slot());
        init.loadUndefined(Type.OBJECT);
        final Iterator<String> iter = fieldNames.iterator();
        while (iter.hasNext()) {
            final String fieldName = iter.next();
            if (iter.hasNext()) {
                init.dup2();
            }
            init.putField(className, fieldName, Type.OBJECT.getDescriptor());
        }
    }

    public byte[] generate(final String descriptor) {
        final String[] counts = descriptor.split(SCOPE_MARKER);
        final int fieldCount = Integer.valueOf(counts[0]);
        if (counts.length == 1) {
            return generate(fieldCount);
        }
        final int paramCount = Integer.valueOf(counts[1]);
        return generate(fieldCount, paramCount);
    }

    public byte[] generate(final int fieldCount) {
        final String className = getClassName(fieldCount);
        final String superName = className(ScriptObject.class);
        final ClassEmitter classEmitter = newClassEmitter(className, superName);
        final List<String> initFields = addFields(classEmitter, fieldCount);
        final MethodEmitter init = newInitMethod(classEmitter);
        initializeToUndefined(init, className, initFields);
        init.returnVoid();
        init.end();
        newEmptyInit(classEmitter, className);
        newAllocate(classEmitter, className);
        return toByteArray(classEmitter);
    }

    public byte[] generate(final int fieldCount, final int paramCount) {
        final String className = getClassName(fieldCount, paramCount);
        final String superName = className(FunctionScope.class);
        final ClassEmitter classEmitter = newClassEmitter(className, superName);
        final List<String> initFields = addFields(classEmitter, fieldCount);
        final MethodEmitter init = newInitScopeMethod(classEmitter);
        initializeToUndefined(init, className, initFields);
        init.returnVoid();
        init.end();
        final MethodEmitter initWithArguments = newInitScopeWithArgumentsMethod(classEmitter);
        initializeToUndefined(initWithArguments, className, initFields);
        initWithArguments.returnVoid();
        initWithArguments.end();
        return toByteArray(classEmitter);
    }

    private static List<String> addFields(final ClassEmitter classEmitter, final int fieldCount) {
        final List<String> initFields = new LinkedList<>();
        for (int i = 0; i < fieldCount; i++) {
            for (final Type type : FIELD_TYPES) {
                final String fieldName = getFieldName(i, type);
                classEmitter.field(fieldName, type.getTypeClass());
                if (type == Type.OBJECT) {
                    initFields.add(fieldName);
                }
            }
        }
        return initFields;
    }

    private ClassEmitter newClassEmitter(final String className, final String superName) {
        final ClassEmitter classEmitter = new ClassEmitter(context.getEnv(), className, superName);
        classEmitter.begin();
        return classEmitter;
    }

    private static MethodEmitter newInitMethod(final ClassEmitter classEmitter) {
        final MethodEmitter init = classEmitter.init(PropertyMap.class);
        init.begin();
        init.load(Type.OBJECT, JAVA_THIS.slot());
        init.load(Type.OBJECT, INIT_MAP.slot());
        init.invoke(constructorNoLookup(ScriptObject.class, PropertyMap.class));
        return init;
    }

    private static MethodEmitter newInitScopeMethod(final ClassEmitter classEmitter) {
        final MethodEmitter init = classEmitter.init(PropertyMap.class, ScriptObject.class);
        init.begin();
        init.load(Type.OBJECT, JAVA_THIS.slot());
        init.load(Type.OBJECT, INIT_MAP.slot());
        init.load(Type.OBJECT, INIT_SCOPE.slot());
        init.invoke(constructorNoLookup(FunctionScope.class, PropertyMap.class, ScriptObject.class));
        return init;
    }

    private static MethodEmitter newInitScopeWithArgumentsMethod(final ClassEmitter classEmitter) {
        final MethodEmitter init = classEmitter.init(PropertyMap.class, ScriptObject.class, Object.class);
        init.begin();
        init.load(Type.OBJECT, JAVA_THIS.slot());
        init.load(Type.OBJECT, INIT_MAP.slot());
        init.load(Type.OBJECT, INIT_SCOPE.slot());
        init.load(Type.OBJECT, INIT_ARGUMENTS.slot());
        init.invoke(constructorNoLookup(FunctionScope.class, PropertyMap.class, ScriptObject.class, Object.class));
        return init;
    }

    private static void newEmptyInit(final ClassEmitter classEmitter, final String className) {
        final MethodEmitter emptyInit = classEmitter.init();
        emptyInit.begin();
        emptyInit.load(Type.OBJECT, JAVA_THIS.slot());
        emptyInit.loadNull();
        emptyInit.invoke(constructorNoLookup(className, PropertyMap.class));
        emptyInit.returnVoid();
        emptyInit.end();
    }

    private static void newAllocate(final ClassEmitter classEmitter, final String className) {
        final MethodEmitter allocate = classEmitter.method(EnumSet.of(Flag.PUBLIC, Flag.STATIC), ALLOCATE.symbolName(), ScriptObject.class, PropertyMap.class);
        allocate.begin();
        allocate._new(className);
        allocate.dup();
        allocate.load(Type.typeFor(PropertyMap.class), 0);
        allocate.invoke(constructorNoLookup(className, PropertyMap.class));
        allocate._return();
        allocate.end();
    }

    private byte[] toByteArray(final ClassEmitter classEmitter) {
        classEmitter.end();
        final byte[] code = classEmitter.toByteArray();
        final ScriptEnvironment env = context.getEnv();
        if (env._print_code) {
            env.getErr().println(ClassEmitter.disassemble(code));
        }
        if (env._verify_code) {
            context.verify(code);
        }
        return code;
    }

    private static final MethodHandle PACK_DOUBLE = MH.explicitCastArguments(MH.findStatic(MethodHandles.publicLookup(), Double.class, "doubleToRawLongBits", MH.type(long.class, double.class)), MH.type(long.class, double.class));

    private static MethodHandle UNPACK_DOUBLE = MH.findStatic(MethodHandles.publicLookup(), Double.class, "longBitsToDouble", MH.type(double.class, long.class));

    private static MethodHandle[] CONVERT_OBJECT = { JSType.TO_INT32.methodHandle(), JSType.TO_UINT32.methodHandle(), JSType.TO_NUMBER.methodHandle(), null };

    public static MethodHandle createGetter(final Class<?> forType, final Class<?> type, final MethodHandle primitiveGetter, final MethodHandle objectGetter) {
        final int fti = forType == null ? -1 : getAccessorTypeIndex(forType);
        final int ti = getAccessorTypeIndex(type);
        if (fti == TYPE_OBJECT_INDEX || OBJECT_FIELDS_ONLY) {
            if (ti == TYPE_OBJECT_INDEX) {
                return objectGetter;
            }
            return MH.filterReturnValue(objectGetter, CONVERT_OBJECT[ti]);
        }
        assert !OBJECT_FIELDS_ONLY;
        if (forType == null) {
            return GET_UNDEFINED[ti];
        }
        final MethodType pmt = primitiveGetter.type();
        switch(fti) {
            case TYPE_INT_INDEX:
            case TYPE_LONG_INDEX:
                switch(ti) {
                    case TYPE_INT_INDEX:
                        return MH.explicitCastArguments(primitiveGetter, pmt.changeReturnType(int.class));
                    case TYPE_LONG_INDEX:
                        return primitiveGetter;
                    default:
                        return MH.asType(primitiveGetter, pmt.changeReturnType(type));
                }
            case TYPE_DOUBLE_INDEX:
                final MethodHandle getPrimitiveAsDouble = MH.filterReturnValue(primitiveGetter, UNPACK_DOUBLE);
                switch(ti) {
                    case TYPE_INT_INDEX:
                    case TYPE_LONG_INDEX:
                        return MH.explicitCastArguments(getPrimitiveAsDouble, pmt.changeReturnType(type));
                    case TYPE_DOUBLE_INDEX:
                        return getPrimitiveAsDouble;
                    default:
                        return MH.asType(getPrimitiveAsDouble, pmt.changeReturnType(Object.class));
                }
            default:
                assert false;
                return null;
        }
    }

    private static final MethodHandle IS_TYPE_GUARD = findOwnMH("isType", boolean.class, Class.class, Object.class);

    @SuppressWarnings("unused")
    private static boolean isType(final Class<?> boxedForType, final Object x) {
        return x.getClass() == boxedForType;
    }

    private static Class<? extends Number> getBoxedType(final Class<?> forType) {
        if (forType == int.class) {
            return Integer.class;
        }
        if (forType == long.class) {
            return Long.class;
        }
        if (forType == double.class) {
            return Double.class;
        }
        assert false;
        return null;
    }

    public static MethodHandle createGuardBoxedPrimitiveSetter(final Class<?> forType, final MethodHandle primitiveSetter, final MethodHandle objectSetter) {
        final Class<? extends Number> boxedForType = getBoxedType(forType);
        return MH.guardWithTest(MH.insertArguments(MH.dropArguments(IS_TYPE_GUARD, 1, Object.class), 0, boxedForType), MH.asType(primitiveSetter, objectSetter.type()), objectSetter);
    }

    public static MethodHandle createSetter(final Class<?> forType, final Class<?> type, final MethodHandle primitiveSetter, final MethodHandle objectSetter) {
        assert forType != null;
        final int fti = getAccessorTypeIndex(forType);
        final int ti = getAccessorTypeIndex(type);
        if (fti == TYPE_OBJECT_INDEX || OBJECT_FIELDS_ONLY) {
            if (ti == TYPE_OBJECT_INDEX) {
                return objectSetter;
            }
            return MH.asType(objectSetter, objectSetter.type().changeParameterType(1, type));
        }
        assert !OBJECT_FIELDS_ONLY;
        final MethodType pmt = primitiveSetter.type();
        switch(fti) {
            case TYPE_INT_INDEX:
            case TYPE_LONG_INDEX:
                switch(ti) {
                    case TYPE_INT_INDEX:
                        return MH.asType(primitiveSetter, pmt.changeParameterType(1, int.class));
                    case TYPE_LONG_INDEX:
                        return primitiveSetter;
                    case TYPE_DOUBLE_INDEX:
                        return MH.filterArguments(primitiveSetter, 1, PACK_DOUBLE);
                    default:
                        return objectSetter;
                }
            case TYPE_DOUBLE_INDEX:
                if (ti == TYPE_OBJECT_INDEX) {
                    return objectSetter;
                }
                return MH.asType(MH.filterArguments(primitiveSetter, 1, PACK_DOUBLE), pmt.changeParameterType(1, type));
            default:
                assert false;
                return null;
        }
    }

    public static final int UNDEFINED_INT = 0;

    public static final long UNDEFINED_LONG = 0L;

    public static final double UNDEFINED_DOUBLE = Double.NaN;

    private static String typeName(final Type type) {
        String name = type.getTypeClass().getName();
        final int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(dot + 1);
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static final MethodHandle[] GET_UNDEFINED = new MethodHandle[ObjectClassGenerator.getNumberOfAccessorTypes()];

    public static MethodHandle getUndefined(final Class<?> returnType) {
        return GET_UNDEFINED[ObjectClassGenerator.getAccessorTypeIndex(returnType)];
    }

    static {
        int pos = 0;
        for (final Type type : ACCESSOR_TYPES) {
            GET_UNDEFINED[pos++] = findOwnMH("getUndefined" + typeName(type), type.getTypeClass(), Object.class);
        }
    }

    @SuppressWarnings("unused")
    private static int getUndefinedInt(final Object obj) {
        return UNDEFINED_INT;
    }

    @SuppressWarnings("unused")
    private static long getUndefinedLong(final Object obj) {
        return UNDEFINED_LONG;
    }

    @SuppressWarnings("unused")
    private static double getUndefinedDouble(final Object obj) {
        return UNDEFINED_DOUBLE;
    }

    @SuppressWarnings("unused")
    private static Object getUndefinedObject(final Object obj) {
        return ScriptRuntime.UNDEFINED;
    }

    private static MethodHandle findOwnMH(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(MethodHandles.lookup(), ObjectClassGenerator.class, name, MH.type(rtype, types));
    }
}
