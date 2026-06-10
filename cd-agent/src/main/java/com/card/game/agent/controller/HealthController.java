package com.card.game.agent.controller;

import com.card.game.agent.config.AgentConfig;
import com.card.game.agent.service.PlayerSessionManagerService;
import com.card.game.common.controller.BaseHealthController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController extends BaseHealthController {

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private PlayerSessionManagerService sessionManager;

    /**
     * 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", agentConfig.getName());
        result.put("version", agentConfig.getVersion());
        result.put("env", agentConfig.getEnv());
        result.put("instanceId", agentConfig.getInstanceId());
        result.put("onlinePlayers", sessionManager.getOnlinePlayerCount());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
