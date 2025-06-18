<template>
  <div class="diamond-node">
    <svg width="100" height="100">
      <polygon points="50,10 90,50 50,90 10,50" fill="#fff" stroke="#222" stroke-width="2" />
      <text x="50" y="48" text-anchor="middle" font-size="16" font-weight="bold" fill="#ff7043">判断节点</text>
    </svg>
    <div class="diamond-label">
      <span v-if="condition">{{ condition }}</span>
    </div>
    <Handle type="target" :position="Position.Top" :id="'in'" class="node-handle node-handle-top" />
    <Handle type="source" :position="Position.Left" :id="'if'" class="node-handle node-handle-left" />
    <Handle type="source" :position="Position.Right" :id="'else'" class="node-handle node-handle-right" />
    <Handle type="source" :position="Position.Bottom" :id="'default'" class="node-handle node-handle-bottom" />
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { Handle, Position } from '@vue-flow/core';

defineProps({
  label: String,
  condition: String
});
</script>

<style scoped>
.diamond-node {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-direction: column;
}
.diamond-label {
  position: absolute;
  top: 60px;
  left: 0;
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: #888;
}
.node-handle {
  position: absolute;
  z-index: 2;
}
.node-handle-top {
  left: 50%;
  top: -10px;
  transform: translateX(-50%);
}
.node-handle-bottom {
  left: 50%;
  bottom: -10px;
  transform: translateX(-50%);
}
.node-handle-left {
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
}
.node-handle-right {
  right: -10px;
  top: 50%;
  transform: translateY(-50%);
}
</style> 