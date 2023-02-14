package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.game.NZPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PlayerManager {

    private static PlayerManager Instance;
    public static PlayerManager getInstance() {
        if (Instance == null) Instance = new PlayerManager();
        return Instance;
    }

    public List<NZPlayer> playerList = new ArrayList<>();

    public void addPlayer(Player player) {
        NZPlayer nzPlayer = new NZPlayer(player);
        playerList.add(nzPlayer);

        InventoryManager.getInstance().updateConfig();
    }

    public void removePlayer(Player player) {
        for (NZPlayer item : playerList) {
            if (item.getPlayer() != player) continue;
            item.getNZTeam().destroy();
            playerList.remove(item);
            break;
        }

        InventoryManager.getInstance().updateConfig();
    }

    public void resetPlayerList() {
        for (NZPlayer nzPlayer : playerList) nzPlayer.resetAll();
    }

    public NZPlayer getNZPlayer(Player player) {
        for (NZPlayer nzPlayer : playerList) {
            if (nzPlayer.getPlayer() == player) return nzPlayer;
        }
        return null;
    }

    //Return list of non setup players
    public HashMap<NZPlayer,String> getUnreadyPlayers() {
        HashMap<NZPlayer,String> unreadyPlayers = new HashMap<>();

        for (NZPlayer nzPlayer : playerList) {
            if (nzPlayer.getPvpLevel() == 0) unreadyPlayers.put(nzPlayer,"Pvp Level not set !");
            if (GameManager.getInstance().gameMode == NZGameMode.FFA) continue;
            if (nzPlayer.getTeam() == NZColor.None) unreadyPlayers.put(nzPlayer,"Team not selected !");
        }

        return unreadyPlayers;
    }

    public List<NZPlayer> getNZPlayersInRange(NZPlayer centralPlayer, int radius, boolean takeMates) {
        List<NZPlayer> players = new ArrayList<>();
        for (NZPlayer item : playerList) {
            if (item == centralPlayer) continue;
            if (GameManager.getInstance().gameMode == NZGameMode.Team && item.getTeam() == centralPlayer.getTeam() && !takeMates) continue;
            if (item.getLocation().distance(centralPlayer.getLocation()) <= radius) players.add(item);
        }
        return players;
    }

    public NZPlayer getRandomPlayer(List<NZPlayer> exceptions) {
        List<NZPlayer> players = new ArrayList<>();
        for (NZPlayer item : playerList) {
            if (exceptions.contains(item)) continue;
            players.add(item);
        }

        Random rdm = new Random();
        return  players.get(rdm.nextInt(players.size()));
    }
}
