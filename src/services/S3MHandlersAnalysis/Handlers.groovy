package services.S3MHandlersAnalysis

enum Handlers {
    Renaming

    static final Map<Integer, String> mergeResultPaths = [0: 'textual.java', 1: 'Renaming/CT.java', 2: 'Renaming/SF.java', 3: 'Renaming/MM.java', 4: 'Renaming/KB.java']
    static final Map<Integer, String> mergeAlgorithms = [0: 'TM', 1: 'CT', 2: 'SF', 3: 'MM', 4: 'KB']

}