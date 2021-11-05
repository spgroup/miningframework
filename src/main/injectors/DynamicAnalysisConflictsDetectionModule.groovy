package injectors

import com.google.inject.*
import com.google.inject.multibindings.Multibinder

import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor

import services.commitFilters.InCommitListMutuallyModifiedMethodsFilter

import services.dataCollectors.buildRequester.BuildRequesterDynamicSemanticStudy
import services.dataCollectors.modifiedLinesCollector.ModifiedLinesCollectorDynamicSemanticStudy

import services.outputProcessors.FetchBuildsOutputProcessor
import services.outputProcessors.WaitForBuildsOutputProcessor

import services.projectProcessors.FilterNonExistentProjectsProcessor
import services.projectProcessors.ForkAndEnableCIProcessor

import services.util.ci.CIPlatform
import services.util.ci.GithubActionsPlatform

import services.util.FetchBuildsScript

class DynamicAnalysisConflictsDetectionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(ModifiedLinesCollectorDynamicSemanticStudy.class)
        dataCollectorBinder.addBinding().to(BuildRequesterDynamicSemanticStudy.class)
        
        bind(CommitFilter.class).to(InCommitListMutuallyModifiedMethodsFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)
        projectProcessorBinder.addBinding().to(ForkAndEnableCIProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(WaitForBuildsOutputProcessor.class)

        FetchBuildsOutputProcessor fetchBuildsOutputProcessor = new FetchBuildsOutputProcessor(FetchBuildsScript.MultipleBuildsPerScenario)
        outputProcessorBinder.addBinding().toInstance(fetchBuildsOutputProcessor)
        
        bind(CIPlatform.class).to(GithubActionsPlatform.class)
    }

}