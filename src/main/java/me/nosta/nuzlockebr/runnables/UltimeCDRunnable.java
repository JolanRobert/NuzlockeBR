package me.nosta.nuzlockebr.runnables;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.game.NZType;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UltimeCDRunnable extends BukkitRunnable {

    private NZType nzType;

    public UltimeCDRunnable(NZType nzType) {
        this.nzType = nzType;
        runTaskTimer(Main.getInstance(),20,20);
    }

    @Override
    public void run() {
        if (nzType.getUltimeTimeLeft() > 0) nzType.setUltimeTimeLeft(nzType.getUltimeTimeLeft()-1);
        else if (nzType.getUltimeLocktime() > 0) return;
        else if (nzType.getUltimeCooldown() > 0) nzType.setUltimeCooldown(nzType.getUltimeCooldown()-1);

        if (nzType.getUltimeTimeLeft() == 0 && nzType.getUltimeCooldown() == 0) {
            Player player = nzType.getNZPlayer().getPlayer();
            player.sendMessage(ChatColor.GREEN+"Ultime rechargé !");
            player.playSound(player.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,Integer.MAX_VALUE,1);
            this.cancel();
        }
    }
}
