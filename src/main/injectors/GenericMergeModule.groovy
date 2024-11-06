package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.common.RunNormalizationOnScenarioFilesDataCollector
import services.dataCollectors.mergeToolExecutors.GitMergeFileMergeToolDataCollector
import services.dataCollectors.mergeToolExecutors.JDimeMergeToolExecutorDataCollector
import services.dataCollectors.mergeToolExecutors.LastMergeMergeToolExecutorDataCollector
import services.dataCollectors.mergeToolExecutors.SporkMergeToolExecutorDataCollector
import services.outputProcessors.genericMerge.TriggerBuildAndTestsOutputProcessor
import services.projectProcessors.DummyProjectProcessor

class GenericMergeModule extends AbstractModule {
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

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
