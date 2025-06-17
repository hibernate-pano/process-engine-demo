import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProcessFlowEditor from '../ProcessFlowEditor.vue'
import * as processApi from '../../api/processApi'
import axios from 'axios'

// Mock API calls
vi.mock('../../api/processApi', () => ({
  getProcessDefinitions: vi.fn(),
  getProcessDefinition: vi.fn(),
  saveProcessDefinition: vi.fn(),
  deleteProcessDefinition: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('ProcessFlowEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    processApi.getProcessDefinitions.mockResolvedValue({ data: [] });
    axios.default.get.mockResolvedValue({ data: [] });
    axios.default.post.mockResolvedValue({ data: {} });
  });

  it('renders correctly', () => {
    const wrapper = mount(ProcessFlowEditor);
    expect(wrapper.exists()).toBe(true);
    expect(wrapper.find('.process-flow-editor').exists()).toBe(true);
  });

  it('can add a node', async () => {
    const wrapper = mount(ProcessFlowEditor);
    await wrapper.find('button').trigger('click'); // Click add node button
    expect(wrapper.vm.nodes.length).toBe(2); // Initial node + new node
    expect(wrapper.vm.nodes[1].label).toMatch(/节点/);
  });

  it('switches between editor and monitor tabs', async () => {
    const wrapper = mount(ProcessFlowEditor);
    await wrapper.findAll('.tab-bar button')[1].trigger('click');
    expect(wrapper.vm.activeTab).toBe('monitor');
    expect(wrapper.find('.monitor-panel').exists()).toBe(true);

    await wrapper.findAll('.tab-bar button')[0].trigger('click');
    expect(wrapper.vm.activeTab).toBe('editor');
    expect(wrapper.find('.flow-canvas').exists()).toBe(true);
  });

  it('loads process definitions', async () => {
    const mockDefs = [{ id: '1', name: 'Def A' }];
    processApi.getProcessDefinitions.mockResolvedValue({ data: mockDefs });
    const wrapper = mount(ProcessFlowEditor);

    // Trigger onMounted manually if needed, or ensure component re-mounts
    // For this test, we'll just check if the mock was called.
    await new Promise(resolve => setTimeout(resolve, 0)); // Allow promises to resolve

    expect(processApi.getProcessDefinitions).toHaveBeenCalled();
    expect(wrapper.vm.processList).toEqual(mockDefs);
  });

  it('selects an instance and fetches history', async () => {
    const mockInstance = { id: 'inst1', status: 'RUNNING', processDefinitionId: 'def1', currentNodeId: 'node1' };
    const mockHistory = [
      { nodeId: 'node1', status: 'RUNNING', startTime: new Date().toISOString() }
    ];

    axios.default.get.mockImplementation((url) => {
      if (url === '/api/process-instances') return Promise.resolve({ data: [mockInstance] });
      if (url === '/api/process-instances/inst1/history') return Promise.resolve({ data: mockHistory });
      if (url === '/api/process-instances/inst1') return Promise.resolve({ data: mockInstance });
      return Promise.reject(new Error('not found'));
    });

    const wrapper = mount(ProcessFlowEditor);
    await wrapper.findAll('.tab-bar button')[1].trigger('click'); // Switch to monitor
    
    // Wait for instance list to load
    await new Promise(resolve => setTimeout(resolve, 0));
    
    await wrapper.find('select').setValue('inst1');

    expect(wrapper.vm.selectedInstanceId).toBe('inst1');
    expect(axios.default.get).toHaveBeenCalledWith('/api/process-instances/inst1/history');
    expect(wrapper.vm.instanceHistory).toEqual(mockHistory);
    expect(wrapper.vm.currentInstanceStatus).toBe('RUNNING');

    // Verify node highlighting - shallow mount might not render VueFlow nodes properly for CSS classes
    // For this, we'll check the vm.nodes data directly.
    expect(wrapper.vm.nodes.find(n => n.id === 'node1').class).toBe('highlighted-node');
  });

  it('stepInstance calls replay API and refreshes data', async () => {
    const mockInstance = { id: 'inst1', status: 'RUNNING', processDefinitionId: 'def1', currentNodeId: 'node1' };
    const mockHistory = [
      { nodeId: 'node1', status: 'COMPLETED', startTime: '2023-01-01T10:00:00Z', endTime: '2023-01-01T10:01:00Z', output: 'Node 1 processed.' },
      { nodeId: 'node2', status: 'RUNNING', startTime: '2023-01-01T10:01:01Z' }
    ];
    
    axios.default.get.mockImplementation((url) => {
        if (url === '/api/process-instances') return Promise.resolve({ data: [mockInstance] });
        if (url === '/api/process-instances/inst1/history') return Promise.resolve({ data: mockHistory });
        if (url === '/api/process-instances/inst1') return Promise.resolve({ data: { ...mockInstance, currentNodeId: 'node2' } });
        return Promise.reject(new Error('not found'));
    });
    axios.default.post.mockResolvedValueOnce({ data: 'Step to node node2' });

    const wrapper = mount(ProcessFlowEditor);
    await wrapper.vm.selectedInstanceId = 'inst1'; // Manually set for testing
    await wrapper.vm.currentInstanceStatus = 'RUNNING';
    await wrapper.find('.tab-bar button:nth-child(2)').trigger('click'); // Switch to monitor tab
    await new Promise(resolve => setTimeout(resolve, 0)); // Wait for data to load

    await wrapper.find('button', { text: '单步执行' }).trigger('click');
    
    expect(axios.default.post).toHaveBeenCalledWith('/api/process-instances/inst1/replay', { action: 'step' });
    expect(axios.default.get).toHaveBeenCalledWith('/api/process-instances/inst1/history');
    expect(axios.default.get).toHaveBeenCalledWith('/api/process-instances/inst1'); // Called twice
    expect(wrapper.vm.instanceHistory).toEqual(mockHistory);
    expect(wrapper.vm.nodes.find(n => n.id === 'node2').class).toBe('highlighted-node');
  });

  it('startReplay, pauseReplay, resumeReplay calls replay API and manages interval', async () => {
    const mockInstance = { id: 'inst1', status: 'RUNNING', processDefinitionId: 'def1', currentNodeId: 'node1' };
    axios.default.get.mockResolvedValue({ data: [mockInstance] });
    axios.default.post.mockResolvedValue({ data: 'OK' });

    const wrapper = mount(ProcessFlowEditor);
    await wrapper.vm.selectedInstanceId = 'inst1';
    await wrapper.vm.currentInstanceStatus = 'COMPLETED';
    await wrapper.find('.tab-bar button:nth-child(2)').trigger('click'); // Switch to monitor tab
    await new Promise(resolve => setTimeout(resolve, 0)); // Wait for data to load
    
    // Start replay
    await wrapper.find('button', { text: '启动回放' }).trigger('click');
    expect(axios.default.post).toHaveBeenCalledWith('/api/process-instances/inst1/replay', { action: 'start' });
    expect(wrapper.vm.isPlaying).toBe(true);
    // Manually advance time for interval test
    vi.advanceTimersByTime(1000);
    expect(wrapper.vm.replayTime).toBeGreaterThan(0);

    // Pause replay
    axios.default.post.mockResolvedValueOnce({ data: 'OK' });
    await wrapper.find('button', { text: '暂停回放' }).trigger('click');
    expect(axios.default.post).toHaveBeenCalledWith('/api/process-instances/inst1/replay', { action: 'pause' });
    expect(wrapper.vm.isPlaying).toBe(false);
    const pausedTime = wrapper.vm.replayTime;
    vi.advanceTimersByTime(1000); // Should not advance
    expect(wrapper.vm.replayTime).toBe(pausedTime);

    // Resume replay
    axios.default.post.mockResolvedValueOnce({ data: 'OK' });
    await wrapper.find('button', { text: '恢复回放' }).trigger('click');
    expect(axios.default.post).toHaveBeenCalledWith('/api/process-instances/inst1/replay', { action: 'resume' });
    expect(wrapper.vm.isPlaying).toBe(true);
    vi.advanceTimersByTime(1000);
    expect(wrapper.vm.replayTime).toBeGreaterThan(pausedTime);
  });
}); 