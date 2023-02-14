package me.nosta.nuzlockebr.managers;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;

public class WorldBorderManager {

    private static WorldBorderManager Instance;
    public static WorldBorderManager getInstance() {
        if (Instance == null) Instance = new WorldBorderManager();
        return Instance;
    }

    private WorldBorder worldborder;

    public void setWorldBorder(int size) {
        worldborder = Bukkit.getServer().getWorld("world").getWorldBorder();
        worldborder.setCenter(0,0);
        worldborder.setSize(size*2);
        worldborder.setWarningDistance(0);
    }

    public void removeWorldBorder() {
        worldborder.reset();
    }
}
