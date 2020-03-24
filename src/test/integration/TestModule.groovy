package integration

import interfaces.DataCollector
import interfaces.CommitFilter
import interfaces.ProjectProcessor
import interfaces.OutputProcessor

import util.*
import services.*

import services.modifiedLinesCollector.ModifiedLinesCollector

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.*
import com.google.inject.multibindings.Multibinder

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class);

        dataCollectorBinder.addBinding().to(ModifiedLinesCollector.class);
        dataCollectorBinder.addBinding().to(StatisticsCollectorImpl.class);
        bind(CommitFilter.class).to(CommitFilterImpl.class)
        bind(ProjectProcessor.class).to(EmptyProjectProcessor.class)
        bind(OutputProcessor.class).to(EmptyOutputProcessor.class)
    }

}