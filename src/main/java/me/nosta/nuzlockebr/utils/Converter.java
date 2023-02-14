package me.nosta.nuzlockebr.utils;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.TypeManager;

public class Converter {

    public static NZType StringToNZType(String type) {
        if (type == null) return null;

        if (type.equalsIgnoreCase(Type.None.name())) return TypeManager.getInstance().noneType;

        for (NZType nzType : TypeManager.getInstance().typeList) {
            if (type.equalsIgnoreCase(nzType.getName())) return nzType;
        }
        return null;
    }

    public static NZPlayer StringToNZPlayer(String playerName) {
        if (playerName == null) return null;

        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            if (playerName.equalsIgnoreCase(nzPlayer.getName())) return nzPlayer;
        }
        return null;
    }
}
