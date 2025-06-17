package com.example.processengine.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程实例实体，跟踪流程执行状态
 */
public class ProcessInstance {
    private String id;
    private String processDefinitionId;
    private String currentNodeId;
    private String status; // RUNNING|COMPLETED|FAILED
    private Map<String, String> nodeStatus = new HashMap<>(); // 节点ID -> 状态

    // getter/setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }
    public String getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(String currentNodeId) { this.currentNodeId = currentNodeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, String> getNodeStatus() { return nodeStatus; }
    public void setNodeStatus(Map<String, String> nodeStatus) { this.nodeStatus = nodeStatus; }
}
