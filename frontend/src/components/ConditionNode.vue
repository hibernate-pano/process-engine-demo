<template>
  <div class="custom-node-card condition">
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
  // 自定义属性
  condition: String,
  customProps: Array,
});
</script>

<style scoped>
.custom-node-card.condition {
  width: 240px;
  min-height: 110px;
  background: linear-gradient(135deg, #ff7043 0%, #ffa726 100%);
  border-radius: 18px;
  box-shadow: 0 6px 24px rgba(255, 112, 67, 0.18);
  padding: 24px 32px 18px 32px;
  text-align: center;
  font-family: 'Fira Mono', monospace;
  font-size: 1.1em;
  color: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: transform 0.18s, box-shadow 0.18s;
  margin-bottom: 12px;
}
.custom-node-card.condition:hover {
  transform: scale(1.06) rotate(-1deg);
  box-shadow: 0 12px 36px rgba(255, 112, 67, 0.28);
}
.node-header {
  font-weight: bold;
  font-size: 1.3em;
  margin-bottom: 6px;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.node-body {
  font-size: 1em;
  word-break: break-word;
  margin-bottom: 8px;
}
.animated-bar {
  width: 80%;
  height: 6px;
  border-radius: 4px;
  background: linear-gradient(90deg, #ff7043, #ffa726, #ff7043);
  background-size: 200% 100%;
  animation: bar-move 2.2s linear infinite;
  margin: 0 auto 0 auto;
}
@keyframes bar-move {
  0% { background-position: 0% 50%; }
  100% { background-position: 100% 50%; }
}
.custom-props {
  margin-top: 8px;
  padding-top: 5px;
  border-top: 1px dashed #ffe0b2;
  text-align: left;
}
</style> 