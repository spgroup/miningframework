package services

import main.interfaces.DataCollector
import main.interfaces.CommitFilter
import main.interfaces.ProjectProcessor
import main.interfaces.OutputProcessor

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.*
import com.google.inject.multibindings.Multibinder

public class MiningModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(ExperimentalDataCollectorImpl.class)
        dataCollectorBinder.addBinding().to(StatisticsCollectorImpl.class)
        dataCollectorBinder.addBinding().to(BuildRequester.class)

        bind(CommitFilter.class).to(CommitFilterImpl.class)
        bind(ProjectProcessor.class).to(ProjectProcessorImpl.class)
        bind(OutputProcessor.class).to(OutputProcessorImpl.class)
    }

}