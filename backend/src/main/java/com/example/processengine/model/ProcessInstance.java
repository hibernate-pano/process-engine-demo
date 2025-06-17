package com.example.processengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程实例实体，跟踪流程执行状态
 */
@Entity
@Table(name = "process_instances")
public class ProcessInstance {
    @Id
    private String id;
    private String processDefinitionId;
    private String currentNodeId;
    private String status; // RUNNING|COMPLETED|FAILED

    @Convert(converter = JsonToMapConverter.class)
    @Column(columnDefinition = "TEXT")
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

    // 定义一个静态内部类作为 JPA 属性转换器
    @Converter(autoApply = false)
    public static class JsonToMapConverter implements AttributeConverter<Map<String, String>, String> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(Map<String, String> attribute) {
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting map to JSON string", e);
            }
        }

        @Override
        public Map<String, String> convertToEntityAttribute(String dbData) {
            try {
                return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting JSON string to map", e);
            }
        }
    }
}
