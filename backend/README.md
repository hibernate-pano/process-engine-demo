# 设备流程引擎后端（Spring Boot）

## 项目简介
本项目为设备处置流程的轻量级流程引擎后端，支持流程的可视化编辑（前端 vue-flow），流程定义与实例均采用 JSON 格式，后端基于 Spring Boot 3 + JDK 17 实现。

## 主要功能
- 流程定义的增删改查（JSON 格式，兼容 vue-flow）
- 流程实例的启动与状态跟踪
- 简单内存存储，便于后续扩展数据库

## 目录结构
```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/processengine/
│   │   │   ├── controller/         # 控制器（API接口）
│   │   │   ├── model/              # 实体类
│   │   │   ├── repository/         # 内存仓库
│   │   │   └── service/            # 业务逻辑
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── README.md
```

## 启动方式
1. JDK 17 环境下，进入 backend 目录：
2. 执行 `mvn spring-boot:run` 或 `mvn clean package` 后运行 jar 包。

## API 说明
### 流程定义
- `GET    /api/process-definitions`      获取所有流程定义
- `POST   /api/process-definitions`      新建/保存流程定义（JSON）
- `GET    /api/process-definitions/{id}` 获取单个流程定义
- `DELETE /api/process-definitions/{id}` 删除流程定义

### 流程实例
- `POST   /api/process-instances`        启动流程实例（参数：processDefinitionId）
- `GET    /api/process-instances/{id}`   查询流程实例状态
- `GET    /api/process-instances`        查询所有流程实例

## 数据结构示例
### 流程定义（ProcessDefinition）
```json
{
  "id": "process-001",
  "name": "设备A处置流程",
  "deviceType": "A",
  "nodes": [ ...vue-flow 节点... ],
  "edges": [ ...vue-flow 连线... ]
}
```

### 流程实例（ProcessInstance）
```json
{
  "id": "instance-001",
  "processDefinitionId": "process-001",
  "currentNodeId": "A1",
  "status": "RUNNING",
  "nodeStatus": {
    "A1": "RUNNING",
    "A2": "PENDING"
  }
}
```

## 后续可扩展
- 支持数据库持久化
- 支持流程分支、条件、并行等高级特性
- 步骤自动化执行与回调
- 权限与多用户协作

---
如需前端对接或详细用法，请参考前端项目说明。
