<template>
  <div v-if="visible" class="dialog-mask">
    <div class="dialog">
      <h3>节点属性配置</h3>
      <div class="form-group">
        <label>节点类型</label>
        <select v-model="localNode.type">
          <option value="action">动作节点</option>
          <option value="condition">判断节点</option>
        </select>
      </div>
      <div class="form-group">
        <label>节点名称</label>
        <input v-model="localNode.label" placeholder="请输入节点名称" />
      </div>
      <div v-if="localNode.type === 'action'">
        <div class="form-group">
          <label>设备类型</label>
          <input v-model="localNode.deviceType" placeholder="如：无人车" />
        </div>
        <div class="form-group">
          <label>设备动作</label>
          <input v-model="localNode.deviceAction" placeholder="如：启动、停止" />
        </div>
      </div>
      <div v-if="localNode.type === 'condition'">
        <div class="form-group">
          <label>条件表达式</label>
          <input v-model="localNode.condition" placeholder="如：battery > 20%" />
        </div>
      </div>
      <div v-if="localNode.customProps && localNode.customProps.length">
        <div v-for="(prop, idx) in localNode.customProps" :key="idx" class="custom-property-item">
          <label>属性名</label>
          <input v-model="prop.name" placeholder="自定义属性名" />
          <label>属性值</label>
          <input v-model="prop.value" placeholder="属性值" />
          <button class="remove-btn" @click="removeCustomProp(idx)">删除</button>
        </div>
      </div>
      <div class="new-custom-property">
        <input v-model="newPropName" class="new-property-name" placeholder="新属性名" />
        <input v-model="newPropValue" class="new-property-value" placeholder="新属性值" />
        <button class="add-btn" @click="addCustomProp">添加</button>
      </div>
      <div class="dialog-actions">
        <button @click="onSave">保存</button>
        <button @click="onCancel">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, toRefs, ref } from 'vue'
const props = defineProps({
  visible: Boolean,
  node: Object
})
const emits = defineEmits(['save', 'cancel'])
const localNode = reactive({ ...props.node })
const newPropName = ref('')
const newPropValue = ref('')

watch(() => props.node, (val) => {
  Object.assign(localNode, val)
})

function onSave() {
  emits('save', { ...localNode })
}
function onCancel() {
  emits('cancel')
}

if (!localNode.customProps) localNode.customProps = []
function addCustomProp() {
  if (newPropName.value) {
    localNode.customProps.push({ name: newPropName.value, value: newPropValue.value })
    newPropName.value = ''
    newPropValue.value = ''
  }
}
function removeCustomProp(idx) {
  localNode.customProps.splice(idx, 1)
}
</script>

<style scoped>
.dialog-mask {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 24px rgba(60,60,60,0.18);
  padding: 28px 32px 18px 32px;
  min-width: 320px;
}
h3 {
  margin: 0 0 18px 0;
  font-size: 20px;
  color: #1976d2;
}
.form-group {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}
label {
  font-size: 14px;
  color: #444;
  margin-bottom: 4px;
}
input, select {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 15px;
  outline: none;
  transition: border 0.2s;
}
input:focus, select:focus {
  border: 1.5px solid #1976d2;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}
button {
  background: #f5f6fa;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s, border 0.2s;
}
button:hover {
  background: #e3eafc;
  border: 1.5px solid #1976d2;
}
.custom-property-item {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}
.custom-property-item label {
  width: 80px; /* 固定标签宽度 */
  flex-shrink: 0; /* 防止标签缩小 */
  margin-bottom: 0;
}
.custom-property-item input {
  flex-grow: 1;
}
.custom-property-item .remove-btn {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}
.custom-property-item .remove-btn:hover {
  background: #ff7875;
}
.new-custom-property {
  flex-direction: row;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.new-custom-property .new-property-name,
.new-custom-property .new-property-value {
  flex-grow: 1;
}
.new-custom-property .add-btn {
  background: #1976d2;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
}
.new-custom-property .add-btn:hover {
  background: #1565c0;
}
</style>
