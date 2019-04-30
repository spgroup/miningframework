package services

import main.interfaces.ExperimentalDataCollector
import main.interfaces.StatisticsCollector
import main.interfaces.CommitFilter
import main.interfaces.ProjectProcessor

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.*

public class MiningModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ExperimentalDataCollector.class).to(ExperimentalDataCollectorImpl.class)
        bind(StatisticsCollector.class).to(StatisticsCollectorImpl.class)
        bind(CommitFilter.class).to(CommitFilterImpl.class)
        bind(ProjectProcessor.class).to(ProjectProcessorImpl.class)
    }

}