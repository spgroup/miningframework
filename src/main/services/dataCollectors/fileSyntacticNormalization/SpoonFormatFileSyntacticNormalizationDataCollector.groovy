package services.dataCollectors.fileSyntacticNormalization

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import spoon.Launcher
import spoon.compiler.Environment
import spoon.reflect.visitor.DefaultJavaPrettyPrinter
import spoon.support.compiler.FileSystemFile

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class SpoonFormatFileSyntacticNormalizationDataCollector extends BaseFileSyntacticNormalizationDataCollector {
    private static Logger LOG = LogManager.getLogger(SpoonFormatFileSyntacticNormalizationDataCollector.class)

    SpoonFormatFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        super(inputFile, outputFile)
    }

    @Override
    protected boolean runNormalizationOnFile(Path inputFile, Path outputFile) {
        try {
            LOG.info("Starting compilation")
            def launcher = new Launcher()

            launcher.addInputResource(new FileSystemFile(inputFile.toFile()))
            launcher.getEnvironment().setPrettyPrintingMode(Environment.PRETTY_PRINTING_MODE.FULLYQUALIFIED)
            launcher.getEnvironment().noClasspath = true;

            def model = launcher.buildModel()
            def cu = model.getUnnamedModule().factory.CompilationUnit()
            def result = cu.getMap().values().first().prettyprint()

            LOG.info("Finished compilation")
            Files.write(outputFile, result.getBytes(Charset.defaultCharset()),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)
            return true
        } catch (Exception e) {
            LOG.warn("Transformation failed with ${e.getMessage()}")
            LOG.warn(e)
            return false
        }
    }
}
