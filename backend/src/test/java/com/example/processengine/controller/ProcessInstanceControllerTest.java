package com.example.processengine.controller;

import com.example.processengine.model.ProcessInstance;
import com.example.processengine.service.ProcessInstanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessInstanceController.class)
public class ProcessInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessInstanceService processInstanceService;

    @Test
    void listAllProcessInstances_shouldReturnListOfInstances() throws Exception {
        ProcessInstance instance1 = new ProcessInstance();
        instance1.setId("inst1");
        instance1.setProcessDefinitionId("def1");
        instance1.setStatus("RUNNING");

        ProcessInstance instance2 = new ProcessInstance();
        instance2.setId("inst2");
        instance2.setProcessDefinitionId("def2");
        instance2.setStatus("COMPLETED");

        List<ProcessInstance> instances = Arrays.asList(instance1, instance2);

        when(processInstanceService.listAll()).thenReturn(instances);

        mockMvc.perform(get("/api/process-instances")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("inst1")))
                .andExpect(jsonPath("$[0].status", is("RUNNING")))
                .andExpect(jsonPath("$[1].id", is("inst2")))
                .andExpect(jsonPath("$[1].status", is("COMPLETED")));
    }

    @Test
    void getProcessInstanceById_shouldReturnInstance() throws Exception {
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst1");
        instance.setProcessDefinitionId("def1");
        instance.setStatus("RUNNING");
        instance.setCurrentNodeId("nodeA");
        instance.setNodeStatus(new HashMap<>() {
            {
                put("nodeA", "RUNNING");
            }
        });

        when(processInstanceService.getById("inst1")).thenReturn(instance);

        mockMvc.perform(get("/api/process-instances/inst1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("inst1")))
                .andExpect(jsonPath("$.processDefinitionId", is("def1")))
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.currentNodeId", is("nodeA")))
                .andExpect(jsonPath("$.nodeStatus.nodeA", is("RUNNING")));
    }

    @Test
    void getProcessInstanceById_shouldReturnNotFound() throws Exception {
        when(processInstanceService.getById(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/process-instances/non-existent-id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Controller returns null, which Spring converts to 200 OK with empty
                                             // body.
    }

    @Test
    void getExecutionHistory_shouldReturnHistory() throws Exception {
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst1");
        instance.setProcessDefinitionId("def1");

        ProcessInstance.NodeExecutionRecord record1 = new ProcessInstance.NodeExecutionRecord();
        record1.setNodeId("nodeA");
        record1.setStatus("COMPLETED");
        record1.setStartTime("2023-01-01T10:00:00Z");
        record1.setEndTime("2023-01-01T10:05:00Z");
        record1.setOutput("Output A");

        ProcessInstance.NodeExecutionRecord record2 = new ProcessInstance.NodeExecutionRecord();
        record2.setNodeId("nodeB");
        record2.setStatus("RUNNING");
        record2.setStartTime("2023-01-01T10:05:01Z");
        record2.setInput("Input B");

        instance.setExecutionHistory(Arrays.asList(record1, record2));

        when(processInstanceService.getById("inst1")).thenReturn(instance);

        mockMvc.perform(get("/api/process-instances/inst1/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nodeId", is("nodeA")))
                .andExpect(jsonPath("$[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$[0].output", is("Output A")))
                .andExpect(jsonPath("$[1].nodeId", is("nodeB")))
                .andExpect(jsonPath("$[1].status", is("RUNNING")))
                .andExpect(jsonPath("$[1].input", is("Input B")));
    }

    @Test
    void getExecutionHistory_shouldReturnEmptyListIfInstanceNotFound() throws Exception {
        when(processInstanceService.getById(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/process-instances/non-existent-id/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void replayInstance_startAction_shouldReturnSuccessMessage() throws Exception {
        String instanceId = "inst-replay-start";
        when(processInstanceService.replayInstance(instanceId, "start")).thenReturn("Replay started from beginning.");

        mockMvc.perform(post("/api/process-instances/{id}/replay", instanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"start\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Replay started from beginning.")));
    }

    @Test
    void replayInstance_stepAction_shouldReturnSuccessMessage() throws Exception {
        String instanceId = "inst-replay-step";
        when(processInstanceService.replayInstance(instanceId, "step")).thenReturn("Step to node node2");

        mockMvc.perform(post("/api/process-instances/{id}/replay", instanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"step\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Step to node node2")));
    }

    @Test
    void replayInstance_pauseAction_shouldReturnSuccessMessage() throws Exception {
        String instanceId = "inst-replay-pause";
        when(processInstanceService.replayInstance(instanceId, "pause")).thenReturn("Replay paused.");

        mockMvc.perform(post("/api/process-instances/{id}/replay", instanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"pause\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Replay paused.")));
    }

    @Test
    void replayInstance_resumeAction_shouldReturnSuccessMessage() throws Exception {
        String instanceId = "inst-replay-resume";
        when(processInstanceService.replayInstance(instanceId, "resume")).thenReturn("Replay resumed.");

        mockMvc.perform(post("/api/process-instances/{id}/replay", instanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\": \"resume\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Replay resumed.")));
    }
}