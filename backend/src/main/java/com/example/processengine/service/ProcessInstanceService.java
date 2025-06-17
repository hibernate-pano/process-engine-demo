package com.example.processengine.service;

import com.example.processengine.model.ProcessDefinition;
import com.example.processengine.model.ProcessInstance;
import com.example.processengine.repository.ProcessDefinitionRepository;
import com.example.processengine.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 流程实例服务
 */
@Service
public class ProcessInstanceService {
    @Autowired
    private ProcessInstanceRepository instanceRepository;
    @Autowired
    private ProcessDefinitionRepository definitionRepository;

    /**
     * 启动流程实例
     */
    public ProcessInstance startInstance(String processDefinitionId) {
        ProcessDefinition definition = definitionRepository.findById(processDefinitionId);
        if (definition == null) return null;
        ProcessInstance instance = new ProcessInstance();
        instance.setId(UUID.randomUUID().toString());
        instance.setProcessDefinitionId(processDefinitionId);
        instance.setStatus("RUNNING");
        // 初始化节点状态
        Map<String, String> nodeStatus = new HashMap<>();
        if (definition.getNodes() != null) {
            for (Map<String, Object> node : definition.getNodes()) {
                String nodeId = (String) node.get("id");
                nodeStatus.put(nodeId, "PENDING");
            }
            // 设置第一个节点为 RUNNING
            if (!definition.getNodes().isEmpty()) {
                String firstNodeId = (String) definition.getNodes().get(0).get("id");
                nodeStatus.put(firstNodeId, "RUNNING");
                instance.setCurrentNodeId(firstNodeId);
            }
        }
        instance.setNodeStatus(nodeStatus);
        instanceRepository.save(instance);
        return instance;
    }

    public ProcessInstance getById(String id) {
        return instanceRepository.findById(id);
    }

    public List<ProcessInstance> listAll() {
        return instanceRepository.findAll();
    }
}
