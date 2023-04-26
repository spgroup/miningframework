package com.oracle.graal.hotspot;

import static com.oracle.graal.compiler.GraalDebugConfig.*;
import static com.oracle.graal.hotspot.HotSpotOptionsLoader.*;
import static java.lang.Double.*;
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.debug.*;
import com.oracle.graal.options.*;
import com.oracle.graal.options.OptionUtils.OptionConsumer;

public class HotSpotOptions {

    private static final String GRAAL_OPTION_PREFIX = "-G:";

    private static native void parseVMOptions();

    static {
        parseVMOptions();
        assert !Debug.Initialization.isDebugInitialized() : "The class " + Debug.class.getName() + " must not be initialized before the Graal runtime has been initialized. " + "This can be fixed by placing a call to " + Graal.class.getName() + ".runtime() on the path that triggers initialization of " + Debug.class.getName();
        if (areDebugScopePatternsEnabled()) {
            System.setProperty(Debug.Initialization.INITIALIZER_PROPERTY_NAME, "true");
        }
        if ("".equals(Meter.getValue())) {
            System.setProperty(Debug.ENABLE_UNSCOPED_METRICS_PROPERTY_NAME, "true");
        }
        if ("".equals(Time.getValue())) {
            System.setProperty(Debug.ENABLE_UNSCOPED_TIMERS_PROPERTY_NAME, "true");
        }
        if ("".equals(TrackMemUse.getValue())) {
            System.setProperty(Debug.ENABLE_UNSCOPED_MEM_USE_TRACKERS_PROPERTY_NAME, "true");
        }
    }

    public static void initialize() {
    }

    static void setOption(String name, OptionValue<?> option, char spec, String stringValue, long primitiveValue) {
        switch(spec) {
            case '+':
                option.setValue(Boolean.TRUE);
                break;
            case '-':
                option.setValue(Boolean.FALSE);
                break;
            case '?':
                OptionUtils.printFlags(options, GRAAL_OPTION_PREFIX);
                break;
            case ' ':
                OptionUtils.printNoMatchMessage(options, name, GRAAL_OPTION_PREFIX);
                break;
            case 'i':
                option.setValue((int) primitiveValue);
                break;
            case 'f':
                option.setValue((float) longBitsToDouble(primitiveValue));
                break;
            case 'd':
                option.setValue(longBitsToDouble(primitiveValue));
                break;
            case 's':
                option.setValue(stringValue);
                break;
        }
    }

    public static boolean parseOption(String option, OptionConsumer setter) {
        return OptionUtils.parseOption(options, option, GRAAL_OPTION_PREFIX, setter);
    }
}
