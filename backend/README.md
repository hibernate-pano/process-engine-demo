# 设备流程引擎后端（Spring Boot）

## 项目简介
本项目是"无人设备集群控制平台"的**设备流程控制引擎**的后端实现。它支持流程的可视化设计（与前端 `vue-flow` 配合）、流程定义与实例的管理、以及**基于输入/输出和条件判断的流程模拟执行与回放**。后端基于 Spring Boot 3、JDK 17 和 Maven 构建，并已集成 **MySQL 数据库**用于数据持久化，同时具备全面的**自动化测试**。

## 主要功能
- **流程定义管理**：支持流程定义的增删改查（JSON 格式，兼容 `vue-flow`）。
- **流程实例生命周期管理**：支持流程实例的启动、状态跟踪，并提供丰富的回放控制（启动、暂停、恢复、单步执行）。
- **智能流程执行模拟**：
    - **节点输入/输出**：模拟节点执行时的输入和输出传递，并记录到历史中。
    - **条件节点**：能够根据自定义的条件表达式（如 `input === 'success'`）和当前输入，智能选择正确的流程分支（通过出边标签）。
    - **动作节点**：根据配置的设备类型和动作模拟生成执行结果。
- **数据持久化**：所有流程定义和实例数据均持久化存储于 **MySQL 数据库**。
- **自动化测试**：包含对服务层和控制器层的单元测试与集成测试，保障后端逻辑的健壮性。

## 目录结构
```
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/processengine/
│   │   │   ├── controller/         # 控制器（API接口）
│   │   │   ├── model/              # 实体类
│   │   │   ├── repository/         # 数据访问层
│   │   │   └── service/            # 业务逻辑
│   │   └── resources/
│   │       └── application.properties # 配置文件
│   └── test/
│       └── java/com/example/processengine/
│           ├── controller/         # 控制器层测试
│           └── service/            # 服务层测试
└── README.md
```

## 技术栈
- **后端**：Spring Boot 3、JDK 17、Maven、MySQL
- **测试**：JUnit 5、Mockito、Spring Boot Test

## 快速启动
1.  **MySQL 数据库准备**：
    请确保你有一个正在运行的 MySQL 8.0+ 数据库实例。创建一个名为 `process_engine_db` 的数据库，并确保你有一个可以连接该数据库的用户（例如，`root` 用户）。
    
    你可以使用 Docker 快速启动一个 MySQL 实例：
    ```bash
    docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=process_engine_db -p 3306:3306 -d mysql/mysql-server:8.0
    ```
    
    后端配置在 `backend/src/main/resources/application.properties` 中，默认配置如下：
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/process_engine_db?useSSL=false&serverTimezone=UTC
    spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
    spring.datasource.username=root
    spring.datasource.password=root
    spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
    spring.jpa.hibernate.ddl-auto=update
    ```
    如果你的 MySQL 配置不同，请修改上述属性。

2.  **启动后端服务**：
    进入 `backend` 目录，然后执行 Maven 命令：
    ```bash
    cd backend
    mvn spring-boot:run
    ```
    或者先打包再运行：
    ```bash
    cd backend
    mvn clean package
    java -jar target/processengine-backend-0.0.1-SNAPSHOT.jar
    ```

## API 说明
### 1. 流程定义 API (`/api/process-definitions`)
-   `GET /`：获取所有流程定义。
-   `POST /`：新建或更新流程定义。请求体为流程定义的 JSON 数据。
-   `GET /{id}`：根据 ID 获取单个流程定义。
-   `DELETE /{id}`：根据 ID 删除流程定义。

### 2. 流程实例 API (`/api/process-instances`)
-   `POST /`：启动流程实例。请求体包含 `processDefinitionId`。
-   `GET /`：获取所有流程实例。
-   `GET /{id}`：根据 ID 获取单个流程实例的详细状态（包括当前节点、节点状态、执行历史等）。
-   `GET /{id}/history`：获取指定流程实例的执行历史记录。
-   `POST /{id}/replay`：控制流程实例的回放。请求体包含 `action` 字段，支持值：
    -   `start`：从头开始回放。
    -   `step`：单步推进回放。
    -   `pause`：暂停回放。
    -   `resume`：恢复回放。

## 数据结构示例
### 流程定义（ProcessDefinition）
```json
{
  "id": "process-001",
  "name": "设备A处置流程",
  "nodes": [ 
    { "id": "node1", "type": "input", "label": "开始" },
    { "id": "node2", "type": "action", "label": "打开设备", "customProps": { "deviceType": "light", "deviceAction": "on" } },
    { "id": "node3", "type": "condition", "label": "检查状态", "condition": "input === \'success\'" },
    { "id": "node4", "type": "output", "label": "结束" }
  ],
  "edges": [ 
    { "source": "node1", "target": "node2", "id": "edge1" },
    { "source": "node2", "target": "node3", "id": "edge2" },
    { "source": "node3", "target": "node4", "id": "edge3", "label": "true" },
    { "source": "node3", "target": "node5", "id": "edge4", "label": "false" }
  ]
}
```

### 流程实例（ProcessInstance）
```json
{
  "id": "instance-001",
  "processDefinitionId": "process-001",
  "currentNodeId": "node2",
  "status": "REPLAY_RUNNING",
  "nodeStatus": {
    "node1": "COMPLETED",
    "node2": "RUNNING",
    "node3": "PENDING"
  },
  "executionHistory": [
    {
      "nodeId": "node1",
      "startTime": "2023-01-01T10:00:00Z",
      "endTime": "2023-01-01T10:00:05Z",
      "status": "COMPLETED",
      "input": "Process started.",
      "output": "Node node1 processed."
    },
    {
      "nodeId": "node2",
      "startTime": "2023-01-01T10:00:06Z",
      "status": "RUNNING",
      "input": "Node node1 processed.",
      "output": null
    }
  ]
}
```

## 自动化测试
在 `backend` 目录下执行 Maven 测试命令：
```bash
mvn test
```

---

## 后续可扩展
- **流程执行引擎的真正集成**：与真实设备或模拟器对接，实现命令下发与状态回调。
- **用户与权限管理**：实现用户认证、角色管理、流程权限控制。
- **流程版本管理**：更完善的版本创建、回滚、比较功能。
- **告警与通知机制**：流程执行异常时的邮件、短信或平台告警。
- **多租户支持**：隔离不同用户或组织的流程数据。
- **API 文档化**：使用 Swagger/OpenAPI 等工具生成详细的 API 文档。

---
如需前端对接或详细用法，请参考前端项目说明。
