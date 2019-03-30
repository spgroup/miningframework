package services

import main.interfaces.DataCollector
import main.interfaces.StatisticsCollector
import main.interfaces.CommitFilter

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.*

public class MiningModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataCollector.class).to(DataCollectorImpl.class)
        bind(StatisticsCollector.class).to(StatisticsCollectorImpl.class)
        bind(CommitFilter.class).to(CommitFilterImpl.class)
    }

}