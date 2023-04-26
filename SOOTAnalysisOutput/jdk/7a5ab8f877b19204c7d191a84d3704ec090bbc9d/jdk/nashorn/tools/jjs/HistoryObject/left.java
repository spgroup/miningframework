package jdk.nashorn.tools.jjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import jdk.internal.jline.console.history.FileHistory;
import jdk.internal.jline.console.history.History;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.runtime.JSType;
import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;

final class HistoryObject extends AbstractJSObject {

    private static final Set<String> props;

    static {
        final HashSet<String> s = new HashSet<>();
        s.add("clear");
        s.add("forEach");
        s.add("load");
        s.add("print");
        s.add("save");
        s.add("size");
        s.add("toString");
        props = Collections.unmodifiableSet(s);
    }

    private final FileHistory hist;

    private final PrintWriter err;

    private final Consumer<String> evaluator;

    HistoryObject(final FileHistory hist, final PrintWriter err, final Consumer<String> evaluator) {
        this.hist = hist;
        this.err = err;
        this.evaluator = evaluator;
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    @Override
    public Object call(final Object thiz, final Object... args) {
        if (args.length > 0) {
            int index = JSType.toInteger(args[0]);
            if (index < 0) {
                index += (hist.size() - 1);
            } else {
                index--;
            }
            if (index >= 0 && index < (hist.size() - 1)) {
                final CharSequence src = hist.get(index);
                hist.replace(src);
                err.println(src);
                evaluator.accept(src.toString());
            } else {
                hist.removeLast();
                err.println("no history entry @ " + (index + 1));
            }
        }
        return UNDEFINED;
    }

    @Override
    public Object getMember(final String name) {
        switch(name) {
            case "clear":
                return (Runnable) hist::clear;
            case "forEach":
                return (Function<JSObject, Object>) this::iterate;
            case "load":
                return (Consumer<Object>) this::load;
            case "print":
                return (Runnable) this::print;
            case "save":
                return (Consumer<Object>) this::save;
            case "size":
                return hist.size();
            case "toString":
                return (Supplier<String>) this::toString;
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
        final StringBuilder buf = new StringBuilder();
        for (History.Entry e : hist) {
            buf.append(e.value()).append('\n');
        }
        return buf.toString();
    }

    @Override
    public Set<String> keySet() {
        return props;
    }

    private void save(final Object obj) {
        final File file = getFile(obj);
        try (final PrintWriter pw = new PrintWriter(file)) {
            for (History.Entry e : hist) {
                pw.println(e.value());
            }
        } catch (final IOException exp) {
            throw new RuntimeException(exp);
        }
    }

    private void load(final Object obj) {
        final File file = getFile(obj);
        String item = null;
        try (final BufferedReader r = new BufferedReader(new FileReader(file))) {
            while ((item = r.readLine()) != null) {
                hist.add(item);
            }
        } catch (final IOException exp) {
            throw new RuntimeException(exp);
        }
    }

    private void print() {
        for (History.Entry e : hist) {
            System.out.printf("%3d %s\n", e.index() + 1, e.value());
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

    private static File getFile(final Object obj) {
        File file = null;
        if (obj instanceof String) {
            file = new File((String) obj);
        } else if (obj instanceof File) {
            file = (File) obj;
        } else {
            throw typeError("not.a.file", JSType.toString(obj));
        }
        return file;
    }
}
