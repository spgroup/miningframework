package injectors

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder

import interfaces.*
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.MergirafAndSepMergeCollector.MergesCollector
import services.outputProcessors.EmptyOutputProcessor
import services.projectProcessors.DummyProjectProcessor

class MergirafAndSepMergeModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorsBinder = Multibinder.newSetBinder(binder(), DataCollector)
        dataCollectorsBinder.addBinding().to(MergesCollector)

        Multibinder<ProjectProcessor> projectProcessorsBinder = Multibinder.newSetBinder(binder(), ProjectProcessor)
        projectProcessorsBinder.addBinding().to(DummyProjectProcessor)

        Multibinder<OutputProcessor> outputProcessorsBinder = Multibinder.newSetBinder(binder(), OutputProcessor)
        outputProcessorsBinder.addBinding().to(EmptyOutputProcessor)

        bind(CommitFilter).to(MutuallyModifiedFilesCommitFilter)
    }
}
