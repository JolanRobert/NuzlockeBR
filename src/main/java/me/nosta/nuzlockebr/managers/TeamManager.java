package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZTeam;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class TeamManager {

    private static TeamManager Instance;
    public static TeamManager getInstance() {
        if (Instance == null) Instance = new TeamManager();
        return Instance;
    }

    private List<NZTeam> teamList = new ArrayList<>();

    public void init() {
        //Destroy all teams
        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) team.unregister();
    }

    public void subscribe(NZTeam nzTeam) {
        teamList.add(nzTeam);
    }

    public List<Player> getPlayers(NZColor nzColor) {
        List<Player> ret = new ArrayList<>();
        for (NZTeam nzTeam : teamList) {
            if (nzTeam.getNZColor() != nzColor) continue;
            ret.add(nzTeam.getPlayer());
        }
        return ret;
    }

    //Give all players TeamBanner which allow to join teams
    public void giveTeamBanner() {
        ItemStack teamBanner = ItemEditor.getNewItem(Material.WHITE_BANNER,ChatColor.GOLD+"Teams");
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.getPlayer().getInventory().addItem(teamBanner);
        }
    }
}
