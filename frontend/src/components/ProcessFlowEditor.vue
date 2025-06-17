<template>
  <div class="process-flow-editor">
    <div class="toolbar">
      <button @click="addNode">添加节点</button>
      <button @click="saveFlow">保存流程</button>
      <button @click="loadFlow">加载流程</button>
      <input type="file" ref="fileInput" style="display:none" @change="onFileChange" />
    </div>
    <VueFlow v-model:nodes="nodes" v-model:edges="edges" class="flow-canvas" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { VueFlow } from '@vue-flow/core'

const nodes = ref([
  { id: '1', type: 'input', label: '开始', position: { x: 100, y: 100 } }
])
const edges = ref([])
const fileInput = ref(null)

function addNode() {
  const id = (nodes.value.length + 1).toString()
  nodes.value.push({
    id,
    label: `步骤${id}`,
    position: { x: 100 + nodes.value.length * 80, y: 100 }
  })
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
}
.flow-canvas {
  flex: 1;
  border: 1px solid #eee;
  background: #fafbfc;
}
</style>
