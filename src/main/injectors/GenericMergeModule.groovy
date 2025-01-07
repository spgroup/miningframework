package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.common.SyntacticallyCompareScenarioFilesDataCollector
import services.dataCollectors.fileSyntacticNormalization.SporkFileSyntacticNormalizationDataCollector
import services.dataCollectors.mergeToolExecutors.MergirafMergeToolExecutorDataCollector
import services.outputProcessors.EmptyOutputProcessor
import services.projectProcessors.DummyProjectProcessor

class GenericMergeModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(DummyProjectProcessor.class)

        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        // Run the merge tools on the scenarios
//        dataCollectorBinder.addBinding().to(LastMergeMergeToolExecutorDataCollector.class)
//        dataCollectorBinder.addBinding().to(SporkMergeToolExecutorDataCollector.class)
        dataCollectorBinder.addBinding().to(MergirafMergeToolExecutorDataCollector.class)

        dataCollectorBinder.addBinding().toInstance(new SporkFileSyntacticNormalizationDataCollector("merge.mergiraf.java", "merge.mergiraf.spork_normalized.java"))
        dataCollectorBinder.addBinding().toInstance(new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.spork_normalized.java", "merge.mergiraf.spork_normalized.java"))

//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel(new ArrayList<DataCollector>([new SpoonFormatFileSyntacticNormalizationDataCollector("merge.java", "merge.spoon_normalized.java"),
//                                                                                                                  new SpoonFormatFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.spoon_normalized.java"),
//                                                                                                                  new SpoonFormatFileSyntacticNormalizationDataCollector("merge.spork.java", "merge.spork.spoon_normalized.java")])))

//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel(new ArrayList<DataCollector>([new SporkFileSyntacticNormalizationDataCollector("merge.java", "merge.spork_normalized.java"),
//                                                                                                                  new SporkFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.spork_normalized.java"),
//                                                                                                                  new SporkFileSyntacticNormalizationDataCollector("merge.spork.java", "merge.spork.spork_normalized.java")])))


//        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel(new ArrayList<DataCollector>([
        // Syntactically compare both Spork and Last Merge files
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.spork_normalized.java", "merge.last_merge.spork_normalized.java"),
        // Syntactically compare the tools with merge commit
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.spork_normalized.java", "merge.spork_normalized.java"),
//                new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.spork_normalized.java", "merge.spork_normalized.java"),
//                 Run comparisons between conflicts themselves
//                new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.spork.java")])))

//        dataCollectorBinder.addBinding().toInstance(new RequestBuildForRevisionWithFilesDataCollector("merge.last_merge.java"))
//        dataCollectorBinder.addBinding().toInstance(new RequestBuildForRevisionWithFilesDataCollector("merge.spork.java"))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(EmptyOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
