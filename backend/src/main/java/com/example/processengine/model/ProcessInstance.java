package com.example.processengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
    private String status; // RUNNING|COMPLETED|FAILED|REPLAY_RUNNING|REPLAY_PAUSED

    @Convert(converter = JsonToMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> nodeStatus = new HashMap<>(); // 节点ID -> 状态

    @Convert(converter = JsonToExecutionHistoryConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<NodeExecutionRecord> executionHistory = new ArrayList<>();

    // getter/setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(Map<String, String> nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public List<NodeExecutionRecord> getExecutionHistory() {
        return executionHistory;
    }

    public void setExecutionHistory(List<NodeExecutionRecord> executionHistory) {
        this.executionHistory = executionHistory;
    }

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
                return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting JSON string to map", e);
            }
        }
    }

    // 节点执行历史记录
    public static class NodeExecutionRecord {
        private String nodeId;
        private String startTime;
        private String endTime;
        private String status; // RUNNING|COMPLETED|FAILED
        private String input;
        private String output;

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    // JPA 属性转换器：List<NodeExecutionRecord> <-> JSON
    @Converter(autoApply = false)
    public static class JsonToExecutionHistoryConverter
            implements AttributeConverter<List<NodeExecutionRecord>, String> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<NodeExecutionRecord> attribute) {
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting executionHistory to JSON string", e);
            }
        }

        @Override
        public List<NodeExecutionRecord> convertToEntityAttribute(String dbData) {
            try {
                return objectMapper.readValue(dbData, new TypeReference<List<NodeExecutionRecord>>() {
                });
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting JSON string to executionHistory", e);
            }
        }
    }
}
