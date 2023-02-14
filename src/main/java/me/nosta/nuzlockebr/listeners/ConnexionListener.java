package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnexionListener implements Listener {

    private static ConnexionListener Instance;

    public static ConnexionListener getInstance() {
        if (Instance == null) Instance = new ConnexionListener();
        return Instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        event.setJoinMessage(joinMessage(player));

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        if (GameManager.getInstance().gameState == NZGameState.Waiting) {
            player.setGameMode(GameMode.SURVIVAL);
            int ground = player.getWorld().getHighestBlockYAt(0,0);
            player.teleport(new Location(player.getWorld(), 0.5, ground+1, 0.5, 0, 0));

            PlayerManager.getInstance().addPlayer(player);
        }
        else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(quitMessage(player));

        if (GameManager.getInstance().gameState == NZGameState.Waiting) {
            PlayerManager.getInstance().removePlayer(player);
        }
    }

    public String joinMessage(Player player) {
        String msg = ChatColor.DARK_GRAY+"["+ChatColor.GREEN+"+"+ChatColor.DARK_GRAY+"] ";
        msg += player.getName()+" ";
        msg += ChatColor.GRAY+"("+ChatColor.YELLOW+(Bukkit.getOnlinePlayers().size());
        msg += ChatColor.GRAY+"/"+ChatColor.YELLOW+Bukkit.getMaxPlayers()+ChatColor.GRAY+")";
        return msg;
    }

    public String quitMessage(Player player) {
        String msg = ChatColor.DARK_GRAY+"["+ChatColor.RED+"-"+ChatColor.DARK_GRAY+"] ";
        msg += player.getName()+" ";
        msg += ChatColor.GRAY+"("+ChatColor.YELLOW+(Bukkit.getOnlinePlayers().size()-1);
        msg += ChatColor.GRAY+"/"+ChatColor.YELLOW+Bukkit.getMaxPlayers()+ChatColor.GRAY+")";
        return msg;
    }
}
