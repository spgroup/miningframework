import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor


class ArgsManager {

    private  CliBuilder cli
    private  OptionAccessor options

    private  String inputPath
    private  String outputPath

    ArgsManager() {
        this.cli = new CliBuilder(usage: "miningframework [input] [output dir] [options]",
                header: "the Mining Framework take an input csv file and a name for the output dir \n Options: ")

        defParameters()
    }

    private defParameters() {
        this.cli.h(longOpt: 'help', 'Show help for executing commands.')
        this.cli.s(longOpt: 'since', args:1,
                argName:'date', 'Use commits more recent than a specific date (format DD/MM/YYY.')
        this.cli.u(longOpt: 'until', args:1,
                argName:'date', 'Use commits older than a specific date (format DD/MM/YYYY).')
    }


    private void parse(args) {
        this.options = this.cli.parse(args)

        if (!validArgs() || this.options.h) {
            throw new Exception('Invalid input')
        }

        this.inputPath = this.options.arguments()[0]
        this.outputPath = this.options.arguments()[1]

    }

    boolean validArgs() {
        if (this.options.arguments().size() >= 2) {
            String inputFile = this.options.arguments()[0]
            String outputFile = this.options.arguments()[1]
            return inputFile.endsWith('.csv') && outputFile.length() > 0
        }
        return false
    }

    void usageDescription() {
        this.cli.usage()
    }

    String getInputPath() {
        return inputPath
    }

    String getOutputPath() {
        return outputPath
    }


}
