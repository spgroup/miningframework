package util

class MergeBuildHelper {

    static File getMergeBuild(String outputPath, String projectName, String mergeCommitSHA) {
        return new File(outputPath, "files/${projectName}/${mergeCommitSHA}/original-without-dependencies/merge/build.jar")
    }

    static boolean saveMergeBuild(List<File> generatedJars, File mergeBuild) {
        File projectJar = generatedJars.sort(false) { it.name }.find { File jar ->
            jar.name.endsWith('.jar') && !(jar.name =~ /-(sources|javadoc|tests|plain)\.jar$|^(gradle|maven)-wrapper\.jar$/)
        }
        if (projectJar == null) {
            return false
        }
        mergeBuild.parentFile.mkdirs()
        mergeBuild.bytes = projectJar.bytes
        return true
    }
}
