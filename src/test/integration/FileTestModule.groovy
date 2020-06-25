package integration

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.CommitFilter
import interfaces.DataCollector
import interfaces.OutputProcessor
import interfaces.ProjectProcessor
import services.commitFilters.InCommitListMutuallyModifiedMethodsFilter
import services.dataCollectors.BuildRequester
import services.dataCollectors.StatisticsCollector
import services.dataCollectors.modifiedLinesCollector.ModifiedLinesCollector
import services.outputProcessors.GenerateSootInputFilesOutputProcessor
import services.outputProcessors.soot.RunSootAnalysisOutputProcessor
import services.projectProcessors.FilterNonExistentProjectsProcessor

public class FileTestModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(ModifiedLinesCollector.class)
        dataCollectorBinder.addBinding().to(StatisticsCollector.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)

        bind(CommitFilter.class).to(InCommitListMutuallyModifiedMethodsFilter.class)

        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)
        projectProcessorBinder.addBinding().to(FilterNonExistentProjectsProcessor.class)

        Multibinder<OutputProcessor> outputProcessorBinder = Multibinder.newSetBinder(binder(), OutputProcessor.class)
        outputProcessorBinder.addBinding().to(GenerateSootInputFilesOutputProcessor.class)
        outputProcessorBinder.addBinding().to(RunSootAnalysisOutputProcessor.class)
    }

}