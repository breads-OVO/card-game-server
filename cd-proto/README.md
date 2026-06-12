# cd-proto — Protobuf 协议定义

## 概述

Proto 模块是项目的 **协议定义中心**，包含所有服务间通信的 Protobuf 消息定义和 gRPC 服务定义。采用 `proto3` 语法，通过 Maven 插件自动生成 Java 代码。

## 协议文件结构

```
cd-proto/src/main/java/com/card/game/proto/
├── common/
│   └── common.proto           # 通用定义（消息头、响应码、MessageType、GameMessage）
├── author/
│   └── author.proto           # Author 服务（LoginService）
├── agent/
│   └── agtnt.proto            # Agent 服务（AgentService）
└── gateway/
    └── gateway.proto          # Gateway 层协议（客户端通信格式）
```

## 消息类型枚举 (MessageType)

| 范围 | 类型 | 说明 |
|------|------|------|
| 0 | `UNKNOWN` | 未知类型 |
| 1 | `HEART_BEAT` | 心跳消息 |
| 2 | `RESPONSE_CLIENT` | 通用客户端响应 |
| 1001-1006 | `AUTH_*` | Author 认证服务 |
| 2001-2010 | `AGENT_*` | Agent 会话服务 |

## 核心消息结构

### GameMessage（统一消息包装器）

所有服务间通信使用此统一格式：

```protobuf
message GameMessage {
  MsgHeader header = 1;           // 消息头
  bytes body = 2;                 // 消息体（具体业务数据）
  MessageType messageType = 3;    // 消息类型标识
}

message MsgHeader {
  string msgId = 1;       // 消息ID
  int32 version = 2;      // 协议版本
  int64 timestamp = 3;    // 时间戳
  string seqId = 4;       // 请求序列号
  string token = 5;       // 认证Token
  int32 compress = 6;     // 压缩标志
}
```

### 通用响应

```protobuf
message CommonResponse {
  Code code = 1;          // 响应码
  string message = 2;     // 响应消息
}
```

## 服务定义

### Author - LoginService（gRPC）

```protobuf
service LoginService {
  rpc Register(RegisterRequest) returns (RegisterResponse);
  rpc Authenticate(AuthRequest) returns (AuthResponse);
  rpc VerifyToken(VerifyTokenRequest) returns (VerifyTokenResponse);
  rpc RefreshToken(RefreshTokenRequest) returns (RefreshTokenResponse);
  rpc Logout(LogoutRequest) returns (LogoutResponse);
  rpc KickPlayer(KickPlayerRequest) returns (common.CommonResponse);
}
```

### Agent - AgentService（gRPC）

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

## 代码生成

使用 `protobuf-maven-plugin` 自动生成 Java 代码：

```bash
mvn compile -pl cd-proto
```

生成目录：`target/generated-sources/protobuf/java/`
