package services.dataCollectors.S3MMergesCollector

import project.MergeCommit
import project.Project
import util.FileManager
import util.Handlers
import util.ProcessRunner
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import static app.MiningFramework.arguments

class S3MRunner {

    static final Path S3M_PATH = Paths.get("dependencies/s3m.jar")
    static final String newHandlerOutput = "newHandlerOutput.java";
    static final String oldHandlerOutput = "oldHandlerOutput.java";
    /**
     * Runs S3M for each merge scenario and for each handler. Store the results at the same directory
     * the merge scenario is located, in a directory for each handler.
     *
     * To extend the analysis for more handlers, check {@link #runHandlerVariants(Path, List < Handlers >)}
     * @param mergeScenarios
     * @param handlers
     */
    static void collectS3MResults(List<Path> mergeScenarios, List<Handlers> handlers) {
        mergeScenarios.parallelStream()
                .forEach(mergeScenario -> runHandlerVariants(mergeScenario, handlers))
    }
    static void collectS3MResult(String mergeScenario ) {
        Path fileMergeScenario = Paths.get(mergeScenario);
        List<Handlers> handlers = new ArrayList<Handlers>();
        handlers.add(Handlers.InitializationBlocks);
        runHandlerVariants(fileMergeScenario, handlers)
    }

    private static void runHandlerVariants(Path mergeScenario, List<Handlers> handlers) {
        Path leftFile = getInvolvedFile(mergeScenario, 'left')
        Path baseFile = getInvolvedFile(mergeScenario, 'base')
        Path rightFile = getInvolvedFile(mergeScenario, 'right')

        // To extend the analysis for other handlers, clone and modify the following conditional.
        if (handlers.contains(Handlers.Renaming)) {
            runS3M(leftFile, baseFile, rightFile, 'CT.java', Handlers.Renaming, '-hmcrd', 'true')
            runS3M(leftFile, baseFile, rightFile, 'SF.java', Handlers.Renaming, '-r', 'SAFE')
            runS3M(leftFile, baseFile, rightFile, 'MM.java', Handlers.Renaming, '-r', 'MERGE')
            runS3M(leftFile, baseFile, rightFile, 'KB.java', Handlers.Renaming, '-r', 'BOTH')
        }
        if (handlers.contains(Handlers.InitializationBlocks)){
            Path newHandlersOutputFile = getInvolvedFile(mergeScenario,newHandlerOutput);
            Path oldHandlersOutputFile = getInvolvedFile(mergeScenario,oldHandlerOutput);
            run3MInicializedDeclaration(leftFile, baseFile, rightFile,  oldHandlersOutputFile.toString(),  '--handle-initialization-blocks','true','--handle-initialization-blocks-multiple-blocks','false');
            run3MInicializedDeclaration(leftFile, baseFile, rightFile,newHandlersOutputFile.toString(), '--handle-initialization-blocks', 'false','--handle-initialization-blocks-multiple-blocks','true' );

            List<String> resultDiff =  runTextualDiff(newHandlersOutputFile, oldHandlersOutputFile)
            if(resultDiff !=null && resultDiff.size() > 0){
                obtainResultsForProject(newHandlersOutputFile, oldHandlersOutputFile)
            }else{
                obtainResultsForDiscartFile(newHandlersOutputFile, oldHandlersOutputFile)
                FileManager.delete(new File(leftFile.toString()))
                FileManager.delete(new File(baseFile.toString()))
                FileManager.delete(new File(rightFile.toString()))
                FileManager.delete(new File(newHandlersOutputFile.toString()))
                FileManager.delete(new File(oldHandlersOutputFile.toString()))
                FileManager.delete(new File(mergeScenario.toString()))
            }
        }
    }
    private static void obtainResultsForProject(Path newHandlersOutputFile, Path oldHandlersOutputFile) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/comparation_handlersFiles.csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'New Handlers; old Handlers; status\n'
        }
        obtainResultsForProjects  << "${newHandlersOutputFile.toString()};${oldHandlersOutputFile.toString()};1;\n"
    }
    private static void obtainResultsForDiscartFile(Path newHandlersOutputFile, Path oldHandlersOutputFile) {
        File dataFolder = new File(arguments.getOutputPath() + "/data/");
        File obtainResultsForProjects = new File(dataFolder.getAbsolutePath() + "/discart_handlersFiles.csv")
        if (!obtainResultsForProjects.exists()) {
            obtainResultsForProjects << 'New Handlers; old Handlers; status\n'
        }
        obtainResultsForProjects  << "${newHandlersOutputFile.toString()};${oldHandlersOutputFile.toString()};1;\n"
    }
    private static void runS3M(Path leftFile, Path baseFile, Path rightFile, String outputFileName, Handlers handler, String additionalParameters) {
        Process S3M = ProcessRunner.startProcess(buildS3MProcess(leftFile, baseFile, rightFile, outputFileName, handler, additionalParameters))
        S3M.getInputStream().eachLine {
            //println it
        }
        S3M.waitFor()

        renameUnstructuredMergeFile(baseFile.getParent(), handler.name(), outputFileName)
    }

    private static void run3MInicializedDeclaration(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handler, String additionalParameters,String handlerNew, String additionalParametersNew) {
        Process S3M = ProcessRunner.startProcess(buildS3MProcessInizalizedDeclaration(leftFile, baseFile, rightFile, outputFileName, handler, additionalParameters, handlerNew , additionalParametersNew))

        S3M.getInputStream().eachLine {
            println it
        }
        S3M.waitFor()
    }
    private static void renameUnstructuredMergeFile(Path mergeScenario, String handlerName, String outputFileName) {
        Path currentUnstructuredMergeFile = mergeScenario.resolve(handlerName).resolve("${outputFileName}.merge")
        Path renamedUnstructuredMergeFile = mergeScenario.resolve("textual.java")
        Files.move(currentUnstructuredMergeFile, renamedUnstructuredMergeFile, StandardCopyOption.REPLACE_EXISTING)
    }

    private static ProcessBuilder buildS3MProcess(Path leftFile, Path baseFile, Path rightFile, String outputFileName, Handlers handler, String... additionalParameters) {
        ProcessBuilder S3M = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        List<String> parameters = buildS3MParameters(leftFile, baseFile, rightFile, outputFileName, handler.name(), additionalParameters)
        S3M.command().addAll(parameters)
        return S3M
    }
    private static ProcessBuilder buildS3MProcessInizalizedDeclaration(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handlerName, String additionalParameters, String handlerNameNew, String additionalParametersNew) {
        ProcessBuilder S3M = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        List<String> parameters = buildS3MParametersBasic(leftFile, baseFile, rightFile, outputFileName, handlerName, additionalParameters , handlerNameNew, additionalParametersNew)
        S3M.command().addAll(parameters)
        return  S3M
    }

    private static List<String> buildS3MParametersBasic(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handlerName, String additionalParameters , String handlerNameNew, String additionalParametersNew) {
        List<String> parameters = ['java', '-jar', getNameAsString(S3M_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), '-o', outputFileName, handlerName,additionalParameters , handlerNameNew,additionalParametersNew]
        return parameters
    }

    private static List<String> buildS3MParameters(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handlerName, String... additionalParameters) {
        List<String> parameters = ['java', '-jar', getNameAsString(S3M_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), '-o', getOutputPath(baseFile.getParent(), handlerName, outputFileName).toString(), '-c', 'false', '-l', 'false']
        parameters.addAll(additionalParameters.toList())
        return parameters
    }
    private List<String> runDiffJ(File newHandlersFile, File oldHandlersFile) {
        Process diffJ = ProcessRunner.runProcess('dependencies', 'java', '-jar', 'diffj.jar', "--brief", newHandlersFile.getAbsolutePath(), oldHandlersFile.getAbsolutePath())
        BufferedReader reader = new BufferedReader(new InputStreamReader(diffJ.getInputStream()))


        def line = null;
        while ((line = reader.readLine()) != null) {
            println "${line}";
        }
        def output = reader.readLines()
        reader.close();

        return output
    }
    private static List<String> runTextualDiff(Path newHandlersFile, Path oldHandlersFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,newHandlersFile.toString(), oldHandlersFile.toString())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }
    private static Path getOutputPath(Path mergeScenario, String handlerName, String fileName) {
        return mergeScenario.resolve(handlerName).resolve(fileName)
    }

    private static String getParentAsString(Path path) {
        return path.getParent().toString()
    }

    private static String getNameAsString(Path path) {
        return path.getFileName().toString()
    }

    private static Path getInvolvedFile(Path mergeScenario, String fileName) {
        return mergeScenario.resolve("${fileName}.java").toAbsolutePath()
    }
}
