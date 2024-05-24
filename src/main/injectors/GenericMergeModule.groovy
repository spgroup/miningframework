package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.commitFilters.S3MCommitFilter
import services.dataCollectors.GenericMerge.GenericMergeDataCollector
import services.outputProcessors.GenericMergeDataOutputProcessor
import services.projectProcessors.DummyProjectProcessor
import services.util.ci.CIPlatform
import services.util.ci.TravisPlatform

class GenericMergeModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(DummyProjectProcessor.class)

        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)
        dataCollectorBinder.addBinding().to(GenericMergeDataCollector.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(GenericMergeDataOutputProcessor.class)

        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)
        bind(CIPlatform.class).to(TravisPlatform.class)
    }
}
