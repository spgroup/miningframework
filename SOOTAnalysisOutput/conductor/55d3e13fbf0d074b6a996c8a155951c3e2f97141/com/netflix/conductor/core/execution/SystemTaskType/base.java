package com.netflix.conductor.core.execution;

import java.util.HashSet;
import java.util.Set;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.core.execution.tasks.Decision;
import com.netflix.conductor.core.execution.tasks.Fork;
import com.netflix.conductor.core.execution.tasks.Join;
import com.netflix.conductor.core.execution.tasks.SubWorkflow;
import com.netflix.conductor.core.execution.tasks.WorkflowSystemTask;

public enum SystemTaskType {

    DECISION(new Decision()), FORK(new Fork()), JOIN(new Join()), SUB_WORKFLOW(new SubWorkflow());

    private WorkflowSystemTask impl;

    private static Set<String> builtInTasks = new HashSet<>();

    static {
        builtInTasks.add(SystemTaskType.DECISION.name());
        builtInTasks.add(SystemTaskType.FORK.name());
        builtInTasks.add(SystemTaskType.JOIN.name());
        builtInTasks.add(SystemTaskType.SUB_WORKFLOW.name());
    }

    SystemTaskType(WorkflowSystemTask impl) {
        this.impl = impl;
    }

    public static boolean is(String taskType) {
        return WorkflowSystemTask.is(taskType);
    }

    public WorkflowSystemTask impl() {
        return impl;
    }

    public void cancel(Workflow workflow, Task task, WorkflowExecutor workflowExecutor) throws Exception {
        WorkflowSystemTask st = WorkflowSystemTask.get(task.getTaskType());
        st.cancel(workflow, task, workflowExecutor);
    }

    public void start(Workflow workflow, Task task, WorkflowExecutor workflowExecutor) throws Exception {
        WorkflowSystemTask st = WorkflowSystemTask.get(task.getTaskType());
        st.start(workflow, task, workflowExecutor);
    }

    public static boolean isBuiltIn(String taskType) {
        return is(taskType) && builtInTasks.contains(taskType);
    }
}
