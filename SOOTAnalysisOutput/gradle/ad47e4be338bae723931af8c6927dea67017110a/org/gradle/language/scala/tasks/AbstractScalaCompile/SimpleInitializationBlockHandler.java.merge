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
import org.gradle.api.internal.tasks.scala.DefaultScalaJavaJointCompileSpec;
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
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.scala.IncrementalCompileOptions;
import org.gradle.api.tasks.scala.ScalaCompileOptions;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.file.Deleter;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.util.internal.GFileUtils;
import org.gradle.work.DisableCachingByDefault;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
        JavaToolchainService javaToolchainService = getJavaToolchainService();
        this.javaLauncher = objectFactory.property(JavaLauncher.class).convention(javaToolchainService.launcherFor(it -> {
        }));
        CompilerForkUtils.doNotCacheIfForkingViaExecutable(compileOptions, getOutputs());
    }

    @Incubating
    protected AbstractScalaCompile() {
        ObjectFactory objectFactory = getObjectFactory();
        this.scalaCompileOptions = objectFactory.newInstance(ScalaCompileOptions.class);
        this.scalaCompileOptions.setIncrementalOptions(objectFactory.newInstance(IncrementalCompileOptions.class));
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

    @Nested
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

    @Internal
    protected JavaInstallationMetadata getToolchain() {
        return javaLauncher.map(JavaLauncher::getMetadata).get();
    }

    protected ScalaJavaJointCompileSpec createSpec() {
        File javaExecutable = getJavaLauncher().get().getExecutablePath().getAsFile();
        DefaultScalaJavaJointCompileSpec spec = new DefaultScalaJavaJointCompileSpec(javaExecutable);
        spec.setSourceFiles(getSource().getFiles());
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setOriginalDestinationDir(spec.getDestinationDir());
        spec.setWorkingDir(getProjectLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(ImmutableList.copyOf(getClasspath()));
        configureCompatibilityOptions(spec);
        spec.setCompileOptions(getOptions());
        spec.setScalaCompileOptions(new MinimalScalaCompileOptions(scalaCompileOptions));
        spec.setAnnotationProcessorPath(compileOptions.getAnnotationProcessorPath() == null ? ImmutableList.of() : ImmutableList.copyOf(compileOptions.getAnnotationProcessorPath()));
        spec.setBuildStartTimestamp(getServices().get(BuildStartedTime.class).getStartTime());
        return spec;
    }

    private void configureCompatibilityOptions(DefaultScalaJavaJointCompileSpec spec) {
        String toolchainVersion = JavaVersion.toVersion(getToolchain().getLanguageVersion().asInt()).toString();
        String sourceCompatibility = getSourceCompatibility();
        if (sourceCompatibility == null) {
            sourceCompatibility = toolchainVersion;
        }
        String targetCompatibility = getTargetCompatibility();
        if (targetCompatibility == null) {
            targetCompatibility = sourceCompatibility;
        }
        spec.setSourceCompatibility(sourceCompatibility);
        spec.setTargetCompatibility(targetCompatibility);
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
    protected abstract Deleter getDeleter();

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();
}