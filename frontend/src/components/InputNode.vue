<template>
  <div class="custom-node-card">
    <div class="node-header">{{ label }}</div>
    <div class="node-desc">流程起点/入口</div>
    <div class="animated-bar"></div>
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { Handle, Position } from '@vue-flow/core'; // Import Handle and Position

defineProps({
  label: String,
  data: Object,
  // 继承 Vue Flow 提供的所有节点属性
  id: String,
  type: String,
  position: Object,
  selected: Boolean,
  connectable: Boolean,
  isValidTarget: Boolean,
  isValidSource: Boolean,
  parent: String,
  draggable: Boolean,
  selectable: Boolean,
  focusable: Boolean,
  resizable: Boolean,
  initialized: Boolean,
  dimensions: Object,
  handleBounds: Object,
  computedPosition: Object,
  isParent: Boolean,
});
</script>

<style scoped>
.custom-node-card {
  width: 220px;
  min-height: 90px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  border-radius: 18px;
  box-shadow: 0 6px 24px rgba(60, 180, 120, 0.18);
  padding: 24px 32px 18px 32px;
  text-align: center;
  font-family: 'Fira Mono', monospace;
  font-size: 1.2em;
  color: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 0.18s, box-shadow 0.18s;
  margin-bottom: 12px;
}
.custom-node-card:hover {
  transform: scale(1.06) rotate(-1deg);
  box-shadow: 0 12px 36px rgba(60, 180, 120, 0.28);
}
.node-header {
  font-weight: bold;
  font-size: 1.4em;
  margin-bottom: 6px;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.node-desc {
  font-size: 0.95em;
  opacity: 0.85;
  margin-bottom: 8px;
}
.animated-bar {
  width: 80%;
  height: 6px;
  border-radius: 4px;
  background: linear-gradient(90deg, #43e97b, #38f9d7, #43e97b);
  background-size: 200% 100%;
  animation: bar-move 2.2s linear infinite;
  margin: 0 auto 0 auto;
}
@keyframes bar-move {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}
</style> 