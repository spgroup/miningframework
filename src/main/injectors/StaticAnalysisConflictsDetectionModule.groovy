package injectors

import com.google.inject.*
import com.google.inject.multibindings.Multibinder

import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor

import services.commitFilters.InCommitListMutuallyModifiedStaticBlocksFilter
import services.dataCollectors.BuildRequester
import services.dataCollectors.MergeConflictCollector
import services.dataCollectors.StatisticsCollector
import services.dataCollectors.modifiedLinesCollector.ModifiedLinesCollector
import services.outputProcessors.FetchBuildsOutputProcessor
import services.outputProcessors.GenerateSootInputFilesOutputProcessor
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor
import services.projectProcessors.FilterNonExistentProjectsProcessor
import services.projectProcessors.ForkAndEnableTravisProcessor

class StaticAnalysisConflictsDetectionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(ModifiedLinesCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollector.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)
        dataCollectorBinder.addBinding().to(MergeConflictCollector.class)
        
        bind(CommitFilter.class).to(InCommitListMutuallyModifiedStaticBlocksFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)
        projectProcessorBinder.addBinding().to(ForkAndEnableTravisProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(FetchBuildsOutputProcessor.class)
        outputProcessorBinder.addBinding().to(GenerateSootInputFilesOutputProcessor.class)
        outputProcessorBinder.addBinding().to(RunSootAnalysisOutputProcessor.class)
    }

}