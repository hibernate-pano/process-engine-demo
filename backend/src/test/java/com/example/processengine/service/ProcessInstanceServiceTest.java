package com.example.processengine.service;

import com.example.processengine.model.ProcessDefinition;
import com.example.processengine.model.ProcessInstance;
import com.example.processengine.repository.ProcessDefinitionRepository;
import com.example.processengine.repository.ProcessInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcessInstanceServiceTest {

    @Mock
    private ProcessInstanceRepository instanceRepository;

    @Mock
    private ProcessDefinitionRepository definitionRepository;

    @Mock
    private ProcessDefinitionService definitionService;

    @InjectMocks
    private ProcessInstanceService processInstanceService;

    private ProcessDefinition sampleProcessDefinition;

    @BeforeEach
    void setUp() {
        sampleProcessDefinition = new ProcessDefinition();
        sampleProcessDefinition.setId("def-123");
        sampleProcessDefinition.setName("Sample Process");
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", "node1");
        node1.put("type", "input");
        nodes.add(node1);
        Map<String, Object> node2 = new HashMap<>();
        node2.put("id", "node2");
        node2.put("type", "action");
        nodes.add(node2);
        sampleProcessDefinition.setNodes(nodes);

        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> edge1 = new HashMap<>();
        edge1.put("source", "node1");
        edge1.put("target", "node2");
        edges.add(edge1);
        sampleProcessDefinition.setEdges(edges);
    }

    @Test
    void startInstance_shouldCreateNewInstanceSuccessfully() {
        // Given
        when(definitionRepository.findById("def-123")).thenReturn(Optional.of(sampleProcessDefinition));
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProcessInstance instance = processInstanceService.startInstance("def-123");

        // Then
        assertNotNull(instance);
        assertNotNull(instance.getId());
        assertEquals("def-123", instance.getProcessDefinitionId());
        assertEquals("RUNNING", instance.getStatus());

        // Verify node statuses
        Map<String, String> nodeStatus = instance.getNodeStatus();
        assertNotNull(nodeStatus);
        assertEquals("RUNNING", nodeStatus.get("node1"));
        assertEquals("PENDING", nodeStatus.get("node2"));

        // Verify execution history
        List<ProcessInstance.NodeExecutionRecord> history = instance.getExecutionHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        ProcessInstance.NodeExecutionRecord firstRecord = history.get(0);
        assertEquals("node1", firstRecord.getNodeId());
        assertEquals("RUNNING", firstRecord.getStatus());
        assertNotNull(firstRecord.getStartTime());
        assertEquals("Process started.", firstRecord.getInput());
        assertNull(firstRecord.getEndTime());
    }

    @Test
    void startInstance_shouldReturnNullIfDefinitionNotFound() {
        // Given
        when(definitionRepository.findById("non-existent-def")).thenReturn(Optional.empty());

        // When
        ProcessInstance instance = processInstanceService.startInstance("non-existent-def");

        // Then
        assertNull(instance);
    }

    @Test
    void startInstance_shouldHandleEmptyNodes() {
        // Given
        sampleProcessDefinition.setNodes(new ArrayList<>());
        when(definitionRepository.findById("def-123")).thenReturn(Optional.of(sampleProcessDefinition));
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProcessInstance instance = processInstanceService.startInstance("def-123");

        // Then
        assertNotNull(instance);
        assertNotNull(instance.getId());
        assertEquals("def-123", instance.getProcessDefinitionId());
        assertEquals("RUNNING", instance.getStatus());
        assertTrue(instance.getNodeStatus().isEmpty());
        assertTrue(instance.getExecutionHistory().isEmpty());
        assertNull(instance.getCurrentNodeId());
    }

    @Test
    void replayInstance_start_shouldInitializeReplay() {
        // Given
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-123");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("COMPLETED"); // Assume a completed instance
        instance.setNodeStatus(new HashMap<>());
        instance.setExecutionHistory(new ArrayList<>());

        when(instanceRepository.findById("inst-123")).thenReturn(Optional.of(instance));
        when(definitionService.getById("def-123")).thenReturn(sampleProcessDefinition);
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-123", "start");

        // Then
        assertEquals("Replay started from beginning.", result);
        assertEquals("REPLAY_RUNNING", instance.getStatus());
        assertEquals("node1", instance.getCurrentNodeId());
        assertEquals("RUNNING", instance.getNodeStatus().get("node1"));
        assertFalse(instance.getExecutionHistory().isEmpty());
        assertEquals("node1", instance.getExecutionHistory().get(0).getNodeId());
        assertEquals("RUNNING", instance.getExecutionHistory().get(0).getStatus());
    }

    @Test
    void replayInstance_step_shouldAdvanceToNextNode() {
        // Given
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-123");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("RUNNING");
        instance.setCurrentNodeId("node1");
        instance.setNodeStatus(new HashMap<>() {
            {
                put("node1", "RUNNING");
                put("node2", "PENDING");
            }
        });
        instance.setExecutionHistory(new ArrayList<>() {
            {
                ProcessInstance.NodeExecutionRecord rec = new ProcessInstance.NodeExecutionRecord();
                rec.setNodeId("node1");
                rec.setStatus("RUNNING");
                rec.setStartTime("2023-01-01T10:00:00Z");
                rec.setInput("Process started.");
                add(rec);
            }
        });

        when(instanceRepository.findById("inst-123")).thenReturn(Optional.of(instance));
        when(definitionService.getById("def-123")).thenReturn(sampleProcessDefinition);
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-123", "step");

        // Then
        assertEquals("Step to node node2", result);
        assertEquals("RUNNING", instance.getStatus()); // Still running
        assertEquals("node2", instance.getCurrentNodeId());

        // Verify node statuses
        assertEquals("COMPLETED", instance.getNodeStatus().get("node1"));
        assertEquals("RUNNING", instance.getNodeStatus().get("node2"));

        // Verify execution history
        assertEquals(2, instance.getExecutionHistory().size());
        ProcessInstance.NodeExecutionRecord completedRecord = instance.getExecutionHistory().get(0);
        assertEquals("node1", completedRecord.getNodeId());
        assertEquals("COMPLETED", completedRecord.getStatus());
        assertNotNull(completedRecord.getEndTime());
        assertNotNull(completedRecord.getOutput()); // Check if output is simulated

        ProcessInstance.NodeExecutionRecord newRecord = instance.getExecutionHistory().get(1);
        assertEquals("node2", newRecord.getNodeId());
        assertEquals("RUNNING", newRecord.getStatus());
        assertNotNull(newRecord.getStartTime());
        assertEquals(completedRecord.getOutput(), newRecord.getInput()); // Input should be previous output
    }

    @Test
    void replayInstance_pause_shouldPauseReplay() {
        // Given
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-123");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("REPLAY_RUNNING");
        instance.setCurrentNodeId("node1");
        instance.setExecutionHistory(new ArrayList<>() {
            {
                ProcessInstance.NodeExecutionRecord rec = new ProcessInstance.NodeExecutionRecord();
                rec.setNodeId("node1");
                rec.setStatus("RUNNING");
                rec.setStartTime("2023-01-01T10:00:00Z");
                add(rec);
            }
        });

        when(instanceRepository.findById("inst-123")).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-123", "pause");

        // Then
        assertEquals("Replay paused.", result);
        assertEquals("REPLAY_PAUSED", instance.getStatus());
        ProcessInstance.NodeExecutionRecord currentRecord = instance.getExecutionHistory().get(0);
        assertEquals("PAUSED", currentRecord.getStatus());
        assertNotNull(currentRecord.getEndTime());
    }

    @Test
    void replayInstance_resume_shouldResumeReplay() {
        // Given
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-123");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("REPLAY_PAUSED");
        instance.setCurrentNodeId("node1");
        instance.setExecutionHistory(new ArrayList<>() {
            {
                ProcessInstance.NodeExecutionRecord rec = new ProcessInstance.NodeExecutionRecord();
                rec.setNodeId("node1");
                rec.setStatus("PAUSED");
                rec.setStartTime("2023-01-01T10:00:00Z");
                rec.setEndTime("2023-01-01T10:00:10Z");
                add(rec);
            }
        });

        when(instanceRepository.findById("inst-123")).thenReturn(Optional.of(instance));
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-123", "resume");

        // Then
        assertEquals("Replay resumed.", result);
        assertEquals("REPLAY_RUNNING", instance.getStatus());
        ProcessInstance.NodeExecutionRecord currentRecord = instance.getExecutionHistory().get(0);
        assertEquals("RUNNING", currentRecord.getStatus());
        assertNotNull(currentRecord.getStartTime()); // Start time should be updated
        assertNull(currentRecord.getEndTime()); // End time should be cleared
    }

    @Test
    void replayInstance_step_shouldHandleConditionNodeTrue() {
        // Given a process with a condition node
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", "node1");
        node1.put("type", "input");
        nodes.add(node1);
        Map<String, Object> node2 = new HashMap<>();
        node2.put("id", "node2");
        node2.put("type", "condition");
        node2.put("condition", "input === 'true'");
        nodes.add(node2);
        Map<String, Object> node3 = new HashMap<>();
        node3.put("id", "node3");
        node3.put("type", "action");
        nodes.add(node3);
        Map<String, Object> node4 = new HashMap<>();
        node4.put("id", "node4");
        node4.put("type", "action");
        nodes.add(node4);

        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> edge1 = new HashMap<>();
        edge1.put("source", "node1");
        edge1.put("target", "node2");
        edges.add(edge1);
        Map<String, Object> edge2 = new HashMap<>();
        edge2.put("source", "node2");
        edge2.put("target", "node3");
        edge2.put("label", "true");
        edges.add(edge2);
        Map<String, Object> edge3 = new HashMap<>();
        edge3.put("source", "node2");
        edge3.put("target", "node4");
        edge3.put("label", "false");
        edges.add(edge3);

        sampleProcessDefinition.setNodes(nodes);
        sampleProcessDefinition.setEdges(edges);

        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-cond-1");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("RUNNING");
        instance.setCurrentNodeId("node2");
        instance.setNodeStatus(new HashMap<>() {
            {
                put("node2", "RUNNING");
            }
        });
        instance.setExecutionHistory(new ArrayList<>() {
            {
                ProcessInstance.NodeExecutionRecord rec = new ProcessInstance.NodeExecutionRecord();
                rec.setNodeId("node2");
                rec.setStatus("RUNNING");
                rec.setStartTime("2023-01-01T10:00:00Z");
                rec.setInput("true");
                add(rec);
            }
        });

        when(instanceRepository.findById("inst-cond-1")).thenReturn(Optional.of(instance));
        when(definitionService.getById("def-123")).thenReturn(sampleProcessDefinition);
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-cond-1", "step");

        // Then
        assertEquals("Step to node node3", result);
        assertEquals("node3", instance.getCurrentNodeId());
        assertEquals("COMPLETED", instance.getNodeStatus().get("node2"));
        assertEquals("RUNNING", instance.getNodeStatus().get("node3"));
    }

    @Test
    void replayInstance_step_shouldHandleConditionNodeFalse() {
        // Given a process with a condition node
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", "node1");
        node1.put("type", "input");
        nodes.add(node1);
        Map<String, Object> node2 = new HashMap<>();
        node2.put("id", "node2");
        node2.put("type", "condition");
        node2.put("condition", "input === 'true'");
        nodes.add(node2);
        Map<String, Object> node3 = new HashMap<>();
        node3.put("id", "node3");
        node3.put("type", "action");
        nodes.add(node3);
        Map<String, Object> node4 = new HashMap<>();
        node4.put("id", "node4");
        node4.put("type", "action");
        nodes.add(node4);

        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, Object> edge1 = new HashMap<>();
        edge1.put("source", "node1");
        edge1.put("target", "node2");
        edges.add(edge1);
        Map<String, Object> edge2 = new HashMap<>();
        edge2.put("source", "node2");
        edge2.put("target", "node3");
        edge2.put("label", "true");
        edges.add(edge2);
        Map<String, Object> edge3 = new HashMap<>();
        edge3.put("source", "node2");
        edge3.put("target", "node4");
        edge3.put("label", "false");
        edges.add(edge3);

        sampleProcessDefinition.setNodes(nodes);
        sampleProcessDefinition.setEdges(edges);

        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-cond-2");
        instance.setProcessDefinitionId("def-123");
        instance.setStatus("RUNNING");
        instance.setCurrentNodeId("node2");
        instance.setNodeStatus(new HashMap<>() {
            {
                put("node2", "RUNNING");
            }
        });
        instance.setExecutionHistory(new ArrayList<>() {
            {
                ProcessInstance.NodeExecutionRecord rec = new ProcessInstance.NodeExecutionRecord();
                rec.setNodeId("node2");
                rec.setStatus("RUNNING");
                rec.setStartTime("2023-01-01T10:00:00Z");
                rec.setInput("false");
                add(rec);
            }
        });

        when(instanceRepository.findById("inst-cond-2")).thenReturn(Optional.of(instance));
        when(definitionService.getById("def-123")).thenReturn(sampleProcessDefinition);
        when(instanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = processInstanceService.replayInstance("inst-cond-2", "step");

        // Then
        assertEquals("Step to node node4", result);
        assertEquals("node4", instance.getCurrentNodeId());
        assertEquals("COMPLETED", instance.getNodeStatus().get("node2"));
        assertEquals("RUNNING", instance.getNodeStatus().get("node4"));
    }
}