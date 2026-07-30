package injectors

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.IsInCommitListFilter
import services.dataCollectors.MergeConflictCollector
import services.dataCollectors.StatisticsCollector
import services.dataCollectors.buildRequester.BuildRequester
import services.dataCollectors.modifiedLinesCollector.UnrestrictedModifiedLinesCollector
import services.outputProcessors.FetchBuildsOutputProcessor
import services.outputProcessors.GenerateSootInputFilesOutputProcessor
import services.outputProcessors.WaitForBuildsOutputProcessor
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor
import services.projectProcessors.FilterNonExistentProjectsProcessor
import services.projectProcessors.ForkAndEnableCIProcessor
import services.util.FetchBuildsScript
import services.util.ci.CIPlatform
import services.util.ci.GithubActionsPlatform

class UnrestrictedStaticAnalysisConflictsDetectionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(UnrestrictedModifiedLinesCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollector.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)
        dataCollectorBinder.addBinding().to(MergeConflictCollector.class)

        // Only requires that the commit is in commits.csv, no other restrictions
        bind(CommitFilter.class).to(IsInCommitListFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        // projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)
        projectProcessorBinder.addBinding().to(ForkAndEnableCIProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(WaitForBuildsOutputProcessor.class)

        FetchBuildsOutputProcessor fetchBuildsOutputProcessor = new FetchBuildsOutputProcessor(FetchBuildsScript.SingleBuildPerScenario)
        outputProcessorBinder.addBinding().toInstance(fetchBuildsOutputProcessor)

        bind(CIPlatform.class).to(GithubActionsPlatform.class)
    }

}
