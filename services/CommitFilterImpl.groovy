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
            Set<ModifiedMethod> leftModifiedMethods = getModifiedMethods(file, mergeCommit.getLeftSHA(), mergeCommit.getAncestorSHA())
            Set<ModifiedMethod> rightModifiedMethods = getModifiedMethods(file, mergeCommit.getRightSHA(), mergeCommit.getAncestorSHA())
            Set<String> mutuallyModifiedMethods = getMethodsIntersection(leftModifiedMethods, rightModifiedMethods)

            if(mutuallyModifiedMethods.size() > 0)
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

    private Set<ModifiedMethod> getModifiedMethods(String filePath, String childSHA, String ancestorSHA) {
        Set<ModifiedMethod> modifiedMethods = new HashSet<ModifiedMethod>()

        File childFile = copyFile(filePath, childSHA) 
        File ancestorFile = copyFile(filePath, ancestorSHA)

        Process diffJ = new ProcessBuilder('java', '-jar', 'diffj.jar', '--brief', ancestorFile.getAbsolutePath(), childFile.getAbsolutePath())
            .directory(new File('dependencies'))
            .start()

        diffJ.getInputStream().eachLine {
            int inIndex = it.indexOf("in ")
            if(inIndex != -1) {
                String signature = it.substring(inIndex + 3)
                Set<Integer> modifiedLines = getModifiedLines(it.substring(0, it.indexOf(':')))

                ModifiedMethod modifiedMethod = new ModifiedMethod(signature, modifiedLines)
                modifiedMethods.add(modifiedMethod)
            }
        }
        
        childFile.delete()
        ancestorFile.delete()

        return modifiedMethods
    }

    private Set<Integer> getModifiedLines(String lineChanges) {
        for (int i = 0; i < lineChanges.size(); i++) {
            if(lineChanges[i] == 'c' || lineChanges[i] == 'd' || lineChanges[i] == 'a') {
                String ancestorLines = lineChanges.substring(0, i)
                String childLines = lineChanges.substring(i + 1)
                Set<Integer> modifiedLines = parseLines(ancestorLines)
                modifiedLines.addAll(parseLines(childLines))
                return modifiedLines
            }
        }
    }

    private Set<Integer> parseLines(String lines) {
        Set<Integer> modifiedLines = new HashSet<Integer>()
        
        int commaIndex = lines.indexOf(',')
        if (commaIndex == -1) 
            modifiedLines.add(Integer.parseInt(lines))
        else {
            int start = Integer.parseInt(lines.substring(0, commaIndex))
            int end = Integer.parseInt(lines.substring(commaIndex + 1))
            for (int i = start; i <= end; i++)
                modifiedLines.add(i)
        }

        return modifiedLines
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

    private Set<String> getMethodsIntersection(Set<ModifiedMethod> leftMethods, Set<ModifiedMethod> rightMethods) {
        Set<String> intersection = new HashSet<String>()
        for(leftMethod in leftMethods) {
            for(rightMethod in rightMethods) 
                if(leftMethod.equals(rightMethod))
                    intersection.add(leftMethod.getSignature())
        }
        return intersection
    }

}