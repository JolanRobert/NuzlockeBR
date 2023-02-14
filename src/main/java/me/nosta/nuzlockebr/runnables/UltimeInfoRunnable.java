package me.nosta.nuzlockebr.runnables;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class UltimeInfoRunnable extends BukkitRunnable {

    private NZType nzType;

    public UltimeInfoRunnable() {
        runTaskTimer(Main.getInstance(),0,2);
    }

    @Override
    public void run() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzType = nzPlayer.getNZType();

            //Ulti activé
            if (nzType.getUltimeTimeLeft() > 0) {
                sendMessage(nzPlayer,ChatColor.AQUA+"Ultime : Activé ("+nzType.getUltimeTimeLeft()+"s)");
            }

            //Ulti locked
            else if (nzType.getUltimeLocktime() > 0) {
                sendMessage(nzPlayer,ChatColor.RED+"Ultime : Bloqué ("+nzType.getUltimeLocktime()+"s)");
            }

            //Ulti cooldown
            else if (nzType.getUltimeCooldown() > 0) {
                sendMessage(nzPlayer,ChatColor.YELLOW+"Ultime : Recharge ("+nzType.getUltimeCooldown()+"s)");
            }

            //Ulti dispo
            else {
                sendMessage(nzPlayer,ChatColor.GREEN+"Ultime : Disponible");
            }
        }
    }

    private void sendMessage(NZPlayer nzPlayer, String message) {
        nzPlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
