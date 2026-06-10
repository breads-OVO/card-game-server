package com.card.game.gateway.service;

import com.card.game.proto.common.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理器工厂
 * 根据 MessageType 前缀自动分发到对应的处理器
 */
@Slf4j
@Component
public class MessageHandlerFactory {

    private final Map<String, MessageHandler> handlerMap = new HashMap<>();

    /**
     * 构造函数注入所有 MessageHandler 实现类并自动注册
     */
    public MessageHandlerFactory(List<MessageHandler> handlers) {
        for (MessageHandler handler : handlers) {
            String prefix = handler.getMessageTypePrefix();
            handlerMap.put(prefix, handler);
            log.info("注册消息处理器: prefix={}, handler={}", prefix, handler.getClass().getSimpleName());
        }
        log.info("消息处理器工厂初始化完成，共注册 {} 个处理器", handlerMap.size());
    }

    /**
     * 根据消息类型获取对应的处理器
     * @param messageType 消息类型枚举
     * @return 消息处理器，如果没有找到则返回 null
     */
    public MessageHandler getHandler(MessageType messageType) {
        if (messageType == null || messageType == MessageType.UNKNOWN) {
            return null;
        }

        String prefix = getMessageTypePrefix(messageType);
        MessageHandler handler = handlerMap.get(prefix);

        if (handler == null) {
            log.warn("未找到消息类型 {} 的处理器，prefix={}", messageType, prefix);
        }

        return handler;
    }

    /**
     * 从 MessageType 枚举名称中提取前缀
     * 例如: AUTH_LOGIN_REQUEST -> AUTH
     *       AGENT_PLAYER_ONLINE_REQUEST -> AGENT
     */
    private String getMessageTypePrefix(MessageType messageType) {
        String name = messageType.name();
        int underscoreIndex = name.indexOf('_');
        if (underscoreIndex > 0) {
            return name.substring(0, underscoreIndex);
        }
        return name;
    }
}
