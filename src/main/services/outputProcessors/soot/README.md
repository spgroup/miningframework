## Updating the soot-analysis version

1. Generate the conflict-static-analysis executable following the instructions on its README
2. Move the generated file at `target/soot-analysis-<VERSION>-jar-with-dependencies.jar` to the directory `dependencies/` at miningframework
3. Update the version passed to SootAnalysisWrapper at the `services.outputProcessors.soot.RunSootAnalysisOutputProcessor` so that it can resolve the file `dependencies/soot-analysis-<VERSION>-jar-with-dependencies.jar`
