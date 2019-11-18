package services.S3MHandlersAnalysis

@Grab('com.google.inject:guice:4.2.2')
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import main.interfaces.DataCollector
import services.S3MHandlersAnalysis.implementations.CommitFilter
import services.S3MHandlersAnalysis.implementations.MergesCollector
import services.S3MHandlersAnalysis.implementations.OutputProcessor
import services.S3MHandlersAnalysis.implementations.ProjectProcessor

class MiningModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<DataCollector> dataCollectorBinder = Multibinder.newSetBinder(binder(), DataCollector.class)

        dataCollectorBinder.addBinding().to(MergesCollector.class)

        bind(main.interfaces.CommitFilter.class).to(CommitFilter.class)
        bind(main.interfaces.ProjectProcessor.class).to(ProjectProcessor.class)
        bind(main.interfaces.OutputProcessor.class).to(OutputProcessor.class)
    }

}
