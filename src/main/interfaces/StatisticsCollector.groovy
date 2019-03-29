package main.interfaces

import main.project.*

public interface StatisticsCollector {
    
    public void collectStatistics(Project project, MergeCommit mergeCommit)
}