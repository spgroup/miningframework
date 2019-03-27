class Project {
    
    private String name
    private String path
    private boolean remote

    public Project(String name, String path) {
        this.name = name
        this.path = path
        this.remote = path.startsWith('https://github.com/')
    }

    public ArrayList<MergeCommit> getMergeCommits(String sinceDate, String untilDate) {
        ArrayList<MergeCommit> mergeCommits = new ArrayList<MergeCommit>()

        Process gitLog = constructAndRunGitLog(sinceDate, untilDate)
        gitLog.getInputStream().eachLine {

            // Each line contains the hash of the commit followed by the hashes of the parents.
            String[] informations = it.split(' ')
            String SHA = getSHA(informations)
            String[] parentsSHA = getParentsSHA(informations)
            String ancestorSHA = getCommonAncestor(SHA, parentsSHA)

            MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
            mergeCommits.add(mergeCommit)

        }
        
        if(mergeCommits.isEmpty())
            println "No merge commits."
        return mergeCommits
    }

    private String getSHA(String[] informations) {
        return informations[0]
    } 

    private String[] getParentsSHA(String[] informations) {
        return Arrays.copyOfRange(informations, 1, informations.length)
    }

    private String getCommonAncestor(mergeCommitSHA, parentsSHA) {
        Process gitMergeBaseBuilder = constructAndRunGitMergeBase(mergeCommitSHA, parentsSHA)
        gitMergeBase.getInputStream().eachLine {
            return it
        }
    }

    private Process constructAndRunGitMergeBase(String mergeCommitSHA, String parentsSHA) {
        ProcessRunner.buildProcess(path, 'git', 'merge-base')
        if (parentsSHA.length > 2)
            ProcessRunner.addCommand(gitMergeBaseBuilder, '--octopus')
        for (parent in parentsSHA)
            ProcessRunner.addCommand(gitMergeBaseBuilder, parent)
        return ProcessRunner.startProcess(gitMergeBaseBuilder)
    }

    private Process constructAndRunGitLog(String sinceDate, String untilDate) {
       ProcessRunner.buildProcess(path, 'git', '--no-pager', 'log', '--merges', '--pretty=\"%H %P\"')
        if(!sinceDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--since=\"${sinceDate}\"")
        if(!untilDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--until=\"${untilDate}\"")
        return ProcessRunner.startProcess(gitLogBuilder)
    }

    public String getName() {
        return name
    }

    public void setName(String name) {
        this.name = name
    }

    public String getPath() {
        return path
    }   

    public void setPath(String path) {
        this.path = path
    }

    public boolean isRemote() {
        return remote
    }

    public void setRemote(boolean remote) {
        this.remote = remote
    }
}