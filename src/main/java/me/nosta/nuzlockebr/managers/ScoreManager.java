package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreManager {

    private Scoreboard mainSc;
    private Objective points, playerHealth;

    private Score target;
    private int targetValue = 200;

    private static ScoreManager Instance;
    public static ScoreManager getInstance() {
        if (Instance == null) Instance = new ScoreManager();
        return Instance;
    }

    public void init() {
        mainSc = Bukkit.getScoreboardManager().getMainScoreboard();

        //Destroy all objectives
        for (Objective obj : mainSc.getObjectives()) obj.unregister();

        points = mainSc.registerNewObjective("Points", "dummy", ChatColor.GOLD+"Points");
        points.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerHealth = mainSc.registerNewObjective("PlayerHealth", "dummy", ChatColor.LIGHT_PURPLE+"\u2764");
        playerHealth.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public void initPoints() {
        target = points.getScore(""+ChatColor.RED+ChatColor.BOLD+"Target");
        target.setScore(targetValue);

        if (GameManager.getInstance().gameMode == NZGameMode.FFA) {
            for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
                Score playerScore;
                if (!GameManager.getInstance().isAnonymous) playerScore = points.getScore(nzPlayer.getColoredName());
                else {
                    Type type = nzPlayer.getNZType().getType();
                    playerScore = points.getScore(""+type.getColor()+type);
                }

                playerScore.setScore(0);
            }
        }

        else if (GameManager.getInstance().gameMode == NZGameMode.Team) {
            Score redScore = points.getScore(ChatColor.RED+"Red");
            redScore.setScore(0);

            Score blueScore = points.getScore(ChatColor.BLUE+"Blue");
            blueScore.setScore(0);
        }
    }

    //FFA
    public void updatePoints(NZPlayer nzPlayer, int pointsValue) {
        Score playerScore;
        if (!GameManager.getInstance().isAnonymous) playerScore = points.getScore(nzPlayer.getColoredName());
        else {
            Type type = nzPlayer.getNZType().getType();
            playerScore = points.getScore(""+type.getColor()+type);
        }

        playerScore.setScore(playerScore.getScore()+pointsValue);
        if (playerScore.getScore() >= target.getScore()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> GameManager.getInstance().endGame(),1);
        }
    }

    //Team
    public void updatePoints(NZColor nzColor, int pointsValue) {
        Score score = nzColor == NZColor.Red ? points.getScore(ChatColor.RED+"Red") : points.getScore(ChatColor.BLUE+"Blue");
        score.setScore(score.getScore()+pointsValue);
        if (score.getScore() >= target.getScore()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> GameManager.getInstance().endGame(),1);
        }
    }

    public void initHealth() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            Score score = playerHealth.getScore(nzPlayer.getName());
            score.setScore((int)(nzPlayer.getMaxHealth()));
        }
    }

    public void updateHealth(NZPlayer nzPlayer, int healthValue) {
        Score score = playerHealth.getScore(nzPlayer.getName());
        score.setScore(healthValue);
    }

    public void updateTarget(int value) {
        targetValue = value;
    }

    public void resetScoreboard() {
        for (String entry : mainSc.getEntries()) {
            mainSc.resetScores(entry);
        }
    }
}
