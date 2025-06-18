<template>
  <div class="ellipse-node">
    <svg width="120" height="60">
      <ellipse cx="60" cy="30" rx="56" ry="26" fill="#e6f9ec" stroke="#222" stroke-width="2" />
      <text x="60" y="36" text-anchor="middle" font-size="20" font-weight="bold" fill="#222">{{ label }}</text>
    </svg>
    <Handle type="source" :position="Position.Bottom" :id="'out'" class="node-handle" />
  </div>
</template>

<script setup>
import { defineProps } from 'vue';
import { Handle, Position } from '@vue-flow/core';
defineProps({ label: String });
</script>

<style scoped>
.ellipse-node {
  width: 120px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.node-handle {
  position: absolute;
  left: 50%;
  bottom: -10px;
  transform: translateX(-50%);
  z-index: 2;
}
</style> 