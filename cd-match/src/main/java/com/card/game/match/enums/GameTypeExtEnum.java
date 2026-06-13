package com.card.game.match.enums;

import com.card.game.proto.game.GameType;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum GameTypeExtEnum {
    UNKNOWN(GameType.GAME_TYPE_UNKNOWN, "未知", "unknown", 0),
    DOU_DI_ZHU(GameType.GAME_TYPE_DOU_DI_ZHU, "斗地主", "dou_di_zhu", 3),
    JUN_ZHENG(GameType.GAME_TYPE_JUN_ZHENG, "军争", "jun_zheng", 8)
    ;
    private final GameType gameType;
    private final String name;
    private final String key;
    private final Integer playersNeeded;

    GameTypeExtEnum(GameType gameType, String name, String key, Integer playersNeeded) {
        this.gameType = gameType;
        this.name = name;
        this.key = key;
        this.playersNeeded = playersNeeded;
    }

    public static GameTypeExtEnum getByGameType(GameType gameType) {
        for (GameTypeExtEnum value : values()) {
            if (value.gameType == gameType) {
                return value;
            }
        }
        return UNKNOWN;
    }

    public static List<String> getAllGameTypesKeys() {
        List<String> gameTypes = new ArrayList<>();
        for (GameTypeExtEnum value : values()){
            if (StringUtils.isNotEmpty(value.key)){
                gameTypes.add(value.key);
            }
        }
        return gameTypes;
    }

    public static List<GameTypeExtEnum> getAllGameTypes() {
        List<GameTypeExtEnum> gameTypes = new ArrayList<>();
        for (GameTypeExtEnum value : values()){
            if (value.gameType != GameType.GAME_TYPE_UNKNOWN){
                gameTypes.add(value);
            }
        }
        return gameTypes;
    }

}
