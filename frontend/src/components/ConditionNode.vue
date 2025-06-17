<template>
  <div class="vue-flow__node-condition">
    <Handle type="target" :position="Position.Top" />
    <div class="node-header">判断节点</div>
    <div class="node-body">
      <strong>名称:</strong> {{ label }}<br>
      <span v-if="condition"><strong>条件:</strong> {{ condition }}</span>
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
  condition: String,
  customProps: Array,
});
</script>

<style scoped>
.vue-flow__node-condition {
  width: 150px;
  background: #ffccbc; /* Light orange background */
  border: 1px solid #ff7043; /* Orange border */
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
  color: #e64a19; /* Darker orange for header */
}

.node-body {
  font-size: 12px;
  word-break: break-word;
}

.custom-props {
  margin-top: 8px;
  padding-top: 5px;
  border-top: 1px dashed #ffab91; /* Lighter orange dashed border */
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