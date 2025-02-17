package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.buildRequester.RequestBuildForRevisionWithFilesDataCollector
import services.dataCollectors.common.CompareScenarioMergeConflictsDataCollector
import services.dataCollectors.common.RunDataCollectorsInParallel
import services.dataCollectors.common.SyntacticallyCompareScenarioFilesDataCollector
import services.dataCollectors.fileSyntacticNormalization.FormatFileSyntacticNormalizationDataCollector
import services.outputProcessors.EmptyOutputProcessor
import services.projectProcessors.DummyProjectProcessor

class GenericMergeModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(DummyProjectProcessor.class)

        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

//        // Run the merge tools on the scenarios
//        dataCollectorBinder.addBinding().to(JDimeMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(LastMergeMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(SporkMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(MergirafMergeToolExecutorDataCollector.class)

//        // Run, in parallel, normalizations on resulting files
//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel([
//                new JDimeFileSyntacticNormalizationDataCollector("merge.java", "merge.jdime_normalized.java"),
//                new JDimeFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.jdime_normalized.java"),
//                new SporkFileSyntacticNormalizationDataCollector("merge.java", "merge.spork_normalized.java"),
//                new SporkFileSyntacticNormalizationDataCollector("merge.spork.java", "merge.spork.spork_normalized.java"),
//                new SporkFileSyntacticNormalizationDataCollector("merge.mergiraf.java", "merge.mergiraf.spork_normalized.java"),
//        ]))

        // Run, in parallel, syntactical comparisons between files and textual comparison between commits
//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel([
//                // Syntactic comparison with merge commits
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.java", "merge.last_merge.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime_normalized.java", "merge.jdime.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork_normalized.java", "merge.spork.spork_normalized.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.java", "merge.mergiraf.java"),
//
//                // Syntactic comparison between tools themselves
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.jdime.java", "merge.last_merge.jdime_normalized.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.mergiraf.spork_normalized.java", "merge.spork.spork_normalized.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.mergiraf.java", "merge.last_merge.java"),

                // Conflicts comparison between tools themselves
//                new CompareScenarioMergeConflictsDataCollector("merge.jdime.java", "merge.last_merge.java"),
//                new CompareScenarioMergeConflictsDataCollector("merge.mergiraf.java", "merge.spork.java"),
//                new CompareScenarioMergeConflictsDataCollector("merge.mergiraf.java", "merge.last_merge.java"),
//        ]))

//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel([new FormatFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.format_normalized.java"),
//                                                                                     new FormatFileSyntacticNormalizationDataCollector("merge.mergiraf.java", "merge.mergiraf.format_normalized.java")]))

//        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.mergiraf.format_normalized.java", "merge.last_merge.format_normalized.java"))


        dataCollectorBinder.addBinding().toInstance(new RequestBuildForRevisionWithFilesDataCollector("merge.spork.java"))


        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(EmptyOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
