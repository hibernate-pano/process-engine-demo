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
    <VueFlow v-model:nodes="nodes" v-model:edges="edges" class="flow-canvas" @nodeDoubleClick="onNodeDblClick" />
    <NodePropertyDialog
      v-if="propertyDialog.visible"
      :visible="propertyDialog.visible"
      :node="propertyDialog.node"
      @save="onPropertySave"
      @cancel="onPropertyCancel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { VueFlow } from '@vue-flow/core'
import NodePropertyDialog from './NodePropertyDialog.vue'
import {
  getProcessDefinitions,
  getProcessDefinition,
  saveProcessDefinition,
  deleteProcessDefinition
} from '../api/processApi'
import '../components/styles/flow-theme.css'

const nodes = ref([
  { id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } }
])
const edges = ref([])
const fileInput = ref(null)
const processName = ref('')
const processList = ref([])
const selectedProcessId = ref('')

const propertyDialog = ref({
  visible: false,
  node: {}
})

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
  if (node.type === 'input' || node.type === 'output') return
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

  const data = {
    id: selectedProcessId.value || Date.now().toString(),
    name: processName.value,
    nodes: nodes.value,
    edges: edges.value
  }
  await saveProcessDefinition(data)
  alert('保存成功')
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

onMounted(() => {
  loadProcessList()
})
</script>

<style scoped>
.process-flow-editor {
  width: 100vw;
  height: 90vh;
  display: flex;
  flex-direction: column;
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
</style>
