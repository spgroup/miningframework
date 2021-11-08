package util

enum TextualMergeStrategy {
    CSDiff('csdiff'),
    Diff3('diff3'),
    ConsecutiveLines('consecutive'),
    CSDiffAndDiff3('autotuning')

    private String commandLineOption

    private TextualMergeStrategy(String commandLineOption) {
        this.commandLineOption = commandLineOption
    }

    public String getCommandLineOption() {
        return this.commandLineOption
    }
}