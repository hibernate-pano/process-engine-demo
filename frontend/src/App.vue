<template>
  <div id="app">
    <h1>设备流程可视化编辑器</h1>
    <ProcessFlowEditor />
  </div>
</template>

<script setup>
import ProcessFlowEditor from './components/ProcessFlowEditor.vue'
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  margin: 0;
  padding: 0;
  background: #f5f6fa;
}
h1 {
  text-align: center;
  margin: 20px 0 10px 0;
}
</style>
