package me.nosta.nuzlockebr.utils;

import me.nosta.nuzlockebr.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class Broadcaster {

    public static void messageOthers(String message, List<Player> exceptions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (exceptions.contains(player)) continue;
            if (GameManager.getInstance().isAnonymous) message = Disguiser.anonymizeMessage(message);
            player.sendMessage(message);
        }
    }

    public static void messageAllOps(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isOp()) continue;
            player.sendMessage(message);
        }
    }

    public static void titleAll(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title,subtitle,fadeIn,stay,fadeOut);
        }
    }

    public static void soundAll(Sound sound, int volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}
