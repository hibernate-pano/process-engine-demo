package com.example.processengine.service;

import com.example.processengine.model.ProcessDefinition;
import com.example.processengine.repository.ProcessDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程定义服务
 */
@Service
public class ProcessDefinitionService {
    @Autowired
    private ProcessDefinitionRepository repository;

    public List<ProcessDefinition> listAll() {
        return repository.findAll();
    }

    public ProcessDefinition getById(String id) {
        return repository.findById(id).orElse(null);
    }

    public ProcessDefinition save(ProcessDefinition definition) {
        return repository.save(definition);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
