package hudson.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.LastErrorException;
import com.sun.jna.ptr.IntByReference;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.SlaveComputer;
import hudson.util.ProcessKillingVeto.VetoCause;
import hudson.util.ProcessTree.OSProcess;
import hudson.util.ProcessTreeRemoting.IOSProcess;
import hudson.util.ProcessTreeRemoting.IProcessTree;
import jenkins.security.SlaveToMasterCallable;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import static com.sun.jna.Pointer.NULL;
import jenkins.util.SystemProperties;
import static hudson.util.jna.GNUCLibrary.LIBC;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import javax.annotation.Nonnull;

public abstract class ProcessTree implements Iterable<OSProcess>, IProcessTree, Serializable {

    protected final Map<Integer, OSProcess> processes = new HashMap<Integer, OSProcess>();

    private transient volatile List<ProcessKiller> killers;

    private boolean skipVetoes;

    private ProcessTree() {
        skipVetoes = false;
    }

    private ProcessTree(boolean vetoesExist) {
        skipVetoes = !vetoesExist;
    }

    public final OSProcess get(int pid) {
        return processes.get(pid);
    }

    public final Iterator<OSProcess> iterator() {
        return processes.values().iterator();
    }

    public abstract OSProcess get(Process proc);

    public abstract void killAll(Map<String, String> modelEnvVars) throws InterruptedException;

    private final long softKillWaitSeconds = Integer.getInteger("SoftKillWaitSeconds", 2 * 60);

    public void killAll(Process proc, Map<String, String> modelEnvVars) throws InterruptedException {
        LOGGER.fine("killAll: process=" + proc + " and envs=" + modelEnvVars);
        OSProcess p = get(proc);
        if (p != null)
            p.killRecursively();
        if (modelEnvVars != null)
            killAll(modelEnvVars);
    }

    final List<ProcessKiller> getKillers() throws InterruptedException {
        if (killers == null)
            try {
                VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                if (channelToMaster != null) {
                    killers = channelToMaster.call(new ListAll());
                } else {
                    killers = Collections.emptyList();
                }
            } catch (IOException | Error e) {
                LOGGER.log(Level.WARNING, "Failed to obtain killers", e);
                killers = Collections.emptyList();
            }
        return killers;
    }

    private static class ListAll extends SlaveToMasterCallable<List<ProcessKiller>, IOException> {

        @Override
        public List<ProcessKiller> call() throws IOException {
            return new ArrayList<>(ProcessKiller.all());
        }
    }

    public abstract class OSProcess implements IOSProcess, Serializable {

        final int pid;

        private OSProcess(int pid) {
            this.pid = pid;
        }

        public final int getPid() {
            return pid;
        }

        public abstract OSProcess getParent();

        final ProcessTree getTree() {
            return ProcessTree.this;
        }

        public final List<OSProcess> getChildren() {
            List<OSProcess> r = new ArrayList<OSProcess>();
            for (OSProcess p : ProcessTree.this) if (p.getParent() == this)
                r.add(p);
            return r;
        }

        public abstract void kill() throws InterruptedException;

        void killByKiller() throws InterruptedException {
            for (ProcessKiller killer : getKillers()) try {
                if (killer.kill(this)) {
                    break;
                }
            } catch (IOException | Error e) {
                LOGGER.log(Level.WARNING, "Failed to kill pid=" + getPid(), e);
            }
        }

        public abstract void killRecursively() throws InterruptedException;

        @CheckForNull
        protected VetoCause getVeto() {
            String causeMessage = null;
            if (!skipVetoes) {
                try {
                    VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                    if (channelToMaster != null) {
                        CheckVetoes vetoCheck = new CheckVetoes(this);
                        causeMessage = channelToMaster.call(vetoCheck);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "I/O Exception while checking for vetoes", e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Interrupted Exception while checking for vetoes", e);
                }
            }
            if (causeMessage != null) {
                return new VetoCause(causeMessage);
            }
            return null;
        }

        public abstract List<String> getArguments();

        public abstract EnvVars getEnvironmentVariables();

        public final boolean hasMatchingEnvVars(Map<String, String> modelEnvVar) {
            if (modelEnvVar.isEmpty())
                return false;
            SortedMap<String, String> envs = getEnvironmentVariables();
            for (Entry<String, String> e : modelEnvVar.entrySet()) {
                String v = envs.get(e.getKey());
                if (v == null || !v.equals(e.getValue()))
                    return false;
            }
            return true;
        }

        public <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException {
            return callable.invoke(this, FilePath.localChannel);
        }

        Object writeReplace() {
            return new SerializedProcess(pid);
        }

        private class CheckVetoes extends SlaveToMasterCallable<String, IOException> {

            private IOSProcess process;

            public CheckVetoes(IOSProcess processToCheck) {
                process = processToCheck;
            }

            @Override
            public String call() throws IOException {
                for (ProcessKillingVeto vetoExtension : ProcessKillingVeto.all()) {
                    VetoCause cause = vetoExtension.vetoProcessKilling(process);
                    if (cause != null) {
                        if (LOGGER.isLoggable(FINEST))
                            LOGGER.info("Killing of pid " + getPid() + " vetoed by " + vetoExtension.getClass().getName() + ": " + cause.getMessage());
                        return cause.getMessage();
                    }
                }
                return null;
            }
        }
    }

    private final class SerializedProcess implements Serializable {

        private final int pid;

        private static final long serialVersionUID = 1L;

        private SerializedProcess(int pid) {
            this.pid = pid;
        }

        Object readResolve() {
            return get(pid);
        }
    }

    public interface ProcessCallable<T> extends Serializable {

        T invoke(OSProcess process, VirtualChannel channel) throws IOException;
    }

    static Boolean vetoersExist;

    public static ProcessTree get() {
        if (!enabled)
            return DEFAULT;
        if (vetoersExist == null) {
            try {
                VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                if (channelToMaster != null) {
                    vetoersExist = channelToMaster.call(new DoVetoersExist());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while determining if vetoers exist", e);
            }
        }
        boolean vetoes = (vetoersExist == null ? true : vetoersExist);
        try {
            if (File.pathSeparatorChar == ';')
                return new Windows(vetoes);
            String os = Util.fixNull(System.getProperty("os.name"));
            if (os.equals("Linux"))
                return new Linux(vetoes);
            if (os.equals("SunOS"))
                return new Solaris(vetoes);
            if (os.equals("Mac OS X"))
                return new Darwin(vetoes);
        } catch (LinkageError e) {
            LOGGER.log(Level.WARNING, "Failed to load winp. Reverting to the default", e);
            enabled = false;
        }
        return DEFAULT;
    }

    private static class DoVetoersExist extends SlaveToMasterCallable<Boolean, IOException> {

        @Override
        public Boolean call() throws IOException {
            return ProcessKillingVeto.all().size() > 0;
        }
    }

    static final ProcessTree DEFAULT = new Local() {

        public OSProcess get(final Process proc) {
            return new OSProcess(-1) {

                public OSProcess getParent() {
                    return null;
                }

                public void killRecursively() {
                    proc.destroy();
                }

                public void kill() throws InterruptedException {
                    if (getVeto() != null)
                        return;
                    proc.destroy();
                    killByKiller();
                }

                public List<String> getArguments() {
                    return Collections.emptyList();
                }

                public EnvVars getEnvironmentVariables() {
                    return new EnvVars();
                }
            };
        }

        public void killAll(Map<String, String> modelEnvVars) {
        }
    };

    private class WindowsOSProcess extends OSProcess {

        private final WinProcess p;

        private EnvVars env;

        private List<String> args;

        WindowsOSProcess(WinProcess p) {
            super(p.getPid());
            this.p = p;
        }

        @Override
        public OSProcess getParent() {
            return null;
        }

        @Override
        public void killRecursively() throws InterruptedException {
            if (getVeto() != null)
                return;
            LOGGER.log(FINER, "Killing recursively {0}", getPid());
            killSoftly();
            p.killRecursively();
            killByKiller();
        }

        @Override
        public void kill() throws InterruptedException {
            if (getVeto() != null) {
                return;
            }
            LOGGER.log(FINER, "Killing {0}", getPid());
            killSoftly();
            p.kill();
            killByKiller();
        }

        private void killSoftly() throws InterruptedException {
            try {
                if (!p.sendCtrlC()) {
                    return;
                }
            } catch (WinpException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Failed to send CTRL+C to pid=" + getPid(), e);
                }
                return;
            }
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            int sleepTime = 10;
            do {
                if (!p.isRunning()) {
                    break;
                }
                Thread.sleep(sleepTime);
                sleepTime = Math.min(sleepTime * 2, 1000);
            } while (System.nanoTime() < deadline);
        }

        @Override
        public synchronized List<String> getArguments() {
            if (args == null) {
                args = Arrays.asList(QuotedStringTokenizer.tokenize(p.getCommandLine()));
            }
            return args;
        }

        @Override
        public synchronized EnvVars getEnvironmentVariables() {
            try {
                return getEnvironmentVariables2();
            } catch (WindowsOSProcessException e) {
                if (LOGGER.isLoggable(FINEST)) {
                    LOGGER.log(FINEST, "Failed to get the environment variables of process with pid=" + p.getPid(), e);
                }
            }
            return null;
        }

        private synchronized EnvVars getEnvironmentVariables2() throws WindowsOSProcessException {
            if (env != null) {
                return env;
            }
            env = new EnvVars();
            try {
                env.putAll(p.getEnvironmentVariables());
            } catch (WinpException e) {
                throw new WindowsOSProcessException("Failed to get the environment variables", e);
            }
            return env;
        }

        private boolean hasMatchingEnvVars2(Map<String, String> modelEnvVar) throws WindowsOSProcessException {
            if (modelEnvVar.isEmpty())
                return false;
            SortedMap<String, String> envs = getEnvironmentVariables2();
            for (Entry<String, String> e : modelEnvVar.entrySet()) {
                String v = envs.get(e.getKey());
                if (v == null || !v.equals(e.getValue()))
                    return false;
            }
            return true;
        }
    }

    private static class WindowsOSProcessException extends Exception {

        WindowsOSProcessException(WinpException ex) {
            super(ex);
        }

        WindowsOSProcessException(String message, WinpException ex) {
            super(message, ex);
        }
    }

    private static final class Windows extends Local {

        Windows(boolean vetoesExist) {
            super(vetoesExist);
            for (final WinProcess p : WinProcess.all()) {
                int pid = p.getPid();
                if (pid == 0 || pid == 4)
                    continue;
                super.processes.put(pid, new WindowsOSProcess(p));
            }
        }

        @Override
        public OSProcess get(Process proc) {
            return get(new WinProcess(proc).getPid());
        }

        @Override
        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this) {
                if (p.getPid() < 10)
                    continue;
                LOGGER.log(FINEST, "Considering to kill {0}", p.getPid());
                boolean matched;
                try {
                    matched = hasMatchingEnvVars(p, modelEnvVars);
                } catch (WindowsOSProcessException e) {
                    if (LOGGER.isLoggable(FINEST)) {
                        LOGGER.log(FINEST, "Failed to check environment variable match for process with pid=" + p.getPid(), e);
                    }
                    continue;
                }
                if (matched) {
                    p.killRecursively();
                } else {
                    LOGGER.log(Level.FINEST, "Environment variable didn't match for process with pid={0}", p.getPid());
                }
            }
        }

        static {
            WinProcess.enableDebugPrivilege();
        }

        private static boolean hasMatchingEnvVars(@Nonnull OSProcess p, @Nonnull Map<String, String> modelEnvVars) throws WindowsOSProcessException {
            if (p instanceof WindowsOSProcess) {
                return ((WindowsOSProcess) p).hasMatchingEnvVars2(modelEnvVars);
            } else {
                try {
                    return p.hasMatchingEnvVars(modelEnvVars);
                } catch (WinpException e) {
                    throw new WindowsOSProcessException(e);
                }
            }
        }
    }

    static abstract class Unix extends Local {

        public Unix(boolean vetoersExist) {
            super(vetoersExist);
        }

        @Override
        public OSProcess get(Process proc) {
            return get(UnixReflection.pid(proc));
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this) if (p.hasMatchingEnvVars(modelEnvVars))
                p.killRecursively();
        }
    }

    static abstract class ProcfsUnix extends Unix {

        ProcfsUnix(boolean vetoersExist) {
            super(vetoersExist);
            File[] processes = new File("/proc").listFiles(new FileFilter() {

                public boolean accept(File f) {
                    return f.isDirectory();
                }
            });
            if (processes == null) {
                LOGGER.info("No /proc");
                return;
            }
            for (File p : processes) {
                int pid;
                try {
                    pid = Integer.parseInt(p.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    this.processes.put(pid, createProcess(pid));
                } catch (IOException e) {
                }
            }
        }

        protected abstract OSProcess createProcess(int pid) throws IOException;
    }

    public abstract class UnixProcess extends OSProcess {

        protected UnixProcess(int pid) {
            super(pid);
        }

        protected final File getFile(String relativePath) {
            return new File(new File("/proc/" + getPid()), relativePath);
        }

        public void kill() throws InterruptedException {
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            kill(deadline);
        }

        private void kill(long deadline) throws InterruptedException {
            if (getVeto() != null)
                return;
            try {
                int pid = getPid();
                LOGGER.fine("Killing pid=" + pid);
                UnixReflection.destroy(pid);
                int sleepTime = 10;
                File status = getFile("status");
                do {
                    if (!status.exists()) {
                        break;
                    }
                    Thread.sleep(sleepTime);
                    sleepTime = Math.min(sleepTime * 2, 1000);
                } while (System.nanoTime() < deadline);
            } catch (IllegalAccessException e) {
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof Error)
                    throw (Error) e.getTargetException();
                LOGGER.log(Level.INFO, "Failed to terminate pid=" + getPid(), e);
            }
            killByKiller();
        }

        public void killRecursively() throws InterruptedException {
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            killRecursively(deadline);
        }

        private void killRecursively(long deadline) throws InterruptedException {
            LOGGER.fine("Recursively killing pid=" + getPid());
            for (OSProcess p : getChildren()) {
                if (p instanceof UnixProcess) {
                    ((UnixProcess) p).killRecursively(deadline);
                } else {
                    p.killRecursively();
                }
            }
            kill(deadline);
        }

        public abstract List<String> getArguments();
    }

    private static final class UnixReflection {

        private static final Field JAVA8_PID_FIELD;

        private static final Method JAVA9_PID_METHOD;

        private static final Method JAVA8_DESTROY_PROCESS;

        private static final Method JAVA_9_PROCESSHANDLE_OF;

        private static final Method JAVA_9_PROCESSHANDLE_DESTROY;

        static {
            try {
                if (isPostJava8()) {
                    Class<?> clazz = Process.class;
                    JAVA9_PID_METHOD = clazz.getMethod("pid");
                    JAVA8_PID_FIELD = null;
                    Class<?> processHandleClazz = Class.forName("java.lang.ProcessHandle");
                    JAVA_9_PROCESSHANDLE_OF = processHandleClazz.getMethod("of", long.class);
                    JAVA_9_PROCESSHANDLE_DESTROY = processHandleClazz.getMethod("destroy");
                    JAVA8_DESTROY_PROCESS = null;
                } else {
                    Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                    JAVA8_PID_FIELD = clazz.getDeclaredField("pid");
                    JAVA8_PID_FIELD.setAccessible(true);
                    JAVA9_PID_METHOD = null;
                    JAVA8_DESTROY_PROCESS = clazz.getDeclaredMethod("destroyProcess", int.class, boolean.class);
                    JAVA8_DESTROY_PROCESS.setAccessible(true);
                    JAVA_9_PROCESSHANDLE_OF = null;
                    JAVA_9_PROCESSHANDLE_DESTROY = null;
                }
            } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
                LinkageError x = new LinkageError("Cannot initialize reflection for Unix Processes", e);
                throw x;
            }
        }

        public static void destroy(int pid) throws IllegalAccessException, InvocationTargetException {
            if (JAVA8_DESTROY_PROCESS != null) {
                JAVA8_DESTROY_PROCESS.invoke(null, pid, false);
            } else {
                final Optional handle = (Optional) JAVA_9_PROCESSHANDLE_OF.invoke(null, pid);
                if (handle.isPresent()) {
                    JAVA_9_PROCESSHANDLE_DESTROY.invoke(handle.get());
                }
            }
        }

        public static int pid(@Nonnull Process proc) {
            try {
                if (JAVA8_PID_FIELD != null) {
                    return JAVA8_PID_FIELD.getInt(proc);
                } else {
                    long pid = (long) JAVA9_PID_METHOD.invoke(proc);
                    if (pid > Integer.MAX_VALUE) {
                        throw new IllegalAccessError("Java 9+ support error (JENKINS-53799). PID is out of Jenkins API bounds: " + pid);
                    }
                    return (int) pid;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }
        }

        private static boolean isPostJava8() {
            String javaVersion = System.getProperty("java.version");
            return !javaVersion.startsWith("1.");
        }
    }

    static class Linux extends ProcfsUnix {

        public Linux(boolean vetoersExist) {
            super(vetoersExist);
        }

        protected LinuxProcess createProcess(int pid) throws IOException {
            return new LinuxProcess(pid);
        }

        class LinuxProcess extends UnixProcess {

            private int ppid = -1;

            private EnvVars envVars;

            private List<String> arguments;

            LinuxProcess(int pid) throws IOException {
                super(pid);
                BufferedReader r = new BufferedReader(new FileReader(getFile("status")));
                try {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.toLowerCase(Locale.ENGLISH);
                        if (line.startsWith("ppid:")) {
                            ppid = Integer.parseInt(line.substring(5).trim());
                            break;
                        }
                    }
                } finally {
                    r.close();
                }
                if (ppid == -1)
                    throw new IOException("Failed to parse PPID from /proc/" + pid + "/status");
            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if (arguments != null)
                    return arguments;
                arguments = new ArrayList<String>();
                try {
                    byte[] cmdline = readFileToByteArray(getFile("cmdline"));
                    int pos = 0;
                    for (int i = 0; i < cmdline.length; i++) {
                        byte b = cmdline[i];
                        if (b == 0) {
                            arguments.add(new String(cmdline, pos, i - pos));
                            pos = i + 1;
                        }
                    }
                } catch (IOException e) {
                }
                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if (envVars != null)
                    return envVars;
                envVars = new EnvVars();
                try {
                    byte[] environ = readFileToByteArray(getFile("environ"));
                    int pos = 0;
                    for (int i = 0; i < environ.length; i++) {
                        byte b = environ[i];
                        if (b == 0) {
                            envVars.addLine(new String(environ, pos, i - pos));
                            pos = i + 1;
                        }
                    }
                } catch (IOException e) {
                }
                return envVars;
            }
        }

        public byte[] readFileToByteArray(File file) throws IOException {
            InputStream in = org.apache.commons.io.FileUtils.openInputStream(file);
            try {
                return org.apache.commons.io.IOUtils.toByteArray(in);
            } finally {
                in.close();
            }
        }
    }

    static class Solaris extends ProcfsUnix {

        public Solaris(boolean vetoersExist) {
            super(vetoersExist);
        }

        protected OSProcess createProcess(final int pid) throws IOException {
            return new SolarisProcess(pid);
        }

        private class SolarisProcess extends UnixProcess {

            private static final byte PR_MODEL_ILP32 = 1;

            private static final byte PR_MODEL_LP64 = 2;

            private final int LINE_LENGTH_LIMIT = SystemProperties.getInteger(Solaris.class.getName() + ".lineLimit", 10000);

            private final boolean b64;

            private final int ppid;

            private final long envp;

            private final long argp;

            private final int argc;

            private EnvVars envVars;

            private List<String> arguments;

            private SolarisProcess(int pid) throws IOException {
                super(pid);
                RandomAccessFile psinfo = new RandomAccessFile(getFile("psinfo"), "r");
                try {
                    psinfo.seek(8);
                    if (adjust(psinfo.readInt()) != pid)
                        throw new IOException("psinfo PID mismatch");
                    ppid = adjust(psinfo.readInt());
                    if (Pointer.SIZE == 8) {
                        psinfo.seek(236);
                        argc = adjust(psinfo.readInt());
                        argp = adjustL(psinfo.readLong());
                        envp = adjustL(psinfo.readLong());
                        b64 = (psinfo.readByte() == PR_MODEL_LP64);
                    } else {
                        psinfo.seek(188);
                        argc = adjust(psinfo.readInt());
                        argp = to64(adjust(psinfo.readInt()));
                        envp = to64(adjust(psinfo.readInt()));
                        b64 = (psinfo.readByte() == PR_MODEL_LP64);
                    }
                } finally {
                    psinfo.close();
                }
                if (ppid == -1)
                    throw new IOException("Failed to parse PPID from /proc/" + pid + "/status");
            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if (arguments != null)
                    return arguments;
                arguments = new ArrayList<String>(argc);
                if (argc == 0) {
                    return arguments;
                }
                int psize = b64 ? 8 : 4;
                Memory m = new Memory(psize);
                try {
                    if (LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading " + getFile("as"));
                    int fd = LIBC.open(getFile("as").getAbsolutePath(), 0);
                    try {
                        for (int n = 0; n < argc; n++) {
                            LIBC.pread(fd, m, new NativeLong(psize), new NativeLong(argp + n * psize));
                            long addr = b64 ? m.getLong(0) : to64(m.getInt(0));
                            arguments.add(readLine(fd, addr, "argv[" + n + "]"));
                        }
                    } finally {
                        LIBC.close(fd);
                    }
                } catch (IOException | LastErrorException e) {
                }
                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if (envVars != null)
                    return envVars;
                envVars = new EnvVars();
                if (envp == 0) {
                    return envVars;
                }
                int psize = b64 ? 8 : 4;
                Memory m = new Memory(psize);
                try {
                    if (LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading " + getFile("as"));
                    int fd = LIBC.open(getFile("as").getAbsolutePath(), 0);
                    try {
                        for (int n = 0; ; n++) {
                            LIBC.pread(fd, m, new NativeLong(psize), new NativeLong(envp + n * psize));
                            long addr = b64 ? m.getLong(0) : to64(m.getInt(0));
                            if (addr == 0)
                                break;
                            envVars.addLine(readLine(fd, addr, "env[" + n + "]"));
                        }
                    } finally {
                        LIBC.close(fd);
                    }
                } catch (IOException | LastErrorException e) {
                }
                return envVars;
            }

            private String readLine(int fd, long addr, String prefix) throws IOException {
                if (LOGGER.isLoggable(FINEST))
                    LOGGER.finest("Reading " + prefix + " at " + addr);
                Memory m = new Memory(1);
                byte ch = 1;
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int i = 0;
                while (true) {
                    if (i++ > LINE_LENGTH_LIMIT) {
                        LOGGER.finest("could not find end of line, giving up");
                        throw new IOException("could not find end of line, giving up");
                    }
                    LIBC.pread(fd, m, new NativeLong(1), new NativeLong(addr));
                    ch = m.getByte(0);
                    if (ch == 0)
                        break;
                    buf.write(ch);
                    addr++;
                }
                String line = buf.toString();
                if (LOGGER.isLoggable(FINEST))
                    LOGGER.finest(prefix + " was " + line);
                return line;
            }
        }

        private static long to64(int i) {
            return i & 0xFFFFFFFFL;
        }

        private static int adjust(int i) {
            if (IS_LITTLE_ENDIAN)
                return (i << 24) | ((i << 8) & 0x00FF0000) | ((i >> 8) & 0x0000FF00) | (i >>> 24);
            else
                return i;
        }

        public static long adjustL(long i) {
            if (IS_LITTLE_ENDIAN) {
                return Long.reverseBytes(i);
            } else {
                return i;
            }
        }
    }

    private static class Darwin extends Unix {

        Darwin(boolean vetoersExist) {
            super(vetoersExist);
            String arch = System.getProperty("sun.arch.data.model");
            if ("64".equals(arch)) {
                sizeOf_kinfo_proc = sizeOf_kinfo_proc_64;
                kinfo_proc_pid_offset = kinfo_proc_pid_offset_64;
                kinfo_proc_ppid_offset = kinfo_proc_ppid_offset_64;
            } else {
                sizeOf_kinfo_proc = sizeOf_kinfo_proc_32;
                kinfo_proc_pid_offset = kinfo_proc_pid_offset_32;
                kinfo_proc_ppid_offset = kinfo_proc_ppid_offset_32;
            }
            try {
                IntByReference ref = new IntByReference(sizeOfInt);
                IntByReference size = new IntByReference(sizeOfInt);
                Memory m;
                int nRetry = 0;
                while (true) {
                    if (LIBC.sysctl(MIB_PROC_ALL, 3, NULL, size, NULL, ref) != 0)
                        throw new IOException("Failed to obtain memory requirement: " + LIBC.strerror(Native.getLastError()));
                    m = new Memory(size.getValue());
                    if (LIBC.sysctl(MIB_PROC_ALL, 3, m, size, NULL, ref) != 0) {
                        if (Native.getLastError() == ENOMEM && nRetry++ < 16)
                            continue;
                        throw new IOException("Failed to call kern.proc.all: " + LIBC.strerror(Native.getLastError()));
                    }
                    break;
                }
                int count = size.getValue() / sizeOf_kinfo_proc;
                LOGGER.fine("Found " + count + " processes");
                for (int base = 0; base < size.getValue(); base += sizeOf_kinfo_proc) {
                    int pid = m.getInt(base + kinfo_proc_pid_offset);
                    int ppid = m.getInt(base + kinfo_proc_ppid_offset);
                    super.processes.put(pid, new DarwinProcess(pid, ppid));
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to obtain process list", e);
            }
        }

        private class DarwinProcess extends UnixProcess {

            private final int ppid;

            private EnvVars envVars;

            private List<String> arguments;

            DarwinProcess(int pid, int ppid) {
                super(pid);
                this.ppid = ppid;
            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if (envVars != null)
                    return envVars;
                parse();
                return envVars;
            }

            public List<String> getArguments() {
                if (arguments != null)
                    return arguments;
                parse();
                return arguments;
            }

            private void parse() {
                try {
                    arguments = new ArrayList<String>();
                    envVars = new EnvVars();
                    IntByReference intByRef = new IntByReference();
                    IntByReference argmaxRef = new IntByReference(0);
                    IntByReference size = new IntByReference(sizeOfInt);
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_ARGMAX }, 2, argmaxRef.getPointer(), size, NULL, intByRef) != 0)
                        throw new IOException("Failed to get kern.argmax: " + LIBC.strerror(Native.getLastError()));
                    int argmax = argmaxRef.getValue();
                    class StringArrayMemory extends Memory {

                        private long offset = 0;

                        StringArrayMemory(long l) {
                            super(l);
                        }

                        int readInt() {
                            int r = getInt(offset);
                            offset += sizeOfInt;
                            return r;
                        }

                        byte peek() {
                            return getByte(offset);
                        }

                        String readString() {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte ch;
                            while ((ch = getByte(offset++)) != '\0') baos.write(ch);
                            return baos.toString();
                        }

                        void skip0() {
                            while (getByte(offset) == '\0') offset++;
                        }
                    }
                    StringArrayMemory m = new StringArrayMemory(argmax);
                    size.setValue(argmax);
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_PROCARGS2, pid }, 3, m, size, NULL, intByRef) != 0)
                        throw new IOException("Failed to obtain ken.procargs2: " + LIBC.strerror(Native.getLastError()));
                    int argc = m.readInt();
                    String args0 = m.readString();
                    m.skip0();
                    try {
                        for (int i = 0; i < argc; i++) {
                            arguments.add(m.readString());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalStateException("Failed to parse arguments: pid=" + pid + ", arg0=" + args0 + ", arguments=" + arguments + ", nargs=" + argc + ". Please see https://jenkins.io/redirect/troubleshooting/darwin-failed-to-parse-arguments", e);
                    }
                    while (m.peek() != 0) envVars.addLine(m.readString());
                } catch (IOException e) {
                }
            }
        }

        private final int sizeOf_kinfo_proc;

        private static final int sizeOf_kinfo_proc_32 = 492;

        private static final int sizeOf_kinfo_proc_64 = 648;

        private final int kinfo_proc_pid_offset;

        private static final int kinfo_proc_pid_offset_32 = 24;

        private static final int kinfo_proc_pid_offset_64 = 40;

        private final int kinfo_proc_ppid_offset;

        private static final int kinfo_proc_ppid_offset_32 = 416;

        private static final int kinfo_proc_ppid_offset_64 = 560;

        private static final int sizeOfInt = Native.getNativeSize(int.class);

        private static final int CTL_KERN = 1;

        private static final int KERN_PROC = 14;

        private static final int KERN_PROC_ALL = 0;

        private static final int ENOMEM = 12;

        private static int[] MIB_PROC_ALL = { CTL_KERN, KERN_PROC, KERN_PROC_ALL };

        private static final int KERN_ARGMAX = 8;

        private static final int KERN_PROCARGS2 = 49;
    }

    public static abstract class Local extends ProcessTree {

        @Deprecated
        Local() {
        }

        Local(boolean vetoesExist) {
            super(vetoesExist);
        }
    }

    public static class Remote extends ProcessTree implements Serializable {

        private final IProcessTree proxy;

        @Deprecated
        public Remote(ProcessTree proxy, Channel ch) {
            this.proxy = ch.export(IProcessTree.class, proxy);
            for (Entry<Integer, OSProcess> e : proxy.processes.entrySet()) processes.put(e.getKey(), new RemoteProcess(e.getValue(), ch));
        }

        public Remote(ProcessTree proxy, Channel ch, boolean vetoersExist) {
            super(vetoersExist);
            this.proxy = ch.export(IProcessTree.class, proxy);
            for (Entry<Integer, OSProcess> e : proxy.processes.entrySet()) processes.put(e.getKey(), new RemoteProcess(e.getValue(), ch));
        }

        @Override
        public OSProcess get(Process proc) {
            return null;
        }

        @Override
        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            proxy.killAll(modelEnvVars);
        }

        Object writeReplace() {
            return this;
        }

        private static final long serialVersionUID = 1L;

        private class RemoteProcess extends OSProcess implements Serializable {

            private final IOSProcess proxy;

            RemoteProcess(OSProcess proxy, Channel ch) {
                super(proxy.getPid());
                this.proxy = ch.export(IOSProcess.class, proxy);
            }

            public OSProcess getParent() {
                IOSProcess p = proxy.getParent();
                if (p == null)
                    return null;
                return get(p.getPid());
            }

            public void kill() throws InterruptedException {
                proxy.kill();
            }

            public void killRecursively() throws InterruptedException {
                proxy.killRecursively();
            }

            public List<String> getArguments() {
                return proxy.getArguments();
            }

            public EnvVars getEnvironmentVariables() {
                return proxy.getEnvironmentVariables();
            }

            Object writeReplace() {
                return this;
            }

            public <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException {
                return proxy.act(callable);
            }

            private static final long serialVersionUID = 1L;
        }
    }

    Object writeReplace() {
        return new Remote(this, Channel.current());
    }

    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));

    private static final Logger LOGGER = Logger.getLogger(ProcessTree.class.getName());

    public static boolean enabled = !SystemProperties.getBoolean(ProcessTreeKiller.class.getName() + ".disable") && !SystemProperties.getBoolean(ProcessTree.class.getName() + ".disable");
}