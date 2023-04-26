package com.oracle.graal.hotspot;

import static com.oracle.graal.compiler.GraalDebugConfig.*;
import static com.oracle.graal.options.OptionsLoader.*;
import com.oracle.graal.api.runtime.*;
import com.oracle.graal.debug.*;
import com.oracle.graal.options.*;

public class HotSpotOptions {

    private static final String GRAAL_OPTION_PREFIX = "-G:";

<<<<<<< MINE
private static native boolean isCITimingEnabled();
=======
private static native void parseVMOptions();
>>>>>>> YOURS


    static {
<<<<<<< MINE
        if (isCITimingEnabled()) {
            unconditionallyEnableTimerOrMetric(InliningUtil.class, "InlinedBytecodes");
            unconditionallyEnableTimerOrMetric(CompilationTask.class, "CompilationTime");
        }
=======
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

    public native Object getOptionValue(String optionName);
}