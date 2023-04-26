package org.gradle.language.scala.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.gradle.api.Incubating;
import org.gradle.api.JavaVersion;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.tasks.compile.CleaningJavaCompiler;
import org.gradle.api.internal.tasks.compile.CompilerForkUtils;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.internal.tasks.compile.MinimalJavaCompilerDaemonForkOptions;
import org.gradle.api.internal.tasks.scala.DefaultScalaJavaJointCompileSpec;
import org.gradle.api.internal.tasks.scala.DefaultScalaJavaJointCompileSpecFactory;
import org.gradle.api.internal.tasks.scala.MinimalScalaCompileOptions;
import org.gradle.api.internal.tasks.scala.ScalaCompileSpec;
import org.gradle.api.internal.tasks.scala.ScalaJavaJointCompileSpec;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.LocalState;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.scala.IncrementalCompileOptions;
import org.gradle.api.tasks.scala.ScalaCompileOptions;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.jvm.Jvm;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.util.internal.GFileUtils;
import org.gradle.work.DisableCachingByDefault;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import static com.google.common.base.Preconditions.checkState;

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
public abstract class AbstractScalaCompile extends AbstractCompile implements HasCompileOptions {

    protected static final Logger LOGGER = Logging.getLogger(AbstractScalaCompile.class);

    private final BaseScalaCompileOptions scalaCompileOptions;

    private final CompileOptions compileOptions;

    private final RegularFileProperty analysisMappingFile;

    private final ConfigurableFileCollection analysisFiles;

    private final Property<JavaLauncher> javaLauncher;

    {
        ObjectFactory objectFactory = getObjectFactory();
        this.analysisMappingFile = objectFactory.fileProperty();
        this.analysisFiles = getProject().files();
        this.compileOptions = objectFactory.newInstance(CompileOptions.class);
        this.javaLauncher = objectFactory.property(JavaLauncher.class);
        CompilerForkUtils.doNotCacheIfForkingViaExecutable(compileOptions, getOutputs());
    }

    @Incubating
    protected AbstractScalaCompile() {
        ObjectFactory objectFactory = getObjectFactory();
        this.scalaCompileOptions = objectFactory.newInstance(ScalaCompileOptions.class);
        this.scalaCompileOptions.setIncrementalOptions(objectFactory.newInstance(IncrementalCompileOptions.class));
    }

    @Deprecated
    @SuppressWarnings("unused")
    protected AbstractScalaCompile(BaseScalaCompileOptions scalaCompileOptions) {
        this.scalaCompileOptions = scalaCompileOptions;
        this.scalaCompileOptions.setIncrementalOptions(getObjectFactory().newInstance(IncrementalCompileOptions.class));
    }

    @Nested
    public BaseScalaCompileOptions getScalaCompileOptions() {
        return scalaCompileOptions;
    }

    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }

    abstract protected Compiler<ScalaJavaJointCompileSpec> getCompiler(ScalaJavaJointCompileSpec spec);

    @TaskAction
    public void compile() {
        ScalaJavaJointCompileSpec spec = createSpec();
        configureIncrementalCompilation(spec);
        Compiler<ScalaJavaJointCompileSpec> compiler = getCompiler(spec);
        if (isNonIncrementalCompilation()) {
            compiler = new CleaningJavaCompiler<>(compiler, getOutputs(), getDeleter());
        }
        compiler.execute(spec);
    }

    @Incubating
    @Nested
    @Optional
    public Property<JavaLauncher> getJavaLauncher() {
        return javaLauncher;
    }

    private boolean isNonIncrementalCompilation() {
        File analysisFile = getScalaCompileOptions().getIncrementalOptions().getAnalysisFile().getAsFile().get();
        if (!analysisFile.exists()) {
            LOGGER.info("Zinc is doing a full recompile since the analysis file doesn't exist");
            return true;
        }
        return false;
    }

    @Nullable
    @Internal
    protected JavaInstallationMetadata getToolchain() {
        return javaLauncher.map(JavaLauncher::getMetadata).getOrNull();
    }

    protected ScalaJavaJointCompileSpec createSpec() {
        validateConfiguration();
        DefaultScalaJavaJointCompileSpec spec = new DefaultScalaJavaJointCompileSpecFactory(compileOptions, getToolchain()).create();
        spec.setSourceFiles(getSource().getFiles());
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setWorkingDir(getProjectLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(ImmutableList.copyOf(getClasspath()));
        configureCompatibilityOptions(spec);
        spec.setCompileOptions(getOptions());
        spec.setScalaCompileOptions(new MinimalScalaCompileOptions(scalaCompileOptions));
        spec.setAnnotationProcessorPath(compileOptions.getAnnotationProcessorPath() == null ? ImmutableList.of() : ImmutableList.copyOf(compileOptions.getAnnotationProcessorPath()));
        spec.setBuildStartTimestamp(getServices().get(BuildStartedTime.class).getStartTime());
        configureExecutable(spec.getCompileOptions().getForkOptions());
        return spec;
    }

    private void configureExecutable(MinimalJavaCompilerDaemonForkOptions forkOptions) {
        if (javaLauncher.isPresent()) {
            forkOptions.setExecutable(javaLauncher.get().getExecutablePath().getAsFile().getAbsolutePath());
        } else {
            forkOptions.setExecutable(Jvm.current().getJavaExecutable().getAbsolutePath());
        }
    }

    private void configureCompatibilityOptions(DefaultScalaJavaJointCompileSpec spec) {
        JavaInstallationMetadata toolchain = getToolchain();
        if (toolchain != null) {
            boolean isSourceOrTargetConfigured = false;
            if (super.getSourceCompatibility() != null) {
                spec.setSourceCompatibility(getSourceCompatibility());
                isSourceOrTargetConfigured = true;
            }
            if (super.getTargetCompatibility() != null) {
                spec.setTargetCompatibility(getTargetCompatibility());
                isSourceOrTargetConfigured = true;
            }
            if (!isSourceOrTargetConfigured) {
                String languageVersion = toolchain.getLanguageVersion().toString();
                spec.setSourceCompatibility(languageVersion);
                spec.setTargetCompatibility(languageVersion);
            }
        } else {
            spec.setSourceCompatibility(getSourceCompatibility());
            spec.setTargetCompatibility(getTargetCompatibility());
        }
    }

    private void validateConfiguration() {
        if (javaLauncher.isPresent()) {
            checkState(getOptions().getForkOptions().getJavaHome() == null, "Must not use `javaHome` property on `ForkOptions` together with `javaLauncher` property");
            checkState(getOptions().getForkOptions().getExecutable() == null, "Must not use `executable` property on `ForkOptions` together with `javaLauncher` property");
        }
    }

    private void configureIncrementalCompilation(ScalaCompileSpec spec) {
        IncrementalCompileOptions incrementalOptions = scalaCompileOptions.getIncrementalOptions();
        File analysisFile = incrementalOptions.getAnalysisFile().getAsFile().get();
        File classpathBackupDir = incrementalOptions.getClassfileBackupDir().getAsFile().get();
        Map<File, File> globalAnalysisMap = resolveAnalysisMappingsForOtherProjects();
        spec.setAnalysisMap(globalAnalysisMap);
        spec.setAnalysisFile(analysisFile);
        spec.setClassfileBackupDir(classpathBackupDir);
        if (incrementalOptions.getPublishedCode().isPresent()) {
            File publishedCode = incrementalOptions.getPublishedCode().getAsFile().get();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("scala-incremental Analysis file: {}", analysisFile);
                LOGGER.debug("scala-incremental Classfile backup dir: {}", classpathBackupDir);
                LOGGER.debug("scala-incremental Published code: {}", publishedCode);
            }
            File analysisMapping = getAnalysisMappingFile().getAsFile().get();
            GFileUtils.writeFile(publishedCode.getAbsolutePath() + "\n" + analysisFile.getAbsolutePath(), analysisMapping);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("scala-incremental Analysis map: {}", globalAnalysisMap);
        }
    }

    private Map<File, File> resolveAnalysisMappingsForOtherProjects() {
        Map<File, File> analysisMap = Maps.newHashMap();
        for (File mapping : analysisFiles.getFiles()) {
            if (mapping.exists()) {
                try {
                    List<String> lines = Files.readLines(mapping, Charset.defaultCharset());
                    assert lines.size() == 2;
                    analysisMap.put(new File(lines.get(0)), new File(lines.get(1)));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return analysisMap;
    }

    @Override
    @PathSensitive(PathSensitivity.RELATIVE)
    public FileTree getSource() {
        return super.getSource();
    }

    @Input
    protected String getJvmVersion() {
        if (javaLauncher.isPresent()) {
            return javaLauncher.get().getMetadata().getLanguageVersion().toString();
        }
        return JavaVersion.current().getMajorVersion();
    }

    @Internal
    public ConfigurableFileCollection getAnalysisFiles() {
        return analysisFiles;
    }

    @LocalState
    public RegularFileProperty getAnalysisMappingFile() {
        return analysisMappingFile;
    }

    @Inject
    protected Deleter getDeleter() {
        throw new UnsupportedOperationException("Decorator takes care of injection");
    }

    @Inject
    protected ProjectLayout getProjectLayout() {
        throw new UnsupportedOperationException();
    }

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }
}
