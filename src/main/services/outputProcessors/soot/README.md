## Updating the soot-analysis version

1. Generate the conflict-static-analysis executable following the instructions on its README
2. Move the generated file at `target/soot-analysis-<VERSION>-jar-with-dependencies.jar` to the
   directory `dependencies/` at miningframework
3. Update the version passed to SootAnalysisWrapper at
   the `services.outputProcessors.soot.RunSootAnalysisOutputProcessor` so that it can resolve the
   file `dependencies/soot-analysis-<VERSION>-jar-with-dependencies.jar`

## Analyze multiple scenarios

1. Generate (by running the mining framework) or add a file to the `output/data` folder. with the dataset information,
   that is, information about the merge scenarios to be analyzed, following the template of the example
   file [results-with-build-information.csv](../../../../../results-with-build-information.csv). This file contains the
   following columns:
    1. **project**: name of project
    2. **merge commit**: hash of merge commit
    3. **className**: name of the class with a potential conflict, that is, the class to be analyzed
    4. **method**: name of the method with a potential conflict, that is, the method to be analyzed
    5. **left modifications**: array with lines of code modified by left
    6. **has_build**: boolean variable that defines if there is a build for this scenario. if false, the scenario will
       be ignored
    7. **left deletions**: array with lines of code deleted by left
    8. **right modifications**: array with lines of code modified by right
    9. **right deletions**: array with lines of code deleted by right
    10. **realistic case path**: relative path for folder with realistic scenario information, that is, source code and
        JAR files for slightly adapted version of the real merge scenario

2. Make sure you have all the scenarios contained in the results-with-build-information.csv inside the `output/files`
   directory. (You can use a [mergedataset](https://github.com/spgroup/mergedataset) clone or link)
3. Configure the analyzes you want to run in the `detectionAlgorithms` array in
   the [RunSootAnalysisOutputProcessor](./RunSootAnalysisOutputProcessor.groovy) class
4. At the miningframework root, run the command `./gradlew run -DmainClass="services.outputProcessors.soot.Main"
   
> The CLI has the following help page:
```
usage: ./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="[options]"
Options:
 -a,--allanalysis                        Excute all analysis
 -cd,--cd                                Run cd
 -cde,--cde                              Run cd-e
 -cf,--dfp-confluence-intraprocedural    Run
                                         dfp-confluence-intraprocedural
 -df,--svfa-intraprocedural              Run svfa-intraprocedural
 -dfp,--dfp-intra                        Run dfp-intra
 -h,--help                               Show help for executing commands
 -icf,--dfp-confluence-interprocedural   Run
                                         dfp-confluence-interprocedural
 -idf,--svfa-interprocedural             Run svfa-interprocedural
 -idfp,--dfp-inter                       Run dfp-inter
 -ioa, --overriding-interprocedural      Run interprocedural overriding analysis
 -oa, --overriding-intraprocedural       Run intraprocedural overriding analysis
 -ioaWithoutPA, --ioa-without-pa         Run interprocedural overriding analysis without Pointer Analysis 
 -oaWithoutPA, --oa-without-pa           Run intraprocedural overriding analysis without Pointer Analysis 
 -pd,--pessimistic-dataflow              Run pessimistic-dataflow
 -pdg,--pdg                              Run pdg
 -pdge,--pdge                            Run pdg-e
 -r,--reachability                       Run reachability
 -report                                 Run report results for experiment using -icf -ioa -idfp -pdg
 -t,--timeout <timeout>                  Run -t time: time limit for each analysis (default: 240)
  -depthLimit                             Sets the depth limit on accessing methods when performing Overriding Assignment Interprocedural, Direct Flow Interprocedural and Confluence Interprocedural analyses. Default = 5
 -printDepthSVFA                         Print depth in SVFA analysis
 -cg,--callgraph <algorithm>             Select call graph algorithm [CHA, RTA, VTA, SPARK]
 -o,--output <output>                    Output folder with the dataset (default: output)
 -s,--strategies <strategies>            Run once per entrypoint strategy, from the JSON: comma separated MOP, MM, MMM, LCA, or all (default: no entrypoints, from the CSV)
 -i,--input <json>                       Modified lines JSON read with --strategies (default: [output]/data/results-with-build-information.json)
 -mr,--method-resolver <resolver>        Finds the methods of the modified lines, with --strategies: bytecode, source (default: bytecode)
 -prt,--partial-results-on-timeout       When a soot analysis times out, capture and record the partial results
```

For example: 
```
./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="-icf -ioa -idfp -pdg"
```
5. The result will be written in `output/data/soot-results.csv`
   
6. The result for report analysis will be written in `output/data/results.pdf`

## Analyze with entrypoint strategies

With `--strategies`, each merge scenario is analysed once per strategy, with the entrypoints it chooses from the
methods modified by left and by right:

| Acronym | Entrypoints |
|---|---|
| MOP | main methods, or else the public ones |
| MM | methods modified by left or by right |
| MMM | methods modified by both |
| LCA | lowest common callers, in the call graph, of each pair of methods modified by left and by right |

Run the strategies (`all` or a list):
```
./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="-s MM,LCA -ioa -o output"
```

Outputs, in `output/data/`:
- `soot-results-[strategy].csv`: `project;merge commit;[analyses];Time`. A scenario without entrypoints is not analysed
  (`no-entrypoints`).
- `entrypoints-[strategy].csv`: `project;merge commit;left modified methods;right modified methods;entrypoints`.

The modified methods come from a method resolver (`-mr`, `--method-resolver`):
- `bytecode` (default): methods of the build with a statement in a modified line. Deleted lines and lines without
  bytecode (e.g. `if (true)`) give no method.
- `source`: methods of `source/merge` that contain a modified line (Eclipse JDT).

The entrypoints go to conflict-static-analysis as `-entrypoints @[scenario]/entrypoints.txt`, so its jar must support
it (`convertStringEntrypointsToList`).
