# 设备流程引擎前端（Vue3 + vue-flow）

## 项目简介
本项目为设备处置流程的可视化编辑器，基于 Vue3 + @vue-flow/core 实现，支持流程的拖拽建模、导入导出（JSON），可与后端 Spring Boot 项目对接。

## 主要功能
- 流程节点拖拽、编辑、连线
- 流程保存为 JSON 文件
- 流程从 JSON 文件导入
- 可扩展对接后端 API

## 目录结构
```
frontend/
├── src/
│   ├── components/
│   │   └── ProcessFlowEditor.vue   # 流程编辑器主组件
│   ├── App.vue
│   └── main.js
├── package.json
└── README.md
```

## 启动方式
1. 安装依赖：`npm install`
2. 启动开发：`npm run dev`
3. 访问：`http://localhost:5173`

## 关键依赖
- vue@3.x
- @vue-flow/core
- axios

## 用法说明
- 进入页面后可直接拖拽、编辑流程节点
- 点击“保存流程”可导出当前流程为 JSON 文件
- 点击“加载流程”可导入本地 JSON 流程文件

## 后续可扩展
- 节点属性编辑（如参数、描述）
- 与后端 API 对接，实现流程的保存、加载、实例启动等
- 流程执行监控与状态展示

---
如需后端对接或详细用法，请参考后端项目说明。
