package services.outputProcessors

import interfaces.OutputProcessor

class GenericMergeDataOutputProcessor implements OutputProcessor{
    @Override
    void processOutput() {
        println "Processing output"
    }
}
