<template>
  <div class="vue-flow__node-input">
    <div class="node-header">{{ label }}</div>
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
.vue-flow__node-input {
  width: 120px;
  background: #c8e6c9; /* Light green background */
  border: 1px solid #4caf50; /* Green border */
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  font-size: 14px;
  color: #333;
  position: relative;
  min-height: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.node-header {
  font-weight: bold;
  color: #2e7d32; /* Darker green for header */
}

/* 移除原有的伪 Handle 样式 */
/*
.vue-flow__handle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #5F5F5F;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  cursor: crosshair;
}

.vue-flow__handle-bottom {
  bottom: -6px;
}
*/
</style> 