package jdk.nashorn.tools.jjs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import jdk.internal.jline.console.history.FileHistory;
import jdk.internal.jline.console.history.History;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.JSType;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;

final class HistoryObject extends AbstractJSObject {

    private static final Set<String> props;

    static {
        final HashSet<String> s = new HashSet<>();
        s.add("clear");
        s.add("forEach");
        s.add("print");
        s.add("size");
        props = Collections.unmodifiableSet(s);
    }

    private final FileHistory hist;

    HistoryObject(final FileHistory hist) {
        this.hist = hist;
    }

    @Override
    public Object getMember(final String name) {
        switch(name) {
            case "clear":
                return (Runnable) hist::clear;
            case "forEach":
                return (Function<JSObject, Object>) this::iterate;
            case "print":
                return (Runnable) this::print;
            case "size":
                return hist.size();
        }
        return UNDEFINED;
    }

    @Override
    public Object getDefaultValue(final Class<?> hint) {
        if (hint == String.class) {
            return toString();
        }
        return UNDEFINED;
    }

    @Override
    public String toString() {
        return "[object history]";
    }

    @Override
    public Set<String> keySet() {
        return props;
    }

    private void print() {
        for (History.Entry e : hist) {
            System.out.println(e.value());
        }
    }

    private Object iterate(final JSObject func) {
        for (History.Entry e : hist) {
            if (JSType.toBoolean(func.call(this, e.value().toString()))) {
                break;
            }
        }
        return UNDEFINED;
    }
}
