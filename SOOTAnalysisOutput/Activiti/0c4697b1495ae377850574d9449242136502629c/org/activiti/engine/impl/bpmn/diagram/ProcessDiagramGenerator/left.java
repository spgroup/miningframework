package org.activiti.engine.impl.bpmn.diagram;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;

public class ProcessDiagramGenerator {

    protected static final Map<Class<? extends BaseElement>, ActivityDrawInstruction> activityDrawInstructions = new HashMap<Class<? extends BaseElement>, ActivityDrawInstruction>();

    static {
        activityDrawInstructions.put(StartEvent.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                StartEvent startEvent = (StartEvent) flowNode;
                if (startEvent.getEventDefinitions() != null && startEvent.getEventDefinitions().size() > 0) {
                    if (startEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
                        processDiagramCanvas.drawTimerStartEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                    } else if (startEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
                        processDiagramCanvas.drawErrorStartEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                    }
                } else {
                    processDiagramCanvas.drawNoneStartEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                }
            }
        });
        activityDrawInstructions.put(IntermediateCatchEvent.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                IntermediateCatchEvent intermediateCatchEvent = (IntermediateCatchEvent) flowNode;
                if (intermediateCatchEvent.getEventDefinitions() != null && intermediateCatchEvent.getEventDefinitions().size() > 0) {
                    if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
                        processDiagramCanvas.drawCatchingSignalEvent(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), false);
                    } else if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
                        processDiagramCanvas.drawCatchingTimerEvent(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), false);
                    }
                }
            }
        });
        activityDrawInstructions.put(ThrowEvent.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                ThrowEvent throwEvent = (ThrowEvent) flowNode;
                if (throwEvent.getEventDefinitions() != null && throwEvent.getEventDefinitions().size() > 0) {
                    if (throwEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
                        processDiagramCanvas.drawThrowingSignalEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                    }
                } else {
                    processDiagramCanvas.drawThrowingNoneEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                }
            }
        });
        activityDrawInstructions.put(EndEvent.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                EndEvent endEvent = (EndEvent) flowNode;
                if (endEvent.getEventDefinitions() != null && endEvent.getEventDefinitions().size() > 0) {
                    if (endEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
                        processDiagramCanvas.drawErrorEndEvent(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                    }
                } else {
                    processDiagramCanvas.drawNoneEndEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
                }
            }
        });
        activityDrawInstructions.put(Task.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(UserTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawUserTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ScriptTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawScriptTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ServiceTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawServiceTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ReceiveTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawReceiveTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(SendTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawSendTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ManualTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawManualTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(BusinessRuleTask.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawBusinessRuleTask(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ExclusiveGateway.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawExclusiveGateway((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(InclusiveGateway.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawInclusiveGateway((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(ParallelGateway.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawParallelGateway((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(EventGateway.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawEventBasedGateway((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
        activityDrawInstructions.put(BoundaryEvent.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                BoundaryEvent boundaryEvent = (BoundaryEvent) flowNode;
                if (boundaryEvent.getEventDefinitions() != null && boundaryEvent.getEventDefinitions().size() > 0) {
                    if (boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
                        processDiagramCanvas.drawCatchingTimerEvent(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), boundaryEvent.isCancelActivity());
                    } else if (boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
                        processDiagramCanvas.drawCatchingErrorEvent((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), boundaryEvent.isCancelActivity());
                    } else if (boundaryEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
                        processDiagramCanvas.drawCatchingSignalEvent(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), boundaryEvent.isCancelActivity());
                    }
                }
            }
        });
        activityDrawInstructions.put(SubProcess.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                if (graphicInfo.getExpanded() != null && graphicInfo.getExpanded() == false) {
                    processDiagramCanvas.drawCollapsedSubProcess(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), false);
                } else {
                    processDiagramCanvas.drawExpandedSubProcess(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), false);
                }
            }
        });
        activityDrawInstructions.put(EventSubProcess.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                if (graphicInfo.getExpanded() != null && graphicInfo.getExpanded() == false) {
                    processDiagramCanvas.drawCollapsedSubProcess(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), true);
                } else {
                    processDiagramCanvas.drawExpandedSubProcess(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), true);
                }
            }
        });
        activityDrawInstructions.put(CallActivity.class, new ActivityDrawInstruction() {

            public void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
                processDiagramCanvas.drawCollapsedCallActivity(flowNode.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        });
    }

    public static InputStream generatePngDiagram(BpmnModel bpmnModel) {
        return generateDiagram(bpmnModel, "png", Collections.<String>emptyList());
    }

    public static InputStream generateJpgDiagram(BpmnModel bpmnModel) {
        return generateDiagram(bpmnModel, "jpg", Collections.<String>emptyList());
    }

    protected static ProcessDiagramCanvas generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities) {
        return generateDiagram(bpmnModel, highLightedActivities, Collections.<String>emptyList());
    }

    protected static ProcessDiagramCanvas generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows) {
        ProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(bpmnModel);
        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            processDiagramCanvas.drawPoolOrLane(pool.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
        }
        for (Process process : bpmnModel.getProcesses()) {
            for (Lane lane : process.getLanes()) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(lane.getId());
                processDiagramCanvas.drawPoolOrLane(lane.getName(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
            }
        }
        for (FlowNode flowNode : bpmnModel.getProcesses().get(0).findFlowElementsOfType(FlowNode.class)) {
            drawActivity(processDiagramCanvas, bpmnModel, flowNode, highLightedActivities, highLightedFlows);
        }
        return processDiagramCanvas;
    }

    public static InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities) {
        ProcessDiagramCanvas canvas = generateDiagram(bpmnModel, highLightedActivities, Collections.<String>emptyList());
        return canvas.generateImage(imageType);
    }

    public static InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities, List<String> highLightedFlows) {
        return generateDiagram(bpmnModel, highLightedActivities, highLightedFlows).generateImage(imageType);
    }

    protected static void drawActivity(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode, List<String> highLightedActivities, List<String> highLightedFlows) {
        ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(flowNode.getClass());
        if (drawInstruction != null) {
            drawInstruction.draw(processDiagramCanvas, bpmnModel, flowNode);
            boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
            if (flowNode instanceof Activity) {
                Activity activity = (Activity) flowNode;
                MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
                if (multiInstanceLoopCharacteristics != null) {
                    multiInstanceSequential = multiInstanceLoopCharacteristics.isSequential();
                    multiInstanceParallel = !multiInstanceSequential;
                }
            }
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            if (flowNode instanceof SubProcess) {
                collapsed = graphicInfo.getExpanded() != null && graphicInfo.getExpanded() == false;
            } else if (flowNode instanceof CallActivity) {
                collapsed = true;
            }
            processDiagramCanvas.drawActivityMarkers((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), multiInstanceSequential, multiInstanceParallel, collapsed);
            if (highLightedActivities.contains(flowNode.getId())) {
                drawHighLight(processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()));
            }
        }
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
            List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
            boolean drawedLabel = false;
            for (int i = 1; i < graphicInfoList.size(); i++) {
                GraphicInfo graphicInfo = graphicInfoList.get(i);
                GraphicInfo previousGraphicInfo = graphicInfoList.get(i - 1);
                boolean highLighted = (highLightedFlows.contains(sequenceFlow.getId()));
                boolean drawConditionalIndicator = (i == 1) && sequenceFlow.getConditionExpression() != null && !(flowNode instanceof Gateway);
                if (i < graphicInfoList.size() - 1) {
                    processDiagramCanvas.drawSequenceflowWithoutArrow((int) previousGraphicInfo.getX(), (int) previousGraphicInfo.getY(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), drawConditionalIndicator, highLighted);
                } else {
                    processDiagramCanvas.drawSequenceflow((int) previousGraphicInfo.getX(), (int) previousGraphicInfo.getY(), (int) graphicInfo.getX(), (int) graphicInfo.getY(), drawConditionalIndicator, highLighted);
                    if (!drawedLabel) {
                        GraphicInfo labelGraphicInfo = bpmnModel.getLabelGraphicInfo(sequenceFlow.getId());
                        if (labelGraphicInfo != null) {
                            int middleX = (int) (((previousGraphicInfo.getX() + labelGraphicInfo.getX()) + (graphicInfo.getX() + labelGraphicInfo.getX())) / 2);
                            int middleY = (int) (((previousGraphicInfo.getY() + labelGraphicInfo.getY()) + (graphicInfo.getY() + labelGraphicInfo.getY())) / 2);
                            middleX += 15;
                            processDiagramCanvas.drawLabel(sequenceFlow.getName(), middleX, middleY, (int) labelGraphicInfo.getWidth(), (int) labelGraphicInfo.getHeight());
                            drawedLabel = true;
                        }
                    }
                }
            }
        }
        if (flowNode instanceof FlowElementsContainer) {
            for (FlowElement nestedFlowElement : ((FlowElementsContainer) flowNode).getFlowElements()) {
                if (nestedFlowElement instanceof FlowNode) {
                    drawActivity(processDiagramCanvas, bpmnModel, (FlowNode) nestedFlowElement, highLightedActivities, highLightedFlows);
                }
            }
        }
    }

    private static void drawHighLight(ProcessDiagramCanvas processDiagramCanvas, GraphicInfo graphicInfo) {
        processDiagramCanvas.drawHighLight((int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());
    }

    protected static ProcessDiagramCanvas initProcessDiagramCanvas(BpmnModel bpmnModel) {
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;
        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            minX = graphicInfo.getX();
            maxX = graphicInfo.getX() + graphicInfo.getWidth();
            minY = graphicInfo.getY();
            maxY = graphicInfo.getY() + graphicInfo.getHeight();
        }
        List<FlowNode> flowNodes = gatherAllFlowNodes(bpmnModel);
        for (FlowNode flowNode : flowNodes) {
            GraphicInfo flowNodeGraphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            if (flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth() > maxX) {
                maxX = flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth();
            }
            if (flowNodeGraphicInfo.getX() < minX) {
                minX = flowNodeGraphicInfo.getX();
            }
            if (flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight() > maxY) {
                maxY = flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight();
            }
            if (flowNodeGraphicInfo.getY() < minY) {
                minY = flowNodeGraphicInfo.getY();
            }
            for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
                for (GraphicInfo graphicInfo : graphicInfoList) {
                    if (graphicInfo.getX() > maxX) {
                        maxX = graphicInfo.getX();
                    }
                    if (graphicInfo.getX() < minX) {
                        minX = graphicInfo.getX();
                    }
                    if (graphicInfo.getY() > maxY) {
                        maxY = graphicInfo.getY();
                    }
                    if (graphicInfo.getY() < minY) {
                        minY = graphicInfo.getY();
                    }
                }
            }
        }
        int nrOfLanes = 0;
        for (Process process : bpmnModel.getProcesses()) {
            for (Lane l : process.getLanes()) {
                nrOfLanes++;
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(l.getId());
                if (graphicInfo.getX() + graphicInfo.getWidth() > maxX) {
                    maxX = graphicInfo.getX() + graphicInfo.getWidth();
                }
                if (graphicInfo.getX() < minX) {
                    minX = graphicInfo.getX();
                }
                if (graphicInfo.getY() + graphicInfo.getHeight() > maxY) {
                    maxY = graphicInfo.getY() + graphicInfo.getHeight();
                }
                if (graphicInfo.getY() < minY) {
                    minY = graphicInfo.getY();
                }
            }
        }
        if (flowNodes.size() == 0 && bpmnModel.getPools().size() == 0 && nrOfLanes == 0) {
            minX = 0;
            minY = 0;
        }
        return new ProcessDiagramCanvas((int) maxX + 10, (int) maxY + 10, (int) minX, (int) minY);
    }

    protected static List<FlowNode> gatherAllFlowNodes(BpmnModel bpmnModel) {
        List<FlowNode> flowNodes = new ArrayList<FlowNode>();
        for (Process process : bpmnModel.getProcesses()) {
            flowNodes.addAll(gatherAllFlowNodes(process));
        }
        return flowNodes;
    }

    protected static List<FlowNode> gatherAllFlowNodes(FlowElementsContainer flowElementsContainer) {
        List<FlowNode> flowNodes = new ArrayList<FlowNode>();
        for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (flowElement instanceof FlowNode) {
                flowNodes.add((FlowNode) flowElement);
            }
            if (flowElement instanceof FlowElementsContainer) {
                flowNodes.addAll(gatherAllFlowNodes((FlowElementsContainer) flowElement));
            }
        }
        return flowNodes;
    }

    protected interface ActivityDrawInstruction {

        void draw(ProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode);
    }
}
