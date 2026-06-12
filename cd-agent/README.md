# cd-agent — Agent 服务

## 概述

Agent 是游戏服务器架构中的 **会话管理中心与消息路由中间件**，处于 Gateway（网关层）和具体业务服务之间。它不处理具体业务逻辑，而是作为在线玩家的注册中心和消息路由器。

## 技术架构

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2 | 应用框架 |
| gRPC (io.grpc 1.62) | 服务间远程调用 |
| Protobuf 3.25 | 消息序列化 |
| etcd (Jetcd) | 服务注册与发现 |
| MySQL + JPA | 持久化存储 |
| Redis (Lettuce) | 缓存与会话管理 |

## 模块结构

```
cd-agent/
├── config/
│   └── AgentConfig.java          # 应用配置（会话过期、线程池等）
├── controller/
│   └── HealthController.java     # 健康检查端点
├── dto/
│   ├── OnlinePlayer.java         # 在线玩家 DTO
│   └── PlayerSession.java        # 玩家会话 DTO
├── grpc/
│   └── AgentGrpcServiceImpl.java # gRPC 服务实现
├── service/
│   ├── MessageForwardService.java      # 消息转发接口
│   └── PlayerSessionManagerService.java  # 玩家会话管理接口
└── AgentApplication.java
```

## 核心功能

| 功能 | 说明 |
|------|------|
| **玩家上线通知** | 记录玩家登录后的会话信息（sessionId、gatewayId、token 等） |
| **玩家下线通知** | 清理玩家会话状态 |
| **消息转发** | 根据玩家 ID 路由到对应的业务服务 |
| **玩家状态查询** | 单人或批量查询玩家在线状态及会话详情 |
| **广播消息** | 向所有（或指定条件）在线玩家发送消息 |
| **踢玩家下线** | 主动将指定玩家踢出游戏 |

## gRPC 服务

定义在 [agtnt.proto](../cd-proto/src/main/java/com/card/game/proto/agent/agtnt.proto) 中的 `AgentService`：

```protobuf
service AgentService {
  rpc NotifyPlayerOnline(OnlineNotifyRequest) returns (common.CommonResponse);
  rpc NotifyPlayerOffline(OfflineNotifyRequest) returns (common.CommonResponse);
  rpc ForwardMessage(ForwardMessageRequest) returns (ForwardMessageResponse);
  rpc GetPlayerStatus(GetPlayerStatusRequest) returns (GetPlayerStatusResponse);
  rpc BatchGetPlayerStatus(BatchGetPlayerStatusRequest) returns (BatchGetPlayerStatusResponse);
  rpc BroadcastMessage(BroadcastMessageRequest) returns (common.CommonResponse);
  rpc KickPlayer(KickPlayerRequest) returns (common.CommonResponse);
}
```

## 配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `grpc.server.port` | 8004 | gRPC 服务端口 |
| `app.session.expire-seconds` | 90 | 会话过期时间 |
| `app.session.max-online-players` | 100000 | 最大在线玩家数 |

## 依赖关系

```
Gateway → Agent → 业务服务
     ↓          ↓
   etcd 服务发现与注册
```
