package services

@Grab('com.xlson.groovycsv:groovycsv:1.3')
import static com.xlson.groovycsv.CsvParser.parseCsv
import main.interfaces.DataCollector
import services.*
import main.project.*
import static main.app.MiningFramework.arguments
import main.util.FileManager
import main.util.FileTransformations

class SemanticConflictCollector implements DataCollector{
    private ExperimentalDataCollectorSemanticConflictDynamicImpl dataCollectorSemanticConflictDynamic;
    private BuildRequesterSemanticConflictDynamicImpl buildRequestSemanticConflictDynamic;

    public SemanticConflictCollector(){
        this.dataCollectorSemanticConflictDynamic = new ExperimentalDataCollectorSemanticConflictDynamicImpl();
        this.buildRequestSemanticConflictDynamic = new BuildRequesterSemanticConflictDynamicImpl();
    }

    public void collectData(Project project, MergeCommit mergeCommit){
        dataCollectorSemanticConflictDynamic.collectData(project, mergeCommit)
        def codeTransformationInfo = collectCodeTransformationInfoOfMergeCommit(mergeCommit.getSHA())
        for (code in codeTransformationInfo){
            for (file in FileManager.findLocalFileOfChangedClass(code[6], code[4], mergeCommit.getSHA())){
                FileTransformations.runTransformation(file, code[5].split("\\(")[0])                
                String branchComplement = ""
                try{
                    int index = file.lastIndexOf("/");
                    branchComplement = file.substring(index+1, file.size()).replace(".java","#") + code[5].split("\\(")[0];
                }catch (Exception e1){
                    println(e1)
                }
                buildRequestSemanticConflictDynamic.collectDataWithCodeTransformation(project, mergeCommit, file, branchComplement)
            }
        }
    }

    public ArrayList<String> collectCodeTransformationInfoOfMergeCommit (String mergeCommit){
        ArrayList<String> codeTransformationInfo = new ArrayList<String>();
        try{
            File outputFile = new File("${arguments.getOutputPath()}/data/results.csv")
            def csv_content = outputFile.getText('utf-8')
            def data_iterator = parseCsv(csv_content, separator: ';', readFirstLine: false)
            
            for (line in data_iterator) {
                if (line[1] == mergeCommit){
                    codeTransformationInfo.add([line[1],line[2],line[3],line[4],line[5],line[6],line[7]])
                }
            }
        }catch(Exception e1){
            print(e1)
        }

        return codeTransformationInfo;
    }

}