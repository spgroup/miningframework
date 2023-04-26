package com.netflix.conductor.common.metadata.tasks;

import java.util.HashMap;
import java.util.Map;

public class TaskResult {

    public enum Status {

        IN_PROGRESS, FAILED, COMPLETED
    }

    private String workflowInstanceId;

    private String taskId;

    private String reasonForIncompletion;

    private long callbackAfterSeconds;

    private String workerId;

    private Status status;

    private Map<String, Object> outputData = new HashMap<>();

    private TaskExecLog log = new TaskExecLog();

    public TaskResult(Task task) {
        this.workflowInstanceId = task.getWorkflowInstanceId();
        this.taskId = task.getTaskId();
        this.reasonForIncompletion = task.getReasonForIncompletion();
        this.callbackAfterSeconds = task.getCallbackAfterSeconds();
        this.status = Status.valueOf(task.getStatus().name());
        this.workerId = task.getWorkerId();
        this.outputData = task.getOutputData();
    }

    public TaskResult(String workflowInstanceId, String taskId) {
        this.workflowInstanceId = workflowInstanceId;
        this.taskId = taskId;
    }

    public TaskResult() {
    }

    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(String workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getReasonForIncompletion() {
        return reasonForIncompletion;
    }

    public void setReasonForIncompletion(String reasonForIncompletion) {
        this.reasonForIncompletion = reasonForIncompletion;
    }

    public long getCallbackAfterSeconds() {
        return callbackAfterSeconds;
    }

    public void setCallbackAfterSeconds(long callbackAfterSeconds) {
        this.callbackAfterSeconds = callbackAfterSeconds;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Object> getOutputData() {
        return outputData;
    }

    public void setOutputData(Map<String, Object> outputData) {
        this.outputData = outputData;
    }

    public TaskExecLog getLog() {
        return log;
    }

    public void setLog(TaskExecLog log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return "TaskResult [workflowInstanceId=" + workflowInstanceId + ", taskId=" + taskId + ", status=" + status + "]";
    }
}
