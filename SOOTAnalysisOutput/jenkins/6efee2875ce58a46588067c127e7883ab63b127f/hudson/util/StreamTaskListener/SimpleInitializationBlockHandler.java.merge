package hudson.util;

import hudson.CloseProofOutputStream;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.framework.io.WriterOutputStream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class StreamTaskListener extends AbstractTaskListener implements TaskListener, Closeable {

    @Nonnull
    private PrintStream out;

    @CheckForNull
    private Charset charset;

    @Deprecated
    public StreamTaskListener(@Nonnull PrintStream out) {
        this(out, null);
    }

    public StreamTaskListener(@Nonnull OutputStream out) {
        this(out, null);
    }

    public StreamTaskListener(@Nonnull OutputStream out, @CheckForNull Charset charset) {
        try {
            if (charset == null)
                this.out = (out instanceof PrintStream) ? (PrintStream) out : new PrintStream(out, false);
            else
                this.out = new PrintStream(out, false, charset.name());
            this.charset = charset;
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public StreamTaskListener(@Nonnull File out) throws IOException {
        this(out, null);
    }

    public StreamTaskListener(@Nonnull File out, @CheckForNull Charset charset) throws IOException {
        this(Files.newOutputStream(asPath(out)), charset);
    }

    private static Path asPath(@Nonnull File out) throws IOException {
        try {
            return out.toPath();
        } catch (InvalidPathException e) {
            throw new IOException(e);
        }
    }

    public StreamTaskListener(@Nonnull File out, boolean append, @CheckForNull Charset charset) throws IOException {
        this(Files.newOutputStream(asPath(out), StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING), charset);
    }

    public StreamTaskListener(@Nonnull Writer w) throws IOException {
        this(new WriterOutputStream(w));
    }

    @Deprecated
    public StreamTaskListener() throws IOException {
        this(new NullStream());
    }

    public static StreamTaskListener fromStdout() {
        return new StreamTaskListener(System.out, Charset.defaultCharset());
    }

    public static StreamTaskListener fromStderr() {
        return new StreamTaskListener(System.err, Charset.defaultCharset());
    }

    @Override
    public PrintStream getLogger() {
        return out;
    }

    @Override
    public Charset getCharset() {
        return charset != null ? charset : Charset.defaultCharset();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(new RemoteOutputStream(new CloseProofOutputStream(this.out)));
        out.writeObject(charset == null ? null : charset.name());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, null, new Throwable("serializing here with AUTO_FLUSH=" + AUTO_FLUSH));
        }
    }

    private static final String KEY_AUTO_FLUSH = StreamTaskListener.class.getName() + ".AUTO_FLUSH";

    static {
        SystemProperties.allowOnAgent(KEY_AUTO_FLUSH);
    }

    private static boolean AUTO_FLUSH = SystemProperties.getBoolean(KEY_AUTO_FLUSH);

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        out = new PrintStream((OutputStream) in.readObject(), AUTO_FLUSH);
        String name = (String) in.readObject();
        charset = name == null ? null : Charset.forName(name);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, null, new Throwable("deserializing here with AUTO_FLUSH=" + AUTO_FLUSH));
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public void closeQuietly() {
        try {
            close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to close", e);
        }
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(StreamTaskListener.class.getName());
}