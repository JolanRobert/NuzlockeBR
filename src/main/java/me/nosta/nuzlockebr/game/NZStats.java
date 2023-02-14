package me.nosta.nuzlockebr.game;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.utils.Broadcaster;
import me.nosta.nuzlockebr.utils.Disguiser;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;

public class NZStats {

    private NZPlayer nzPlayer;

    private int bounty, points;
    private int killStreak, assistStreak;
    private int totalKills, totalDeaths, totalAssists;

    private int killValue = 20;
    private int assistValue = 10;
    private int suicideValue = -15;

    public NZStats(NZPlayer nzPlayer) {
        this.nzPlayer = nzPlayer;
    }

    public void addKill(NZPlayer nzDead) {
        totalKills++;
        killStreak++;

        int enemyBounty = nzDead.getNZStats().getBounty();
        int killStreakValue = getKillStreakValue();
        int killPoints = killValue+enemyBounty+killStreakValue;

        killInfo(nzDead);

        gainPoints(killPoints);
        increaseBounty();
    }

    public void addAssist(NZPlayer nzDead) {
        totalAssists++;
        assistStreak++;

        int enemyBounty = nzDead.getNZStats().getBounty();
        int assistStreakValue = getAssistStreakValue();
        int assistPoints = assistValue+enemyBounty/2+assistStreakValue;

        if (nzPlayer.getNZType().getType() == Type.Fee) assistPoints *= 1.5f;

        assistInfo(nzDead);

        gainPoints(assistPoints);
        increaseBounty();
    }

    public void addDeath(NZPlayer nzKiller) {
        totalDeaths++;

        //Suicide
        if (nzKiller == null) {
            int suicideMalus = suicideValue-getBounty();
            gainPoints(suicideMalus);
            suicideInfo();
        }
        else deathInfo(nzKiller);

        resetBounty();
        killStreak = 0;
        assistStreak = 0;
    }

    public int getKillStreakValue() {
        if (killStreak >= 3) return Math.min(10+2*(killStreak-3),20);
        else return 0;
    }

    public int getAssistStreakValue() {
        if (assistStreak >= 3) return Math.min(5+2*(assistStreak-3),15);
        else return 0;
    }

    public void gainPoints(int amount) {
        points += amount;

        if (nzPlayer.getTeam() == NZColor.None) ScoreManager.getInstance().updatePoints(nzPlayer,amount);
        else ScoreManager.getInstance().updatePoints(nzPlayer.getTeam(),amount);
    }

    public void increaseBounty() {
        if (killStreak < 3 && assistStreak < 3) return;
        bounty = 10*killStreak+5*assistStreak;
        if (bounty < 0) bounty = 0;
        nzPlayer.setBounty(bounty);
    }

    public void resetBounty() {
        bounty = 0;
        nzPlayer.setBounty(bounty);
    }
    public int getBounty() {return bounty;}

    public void resetAll() {
        resetBounty();
        points = 0;
        killStreak = 0;
        assistStreak = 0;
        totalKills = 0;
        totalDeaths = 0;
        totalAssists = 0;
    }

    //MESSAGE INFO

    private void killInfo(NZPlayer nzDead) {
        int enemyBounty = nzDead.getNZStats().getBounty();
        int killStreakValue = getKillStreakValue();
        int killPoints = killValue+enemyBounty+killStreakValue;

        String killType = enemyBounty > 0 ? ChatColor.GOLD+"SHUTDOWN " : ChatColor.GREEN+"KILL ";
        String deadName = GameManager.getInstance().isAnonymous ? ""+ChatColor.WHITE+ChatColor.MAGIC+"aaaaa" : nzDead.getColoredName();
        TextComponent msg = new TextComponent(killType+deadName);

        TextComponent pointDesc = new TextComponent(ChatColor.AQUA+" (+"+killPoints+"pts)");
        pointDesc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                "Kill : "+killValue+"pts\n"+
                "KillStreak : "+killStreakValue+"pts\n"+
                "Bounty : "+enemyBounty+"pts"
        )));

        msg.addExtra(pointDesc);
        nzPlayer.getPlayer().spigot().sendMessage(msg);

        String deathIcon = ""+ChatColor.WHITE;
        deathIcon += nzDead.getPlayer().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.PROJECTILE ? " \u2916 " : " \u2694 ";
        Broadcaster.messageOthers(nzPlayer.getColoredName()+deathIcon+nzDead.getColoredName(), Arrays.asList(nzPlayer.getPlayer(), nzDead.getPlayer()));
    }

    private void deathInfo(NZPlayer nzKiller) {
        int killerHealthAmount = nzKiller.getHealth();
        ChatColor deathColor;
        if (killerHealthAmount >= 18) deathColor = ChatColor.GREEN;
        else if (killerHealthAmount >= 10) deathColor = ChatColor.YELLOW;
        else if (killerHealthAmount >= 3) deathColor = ChatColor.RED;
        else deathColor = ChatColor.DARK_RED;
        String killerHealth = " "+deathColor+"("+killerHealthAmount+"\u2764)";

        nzPlayer.sendMessage(ChatColor.RED+"DEATH "+nzKiller.getColoredName()+ChatColor.RED+killerHealth);
    }

    private void suicideInfo() {
        int suicideMalus = suicideValue-getBounty();

        nzPlayer.sendMessage(ChatColor.RED+"DEATH \u2620 ("+suicideMalus+"pts)");
        Broadcaster.messageOthers("\u2620 "+nzPlayer.getColoredName(), Arrays.asList(nzPlayer.getPlayer()));
    }

    private void assistInfo(NZPlayer nzDead) {
        int enemyBounty = nzDead.getNZStats().getBounty();
        int assistStreakValue = getAssistStreakValue();
        int assistPoints = assistValue+enemyBounty/2+assistStreakValue;

        if (nzPlayer.getNZType().getType() == Type.Fee) assistPoints *= 1.5f;

        String deadName = GameManager.getInstance().isAnonymous ? ""+ChatColor.WHITE+ChatColor.MAGIC+"aaaaa" : nzDead.getColoredName();
        TextComponent msg = new TextComponent(ChatColor.GREEN+"ASSIST "+deadName);
        TextComponent pointDesc = new TextComponent(ChatColor.AQUA+" (+"+assistPoints+"pts)");
        pointDesc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                "Assist : "+assistValue+"pts\n"+
                "AssistStreak : "+assistStreakValue+"pts\n"+
                "Bounty : "+enemyBounty/2+"pts"
        )));

        msg.addExtra(pointDesc);
        nzPlayer.getPlayer().spigot().sendMessage(msg);
    }
}
