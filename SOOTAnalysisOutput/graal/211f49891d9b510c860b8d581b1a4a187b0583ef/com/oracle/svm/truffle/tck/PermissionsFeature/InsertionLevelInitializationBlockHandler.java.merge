package com.oracle.svm.truffle.tck;

import static com.oracle.graal.pointsto.reports.ReportUtils.report;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.graph.NodeInputList;
import org.graalvm.compiler.nodes.Invoke;
import org.graalvm.compiler.nodes.PiNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.java.NewInstanceNode;
import org.graalvm.compiler.options.Option;
import org.graalvm.compiler.options.OptionType;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.polyglot.io.FileSystem;
import com.oracle.graal.pointsto.BigBang;
import com.oracle.graal.pointsto.flow.InvokeTypeFlow;
import com.oracle.graal.pointsto.meta.AnalysisMethod;
import com.oracle.graal.pointsto.meta.AnalysisType;
import com.oracle.svm.core.SubstrateOptions;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.option.HostedOptionKey;
import com.oracle.svm.core.option.LocatableMultiOptionValue;
import com.oracle.svm.core.util.UserError;
import com.oracle.svm.hosted.FeatureImpl;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.SVMHost;
import com.oracle.svm.hosted.config.ConfigurationParserUtils;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

public class PermissionsFeature implements Feature {

    private static final String CONFIG = "truffle-language-permissions-config.json";

    public static class Options {

        @Option(help = "Path to file where to store report of Truffle language privilege access.")
        public static final HostedOptionKey<String> TruffleTCKPermissionsReportFile = new HostedOptionKey<>(null);

        @Option(help = "Comma separated list of exclude files.")
        public static final HostedOptionKey<LocatableMultiOptionValue.Strings> TruffleTCKPermissionsExcludeFiles = new HostedOptionKey<>(new LocatableMultiOptionValue.Strings());

        @Option(help = "Maximal depth of a stack trace.", type = OptionType.Expert)
        public static final HostedOptionKey<Integer> TruffleTCKPermissionsMaxStackTraceDepth = new HostedOptionKey<>(-1);

        @Option(help = "Maximum number of errounous privileged accesses reported.", type = OptionType.Expert)
        public static final HostedOptionKey<Integer> TruffleTCKPermissionsMaxErrors = new HostedOptionKey<>(100);
    }

    static final class IsEnabled implements BooleanSupplier {

        @Override
        public boolean getAsBoolean() {
            return ImageSingletons.contains(PermissionsFeature.class);
        }
    }

    private static final Set<String> compilerPackages;

    static {
        compilerPackages = new HashSet<>();
        compilerPackages.add("org.graalvm.");
        compilerPackages.add("com.oracle.graalvm.");
        compilerPackages.add("com.oracle.truffle.api.");
        compilerPackages.add("com.oracle.truffle.polyglot.");
        compilerPackages.add("com.oracle.truffle.nfi.");
        compilerPackages.add("com.oracle.truffle.object.");
    }

    private static final Set<ClassLoader> systemClassLoaders;

    static {
        systemClassLoaders = new HashSet<>();
        for (ClassLoader cl = ClassLoader.getSystemClassLoader(); cl != null; cl = cl.getParent()) {
            systemClassLoaders.add(cl);
        }
    }

    private Path reportFilePath;

    private Set<AnalysisMethod> whiteList;

    private AnalysisType reflectionProxy;

    @Override
    public void duringSetup(DuringSetupAccess access) {
        if (SubstrateOptions.FoldSecurityManagerGetter.getValue()) {
            UserError.abort("%s requires -H:-FoldSecurityManagerGetter option.", getClass().getSimpleName());
        }
        String reportFile = Options.TruffleTCKPermissionsReportFile.getValue();
        if (reportFile == null) {
            UserError.abort("Path to report file must be given by -H:TruffleTCKPermissionsReportFile option.");
        }
        reportFilePath = Paths.get(reportFile);
        FeatureImpl.DuringSetupAccessImpl accessImpl = (FeatureImpl.DuringSetupAccessImpl) access;
        accessImpl.getHostVM().keepAnalysisGraphs();
    }

    @Override
    @SuppressWarnings("try")
    public void afterAnalysis(AfterAnalysisAccess access) {
        try {
            if (Files.exists(reportFilePath) && Files.size(reportFilePath) > 0) {
                Files.newOutputStream(reportFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException ioe) {
            throw UserError.abort("Cannot delete existing report file %s.", reportFilePath);
        }
        FeatureImpl.AfterAnalysisAccessImpl accessImpl = (FeatureImpl.AfterAnalysisAccessImpl) access;
        DebugContext debugContext = accessImpl.getDebugContext();
        try (DebugContext.Scope s = debugContext.scope(getClass().getSimpleName())) {
            BigBang bigbang = accessImpl.getBigBang();
            WhiteListParser parser = new WhiteListParser(accessImpl.getImageClassLoader(), bigbang);
            ConfigurationParserUtils.parseAndRegisterConfigurations(parser, accessImpl.getImageClassLoader(), getClass().getSimpleName(), Options.TruffleTCKPermissionsExcludeFiles, new ResourceAsOptionDecorator(getClass().getPackage().getName().replace('.', '/') + "/resources/jre.json"), CONFIG);
            reflectionProxy = bigbang.forClass("com.oracle.svm.reflect.helpers.ReflectionProxy");
            if (reflectionProxy == null) {
                UserError.abort("Cannot load ReflectionProxy type");
            }
            whiteList = parser.getLoadedWhiteList();
            Set<AnalysisMethod> deniedMethods = new HashSet<>();
            deniedMethods.addAll(findMethods(bigbang, SecurityManager.class, (m) -> m.getName().startsWith("check")));
            deniedMethods.addAll(findMethods(bigbang, sun.misc.Unsafe.class, (m) -> m.isPublic()));
            deniedMethods.addAll(findMethods(bigbang, FileSystem.newDefaultFileSystem().getClass(), (m) -> m.isPublic()));
            if (!deniedMethods.isEmpty()) {
                Map<AnalysisMethod, Set<AnalysisMethod>> cg = callGraph(bigbang, deniedMethods, debugContext);
                List<List<AnalysisMethod>> report = new ArrayList<>();
                Set<CallGraphFilter> contextFilters = new HashSet<>();
                Collections.addAll(contextFilters, new SafeInterruptRecognizer(bigbang), new SafePrivilegedRecognizer(bigbang), new SafeServiceLoaderRecognizer(bigbang, accessImpl.getImageClassLoader()));
                int maxStackDepth = Options.TruffleTCKPermissionsMaxStackTraceDepth.getValue();
                maxStackDepth = maxStackDepth == -1 ? Integer.MAX_VALUE : maxStackDepth;
                for (AnalysisMethod deniedMethod : deniedMethods) {
                    if (cg.containsKey(deniedMethod)) {
                        collectViolations(report, deniedMethod, maxStackDepth, Options.TruffleTCKPermissionsMaxErrors.getValue(), cg, contextFilters, new LinkedHashSet<>(), 1, 0);
                    }
                }
                if (!report.isEmpty()) {
                    report("detected privileged calls originated in language packages ", reportFilePath, (pw) -> {
                        StringBuilder builder = new StringBuilder();
                        for (List<AnalysisMethod> callPath : report) {
                            for (AnalysisMethod call : callPath) {
                                builder.append(call.asStackTraceElement(0)).append('\n');
                            }
                            builder.append('\n');
                        }
                        pw.print(builder);
                    });
                }
            }
        }
    }

    private Map<AnalysisMethod, Set<AnalysisMethod>> callGraph(BigBang bigbang, Set<AnalysisMethod> targets, DebugContext debugContext) {
        Deque<AnalysisMethod> todo = new LinkedList<>();
        Map<AnalysisMethod, Set<AnalysisMethod>> visited = new HashMap<>();
        for (AnalysisMethod m : bigbang.getUniverse().getMethods()) {
            if (m.isEntryPoint()) {
                visited.put(m, new HashSet<>());
                todo.offer(m);
            }
        }
        Deque<AnalysisMethod> path = new LinkedList<>();
        for (AnalysisMethod m : todo) {
            callGraphImpl(m, targets, visited, path, debugContext);
        }
        return visited;
    }

    private boolean callGraphImpl(AnalysisMethod m, Set<AnalysisMethod> targets, Map<AnalysisMethod, Set<AnalysisMethod>> visited, Deque<AnalysisMethod> path, DebugContext debugContext) {
        String mName = getMethodName(m);
        path.addFirst(m);
        try {
            boolean callPathContainsTarget = false;
            debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Entered method: %s.", mName);
            for (InvokeTypeFlow invoke : m.getTypeFlow().getInvokes()) {
                for (AnalysisMethod callee : invoke.getCallees()) {
                    Set<AnalysisMethod> parents = visited.get(callee);
                    String calleeName = getMethodName(callee);
                    debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Callee: %s, new: %b.", calleeName, parents == null);
                    if (parents == null) {
                        parents = new HashSet<>();
                        visited.put(callee, parents);
                        if (targets.contains(callee)) {
                            parents.add(m);
                            callPathContainsTarget = true;
                            continue;
                        }
                        boolean add = callGraphImpl(callee, targets, visited, path, debugContext);
                        if (add) {
                            parents.add(m);
                            debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Added callee: %s for %s.", calleeName, mName);
                        }
                        callPathContainsTarget |= add;
                    } else if (!isBacktrace(callee, path) || isBackTraceOverLanguageMethod(callee, path)) {
                        parents.add(m);
                        debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Added backtrace callee: %s for %s.", calleeName, mName);
                        callPathContainsTarget = true;
                    } else {
                        if (debugContext.isLogEnabled(DebugContext.VERY_DETAILED_LEVEL)) {
                            debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Ignoring backtrace callee: %s for %s.", calleeName, mName);
                        }
                    }
                }
            }
            debugContext.log(DebugContext.VERY_DETAILED_LEVEL, "Exited method: %s.", mName);
            return callPathContainsTarget;
        } finally {
            path.removeFirst();
        }
    }

    private static boolean isBacktrace(AnalysisMethod method, Deque<AnalysisMethod> path) {
        return path.contains(method);
    }

    private static boolean isBackTraceOverLanguageMethod(AnalysisMethod method, Deque<AnalysisMethod> path) {
        if (!isCompilerClass(method) && !isSystemClass(method)) {
            return false;
        }
        boolean found = false;
        for (Iterator<AnalysisMethod> it = path.descendingIterator(); it.hasNext(); ) {
            AnalysisMethod pe = it.next();
            if (method.equals(pe)) {
                found = true;
            } else if (found && !isCompilerClass(pe) && !isSystemClass(pe)) {
                return true;
            }
        }
        return false;
    }

    private int collectViolations(List<? super List<AnalysisMethod>> report, AnalysisMethod m, int maxDepth, int maxReports, Map<AnalysisMethod, Set<AnalysisMethod>> callGraph, Set<CallGraphFilter> contextFilters, LinkedHashSet<AnalysisMethod> visited, int depth, int noReports) {
        int useNoReports = noReports;
        if (useNoReports >= maxReports) {
            return useNoReports;
        }
        if (depth > 1) {
            if (isCompilerClass(m)) {
                return useNoReports;
            }
            if (isExcludedClass(m)) {
                return useNoReports;
            }
        }
        if (!visited.contains(m)) {
            visited.add(m);
            try {
                Set<AnalysisMethod> callers = callGraph.get(m);
                if (depth > maxDepth) {
                    if (!callers.isEmpty()) {
                        useNoReports = collectViolations(report, callers.iterator().next(), maxDepth, maxReports, callGraph, contextFilters, visited, depth + 1, useNoReports);
                    }
                } else if (!isSystemClass(m) && !isReflectionProxy(m)) {
                    List<AnalysisMethod> callPath = new ArrayList<>(visited);
                    report.add(callPath);
                    useNoReports++;
                } else {
                    nextCaller: for (AnalysisMethod caller : callers) {
                        for (CallGraphFilter filter : contextFilters) {
                            if (filter.test(m, caller, visited)) {
                                continue nextCaller;
                            }
                        }
                        useNoReports = collectViolations(report, caller, maxDepth, maxReports, callGraph, contextFilters, visited, depth + 1, useNoReports);
                    }
                }
            } finally {
                visited.remove(m);
            }
        }
        return useNoReports;
    }

    private boolean isReflectionProxy(AnalysisMethod method) {
        for (AnalysisType iface : method.getDeclaringClass().getInterfaces()) {
            if (iface.equals(reflectionProxy)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSystemClass(AnalysisMethod method) {
        Class<?> clz = method.getDeclaringClass().getJavaClass();
        if (clz == null) {
            return false;
        }
        return clz.getClassLoader() == null || systemClassLoaders.contains(clz.getClassLoader());
    }

    private static boolean isCompilerClass(AnalysisMethod method) {
        return isClassInPackage(getClassName(method), compilerPackages);
    }

    private boolean isExcludedClass(AnalysisMethod method) {
        return whiteList.contains(method);
    }

    private static boolean isClassInPackage(String javaName, Collection<? extends String> packages) {
        for (String pkg : packages) {
            if (javaName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private static Set<AnalysisMethod> findMethods(BigBang bigBang, Class<?> owner, Predicate<ResolvedJavaMethod> filter) {
        AnalysisType clazz = bigBang.forClass(owner);
        if (clazz == null) {
            throw new IllegalStateException("Cannot resolve " + owner.getName() + ".");
        }
        return findMethods(bigBang, clazz, filter);
    }

    static Set<AnalysisMethod> findMethods(BigBang bigBang, AnalysisType owner, Predicate<ResolvedJavaMethod> filter) {
        return findImpl(bigBang, owner.getWrappedWithoutResolve().getDeclaredMethods(), filter);
    }

    static Set<AnalysisMethod> findConstructors(BigBang bigBang, AnalysisType owner, Predicate<ResolvedJavaMethod> filter) {
        return findImpl(bigBang, owner.getWrappedWithoutResolve().getDeclaredConstructors(), filter);
    }

    private static Set<AnalysisMethod> findImpl(BigBang bigBang, ResolvedJavaMethod[] methods, Predicate<ResolvedJavaMethod> filter) {
        Set<AnalysisMethod> result = new HashSet<>();
        for (ResolvedJavaMethod m : methods) {
            if (filter.test(m)) {
                result.add(bigBang.getUniverse().lookup(m));
            }
        }
        return result;
    }

    private static String getMethodName(AnalysisMethod method) {
        return method.format("%H.%n(%p)");
    }

    private static String getClassName(AnalysisMethod method) {
        return method.getDeclaringClass().toJavaName();
    }

    private interface CallGraphFilter {

        boolean test(AnalysisMethod method, AnalysisMethod caller, LinkedHashSet<AnalysisMethod> trace);
    }

    private static final class SafeInterruptRecognizer implements CallGraphFilter {

        private final SVMHost hostVM;

        private final ResolvedJavaMethod threadInterrupt;

        private final ResolvedJavaMethod threadCurrentThread;

        SafeInterruptRecognizer(BigBang bigBang) {
            this.hostVM = (SVMHost) bigBang.getHostVM();
            Set<AnalysisMethod> methods = findMethods(bigBang, Thread.class, (m) -> m.getName().equals("interrupt"));
            if (methods.size() != 1) {
                throw new IllegalStateException("Failed to lookup Thread.interrupt().");
            }
            threadInterrupt = methods.iterator().next();
            methods = findMethods(bigBang, Thread.class, (m) -> m.getName().equals("currentThread"));
            if (methods.size() != 1) {
                throw new IllegalStateException("Failed to lookup Thread.currentThread().");
            }
            threadCurrentThread = methods.iterator().next();
        }

        @Override
        public boolean test(AnalysisMethod method, AnalysisMethod caller, LinkedHashSet<AnalysisMethod> trace) {
            Boolean res = null;
            if (threadInterrupt.equals(method)) {
                StructuredGraph graph = hostVM.getAnalysisGraph(caller);
                for (Invoke invoke : graph.getInvokes()) {
                    if (threadInterrupt.equals(invoke.callTarget().targetMethod())) {
                        ValueNode node = invoke.getReceiver();
                        if (node instanceof PiNode) {
                            node = ((PiNode) node).getOriginalNode();
                            if (node instanceof Invoke) {
                                boolean isCurrentThread = threadCurrentThread.equals(((Invoke) node).callTarget().targetMethod());
                                res = res == null ? isCurrentThread : (res && isCurrentThread);
                            }
                        }
                    }
                }
            }
            res = res == null ? false : res;
            return res;
        }
    }

    private final class SafePrivilegedRecognizer implements CallGraphFilter {

        private final SVMHost hostVM;

        private final Set<AnalysisMethod> dopriviledged;

        SafePrivilegedRecognizer(BigBang bigbang) {
            this.hostVM = (SVMHost) bigbang.getHostVM();
            this.dopriviledged = findMethods(bigbang, java.security.AccessController.class, (m) -> m.getName().equals("doPrivileged") || m.getName().equals("doPrivilegedWithCombiner"));
        }

        @Override
        public boolean test(AnalysisMethod method, AnalysisMethod caller, LinkedHashSet<AnalysisMethod> trace) {
            if (!dopriviledged.contains(method)) {
                return false;
            }
            boolean safeClass = isCompilerClass(caller) || isSystemClass(caller);
            if (safeClass) {
                return true;
            }
            StructuredGraph graph = hostVM.getAnalysisGraph(caller);
            for (Invoke invoke : graph.getInvokes()) {
                if (method.equals(invoke.callTarget().targetMethod())) {
                    NodeInputList<ValueNode> args = invoke.callTarget().arguments();
                    if (args.isEmpty()) {
                        return false;
                    }
                    ValueNode arg0 = args.get(0);
                    if (!(arg0 instanceof NewInstanceNode)) {
                        return false;
                    }
                    ResolvedJavaType newType = ((NewInstanceNode) arg0).instanceClass();
                    AnalysisMethod methodCalledByAccessController = findPrivilegedEntryPoint(method, trace);
                    if (newType == null || methodCalledByAccessController == null) {
                        return false;
                    }
                    if (newType.equals(methodCalledByAccessController.getDeclaringClass())) {
                        return false;
                    }
                }
            }
            return true;
        }

        private AnalysisMethod findPrivilegedEntryPoint(AnalysisMethod dopriviledgedMethod, LinkedHashSet<AnalysisMethod> trace) {
            AnalysisMethod ep = null;
            for (AnalysisMethod m : trace) {
                if (dopriviledgedMethod.equals(m)) {
                    return ep;
                }
                ep = m;
            }
            return null;
        }
    }

    private final class SafeServiceLoaderRecognizer implements CallGraphFilter {

        private final ResolvedJavaMethod nextService;

        private final ImageClassLoader imageClassLoader;

        SafeServiceLoaderRecognizer(BigBang bigBang, ImageClassLoader imageClassLoader) {
            AnalysisType serviceLoaderIterator = bigBang.forClass("java.util.ServiceLoader$LazyIterator");
            Set<AnalysisMethod> methods = findMethods(bigBang, serviceLoaderIterator, (m) -> m.getName().equals("nextService"));
            if (methods.size() != 1) {
                throw new IllegalStateException("Failed to lookup ServiceLoader$LazyIterator.nextService().");
            }
            this.nextService = methods.iterator().next();
            this.imageClassLoader = imageClassLoader;
        }

        @Override
        public boolean test(AnalysisMethod method, AnalysisMethod caller, LinkedHashSet<AnalysisMethod> trace) {
            if (nextService.equals(method)) {
                AnalysisType instantiatedType = findInstantiatedType(trace);
                if (instantiatedType != null) {
                    if (!isRegiseredInServiceLoader(instantiatedType)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private AnalysisType findInstantiatedType(LinkedHashSet<AnalysisMethod> trace) {
            AnalysisType res = null;
            for (AnalysisMethod m : trace) {
                if ("<init>".equals(m.getName())) {
                    res = m.getDeclaringClass();
                }
            }
            return res;
        }

        private boolean isRegiseredInServiceLoader(AnalysisType type) {
            String resource = String.format("META-INF/services/%s", type.toClassName());
            if (imageClassLoader.getClassLoader().getResource(resource) != null) {
                return true;
            }
            for (AnalysisType ifc : type.getInterfaces()) {
                if (isRegiseredInServiceLoader(ifc)) {
                    return true;
                }
            }
            AnalysisType superClz = type.getSuperclass();
            if (superClz != null) {
                return isRegiseredInServiceLoader(superClz);
            }
            return false;
        }
    }

    private static final class ResourceAsOptionDecorator extends HostedOptionKey<LocatableMultiOptionValue.Strings> {

        ResourceAsOptionDecorator(String defaultValue) {
            super(new LocatableMultiOptionValue.Strings(Collections.singletonList(defaultValue)));
        }
    }
}

@TargetClass(value = java.lang.SecurityManager.class, onlyWith = PermissionsFeature.IsEnabled.class)
final class Target_java_lang_SecurityManager {

    @Substitute
    @SuppressWarnings("unused")
    private void checkSecurityAccess(String target) {
    }

    @Substitute
    private void checkSetFactory() {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkPackageDefinition(String pkg) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkPackageAccess(String pkg) {
    }

    @Substitute
    private void checkPrintJobAccess() {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkPropertyAccess(String key) {
    }

    @Substitute
    private void checkPropertiesAccess() {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkMulticast(InetAddress maddr) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkAccept(String host, int port) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkListen(int port) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkConnect(String host, int port, Object context) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkConnect(String host, int port) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkDelete(String file) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkWrite(String file) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkWrite(FileDescriptor fd) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkRead(String file, Object context) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkRead(String file) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkRead(FileDescriptor fd) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkLink(String lib) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkExec(String cmd) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkExit(int status) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkAccess(ThreadGroup g) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkAccess(Thread t) {
    }

    @Substitute
    private void checkCreateClassLoader() {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkPermission(Permission perm, Object context) {
    }

    @Substitute
    @SuppressWarnings("unused")
    private void checkPermission(Permission perm) {
    }
}

final class SecurityManagerHolder {

    static final SecurityManager SECURITY_MANAGER = new SecurityManager();
}

@TargetClass(value = java.lang.System.class, onlyWith = PermissionsFeature.IsEnabled.class)
final class Target_java_lang_System {

    @Substitute
    private static SecurityManager getSecurityManager() {
        return SecurityManagerHolder.SECURITY_MANAGER;
    }
}