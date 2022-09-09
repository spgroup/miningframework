## Updating the soot-analysis version

1. Generate the conflict-static-analysis executable following the instructions on its README
2. Move the generated file at `target/soot-analysis-<VERSION>-jar-with-dependencies.jar` to the directory `dependencies/` at miningframework
3. Update the version passed to SootAnalysisWrapper at the `services.outputProcessors.soot.RunSootAnalysisOutputProcessor` so that it can resolve the file `dependencies/soot-analysis-<VERSION>-jar-with-dependencies.jar`


## Run multiple scenarios

1. Add an example of the file the [results-with-build-information.csv](../../../../../results-with-build-information.csv) into the `output/data` folder. This file contains the following fields:
      1. **project**: name of project
      2. **merge commit**: hash of merge commit 
      3. **className**: name of the class where the conflict occurred
      4. **method**: name of the method where the conflict occurred
      5. **left modifications**: array with rows modified by left
      6. **has_build**: boolean variable that defines if there is a build for this scenario. if false, the scenario will be ignored
      7. **left deletions**: array with lines deleted by left
      8. **right modifications**: array with rows modified by right
      9. **right deletions**: array with lines deleted by right
      10. **realistic case path**: relative path for realistic scenery
2. Make sure you have all the scenarios contained in the results-with-build-information.csv inside the `output/files` directory. (You can use a [mergedataset](https://github.com/spgroup/mergedataset) clone or link)
3. Configure the analyzes you want to run in the `detectionAlgorithms` array in the [RunSootAnalysisOutputProcessor](./RunSootAnalysisOutputProcessor.groovy) class
4. At the miningframework root, run the command `./gradlew run -DmainClass="services.outputProcessors.soot.Main"`
5. The result will be written in `output/data/soot-results.csv`