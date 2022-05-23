package injectors

import com.google.inject.*
import com.google.inject.multibindings.Multibinder

import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.dataCollectors.BuildRequester
import services.dataCollectors.MergeConflictCollector
import services.dataCollectors.StatisticsCollector
import services.dataCollectors.staticBlockCollector.MergesCollector
import services.projectProcessors.DummyProjectProcessor
import services.outputProcessors.EmptyOutputProcessor
import services.commitFilters.InCommitListMutuallyModifiedStaticBlocksFilter


class StaticBlockAnalysisModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorsBinder = Multibinder.newSetBinder(binder(), DataCollector)
        dataCollectorsBinder.addBinding().to(MergesCollector)
        dataCollectorsBinder.addBinding().to(StatisticsCollector.class)
        dataCollectorsBinder.addBinding().to(BuildRequester.class)
        dataCollectorsBinder.addBinding().to(MergeConflictCollector.class)

        Multibinder<ProjectProcessor> projectProcessorsBinder = Multibinder.newSetBinder(binder(), ProjectProcessor)
        projectProcessorsBinder.addBinding().to(DummyProjectProcessor)

        Multibinder<OutputProcessor> outputProcessorsBinder = Multibinder.newSetBinder(binder(), OutputProcessor)
        outputProcessorsBinder.addBinding().to(EmptyOutputProcessor)

        bind(CommitFilter).to(InCommitListMutuallyModifiedStaticBlocksFilter)  
	}
}