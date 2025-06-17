package com.example.processengine.model;

import java.util.List;
import java.util.Map;

/**
 * 流程定义实体，保存流程结构（JSON 格式，兼容 vue-flow 数据结构）
 */
public class ProcessDefinition {
    private String id;
    private String name;
    private String deviceType;
    private List<Map<String, Object>> nodes; // vue-flow 节点
    private List<Map<String, Object>> edges; // vue-flow 连线

    // getter/setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public List<Map<String, Object>> getNodes() { return nodes; }
    public void setNodes(List<Map<String, Object>> nodes) { this.nodes = nodes; }
    public List<Map<String, Object>> getEdges() { return edges; }
    public void setEdges(List<Map<String, Object>> edges) { this.edges = edges; }
}
