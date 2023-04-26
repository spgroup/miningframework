package com.oracle.graal.hotspot;

import static com.oracle.graal.compiler.GraalDebugConfig.*;
<<<<<<< MINE
import static com.oracle.graal.options.OptionsLoader.*;
import java.lang.reflect.*;
=======
import static com.oracle.graal.hotspot.HotSpotOptionsLoader.*;
import static java.lang.Double.*;
>>>>>>> YOURS
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.debug.*;
import com.oracle.graal.options.*;
<<<<<<< MINE
import com.oracle.graal.phases.common.inlining.*;
=======
import com.oracle.graal.options.OptionUtils.OptionConsumer;
>>>>>>> YOURS

public class HotSpotOptions {

    private static final String GRAAL_OPTION_PREFIX = "-G:";

<<<<<<< MINE
    private static native boolean isCITimingEnabled();

    static {
        if (isCITimingEnabled()) {
            unconditionallyEnableTimerOrMetric(InliningUtil.class, "InlinedBytecodes");
            unconditionallyEnableTimerOrMetric(CompilationTask.class, "CompilationTime");
        }
=======
    private static native void parseVMOptions();

    static {
        parseVMOptions();
>>>>>>> YOURS
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

    static void printFlags() {
        OptionUtils.printFlags(options, GRAAL_OPTION_PREFIX);
    }
<<<<<<< MINE

    private static void unconditionallyEnableTimerOrMetric(Class<?> c, String name) {
        try {
            Field field = c.getDeclaredField(name);
            String propertyName;
            if (DebugTimer.class.isAssignableFrom(field.getType())) {
                propertyName = Debug.ENABLE_TIMER_PROPERTY_NAME_PREFIX + name;
            } else {
                assert DebugMetric.class.isAssignableFrom(field.getType());
                propertyName = Debug.ENABLE_METRIC_PROPERTY_NAME_PREFIX + name;
            }
            String previous = System.setProperty(propertyName, "true");
            if (previous != null) {
                System.err.println("Overrode value \"" + previous + "\" of system property \"" + propertyName + "\" with \"true\"");
            }
        } catch (Exception e) {
            throw new GraalInternalError(e);
        }
    }

    public native Object getOptionValue(String optionName);
=======
>>>>>>> YOURS
}
