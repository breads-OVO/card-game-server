package com.card.game.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础健康检查端点
 * 用于 Kubernetes 或监控系统检测服务状态
 */
@Slf4j
public class BaseHealthController {

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
}