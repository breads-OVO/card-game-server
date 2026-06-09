package com.card.game.common.exception;

import com.card.game.common.constant.CodeEnum;
import lombok.Getter;

/**
 * 业务异常（可控异常，用于提示用户）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(CodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }

    public BusinessException(CodeEnum codeEnum, String message) {
        super(message);
        this.code = codeEnum.getCode();
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(CodeEnum codeEnum, Throwable cause) {
        super(codeEnum.getMessage(), cause);
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }
}