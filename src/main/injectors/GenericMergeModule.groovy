package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import services.commitFilters.NonFastForwardMergeCommitFilter
import services.dataCollectors.GenericMerge.GenericMergeConfig
import services.dataCollectors.common.RunNormalizationOnScenarioFilesDataCollector
import services.dataCollectors.mergeToolExecutors.GitMergeFileMergeToolDataCollector
import services.dataCollectors.mergeToolExecutors.JDimeMergeToolExecutorDataCollector
import services.dataCollectors.mergeToolExecutors.LastMergeMergeToolExecutorDataCollector
import services.dataCollectors.mergeToolExecutors.SporkMergeToolExecutorDataCollector
import services.outputProcessors.genericMerge.TriggerBuildAndTestsOutputProcessor
import services.projectProcessors.DummyProjectProcessor

import java.nio.file.Files
import java.nio.file.Paths

class GenericMergeModule extends AbstractModule {
    private static Logger LOG = LogManager.getLogger(GenericMergeModule.class)

    @Override
    protected void configure() {
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(DummyProjectProcessor.class)

        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesDataCollector(["base.java", "left.java", "right.java", "merge.java"]))
        dataCollectorBinder.addBinding().to(JDimeMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(LastMergeMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(SporkMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(GitMergeFileMergeToolDataCollector.class)
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesDataCollector(["merge.spork.java", "merge.last_merge.java"]))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(TriggerBuildAndTestsOutputProcessor.class)

        bind(CommitFilter.class).to(NonFastForwardMergeCommitFilter.class)

        createExecutionReportsFile()
    }

    private static void createExecutionReportsFile() {
        LOG.info("Creating Generic Merge report file")
        Files.createDirectories(Paths.get(GenericMergeConfig.GENERIC_MERGE_REPORT_PATH))
        def reportFile = new File(GenericMergeConfig.GENERIC_MERGE_REPORT_FILE_NAME)
        reportFile.createNewFile()
    }
}
