# 设备流程引擎前端（Vue3 + vue-flow）

## 项目简介

本项目是**无人设备集群控制平台**的前端可视化流程编辑器，基于 Vue3 和 `@vue-flow/core` 实现。它支持流程的拖拽建模、自定义节点属性、与后端 API 的数据同步，并提供了**流程实例的可视化监控与回放功能**。前端集成了 `Vitest` 进行自动化测试。

## 主要功能

- **可视化流程设计**：支持拖拽添加节点、节点间连线，所见即所得的流程图编辑。
- **节点属性自定义**：通过弹窗可为节点添加和编辑任意自定义属性（键值对）。
- **流程管理**：支持流程保存到后端、从后端加载、删除流程，以及本地导入导出 JSON 文件。
- **流程实例监控与回放**：
    - 提供"实例监控"Tab，可选择查看流程实例。
    - 支持"启动回放"、"暂停"、"恢复"、"单步执行"等操作，控制流程实例的模拟执行。
    - 流程图节点会根据回放时间轴实时高亮显示当前执行节点和已完成节点。
- **前后端数据同步**：与后端 API 实时交互，确保流程定义和实例数据的一致性。

## 目录结构

```
frontend/
├── src/
│   ├── api/
│   │   └── __tests__/processApi.test.js # API测试
│   │   └── processApi.js              # 后端API接口
│   ├── components/
│   │   ├── __tests__/NodePropertyDialog.test.js # 节点属性弹窗测试
│   │   ├── __tests__/ProcessFlowEditor.test.js  # 流程编辑器测试
│   │   ├── NodePropertyDialog.vue     # 节点属性配置弹窗
│   │   ├── ProcessFlowEditor.vue      # 流程编辑器主组件
│   │   └── styles/flow-theme.css      # 流程图样式
│   ├── App.vue
│   └── main.js
├── package.json
├── vite.config.js
└── README.md
```

## 技术栈

- **前端**：Vue3、@vue-flow/core、Axios、Vite
- **测试**：Vitest、@vue/test-utils

## 快速启动

1.  **安装依赖**：
    进入 `frontend` 目录，执行 npm 命令安装依赖：
    ```bash
    cd frontend
    npm install
    ```
2.  **启动开发服务器**：
    继续在 `frontend` 目录执行：
    ```bash
    npm run dev
    ```
3.  **访问**：
    在浏览器中打开：`http://localhost:5173`

## 自动化测试

本项目已集成 Vitest 进行前端自动化测试。在 `frontend` 目录下执行以下命令运行测试：

```bash
cd frontend
npm test
```

## 关键组件说明

-   `ProcessFlowEditor.vue`：主流程编辑器组件，包含流程图渲染、节点操作、Tab 切换（编辑/监控）和回放控制。
-   `NodePropertyDialog.vue`：节点属性配置弹窗组件，支持编辑节点名称、类型以及动态添加自定义属性。
-   `src/api/processApi.js`：定义了与后端 Spring Boot API 交互的所有接口。

---

如需后端对接或详细用法，请参考后端项目说明和根目录的 `README.md`。
