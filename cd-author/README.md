# cd-author — Author 认证服务

## 概述

Author 是游戏服务器的 **用户认证与账号管理服务**，负责处理玩家注册、登录认证、Token 验证、Token 刷新等身份鉴权相关的业务逻辑。

## 技术架构

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2 | 应用框架 |
| Spring Data JPA | ORM 持久化 |
| MySQL 8.0 | 数据库 |
| Redis (Lettuce) | Token 缓存 |
| gRPC (io.grpc 1.62) | 服务间远程调用 |
| Protobuf 3.25 | 消息序列化 |
| etcd (Jetcd) | 服务注册与发现 |
| JWT (jjwt) | 无状态 Token 生成与验证 |

## 模块结构

```
cd-author/
├── config/
│   └── AuthorConfig.java          # 应用配置
├── controller/
│   └── HealthController.java      # 健康检查端点
├── dto/
│   ├── LoginResult.java           # 登录结果 DTO
│   ├── RefreshResult.java         # Token 刷新结果 DTO
│   ├── RegisterResult.java        # 注册结果 DTO
│   └── VerifyResult.java          # Token 验证结果 DTO
├── entity/
│   └── AccountEntity.java         # 账号实体
├── enums/
│   └── AccountStatusEnum.java     # 账号状态枚举
├── grpc/
│   └── LoginGrpcServiceImpl.java  # gRPC 服务实现
├── repository/
│   └── AccountRepository.java     # 账号数据访问层
├── service/
│   ├── AccountService.java        # 账号管理服务
│   ├── AuthService.java           # 认证服务接口
│   └── TokenCacheService.java     # Token 缓存服务
├── util/
│   └── JwtUtils.java              # JWT 工具类
└── AuthorApplication.java
```

## 核心功能

| 功能 | 说明 |
|------|------|
| **账号注册** | 创建新游戏账号，密码加密存储 |
| **登录认证** | 验证用户名密码，签发 JWT Token |
| **Token 验证** | 验证 JWT Token 有效性及归属 |
| **Token 刷新** | 使用旧 Token 换取新 Token |
| **账号登出** | 清理 Token 缓存 |
| **踢下线** | 强制指定玩家下线 |

## gRPC 服务

定义在 [author.proto](../cd-proto/src/main/java/com/card/game/proto/author/author.proto) 中的 `LoginService`：

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

## 数据流

```
客户端 → Gateway(AUTH_请求) → Author(gRPC) → MySQL/Redis
                        ← Token/Result ←
```
