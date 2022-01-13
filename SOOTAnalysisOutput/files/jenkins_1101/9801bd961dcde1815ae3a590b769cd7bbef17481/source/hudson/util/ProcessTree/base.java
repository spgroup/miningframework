/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

import java.io.*;
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

import javax.annotation.CheckForNull;
import static com.sun.jna.Pointer.NULL;
import jenkins.util.SystemProperties;
import static hudson.util.jna.GNUCLibrary.LIBC;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import javax.annotation.Nonnull;

/**
 * Represents a snapshot of the process tree of the current system.
 *
 * <p>
 * A {@link ProcessTree} is really conceptually a map from process ID to a {@link OSProcess} object.
 * When Hudson runs on platforms that support process introspection, this allows you to introspect
 * and do some useful things on processes. On other platforms, the implementation falls back to
 * "do nothing" behavior.
 *
 * <p>
 * {@link ProcessTree} is remotable.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.315
 */
public abstract class ProcessTree implements Iterable<OSProcess>, IProcessTree, Serializable {
    /**
     * To be filled in the constructor of the derived type.
     */
    protected final Map<Integer/*pid*/, OSProcess> processes = new HashMap<Integer, OSProcess>();

    /**
     * Lazily obtained {@link ProcessKiller}s to be applied on this process tree.
     */
    private transient volatile List<ProcessKiller> killers;
    
    /**
     * Flag to skip the veto check since there aren't any.
     */
    private boolean skipVetoes;

    // instantiation only allowed for subtypes in this class
    private ProcessTree() {
       skipVetoes = false;
    }
    
    private ProcessTree(boolean vetoesExist) {
        skipVetoes = !vetoesExist;
    }

    /**
     * Gets the process given a specific ID, or null if no such process exists.
     */
    public final OSProcess get(int pid) {
        return processes.get(pid);
    }

    /**
     * Lists all the processes in the system.
     */
    public final Iterator<OSProcess> iterator() {
        return processes.values().iterator();
    }

    /**
     * Try to convert {@link Process} into this process object
     * or null if it fails (for example, maybe the snapshot is taken after
     * this process has already finished.)
     */
    public abstract OSProcess get(Process proc);

    /**
     * Kills all the processes that have matching environment variables.
     *
     * <p>
     * In this method, the method is given a
     * "model environment variables", which is a list of environment variables
     * and their values that are characteristic to the launched process.
     * The implementation is expected to find processes
     * in the system that inherit these environment variables, and kill
     * them all. This is suitable for locating daemon processes
     * that cannot be tracked by the regular ancestor/descendant relationship.
     */
    public abstract void killAll(Map<String, String> modelEnvVars) throws InterruptedException;

    private final long softKillWaitSeconds = Integer.getInteger("SoftKillWaitSeconds", 2 * 60); // by default processes get at most 2 minutes to respond to SIGTERM (JENKINS-17116)

    /**
     * Convenience method that does {@link #killAll(Map)} and {@link OSProcess#killRecursively()}.
     * This is necessary to reliably kill the process and its descendants, as some OS
     * may not implement {@link #killAll(Map)}.
     *
     * Either of the parameter can be null.
     */
    public void killAll(Process proc, Map<String, String> modelEnvVars) throws InterruptedException {
        LOGGER.fine("killAll: process="+proc+" and envs="+modelEnvVars);
        OSProcess p = get(proc);
        if(p!=null) p.killRecursively();
        if(modelEnvVars!=null)
            killAll(modelEnvVars);
    }

    /**
     * Obtains the list of killers.
     */
    /*package*/ final List<ProcessKiller> getKillers() throws InterruptedException {
        if (killers==null)
            try {
                VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                if (channelToMaster!=null) {
                    killers = channelToMaster.call(new ListAll());
                } else {
                    // used in an environment that doesn't support talk-back to the master.
                    // let's do with what we have.
                    killers = Collections.emptyList();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to obtain killers",e);
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

    /**
     * Represents a process.
     */
    public abstract class OSProcess implements IOSProcess, Serializable {
        final int pid;

        // instantiation only allowed for subtypes in this class
        private OSProcess(int pid) {
            this.pid = pid;
        }

        public final int getPid() {
            return pid;
        }
        /**
         * Gets the parent process. This method may return null, because
         * there's no guarantee that we are getting a consistent snapshot
         * of the whole system state.
         */
        public abstract OSProcess getParent();

        /*package*/ final ProcessTree getTree() {
            return ProcessTree.this;
        }

        /**
         * Immediate child processes.
         */
        public final List<OSProcess> getChildren() {
            List<OSProcess> r = new ArrayList<OSProcess>();
            for (OSProcess p : ProcessTree.this)
                if(p.getParent()==this)
                    r.add(p);
            return r;
        }

        /**
         * Kills this process.
         */
        public abstract void kill() throws InterruptedException;

        void killByKiller() throws InterruptedException {
            for (ProcessKiller killer : getKillers())
                try {
                    if (killer.kill(this))
                        break;
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to kill pid="+getPid(),e);
                }
        }

        /**
         * Kills this process and all the descendants.
         * <p>
         * Note that the notion of "descendants" is somewhat vague,
         * in the presence of such things like daemons. On platforms
         * where the recursive operation is not supported, this just kills
         * the current process.
         */
        public abstract void killRecursively() throws InterruptedException;

        /**
         * @return The first non-null {@link VetoCause} provided by a process killing veto extension for this OSProcess. 
         * null if no one objects killing the process.
         */
        protected @CheckForNull VetoCause getVeto() {
            String causeMessage = null;
            
            // Quick check, does anything exist to check against
            if (!skipVetoes) {
                try {
                    VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                    if (channelToMaster!=null) {
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

        /**
         * Gets the command-line arguments of this process.
         *
         * <p>
         * On Windows, where the OS models command-line arguments as a single string, this method
         * computes the approximated tokenization.
         */
        public abstract List<String> getArguments();

        /**
         * Obtains the environment variables of this process.
         *
         * @return
         *      empty map if failed (for example because the process is already dead,
         *      or the permission was denied.)
         */
        public abstract EnvVars getEnvironmentVariables();

        /**
         * Given the environment variable of a process and the "model environment variable" that Hudson
         * used for launching the build, returns true if there's a match (which means the process should
         * be considered a descendant of a build.)
         */
        public final boolean hasMatchingEnvVars(Map<String,String> modelEnvVar) {
            if(modelEnvVar.isEmpty())
                // sanity check so that we don't start rampage.
                return false;

            SortedMap<String,String> envs = getEnvironmentVariables();
            for (Entry<String,String> e : modelEnvVar.entrySet()) {
                String v = envs.get(e.getKey());
                if(v==null || !v.equals(e.getValue()))
                    return false;   // no match
            }

            return true;
        }

        /**
         * Executes a chunk of code at the same machine where this process resides.
         */
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

    /**
     * Serialized form of {@link OSProcess} is the PID and {@link ProcessTree}
     */
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

    /**
     * Code that gets executed on the machine where the {@link OSProcess} is local.
     * Used to act on {@link OSProcess}.
     *
     * @see ProcessTree.OSProcess#act(ProcessTree.ProcessCallable)
     */
    public interface ProcessCallable<T> extends Serializable {
        /**
         * Performs the computational task on the node where the data is located.
         *
         * @param process
         *      {@link OSProcess} that represents the local process.
         * @param channel
         *      The "back pointer" of the {@link Channel} that represents the communication
         *      with the node from where the code was sent.
         */
        T invoke(OSProcess process, VirtualChannel channel) throws IOException;
    }


    /* package */ static Boolean vetoersExist;
    
    /**
     * Gets the {@link ProcessTree} of the current system
     * that JVM runs in, or in the worst case return the default one
     * that's not capable of killing descendants at all.
     */
    public static ProcessTree get() {
        if(!enabled)
            return DEFAULT;

        // Check for the existance of vetoers if I don't know already
        if (vetoersExist == null) {
            try {
                VirtualChannel channelToMaster = SlaveComputer.getChannelToMaster();
                if (channelToMaster != null) {
                    vetoersExist = channelToMaster.call(new DoVetoersExist());
                }
            }
            catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error while determining if vetoers exist", e);
            }
        }
        
        // Null-check in case the previous call worked
        boolean vetoes = (vetoersExist == null ? true : vetoersExist);
        
        try {
            if(File.pathSeparatorChar==';')
                return new Windows(vetoes);

            String os = Util.fixNull(System.getProperty("os.name"));
            if(os.equals("Linux"))
                return new Linux(vetoes);
            if(os.equals("SunOS"))
                return new Solaris(vetoes);
            if(os.equals("Mac OS X"))
                return new Darwin(vetoes);
        } catch (LinkageError e) {
            LOGGER.log(Level.WARNING,"Failed to load winp. Reverting to the default",e);
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

//
//
// implementation follows
//-------------------------------------------
//

    /**
     * Empty process list as a default value if the platform doesn't support it.
     */
    /*package*/ static final ProcessTree DEFAULT = new Local() {
        public OSProcess get(final Process proc) {
            return new OSProcess(-1) {
                public OSProcess getParent() {
                    return null;
                }

                public void killRecursively() {
                    // fall back to a single process killer
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
            // no-op
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
            // Windows process doesn't have parent/child relationship
            return null;
        }

        @Override
        public void killRecursively() throws InterruptedException {
            if (getVeto() != null) 
                return;

            LOGGER.log(FINER, "Killing recursively {0}", getPid());
            // Firstly try to kill the root process gracefully, then do a forcekill if it does not help (algorithm is described in JENKINS-17116)
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
            // Firstly try to kill it gracefully, then do a forcekill if it does not help (algorithm is described in JENKINS-17116)
            killSoftly();
            p.kill();
            killByKiller();
        }

        private void killSoftly() throws InterruptedException {
            // send Ctrl+C to the process
            try {
                if (!p.sendCtrlC()) {
                    return;
                }
            }
            catch (WinpException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Failed to send CTRL+C to pid=" + getPid(), e);
                }
                return;
            }

            // after that wait for it to cease to exist
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            int sleepTime = 10; // initially we sleep briefly, then sleep up to 1sec
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
            if(args==null) {
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
            if(env !=null) {
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
        
        private boolean hasMatchingEnvVars2(Map<String,String> modelEnvVar) throws WindowsOSProcessException {
            if(modelEnvVar.isEmpty())
                // sanity check so that we don't start rampage.
                return false;

            SortedMap<String,String> envs = getEnvironmentVariables2();
            for (Entry<String,String> e : modelEnvVar.entrySet()) {
                String v = envs.get(e.getKey());
                if(v==null || !v.equals(e.getValue()))
                    return false;   // no match
            }

            return true;
        }
    }
    
    //TODO: Cleanup once Winp provides proper API 
    /**
     * Wrapper for runtime {@link WinpException}.
     */
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
                if(pid == 0 || pid == 4) continue; // skip the System Idle and System processes
                super.processes.put(pid, new WindowsOSProcess(p));
            }
        }

        @Override
        public OSProcess get(Process proc) {
            return get(new WinProcess(proc).getPid());
        }

        @Override
        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for( OSProcess p : this) {
                if(p.getPid()<10)
                    continue;   // ignore system processes like "idle process"

                LOGGER.log(FINEST, "Considering to kill {0}", p.getPid());

                boolean matched;
                try {
                    matched = hasMatchingEnvVars(p, modelEnvVars);
                } catch (WindowsOSProcessException e) {
                    // likely a missing privilege
                    // TODO: not a minor issue - causes process termination error in JENKINS-30782
                    if (LOGGER.isLoggable(FINEST)) {
                        LOGGER.log(FINEST, "Failed to check environment variable match for process with pid=" + p.getPid() ,e);
                    }
                    continue;
                }

                if(matched) {
                    p.killRecursively();
                } else {
                    LOGGER.log(Level.FINEST, "Environment variable didn't match for process with pid={0}", p.getPid());
                }
            }
        }

        static {
            WinProcess.enableDebugPrivilege();
        }
        
        private static boolean hasMatchingEnvVars(@Nonnull OSProcess p, @Nonnull Map<String, String> modelEnvVars)
                throws WindowsOSProcessException {
            if (p instanceof WindowsOSProcess) {
                return ((WindowsOSProcess)p).hasMatchingEnvVars2(modelEnvVars);
            } else {
                // Should never happen, but there is a risk of getting such class during deserialization
                try {
                    return p.hasMatchingEnvVars(modelEnvVars);
                } catch (WinpException e) {
                    // likely a missing privilege
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
            try {
                return get((Integer) UnixReflection.PID_FIELD.get(proc));
            } catch (IllegalAccessException e) { // impossible
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }
        }

        public void killAll(Map<String, String> modelEnvVars) throws InterruptedException {
            for (OSProcess p : this)
                if(p.hasMatchingEnvVars(modelEnvVars))
                    p.killRecursively();
        }
    }
    /**
     * {@link ProcessTree} based on /proc.
     */
    static abstract class ProcfsUnix extends Unix {
        ProcfsUnix(boolean vetoersExist) {
            super(vetoersExist);
            
            File[] processes = new File("/proc").listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            });
            if(processes==null) {
                LOGGER.info("No /proc");
                return;
            }

            for (File p : processes) {
                int pid;
                try {
                    pid = Integer.parseInt(p.getName());
                } catch (NumberFormatException e) {
                    // other sub-directories
                    continue;
                }
                try {
                    this.processes.put(pid,createProcess(pid));
                } catch (IOException e) {
                    // perhaps the process status has changed since we obtained a directory listing
                }
            }
        }

        protected abstract OSProcess createProcess(int pid) throws IOException;
    }

    /**
     * A process.
     */
    public abstract class UnixProcess extends OSProcess {
        protected UnixProcess(int pid) {
            super(pid);
        }

        protected final File getFile(String relativePath) {
            return new File(new File("/proc/"+getPid()),relativePath);
        }

        /**
         * Tries to kill this process.
         */
        public void kill() throws InterruptedException {
            // after sending SIGTERM, wait for the process to cease to exist
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            kill(deadline);
        }

        private void kill(long deadline) throws InterruptedException {
            if (getVeto() != null) 
                return;
            try {
                int pid = getPid();
                LOGGER.fine("Killing pid="+pid);
                UnixReflection.destroy(pid);
                // after sending SIGTERM, wait for the process to cease to exist
                int sleepTime = 10; // initially we sleep briefly, then sleep up to 1sec
                File status = getFile("status");
                do {
                    if (!status.exists()) {
                        break; // status is gone, process therefore as well
                    }

                    Thread.sleep(sleepTime);
                    sleepTime = Math.min(sleepTime * 2, 1000);
                } while (System.nanoTime() < deadline);
            } catch (IllegalAccessException e) {
                // this is impossible
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            } catch (InvocationTargetException e) {
                // tunnel serious errors
                if(e.getTargetException() instanceof Error)
                    throw (Error)e.getTargetException();
                // otherwise log and let go. I need to see when this happens
                LOGGER.log(Level.INFO, "Failed to terminate pid="+getPid(),e);
            }
            killByKiller();
        }

        public void killRecursively() throws InterruptedException {
            // after sending SIGTERM, wait for the processes to cease to exist until the deadline
            long deadline = System.nanoTime() + softKillWaitSeconds * 1000000000;
            killRecursively(deadline);
        }

        private void killRecursively(long deadline) throws InterruptedException {
            // We kill individual processes of a tree, so handling vetoes inside #kill() is enough for UnixProcess es
            LOGGER.fine("Recursively killing pid="+getPid());
            for (OSProcess p : getChildren()) {
                if (p instanceof UnixProcess) {
                    ((UnixProcess)p).killRecursively(deadline);
                } else {
                    p.killRecursively(); // should not happen, fallback to non-deadline version
                }
            }
            kill(deadline);
        }

        /**
         * Obtains the argument list of this process.
         *
         * @return
         *      empty list if failed (for example because the process is already dead,
         *      or the permission was denied.)
         */
        public abstract List<String> getArguments();
    }

    /**
     * Reflection used in the Unix support.
     */
    private static final class UnixReflection {
        /**
         * Field to access the PID of the process.
         */
        private static final Field PID_FIELD;

        /**
         * Method to destroy a process, given pid.
         *
         * Looking at the JavaSE source code, this is using SIGTERM (15)
         */
        private static final Method DESTROY_PROCESS;

        static {
            try {
                Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                PID_FIELD = clazz.getDeclaredField("pid");
                PID_FIELD.setAccessible(true);

                if (isPreJava8()) {
                    DESTROY_PROCESS = clazz.getDeclaredMethod("destroyProcess",int.class);
                } else {
                    DESTROY_PROCESS = clazz.getDeclaredMethod("destroyProcess",int.class, boolean.class);
                }
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

        public static void destroy(int pid) throws IllegalAccessException, InvocationTargetException {
            if (isPreJava8()) {
                DESTROY_PROCESS.invoke(null, pid);
            } else {
                DESTROY_PROCESS.invoke(null, pid, false);
            }
        }

        private static boolean isPreJava8() {
            int javaVersionAsAnInteger = Integer.parseInt(System.getProperty("java.version").replaceAll("\\.", "").replaceAll("_", "").substring(0, 2));
            return javaVersionAsAnInteger < 18;
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
                    while((line=r.readLine())!=null) {
                        line=line.toLowerCase(Locale.ENGLISH);
                        if(line.startsWith("ppid:")) {
                            ppid = Integer.parseInt(line.substring(5).trim());
                            break;
                        }
                    }
                } finally {
                    r.close();
                }
                if(ppid==-1)
                    throw new IOException("Failed to parse PPID from /proc/"+pid+"/status");
            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if(arguments!=null)
                    return arguments;
                arguments = new ArrayList<String>();
                try {
                    byte[] cmdline = readFileToByteArray(getFile("cmdline"));
                    int pos=0;
                    for (int i = 0; i < cmdline.length; i++) {
                        byte b = cmdline[i];
                        if(b==0) {
                            arguments.add(new String(cmdline,pos,i-pos));
                            pos=i+1;
                        }
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }
                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if(envVars !=null)
                    return envVars;
                envVars = new EnvVars();
                try {
                    byte[] environ = readFileToByteArray(getFile("environ"));
                    int pos=0;
                    for (int i = 0; i < environ.length; i++) {
                        byte b = environ[i];
                        if(b==0) {
                            envVars.addLine(new String(environ,pos,i-pos));
                            pos=i+1;
                        }
                    }
                } catch (IOException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
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

    /**
     * Implementation for Solaris that uses <tt>/proc</tt>.
     *
     * /proc/PID/psinfo contains a psinfo_t struct. We use it to determine where the
     *     process arguments and environment are located in PID's address space.
     *     Note that the psinfo_t struct is different (different sized elements) for 32-bit
     *     vs 64-bit processes and the kernel will provide the version of the struct that
     *     matches the _reader_ (this Java process) regardless of whether PID is a
     *     32-bit or 64-bit process.
     *
     *     Note that this means that if PID is a 64-bit process, then a 32-bit Java
     *     process can not get meaningful values for envp and argv out of the psinfo_t. The
     *     values will have been truncated to 32-bits.
     *
     * /proc/PID/as contains the address space of the process we are inspecting. We can
     *     follow the envp and argv pointers from psinfo_t to find the environment variables
     *     and process arguments. When following pointers in this address space we need to
     *     make sure to use 32-bit or 64-bit pointers depending on what sized pointers
     *     PID uses, regardless of what size pointers the Java process uses.
     *
     *     Note that the size of a 64-bit address space is larger than Long.MAX_VALUE (because
     *     longs are signed). So normal Java utilities like RandomAccessFile and FileChannel
     *     (which use signed longs as offsets) are not able to read from the end of the address
     *     space, where envp and argv will be. Therefore we need to use LIBC.pread() directly.
     *     when accessing this file.
     */
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

            /*
             * An arbitrary upper-limit on how many characters readLine() will
             * try reading before giving up. This avoids having readLine() loop
             * over the entire process address space if this class has bugs.
             */
            private final int LINE_LENGTH_LIMIT =
                SystemProperties.getInteger(Solaris.class.getName()+".lineLimit", 10000);

            /*
             * True if target process is 64-bit (Java process may be different).
             */
            private final boolean b64;

            private final int ppid;
            /**
             * Address of the environment vector.
             */
            private final long envp;
            /**
             * Similarly, address of the arguments vector.
             */
            private final long argp;
            private final int argc;
            private EnvVars envVars;
            private List<String> arguments;

            private SolarisProcess(int pid) throws IOException {
                super(pid);

                RandomAccessFile psinfo = new RandomAccessFile(getFile("psinfo"),"r");
                try {
                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/procfs.h
                    //typedef struct psinfo {
                    //	int	pr_flag;	/* process flags */
                    //	int	pr_nlwp;	/* number of lwps in the process */
                    //	pid_t	pr_pid;	/* process id */
                    //	pid_t	pr_ppid;	/* process id of parent */
                    //	pid_t	pr_pgid;	/* process id of process group leader */
                    //	pid_t	pr_sid;	/* session id */
                    //	uid_t	pr_uid;	/* real user id */
                    //	uid_t	pr_euid;	/* effective user id */
                    //	gid_t	pr_gid;	/* real group id */
                    //	gid_t	pr_egid;	/* effective group id */
                    //	uintptr_t	pr_addr;	/* address of process */
                    //	size_t	pr_size;	/* size of process image in Kbytes */
                    //	size_t	pr_rssize;	/* resident set size in Kbytes */
                    //	dev_t	pr_ttydev;	/* controlling tty device (or PRNODEV) */
                    //	ushort_t	pr_pctcpu;	/* % of recent cpu time used by all lwps */
                    //	ushort_t	pr_pctmem;	/* % of system memory used by process */
                    //	timestruc_t	pr_start;	/* process start time, from the epoch */
                    //	timestruc_t	pr_time;	/* cpu time for this process */
                    //	timestruc_t	pr_ctime;	/* cpu time for reaped children */
                    //	char	pr_fname[PRFNSZ];	/* name of exec'ed file */
                    //	char	pr_psargs[PRARGSZ];	/* initial characters of arg list */
                    //	int	pr_wstat;	/* if zombie, the wait() status */
                    //	int	pr_argc;	/* initial argument count */
                    //	uintptr_t	pr_argv;	/* address of initial argument vector */
                    //	uintptr_t	pr_envp;	/* address of initial environment vector */
                    //	char	pr_dmodel;	/* data model of the process */
                    //	lwpsinfo_t	pr_lwp;	/* information for representative lwp */
                    //} psinfo_t;

                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/uts/common/sys/types.h
                    // for the size of the various datatype.

                    // see http://cvs.opensolaris.org/source/xref/onnv/onnv-gate/usr/src/cmd/ptools/pargs/pargs.c
                    // for how to read this information

                    psinfo.seek(8);
                    if(adjust(psinfo.readInt())!=pid)
                        throw new IOException("psinfo PID mismatch");   // sanity check
                    ppid = adjust(psinfo.readInt());

                    /*
                     * Read the remainder of psinfo_t differently depending on whether the
                     * Java process is 32-bit or 64-bit.
                     */
                    if (Pointer.SIZE == 8) {
                        psinfo.seek(236);  // offset of pr_argc
                        argc = adjust(psinfo.readInt());
                        argp = adjustL(psinfo.readLong());
                        envp = adjustL(psinfo.readLong());
                        b64 = (psinfo.readByte() == PR_MODEL_LP64);
                    } else {
                        psinfo.seek(188);  // offset of pr_argc
                        argc = adjust(psinfo.readInt());
                        argp = to64(adjust(psinfo.readInt()));
                        envp = to64(adjust(psinfo.readInt()));
                        b64 = (psinfo.readByte() == PR_MODEL_LP64);
                    }
                } finally {
                    psinfo.close();
                }
                if(ppid==-1)
                    throw new IOException("Failed to parse PPID from /proc/"+pid+"/status");

            }

            public OSProcess getParent() {
                return get(ppid);
            }

            public synchronized List<String> getArguments() {
                if(arguments!=null)
                    return arguments;

                arguments = new ArrayList<String>(argc);
		if (argc == 0) {
		    return arguments;
		}

                int psize = b64 ? 8 : 4;
                Memory m = new Memory(psize);
                try {
                    if(LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading "+getFile("as"));
                    int fd = LIBC.open(getFile("as").getAbsolutePath(), 0);
                    try {
                        for( int n=0; n<argc; n++ ) {
                            // read a pointer to one entry
                            LIBC.pread(fd, m, new NativeLong(psize), new NativeLong(argp+n*psize));
                            long addr = b64 ? m.getLong(0) : to64(m.getInt(0));

                            arguments.add(readLine(fd, addr, "argv["+ n +"]"));
                        }
                    } finally {
                        LIBC.close(fd);
                    }
                } catch (IOException | LastErrorException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }

                arguments = Collections.unmodifiableList(arguments);
                return arguments;
            }

            public synchronized EnvVars getEnvironmentVariables() {
                if(envVars !=null)
                    return envVars;
                envVars = new EnvVars();

		if (envp == 0) {
		    return envVars;
		}

                int psize = b64 ? 8 : 4;
                Memory m = new Memory(psize);
                try {
                    if(LOGGER.isLoggable(FINER))
                        LOGGER.finer("Reading "+getFile("as"));
                    int fd = LIBC.open(getFile("as").getAbsolutePath(), 0);
                    try {
                        for( int n=0; ; n++ ) {
                            // read a pointer to one entry
                            LIBC.pread(fd, m, new NativeLong(psize), new NativeLong(envp+n*psize));
                            long addr = b64 ? m.getLong(0) : to64(m.getInt(0));
                            if (addr == 0) // completed the walk
                                break;

                            // now read the null-terminated string
                            envVars.addLine(readLine(fd, addr, "env["+ n +"]"));
                        }
                    } finally {
                        LIBC.close(fd);
                    }
                } catch (IOException | LastErrorException e) {
                    // failed to read. this can happen under normal circumstances (most notably permission denied)
                    // so don't report this as an error.
                }
                return envVars;
            }

            private String readLine(int fd, long addr, String prefix) throws IOException {
                if(LOGGER.isLoggable(FINEST))
                    LOGGER.finest("Reading "+prefix+" at "+addr);

                Memory m = new Memory(1);
                byte ch = 1;
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int i = 0;
                while(true) {
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
                if(LOGGER.isLoggable(FINEST))
                    LOGGER.finest(prefix+" was "+line);
                return line;
            }
        }

        /**
         * int to long conversion with zero-padding.
         */
        private static long to64(int i) {
            return i&0xFFFFFFFFL;
        }

        /**
         * {@link DataInputStream} reads a value in big-endian, so
         * convert it to the correct value on little-endian systems.
         */
        private static int adjust(int i) {
            if(IS_LITTLE_ENDIAN)
                return (i<<24) |((i<<8) & 0x00FF0000) | ((i>>8) & 0x0000FF00) | (i>>>24);
            else
                return i;
        }

        public static long adjustL(long i) {
            if(IS_LITTLE_ENDIAN) {
                return Long.reverseBytes(i);
            } else {
                return i;
            }
        }
    }

    /**
     * Implementation for Mac OS X based on sysctl(3).
     */
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
                IntByReference _ = new IntByReference(sizeOfInt);
                IntByReference size = new IntByReference(sizeOfInt);
                Memory m;
                int nRetry = 0;
                while(true) {
                    // find out how much memory we need to do this
                    if(LIBC.sysctl(MIB_PROC_ALL,3, NULL, size, NULL, _)!=0)
                        throw new IOException("Failed to obtain memory requirement: "+LIBC.strerror(Native.getLastError()));

                    // now try the real call
                    m = new Memory(size.getValue());
                    if(LIBC.sysctl(MIB_PROC_ALL,3, m, size, NULL, _)!=0) {
                        if(Native.getLastError()==ENOMEM && nRetry++<16)
                            continue; // retry
                        throw new IOException("Failed to call kern.proc.all: "+LIBC.strerror(Native.getLastError()));
                    }
                    break;
                }

                int count = size.getValue()/sizeOf_kinfo_proc;
                LOGGER.fine("Found "+count+" processes");

                for( int base=0; base<size.getValue(); base+=sizeOf_kinfo_proc) {
                    int pid = m.getInt(base+ kinfo_proc_pid_offset);
                    int ppid = m.getInt(base+ kinfo_proc_ppid_offset);
//                    int effective_uid = m.getInt(base+304);
//                    byte[] comm = new byte[16];
//                    m.read(base+163,comm,0,16);

                    super.processes.put(pid,new DarwinProcess(pid,ppid));
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to obtain process list",e);
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
                if(envVars !=null)
                    return envVars;
                parse();
                return envVars;
            }

            public List<String> getArguments() {
                if(arguments !=null)
                    return arguments;
                parse();
                return arguments;
            }

            private void parse() {
                try {
// allocate them first, so that the parse error wil result in empty data
                    // and avoid retry.
                    arguments = new ArrayList<String>();
                    envVars = new EnvVars();

                    IntByReference _ = new IntByReference();

                    IntByReference argmaxRef = new IntByReference(0);
                    IntByReference size = new IntByReference(sizeOfInt);

                    // for some reason, I was never able to get sysctlbyname work.
//        if(LIBC.sysctlbyname("kern.argmax", argmaxRef.getPointer(), size, NULL, _)!=0)
                    if(LIBC.sysctl(new int[]{CTL_KERN,KERN_ARGMAX},2, argmaxRef.getPointer(), size, NULL, _)!=0)
                        throw new IOException("Failed to get kern.argmax: "+LIBC.strerror(Native.getLastError()));

                    int argmax = argmaxRef.getValue();

                    class StringArrayMemory extends Memory {
                        private long offset=0;

                        StringArrayMemory(long l) {
                            super(l);
                        }

                        int readInt() {
                            int r = getInt(offset);
                            offset+=sizeOfInt;
                            return r;
                        }

                        byte peek() {
                            return getByte(offset);
                        }

                        String readString() {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte ch;
                            while((ch = getByte(offset++))!='\0')
                                baos.write(ch);
                            return baos.toString();
                        }

                        void skip0() {
                            // skip padding '\0's
                            while(getByte(offset)=='\0')
                                offset++;
                        }
                    }
                    StringArrayMemory m = new StringArrayMemory(argmax);
                    size.setValue(argmax);
                    if(LIBC.sysctl(new int[]{CTL_KERN,KERN_PROCARGS2,pid},3, m, size, NULL, _)!=0)
                        throw new IOException("Failed to obtain ken.procargs2: "+LIBC.strerror(Native.getLastError()));


                    /*
                    * Make a sysctl() call to get the raw argument space of the
                        * process.  The layout is documented in start.s, which is part
                        * of the Csu project.  In summary, it looks like:
                        *
                        * /---------------\ 0x00000000
                        * :               :
                        * :               :
                        * |---------------|
                        * | argc          |
                        * |---------------|
                        * | arg[0]        |
                        * |---------------|
                        * :               :
                        * :               :
                        * |---------------|
                        * | arg[argc - 1] |
                        * |---------------|
                        * | 0             |
                        * |---------------|
                        * | env[0]        |
                        * |---------------|
                        * :               :
                        * :               :
                        * |---------------|
                        * | env[n]        |
                        * |---------------|
                        * | 0             |
                        * |---------------| <-- Beginning of data returned by sysctl()
                        * | exec_path     |     is here.
                        * |:::::::::::::::|
                        * |               |
                        * | String area.  |
                        * |               |
                        * |---------------| <-- Top of stack.
                        * :               :
                        * :               :
                        * \---------------/ 0xffffffff
                        */

                    // I find the Darwin source code of the 'ps' command helpful in understanding how it does this:
                    // see http://www.opensource.apple.com/source/adv_cmds/adv_cmds-147/ps/print.c
                    int argc = m.readInt();
                    String args0 = m.readString(); // exec path
                    m.skip0();
                    try {
                        for( int i=0; i<argc; i++) {
                            arguments.add(m.readString());
                        }
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalStateException("Failed to parse arguments: pid="+pid+", arg0="+args0+", arguments="+arguments+", nargs="+argc+". Please see https://jenkins.io/redirect/troubleshooting/darwin-failed-to-parse-arguments",e);
                    }

                    // read env vars that follow
                    while(m.peek()!=0)
                        envVars.addLine(m.readString());
                } catch (IOException e) {
                    // this happens with insufficient permissions, so just ignore the problem.
                }
            }
        }

        // local constants
        private final int sizeOf_kinfo_proc;
        private static final int sizeOf_kinfo_proc_32 = 492; // on 32bit Mac OS X.
        private static final int sizeOf_kinfo_proc_64 = 648; // on 64bit Mac OS X.
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
        private static int[] MIB_PROC_ALL = {CTL_KERN, KERN_PROC, KERN_PROC_ALL};
        private static final int KERN_ARGMAX = 8;
        private static final int KERN_PROCARGS2 = 49;
    }

    /**
     * Represents a local process tree, where this JVM and the process tree run on the same system.
     * (The opposite of {@link Remote}.)
     */
    public static abstract class Local extends ProcessTree {
        @Deprecated
        Local() {
        }
        
        Local(boolean vetoesExist) {
            super(vetoesExist);
        }
    }

    /**
     * Represents a process tree over a channel.
     */
    public static class Remote extends ProcessTree implements Serializable {
        private final IProcessTree proxy;

        @Deprecated
        public Remote(ProcessTree proxy, Channel ch) {
            this.proxy = ch.export(IProcessTree.class,proxy);
            for (Entry<Integer,OSProcess> e : proxy.processes.entrySet())
                processes.put(e.getKey(),new RemoteProcess(e.getValue(),ch));
        }
        
        public Remote(ProcessTree proxy, Channel ch, boolean vetoersExist) {
            super(vetoersExist);
            
            this.proxy = ch.export(IProcessTree.class,proxy);
            for (Entry<Integer,OSProcess> e : proxy.processes.entrySet())
                processes.put(e.getKey(),new RemoteProcess(e.getValue(),ch));
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
            return this; // cancel out super.writeReplace()
        }

        private static final long serialVersionUID = 1L;

        private class RemoteProcess extends OSProcess implements Serializable {
            private final IOSProcess proxy;

            RemoteProcess(OSProcess proxy, Channel ch) {
                super(proxy.getPid());
                this.proxy = ch.export(IOSProcess.class,proxy);
            }

            public OSProcess getParent() {
                IOSProcess p = proxy.getParent();
                if (p==null)    return null;
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
                return this; // cancel out super.writeReplace()
            }

            public <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException {
                return proxy.act(callable);
            }


            private static final long serialVersionUID = 1L;
        }
    }

    /**
     * Use {@link Remote} as the serialized form.
     */
    /*package*/ Object writeReplace() {
        return new Remote(this,Channel.current());
    }

//    public static void main(String[] args) {
//        // dump everything
//        LOGGER.setLevel(Level.ALL);
//        ConsoleHandler h = new ConsoleHandler();
//        h.setLevel(Level.ALL);
//        LOGGER.addHandler(h);
//
//        Solaris killer = (Solaris)get();
//        Solaris.SolarisSystem s = killer.createSystem();
//        Solaris.SolarisProcess p = s.get(Integer.parseInt(args[0]));
//        System.out.println(p.getEnvVars());
//
//        if(args.length==2)
//            p.kill();
//    }

    /*
        On MacOS X, there's no procfs <http://www.osxbook.com/book/bonus/chapter11/procfs/>
        instead you'd do it with the sysctl <http://search.cpan.org/src/DURIST/Proc-ProcessTable-0.42/os/darwin.c>
        <http://developer.apple.com/documentation/Darwin/Reference/ManPages/man3/sysctl.3.html>

        There's CLI but that doesn't seem to offer the access to per-process info
        <http://developer.apple.com/documentation/Darwin/Reference/ManPages/man8/sysctl.8.html>



        On HP-UX, pstat_getcommandline get you command line, but I'm not seeing any environment variables.
     */

    private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
    private static final Logger LOGGER = Logger.getLogger(ProcessTree.class.getName());

    /**
     * Flag to control this feature.
     *
     * <p>
     * This feature involves some native code, so we are allowing the user to disable this
     * in case there's a fatal problem.
     *
     * <p>
     * This property supports two names for a compatibility reason.
     */
    public static boolean enabled = !SystemProperties.getBoolean(ProcessTreeKiller.class.getName()+".disable")
                                 && !SystemProperties.getBoolean(ProcessTree.class.getName()+".disable");
}
