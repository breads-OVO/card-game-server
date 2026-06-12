# Card Game Server — v2.0 任务分解

> 版本：v2.0
> 日期：2026-06-12
> 说明：根据 [需求文档-v2.md](需求文档-v2.md) 拆分为可执行的任务列表，按阶段和模块组织

---

## 任务总览

| 阶段 | 任务数 | 预估工时 | 说明 |
|------|--------|---------|------|
| Phase 0 — 基础准备 | 3 | 2人天 | Proto 协议定义 + gRPC 代码生成 + 公共 Bean 注册 |
| Phase 1 — Gateway 增强 | 3 | 2人天 | GrpcClient + MessageHandler + 消息路由注册 |
| Phase 2 — 匹配服务 | 4 | 4人天 | 项目脚手架 + gRPC 实现 + 匹配算法 + 队列管理 |
| Phase 3 — 游戏逻辑服务 | 9 | 15人天 | 项目脚手架 + 房间管理 + 回合制引擎 + 卡牌系统 + 技能系统 + 结算 |
| Phase 4 — 集成与容灾 | 3 | 3人天 | 断线重连 + 快照持久化 + 容灾恢复 |
| Phase 5 — 收尾 | 2 | 1人天 | 更新 README + 端到端测试 |
| **合计** | **24** | **~27人天** | |

---

## Phase 0 — 基础准备（依赖前置）

> **说明**：Proto 协议定义是后续所有模块的基础，必须先完成。

### TASK-0.1: Proto 协议扩展 — MessageType 枚举

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-proto |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | 无（可最先开始） |
| **对应需求** | 需求文档 6.1 节 |

**描述**：
在 [common.proto](../../cd-proto/src/main/java/com/card/game/proto/common/common.proto) 的 `MessageType` 枚举中追加匹配和游戏相关消息类型。

**交付物**：
- [ ] 追加 `MATCH_` 系列枚举（3001~3005）
  ```protobuf
  MATCH_START_REQUEST = 3001;
  MATCH_CANCEL_REQUEST = 3002;
  MATCH_SUCCESS_RESPONSE = 3003;
  MATCH_FAILED_RESPONSE = 3004;
  MATCH_STATUS_QUERY = 3005;
  ```
- [ ] 追加 `GAME_` 系列枚举（4001~4012）
- [ ] 运行 `mvn compile -pl cd-proto` 生成 Java 代码，确认无编译错误

---

### TASK-0.2: Proto 协议扩展 — 新增消息定义

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-proto |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-0.1 |
| **对应需求** | 需求文档 6.2 节、6.3 节 |

**描述**：
在 cd-proto 模块中新增匹配和游戏相关的 proto 文件，定义客户端消息结构和服务端 gRPC 接口。

**交付物**：

1. **新建 `match/match.proto`** — 匹配客户端消息
   - [ ] `MatchStartRequest`（gameMode）
   - [ ] `MatchCancelRequest`（gameMode）
   - [ ] `MatchSuccessResponse`（roomId, gameServerId, playerIds）
   - [ ] `MatchFailedResponse`（reason）
   - [ ] `MatchStatusQuery` / `MatchStatusResponse`

2. **新建 `match/match_service.proto`** — Matching gRPC 服务接口
   - [ ] `service MatchingService { StartMatch, CancelMatch, QueryMatchStatus, UpdateRating }`
   - [ ] `UpdateRatingRequest` / `PlayerRatingChange`

3. **新建 `game/game.proto`** — 游戏客户端消息
   - [ ] `PlayCardRequest` / `UseSkillRequest` / `JoinRoomRequest` / `ReconnectRequest` / `ExitRoomRequest`
   - [ ] `GameSnapshot` / `GameStateUpdate` / `PlayerFullState` / `PlayerDelta`
   - [ ] `Phase` 枚举（PREPARE/JUDGE/DRAW/PLAY/RESPOND/DISCARD/END）
   - [ ] `CardEvent` / `DamageEvent` / `ActRequest`

4. **新建 `game/game_service.proto`** — Game gRPC 服务接口
   - [ ] `service GameService { CreateRoom, JoinRoom, PlayCard, UseSkill, GetSnapshot, ExitRoom, GetRoomEvents }`
   - [ ] `CreateRoomRequest/Response` / `PlayCardResponse` / `UseSkillResponse` / `GetSnapshotRequest` / `GetRoomEventsRequest/Response`

5. [ ] 运行 `mvn compile -pl cd-proto` 确认全部生成成功

---

### TASK-0.3: 新增 gRPC Client + Bean 注册

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-common |
| **优先级** | P0 |
| **预估工时** | 1人天 |
| **依赖** | TASK-0.2（需要生成的 gRPC Stub 类） |
| **对应需求** | 需求文档 2.4 节、3.4 节 |

**描述**：
参照 [AuthorGrpcClient](../../cd-common/src/main/java/com/card/game/common/client/AuthorGrpcClient.java) 和 [AgentGrpcClient](../../cd-common/src/main/java/com/card/game/common/client/AgentGrpcClient.java) 的实现模式，新增 Matching 和 Game 的 gRPC 客户端，并在 [GrpcClientConfig](../../cd-common/src/main/java/com/card/game/common/config/GrpcClientConfig.java) 中注册 Bean。

**交付物**：

- [ ] 新建 `MatchingGrpcClient.java`
  - 懒加载 `MatchingServiceGrpc.MatchingServiceBlockingStub`
  - 方法：`startMatch()`、`cancelMatch()`、`queryMatchStatus()`（返回原型或包装对象）
  - 统一异常处理（try-catch → 抛 RuntimeException）

- [ ] 新建 `GameGrpcClient.java`
  - 懒加载 `GameServiceGrpc.GameServiceBlockingStub`
  - 方法：`createRoom()`、`joinRoom()`、`playCard()`、`useSkill()`、`getSnapshot()`、`exitRoom()`、`getRoomEvents()`
  - **注意**：`joinRoom()` 和 `getSnapshot()` 返回 `GameSnapshot` 对象，需要做 protobuf 到通用消息体的转换

- [ ] 在 `GrpcClientConfig.java` 中追加：
  ```java
  @Bean
  public MatchingGrpcClient matchingGrpcClient(GrpcChannelFactory channelFactory) {
      return new MatchingGrpcClient(channelFactory, "matching-server");
  }

  @Bean
  public GameGrpcClient gameGrpcClient(GrpcChannelFactory channelFactory) {
      return new GameGrpcClient(channelFactory, "game-server");
  }
  ```

- [ ] 在 `grpc-client-defaults.yml` 中追加 matching-server 和 game-server 的 discovery 配置

---

## Phase 1 — Gateway 增强

> **说明**：在 Gateway 模块中新增消息处理器，对接新增的 Matching 和 Game 服务。

### TASK-1.1: 实现 MatchMessageHandler

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-gateway |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-0.3（需要 MatchingGrpcClient） |
| **对应需求** | 需求文档 3.2 节 |

**描述**：
参照 [AuthMessageHandler](../../cd-gateway/src/main/java/com/card/game/gateway/service/impl/AuthMessageHandler.java) 的模式，实现 `MatchMessageHandler`，处理 `MATCH_` 前缀的消息。

**交付物**：

- [ ] 新建 `service/impl/MatchMessageHandler.java`
  - `@Component` 实现 `MessageHandler` 接口
  - `getMessageTypePrefix()` → 返回 `"MATCH"`
  - `handle()` → switch 分发

- [ ] 实现以下 handler 方法：
  - `handleStartMatch()` — 解析 `MatchStartRequest` body → 调用 `MatchingGrpcClient.startMatch()` → `MessageUtils.sendMessage()`
  - `handleCancelMatch()` — 解析 `MatchCancelRequest` body → 调用 `MatchingGrpcClient.cancelMatch()` → `MessageUtils.sendMessage()`
  - `handleQueryStatus()` — 解析 `MatchStatusQuery` body → 调用 `MatchingGrpcClient.queryMatchStatus()` → `MessageUtils.sendMessage()`

- [ ] `default` 分支处理 + `catch` 统一异常处理（同 AuthMessageHandler 模式）

---

### TASK-1.2: 实现 GameMessageHandler

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-gateway |
| **优先级** | P0 |
| **预估工时** | 1人天 |
| **依赖** | TASK-0.3（需要 GameGrpcClient） |
| **对应需求** | 需求文档 3.3 节 |

**描述**：
参照 `AuthMessageHandler` 的模式，实现 `GameMessageHandler`，处理 `GAME_` 前缀的消息。这是客户端与游戏引擎交互的桥梁。

**交付物**：

- [ ] 新建 `service/impl/GameMessageHandler.java`
  - `@Component` 实现 `MessageHandler` 接口
  - `getMessageTypePrefix()` → 返回 `"GAME"`

- [ ] 实现以下 handler 方法：
  - `handleJoinRoom()` — 解析 `JoinRoomRequest` → `GameGrpcClient.joinRoom()` → 返回 `GAME_SNAPSHOT` 给客户端
  - `handlePlayCard()` — 解析 `PlayCardRequest` → `GameGrpcClient.playCard()` → 将 `GameStateUpdate` 或 `PlayCardResponse` 返回给客户端
  - `handleUseSkill()` — 解析 `UseSkillRequest` → `GameGrpcClient.useSkill()` → 返回技能结果
  - `handleReconnect()` — 解析 `ReconnectRequest` → `GameGrpcClient.getSnapshot()` → 返回完整 `GAME_SNAPSHOT`
  - `handleExitRoom()` — 解析 `ExitRoomRequest` → `GameGrpcClient.exitRoom()` → 返回结果

- [ ] 注意：部分 Game 响应需要**广播**给房间内所有玩家，但 Gateway 层只负责单次请求-响应的转发。**广播逻辑由 Game 服务通过 Agent 的 ForwardMessage 推送**，Gateway 不需处理广播。

---

### TASK-1.3: Gateway 配置与验证

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-gateway |
| **优先级** | P1 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-1.1, TASK-1.2 |
| **对应需求** | 需求文档 3.1 节 |

**描述**：
确保两个新 Handler 被 `MessageHandlerFactory` 自动扫描并注册，做基本的集成验证。

**交付物**：

- [ ] 确认 `MessageHandlerFactory` 能自动扫描到 `MatchMessageHandler` 和 `GameMessageHandler`（检查启动日志输出）
- [ ] 确认 `MATCH_` 和 `GAME_` 前缀通过 `getMessageTypePrefix()` 方法正确匹配
- [ ] 编写 Gateway 模块的日志确认：启动时打印 `注册消息处理器: prefix=MATCH, handler=MatchMessageHandler`
- [ ] 单元测试：验证 `getMessageTypePrefix()` 返回值正确

---

## Phase 2 — 匹配服务 cd-matching

### TASK-2.1: 创建 cd-matching 项目脚手架

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-matching（新建） |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-0.2（gRPC 服务接口已生成） |
| **对应需求** | 需求文档 第 4 章 |

**描述**：
创建匹配服务的基础项目结构和 Spring Boot 应用。

**交付物**：

- [ ] 新建 Maven 模块 `cd-matching`，在父 `pom.xml` 中加入 `<module>cd-matching</module>`
- [ ] 目录结构：
  ```
  cd-matching/
  ├── src/main/java/com/card/game/matching/
  │   ├── config/
  │   │   └── MatchingConfig.java        # @ConfigurationProperties(app)
  │   ├── controller/
  │   │   └── HealthController.java
  │   ├── grpc/
  │   │   └── MatchingGrpcServiceImpl.java  # gRPC 服务实现
  │   ├── service/
  │   │   ├── MatchQueueService.java        # 匹配队列管理
  │   │   └── RatingService.java            # 段位分更新
  │   └── MatchingApplication.java
  ├── src/main/resources/
  │   ├── application.yml
  │   └── application-dev.yml
  ├── pom.xml
  └── README.md（可后续补充）
  ```
- [ ] `pom.xml` 依赖：`cd-common`、`cd-proto`、`spring-boot-starter-web`（健康检查）、`redisson`
- [ ] `application.yml`：`spring.application.name: matching-server`，gRPC 端口 8010
- [ ] 启动类 + 健康检查 Controller（参照 cd-agent 的模式）

---

### TASK-2.2: 实现 MatchingGrpcServiceImpl

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-matching |
| **优先级** | P0 |
| **预估工时** | 1人天 |
| **依赖** | TASK-2.1, TASK-2.3（MatchQueueService 先提供接口桩） |
| **对应需求** | 需求文档 4.1 节、4.5 节 |

**描述**：
实现 `MatchingServiceGrpc.MatchingServiceImplBase` 的具体逻辑。

**交付物**：

- [ ] `startMatch(MatchStartRequest, StreamObserver<MatchStartResponse>)`
  - 校验玩家是否已在匹配中（查 Redis）
  - 将玩家加入 Redis Sorted Set `match:queue:{gameMode}`
  - 返回 `MatchStartResponse`（含 waitTimeSeconds、queueSize）

- [ ] `cancelMatch(MatchCancelRequest, StreamObserver<CommonResponse>)`
  - 从队列中移除玩家
  - 返回成功响应

- [ ] `queryMatchStatus(MatchStatusQuery, StreamObserver<MatchStatusResponse>)`
  - 查询玩家是否在队列中 + 排队时间
  - 返回状态（IDLE/MATCHING/SUCCESS/FAILED）

- [ ] `updateRating(UpdateRatingRequest, StreamObserver<CommonResponse>)`
  - 使用 `gameRoundId` 幂等校验
  - 更新各玩家段位分（调用 RatingService）
  - 写入 `player_rating_log` 表

---

### TASK-2.3: 实现匹配队列与匹配算法

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-matching |
| **优先级** | P0 |
| **预估工时** | 2人天 |
| **依赖** | TASK-2.1 |
| **对应需求** | 需求文档 4.1 节、4.2 节、4.3 节 |

**描述**：
这是匹配服务的核心——实现匹配队列管理和段位分匹配算法。

**交付物**：

- [ ] `MatchQueueService` 实现：
  - `enqueue(playerId, gameMode, score)` — 加入 Redis Sorted Set
  - `dequeue(playerId, gameMode)` — 从队列移除
  - `isInQueue(playerId, gameMode)` — 检查是否在队列中
  - `getQueueSize(gameMode)` — 获取队列长度
  - `getWaitTime(playerId, gameMode)` — 获取已等待时间
  - 使用 Redisson RLock 保证并发安全

- [ ] **匹配调度器** `MatchScheduler`：
  - `@Scheduled(fixedRate = 2000)` 定时任务
  - 每次扫描：
    1. 获取 Redisson RLock 防止多实例冲突
    2. 按分数升序扫描队列
    3. 实现扩圈逻辑：`allowedRange = initialRange + min(expandCount × expandStep, maxRange)`
    4. 找到 8 人组，从队列移除
    5. 调用 Game 服务的 `CreateRoom` RPC
    6. 成功 → 向 8 人发送 `MATCH_SUCCESS_RESPONSE`
    7. 失败（重试 3 次后）→ 将 8 人重新入队

- [ ] 匹配超时清理：
  - 扫描队列中等待时间超过 `maxWaitSeconds` 的玩家
  - 自动移除并推送 `MATCH_FAILED_RESPONSE`（reason: timeout）

---

### TASK-2.4: 实现段位分服务

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-matching |
| **优先级** | P1 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-2.1 |
| **对应需求** | 需求文档 4.4 节 |

**描述**：
实现 RatingService，处理段位分更新和持久化。

**交付物**：

- [ ] `RatingService` 实现：
  - `updateRating(PlayerRatingChange[] changes, String gameRoundId)` — 批量更新段位分
  - **幂等**：Redis 记录 `match:rating:done:{gameRoundId}`，已处理的直接返回 SUCCESS
  - ELO 计算公式：`newScore = oldScore + 32 × (actual - expected)`，其中 `expected = 1 / (1 + 10^((opponentAvg - myScore) / 400))`
  - 写入 MySQL `player_rating_log` 表（playerId, oldScore, newScore, change, gameRoundId, createTime）

- [ ] MySQL `player_rating_log` 表的 JPA Entity + Repository

---

## Phase 3 — 游戏逻辑服务 cd-game（核心）

> **说明**：这是 v2.0 最复杂的模块，分多个子模块并行开发。

### TASK-3.1: 创建 cd-game 项目脚手架

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game（新建） |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-0.2（gRPC 服务接口已生成） |
| **对应需求** | 需求文档 第 5 章 |

**描述**：
创建游戏逻辑服务的基础项目结构。

**交付物**：

- [ ] 新建 Maven 模块 `cd-game`，在父 `pom.xml` 中加入 `<module>cd-game</module>`
- [ ] 目录结构：
  ```
  cd-game/
  ├── src/main/java/com/card/game/game/
  │   ├── config/
  │   │   └── GameConfig.java
  │   ├── controller/
  │   │   └── HealthController.java
  │   ├── grpc/
  │   │   └── GameGrpcServiceImpl.java       # gRPC 服务实现
  │   ├── engine/
  │   │   ├── RoomActor.java                 # 房间 Actor（单线程事件循环）
  │   │   ├── RoomActorManager.java          # Actor 管理器
  │   │   ├── GameState.java                 # 游戏状态
  │   │   ├── PhaseManager.java              # 阶段管理器
  │   │   ├── CardDeck.java                  # 牌堆
  │   │   ├── Card.java / CardManager.java   # 卡牌定义与管理
  │   │   ├── Skill.java / SkillManager.java # 技能接口与管理
  │   │   └── EventBus.java                  # 事件总线（技能触发）
  │   ├── service/
  │   │   ├── RoomService.java               # 房间管理服务
  │   │   ├── GameSnapshotService.java       # 快照服务
  │   │   └── SettlementService.java         # 结算服务
  │   ├── entity/
  │   │   └── GameRecordEntity.java          # 对局记录实体
  │   ├── repository/
  │   │   └── GameRecordRepository.java
  │   └── GameApplication.java
  ├── src/main/resources/
  │   ├── application.yml
  │   └── application-dev.yml
  ├── pom.xml
  └── README.md
  ```
- [ ] `pom.xml` 依赖：`cd-common`、`cd-proto`、`redisson`、`spring-boot-starter-data-jpa`、`mysql-connector-j`
- [ ] `application.yml`：`spring.application.name: game-server`，gRPC 端口 8020
- [ ] `GameConfig.java`：配置项（Actor 线程池大小、超时时间、快照间隔等）

---

### TASK-3.2: 实现 GameGrpcServiceImpl 骨架

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 1人天 |
| **依赖** | TASK-3.1, TASK-3.3（RoomService 先提供接口桩） |
| **对应需求** | 需求文档 5.6 节 |

**描述**：
实现 `GameServiceGrpc.GameServiceImplBase`，作为 Gateway 与游戏引擎之间的入口层。各方法先将请求转发给 RoomActor 处理。

**交付物**：

- [ ] `createRoom(CreateRoomRequest, StreamObserver<CreateRoomResponse>)` — 创建房间 → RoomActorManager 分配 Actor

- [ ] `joinRoom(JoinRoomRequest, StreamObserver<GameSnapshot>)` — 返回房间快照

- [ ] `playCard(PlayCardRequest, StreamObserver<PlayCardResponse>)` — 提交操作到 RoomActor

- [ ] `useSkill(UseSkillRequest, StreamObserver<UseSkillResponse>)` — 提交技能到 RoomActor

- [ ] `getSnapshot(GetSnapshotRequest, StreamObserver<GameSnapshot>)` — 从 Redis 或内存读取快照返回

- [ ] `exitRoom(ExitRoomRequest, StreamObserver<CommonResponse>)` — 标记逃跑

- [ ] `getRoomEvents(GetRoomEventsRequest, StreamObserver<GetRoomEventsResponse>)` — 返回事件列表

---

### TASK-3.3: 实现房间管理与 Actor 模型

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 2人天 |
| **依赖** | TASK-3.1 |
| **对应需求** | 需求文档 5.1 节、5.4 节 |

**描述**：
实现房间生命周期管理和单线程 Actor 模型，这是游戏引擎的骨架。

**交付物**：

- [ ] `GameState` — 游戏状态数据结构
  - 房间元数据（roomId, gameMode, status, createTime）
  - 玩家列表、座位安排、身份分配
  - 当前回合索引、当前阶段
  - 牌堆、弃牌堆
  - 事件队列（最近 100 条）

- [ ] `RoomActor` — 单线程事件循环
  - `submitAction(PlayerAction)` — 外部提交玩家操作
  - `processLoop()` — 内部单线程循环
  - `processAction(action)` — 处理单个操作：合法性校验 → 状态变更 → 事件链 → 广播
  - `checkTimeouts()` — 检查出牌超时/阶段超时
  - `checkAutoTransitions()` — 自动阶段切换
  - `fireEvent(GameEvent)` — 触发技能事件链
  - 使用 `ScheduledExecutorService` 作为调度器

- [ ] `RoomActorManager` — Actor 管理器
  - `createRoom(players, gameMode)` → 创建 RoomActor 并启动
  - `getActor(roomId)` → 获取 Actor
  - `destroyRoom(roomId)` → 销毁 Actor
  - 线程池配置：可配置大小（默认 16）
  - 房间 → Actor 映射存储在内存 + Redis（`game:room:{roomId}:owner`）

- [ ] **身份分配逻辑**：随机分配主公、忠臣（x2）、反贼（x4）、内奸（x1）

---

### TASK-3.4: 实现回合制阶段管理器

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 2人天 |
| **依赖** | TASK-3.3 |
| **对应需求** | 需求文档 5.2.1 节、5.2.2 节 |

**描述**：
实现完整的回合阶段流转和响应链逻辑，这相当于"游戏引擎的操作系统"。

**交付物**：

- [ ] `PhaseManager` — 阶段管理器
  - `enterPhase(phase)` — 进入指定阶段，触发 `PHASE_START` 事件
  - `exitPhase(phase)` — 退出阶段，触发 `PHASE_END` 事件
  - `nextPhase()` — 自动切换到下一阶段
  - `nextTurn()` — 切换到下一个玩家回合

- [ ] 各阶段处理逻辑：
  - **准备阶段**：技能触发点（如诸葛观星）
  - **判定阶段**：检查判定区是否有延时锦囊，执行判定
  - **摸牌阶段**：默认摸 2 张（技能可修改数量）
  - **出牌阶段**：核心阶段，等待玩家主动操作，或点击"结束出牌"→ 进入弃牌阶段
  - **弃牌阶段**：手牌数 > 体力值 → 弃至体力值
  - **结束阶段**：技能触发点（如闭月）

- [ ] **响应链（Request-Response 模型）**：
  - `enterResponding(actionType, timeout, targetPlayer)` — 进入响应等待状态
  - `submitResponse(playerId, cardId/SkillName)` — 处理响应
  - 支持链式响应（南蛮入侵：对每个玩家依次执行）
  - 超时处理：超时未响应视为放弃

- [ ] 回合超时看门狗：出牌阶段 30 秒无操作，自动进入弃牌阶段

---

### TASK-3.5: 实现牌堆与卡牌系统

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 2人天 |
| **依赖** | TASK-3.3 |
| **对应需求** | 需求文档 5.2.3 节 |

**描述**：
实现标准版 108 张牌的牌堆、洗牌、摸牌、卡牌效果等。

**交付物**：

- [ ] `Card` 数据结构：
  ```java
  class Card {
      String cardId;      // 唯一 ID（UUID）
      CardSuit suit;      // 花色：SPADE/HEART/CLUB/DIAMOND
      int number;         // 点数：1-13
      CardName name;      // 牌名：杀/闪/桃/南蛮入侵/...
      CardType type;      // 类型：BASIC/STRATEGY/EQUIP
      // ... 额外属性
  }
  ```

- [ ] `CardDeck` — 牌堆实现：
  - 初始化标准版 108 张牌（按三国杀标准配置）
  - `shuffle()` — 洗牌
  - `draw()` — 摸一张牌
  - `draw(count)` — 摸多张
  - `discard(card)` — 弃牌
  - `findById(cardId)` — 按 ID 查找

- [ ] **基本牌效果**：
  - 杀（SHA）：对攻击范围内的目标造成 1 点伤害
  - 闪（SHAN）：抵消杀的效果
  - 桃（TAO）：回复 1 点体力 / 濒死时救回

- [ ] **锦囊牌效果**：
  - 南蛮入侵：除使用者外，每人需出一张杀，否则扣 1 血
  - 万箭齐发：除使用者外，每人需出一张闪，否则扣 1 血
  - 桃园结义：所有角色回复 1 血
  - 五谷丰登：所有角色从牌堆摸 1 张牌（使用者先摸）
  - 乐不思蜀（延时）：判定阶段判定，若不为红桃则跳过出牌阶段
  - 兵粮寸断（延时）：判定阶段判定，若不为梅花则跳过摸牌阶段

- [ ] **装备牌效果**：
  - 武器：改变攻击范围（诸葛连弩：攻击范围 1，可出多张杀；青龙偃月刀：攻击范围 3；等等）
  - 防具：仁王盾（黑色杀无效），八卦阵（判定红色视为闪）
  - +1 马：别人打你需要距离 +1
  - -1 马：你打别人距离 -1

- [ ] **距离计算**：
  - `calculateDistance(from, to)` — 座位距离（最少座位差）
  - 受武器、-1马、+1马影响

---

### TASK-3.6: 实现技能系统

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P1 |
| **预估工时** | 3人天 |
| **依赖** | TASK-3.4（需要阶段系统和响应链） |
| **对应需求** | 需求文档 5.2.4 节 |

**描述**：
实现技能接口、事件总线和标准版武将技能。

**交付物**：

- [ ] `Skill` 接口：
  ```java
  interface Skill {
      String getName();
      SkillType getType();      // ACTIVE / PASSIVE / LOCKED
      boolean canTrigger(GameEvent event, Player player, GameContext context);
      boolean canActivate(Player player, GameContext context);  // 主动技能可发动检查
      void execute(GameEvent event, Player player, GameContext context);
  }
  ```

- [ ] `EventBus` — 事件总线：
  - `fire(event)` — 触发事件，遍历所有存活玩家的技能，符合条件的自动触发
  - `register(player, skill)` — 注册技能
  - `unregister(player)` — 注销技能（死亡时）
  - 事件类型（参考需求文档 5.2.4 节的 `GameEvent.Type`）

- [ ] **标准版武将技能实现（部分）**：
  - 曹操（奸雄）：当你受到伤害后，你可以获得造成伤害的牌
  - 孙权（制衡）：出牌阶段，你可以弃置任意张牌，然后摸等量的牌（每回合限一次）
  - 刘备（仁德）：出牌阶段，你可以将任意张手牌交给其他角色，然后你回复 1 点体力
  - 关羽（武圣）：你可以将一张红色牌当作"杀"使用
  - 张飞（咆哮）：你使用"杀"无次数限制
  - 诸葛亮（观星）：准备阶段，你可以观看牌堆顶 X 张牌（X 为存活角色数≤5），调整顺序
  - 司马懿（反馈）：当你受到伤害后，你可以获得伤害来源的一张牌
  - （后续技能可在 v2.1 迭代中扩展）

---

### TASK-3.7: 实现游戏结算系统

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 1人天 |
| **依赖** | TASK-3.3, TASK-3.4（需要房间和阶段系统） |
| **对应需求** | 需求文档 5.1 节 |

**描述**：
实现游戏结束条件检查和结算流程。

**交付物**：

- [ ] **结束条件检查** `checkWinCondition()`：
  - 主公死亡 → 若内奸存活，内奸胜利；否则反贼胜利
  - 反贼全灭 → 若内奸存活且主公存活，继续；若仅剩主公和内奸 → 进入单挑
  - 仅剩一方势力存活 → 该方胜利

- [ ] `SettlementService`：
  - `executeSettlement(roomId, winner, players)` — 执行结算
  - 计算 MVP（输出伤害最高者）
  - 计算各玩家积分变化
  - 构建结算结果
  - 调用 Matching 的 `UpdateRating` RPC 更新段位分
  - 写入 MySQL `game_record` 表

- [ ] **逃跑结算**：
  - WAITING 阶段逃跑：扣分（逃跑者扣 50，其他人不扣）
  - PLAYING 阶段逃跑：视为死亡，弃置所有牌，扣分（逃跑者扣 30，其他人不扣）

- [ ] MySQL `game_record` 表的 JPA Entity + Repository
  - 字段：id, roomId, gameMode, startTime, endTime, winnerIdentity, players (JSON), winnerId, mvps (JSON), createTime

---

### TASK-3.8: 实现快照持久化与状态同步

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P0 |
| **预估工时** | 1.5人天 |
| **依赖** | TASK-3.3（需要 GameState） |
| **对应需求** | 需求文档 5.5 节、7.4 节 |

**描述**：
实现游戏状态快照的 Redis 持久化、全量/增量同步消息构建。

**交付物**：

- [ ] `GameSnapshotService`：
  - `buildSnapshot(roomId)` — 构建完整 `GameSnapshot` protobuf 消息
  - `saveSnapshot(roomId)` — 异步写入 Redis（key: `game:room:{roomId}:snapshot`），不过期时间 30 分钟
  - `loadSnapshot(roomId)` — 从 Redis 读取快照
  - `saveRoomInfo(roomId, info)` — 保存房间元数据（Redis Hash）
  - `appendEvent(roomId, event)` — 追加事件到 List（保持最多 100 条）
  - `getRecentEvents(roomId, fromSeq, maxCount)` — 获取最近事件（重连用）

- [ ] **全量同步消息** `buildFullSnapshot()`：
  - 包含所有 `PlayerFullState`（含手牌 ID、身份、装备等）
  - 牌堆/弃牌堆大小
  - 当前回合、当前阶段

- [ ] **增量同步消息** `buildDeltaUpdate()`：
  - 仅包含变化的 `PlayerDelta`
  - 卡牌事件列表和伤害事件列表
  - 死亡的玩家 ID

- [ ] **广播机制**：
  - 构建增量消息后，通过 Agent 的 `ForwardMessage` 接口推送给房间内所有存活玩家
  - 注意：死亡玩家也接收消息（旁观模式）

---

### TASK-3.9: 实现托管系统

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P1 |
| **预估工时** | 1人天 |
| **依赖** | TASK-3.4, TASK-3.5 |
| **对应需求** | 需求文档 7.2 节 |

**描述**：
实现玩家断线后的 AI 托管逻辑。

**交付物**：

- [ ] 托管触发时机：
  - RoomActor 检测到玩家心跳超时（由 Agent 通知 Game：玩家下线）
  - 玩家状态标记为 `OFFLINE`

- [ ] 托管策略（v2.0 简化版）：
  - 出牌阶段：自动结束出牌阶段（跳过）
  - 需要出"杀"响应时：如果有杀，自动出
  - 需要出"闪"响应时：如果有闪，自动出
  - 濒死求桃时：如果有桃，自动出桃自救
  - 判定阶段：正常判定
  - 摸牌/弃牌：正常执行

- [ ] 托管恢复：
  - 玩家重连后，清除 `OFFLINE` 标记
  - 恢复正常控制权

---

## Phase 4 — 集成与容灾

### TASK-4.1: 实现断线重连

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game, cd-gateway 联调 |
| **优先级** | P0 |
| **预估工时** | 1.5人天 |
| **依赖** | TASK-3.8（需要快照服务） |
| **对应需求** | 需求文档 第 8 章 |

**描述**：
实现从客户端断线到重连恢复的完整流程。

**交付物**：

- [ ] Gateway 端（TASK-1.2 已包含 `handleReconnect`）：
  - 接收 `RECONNECT_REQUEST`，转发 `GameGrpcClient.getSnapshot()`

- [ ] Game 端：
  - `GetSnapshot` 处理：校验玩家是否在房间内 → 从 Redis 加载快照 → 添加最近事件 → 返回
  - 重连成功后：玩家状态从 `OFFLINE` 改为 `ALIVE`
  - 广播给房间其他玩家：XXX 已重连

- [ ] 断线超时清理：
  - RoomActor 中启动定时任务
  - 玩家断线超过 90 秒未重连 → 视为逃跑（TASK-3.7 逃跑结算）

---

### TASK-4.2: 实现 Game → Agent 推送（状态广播）

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game, cd-agent 联调 |
| **优先级** | P1 |
| **预估工时** | 1人天 |
| **依赖** | TASK-3.8 |
| **对应需求** | 需求文档 5.5 节、2.2 节 |

**描述**：
Game 服务需要将状态更新推送给房间内玩家。由于玩家在 Gateway 上，Game 需要通过 Agent 的 `ForwardMessage` 接口转发。

**交付物**：

- [ ] 在 Game 模块中注入 `AgentGrpcClient`（需要先确认 Agent 的 `ForwardMessage` 接口是否已实现）

- [ ] 实现推送代理 `PushService`：
  - `pushToPlayer(playerId, messageType, body)` — 向单播
  - `pushToRoom(roomId, messageType, body, excludePlayers)` — 向房间内所有存活玩家广播
  - 底层调用 `AgentGrpcClient.forwardMessage()`

- [ ] 如果 Agent 的 `messageForwardService` 尚未实现（v1 TODO），需要在此任务中补充实现：
  - `MessageForwardService.processMessage()` → 根据 `playerId` 查询所在 Gateway
  - `AgentGrpcClient` 已经存在（v1 已创建）
  - Gateway 端需要有一个通用的消息推送接口

---

### TASK-4.3: 容灾恢复机制

| 字段 | 内容 |
|------|------|
| **所属模块** | cd-game |
| **优先级** | P1 |
| **预估工时** | 0.5人天 |
| **依赖** | TASK-3.8（需要快照数据） |
| **对应需求** | 需求文档 9.2 节 |

**描述**：
实现 Game 实例宕机后的房间恢复机制。

**交付物**：

- [ ] **心跳续租**：
  - 每个房间定期续租 Redis key `game:room:{roomId}:owner` 的 TTL（类似 etcd 租约）
  - 续租间隔：15 秒（TTL 设为 30 秒）

- [ ] **宕机检测与恢复**（在 RoomActorManager 中实现）：
  - 启动时扫描 Redis 中所有 `game:room:*:owner` 的 key
  - 检查 owner 是否为当前实例
  - 如果是其他已宕机的实例 → 尝试恢复房间
  - 恢复步骤：读取快照 → 创建新 RoomActor → 继续游戏
  - 恢复失败 → 广播"对局异常终止"，释放房间

- [ ] **看门狗**：
  - 每个 RoomActor 关联一个看门狗定时器
  - 若一个操作超过 30 秒未返回，强制 kill Actor，清理房间并通知玩家

---

## Phase 5 — 收尾

### TASK-5.1: 更新 README 与文档

| 字段 | 内容 |
|------|------|
| **所属模块** | 文档 |
| **优先级** | P1 |
| **预估工时** | 0.5人天 |
| **依赖** | 无（可在开发过程中并行） |
| **对应需求** | 需求文档 14 章 |

**描述**：
更新项目 README 和新增模块的说明文档。

**交付物**：

- [ ] 更新根目录 [README.md](../../README.md)
  - 架构图加入 Matching 和 Game 服务
  - 端口分配表加入 8010/8020
  - 消息流说明加入匹配和游戏流程
- [ ] 创建 [cd-matching/README.md](../../cd-matching/README.md)
- [ ] 创建 [cd-game/README.md](../../cd-game/README.md)
- [ ] 更新 [cd-proto/README.md](../../cd-proto/README.md) 加入新增的 proto 文件说明

---

### TASK-5.2: 端到端集成测试

| 字段 | 内容 |
|------|------|
| **所属模块** | 集成测试 |
| **优先级** | P0 |
| **预估工时** | 0.5人天 |
| **依赖** | 所有 TASK 完成 |
| **对应需求** | 全部 |

**描述**：
验证完整链路：客户端登录 → 匹配 → 加入房间 → 出牌 → 结算。

**交付物**：

- [ ] 验证 `cd-proto` 编译无错误
- [ ] 验证 `cd-common` 编译无错误
- [ ] 验证各服务启停正常（Matching、Game）
- [ ] 验证完整消息流：
  - Client → Gateway(MATCH_START_REQUEST) → Matching 加入队列
  - Matching 匹配成功 → Game.CreateRoom
  - Client → Gateway(JOIN_ROOM_REQUEST) → Game 返回快照
  - Client → Gateway(PLAY_CARD_REQUEST) → Game 处理并广播
  - Game 结算 → Matching.UpdateRating
- [ ] 验证断线重连流程
- [ ] 验证容灾场景（手动 kill Game 实例，观察恢复）

---

## 甘特图 / 依赖顺序

```
TASK-0.1 ──────────────────────────────────────────────────
TASK-0.2 ──┤
TASK-0.3 ─────┤
              │
TASK-1.1 ─────┼─────┤
TASK-1.2 ─────┼─────┤
TASK-1.3 ─────┼─────┼──┤
              │     │
TASK-2.1 ─────┼─────┤
TASK-2.2 ─────┼─────┼────┤
TASK-2.3 ─────┼─────┼────────┤
TASK-2.4 ─────┼─────┼────────┼───┤
              │     │
TASK-3.1 ─────┼─────┤
TASK-3.2 ─────┼─────┼────┤
TASK-3.3 ─────┼─────┼────────┤
TASK-3.4 ─────┼─────┼────────┼──────┤
TASK-3.5 ─────┼─────┼────────┼──────┤
TASK-3.6 ─────┼─────┼────────┼──────┼──────┤
TASK-3.7 ─────┼─────┼────────┼──────┼─────────┤
TASK-3.8 ─────┼─────┼────────┼──────┼────┤
TASK-3.9 ─────┼─────┼────────┼──────┼──────┼───┤
              │     │
TASK-4.1 ─────┼─────┼──────────────────────────┼──────┤
TASK-4.2 ─────┼─────┼──────────────────────────┼──────┤
TASK-4.3 ─────┼─────┼──────────────────────────┼──────┤
              │     │
TASK-5.1 ─────┼─────┼────────────────────────────────────┤
TASK-5.2 ─────┼─────┼────────────────────────────────────────┤
```

**关键依赖路径**：
1. `TASK-0.1` → `TASK-0.2` → `TASK-0.3` → `TASK-1.x`（Gateway 路径）
2. `TASK-0.x` → `TASK-2.1` → `TASK-2.3` → `TASK-2.2`（Matching 路径）
3. `TASK-0.x` → `TASK-3.1` → `TASK-3.3` → `TASK-3.4` → `TASK-3.5/3.6` → `TASK-3.7`（Game 主路径）
4. `TASK-3.8` 依赖 `TASK-3.3`，完成后可并行推进 `TASK-4.1/4.2/4.3`

---

## 任务统计

| 类型 | 数量 |
|------|------|
| P0 任务 | 15 |
| P1 任务 | 9 |
| 新建模块 | 2（cd-matching, cd-game） |
| 修改模块 | 3（cd-proto, cd-common, cd-gateway） |
| 新建 proto 文件 | 4（match/match.proto, match/match_service.proto, game/game.proto, game/game_service.proto） |
| 新建 Java 文件（预估） | ~40+ |
