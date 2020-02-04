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
        buildRequestSemanticConflictDynamic.collectData(project, mergeCommit)
    }

}