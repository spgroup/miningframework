class CommitFilterImpl extends CommitFilter {

    public boolean applyFilter() {
        return containsMutuallyModifiedMethods()
    }

    private boolean containsMutuallyModifiedMethods() {

        Set<String> leftModifiedFiles = getModifiedFiles(mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
        Set<String> rightModifiedFiles = getModifiedFiles(mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
        Set<String> mutuallyModifiedFiles = new HashSet<String>(leftModifiedFiles)
        mutuallyModifiedFiles.retainAll(rightModifiedFiles)

        for(file in mutuallyModifiedFiles) {
            Set<String> leftModifiedMethods = getModifiedMethods(file, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
            Set<String> rightModifiedMethods = getModifiedMethods(file, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
            leftModifiedMethods.retainAll(rightModifiedMethods) // Intersection.

            if(leftModifiedMethods.size() > 0)
                return true
        }

        return false
    }

    private Set<String> getModifiedFiles(String childSHA, String ancestorSHA) {
        Set<String> modifiedFiles = new HashSet<String>()
        Process gitDiff = new ProcessBuilder('git', 'diff', '--name-only', childSHA, ancestorSHA)
            .directory(new File(project.getPath()))
            .start()
        
        gitDiff.getInputStream().eachLine {
            if(it.endsWith('.java'))
                modifiedFiles.add(it)
        }

        return modifiedFiles
    }

    private Set<String> getModifiedMethods(String filePath, String childSHA, String ancestorSHA) {
        Set<String> modifiedMethods = new HashSet<ModifiedMethod>()

        File childFile = copyFile(filePath, childSHA) 
        File ancestorFile = copyFile(filePath, ancestorSHA)

        Process diffJ = new ProcessBuilder('java', '-jar', 'diffj.jar', '--brief', ancestorFile.getAbsolutePath(), childFile.getAbsolutePath())
            .directory(new File('dependencies'))
            .start()

        diffJ.getInputStream().eachLine {
            int inIndex = it.indexOf("in ")
            if(inIndex != -1) {
                String signature = it.substring(inIndex + 3)
                modifiedMethods.add(signature)
            }
        }
        
        childFile.delete()
        ancestorFile.delete()

        return modifiedMethods
    }

    private File copyFile(String path, String SHA) {
        Process gitCatFile = new ProcessBuilder('git', 'cat-file', '-p', "${SHA}:${path}")
            .directory(new File(project.getPath()))
            .start()    
        
        File target = new File("${SHA}.java")
        gitCatFile.getInputStream().eachLine {
            target << "${it}\n"
        }
       
        return target
    }

}