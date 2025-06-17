package com.example.processengine.service;

import com.example.processengine.model.ProcessDefinition;
import com.example.processengine.model.ProcessInstance;
import com.example.processengine.repository.ProcessDefinitionRepository;
import com.example.processengine.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 流程实例服务
 */
@Service
public class ProcessInstanceService {
    @Autowired
    private ProcessInstanceRepository instanceRepository;
    @Autowired
    private ProcessDefinitionRepository definitionRepository;
    @Autowired
    private ProcessDefinitionService definitionService;

    /**
     * 启动流程实例
     */
    public ProcessInstance startInstance(String processDefinitionId) {
        ProcessDefinition definition = definitionRepository.findById(processDefinitionId).orElse(null);
        if (definition == null)
            return null;
        ProcessInstance instance = new ProcessInstance();
        instance.setId(UUID.randomUUID().toString());
        instance.setProcessDefinitionId(processDefinitionId);
        instance.setStatus("RUNNING");
        // 初始化节点状态
        Map<String, String> nodeStatus = new HashMap<>();
        if (definition.getNodes() != null) {
            for (Map<String, Object> node : definition.getNodes()) {
                String nodeId = (String) node.get("id");
                nodeStatus.put(nodeId, "PENDING");
            }
            // 设置第一个节点为 RUNNING
            if (!definition.getNodes().isEmpty()) {
                String firstNodeId = (String) definition.getNodes().get(0).get("id");
                nodeStatus.put(firstNodeId, "RUNNING");
                instance.setCurrentNodeId(firstNodeId);
            }
        }
        instance.setNodeStatus(nodeStatus);
        // 初始化执行历史
        List<ProcessInstance.NodeExecutionRecord> history = new ArrayList<>();
        if (!definition.getNodes().isEmpty()) {
            String firstNodeId = (String) definition.getNodes().get(0).get("id");
            ProcessInstance.NodeExecutionRecord record = new ProcessInstance.NodeExecutionRecord();
            record.setNodeId(firstNodeId);
            record.setStartTime(java.time.Instant.now().toString());
            record.setStatus("RUNNING");
            record.setInput("Process started.");
            history.add(record);
        }
        instance.setExecutionHistory(history);
        instanceRepository.save(instance);
        return instance;
    }

    public ProcessInstance getById(String id) {
        return instanceRepository.findById(id).orElse(null);
    }

    public List<ProcessInstance> listAll() {
        return instanceRepository.findAll();
    }

    public String replayInstance(String id, String action) {
        ProcessInstance instance = instanceRepository.findById(id).orElse(null);
        if (instance == null)
            return "Instance not found";
        ProcessDefinition def = definitionService.getById(instance.getProcessDefinitionId());
        if (def == null)
            return "Definition not found";
        List<Map<String, Object>> nodes = def.getNodes();
        if (nodes == null || nodes.isEmpty())
            return "No nodes in definition";

        switch (action.toLowerCase()) {
            case "start":
                instance.setStatus("REPLAY_RUNNING");
                instance.setCurrentNodeId(nodes.get(0).get("id").toString());
                instance.setNodeStatus(new HashMap<>());
                instance.getNodeStatus().put(instance.getCurrentNodeId(), "RUNNING");

                List<ProcessInstance.NodeExecutionRecord> newHistory = new ArrayList<>();
                ProcessInstance.NodeExecutionRecord startRecord = new ProcessInstance.NodeExecutionRecord();
                startRecord.setNodeId(instance.getCurrentNodeId());
                startRecord.setStartTime(java.time.Instant.now().toString());
                startRecord.setStatus("RUNNING");
                newHistory.add(startRecord);
                instance.setExecutionHistory(newHistory);
                instanceRepository.save(instance);
                return "Replay started from beginning.";
            case "pause":
                if ("REPLAY_RUNNING".equals(instance.getStatus())) {
                    instance.setStatus("REPLAY_PAUSED");
                    // 更新当前节点的结束时间为暂停时间，状态为PAUSED
                    List<ProcessInstance.NodeExecutionRecord> history = instance.getExecutionHistory();
                    if (!history.isEmpty()) {
                        ProcessInstance.NodeExecutionRecord currentRecord = history.get(history.size() - 1);
                        if (currentRecord.getEndTime() == null) { // 确保是当前运行的节点
                            currentRecord.setEndTime(java.time.Instant.now().toString());
                            currentRecord.setStatus("PAUSED");
                        }
                    }
                    instanceRepository.save(instance);
                    return "Replay paused.";
                }
                return "Instance is not running a replay to pause.";
            case "resume":
                if ("REPLAY_PAUSED".equals(instance.getStatus())) {
                    instance.setStatus("REPLAY_RUNNING");
                    // 更新当前节点的开始时间为恢复时间，状态为RUNNING
                    List<ProcessInstance.NodeExecutionRecord> history = instance.getExecutionHistory();
                    if (!history.isEmpty()) {
                        ProcessInstance.NodeExecutionRecord currentRecord = history.get(history.size() - 1);
                        currentRecord.setStartTime(java.time.Instant.now().toString()); // 更新开始时间
                        currentRecord.setStatus("RUNNING");
                        currentRecord.setEndTime(null); // 清除结束时间，表示再次运行
                    }
                    instanceRepository.save(instance);
                    return "Replay resumed.";
                }
                return "Instance is not paused to resume.";
            case "step":
                if (!"RUNNING".equals(instance.getStatus()) && !"REPLAY_RUNNING".equals(instance.getStatus())) {
                    return "Instance is not in a running or replay running state to step.";
                }
                String currId = instance.getCurrentNodeId();
                Map<String, Object> currNode = null;
                for (Map<String, Object> node : nodes) {
                    if (currId != null && currId.equals(node.get("id"))) {
                        currNode = node;
                        break;
                    }
                }
                if (currNode == null) {
                    instance.setStatus("FAILED");
                    instanceRepository.save(instance);
                    return "Current node not found in definition.";
                }

                // Complete current node record in history
                List<ProcessInstance.NodeExecutionRecord> history = instance.getExecutionHistory();
                ProcessInstance.NodeExecutionRecord currRecord = null;
                for (ProcessInstance.NodeExecutionRecord r : history) {
                    if (r.getNodeId().equals(currId) && r.getEndTime() == null) {
                        currRecord = r;
                        break;
                    }
                }
                if (currRecord != null) {
                    currRecord.setEndTime(java.time.Instant.now().toString());
                    currRecord.setStatus("COMPLETED");
                    // 模拟输出
                    String simulatedOutput = "";
                    if ("action".equals(currNode.get("type"))) {
                        // 获取 customProps 中的 deviceType 和 deviceAction
                        Map<String, String> customProps = (Map<String, String>) currNode.getOrDefault("customProps",
                                new HashMap<>());
                        String deviceType = customProps.getOrDefault("deviceType", "N/A");
                        String deviceAction = customProps.getOrDefault("deviceAction", "N/A");
                        simulatedOutput = "Action node processed: deviceType=" + deviceType + ", deviceAction="
                                + deviceAction;
                    } else if ("condition".equals(currNode.get("type"))) {
                        String conditionExpression = (String) currNode.getOrDefault("condition", "");
                        String input = currRecord.getInput(); // 获取当前节点的输入（即上一个节点的输出）
                        boolean conditionResult = evaluateCondition(conditionExpression, input); // 模拟条件评估
                        simulatedOutput = "Condition evaluated: " + conditionResult + "; Expression: \""
                                + conditionExpression + "\", Input: \"" + input + "\"";
                    } else {
                        simulatedOutput = "Node " + currNode.get("id") + " processed.";
                    }
                    currRecord.setOutput(simulatedOutput);
                    instance.getNodeStatus().put(currId, "COMPLETED");
                }

                String nextId = null;
                List<Map<String, Object>> outgoingEdges = new ArrayList<>();
                for (Map<String, Object> edge : def.getEdges()) {
                    if (currId != null && currId.equals(edge.get("source"))) {
                        outgoingEdges.add(edge);
                    }
                }

                if (!outgoingEdges.isEmpty()) {
                    if ("condition".equals(currNode.get("type"))) {
                        String conditionExpression = (String) currNode.getOrDefault("condition", "");
                        String input = currRecord != null ? currRecord.getInput() : null;
                        boolean conditionResult = evaluateCondition(conditionExpression, input);

                        // 尝试查找匹配条件的边
                        for (Map<String, Object> edge : outgoingEdges) {
                            String edgeLabel = (String) edge.getOrDefault("label", "");
                            if (conditionResult && "true".equalsIgnoreCase(edgeLabel)) {
                                nextId = (String) edge.get("target");
                                break;
                            } else if (!conditionResult && "false".equalsIgnoreCase(edgeLabel)) {
                                nextId = (String) edge.get("target");
                                break;
                            }
                        }

                        // 如果没有匹配的带标签的边，或者条件表达式为空，默认选择第一条出边
                        if (nextId == null) {
                            nextId = (String) outgoingEdges.get(0).get("target");
                        }
                    } else { // action, input, output or other types
                        nextId = (String) outgoingEdges.get(0).get("target");
                    }
                }

                if (nextId == null) {
                    // No outgoing edges, process ends or is completed
                    instance.setStatus("COMPLETED");
                    instanceRepository.save(instance);
                    return "Process completed or no valid next node.";
                }

                // Start next node
                instance.setCurrentNodeId(nextId);
                instance.getNodeStatus().put(nextId, "RUNNING");
                ProcessInstance.NodeExecutionRecord nextRecord = new ProcessInstance.NodeExecutionRecord();
                nextRecord.setNodeId(nextId);
                nextRecord.setStartTime(java.time.Instant.now().toString());
                nextRecord.setStatus("RUNNING");
                // 将上一个节点的输出作为当前节点的输入
                if (currRecord != null) {
                    nextRecord.setInput(currRecord.getOutput());
                }
                history.add(nextRecord);
                instanceRepository.save(instance);
                return "Step to node " + nextId;
            default:
                return "Unknown replay action: " + action;
        }
    }

    private boolean evaluateCondition(String conditionExpression, String input) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            return true; // 没有条件表达式，默认通过
        }
        if (input == null) {
            return false; // 没有输入，无法评估条件
        }

        String lowerCaseExpression = conditionExpression.toLowerCase().trim();
        String lowerCaseInput = input.toLowerCase().trim();

        // 模拟简单条件判断：例如 "input === 'success'" 或 "input contains 'error'"
        if (lowerCaseExpression.startsWith("input === ")) {
            String expectedValue = lowerCaseExpression.substring("input === ".length()).trim().replaceAll("'\"", "");
            return lowerCaseInput.equals(expectedValue);
        } else if (lowerCaseExpression.startsWith("input contains ")) {
            String keyword = lowerCaseExpression.substring("input contains ".length()).trim().replaceAll("'\"", "");
            return lowerCaseInput.contains(keyword);
        }

        // 默认或无法识别的条件，返回 false
        return false;
    }
}
