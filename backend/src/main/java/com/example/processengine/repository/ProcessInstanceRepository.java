package com.example.processengine.repository;

import com.example.processengine.model.ProcessInstance;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 简单内存流程实例仓库
 */
@Repository
public class ProcessInstanceRepository {
    private final Map<String, ProcessInstance> store = new HashMap<>();

    public List<ProcessInstance> findAll() {
        return new ArrayList<>(store.values());
    }

    public ProcessInstance findById(String id) {
        return store.get(id);
    }

    public void save(ProcessInstance instance) {
        store.put(instance.getId(), instance);
    }

    public void delete(String id) {
        store.remove(id);
    }
}
