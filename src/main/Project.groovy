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
        String line
        BufferedReader reader = new BufferedReader(new InputStreamReader(gitLog.getInputStream()))
        while((line = reader.readLine()) != null) {

            if (line.startsWith('commit')) {
                String SHA = getSHA(line)
                String[] parentsSHA = getParentsSHA(reader.readLine())
                String ancestorSHA = getCommonAncestor(SHA, parentsSHA)
                
                MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                mergeCommits.add(mergeCommit)
            
                // Forwarding two lines from the output because they are not necessary.
                reader.readLine() // Author
                reader.readLine() // Date
            }
        }
        
        if(mergeCommits.isEmpty())
            println "No merge commits."
        return mergeCommits
    }

    private String getSHA(String line) {
        return line.split(' ')[1]
    } 

    private String[] getParentsSHA(String line) {
        String[] parents = reader.readLine().split(' ')
        return Arrays.copyOfRange(parents, 1, parents.length)
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
       ProcessRunner.buildProcess(path, 'git', '--no-pager', 'log', '--merges')
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