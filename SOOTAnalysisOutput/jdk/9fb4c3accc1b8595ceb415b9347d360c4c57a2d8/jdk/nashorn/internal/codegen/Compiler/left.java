package jdk.nashorn.internal.codegen;

import static jdk.nashorn.internal.codegen.CompilerConstants.ARGUMENTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.CALLEE;
import static jdk.nashorn.internal.codegen.CompilerConstants.RETURN;
import static jdk.nashorn.internal.codegen.CompilerConstants.SCOPE;
import static jdk.nashorn.internal.codegen.CompilerConstants.THIS;
import static jdk.nashorn.internal.codegen.CompilerConstants.VARARGS;
import static jdk.nashorn.internal.runtime.logging.DebugLogger.quote;
import java.io.File;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import jdk.internal.dynalink.support.NameCodec;
import jdk.nashorn.internal.codegen.ClassEmitter.Flag;
import jdk.nashorn.internal.codegen.types.Type;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.Optimistic;
import jdk.nashorn.internal.ir.debug.ClassHistogramElement;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import jdk.nashorn.internal.runtime.CodeInstaller;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.RecompilableScriptFunctionData;
import jdk.nashorn.internal.runtime.ScriptEnvironment;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.logging.DebugLogger;
import jdk.nashorn.internal.runtime.logging.Loggable;
import jdk.nashorn.internal.runtime.logging.Logger;

@Logger(name = "compiler")
public final class Compiler implements Loggable {

    public static final String SCRIPTS_PACKAGE = "jdk/nashorn/internal/scripts";

    public static final String OBJECTS_PACKAGE = "jdk/nashorn/internal/objects";

    private final ScriptEnvironment env;

    private final Source source;

    private final String sourceName;

    private final String sourceURL;

    private final boolean optimistic;

    private final Map<String, byte[]> bytecode;

    private final Set<CompileUnit> compileUnits;

    private final ConstantData constantData;

    private final CodeInstaller<ScriptEnvironment> installer;

    private final DebugLogger log;

    private final Context context;

    private final TypeMap types;

    private final TypeEvaluator typeEvaluator;

    private final boolean strict;

    private final boolean onDemand;

    private final Map<Integer, Type> invalidatedProgramPoints;

    private final String firstCompileUnitName;

    private final int[] continuationEntryPoints;

    private RecompilableScriptFunctionData compiledFunction;

    private static boolean initialized = false;

    public static class CompilationPhases implements Iterable<CompilationPhase> {

        public final static CompilationPhases COMPILE_ALL = new CompilationPhases("Compile all", new CompilationPhase[] { CompilationPhase.CONSTANT_FOLDING_PHASE, CompilationPhase.LOWERING_PHASE, CompilationPhase.PROGRAM_POINT_PHASE, CompilationPhase.TRANSFORM_BUILTINS_PHASE, CompilationPhase.SPLITTING_PHASE, CompilationPhase.SYMBOL_ASSIGNMENT_PHASE, CompilationPhase.SCOPE_DEPTH_COMPUTATION_PHASE, CompilationPhase.OPTIMISTIC_TYPE_ASSIGNMENT_PHASE, CompilationPhase.LOCAL_VARIABLE_TYPE_CALCULATION_PHASE, CompilationPhase.BYTECODE_GENERATION_PHASE, CompilationPhase.INSTALL_PHASE });

        public final static CompilationPhases COMPILE_ALL_RESTOF = COMPILE_ALL.setDescription("Compile all, rest of").addAfter(CompilationPhase.LOCAL_VARIABLE_TYPE_CALCULATION_PHASE, CompilationPhase.REUSE_COMPILE_UNITS_PHASE);

        public final static CompilationPhases COMPILE_ALL_NO_INSTALL = COMPILE_ALL.removeLast().setDescription("Compile without install");

        public final static CompilationPhases COMPILE_UPTO_BYTECODE = COMPILE_ALL.removeLast().removeLast().setDescription("Compile upto bytecode");

        public final static CompilationPhases COMPILE_FROM_BYTECODE = new CompilationPhases("Generate bytecode and install", new CompilationPhase[] { CompilationPhase.BYTECODE_GENERATION_PHASE, CompilationPhase.INSTALL_PHASE });

        public final static CompilationPhases COMPILE_FROM_BYTECODE_RESTOF = COMPILE_FROM_BYTECODE.addFirst(CompilationPhase.REUSE_COMPILE_UNITS_PHASE).setDescription("Generate bytecode and install - RestOf method");

        private final List<CompilationPhase> phases;

        private final String desc;

        private CompilationPhases(final String desc, final CompilationPhase... phases) {
            this.desc = desc;
            final List<CompilationPhase> newPhases = new LinkedList<>();
            newPhases.addAll(Arrays.asList(phases));
            this.phases = Collections.unmodifiableList(newPhases);
        }

        @Override
        public String toString() {
            return "'" + desc + "' " + phases.toString();
        }

        private CompilationPhases setDescription(final String desc) {
            return new CompilationPhases(desc, phases.toArray(new CompilationPhase[phases.size()]));
        }

        private CompilationPhases removeLast() {
            final LinkedList<CompilationPhase> list = new LinkedList<>(phases);
            list.removeLast();
            return new CompilationPhases(desc, list.toArray(new CompilationPhase[list.size()]));
        }

        private CompilationPhases addFirst(final CompilationPhase phase) {
            if (phases.contains(phase)) {
                return this;
            }
            final LinkedList<CompilationPhase> list = new LinkedList<>(phases);
            list.addFirst(phase);
            return new CompilationPhases(desc, list.toArray(new CompilationPhase[list.size()]));
        }

        private CompilationPhases addAfter(final CompilationPhase phase, final CompilationPhase newPhase) {
            final LinkedList<CompilationPhase> list = new LinkedList<>();
            for (final CompilationPhase p : phases) {
                list.add(p);
                if (p == phase) {
                    list.add(newPhase);
                }
            }
            return new CompilationPhases(desc, list.toArray(new CompilationPhase[list.size()]));
        }

        boolean contains(final CompilationPhase phase) {
            return phases.contains(phase);
        }

        @Override
        public Iterator<CompilationPhase> iterator() {
            return phases.iterator();
        }

        boolean isRestOfCompilation() {
            return this == COMPILE_ALL_RESTOF || this == COMPILE_FROM_BYTECODE_RESTOF;
        }

        String getDesc() {
            return desc;
        }

        String toString(final String prefix) {
            final StringBuilder sb = new StringBuilder();
            for (final CompilationPhase phase : phases) {
                sb.append(prefix).append(phase).append('\n');
            }
            return sb.toString();
        }
    }

    private static String[] RESERVED_NAMES = { SCOPE.symbolName(), THIS.symbolName(), RETURN.symbolName(), CALLEE.symbolName(), VARARGS.symbolName(), ARGUMENTS.symbolName() };

    private final int compilationId = COMPILATION_ID.getAndIncrement();

    private final AtomicInteger nextCompileUnitId = new AtomicInteger(0);

    private static final AtomicInteger COMPILATION_ID = new AtomicInteger(0);

    public Compiler(final Context context, final ScriptEnvironment env, final CodeInstaller<ScriptEnvironment> installer, final Source source, final String sourceURL, final boolean isStrict) {
        this(context, env, installer, source, sourceURL, isStrict, false, null, null, null, null, null);
    }

    public Compiler(final Context context, final ScriptEnvironment env, final CodeInstaller<ScriptEnvironment> installer, final Source source, final String sourceURL, final boolean isStrict, final boolean isOnDemand, final RecompilableScriptFunctionData compiledFunction, final TypeMap types, final Map<Integer, Type> invalidatedProgramPoints, final int[] continuationEntryPoints, final ScriptObject runtimeScope) {
        this.context = context;
        this.env = env;
        this.installer = installer;
        this.constantData = new ConstantData();
        this.compileUnits = CompileUnit.createCompileUnitSet();
        this.bytecode = new LinkedHashMap<>();
        this.log = initLogger(context);
        this.source = source;
        this.sourceURL = sourceURL;
        this.sourceName = FunctionNode.getSourceName(source, sourceURL);
        this.onDemand = isOnDemand;
        this.compiledFunction = compiledFunction;
        this.types = types;
        this.invalidatedProgramPoints = invalidatedProgramPoints == null ? new HashMap<Integer, Type>() : invalidatedProgramPoints;
        this.continuationEntryPoints = continuationEntryPoints == null ? null : continuationEntryPoints.clone();
        this.typeEvaluator = new TypeEvaluator(this, runtimeScope);
        this.firstCompileUnitName = firstCompileUnitName();
        this.strict = isStrict;
        if (!initialized) {
            initialized = true;
            if (!ScriptEnvironment.globalOptimistic()) {
                log.warning("Running without optimistic types. This is a configuration that may be deprecated.");
            }
        }
        this.optimistic = ScriptEnvironment.globalOptimistic();
    }

    private static String safeSourceName(final ScriptEnvironment env, final CodeInstaller<ScriptEnvironment> installer, final Source source) {
        String baseName = new File(source.getName()).getName();
        final int index = baseName.lastIndexOf(".js");
        if (index != -1) {
            baseName = baseName.substring(0, index);
        }
        baseName = baseName.replace('.', '_').replace('-', '_');
        if (!env._loader_per_compile) {
            baseName = baseName + installer.getUniqueScriptId();
        }
        final String mangled = NameCodec.encode(baseName);
        return mangled != null ? mangled : baseName;
    }

    private String firstCompileUnitName() {
        final StringBuilder sb = new StringBuilder(SCRIPTS_PACKAGE).append('/').append(CompilerConstants.DEFAULT_SCRIPT_NAME.symbolName()).append('$');
        if (isOnDemandCompilation()) {
            sb.append(RecompilableScriptFunctionData.RECOMPILATION_PREFIX);
        }
        if (compilationId > 0) {
            sb.append(compilationId).append('$');
        }
        sb.append(Compiler.safeSourceName(env, installer, source));
        return sb.toString();
    }

    void declareLocalSymbol(final String symbolName) {
        typeEvaluator.declareLocalSymbol(symbolName);
    }

    void setData(final RecompilableScriptFunctionData data) {
        assert this.compiledFunction == null : data;
        this.compiledFunction = data;
    }

    @Override
    public DebugLogger getLogger() {
        return log;
    }

    @Override
    public DebugLogger initLogger(final Context ctxt) {
        return ctxt.getLogger(this.getClass());
    }

    ScriptEnvironment getScriptEnvironment() {
        return env;
    }

    boolean isOnDemandCompilation() {
        return onDemand;
    }

    boolean useOptimisticTypes() {
        return optimistic;
    }

    Context getContext() {
        return context;
    }

    Type getOptimisticType(final Optimistic node) {
        return typeEvaluator.getOptimisticType(node);
    }

    void addInvalidatedProgramPoint(final int programPoint, final Type type) {
        invalidatedProgramPoints.put(programPoint, type);
    }

    TypeMap getTypeMap() {
        return types;
    }

    MethodType getCallSiteType(final FunctionNode fn) {
        if (types == null || !isOnDemandCompilation()) {
            return null;
        }
        return types.getCallSiteType(fn);
    }

    Type getParamType(final FunctionNode fn, final int pos) {
        return types == null ? null : types.get(fn, pos);
    }

    public FunctionNode compile(final FunctionNode functionNode, final CompilationPhases phases) throws CompilationException {
        log.info("Starting compile job for ", DebugLogger.quote(functionNode.getName()), " phases=", quote(phases.getDesc()));
        log.indent();
        final String name = DebugLogger.quote(functionNode.getName());
        FunctionNode newFunctionNode = functionNode;
        for (final String reservedName : RESERVED_NAMES) {
            newFunctionNode.uniqueName(reservedName);
        }
        final boolean info = log.levelFinerThanOrEqual(Level.INFO);
        final DebugLogger timeLogger = env.isTimingEnabled() ? env._timing.getLogger() : null;
        long time = 0L;
        for (final CompilationPhase phase : phases) {
            log.fine(phase, " starting for ", quote(name));
            newFunctionNode = phase.apply(this, phases, newFunctionNode);
            log.fine(phase, " done for function ", quote(name));
            if (env._print_mem_usage) {
                printMemoryUsage(functionNode, phase.toString());
            }
            time += (env.isTimingEnabled() ? phase.getEndTime() - phase.getStartTime() : 0L);
        }
        log.unindent();
        if (info) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Compile job for ").append(newFunctionNode.getSource()).append(':').append(quote(newFunctionNode.getName())).append(" finished");
            if (time > 0L && timeLogger != null) {
                assert env.isTimingEnabled();
                sb.append(" in ").append(time).append(" ms");
            }
            log.info(sb);
        }
        return newFunctionNode;
    }

    Source getSource() {
        return source;
    }

    Map<String, byte[]> getBytecode() {
        return Collections.unmodifiableMap(bytecode);
    }

    byte[] getBytecode(final String className) {
        return bytecode.get(className);
    }

    CompileUnit getFirstCompileUnit() {
        assert !compileUnits.isEmpty();
        return compileUnits.iterator().next();
    }

    Set<CompileUnit> getCompileUnits() {
        return compileUnits;
    }

    ConstantData getConstantData() {
        return constantData;
    }

    CodeInstaller<ScriptEnvironment> getCodeInstaller() {
        return installer;
    }

    void addClass(final String name, final byte[] code) {
        bytecode.put(name, code);
    }

    void removeClass(final String name) {
        assert bytecode.get(name) != null;
        bytecode.remove(name);
    }

    String getSourceURL() {
        return sourceURL;
    }

    String nextCompileUnitName() {
        final StringBuilder sb = new StringBuilder(firstCompileUnitName);
        final int cuid = nextCompileUnitId.getAndIncrement();
        if (cuid > 0) {
            sb.append("$cu").append(cuid);
        }
        return sb.toString();
    }

    void clearCompileUnits() {
        compileUnits.clear();
    }

    CompileUnit addCompileUnit(final long initialWeight) {
        final CompileUnit compileUnit = createCompileUnit(initialWeight);
        compileUnits.add(compileUnit);
        log.fine("Added compile unit ", compileUnit);
        return compileUnit;
    }

    CompileUnit createCompileUnit(final String unitClassName, final long initialWeight) {
        final ClassEmitter classEmitter = new ClassEmitter(context, sourceName, unitClassName, isStrict());
        final CompileUnit compileUnit = new CompileUnit(unitClassName, classEmitter, initialWeight);
        classEmitter.begin();
        final MethodEmitter initMethod = classEmitter.init(EnumSet.of(Flag.PRIVATE));
        initMethod.begin();
        initMethod.load(Type.OBJECT, 0);
        initMethod.newInstance(jdk.nashorn.internal.scripts.JS.class);
        initMethod.returnVoid();
        initMethod.end();
        return compileUnit;
    }

    private CompileUnit createCompileUnit(final long initialWeight) {
        return createCompileUnit(nextCompileUnitName(), initialWeight);
    }

    boolean isStrict() {
        return strict;
    }

    void replaceCompileUnits(final Set<CompileUnit> newUnits) {
        compileUnits.clear();
        compileUnits.addAll(newUnits);
    }

    CompileUnit findUnit(final long weight) {
        for (final CompileUnit unit : compileUnits) {
            if (unit.canHold(weight)) {
                unit.addWeight(weight);
                return unit;
            }
        }
        return addCompileUnit(weight);
    }

    public static String binaryName(final String name) {
        return name.replace('/', '.');
    }

    RecompilableScriptFunctionData getProgram() {
        if (compiledFunction == null) {
            return null;
        }
        return compiledFunction.getProgram();
    }

    RecompilableScriptFunctionData getScriptFunctionData(final int functionId) {
        return compiledFunction == null ? null : compiledFunction.getScriptFunctionData(functionId);
    }

    boolean isGlobalSymbol(final FunctionNode fn, final String name) {
        return getScriptFunctionData(fn.getId()).isGlobalSymbol(fn, name);
    }

    int[] getContinuationEntryPoints() {
        return continuationEntryPoints;
    }

    Type getInvalidatedProgramPointType(final int programPoint) {
        return invalidatedProgramPoints.get(programPoint);
    }

    private void printMemoryUsage(final FunctionNode functionNode, final String phaseName) {
        if (!log.isEnabled()) {
            return;
        }
        log.info(phaseName, "finished. Doing IR size calculation...");
        final ObjectSizeCalculator osc = new ObjectSizeCalculator(ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification());
        osc.calculateObjectSize(functionNode);
        final List<ClassHistogramElement> list = osc.getClassHistogram();
        final StringBuilder sb = new StringBuilder();
        final long totalSize = osc.calculateObjectSize(functionNode);
        sb.append(phaseName).append(" Total size = ").append(totalSize / 1024 / 1024).append("MB");
        log.info(sb);
        Collections.sort(list, new Comparator<ClassHistogramElement>() {

            @Override
            public int compare(final ClassHistogramElement o1, final ClassHistogramElement o2) {
                final long diff = o1.getBytes() - o2.getBytes();
                if (diff < 0) {
                    return 1;
                } else if (diff > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (final ClassHistogramElement e : list) {
            final String line = String.format("    %-48s %10d bytes (%8d instances)", e.getClazz(), e.getBytes(), e.getInstances());
            log.info(line);
            if (e.getBytes() < totalSize / 200) {
                log.info("    ...");
                break;
            }
        }
    }
}
