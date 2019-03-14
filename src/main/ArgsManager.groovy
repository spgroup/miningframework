import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor
import java.text.SimpleDateFormat
import java.text.ParseException

class ArgsManager {

    private  CliBuilder cli
    private  OptionAccessor options

    private  String inputPath
    private  String outputPath
    private  String sinceDate
    private String untilDate
    ArgsManager() {
        this.cli = new CliBuilder(usage: "miningframework [options] [input] [output]",
                header: "the Mining Framework take an input csv file and a name for the output dir (default: output) \n Options: ")

        defParameters()
        // set default values
        this.sinceDate = ''
        this.untilDate = ''
        this.outputPath = 'output'
    }

    private defParameters() {
        this.cli.h(longOpt: 'help', 'Show help for executing commands.')
        this.cli.s(longOpt: 'since', args: 1,
                argName:'date', 'Use commits more recent than a specific date (format DD/MM/YYY.')
        this.cli.u(longOpt: 'until', args: 1,
                argName:'date', 'Use commits older than a specific date (format DD/MM/YYYY).')
    }


    private void parse(args) {
        this.options = this.cli.parse(args)
        
        if (!validArgs() || this.options.h) {
            throw new InvalidArgsException()
        }
        
        this.inputPath = this.options.arguments()[0]
        
        if (this.options.arguments().size() > 1) {
            String dir = this.options.arguments()[1]
            this.outputPath = dir.endsWith("/") ? dir.substring(0,dir.lastIndexOf("/")) : dir;
        }

        if (this.options.since)
            this.sinceDate = this.options.since 

        if (this.options.until)
            this.untilDate = this.options.until
    }

    boolean validArgs() {
        if (this.options.arguments().size() < 1 || this.options.arguments().size() > 2)
            return false
        
        String inputFile = this.options.arguments()[0]
        if (!inputFile.endsWith('.csv'))
            return false

        if (this.options.since && !validDate(this.options.since))
            return false

        if (this.options.until && !validDate(this.options.until))
            return false
    
        return true
    }

    private boolean validDate(String value) {
        try {
            new SimpleDateFormat("dd/mm/yyyy").parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
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

    String getSinceDate() {
        return sinceDate
    }

    String getUntilDate() {
        return untilDate
    }

}
