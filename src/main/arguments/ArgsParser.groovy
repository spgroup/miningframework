package arguments

import exception.InvalidArgsException
import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor
import org.apache.logging.log4j.Level

import java.text.ParseException
import java.text.SimpleDateFormat

class ArgsParser {

    private CliBuilder cli
    private OptionAccessor options

    ArgsParser() {
        this.cli = new CliBuilder(usage: "miningframework [options] [input] [output]",
                header: "the Mining Framework take an input csv file and a name for the output dir (default: output) \n Options: ")

        defParameters()
    }

    private defParameters() {
        this.cli.h(longOpt: 'help', 'Show help for executing commands')
        this.cli.r(longOpt: 'random-seed', args: 1,
                argName: 'seed', 'Random seed used for shuffling merge commits array')
        this.cli.s(longOpt: 'since', args: 1,
                argName: 'date', 'Use commits more recent than a specific date (format YYYY-MM-DD)')
        this.cli.u(longOpt: 'until', args: 1,
                argName: 'date', 'Use commits older than a specific date(format YYYY-MM-DD)')
        this.cli.m(longOpt: 'max-commits-per-project', args: 1,
                argName: 'commits', 'Maximum number of commits to use for each project. Commits will be selected randomly, according to provided random seed')
        this.cli.i(longOpt: 'injector', args: 1,
                argName: 'class', 'Specify the class of the dependency injector (Must provide full name, default injectors.StaticAnalysisConflictsDetectionModule)')
        this.cli.p(longOpt: 'push', args: 1, argName: 'link', 'Specify a git repository to upload the output in the end of the analysis (format https://github.com/<owner>/<name>')
        this.cli.a(longOpt: 'access-key', args: 1, argName: 'access key', 'Specify the access key of the git account for when the analysis needs user access to GitHub')
        this.cli.t(longOpt: 'threads', args: 1, argName: 'threads', "Number of cores used in analysis (default: 1)")
        this.cli.k(longOpt: 'keep-projects', argName: 'keep projects', 'Specify that cloned projects must be kept after the analysis (those are kept in clonedRepositories/ )')
        this.cli.e(longOpt: 'extension', args: 1, argName: 'file extenson', 'Specify the file extension that should be used in the analysis (e.g. .rb, .ts, .java, .cpp. Default: .java)')
        this.cli.l(longOpt: 'language-separators', args: 1, argName: 'language syntactic separators', 'Specify the language separators that should be used in the analysis. Required for (and only considered when) running studies with the CSDiff tool. Default: \"{ } ( ) ; ,\"')
        this.cli.log(longOpt: 'log-level', args: 1, argName: 'log level', 'Specify the minimum log level: (OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL). Default: \"INFO\"')
    }

    Arguments parse(args) {
        this.options = this.cli.parse(args)
        Arguments resultArgs = new Arguments()

        if (this.getArgumentQuantity() == 0 || this.options.h) {
            resultArgs.setHelp()
        } else {
            parseInputs(resultArgs)
            parseOptions(resultArgs)
        }

        return resultArgs
    }

    void printHelp() {
        this.cli.usage()
    }

    private void parseInputs(Arguments args) {
        if (this.getArgumentQuantity() > 2) {
            String message = hasOptionValid() ? 'Too many arguments passed' : 'Invalid argument passed'
            throw new InvalidArgsException(message)
        }

        String inputFile = this.options.arguments()[0]
        if (!inputFile.endsWith('.csv'))
            throw new InvalidArgsException('The input must be a csv file')

        if (!new File(inputFile).exists())
            throw new InvalidArgsException("Could not find input file: ${inputFile}")

        args.setInputPath(inputFile)

        if (this.getArgumentQuantity() > 1) {
            String outputPath = this.options.arguments()[1]
            String parsedOutputPath = outputPath.endsWith("/") ? outputPath.substring(0, outputPath.lastIndexOf("/")) : outputPath;
            args.setOutputPath(parsedOutputPath)
        }
    }

    private void parseOptions(Arguments args) {
        if (this.options.r) {
            args.setRandomSeed(this.options.r.toInteger())
        }

        if (this.options.since) {
            if (!validDate(this.options.since))
                throw new InvalidArgsException('Invalid since date. You must specify it with the format YYYY-MM-DD')

            args.setSinceDate(this.options.since)
        }

        if (this.options.until) {
            if (!validDate(this.options.until))
                throw new InvalidArgsException('Invalid until date. You must specify it with the format YYYY-MM-DD')

            args.setUntilDate(this.options.until)
        }

        if (this.options.m) {
            args.setMaxCommitsPerProject(this.options.m.toInteger())
        }

        if (this.options.injector) {
            try {
                args.setInjector(Class.forName(this.options.injector));
            } catch (Exception e) {
                throw new InvalidArgsException('Invalid injector class. be sure it is in your classpath')
            }
        }

        if (this.options.push) {
            if (!repositoryExists(this.options.push))
                throw new InvalidArgsException('Inexistent remote git repository.')

            args.setResultsRemoteRepositoryURL(this.options.push)
        }

        if (this.options.a) {
            args.setAccessKey(this.options.a)
        }

        if (this.options.t) {
            args.setNumOfThreads(this.options.t.toInteger())
        }

        if (this.options.k) {
            args.setKeepProjects()
        }

        if(this.options.extension) {
            args.setFileExtension(this.options.extension)
        }

        if(this.options.l) {
            args.setLanguageSyntacticSeparators(this.options.l)
        }

        if(this.options.log) {
            args.setLogLevel(Level.toLevel(this.options.log))
        }
    }

    private boolean repositoryExists(String repositoryURL) {
        try {
            final URL url = new URL(repositoryURL)
            HttpURLConnection huc = (HttpURLConnection) url.openConnection()
            huc.setRequestMethod("HEAD")
            return huc.getResponseCode() == 200
        } catch (MalformedURLException e) {
            throw new InvalidArgsException('Invalid url.')
        }
    }

    private boolean validDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd")
        try {
            format.setLenient(false)
            format.parse(value)
            return true;
        } catch (ParseException | NullPointerException e) {
            return false;
        }
    }

    private boolean hasOptionValid() {
        List<String> arguments = this.options.arguments()
        for (String arg : arguments) {
            if (arg.contains('-') || arg.contains('--')) {
                return false
            }
        }
        return true
    }

    private int getArgumentQuantity() {
        return this.options.arguments().size()
    }
}
