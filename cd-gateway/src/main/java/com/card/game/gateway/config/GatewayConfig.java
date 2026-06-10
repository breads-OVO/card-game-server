package com.card.game.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class GatewayConfig {

    /** 服务名称 */
    private String name ;

    /** 服务版本 */
    private String version ;

    /** 环境标识 */
    private String env ;
}