package injectors

import com.google.inject.*
import com.google.inject.multibindings.Multibinder

import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor

import services.commitFilters.InCommitListMutuallyModifiedMethodsFilter
import services.commitFilters.MutuallyModifiedFilesCommitFilter
import services.dataCollectors.buildRequester.BuildRequester
import services.dataCollectors.MergeConflictCollector
import services.dataCollectors.StatisticsCollector
import services.dataCollectors.modifiedLinesCollector.ModifiedFilesLinesPerFileCollector
import services.outputProcessors.FetchBuildsOutputProcessor
import services.outputProcessors.GenerateSootInputFilesOutputProcessor
import services.outputProcessors.WaitForBuildsOutputProcessor
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor
import services.projectProcessors.FilterNonExistentProjectsProcessor
import services.projectProcessors.ForkAndEnableCIProcessor
import services.util.ci.CIPlatform
import services.util.ci.GithubActionsPlatform
import services.util.ci.TravisPlatform
import services.util.FetchBuildsScript

class DynamicAnalysisJsModifiedLinesDetectionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(ModifiedFilesLinesPerFileCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollector.class)
        
        bind(CommitFilter.class).to(MutuallyModifiedFilesCommitFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
    }

}