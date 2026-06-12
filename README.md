# Card Game Server

分布式卡牌游戏服务器，采用微服务架构设计，基于 Netty 实现高性能 TCP 长连接网关，支持海量玩家并发在线。

## 项目概览

```
card-game-server/
├── cd-gateway/       # 网关服务 — 客户端 TCP 接入层（Netty）
├── cd-agent/         # Agent 服务 — 会话管理 + 消息路由中间件
├── cd-author/        # Author 服务 — 账号认证 + 鉴权
├── cd-common/        # 公共模块 — 工具类、gRPC 客户端、etcd 服务发现
├── cd-proto/         # 协议定义 — Protobuf 消息 + gRPC 服务定义
└── pom.xml           # Maven 父项目（Spring Boot 3.2）
```

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.2 | 应用框架 |
| Java | 21 | 开发语言 |
| Netty | 4.1.115 | 高性能 NIO 网络框架（Gateway） |
| gRPC | 1.62.2 | 服务间远程调用 |
| Protobuf | 3.25.3 | 消息序列化 |
| etcd (Jetcd) | — | 服务注册与发现 |
| MySQL 8.0 + JPA | — | 持久化存储 |
| Redis (Lettuce) | — | 缓存与会话 |
| JWT (jjwt) | — | 无状态 Token 鉴权 |

## 项目架构

### 服务架构图

```
                          ┌─────────────────────────────────┐
                          │          etcd 服务注册中心         │
                          └──────────┬──────────────────────┘
                                     │ 服务发现
         ┌───────────────────────────┼───────────────────────────┐
         │                           │                           │
    ┌────▼─────┐              ┌──────▼──────┐           ┌───────▼───────┐
    │  Gateway  │───gRPC─────▶│    Agent     │───gRPC───▶│ 业务服务(待扩展)│
    │ (Netty)   │              │ (会话管理)    │           │               │
    │ 端口 9527 │              │ 端口 8004    │           │               │
    └────┬──────┘              └──────┬───────┘           └───────────────┘
         │                            │
         │ gRPC                       │
         │                            │
    ┌────▼──────┐                     │
    │   Author  │◀────────────────────┘
    │ (认证服务) │
    │ 端口 8002 │
    └───────────┘
         │
    ┌────▼────┐   ┌───────▼────────┐
    │  MySQL  │   │     Redis      │
    └─────────┘   └────────────────┘
```

### 消息流说明

#### 1. 客户端连接与认证

```
Client → Gateway(NETTY) → AUTH_LOGIN_REQUEST
  ├── AuthMessageHandler
  ├── AuthorGrpcClient.authenticate() → Author(gRPC)
  ├── Author 验证账号密码，签发 JWT Token
  ├── AgentGrpcClient.notifyPlayerOnline() → Agent(gRPC)
  └── Agent 记录玩家会话（PlayerId → GatewayId）
```

#### 2. 心跳维护

```
Client → HEART_BEAT → HeartBeatMessageHandler → 回复 CommonResponse
                                  ↓
如果 90 秒内未收到任何数据 → HeartbeatHandler 触发 → 关闭连接
                                  ↓
                         ServerChannelHandler.channelInactive()
                                  ↓
                         AgentGrpcClient.notifyPlayerOffline()
```

#### 3. 消息路由

```
Client → Gateway → ServerChannelHandler.channelRead()
                        ↓
              MessageHandlerFactory.getHandler(MessageType)
                        ↓
     ┌──────────────────┼──────────────────┐
     ▼                  ▼                  ▼
AUTH_xxx           AGENT_xxx          HEART_xxx
     ▼                  ▼                  ▼
AuthMessageHandler  AgentMessageHandler  HeartBeatMessageHandler
     ▼                  ▼                  ▼
 Author gRPC       Agent gRPC         响应心跳
```

#### 4. 断连处理

```
连接断开 → channelInactive()
  ├── 若已绑定 PlayerId
  │   ├── AgentGrpcClient.notifyPlayerOffline() → Agent 清理会话
  │   └── sessionManager.removeChannel()
  └── 若未绑定
      └── sessionManager.removeChannel()
```

## 模块说明

| 模块 | 端口 | 职责 |
|------|------|------|
| **[cd-gateway](cd-gateway/README.md)** | 9527 (TCP) / 8081 (HTTP) / 8082 (gRPC) | 客户端长连接接入、Protobuf 编解码、消息分发路由 |
| **[cd-agent](cd-agent/README.md)** | 8004 (gRPC) | 在线玩家会话管理、消息转发路由、广播 |
| **[cd-author](cd-author/README.md)** | 8002 (gRPC) | 账号注册、登录认证、JWT Token 签发与验证 |
| **[cd-common](cd-common/README.md)** | — | 公共基础设施（gRPC 客户端、etcd 服务发现、工具类） |
| **[cd-proto](cd-proto/README.md)** | — | Protobuf 协议定义与代码生成 |

## 服务端口分配

| 服务 | HTTP/Web 端口 | gRPC 端口 | Netty TCP 端口 |
|------|-------------|-----------|---------------|
| Gateway | 8081 | 8082 | 9527 |
| Author | — | 8002 | — |
| Agent | 8003 | 8004 | — |
| etcd | — | 2379 | — |

## 快速启动

### 前置依赖
- JDK 21+
- Maven 3.8+
- MySQL 8.0
- Redis 6.0+
- etcd 3.5+

### 启动顺序

```bash
# 1. 编译 Proto（生成 Java 代码）
mvn compile -pl cd-proto

# 2. 编译全部模块
mvn clean package -DskipTests

# 3. 按顺序启动服务
# 确保 etcd、MySQL、Redis 已启动

# 启动 Author 服务
java -jar cd-author/target/cd-author-1.0-SNAPSHOT.jar

# 启动 Agent 服务
java -jar cd-agent/target/cd-agent-1.0-SNAPSHOT.jar

# 启动 Gateway 服务
java -jar cd-gateway/target/cd-gateway-1.0-SNAPSHOT.jar
```

## 协议说明

所有消息采用 Protobuf 序列化，通信协议分为两层：

1. **传输层**：Netty 帧协议（Varint32 长度前缀 + Protobuf 二进制数据）
2. **消息层**：统一 `GameMessage` 包装（Header + Body + MessageType）

详见 [cd-proto 协议定义](cd-proto/README.md)。

## 工程依赖

```
card-game-server (pom)
├── cd-proto        — Protobuf 定义 + gRPC 服务定义
├── cd-common       — 公共工具、配置、gRPC 客户端
├── cd-gateway      — Netty 网关（依赖 cd-common, cd-proto）
├── cd-author       — 认证服务（依赖 cd-common, cd-proto）
└── cd-agent        — Agent 服务（依赖 cd-common, cd-proto）
```
