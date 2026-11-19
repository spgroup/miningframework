package services.outputProcessors.soot.arguments

import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor
import services.outputProcessors.soot.methodResolvers.MethodResolvers

class ArgsParser {

    private CliBuilder cli
    private OptionAccessor options

    ArgsParser() {
        this.cli = new CliBuilder(usage: "./gradlew run -DmainClass=\"services.outputProcessors.soot.Main --args=\"[options]\"",
                header: "Options: ")

        defParameters()
    }

    private defParameters() {
        this.cli.h(longOpt: 'help', 'Show help for executing commands')
        this.cli.a(longOpt: 'allanalysis', 'Excute all analysis')
        this.cli.t(longOpt: 'timeout', args: 1, argName: 'timeout', "timeout (default: 240)")
        this.cli.l(longOpt: 'depthLimit', args: 1, argName: 'depthLimit', "Maximum depth limit for analysis (default: 5)")
        this.cli.df(longOpt: 'svfa-intraprocedural',  "Run svfa-intraprocedural")
        this.cli.idf(longOpt: 'svfa-interprocedural',  "Run svfa-interprocedural")
        this.cli.cf(longOpt: 'dfp-confluence-intraprocedural',  "Run dfp-confluence-intraprocedural")
        this.cli.icf(longOpt: 'dfp-confluence-interprocedural',  "Run dfp-confluence-interprocedural")
        this.cli.oa(longOpt: 'overriding-intraprocedural',  "Run overriding-intraprocedural")
        this.cli.ioa(longOpt: 'overriding-interprocedural',  "Run overriding-interprocedural")
        this.cli.oawopa(longOpt: 'oa-without-pa',  "Run oa-without-pa")
        this.cli.ioawopa(longOpt: 'ioa-without-pa',  "Run ioa-without-pa")
        this.cli.dfp(longOpt: 'dfp-intra',  "Run dfp-intra")
        this.cli.idfp(longOpt: 'dfp-inter',  "Run dfp-inter")
        this.cli.cd(longOpt: 'cd',  "Run cd")
        this.cli.cde(longOpt: 'cde',  "Run cd-e")
        this.cli.pdg(longOpt: 'pdg',  "Run pdg")
        this.cli.pdge(longOpt: 'pdge',  "Run pdg-e")
        this.cli.pd(longOpt: 'pessimistic-dataflow',  "Run pessimistic-dataflow")
        this.cli.report(longOpt: 'report',  "Run report results for experiment using -icf -ioa -idfp -pdg")
        this.cli.r(longOpt: 'reachability',  "Run reachability")
        this.cli.cg(longOpt: 'callgraph', args: 1, argName: 'algorithm', "Call graph algorithm [CHA, RTA, VTA, SPARK]")
        this.cli.o(longOpt: 'output', args: 1, argName: 'output', "Output folder with the dataset (default: output)")
        this.cli.s(longOpt: 'strategies', args: 1, argName: 'strategies', "Run once per entrypoint strategy, from the JSON: comma separated MOP, MM, MMM, LCA, or all (default: no entrypoints, from the CSV)")
        this.cli.mr(longOpt: 'method-resolver', args: 1, argName: 'resolver', "Finds the methods of the modified lines, with --strategies: ${MethodResolvers.NAMES.join(', ')} (default: ${MethodResolvers.DEFAULT})")
        this.cli.i(longOpt: 'input', args: 1, argName: 'json', "Modified lines JSON read with --strategies (default: [output]/data/results-with-build-information.json)")
    }

    Arguments parse(args) {
        this.options = this.cli.parse(args)
        Arguments resultArgs = new Arguments()

        parseOptions(resultArgs)

        return resultArgs
    }

    void printHelp() {
        this.cli.usage()
    }


    private void parseOptions(Arguments args) {
        if (!this.options.a) {
            args.setAllanalysis(false)
        }
        if (this.options.h) {
            args.setIsHelp(true)
        }
        if (this.options.t) {
            args.setTimeout(this.options.t.toLong());
        }
        if (this.options.l) {
            args.setDepthLimit(this.options.l.toLong())
        }
        if (this.options.df) {
            args.setDfIntra(true)
        }
        if (this.options.idf) {
            args.setDfInter(true)
        }
        if (this.options.cf) {
            args.setCfIntra(true)
        }
        if (this.options.icf) {
            args.setCfInter(true)
        }
        if (this.options.oa) {
            args.setOaIntra(true)
        }
        if (this.options.ioa) {
            args.setOaInter(true)
        }
        if (this.options.oawopa) {
            args.setOaIntraWithoutPA(true)
        }
        if (this.options.ioawopa) {
            args.setOaInterWithoutPA(true)
        }
        if (this.options.dfp) {
            args.setDfpIntra(true)
        }
        if (this.options.idfp) {
            args.setDfpInter(true)
        }
        if (this.options.cd) {
            args.setCd(true)
        }
        if (this.options.cde) {
            args.setCde(true)
        }
        if (this.options.pdg) {
            args.setPdg(true)
        }
        if (this.options.pdge) {
            args.setPdge(true)
        }
        if (this.options.pd) {
            args.setPessimisticDataflow(true)
        }
        if (this.options.report) {
            args.setReport(true)
        }
        if (this.options.r) {
            args.setReachability(true)
        }
        if (this.options.cg) {
            String algorithm = options.cg?.toString()?.trim()?.toUpperCase()
            if (!algorithm || !["CHA", "RTA", "VTA", "SPARK"].contains(algorithm)) {
                throw new IllegalArgumentException("Invalid callgraph algorithm: ${options.cg}")
            }
            args.setCallgraph(algorithm)
        }
        if (this.options.i) {
            args.setInput(this.options.i)
        }
        if (this.options.s) {
            args.setStrategies(this.options.s.split(',')*.trim())
        }
        if (this.options.o) {
            args.setOutputPath(this.options.o)
        }
        if (this.options.mr) {
            if (!MethodResolvers.NAMES.contains(this.options.mr)) {
                throw new IllegalArgumentException("Invalid method resolver: ${options.mr}")
            }
            args.setMethodResolver(this.options.mr)
        }
    }
}
