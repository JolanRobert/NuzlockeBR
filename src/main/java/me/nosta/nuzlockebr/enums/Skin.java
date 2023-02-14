package me.nosta.nuzlockebr.enums;

import java.util.Random;

public enum Skin {
    Asuka,
    Spy,
    Flyde,
    Waluigi,
    Reset;

    public static Skin getRandomSkin() {
        Random rdm = new Random();
        Skin[] skins = Skin.values();
        return skins[rdm.nextInt(skins.length-1)];
    }
}
