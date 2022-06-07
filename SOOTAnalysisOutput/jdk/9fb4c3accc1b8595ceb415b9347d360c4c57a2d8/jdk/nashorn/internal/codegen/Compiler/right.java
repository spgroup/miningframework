package jdk.nashorn.internal.codegen;

import static jdk.nashorn.internal.codegen.CompilerConstants.ARGUMENTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.CALLEE;
import static jdk.nashorn.internal.codegen.CompilerConstants.CONSTANTS;
import static jdk.nashorn.internal.codegen.CompilerConstants.DEFAULT_SCRIPT_NAME;
import static jdk.nashorn.internal.codegen.CompilerConstants.LAZY;
import static jdk.nashorn.internal.codegen.CompilerConstants.RETURN;
import static jdk.nashorn.internal.codegen.CompilerConstants.SCOPE;
import static jdk.nashorn.internal.codegen.CompilerConstants.SOURCE;
import static jdk.nashorn.internal.codegen.CompilerConstants.THIS;
import static jdk.nashorn.internal.codegen.CompilerConstants.VARARGS;
import java.io.File;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import jdk.internal.dynalink.support.NameCodec;
import jdk.nashorn.internal.codegen.ClassEmitter.Flag;
import jdk.nashorn.internal.codegen.types.Type;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.FunctionNode.CompilationState;
import jdk.nashorn.internal.ir.TemporarySymbols;
import jdk.nashorn.internal.ir.debug.ClassHistogramElement;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import jdk.nashorn.internal.runtime.CodeInstaller;
import jdk.nashorn.internal.runtime.DebugLogger;
import jdk.nashorn.internal.runtime.ScriptEnvironment;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.Timing;
import jdk.nashorn.internal.runtime.options.Options;

public final class Compiler {

    public static final String SCRIPTS_PACKAGE = "jdk/nashorn/internal/scripts";

    public static final String OBJECTS_PACKAGE = "jdk/nashorn/internal/objects";

    private Source source;

    private String sourceName;

    private final Map<String, byte[]> bytecode;

    private final Set<CompileUnit> compileUnits;

    private final ConstantData constantData;

    private final CompilationSequence sequence;

    private final ScriptEnvironment env;

    private String scriptName;

    private boolean strict;

    private final CodeInstaller<ScriptEnvironment> installer;

    private final TemporarySymbols temporarySymbols = new TemporarySymbols();

    public static final DebugLogger LOG = new DebugLogger("compiler");

    private static String[] RESERVED_NAMES = { SCOPE.symbolName(), THIS.symbolName(), RETURN.symbolName(), CALLEE.symbolName(), VARARGS.symbolName(), ARGUMENTS.symbolName() };

    @SuppressWarnings("serial")
    static class CompilationSequence extends LinkedList<CompilationPhase> {

        CompilationSequence(final CompilationPhase... phases) {
            super(Arrays.asList(phases));
        }

        CompilationSequence(final CompilationSequence sequence) {
            this(sequence.toArray(new CompilationPhase[sequence.size()]));
        }

        CompilationSequence insertAfter(final CompilationPhase phase, final CompilationPhase newPhase) {
            final CompilationSequence newSeq = new CompilationSequence();
            for (final CompilationPhase elem : this) {
                newSeq.add(phase);
                if (elem.equals(phase)) {
                    newSeq.add(newPhase);
                }
            }
            assert newSeq.contains(newPhase);
            return newSeq;
        }

        CompilationSequence insertBefore(final CompilationPhase phase, final CompilationPhase newPhase) {
            final CompilationSequence newSeq = new CompilationSequence();
            for (final CompilationPhase elem : this) {
                if (elem.equals(phase)) {
                    newSeq.add(newPhase);
                }
                newSeq.add(phase);
            }
            assert newSeq.contains(newPhase);
            return newSeq;
        }

        CompilationSequence insertFirst(final CompilationPhase phase) {
            final CompilationSequence newSeq = new CompilationSequence(this);
            newSeq.addFirst(phase);
            return newSeq;
        }

        CompilationSequence insertLast(final CompilationPhase phase) {
            final CompilationSequence newSeq = new CompilationSequence(this);
            newSeq.addLast(phase);
            return newSeq;
        }
    }

    public static class Hints {

        private final Type[] paramTypes;

        public static final Hints EMPTY = new Hints();

        private Hints() {
            this.paramTypes = null;
        }

        public Hints(final Type[] paramTypes) {
            this.paramTypes = paramTypes;
        }

        public Type getParameterType(final int pos) {
            if (paramTypes != null && pos < paramTypes.length) {
                return paramTypes[pos];
            }
            return null;
        }
    }

    final static CompilationSequence SEQUENCE_EAGER = new CompilationSequence(CompilationPhase.CONSTANT_FOLDING_PHASE, CompilationPhase.LOWERING_PHASE, CompilationPhase.ATTRIBUTION_PHASE, CompilationPhase.RANGE_ANALYSIS_PHASE, CompilationPhase.SPLITTING_PHASE, CompilationPhase.TYPE_FINALIZATION_PHASE, CompilationPhase.BYTECODE_GENERATION_PHASE);

    final static CompilationSequence SEQUENCE_LAZY = SEQUENCE_EAGER.insertFirst(CompilationPhase.LAZY_INITIALIZATION_PHASE);

    private static CompilationSequence sequence(final boolean lazy) {
        return lazy ? SEQUENCE_LAZY : SEQUENCE_EAGER;
    }

    boolean isLazy() {
        return sequence == SEQUENCE_LAZY;
    }

    private static String lazyTag(final FunctionNode functionNode) {
        if (functionNode.isLazy()) {
            return '$' + LAZY.symbolName() + '$' + functionNode.getName();
        }
        return "";
    }

    Compiler(final ScriptEnvironment env, final CodeInstaller<ScriptEnvironment> installer, final CompilationSequence sequence, final boolean strict) {
        this.env = env;
        this.sequence = sequence;
        this.installer = installer;
        this.constantData = new ConstantData();
        this.compileUnits = new TreeSet<>();
        this.bytecode = new LinkedHashMap<>();
    }

    private void initCompiler(final FunctionNode functionNode) {
        this.strict = strict || functionNode.isStrict();
        final StringBuilder sb = new StringBuilder();
        sb.append(functionNode.uniqueName(DEFAULT_SCRIPT_NAME.symbolName() + lazyTag(functionNode))).append('$').append(safeSourceName(functionNode.getSource()));
        this.source = functionNode.getSource();
        this.sourceName = functionNode.getSourceName();
        this.scriptName = sb.toString();
    }

    public Compiler(final CodeInstaller<ScriptEnvironment> installer, final boolean strict) {
        this(installer.getOwner(), installer, sequence(installer.getOwner()._lazy_compilation), strict);
    }

    public Compiler(final CodeInstaller<ScriptEnvironment> installer) {
        this(installer.getOwner(), installer, sequence(installer.getOwner()._lazy_compilation), installer.getOwner()._strict);
    }

    public Compiler(final ScriptEnvironment env) {
        this(env, null, sequence(env._lazy_compilation), env._strict);
    }

    private static void printMemoryUsage(final String phaseName, final FunctionNode functionNode) {
        LOG.info(phaseName + " finished. Doing IR size calculation...");
        final ObjectSizeCalculator osc = new ObjectSizeCalculator(ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification());
        osc.calculateObjectSize(functionNode);
        final List<ClassHistogramElement> list = osc.getClassHistogram();
        final StringBuilder sb = new StringBuilder();
        final long totalSize = osc.calculateObjectSize(functionNode);
        sb.append(phaseName).append(" Total size = ").append(totalSize / 1024 / 1024).append("MB");
        LOG.info(sb);
        Collections.sort(list, new Comparator<ClassHistogramElement>() {

            @Override
            public int compare(ClassHistogramElement o1, ClassHistogramElement o2) {
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
            LOG.info(line);
            if (e.getBytes() < totalSize / 200) {
                LOG.info("    ...");
                break;
            }
        }
    }

    public FunctionNode compile(final FunctionNode functionNode) throws CompilationException {
        FunctionNode newFunctionNode = functionNode;
        initCompiler(newFunctionNode);
        for (final String reservedName : RESERVED_NAMES) {
            newFunctionNode.uniqueName(reservedName);
        }
        final boolean fine = !LOG.levelAbove(Level.FINE);
        final boolean info = !LOG.levelAbove(Level.INFO);
        long time = 0L;
        for (final CompilationPhase phase : sequence) {
            newFunctionNode = phase.apply(this, newFunctionNode);
            if (env._print_mem_usage) {
                printMemoryUsage(phase.toString(), newFunctionNode);
            }
            final long duration = Timing.isEnabled() ? (phase.getEndTime() - phase.getStartTime()) : 0L;
            time += duration;
            if (fine) {
                final StringBuilder sb = new StringBuilder();
                sb.append(phase.toString()).append(" done for function '").append(newFunctionNode.getName()).append('\'');
                if (duration > 0L) {
                    sb.append(" in ").append(duration).append(" ms ");
                }
                LOG.fine(sb);
            }
        }
        if (info) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Compile job for '").append(newFunctionNode.getSource()).append(':').append(newFunctionNode.getName()).append("' finished");
            if (time > 0L) {
                sb.append(" in ").append(time).append(" ms");
            }
            LOG.info(sb);
        }
        return newFunctionNode;
    }

    private Class<?> install(final String className, final byte[] code, final Object[] constants) {
        return installer.install(className, code, source, constants);
    }

    public Class<?> install(final FunctionNode functionNode) {
        final long t0 = Timing.isEnabled() ? System.currentTimeMillis() : 0L;
        assert functionNode.hasState(CompilationState.EMITTED) : functionNode.getName() + " has no bytecode and cannot be installed";
        final Map<String, Class<?>> installedClasses = new HashMap<>();
        final Object[] constants = getConstantData().toArray();
        final String rootClassName = firstCompileUnitName();
        final byte[] rootByteCode = bytecode.get(rootClassName);
        final Class<?> rootClass = install(rootClassName, rootByteCode, constants);
        if (!isLazy()) {
            installer.storeCompiledScript(source, rootClassName, bytecode, constants);
        }
        int length = rootByteCode.length;
        installedClasses.put(rootClassName, rootClass);
        for (final Entry<String, byte[]> entry : bytecode.entrySet()) {
            final String className = entry.getKey();
            if (className.equals(rootClassName)) {
                continue;
            }
            final byte[] code = entry.getValue();
            length += code.length;
            installedClasses.put(className, install(className, code, constants));
        }
        for (final CompileUnit unit : compileUnits) {
            unit.setCode(installedClasses.get(unit.getUnitClassName()));
        }
        final StringBuilder sb;
        if (LOG.isEnabled()) {
            sb = new StringBuilder();
            sb.append("Installed class '").append(rootClass.getSimpleName()).append('\'').append(" bytes=").append(length).append('.');
            if (bytecode.size() > 1) {
                sb.append(' ').append(bytecode.size()).append(" compile units.");
            }
        } else {
            sb = null;
        }
        if (Timing.isEnabled()) {
            final long duration = System.currentTimeMillis() - t0;
            Timing.accumulateTime("[Code Installation]", duration);
            if (sb != null) {
                sb.append(" Install time: ").append(duration).append(" ms");
            }
        }
        if (sb != null) {
            LOG.fine(sb);
        }
        return rootClass;
    }

    Set<CompileUnit> getCompileUnits() {
        return compileUnits;
    }

    boolean getStrictMode() {
        return strict;
    }

    void setStrictMode(final boolean strict) {
        this.strict = strict;
    }

    ConstantData getConstantData() {
        return constantData;
    }

    CodeInstaller<ScriptEnvironment> getCodeInstaller() {
        return installer;
    }

    TemporarySymbols getTemporarySymbols() {
        return temporarySymbols;
    }

    void addClass(final String name, final byte[] code) {
        bytecode.put(name, code);
    }

    ScriptEnvironment getEnv() {
        return this.env;
    }

    private String safeSourceName(final Source src) {
        String baseName = new File(src.getName()).getName();
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

    private int nextCompileUnitIndex() {
        return compileUnits.size() + 1;
    }

    String firstCompileUnitName() {
        return SCRIPTS_PACKAGE + '/' + scriptName;
    }

    private String nextCompileUnitName() {
        return firstCompileUnitName() + '$' + nextCompileUnitIndex();
    }

    CompileUnit addCompileUnit(final long initialWeight) {
        return addCompileUnit(nextCompileUnitName(), initialWeight);
    }

    CompileUnit addCompileUnit(final String unitClassName) {
        return addCompileUnit(unitClassName, 0L);
    }

    private CompileUnit addCompileUnit(final String unitClassName, final long initialWeight) {
        final CompileUnit compileUnit = initCompileUnit(unitClassName, initialWeight);
        compileUnits.add(compileUnit);
        LOG.fine("Added compile unit ", compileUnit);
        return compileUnit;
    }

    private CompileUnit initCompileUnit(final String unitClassName, final long initialWeight) {
        final ClassEmitter classEmitter = new ClassEmitter(env, sourceName, unitClassName, strict);
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

    static boolean shouldUseIntegerArithmetic() {
        return USE_INT_ARITH;
    }

    private static final boolean USE_INT_ARITH;

    static {
        USE_INT_ARITH = Options.getBooleanProperty("nashorn.compiler.intarithmetic");
        assert !USE_INT_ARITH : "Integer arithmetic is not enabled";
    }
}
