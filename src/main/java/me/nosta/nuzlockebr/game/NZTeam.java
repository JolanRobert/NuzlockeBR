package me.nosta.nuzlockebr.game;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NZTeam {

    private NZPlayer nzPlayer;
    private NZColor nzColor;
    private Team team;

    public NZTeam(NZPlayer nzPlayer) {
        this.nzPlayer = nzPlayer;
        nzColor = NZColor.None;

        Scoreboard mainSc = Bukkit.getScoreboardManager().getMainScoreboard();
        team = mainSc.getTeam(nzPlayer.getName());
        if (team == null) team = mainSc.registerNewTeam(nzPlayer.getName());
        team.addEntry(nzPlayer.getName());

        TeamManager.getInstance().subscribe(this);
    }

    public void destroy() {
        team.unregister();
    }

    public void hideName(boolean state) {
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, state ? Team.OptionStatus.NEVER : Team.OptionStatus.ALWAYS);
        if (state) nzPlayer.getPlayer().setPlayerListName(""+team.getColor()+ChatColor.MAGIC+"aaaaa");
        else nzPlayer.getPlayer().setPlayerListName(nzPlayer.getName());
    }

    public void setPrefix(String prefix) {team.setPrefix(prefix);}
    public void setColor(ChatColor chatColor) {team.setColor(chatColor);}
    public void setSuffix(String suffix) {team.setSuffix(suffix);}

    public Player getPlayer() {return nzPlayer.getPlayer();}
    public ChatColor getChatColor() {return team.getColor();}

    public NZColor getNZColor() {return nzColor;}
    public void setNZColor(NZColor nzColor) {this.nzColor = nzColor;}

    public void resetAll() {
        hideName(false);
    }
}
