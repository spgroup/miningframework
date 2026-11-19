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
import services.dataCollectors.buildRequester.LocalMergeBuildCollector
import services.dataCollectors.buildRequester.BuildRequester
import services.dataCollectors.modifiedLinesCollector.UnrestrictedModifiedLinesCollector
import services.outputProcessors.FetchBuildsFromJsonOutputProcessor
import services.outputProcessors.GenerateSootInputFilesFromJsonOutputProcessor
import services.outputProcessors.WaitForBuildsOutputProcessor
import services.projectProcessors.ForkAndEnableCIProcessor
import services.util.ci.CIPlatform
import services.util.ci.GithubActionsPlatform

class UnrestrictedStaticAnalysisConflictsDetectionModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(LocalMergeBuildCollector.class)
        dataCollectorBinder.addBinding().to(UnrestrictedModifiedLinesCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollector.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)
        dataCollectorBinder.addBinding().to(MergeConflictCollector.class)

        // Only requires that the commit is in commits.csv, no other restrictions
        bind(CommitFilter.class).to(IsInCommitListFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(ForkAndEnableCIProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(WaitForBuildsOutputProcessor.class)
        outputProcessorBinder.addBinding().to(FetchBuildsFromJsonOutputProcessor.class)
        outputProcessorBinder.addBinding().to(GenerateSootInputFilesFromJsonOutputProcessor.class)

        bind(CIPlatform.class).to(GithubActionsPlatform.class)
    }

}
