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
 -ioa,--overriding-interprocedural       Run overriding-interprocedural
 -oa,--overriding-intraprocedural        Run overriding-intraprocedural
 -pd,--pessimistic-dataflow              Run pessimistic-dataflow
 -pdg,--pdg                              Run pdg
 -pdge,--pdge                            Run pdg-e
 -r,--reachability                       Run reachability
 -report                                 Run report results for experiment using -icf -ioa -idfp -pdg
 -t,--timeout <timeout>                  Run -t time: time limit for each analysis (default: 240)
```

For example: 
```
./gradlew run -DmainClass="services.outputProcessors.soot.Main" --args="-icf -ioa -idfp -pdg"
```
5. The result will be written in `output/data/soot-results.csv`
   
6. The result for report analysis will be written in `output/data/results.pdf`