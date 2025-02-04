package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.common.RunDataCollectorsInParallel
import services.dataCollectors.fileSyntacticNormalization.JDimeFileSyntacticNormalizationDataCollector
import services.dataCollectors.fileSyntacticNormalization.SporkFileSyntacticNormalizationDataCollector
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

        // Run, in parallel, normalizations on resulting files
        dataCollectorBinder.addBinding().toInstance(new RunDataCollectorsInParallel([
                new JDimeFileSyntacticNormalizationDataCollector("merge.java", "merge.jdime_normalized.java"),
                new JDimeFileSyntacticNormalizationDataCollector("merge.last_merge.java", "merge.last_merge.jdime_normalized.java"),
                new SporkFileSyntacticNormalizationDataCollector("merge.java", "merge.spork_normalized.java"),
                new SporkFileSyntacticNormalizationDataCollector("merge.spork.java", "merge.spork.spork_normalized.java"),
                new SporkFileSyntacticNormalizationDataCollector("merge.mergiraf.java", "merge.mergiraf.spork_normalized.java"),
        ]))

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(EmptyOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
    }
}
