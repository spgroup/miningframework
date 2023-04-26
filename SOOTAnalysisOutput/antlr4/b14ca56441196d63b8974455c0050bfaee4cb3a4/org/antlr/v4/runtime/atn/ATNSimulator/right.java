package org.antlr.v4.runtime.atn;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.dfa.DFAState;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Pair;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class ATNSimulator {

    public static final int SERIALIZED_VERSION;

    static {
        SERIALIZED_VERSION = 3;
    }

    public static final UUID SERIALIZED_UUID;

    static {
        SERIALIZED_UUID = UUID.fromString("33761B2D-78BB-4A43-8B0B-4F5BEE8AACF3");
    }

    @NotNull
    public static final DFAState ERROR;

    @NotNull
    public final ATN atn;

    protected final PredictionContextCache sharedContextCache;

    static {
        ERROR = new DFAState(new ATNConfigSet());
        ERROR.stateNumber = Integer.MAX_VALUE;
    }

    public ATNSimulator(@NotNull ATN atn, @NotNull PredictionContextCache sharedContextCache) {
        this.atn = atn;
        this.sharedContextCache = sharedContextCache;
    }

    public abstract void reset();

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

    public static ATN deserialize(@NotNull char[] data) {
        data = data.clone();
        for (int i = 1; i < data.length; i++) {
            data[i] = (char) (data[i] - 2);
        }
        int p = 0;
        int version = toInt(data[p++]);
        if (version != SERIALIZED_VERSION) {
            String reason = String.format(Locale.getDefault(), "Could not deserialize ATN with version %d (expected %d).", version, SERIALIZED_VERSION);
            throw new UnsupportedOperationException(new InvalidClassException(ATN.class.getName(), reason));
        }
        UUID uuid = toUUID(data, p);
        p += 8;
        if (!uuid.equals(SERIALIZED_UUID)) {
            String reason = String.format(Locale.getDefault(), "Could not deserialize ATN with UUID %s (expected %s).", uuid, SERIALIZED_UUID);
            throw new UnsupportedOperationException(new InvalidClassException(ATN.class.getName(), reason));
        }
        ATNType grammarType = ATNType.values()[toInt(data[p++])];
        int maxTokenType = toInt(data[p++]);
        ATN atn = new ATN(grammarType, maxTokenType);
        List<Pair<LoopEndState, Integer>> loopBackStateNumbers = new ArrayList<Pair<LoopEndState, Integer>>();
        List<Pair<BlockStartState, Integer>> endStateNumbers = new ArrayList<Pair<BlockStartState, Integer>>();
        int nstates = toInt(data[p++]);
        for (int i = 0; i < nstates; i++) {
            int stype = toInt(data[p++]);
            if (stype == ATNState.INVALID_TYPE) {
                atn.addState(null);
                continue;
            }
            int ruleIndex = toInt(data[p++]);
            if (ruleIndex == Character.MAX_VALUE) {
                ruleIndex = -1;
            }
            ATNState s = stateFactory(stype, ruleIndex);
            if (stype == ATNState.LOOP_END) {
                int loopBackStateNumber = toInt(data[p++]);
                loopBackStateNumbers.add(new Pair<LoopEndState, Integer>((LoopEndState) s, loopBackStateNumber));
            } else if (s instanceof BlockStartState) {
                int endStateNumber = toInt(data[p++]);
                endStateNumbers.add(new Pair<BlockStartState, Integer>((BlockStartState) s, endStateNumber));
            }
            atn.addState(s);
        }
        for (Pair<LoopEndState, Integer> pair : loopBackStateNumbers) {
            pair.a.loopBackState = atn.states.get(pair.b);
        }
        for (Pair<BlockStartState, Integer> pair : endStateNumbers) {
            pair.a.endState = (BlockEndState) atn.states.get(pair.b);
        }
        int numNonGreedyStates = toInt(data[p++]);
        for (int i = 0; i < numNonGreedyStates; i++) {
            int stateNumber = toInt(data[p++]);
            ((DecisionState) atn.states.get(stateNumber)).nonGreedy = true;
        }
        int nrules = toInt(data[p++]);
        if (atn.grammarType == ATNType.LEXER) {
            atn.ruleToTokenType = new int[nrules];
            atn.ruleToActionIndex = new int[nrules];
        }
        atn.ruleToStartState = new RuleStartState[nrules];
        for (int i = 0; i < nrules; i++) {
            int s = toInt(data[p++]);
            RuleStartState startState = (RuleStartState) atn.states.get(s);
            atn.ruleToStartState[i] = startState;
            if (atn.grammarType == ATNType.LEXER) {
                int tokenType = toInt(data[p++]);
                if (tokenType == 0xFFFF) {
                    tokenType = Token.EOF;
                }
                atn.ruleToTokenType[i] = tokenType;
                int actionIndex = toInt(data[p++]);
                if (actionIndex == 0xFFFF) {
                    actionIndex = -1;
                }
                atn.ruleToActionIndex[i] = actionIndex;
            }
        }
        atn.ruleToStopState = new RuleStopState[nrules];
        for (ATNState state : atn.states) {
            if (!(state instanceof RuleStopState)) {
                continue;
            }
            RuleStopState stopState = (RuleStopState) state;
            atn.ruleToStopState[state.ruleIndex] = stopState;
            atn.ruleToStartState[state.ruleIndex].stopState = stopState;
        }
        int nmodes = toInt(data[p++]);
        for (int i = 0; i < nmodes; i++) {
            int s = toInt(data[p++]);
            atn.modeToStartState.add((TokensStartState) atn.states.get(s));
        }
        List<IntervalSet> sets = new ArrayList<IntervalSet>();
        int nsets = toInt(data[p++]);
        for (int i = 0; i < nsets; i++) {
            int nintervals = toInt(data[p]);
            p++;
            IntervalSet set = new IntervalSet();
            sets.add(set);
            boolean containsEof = toInt(data[p++]) != 0;
            if (containsEof) {
                set.add(-1);
            }
            for (int j = 0; j < nintervals; j++) {
                set.add(toInt(data[p]), toInt(data[p + 1]));
                p += 2;
            }
        }
        int nedges = toInt(data[p++]);
        for (int i = 0; i < nedges; i++) {
            int src = toInt(data[p]);
            int trg = toInt(data[p + 1]);
            int ttype = toInt(data[p + 2]);
            int arg1 = toInt(data[p + 3]);
            int arg2 = toInt(data[p + 4]);
            int arg3 = toInt(data[p + 5]);
            Transition trans = edgeFactory(atn, ttype, src, trg, arg1, arg2, arg3, sets);
            ATNState srcState = atn.states.get(src);
            srcState.addTransition(trans);
            p += 6;
        }
        for (ATNState state : atn.states) {
            for (int i = 0; i < state.getNumberOfTransitions(); i++) {
                Transition t = state.transition(i);
                if (!(t instanceof RuleTransition)) {
                    continue;
                }
                RuleTransition ruleTransition = (RuleTransition) t;
                atn.ruleToStopState[ruleTransition.target.ruleIndex].addTransition(new EpsilonTransition(ruleTransition.followState));
            }
        }
        for (ATNState state : atn.states) {
            if (state instanceof BlockStartState) {
                if (((BlockStartState) state).endState == null) {
                    throw new IllegalStateException();
                }
                if (((BlockStartState) state).endState.startState != null) {
                    throw new IllegalStateException();
                }
                ((BlockStartState) state).endState.startState = (BlockStartState) state;
            }
            if (state instanceof PlusLoopbackState) {
                PlusLoopbackState loopbackState = (PlusLoopbackState) state;
                for (int i = 0; i < loopbackState.getNumberOfTransitions(); i++) {
                    ATNState target = loopbackState.transition(i).target;
                    if (target instanceof PlusBlockStartState) {
                        ((PlusBlockStartState) target).loopBackState = loopbackState;
                    }
                }
            } else if (state instanceof StarLoopbackState) {
                StarLoopbackState loopbackState = (StarLoopbackState) state;
                for (int i = 0; i < loopbackState.getNumberOfTransitions(); i++) {
                    ATNState target = loopbackState.transition(i).target;
                    if (target instanceof StarLoopEntryState) {
                        ((StarLoopEntryState) target).loopBackState = loopbackState;
                    }
                }
            }
        }
        int ndecisions = toInt(data[p++]);
        for (int i = 1; i <= ndecisions; i++) {
            int s = toInt(data[p++]);
            DecisionState decState = (DecisionState) atn.states.get(s);
            atn.decisionToState.add(decState);
            decState.decision = i - 1;
        }
        verifyATN(atn);
        return atn;
    }

    private static void verifyATN(ATN atn) {
        for (ATNState state : atn.states) {
            if (state == null) {
                continue;
            }
            checkCondition(state.onlyHasEpsilonTransitions() || state.getNumberOfTransitions() <= 1);
            if (state instanceof PlusBlockStartState) {
                checkCondition(((PlusBlockStartState) state).loopBackState != null);
            }
            if (state instanceof StarLoopEntryState) {
                StarLoopEntryState starLoopEntryState = (StarLoopEntryState) state;
                checkCondition(starLoopEntryState.loopBackState != null);
                checkCondition(starLoopEntryState.getNumberOfTransitions() == 2);
                if (starLoopEntryState.transition(0).target instanceof StarBlockStartState) {
                    checkCondition(starLoopEntryState.transition(1).target instanceof LoopEndState);
                    checkCondition(!starLoopEntryState.nonGreedy);
                } else if (starLoopEntryState.transition(0).target instanceof LoopEndState) {
                    checkCondition(starLoopEntryState.transition(1).target instanceof StarBlockStartState);
                    checkCondition(starLoopEntryState.nonGreedy);
                } else {
                    throw new IllegalStateException();
                }
            }
            if (state instanceof StarLoopbackState) {
                checkCondition(state.getNumberOfTransitions() == 1);
                checkCondition(state.transition(0).target instanceof StarLoopEntryState);
            }
            if (state instanceof LoopEndState) {
                checkCondition(((LoopEndState) state).loopBackState != null);
            }
            if (state instanceof RuleStartState) {
                checkCondition(((RuleStartState) state).stopState != null);
            }
            if (state instanceof BlockStartState) {
                checkCondition(((BlockStartState) state).endState != null);
            }
            if (state instanceof BlockEndState) {
                checkCondition(((BlockEndState) state).startState != null);
            }
            if (state instanceof DecisionState) {
                DecisionState decisionState = (DecisionState) state;
                checkCondition(decisionState.getNumberOfTransitions() <= 1 || decisionState.decision >= 0);
            } else {
                checkCondition(state.getNumberOfTransitions() <= 1 || state instanceof RuleStopState);
            }
        }
    }

    public static void checkCondition(boolean condition) {
        checkCondition(condition, null);
    }

    public static void checkCondition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static int toInt(char c) {
        return c;
    }

    public static int toInt32(char[] data, int offset) {
        return (int) data[offset] | ((int) data[offset + 1] << 16);
    }

    public static long toLong(char[] data, int offset) {
        long lowOrder = toInt32(data, offset) & 0x00000000FFFFFFFFL;
        return lowOrder | ((long) toInt32(data, offset + 2) << 32);
    }

    public static UUID toUUID(char[] data, int offset) {
        long leastSigBits = toLong(data, offset);
        long mostSigBits = toLong(data, offset + 4);
        return new UUID(mostSigBits, leastSigBits);
    }

    @NotNull
    public static Transition edgeFactory(@NotNull ATN atn, int type, int src, int trg, int arg1, int arg2, int arg3, List<IntervalSet> sets) {
        ATNState target = atn.states.get(trg);
        switch(type) {
            case Transition.EPSILON:
                return new EpsilonTransition(target);
            case Transition.RANGE:
                if (arg3 != 0) {
                    return new RangeTransition(target, Token.EOF, arg2);
                } else {
                    return new RangeTransition(target, arg1, arg2);
                }
            case Transition.RULE:
                RuleTransition rt = new RuleTransition((RuleStartState) atn.states.get(arg1), arg2, target);
                return rt;
            case Transition.PREDICATE:
                PredicateTransition pt = new PredicateTransition(target, arg1, arg2, arg3 != 0);
                return pt;
            case Transition.ATOM:
                if (arg3 != 0) {
                    return new AtomTransition(target, Token.EOF);
                } else {
                    return new AtomTransition(target, arg1);
                }
            case Transition.ACTION:
                ActionTransition a = new ActionTransition(target, arg1, arg2, arg3 != 0);
                return a;
            case Transition.SET:
                return new SetTransition(target, sets.get(arg1));
            case Transition.NOT_SET:
                return new NotSetTransition(target, sets.get(arg1));
            case Transition.WILDCARD:
                return new WildcardTransition(target);
        }
        throw new IllegalArgumentException("The specified transition type is not valid.");
    }

    public static ATNState stateFactory(int type, int ruleIndex) {
        ATNState s;
        switch(type) {
            case ATNState.INVALID_TYPE:
                return null;
            case ATNState.BASIC:
                s = new BasicState();
                break;
            case ATNState.RULE_START:
                s = new RuleStartState();
                break;
            case ATNState.BLOCK_START:
                s = new BasicBlockStartState();
                break;
            case ATNState.PLUS_BLOCK_START:
                s = new PlusBlockStartState();
                break;
            case ATNState.STAR_BLOCK_START:
                s = new StarBlockStartState();
                break;
            case ATNState.TOKEN_START:
                s = new TokensStartState();
                break;
            case ATNState.RULE_STOP:
                s = new RuleStopState();
                break;
            case ATNState.BLOCK_END:
                s = new BlockEndState();
                break;
            case ATNState.STAR_LOOP_BACK:
                s = new StarLoopbackState();
                break;
            case ATNState.STAR_LOOP_ENTRY:
                s = new StarLoopEntryState();
                break;
            case ATNState.PLUS_LOOP_BACK:
                s = new PlusLoopbackState();
                break;
            case ATNState.LOOP_END:
                s = new LoopEndState();
                break;
            default:
                String message = String.format(Locale.getDefault(), "The specified state type %d is not valid.", type);
                throw new IllegalArgumentException(message);
        }
        s.ruleIndex = ruleIndex;
        return s;
    }
}
