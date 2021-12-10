/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, CloudBees, Inc.
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
package hudson.maven.reporters;

import hudson.FilePath;
import hudson.Util;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.model.Api;
import hudson.model.BuildListener;
import hudson.model.FingerprintMap;
import hudson.model.Run;
import hudson.util.LRUStringConverter;
import jenkins.model.Jenkins;

import hudson.util.HttpResponses;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;

import com.google.common.collect.Maps;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Captures information about an artifact created by Maven and archived by
 * Jenkins, so that we can later deploy it to repositories of our choice.
 *
 * <p>
 * This object is created within the Maven process and sent back to the master,
 * so it shouldn't contain anything non-serializable as fields.
 *
 * <p>
 * Once it's constructed, the object should be considered final and immutable.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.189
 */
@ExportedBean
public final class MavenArtifact implements Serializable {

    static {
        Run.XSTREAM.registerLocalConverter(MavenArtifact.class, "md5sum", new LRUStringConverter(5000));
    }

    /**
     * Basic parameters of a Maven artifact.
     */
    @Exported
    public final String groupId, artifactId, version, classifier, type;

    /**
     * File name (without directory portion) of this artifact in the Hudson archive.
     * Remembered explicitly because some times this doesn't follow the
     * standard naming convention, due to &lt;finalName> setting in POM.
     *
     * <p>
     * This name is taken directly from the name of the file as used during the build
     * (thus POM would be most likely just <tt>pom.xml</tt> and artifacts would
     * use their <tt>finalName</tt> if one is configured.) This is often
     * different from {@link #canonicalName}.
     */
    @Exported
    public final String fileName;

    /**
     * The canonical artifact file name, used by Maven in the repository.
     * This is <tt>artifactId-version[-classifier].extension</tt>.
     *
     * <p>
     * The reason we persist this is that the extension is only available
     * through {@link ArtifactHandler}. 
     */
    @Exported
    public final String canonicalName;

    /**
     * The md5sum for this artifact.
     */
    @Exported
    public final String md5sum;

    public MavenArtifact(Artifact a) throws IOException {
        this.groupId = a.getGroupId();
        this.artifactId = a.getArtifactId();
        this.version = a.getVersion();
        this.classifier = a.getClassifier();
        this.type = a.getType();
        this.fileName = a.getFile().getName();
        this.md5sum = Util.getDigestOf(new FileInputStream(a.getFile()));
        String extension;
        if(a.getArtifactHandler()!=null) // don't know if this can be null, but just to be defensive.
            extension = a.getArtifactHandler().getExtension();
        else
            extension = a.getType();

        canonicalName = getSeed(extension);
    }

    public MavenArtifact(String groupId, String artifactId, String version, String classifier, String type, String fileName, String md5sum) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.type = type;
        this.fileName = fileName;
        this.canonicalName = getSeed(type);
        this.md5sum = md5sum;
    }

    /**
     * Convenience method to check if the given {@link Artifact} object contains
     * enough information suitable for recording, and if so, create {@link MavenArtifact}.
     */
    public static MavenArtifact create(Artifact a) throws IOException {
        File file = a.getFile();
        if(file==null)
            return null; // perhaps build failed and didn't leave an artifact
        if(!file.isFile())
            return null; // file doesn't exist or artifact points to a directory
        return new MavenArtifact(a);
    }

    public boolean isPOM() {
        return fileName.endsWith(".pom")||"pom.xml".equals(fileName);   // hack
    }

    /**
     * Creates a Maven {@link Artifact} back from the persisted data.
     */
    public Artifact toArtifact(ArtifactHandlerManager handlerManager, ArtifactFactory factory, MavenBuild build) throws IOException {
        // Hack: presence of custom ArtifactHandler during builds could influence the file extension
        // in the repository during deployment. So simulate that behavior if that's necessary.
        final String canonicalExtension = canonicalName.substring(canonicalName.lastIndexOf('.')+1);
        ArtifactHandler ah = handlerManager.getArtifactHandler(type);
        Map<String,ArtifactHandler> handlers = Maps.newHashMap();
        
        handlers.put( type, new DefaultArtifactHandler(type) {
                        public String getExtension() {
                            return canonicalExtension;
                        } } );
        // Fix for HUDSON-3814 - changed from comparing against canonical extension to canonicalName.endsWith.
        if(!canonicalName.endsWith(ah.getExtension())) {
            handlerManager.addHandlers(handlers);
        }

        Artifact a = factory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
        a.setFile(getFile(build));
       
        return a;
    }

    /**
     * Computes the file name seed by taking &lt;finalName> POM entry into consideration.
     */
    private String getSeed(String extension) {
        String name = artifactId+'-'+version;
        if(Util.fixEmpty(classifier)!=null)
            name += '-'+classifier;
        name += '.'+extension;
        return name;
    }

    /**
     * Obtains the {@link File} representing the archived artifact.
     */
    public File getFile(MavenBuild build) throws IOException {
        File f = new File(new File(new File(new File(build.getArtifactsDir(), groupId), artifactId), version), canonicalName);
        if(!f.exists())
            throw new IOException("Archived artifact is missing: "+f);
        return f;
    }

    /**
     * Serve the file.
     *
     * TODO: figure out how to make this URL more discoverable to the remote API.
     */
    public HttpResponse doFile(@AncestorInPath MavenArtifactRecord parent) throws IOException {
        return HttpResponses.staticResource(getFile(parent.parent));
    }

    private FilePath getArtifactArchivePath(MavenBuildProxy build, String groupId, String artifactId, String version) {
        return build.getArtifactsDir().child(groupId).child(artifactId).child(version).child(canonicalName);
    }

    /**
     * Called from within Maven to archive an artifact in Hudson.
     */
    public void archive(MavenBuildProxy build, File file, BuildListener listener) throws IOException, InterruptedException {
        if (build.isArchivingDisabled()) {
            listener.getLogger().println("[JENKINS] Archiving disabled - not archiving " + file);
        }
        else {
            FilePath target = getArtifactArchivePath(build,groupId,artifactId,version);
            FilePath origin = new FilePath(file);
            if (!target.exists()) {
                listener.getLogger().println("[JENKINS] Archiving "+ file+" to "+target);
                origin.copyTo(target);
            } else if (!origin.digest().equals(target.digest())) {
                listener.getLogger().println("[JENKINS] Re-archiving "+file);
                origin.copyTo(target);
            } else {
                LOGGER.fine("Not actually archiving "+origin+" due to digest match");
            }

            /* debug probe to investigate "missing artifact" problem typically seen like this:

            ERROR: Asynchronous execution failure
            java.util.concurrent.ExecutionException: java.io.IOException: Archived artifact is missing: /files/hudson/server/jobs/glassfish-v3/modules/org.glassfish.build$maven-glassfish-extension/builds/2008-04-02_10-17-15/archive/org.glassfish.build/maven-glassfish-extension/1.0-SNAPSHOT/maven-glassfish-extension-1.0-SNAPSHOT.jar
                    at hudson.remoting.Channel$1.adapt(Channel.java:423)
                    at hudson.remoting.Channel$1.adapt(Channel.java:418)
                    at hudson.remoting.FutureAdapter.get(FutureAdapter.java:32)
                    at hudson.maven.MavenBuilder.call(MavenBuilder.java:140)
                    at hudson.maven.MavenModuleSetBuild$Builder.call(MavenModuleSetBuild.java:476)
                    at hudson.maven.MavenModuleSetBuild$Builder.call(MavenModuleSetBuild.java:422)
                    at hudson.remoting.UserRequest.perform(UserRequest.java:69)
                    at hudson.remoting.UserRequest.perform(UserRequest.java:23)
                    at hudson.remoting.Request$2.run(Request.java:200)
                    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:417)
                    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:269)
                    at java.util.concurrent.FutureTask.run(FutureTask.java:123)
                    at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:650)
                    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:675)
                    at java.lang.Thread.run(Thread.java:595)
            Caused by: java.io.IOException: Archived artifact is missing: /files/hudson/server/jobs/glassfish-v3/modules/org.glassfish.build$maven-glassfish-extension/builds/2008-04-02_10-17-15/archive/org.glassfish.build/maven-glassfish-extension/1.0-SNAPSHOT/maven-glassfish-extension-1.0-SNAPSHOT.jar
                    at hudson.maven.reporters.MavenArtifact.getFile(MavenArtifact.java:147)
                    at hudson.maven.reporters.MavenArtifact.toArtifact(MavenArtifact.java:126)
                    at hudson.maven.reporters.MavenArtifactRecord.install(MavenArtifactRecord.java:115)
                    at hudson.maven.reporters.MavenArtifactArchiver$1.call(MavenArtifactArchiver.java:81)
                    at hudson.maven.reporters.MavenArtifactArchiver$1.call(MavenArtifactArchiver.java:71)
                    at hudson.maven.MavenBuild$ProxyImpl.execute(MavenBuild.java:255)
                    at hudson.maven.MavenBuildProxy$Filter$AsyncInvoker.call(MavenBuildProxy.java:177)
                    at hudson.remoting.UserRequest.perform(UserRequest.java:69)
                    at hudson.remoting.UserRequest.perform(UserRequest.java:23)
                    at hudson.remoting.Request$2.run(Request.java:200)
                    at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:441)
                    at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
                    at java.util.concurrent.FutureTask.run(FutureTask.java:138)
                    at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:885)
                    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:907)
                    at java.lang.Thread.run(Thread.java:619)
         */

            if(!target.exists())
                throw new AssertionError("Just copied "+file+" to "+target+" but now I can't find it");
        }
    }

    /**
     * Called from within the master to record fingerprint.
     */
    public void recordFingerprint(MavenBuild build) throws IOException {
        FingerprintMap map = Jenkins.getInstance().getFingerprintMap();
        map.getOrCreate(build,fileName,md5sum);
    }

    public Api getApi() {
        return new Api(this);
    }

    private static final Logger LOGGER = Logger.getLogger(MavenArtifact.class.getName());

    private static final long serialVersionUID = 1L;
}
