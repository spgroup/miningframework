package injectors

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import interfaces.DataCollector
import services.commitFilters.S3MCommitFilter

import services.outputProcessors.FetchBuildsOutputProcessor

class S3MMiningModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(MergesCollector.class)

        bind(main.interfaces.CommitFilter.class).to(S3MCommitFilter.class)
        bind(main.interfaces.ProjectProcessor.class).to(S3MProjectProcessor.class)
        bind(main.interfaces.OutputProcessor.class).to(FetchBuildsOutputProcessor.S3MOutputProcessor.class)
    }

}
