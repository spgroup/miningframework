package com.oracle.truffle.api.object;

import com.oracle.truffle.api.interop.TruffleObject;

@SuppressWarnings("deprecation")
public abstract class DynamicObject implements com.oracle.truffle.api.TypedObject, TruffleObject {

    protected DynamicObject() {
    }

    public abstract Shape getShape();

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
}
