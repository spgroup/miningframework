package services.outputProcessors.soot.arguments

class Arguments {
    private boolean isHelp
    private boolean allanalysis
    private boolean dfIntra
    private boolean dfInter
    private boolean cfIntra
    private boolean cfInter
    private boolean oaIntra;
    private boolean oaInter;
    private boolean dfpIntra
    private boolean dfpInter
    private boolean cd
    private boolean cde
    private boolean pdg
    private boolean pdge
    private boolean pessimisticDataflow
    private boolean reachability
    private int timeout

    Arguments() { // set the default values for all parameters
        isHelp = false
        allanalysis = true
        dfIntra = false
        dfInter = false
        cfIntra = false
        cfInter = false
        oaIntra = false
        oaInter = false
        dfpIntra = false
        dfpInter = false
        cd = false
        cde = false
        pdg = false
        pdge = false
        pessimisticDataflow = false
        reachability = false
        timeout = 240
    }

    boolean isHelp() {
        return isHelp
    }

    void setIsHelp(boolean isHelp) {
        this.isHelp = isHelp
    }

    boolean getAllanalysis() {
        return allanalysis
    }

    void setAllanalysis(boolean allanalysis) {
        this.allanalysis = allanalysis
    }

    boolean getDfIntra() {
        return dfIntra
    }

    void setDfIntra(boolean dfIntra) {
        this.dfIntra = dfIntra
    }

    boolean getDfInter() {
        return dfInter
    }

    void setDfInter(boolean dfInter) {
        this.dfInter = dfInter
    }

    boolean getCfIntra() {
        return cfIntra
    }

    void setCfIntra(boolean cfIntra) {
        this.cfIntra = cfIntra
    }

    boolean getCfInter() {
        return cfInter
    }

    void setCfInter(boolean cfInter) {
        this.cfInter = cfInter
    }

    boolean getOaIntra() {
        return oaIntra
    }

    void setOaIntra(boolean oaIntra) {
        this.oaIntra = oaIntra
    }

    boolean getOaInter() {
        return oaInter
    }

    void setOaInter(boolean oaInter) {
        this.oaInter = oaInter
    }

    boolean getDfpIntra() {
        return dfpIntra
    }

    void setDfpIntra(boolean dfpIntra) {
        this.dfpIntra = dfpIntra
    }

    boolean getDfpInter() {
        return dfpInter
    }

    void setDfpInter(boolean dfpInter) {
        this.dfpInter = dfpInter
    }

    boolean getCd() {
        return cd
    }

    void setCd(boolean cd) {
        this.cd = cd
    }

    boolean getCde() {
        return cde
    }

    void setCde(boolean cde) {
        this.cde = cde
    }

    boolean getPdg() {
        return pdg
    }

    void setPdg(boolean pdg) {
        this.pdg = pdg
    }

    boolean getPdge() {
        return pdge
    }

    void setPdge(boolean pdge) {
        this.pdge = pdge
    }

    boolean getPessimisticDataflow() {
        return pessimisticDataflow
    }

    void setPessimisticDataflow(boolean pessimisticDataflow) {
        this.pessimisticDataflow = pessimisticDataflow
    }

    boolean getReachability() {
        return reachability
    }

    void setReachability(boolean reachability) {
        this.reachability = reachability
    }

    int getTimeout() {
        return timeout
    }

    void setTimeout(int timeout) {
        this.timeout = timeout
    }
}