# cd-gateway — Gateway 网关服务

## 概述

Gateway 是游戏服务器的 **客户端 TCP 接入层**，基于 Netty 实现高性能长连接管理。它是客户端进入游戏服务器的唯一入口，负责协议编解码、心跳管理、消息路由分发。

## 技术架构

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2 | 应用框架 |
| Netty 4.1 | 高性能 NIO 网络框架 |
| Protobuf 3.25 | 消息序列化 |
| gRPC (io.grpc 1.62) | 服务间远程调用（调用 Author/Agent） |
| etcd (Jetcd) | 服务发现 |
| MySQL + JPA | 数据持久化 |
| Redis (Lettuce) | 缓存 |

## 模块结构

```
cd-gateway/
├── config/
│   ├── GatewayConfig.java        # 应用配置
│   └── NettyConfig.java          # Netty 参数配置
├── controller/
│   └── HealthController.java     # 健康检查端点
├── server/
│   ├── GatewayNettyServer.java       # Netty 服务器启动器（@PostConstruct）
│   ├── NettyServerInitializer.java   # Channel Pipeline 初始化
│   └── handler/
│       ├── HeartbeatHandler.java     # 空闲超时检测与断连
│       └── ServerChannelHandler.java # 消息分发 + 连接生命周期管理
├── service/
│   ├── MessageHandler.java           # 消息处理器接口
│   ├── MessageHandlerFactory.java    # 处理器工厂（前缀路由）
│   └── impl/
│       ├── AuthMessageHandler.java     # AUTH_ 消息处理 → Author 服务
│       ├── AgentMessageHandler.java    # AGENT_ 消息处理 → Agent 服务
│       └── HeartBeatMessageHandler.java# 心跳消息处理
├── session/
│   └── ChannelSessionManager.java  # Channel ↔ PlayerId 映射管理
├── util/
│   └── MessageUtils.java           # 消息发送工具类
└── GatewayApplication.java
```

## 核心功能

| 功能 | 说明 |
|------|------|
| **TCP 长连接** | Netty 管理客户端长连接，支持大量并发 |
| **Protobuf 编解码** | Varint32 长度帧 + Protobuf 序列化 |
| **消息路由分发** | 基于 `MessageType` 前缀（AUTH_/AGENT_/HEART_）自动路由到对应处理器 |
| **心跳管理** | 读空闲超时检测（90s），自动关闭僵尸连接 |
| **会话管理** | 维护 Channel 与 PlayerId 的映射关系 |
| **服务间调用** | 通过 gRPC 客户端调用 Author 和 Agent 服务（etcd 服务发现） |

## Netty Pipeline 处理链

```
入站（客户端 → 服务器）：
  IdleStateHandler → Varint32FrameDecoder → ProtobufDecoder → HeartbeatHandler → ServerChannelHandler

出站（服务器 → 客户端）：
  ServerChannelHandler → HeartbeatHandler → ProtobufEncoder → LengthFieldPrepender → IdleStateHandler
```

## 消息路由流程

```
客户端 TCP 连接
    ↓
ServerChannelHandler.channelRead()
    ↓
MessageHandlerFactory.getHandler(messageType)
    ↓
根据前缀分发：
  ├── AUTH_xxx  → AuthMessageHandler → Author gRPC
  ├── AGENT_xxx → AgentMessageHandler → Agent gRPC
  └── HEART_xxx → HeartBeatMessageHandler → 响应心跳
```

## 配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `netty.port` | 9527 | Netty 监听端口 |
| `netty.boss-threads` | 1 | Boss 线程数 |
| `netty.idle-timeout` | 90 | 读空闲超时（秒） |
| `server.port` | 8081 | Tomcat HTTP 端口（健康检查） |
| `grpc.server.port` | 8082 | gRPC 服务端口 |
