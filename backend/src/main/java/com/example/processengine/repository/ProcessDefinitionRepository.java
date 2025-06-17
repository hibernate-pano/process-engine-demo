package com.example.processengine.repository;

import com.example.processengine.model.ProcessDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 简单内存流程定义仓库
 */
@Repository
public class ProcessDefinitionRepository {
    private final Map<String, ProcessDefinition> store = new HashMap<>();

    public List<ProcessDefinition> findAll() {
        return new ArrayList<>(store.values());
    }

    public ProcessDefinition findById(String id) {
        return store.get(id);
    }

    public void save(ProcessDefinition definition) {
        store.put(definition.getId(), definition);
    }

    public void delete(String id) {
        store.remove(id);
    }
}
