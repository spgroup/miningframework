package com.netflix.conductor.core.execution;

import java.util.HashSet;
import java.util.Set;
import com.netflix.conductor.core.execution.tasks.Decision;
import com.netflix.conductor.core.execution.tasks.Event;
import com.netflix.conductor.core.execution.tasks.Fork;
import com.netflix.conductor.core.execution.tasks.Join;
import com.netflix.conductor.core.execution.tasks.SubWorkflow;
import com.netflix.conductor.core.execution.tasks.Wait;
import com.netflix.conductor.core.execution.tasks.WorkflowSystemTask;

public enum SystemTaskType {

    DECISION(new Decision()),
    FORK(new Fork()),
    JOIN(new Join()),
    SUB_WORKFLOW(new SubWorkflow()),
    EVENT(new Event()),
    WAIT(new Wait());

    private static Set<String> builtInTasks = new HashSet<>();

    static {
        builtInTasks.add(SystemTaskType.DECISION.name());
        builtInTasks.add(SystemTaskType.FORK.name());
        builtInTasks.add(SystemTaskType.JOIN.name());
        builtInTasks.add(SystemTaskType.SUB_WORKFLOW.name());
        builtInTasks.add(SystemTaskType.WAIT.name());
        builtInTasks.add(SystemTaskType.EVENT.name());
    }

    private WorkflowSystemTask impl;

    SystemTaskType(WorkflowSystemTask impl) {
        this.impl = impl;
    }

    public WorkflowSystemTask impl() {
        return this.impl;
    }

    public static boolean is(String taskType) {
        return WorkflowSystemTask.is(taskType);
    }

    public static boolean isBuiltIn(String taskType) {
        return is(taskType) && builtInTasks.contains(taskType);
    }
}