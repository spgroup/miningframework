package integration

import interfaces.DataCollector
import interfaces.CommitFilter
import interfaces.ProjectProcessor
import interfaces.OutputProcessor
import services.commitFilters.InCommitListAndHasMutuallyModifiedMethodsFilter

import util.*
import services.*

import services.modifiedLinesCollector.ModifiedLinesCollector

import com.google.inject.*
import com.google.inject.multibindings.Multibinder

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class);
        Multibinder<ProjectProcessor> projectProcessorBinder = Multibinder.newSetBinder(binder(), ProjectProcessor.class)

        dataCollectorBinder.addBinding().to(ModifiedLinesCollector.class);
        dataCollectorBinder.addBinding().to(StatisticsCollectorImpl.class);
        bind(CommitFilter.class).to(InCommitListAndHasMutuallyModifiedMethodsFilter.class)

        projectProcessorBinder.addBinding().to(EmptyProjectProcessor.class)
        bind(OutputProcessor.class).to(EmptyOutputProcessor.class)
    }

}