package services.dataCollectors.fileSyntacticNormalization

import com.github.javaparser.JavaParser
import com.github.javaparser.printer.DefaultPrettyPrinter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class JavaParserFormatFileSyntacticNormalizationDataCollector extends BaseFileSyntacticNormalizationDataCollector {
    private static Logger LOG = LogManager.getLogger(JavaParserFormatFileSyntacticNormalizationDataCollector.class)

    JavaParserFormatFileSyntacticNormalizationDataCollector(String inputFile, String outputFile) {
        super(inputFile, outputFile)
    }

    @Override
    protected boolean runNormalizationOnFile(Path inputFile, Path outputFile) {
        def cu = new JavaParser().parse(inputFile.toFile())
        if (!cu.isSuccessful()) {
            return false
        }

        def prettyPrinter = new DefaultPrettyPrinter()
        def result = prettyPrinter.print(cu.getResult().get())

        Files.write(outputFile, result.getBytes(Charset.defaultCharset()),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)

        return true
    }
}
