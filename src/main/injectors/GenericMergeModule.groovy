package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.common.CompareScenarioMergeConflictsDataCollector
import services.dataCollectors.common.RunDataCollectorsInParallel
import services.dataCollectors.common.RunNormalizationOnScenarioFilesDataCollector
import services.dataCollectors.common.SyntacticallyCompareScenarioFilesDataCollector
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
        // Normalize scenario files formatting due to jDime pretty-printing
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesDataCollector(["base.java", "left.java", "right.java", "merge.java"]))

        // Run the merge tools on the scenarios
        dataCollectorBinder.addBinding().to(JDimeMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(LastMergeMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(SporkMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(GitMergeFileMergeToolDataCollector.class)

        // Normalize the output files formatting due to jDime pretty-printing
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesDataCollector(["merge.spork.java", "merge.last_merge.java"]))

        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel([
                // Run comparisons between tools themselves
                new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.spork.java"),
                new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.jdime.java"),
                new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime.java", "merge.spork.java"),
                // Run comparisons between tools and the original merge file
                new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime.java", "merge.java"),
                new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.java"),
                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.java", "merge.java"),
                // Run comparisons between conflicts themselves
                new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.spork.java"),
                new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.jdime.java"),
                new CompareScenarioMergeConflictsDataCollector("merge.jdime.java", "merge.spork.java")]))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(TriggerBuildAndTestsOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
