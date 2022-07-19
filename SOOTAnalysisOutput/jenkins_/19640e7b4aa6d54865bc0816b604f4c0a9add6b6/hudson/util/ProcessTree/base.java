package hudson.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import hudson.EnvVars;
import hudson.Util;
import jenkins.model.Jenkins;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.SlaveComputer;
import hudson.util.ProcessTree.OSProcess;
import hudson.util.ProcessTreeRemoting.IOSProcess;
import hudson.util.ProcessTreeRemoting.IProcessTree;
import org.apache.commons.io.FileUtils;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.sun.jna.Pointer.NULL;
import static hudson.util.jna.GNUCLibrary.LIBC;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;

public abstract class ProcessTree implements Iterable<OSProcess>, IProcessTree, Serializable {

    protected final Map<Integer, OSProcess> processes = new HashMap<Integer, OSProcess>();

    private transient volatile List<ProcessKiller> killers;

    private ProcessTree() {
    }

    public final OSProcess get(int pid) {
        return processes.get(pid);
    }

    public final Iterator<OSProcess> iterator() {
        return processes.values().iterator();
    }

    public abstract OSProcess get(Process proc);

    public abstract void killAll(Map<String, String> modelEnvVars) throws InterruptedException;

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
                    killers = channelToMaster.call(new Callable<List<ProcessKiller>, IOException>() {

                        public List<ProcessKiller> call() throws IOException {
                            return new ArrayList<ProcessKiller>(ProcessKiller.all());
                        }
                    });
                } else {
                    killers = Collections.emptyList();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to obtain killers", e);
                killers = Collections.emptyList();
            }
        return killers;
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
                if (killer.kill(this))
                    break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to kill pid=" + getPid(), e);
            }
        }

        public abstract void killRecursively() throws InterruptedException;

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
            return callable.invoke(this, Jenkins.MasterComputer.localChannel);
        }

        Object writeReplace() {
            return new SerializedProcess(pid);
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

    public static ProcessTree get() {
        if (!enabled)
            return DEFAULT;
        try {
            if (File.pathSeparatorChar == ';')
                return new Windows();
            String os = Util.fixNull(System.getProperty("os.name"));
            if (os.equals("Linux"))
                return new Linux();
            if (os.equals("SunOS"))
                return new Solaris();
            if (os.equals("Mac OS X"))
                return new Darwin();
        } catch (LinkageError e) {
            LOGGER.log(Level.WARNING, "Failed to load winp. Reverting to the default", e);
            enabled = false;
        }
        return DEFAULT;
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

    private static final class Windows extends Local {

        Windows() {
            for (final WinProcess p : WinProcess.all()) {
                int pid = p.getPid();
                if (pid == 0 || pid == 4)
                    continue;
                super.processes.put(pid, new OSProcess(pid) {

                    private EnvVars env;

                    private List<String> args;

                    public OSProcess getParent() {
                        return null;
                    }

                    public void killRecursively() throws InterruptedException {
                        LOGGER.finer("Killing recursively " + getPid());
                        p.killRecursively();
                        killByKiller();
                    }

                    public void kill() throws InterruptedException {
                        LOGGER.finer("Killing " + getPid());
                        p.kill();
                        killByKiller();
                    }

                    @Override
                    public synchronized List<String> getArguments() {
                        if (args == null)
                            args = Arrays.asList(QuotedStringTokenizer.tokenize(p.getCommandLine()));
                        return args;
                    }

                    @Override
                    public synchronized EnvVars getEnvironmentVariables() {
                        if (env != null)
                            return env;
                        env = new EnvVars();
                        try {
                            env.putAll(p.getEnvironmentVariables());
                        } catch (WinpException e) {
                            LOGGER.log(FINE, "Failed to get environment variable ", e);
                        }
                        return env;
                    }
                });
            }
        }

        @Override
        public OSProcess get(Process proc) {
            return get(new WinProcess(proc).getPid());
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this) {
                if (p.getPid() < 10)
                    continue;
                LOGGER.finest("Considering to kill " + p.getPid());
                boolean matched;
                try {
                    matched = p.hasMatchingEnvVars(modelEnvVars);
                } catch (WinpException e) {
                    LOGGER.log(FINEST, "  Failed to check environment variable match", e);
                    continue;
                }
                if (matched)
                    p.killRecursively();
                else
                    LOGGER.finest("Environment variable didn't match");
            }
        }

        static {
            WinProcess.enableDebugPrivilege();
        }
    }

    static abstract class Unix extends Local {

        @Override
        public OSProcess get(Process proc) {
            try {
                return get((Integer) UnixReflection.PID_FIELD.get(proc));
            } catch (IllegalAccessException e) {
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this) if (p.hasMatchingEnvVars(modelEnvVars))
                p.killRecursively();
        }
    }

    static abstract class ProcfsUnix extends Unix {

        ProcfsUnix() {
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
            try {
                int pid = getPid();
                LOGGER.fine("Killing pid=" + pid);
                UnixReflection.DESTROY_PROCESS.invoke(null, pid);
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
            LOGGER.fine("Recursively killing pid=" + getPid());
            for (OSProcess p : getChildren()) p.killRecursively();
            kill();
        }

        public abstract List<String> getArguments();
    }

    private static final class UnixReflection {

        private static final Field PID_FIELD;

        private static final Method DESTROY_PROCESS;

        static {
            try {
                Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                PID_FIELD = clazz.getDeclaredField("pid");
                PID_FIELD.setAccessible(true);
                DESTROY_PROCESS = clazz.getDeclaredMethod("destroyProcess", int.class);
                DESTROY_PROCESS.setAccessible(true);
            } catch (ClassNotFoundException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            } catch (NoSuchFieldException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            } catch (NoSuchMethodException e) {
                LinkageError x = new LinkageError();
                x.initCause(e);
                throw x;
            }
        }
    }

    static class Linux extends ProcfsUnix {

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
                    byte[] cmdline = FileUtils.readFileToByteArray(getFile("cmdline"));
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
                    byte[] environ = FileUtils.readFileToByteArray(getFile("environ"));
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
    }

    static class Solaris extends ProcfsUnix {

        protected OSProcess createProcess(final int pid) throws IOException {
            return new SolarisProcess(pid);
        }

        private class SolarisProcess extends UnixProcess {

            private final int ppid;

            private final int envp;

            private final int argp;

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
                    psinfo.seek(188);
                    argc = adjust(psinfo.readInt());
                    argp = adjust(psinfo.readInt());
                    envp = adjust(psinfo.readInt());
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
                try {
                    RandomAccessFile as = new RandomAccessFile(getFile("as"), "r");
                    if (LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading " + getFile("as"));
                    try {
                        for (int n = 0; n < argc; n++) {
                            as.seek(to64(argp + n * 4));
                            int p = adjust(as.readInt());
                            arguments.add(readLine(as, p, "argv[" + n + "]"));
                        }
                    } finally {
                        as.close();
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
                    RandomAccessFile as = new RandomAccessFile(getFile("as"), "r");
                    if (LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading " + getFile("as"));
                    try {
                        for (int n = 0; ; n++) {
                            as.seek(to64(envp + n * 4));
                            int p = adjust(as.readInt());
                            if (p == 0)
                                break;
                            envVars.addLine(readLine(as, p, "env[" + n + "]"));
                        }
                    } finally {
                        as.close();
                    }
                } catch (IOException e) {
                }
                return envVars;
            }

            private String readLine(RandomAccessFile as, int p, String prefix) throws IOException {
                if (LOGGER.isLoggable(FINEST))
                    LOGGER.finest("Reading " + prefix + " at " + p);
                as.seek(to64(p));
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int ch, i = 0;
                while ((ch = as.read()) > 0) {
                    if ((++i) % 100 == 0 && LOGGER.isLoggable(FINEST))
                        LOGGER.finest(prefix + " is so far " + buf.toString());
                    buf.write(ch);
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
    }

    private static class Darwin extends Unix {

        Darwin() {
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
                IntByReference _ = new IntByReference(sizeOfInt);
                IntByReference size = new IntByReference(sizeOfInt);
                Memory m;
                int nRetry = 0;
                while (true) {
                    if (LIBC.sysctl(MIB_PROC_ALL, 3, NULL, size, NULL, _) != 0)
                        throw new IOException("Failed to obtain memory requirement: " + LIBC.strerror(Native.getLastError()));
                    m = new Memory(size.getValue());
                    if (LIBC.sysctl(MIB_PROC_ALL, 3, m, size, NULL, _) != 0) {
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
                    IntByReference _ = new IntByReference();
                    IntByReference argmaxRef = new IntByReference(0);
                    IntByReference size = new IntByReference(sizeOfInt);
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_ARGMAX }, 2, argmaxRef.getPointer(), size, NULL, _) != 0)
                        throw new IOException("Failed to get kernl.argmax: " + LIBC.strerror(Native.getLastError()));
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
                    if (LIBC.sysctl(new int[] { CTL_KERN, KERN_PROCARGS2, pid }, 3, m, size, NULL, _) != 0)
                        throw new IOException("Failed to obtain ken.procargs2: " + LIBC.strerror(Native.getLastError()));
                    int argc = m.readInt();
                    String args0 = m.readString();
                    m.skip0();
                    try {
                        for (int i = 0; i < argc; i++) {
                            arguments.add(m.readString());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalStateException("Failed to parse arguments: pid=" + pid + ", arg0=" + args0 + ", arguments=" + arguments + ", nargs=" + argc + ". Please run 'ps e " + pid + "' and report this to https://issues.jenkins-ci.org/browse/JENKINS-9634", e);
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

        Local() {
        }
    }

    public static class Remote extends ProcessTree implements Serializable {

        private final IProcessTree proxy;

        public Remote(ProcessTree proxy, Channel ch) {
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

    public static boolean enabled = !Boolean.getBoolean(ProcessTreeKiller.class.getName() + ".disable") && !Boolean.getBoolean(ProcessTree.class.getName() + ".disable");
}
