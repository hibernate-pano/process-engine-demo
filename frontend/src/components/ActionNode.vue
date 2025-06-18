<template>
  <div class="rect-node">
    <Handle type="target" :position="Position.Top" :id="'in'" class="node-handle node-handle-top" />
    <div class="node-header">{{ label }}</div>
    <div v-if="deviceType || deviceAction" class="node-desc">
      <span v-if="deviceType">设备：{{ deviceType }} </span>
      <span v-if="deviceAction">动作：{{ deviceAction }}</span>
    </div>
    <Handle type="source" :position="Position.Bottom" :id="'out'" class="node-handle node-handle-bottom" />
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { Handle, Position } from '@vue-flow/core';
defineProps({
  label: String,
  deviceType: String,
  deviceAction: String
});
</script>

<style scoped>
.rect-node {
  width: 180px;
  min-height: 60px;
  background: #fff;
  border: 2px solid #222;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  position: relative;
}
.node-header {
  font-size: 18px;
  font-weight: bold;
  color: #222;
  margin-bottom: 2px;
}
.node-desc {
  font-size: 13px;
  color: #888;
}
.node-handle {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}
.node-handle-top {
  top: -10px;
}
.node-handle-bottom {
  bottom: -10px;
}
</style> 