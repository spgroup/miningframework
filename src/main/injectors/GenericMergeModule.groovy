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
import services.dataCollectors.common.SyntacticallyCompareScenarioFilesDataCollector
import services.dataCollectors.fileSyntacticNormalization.JavaParserFormatFileSyntacticNormalizationDataCollector
import services.dataCollectors.mergeToolExecutors.LastMergeMergeToolExecutorDataCollector
import services.dataCollectors.mergeToolExecutors.SporkMergeToolExecutorDataCollector
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

        // Normalize the files formatting by running Format on the resulting files.
        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel(new ArrayList<DataCollector>([new JavaParserFormatFileSyntacticNormalizationDataCollector("merge.java", "merge.java_parser_normalized.java"),
                                                                                                                  new JavaParserFormatFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.java_parser_normalized.java"),
                                                                                                                  new JavaParserFormatFileSyntacticNormalizationDataCollector("merge.spork.java", "merge.spork.java_parser_normalized.java")])))

        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel(new ArrayList<DataCollector>([
                // Syntactically compare both Spork and Last Merge files
                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.java_parser_normalized.java", "merge.last_merge.java_parser_normalized.java"),
                // Syntactically compare the tools with merge commit
                new SyntacticallyCompareScenarioFilesDataCollector("merge.spork.java_parser_normalized.java", "merge.java_parser_normalized.java"),
                new SyntacticallyCompareScenarioFilesDataCollector("merge.last_merge.java_parser_normalized.java", "merge.java_parser_normalized.java"),
                // Run comparisons between conflicts themselves
                new CompareScenarioMergeConflictsDataCollector("merge.last_merge.java", "merge.spork.java")])))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(EmptyOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
