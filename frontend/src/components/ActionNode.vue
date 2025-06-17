<template>
  <div class="vue-flow__node-action">
    <Handle type="target" :position="Position.Top" />
    <div class="node-header">动作节点</div>
    <div class="node-body">
      <strong>名称:</strong> {{ label }}<br>
      <span v-if="deviceType"><strong>设备:</strong> {{ deviceType }}<br></span>
      <span v-if="deviceAction"><strong>动作:</strong> {{ deviceAction }}</span>
      <div v-if="customProps && customProps.length > 0" class="custom-props">
        <div v-for="(prop, idx) in customProps" :key="idx">
          <strong>{{ prop.name }}:</strong> {{ prop.value }}
        </div>
      </div>
    </div>
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
  // 自定义属性
  deviceType: String,
  deviceAction: String,
  customProps: Array,
});
</script>

<style scoped>
.vue-flow__node-action {
  width: 150px;
  background: #bbdefb; /* Light blue background */
  border: 1px solid #2196f3; /* Blue border */
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  font-size: 14px;
  color: #333;
  position: relative;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.node-header {
  font-weight: bold;
  margin-bottom: 5px;
  color: #1976d2; /* Darker blue for header */
}

.node-body {
  font-size: 12px;
  word-break: break-word;
}

.custom-props {
  margin-top: 8px;
  padding-top: 5px;
  border-top: 1px dashed #90caf9; /* Lighter blue dashed border */
  text-align: left;
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

.vue-flow__handle-top {
  top: -6px;
}

.vue-flow__handle-bottom {
  bottom: -6px;
}
*/
</style> 