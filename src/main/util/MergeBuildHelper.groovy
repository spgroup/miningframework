package util

class MergeBuildHelper {

    static File getMergeBuildFolder(String outputPath, String projectName, String mergeCommitSHA) {
        return new File(outputPath, "files/${projectName}/${mergeCommitSHA}/original-without-dependencies/merge")
    }

    static boolean hasMergeBuild(File mergeBuildFolder) {
        return mergeBuildFolder.listFiles()?.any { it.name.endsWith('.jar') } ?: false
    }

    static boolean saveMergeBuild(List<File> jars, File mergeBuildFolder) {
        mergeBuildFolder.mkdirs()
        jars.findAll { it.name.endsWith('.jar') }.each { new File(mergeBuildFolder, it.name).bytes = it.bytes }
        return hasMergeBuild(mergeBuildFolder)
    }
}
