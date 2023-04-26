package services.dataCollectors.staticBlockCollector

import project.MergeCommit
import project.Project
import services.util.Utils

import java.nio.file.Path
import java.math.RoundingMode;
import java.text.DecimalFormat;

class LoggerStatistics {
  
   public static long timeInitial;
   public static long timeFinal;
   private static final String SPREADSHEET_NAME = 'logTime.csv'

    public static void logTimeInitial(){
        timeInitial  = System.currentTimeMillis();
	}
    public static String logTimeFinal() {
        timeFinal = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format((timeFinal - timeInitial) / 1000d)
    }

    static synchronized void updateSpreadsheet(Integer count, Project project, MergeCommit mergeCommit,Path fileName, String approach, String timeStamp){
        File spreadsheet = Utils.getOutputPath().resolve(project.getName() + "_" + approach + "_" + SPREADSHEET_NAME).toFile()
        if (!spreadsheet.exists()) {
            String headerLine = getSpreadsheetHeaderLogStatisticLine()
            appendLineToSpreadsheet(spreadsheet, headerLine)
        }

        String newLine = "${count};${project.getName()};${mergeCommit.getSHA()};${fileName.toAbsolutePath().toString()};${approach};${timeStamp}"
        appendLineToSpreadsheet(spreadsheet, newLine)

        createFileTimestampForHandler(project,approach,timeStamp)
    }
    private static synchronized void createFileTimestampForHandler( Project project, String approach, String timeStamp){
        File spreadsheet = Utils.getOutputPath().resolve(approach + "_" + SPREADSHEET_NAME).toFile()
        if (!spreadsheet.exists()) {
            String headerLine = 'project;' + 'timeStamp'
            appendLineToSpreadsheet(spreadsheet, headerLine)
        }

        String newLine = "${project.getName()};${timeStamp}"
        appendLineToSpreadsheet(spreadsheet, newLine)
    }
    private static String getSpreadsheetHeaderLogStatisticLine() {
        List<String> headers = [ '#','project', 'merge commit', 'file' ,'approach' ,'timeStamp']
    }
    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }
}