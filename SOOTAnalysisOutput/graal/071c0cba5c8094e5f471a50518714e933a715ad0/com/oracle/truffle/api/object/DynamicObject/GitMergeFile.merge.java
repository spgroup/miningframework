package com.oracle.truffle.api.object;

import java.lang.reflect.Field;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.TruffleObject;
import sun.misc.Unsafe;

@SuppressWarnings("deprecation")
public abstract class DynamicObject implements com.oracle.truffle.api.TypedObject, TruffleObject {

    private Shape shape;

    @Deprecated
    protected DynamicObject() {
        CompilerAsserts.neverPartOfCompilation();
        throw new UnsupportedOperationException();
    }

    protected DynamicObject(Shape shape) {
        verifyShape(shape, this.getClass());
        this.shape = shape;
    }

    private static void verifyShape(Shape shape, Class<? extends DynamicObject> subclass) {
        Class<? extends DynamicObject> shapeType = shape.getLayout().getType();
        if (!(shapeType == subclass || (shapeType.isAssignableFrom(subclass) && DynamicObject.class.isAssignableFrom(shapeType)))) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new IllegalArgumentException("Incompatible shape");
        }
    }

    public final Shape getShape() {
        return getShapeHelper(shape);
    }

    private static Shape getShapeHelper(Shape shape) {
        return shape;
    }

    final void setShape(Shape shape) {
        assert shape.getLayout().getType().isInstance(this);
        setShapeHelper(shape, SHAPE_OFFSET);
    }

    private void setShapeHelper(Shape shape, long shapeOffset) {
        this.shape = shape;
    }

    public final Object get(Object key) {
        return get(key, null);
    }

    public abstract Object get(Object key, Object defaultValue);

    public abstract boolean set(Object key, Object value);

    public final boolean containsKey(Object key) {
        return getShape().getProperty(key) != null;
    }

    public final void define(Object key, Object value) {
        define(key, value, 0);
    }

    public abstract void define(Object key, Object value, int flags);

    public abstract void define(Object key, Object value, int flags, LocationFactory locationFactory);

    public abstract boolean delete(Object key);

    public abstract int size();

    public abstract boolean isEmpty();

    public abstract void setShapeAndGrow(Shape oldShape, Shape newShape);

    public abstract void setShapeAndResize(Shape oldShape, Shape newShape);

    public abstract boolean updateShape();

    public abstract DynamicObject copy(Shape currentShape);

    private static final Unsafe UNSAFE;

    private static final long SHAPE_OFFSET;

    static {
        UNSAFE = getUnsafe();
        try {
            SHAPE_OFFSET = UNSAFE.objectFieldOffset(DynamicObject.class.getDeclaredField("shape"));
        } catch (Exception e) {
            throw new IllegalStateException("Could not get 'shape' field offset", e);
        }
    }

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException e) {
        }
        try {
            Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeInstance.setAccessible(true);
            return (Unsafe) theUnsafeInstance.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("exception while trying to get Unsafe.theUnsafe via reflection:", e);
        }
    }
}
