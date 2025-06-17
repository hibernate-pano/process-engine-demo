package com.example.processengine.controller;

import com.example.processengine.model.ProcessInstance;
import com.example.processengine.service.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程实例相关接口
 */
@RestController
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {
    @Autowired
    private ProcessInstanceService service;

    @PostMapping
    public ProcessInstance startInstance(@RequestBody Map<String, String> req) {
        String processDefinitionId = req.get("processDefinitionId");
        return service.startInstance(processDefinitionId);
    }

    @GetMapping("/{id}")
    public ProcessInstance getById(@PathVariable String id) {
        return service.getById(id);
    }

    @GetMapping
    public List<ProcessInstance> listAll() {
        return service.listAll();
    }
}
