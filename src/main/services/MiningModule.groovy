package services

import com.google.inject.*
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor

import services.commitFilters.InCommitListAndHasMutuallyModifiedMethodsFilter

import services.modifiedLinesCollector.ModifiedLinesCollector
import services.projectProcessors.FilterNonExistentProjectsProcessor
import services.projectProcessors.ForkAndEnableTravisProcessor

public class MiningModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)

        dataCollectorBinder.addBinding().to(MergeConflictCollector.class)
        dataCollectorBinder.addBinding().to(ModifiedLinesCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollectorImpl.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)

        bind(CommitFilter.class).to(InCommitListAndHasMutuallyModifiedMethodsFilter.class)

        projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)
        projectProcessorBinder.addBinding().to(ForkAndEnableTravisProcessor.class)

        bind(OutputProcessor.class).to(OutputProcessorImpl.class)
    }

}