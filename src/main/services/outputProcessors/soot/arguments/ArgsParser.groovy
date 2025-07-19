package services.outputProcessors.soot.arguments

import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

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
        if (this.options.oaWithoutPA) {
            args.setOaIntraWithoutPA(true)
        }
        if (this.options.ioaWithoutPA) {
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
    }
}
