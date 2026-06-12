# cd-common — 公共模块

## 概述

Common 是项目的 **公共基础库**，提供各模块共享的工具类、配置、gRPC 客户端、etcd 服务注册/发现、异常定义等基础设施。

## 模块结构

```
cd-common/
├── client/
│   ├── AgentGrpcClient.java     # Agent 服务 gRPC 客户端
│   └── AuthorGrpcClient.java    # Author 服务 gRPC 客户端
├── config/
│   ├── DatabaseConfig.java      # 数据库配置
│   ├── EtcdConfig.java          # etcd 客户端与 NameResolver 注册
│   ├── GrpcClientConfig.java    # gRPC 客户端 Bean 注册
│   └── RedisConfig.java         # Redis 配置
├── constant/
│   ├── CodeEnum.java            # 业务状态码枚举
│   ├── Constants.java           # 通用常量
│   └── MessageId.java           # 消息 ID 定义
├── controller/
│   └── BaseHealthController.java # 健康检查基类
├── db/
│   └── BaseEntity.java          # JPA 实体基类
├── etcd/
│   ├── EtcdNameResolver.java        # gRPC 自定义 NameResolver（etcd 实现）
│   ├── EtcdNameResolverProvider.java # NameResolver 提供者
│   ├── EtcdServiceRegistry.java     # 服务自动注册（启动时注册到 etcd）
│   └── GrpcChannelFactory.java      # gRPC 通道工厂（懒加载 + 缓存）
├── exception/
│   ├── BusinessException.java   # 业务异常
│   └── ErrorCode.java           # 错误码定义
├── service/
│   └── RedisService.java        # Redis 操作封装
└── util/
    ├── DateUtils.java           # 日期工具
    ├── IdGenerator.java         # ID 生成器（UUID）
    └── JsonUtils.java           # JSON 工具
```

## gRPC 客户端

两个 gRPC 客户端采用相同的设计模式：

- **懒加载**：Stub 在首次调用时初始化（双重检查锁定）
- **etcd 服务发现**：通过 `GrpcChannelFactory` 从 etcd 解析服务地址
- **统一异常处理**：gRPC 调用异常统一包装为 `RuntimeException`

```java
GrpcChannelFactory → resolveServiceAddress("author-server")
                    → 查询 etcd 路径 /services/game/author-server/
                    → 返回 "ip:port"
                    → 创建 ManagedChannel
```

## etcd 服务注册与发现

### 服务注册 (EtcdServiceRegistry)

每个服务启动时自动注册到 etcd：

```
etcd key: /services/game/{serviceName}/{ip}:{port}
etcd value: {ip}:{port}
租约：自动续期，服务宕机后自动过期
```

### 服务发现 (GrpcChannelFactory / EtcdNameResolver)

- `GrpcChannelFactory`：Gateway 等客户端模块通过服务名查询 etcd 获取地址
- `EtcdNameResolver`：支持 gRPC `discovery://` 协议的动态服务发现
- 客户端**缓存** gRPC 通道，避免重复创建
