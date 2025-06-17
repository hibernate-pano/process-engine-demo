package com.example.processengine.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * 流程定义实体，保存流程结构（JSON 格式，兼容 vue-flow 数据结构）
 */
@Entity
@Table(name = "process_definitions")
public class ProcessDefinition {
    @Id
    private String id;
    private String name;

    @Convert(converter = JsonToListMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Map<String, Object>> nodes; // vue-flow 节点

    @Convert(converter = JsonToListMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Map<String, Object>> edges; // vue-flow 连线

    // getter/setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Map<String, Object>> getNodes() { return nodes; }
    public void setNodes(List<Map<String, Object>> nodes) { this.nodes = nodes; }
    public List<Map<String, Object>> getEdges() { return edges; }
    public void setEdges(List<Map<String, Object>> edges) { this.edges = edges; }

    // 定义一个静态内部类作为 JPA 属性转换器
    @Converter(autoApply = false)
    public static class JsonToListMapConverter implements AttributeConverter<List<Map<String, Object>>, String> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public String convertToDatabaseColumn(List<Map<String, Object>> attribute) {
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting list to JSON string", e);
            }
        }

        @Override
        public List<Map<String, Object>> convertToEntityAttribute(String dbData) {
            try {
                return objectMapper.readValue(dbData, new TypeReference<List<Map<String, Object>>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error converting JSON string to list", e);
            }
        }
    }
}
