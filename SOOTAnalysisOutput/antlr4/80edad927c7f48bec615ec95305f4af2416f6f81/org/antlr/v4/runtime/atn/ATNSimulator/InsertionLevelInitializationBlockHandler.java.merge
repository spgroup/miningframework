package org.antlr.v4.runtime.atn;

import org.antlr.v4.runtime.dfa.DFAState;
import org.antlr.v4.runtime.misc.IntervalSet;
import java.util.IdentityHashMap;
import java.util.List;

public abstract class ATNSimulator {

    public static final DFAState ERROR;

    public final ATN atn;

    protected final PredictionContextCache sharedContextCache;

    static {
        ERROR = new DFAState(new ATNConfigSet());
        ERROR.stateNumber = Integer.MAX_VALUE;
    }

    public ATNSimulator(ATN atn, PredictionContextCache sharedContextCache) {
        this.atn = atn;
        this.sharedContextCache = sharedContextCache;
    }

    public abstract void reset();

    public void clearDFA() {
        throw new UnsupportedOperationException("This ATN simulator does not support clearing the DFA.");
    }

    public PredictionContextCache getSharedContextCache() {
        return sharedContextCache;
    }

    public PredictionContext getCachedContext(PredictionContext context) {
        if (sharedContextCache == null)
            return context;
        synchronized (sharedContextCache) {
            IdentityHashMap<PredictionContext, PredictionContext> visited = new IdentityHashMap<PredictionContext, PredictionContext>();
            return PredictionContext.getCachedContext(context, sharedContextCache, visited);
        }
    }
}