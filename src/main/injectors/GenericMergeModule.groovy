package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.common.RunNormalizationOnScenarioFilesWithSporkDataCollector
import services.dataCollectors.common.SyntacticallyCompareScenarioFilesDataCollector
import services.outputProcessors.EmptyOutputProcessor
import services.projectProcessors.DummyProjectProcessor

class GenericMergeModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(DummyProjectProcessor.class)

        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        // Run the merge tools on the scenarios
//        dataCollectorBinder.addBinding().to(JDimeMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(LastMergeMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(SporkMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(GitMergeFileMergeToolDataCollector.class)

        // Normalize the output files formatting due to jDime pretty-printing
//        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesDataCollector(["merge.java", "merge.spork.java", "merge.last_merge.java"]))


        // Normalize the output files formatting due to Spork pretty-printing
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesWithSporkDataCollector("merge.java", "merge.spork_normalized.java"))
        dataCollectorBinder.addBinding().toInstance(new RunNormalizationOnScenarioFilesWithSporkDataCollector("merge.last_merge.java", "merge.last_merge.spork_normalized.java"))

        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.spork_normalized.java", "merge.spork.java"))

        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.java", "merge.spork_normalized.java"))


        // Run comparisons between tools themselves
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.spork.java"))
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.jdime.java"))
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime.java", "merge.spork.java"))
//
//        // Run comparisons between tools and the original merge file
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime.java", "merge.java"))
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java", "merge.java"))
//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.java", "merge.java"))

        // Run comparisons between conflicts themselves
//        dataCollectorBinder.addBinding().toInstance(new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.spork.java"))
//        dataCollectorBinder.addBinding().toInstance(new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.jdime.java"))
//        dataCollectorBinder.addBinding().toInstance(new CompareScenarioMergeConflictsDataCollector("merge.jdime.java", "merge.spork.java"))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(EmptyOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
