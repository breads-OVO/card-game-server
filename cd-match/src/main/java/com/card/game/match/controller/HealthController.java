package com.card.game.match.controller;

import com.card.game.common.controller.BaseHealthController;
import com.card.game.match.config.MatchConfig;
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
 * 用于 Kubernetes 或监控系统检测服务状态
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController extends BaseHealthController {

    @Resource
    private MatchConfig matchConfig;

    /**
     * 服务信息
     */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", matchConfig.getName());
        result.put("version", matchConfig.getVersion());
        result.put("env", matchConfig.getEnv());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
