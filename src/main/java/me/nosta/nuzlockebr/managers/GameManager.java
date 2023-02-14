package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.nztypes.capacity.ArmorVisibility;
import me.nosta.nuzlockebr.runnables.DayNightRunnable;
import me.nosta.nuzlockebr.runnables.StartGameRunnable;
import me.nosta.nuzlockebr.runnables.UltimeInfoRunnable;
import me.nosta.nuzlockebr.utils.Disguiser;
import me.nosta.nuzlockebr.utils.Glower;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameManager {

    private static GameManager Instance;
    public static GameManager getInstance() {
        if (Instance == null) Instance = new GameManager();
        return Instance;
    }

    public NZGameState gameState;
    public NZGameMode gameMode;
    public boolean isAnonymous;

    public final int cycleTime = 90;

    public GameManager() {
        gameState = NZGameState.Waiting;
        gameMode = NZGameMode.FFA;
    }

    public void init() {
        TeamManager.getInstance().init();
        ScoreManager.getInstance().init();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerManager.getInstance().addPlayer(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void prepareGame() {
        gameState = NZGameState.Starting;
        Glower.initGlow();

        ScoreManager.getInstance().resetScoreboard();
        WorldBorderManager.getInstance().setWorldBorder(12*PlayerManager.getInstance().playerList.size());

        new StartGameRunnable();
    }

    public void startGame() {
        GameManager.getInstance().gameState = NZGameState.Playing;
        new UltimeInfoRunnable();
        new DayNightRunnable(cycleTime);
        new BukkitRunnable() {
            @Override
            public void run() {
                Glower.refreshGlow();
                ArmorVisibility.refreshInvisibility();
            }
        }.runTaskTimer(Main.getInstance(),0,1);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
                    nzPlayer.getNZType().checkEffects();
                    nzPlayer.getNZType().passivePower();
                }
            }
        }.runTaskTimer(Main.getInstance(),0,2);
    }

    public void endGame() {
        gameState = NZGameState.Waiting;

        Bukkit.getScheduler().cancelTasks(Main.getInstance());
        PlayerManager.getInstance().resetPlayerList();

        WorldBorderManager.getInstance().removeWorldBorder();

        if (gameMode == NZGameMode.Team) {
            TeamManager.getInstance().giveTeamBanner();
        }

        ArmorVisibility.clearAllInvisibles();
        Glower.clearAllGlow();
        Disguiser.clearAllDisguise();
    }

    public void setGameMode(NZGameMode newMode) {
        gameMode = newMode;
        if (newMode == NZGameMode.FFA) {
            for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
                nzPlayer.setTeam(NZColor.None);

                InventoryManager.getInstance().closeInventory(InventoryManager.getInstance().getTeamInventory());

                nzPlayer.getPlayer().getInventory().clear();
            }
        }
        else if (newMode == NZGameMode.Team) {
            TeamManager.getInstance().giveTeamBanner();
        }

        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.updateName();
        }
    }
}
