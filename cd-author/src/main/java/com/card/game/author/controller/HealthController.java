package com.card.game.author.controller;

import com.card.game.author.config.AuthorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查端点
 * 用于 Kubernetes 或监控系统检测服务状态
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final AuthorConfig authorConfig;

    /**
     * 存活检查
     */
    @GetMapping("/liveness")
    public Map<String, Object> liveness() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 就绪检查
     */
    @GetMapping("/readiness")
    public Map<String, Object> readiness() {
        Map<String, Object> result = new HashMap<>();
        // TODO: 检查依赖服务是否可用（Redis、MySQL等）
        result.put("status", "READY");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", authorConfig.getName());
        result.put("version", authorConfig.getVersion());
        result.put("env", authorConfig.getEnv());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}