package com.card.game.gateway.controller;

import com.card.game.common.controller.BaseHealthController;
import com.card.game.gateway.config.GatewayConfig;
import com.card.game.gateway.server.GatewayNettyServer;
import com.card.game.gateway.session.ChannelSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查端点
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController extends BaseHealthController {

    @Resource
    private GatewayConfig gatewayConfig;

    @Resource
    private GatewayNettyServer nettyServer;

    @Resource
    private ChannelSessionManager sessionManager;

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", gatewayConfig.getName());
        result.put("version", gatewayConfig.getVersion());
        result.put("env", gatewayConfig.getEnv());
        result.put("nettyPort", nettyServer.getPort());
        result.put("connections", sessionManager.getChannelCount());
        result.put("onlinePlayers", sessionManager.getOnlinePlayerCount());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
