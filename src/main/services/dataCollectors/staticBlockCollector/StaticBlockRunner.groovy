package services.dataCollectors.staticBlockCollector

import util.FileManager
import util.Handlers;
import util.ProcessRunner;
import java.nio.file.Path;
import java.nio.file.Paths

import static app.MiningFramework.arguments
import static app.MiningFramework.arguments

public class StaticBlockRunner {
    static final Path S3M_PATH = Paths.get("dependencies/s3m.jar")
    static final String newHandlerOutput = "newHandlerOutput.java";
    static final String oldHandlerOutput = "oldHandlerOutput.java";

	/**
     * @param mergeScenarios
     */
    static void collectStaticBlockResults(List<Path> mergeScenarios,  List<Handlers> handlers) {
        mergeScenarios.parallelStream()
                .forEach(mergeScenario -> runStatickBlockForMergeScenario(mergeScenario, handlers))
    }
    static void collectStaticBlockResults(String mergeScenario ) {
        Path fileMergeScenario = Paths.get(mergeScenario);
        List<Handlers> handlers = new ArrayList<Handlers>();
        handlers.add(Handlers.InitializationBlocks);
        runStatickBlockForMergeScenario(fileMergeScenario, handlers)
    }
    private static void runStatickBlockForMergeScenario(Path mergeScenario,  List<Handlers> handlers) {
        Path leftFile = getInvolvedFile(mergeScenario, "left")
        Path baseFile = getInvolvedFile(mergeScenario, "base")
        Path rightFile = getInvolvedFile(mergeScenario, "right")
        
		Path newHandlersOutputFile = getInvolvedFile(mergeScenario,newHandlerOutput);
        Path oldHandlersOutputFile = getInvolvedFile(mergeScenario,oldHandlerOutput);
        runStaticBlockProcess(leftFile, baseFile, rightFile,  oldHandlersOutputFile.toString(),  '--handle-initialization-blocks','true','--handle-initialization-blocks-multiple-blocks','false');
        runStaticBlockProcess(leftFile, baseFile, rightFile,newHandlersOutputFile.toString(), '--handle-initialization-blocks', 'false','--handle-initialization-blocks-multiple-blocks','true' );

        forwentMergeScenariosWithBehaviorDifferent(newHandlersOutputFile, oldHandlersOutputFile)

    }
    private static void runStaticBlockProcess(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handler, String additionalParameters,String handlerNew, String additionalParametersNew) {
        Process S3M = ProcessRunner.startProcess(buildS3MProcessStaticBlock(leftFile, baseFile, rightFile, outputFileName, handler, additionalParameters, handlerNew , additionalParametersNew))

        S3M.getInputStream().eachLine {
            println it
        }
        S3M.waitFor()
    }
      private static ProcessBuilder buildS3MProcessStaticBlock(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handlerName, String additionalParameters, String handlerNameNew, String additionalParametersNew) {
        ProcessBuilder S3M = ProcessRunner.buildProcess(getParentAsString(S3M_PATH))
        List<String> parameters = buildS3MParametersBasic(leftFile, baseFile, rightFile, outputFileName, handlerName, additionalParameters , handlerNameNew, additionalParametersNew)
        S3M.command().addAll(parameters)
        return  S3M
    }
	    private static List<String> buildS3MParametersBasic(Path leftFile, Path baseFile, Path rightFile, String outputFileName, String handlerName, String additionalParameters , String handlerNameNew, String additionalParametersNew) {
        List<String> parameters = ['java', '-jar', getNameAsString(S3M_PATH), leftFile.toString(), baseFile.toString(), rightFile.toString(), '-o', outputFileName, handlerName,additionalParameters , handlerNameNew,additionalParametersNew]
        return parameters
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

    private static List<String> runTextualDiff(Path newHandlersFile, Path oldHandlersFile) {
        Process textDiff = ProcessRunner.runProcess(".", "diff" ,newHandlersFile.toString(), oldHandlersFile.toString())
        BufferedReader reader = new BufferedReader(new InputStreamReader(textDiff.getInputStream()))
        def output = reader.readLines()
        reader.close()
        return output
    }
    private static forwentMergeScenariosWithBehaviorDifferent(Path newHandlersFile, Path oldHandlersFile) {
        List<String> resultDiff =  runTextualDiff(newHandlersFile, oldHandlersFile)
        if(resultDiff !=null && resultDiff.size() > 0){
            obtainResultsForProject(newHandlersFile, oldHandlersFile)
        }else{
            obtainResultsForDiscartFile(newHandlersFile, oldHandlersFile)
            //FileManager.delete(new File(leftFile.toString()))
            //FileManager.delete(new File(baseFile.toString()))
           // FileManager.delete(new File(rightFile.toString()))
            FileManager.delete(new File(oldHandlersFile.toString()))
            FileManager.delete(new File(oldHandlersFile.toString()))

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
}