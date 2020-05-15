package injectors

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.*
import services.commitFilters.S3MCommitFilter
import services.dataCollectors.S3MMergesCollector.MergesCollector
import services.outputProcessors.S3MOutputProcessor
import services.projectProcessors.ForkAndEnableTravisProcessor

class S3MMiningModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(MergesCollector.class)

        bind(CommitFilter.class).to(S3MCommitFilter.class)
        bind(ProjectProcessor.class).to(ForkAndEnableTravisProcessor.class)
        bind(OutputProcessor.class).to(S3MOutputProcessor.class)
    }

}
