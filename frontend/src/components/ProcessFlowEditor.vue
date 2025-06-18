<template>
  <div class="process-flow-editor">
    <div class="toolbar">
      <input v-model="processName" placeholder="流程名称" class="input" />
      <button @click="addNode">添加节点</button>
      <button @click="saveToBackend">保存到后端</button>
      <button @click="loadFromBackend">从后端加载</button>
      <button @click="saveFlow">导出JSON</button>
      <button @click="loadFlow">导入JSON</button>
      <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
      <select v-model="selectedProcessId" @change="onProcessSelect">
        <option value="">选择已有流程</option>
        <option v-for="p in processList" :key="p.id" :value="p.id">{{ p.name }}</option>
      </select>
      <button v-if="selectedProcessId" @click="deleteProcess">删除流程</button>
    </div>
    <div class="tab-bar">
      <button :class="{active: activeTab==='editor'}" @click="activeTab='editor'">流程编辑</button>
      <button :class="{active: activeTab==='monitor'}" @click="activeTab='monitor'">实例监控</button>
    </div>
    <div v-if="activeTab==='editor'">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        class="flow-canvas"
        :fit-view="true"
        :zoom-on-scroll="true"
        :pan-on-drag="true"
        :min-zoom="0.3"
        :max-zoom="2.2"
        :background-color="'#181c23'"
        :background-gap="32"
        :background-size="1"
        @nodeDoubleClick="onNodeDblClick"
        @connect="onConnect"
      >
        <template #node-input="nodeProps">
          <InputNode v-bind="nodeProps" />
        </template>
        <template #node-action="nodeProps">
          <ActionNode v-bind="nodeProps" />
        </template>
        <template #node-condition="nodeProps">
          <ConditionNode v-bind="nodeProps" />
        </template>
        <!-- SVG 动态渐变定义 -->
        <svg width="0" height="0">
          <defs>
            <linearGradient id="animated-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#43e97b">
                <animate attributeName="stop-color" values="#43e97b;#38f9d7;#43e97b" dur="2s" repeatCount="indefinite" />
              </stop>
              <stop offset="100%" stop-color="#38f9d7">
                <animate attributeName="stop-color" values="#38f9d7;#43e97b;#38f9d7" dur="2s" repeatCount="indefinite" />
              </stop>
            </linearGradient>
          </defs>
        </svg>
      </VueFlow>
      <NodePropertyDialog
        v-if="propertyDialog.visible"
        :visible="propertyDialog.visible"
        :node="propertyDialog.node"
        @save="onPropertySave"
        @cancel="onPropertyCancel"
      />
    </div>
    <div v-else class="monitor-panel">
      <h3>流程实例监控</h3>
      <select v-model="selectedInstanceId" @change="onInstanceSelect">
        <option value="">选择实例</option>
        <option v-for="inst in instanceList" :key="inst.id" :value="inst.id">{{ inst.id }} ({{ inst.status }})</option>
      </select>
      <button v-if="selectedInstanceId && (currentInstanceStatus === 'COMPLETED' || currentInstanceStatus === 'FAILED' || currentInstanceStatus === 'RUNNING')" @click="startReplay">启动回放</button>
      <button v-if="selectedInstanceId && currentInstanceStatus === 'REPLAY_RUNNING'" @click="pauseReplay">暂停回放</button>
      <button v-if="selectedInstanceId && currentInstanceStatus === 'REPLAY_PAUSED'" @click="resumeReplay">恢复回放</button>
      <button v-if="selectedInstanceId && (currentInstanceStatus === 'REPLAY_RUNNING' || currentInstanceStatus === 'RUNNING')" @click="stepInstance">单步执行</button>
      <div v-if="instanceHistory && instanceHistory.length">
        <ul>
          <li v-for="record in instanceHistory" :key="record.nodeId">
            节点: {{ record.nodeId }} | 状态: {{ record.status }} | 开始: {{ record.startTime }} | 结束: {{ record.endTime }}
          </li>
        </ul>
        <div class="replay-controls">
          <label>回放进度:</label>
          <input
            type="range"
            v-model="replayTime"
            :min="replayTimeRange.min"
            :max="replayTimeRange.max"
            step="1"
            @input="onReplayTimeChange"
          />
          <span>{{ formatTime(replayTime) }}</span>
        </div>
      </div>
      <div v-else>请选择实例查看历史</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import NodePropertyDialog from './NodePropertyDialog.vue'
import InputNode from './InputNode.vue'
import ActionNode from './ActionNode.vue'
import ConditionNode from './ConditionNode.vue'
import {
  getProcessDefinitions,
  getProcessDefinition,
  saveProcessDefinition,
  deleteProcessDefinition
} from '../api/processApi'
import '../components/styles/flow-theme.css'
import axios from 'axios'

const nodes = ref([
  { id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } }
])
const edges = ref([])
const fileInput = ref(null)
const processName = ref('')
const processList = ref([])
const selectedProcessId = ref('')
const activeTab = ref('editor')
const instanceList = ref([])
const selectedInstanceId = ref('')
const instanceHistory = ref([])
const currentInstanceStatus = ref('')
const replayTime = ref(0)
const replayTimeRange = ref({ min: 0, max: 0 })
const isPlaying = ref(false)
let playbackIntervalId = null

const propertyDialog = ref({
  visible: false,
  node: {}
})

const { addEdges } = useVueFlow()

function addNode() {
  const id = (nodes.value.length + 1).toString()
  const newNode = {
    id,
    label: `节点${id}`,
    position: { x: 100 + nodes.value.length * 80, y: 100 },
  };

  // 随机选择节点类型
  const types = ['action', 'condition'];
  const randomType = types[Math.floor(Math.random() * types.length)];
  newNode.type = randomType;

  if (randomType === 'action') {
    newNode.label = `动作节点${id}`;
    newNode.deviceType = '';
    newNode.deviceAction = '';
  } else if (randomType === 'condition') {
    newNode.label = `判断节点${id}`;
    newNode.condition = '';
  }

  nodes.value.push(newNode);
}

function onNodeDblClick(e) {
  const node = e.node
  propertyDialog.value = {
    visible: true,
    node: { ...node }
  }
}

function onPropertySave(newNode) {
  const idx = nodes.value.findIndex(n => n.id === newNode.id)
  if (idx !== -1) {
    nodes.value[idx] = { ...nodes.value[idx], ...newNode }
  }
  propertyDialog.value.visible = false
  // 保存属性后，自动保存到后端
  saveToBackend()
}
function onPropertyCancel() {
  propertyDialog.value.visible = false
}

function saveFlow() {
  const data = {
    nodes: nodes.value,
    edges: edges.value
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'process-flow.json'
  a.click()
  URL.revokeObjectURL(url)
}

function loadFlow() {
  fileInput.value.click()
}

function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (evt) => {
    try {
      const data = JSON.parse(evt.target.result)
      nodes.value = data.nodes || []
      edges.value = data.edges || []
    } catch (err) {
      alert('流程文件格式错误')
    }
  }
  reader.readAsText(file)
}

async function saveToBackend() {
  if (!processName.value) {
    // 如果流程名称为空，自动生成一个
    const now = new Date();
    const year = now.getFullYear();
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const day = now.getDate().toString().padStart(2, '0');
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const seconds = now.getSeconds().toString().padStart(2, '0');
    processName.value = `流程定义-${year}${month}${day}-${hours}${minutes}${seconds}`;
  }

  // If no nodes exist, add a default 'start' node before saving
  if (nodes.value.length === 0) {
    nodes.value.push({ id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } });
  }

  const data = {
    id: selectedProcessId.value || Date.now().toString(),
    name: processName.value,
    nodes: nodes.value,
    edges: edges.value
  }
  const { data: savedProcess } = await saveProcessDefinition(data)
  alert('保存成功')

  // 如果是新创建的流程，更新 selectedProcessId
  if (!selectedProcessId.value) {
    selectedProcessId.value = savedProcess.id;
  }
  // 重新加载当前流程数据，确保页面显示最新状态
  await loadFromBackend();
  // 更新流程列表
  await loadProcessList()
}

async function loadFromBackend() {
  if (!selectedProcessId.value) {
    alert('请选择流程')
    return
  }
  const { data } = await getProcessDefinition(selectedProcessId.value)
  processName.value = data.name
  nodes.value = data.nodes || []
  edges.value = data.edges || []

  // Ensure there's always a start node if loading an empty process
  if (nodes.value.length === 0) {
    nodes.value.push({ id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } });
  }
}

async function loadProcessList() {
  const { data } = await getProcessDefinitions()
  processList.value = data
}

async function onProcessSelect() {
  if (!selectedProcessId.value) return
  await loadFromBackend()
}

async function deleteProcess() {
  if (!selectedProcessId.value) return
  await deleteProcessDefinition(selectedProcessId.value)
  alert('删除成功')
  selectedProcessId.value = ''
  processName.value = ''
  nodes.value = []
  edges.value = []
  await loadProcessList()
}

function onConnect(params) {
  addEdges([{ ...params, type: 'default', animated: true }])
}

async function loadInstanceList() {
  const { data } = await axios.get('/api/process-instances')
  instanceList.value = data
}

async function onInstanceSelect() {
  if (!selectedInstanceId.value) {
    instanceHistory.value = []
    return
  }
  clearInterval(playbackIntervalId)
  isPlaying.value = false
  const { data } = await axios.get(`/api/process-instances/${selectedInstanceId.value}/history`)
  instanceHistory.value = data
  await loadInstanceList()
  const { data: currentInstance } = await axios.get(`/api/process-instances/${selectedInstanceId.value}`)
  if (currentInstance && currentInstance.currentNodeId) {
    currentInstanceStatus.value = currentInstance.status
    nodes.value = nodes.value.map(node => {
      if (node.id === currentInstance.currentNodeId) {
        return { ...node, class: 'highlighted-node' }
      } else {
        const { class: _, ...rest } = node
        return rest
      }
    })
  }
}

async function stepInstance() {
  if (!selectedInstanceId.value) return
  clearInterval(playbackIntervalId)
  isPlaying.value = false
  await axios.post(`/api/process-instances/${selectedInstanceId.value}/replay`, { action: 'step' })
  await onInstanceSelect()
}

async function startReplay() {
  if (!selectedInstanceId.value) return
  clearInterval(playbackIntervalId)
  await axios.post(`/api/process-instances/${selectedInstanceId.value}/replay`, { action: 'start' })
  await onInstanceSelect()
  isPlaying.value = true
  playbackIntervalId = setInterval(() => {
    if (replayTime.value < replayTimeRange.value.max) {
      replayTime.value += 1000;
    } else {
      clearInterval(playbackIntervalId);
      isPlaying.value = false;
      alert('回放结束');
    }
  }, 1000);
}

async function pauseReplay() {
  if (!selectedInstanceId.value) return
  clearInterval(playbackIntervalId)
  isPlaying.value = false
  await axios.post(`/api/process-instances/${selectedInstanceId.value}/replay`, { action: 'pause' })
  await onInstanceSelect()
}

async function resumeReplay() {
  if (!selectedInstanceId.value) return
  if (!isPlaying.value && playbackIntervalId) {
    await axios.post(`/api/process-instances/${selectedInstanceId.value}/replay`, { action: 'resume' })
    await onInstanceSelect()
    isPlaying.value = true
    playbackIntervalId = setInterval(() => {
      if (replayTime.value < replayTimeRange.value.max) {
        replayTime.value += 1000;
      } else {
        clearInterval(playbackIntervalId);
        isPlaying.value = false;
        alert('回放结束');
      }
    }, 1000);
  }
}

function formatTime(timestamp) {
  if (!timestamp) return 'N/A'
  const date = new Date(timestamp)
  return date.toLocaleString()
}

function onReplayTimeChange() {
  updateNodeHighlightByTime(replayTime.value)
}

function updateNodeHighlightByTime(time) {
  if (!instanceHistory.value || instanceHistory.value.length === 0) {
    nodes.value = nodes.value.map(node => { const { class: _, ...rest } = node; return rest })
    return
  }

  const activeNodesAtTime = new Set()
  let currentNodeIdAtTime = null

  for (const record of instanceHistory.value) {
    const recordStartTime = new Date(record.startTime).getTime()
    const recordEndTime = record.endTime ? new Date(record.endTime).getTime() : Date.now()

    if (time >= recordStartTime && time <= recordEndTime) {
      activeNodesAtTime.add(record.nodeId)
      if (record.status === 'RUNNING' || record.status === 'REPLAY_RUNNING' && record.endTime === null) {
        currentNodeIdAtTime = record.nodeId
      }
    }
  }

  nodes.value = nodes.value.map(node => {
    const newNode = { ...node }
    let nodeClass = ''

    if (node.id === currentNodeIdAtTime) {
      nodeClass = 'highlighted-node'
    } else if (activeNodesAtTime.has(node.id)) {
      nodeClass = 'completed-node' 
    } else {
      const { class: _, ...rest } = node
      return rest
    }

    if (nodeClass) {
      newNode.class = nodeClass
    } else {
      delete newNode.class
    }
    return newNode
  })
}

watch(instanceHistory, (newHistory) => {
  if (newHistory.length > 0) {
    const firstTime = new Date(newHistory[0].startTime).getTime()
    const lastRecord = newHistory[newHistory.length - 1]
    const lastTime = new Date(lastRecord.endTime || lastRecord.startTime).getTime()
    replayTimeRange.value = { min: firstTime, max: lastTime }
    replayTime.value = lastTime
  } else {
    replayTimeRange.value = { min: 0, max: 0 }
    replayTime.value = 0
  }
  updateNodeHighlightByTime(replayTime.value)
}, { deep: true })

onMounted(() => {
  // 保证初始化时至少有一个"开始"节点
  if (!nodes.value || nodes.value.length === 0) {
    nodes.value = [{ id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } }]
  }
  loadProcessList()
  loadInstanceList()
})
</script>

<style scoped>
.process-flow-editor {
  width: 100vw;
  height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: visible;
}
.toolbar {
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(60,60,60,0.06);
  padding: 12px 18px;
}
.input {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 15px;
  outline: none;
  transition: border 0.2s;
}
.input:focus {
  border: 1.5px solid #1976d2;
}
button {
  background: #f5f6fa;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s, border 0.2s;
}
button:hover {
  background: #e3eafc;
  border: 1.5px solid #1976d2;
}
select {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 15px;
}
.tab-bar {
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(60,60,60,0.06);
  padding: 12px 18px;
}
.monitor-panel {
  flex: 1;
  padding: 18px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(60,60,60,0.06);
}
.replay-controls {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 10px;
}
.replay-controls input[type="range"] {
  flex-grow: 1;
}
.replay-controls span {
  font-size: 14px;
  color: #555;
}

.vue-flow__node.completed-node {
  border: 1.5px solid #4caf50;
  box-shadow: 0 2px 8px rgba(76,175,80,0.08);
  background: #e8f5e9;
}

.flow-canvas {
  min-height: 500px;
  overflow: visible !important;
}
</style>
