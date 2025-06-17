package com.example.processengine.controller;

import com.example.processengine.model.ProcessDefinition;
import com.example.processengine.service.ProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义相关接口
 */
@RestController
@RequestMapping("/api/process-definitions")
public class ProcessDefinitionController {
    @Autowired
    private ProcessDefinitionService service;

    @GetMapping
    public List<ProcessDefinition> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ProcessDefinition getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PostMapping
    public void save(@RequestBody ProcessDefinition definition) {
        service.save(definition);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
