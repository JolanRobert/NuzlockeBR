package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private static InventoryManager Instance;
    public static InventoryManager getInstance() {
        if (Instance == null) Instance = new InventoryManager();
        return Instance;
    }

    private Inventory configInventory;
    private Inventory teamInventory;

    public void updateConfig() {
        if (configInventory == null) configInventory = Bukkit.createInventory(null, 18, "PvP Level");
        configInventory.clear();

        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            ItemStack head = nzPlayer.getPlayerHead();
            configInventory.addItem(head);
        }

        ItemStack anonymous;
        if (GameManager.getInstance().isAnonymous) {
            anonymous = ItemEditor.getNewItem(Material.SPYGLASS,ChatColor.GOLD+"Anonymous : "+ChatColor.GREEN+"ON");
        }
        else {
            anonymous = ItemEditor.getNewItem(Material.SPYGLASS,ChatColor.GOLD+"Anonymous : "+ChatColor.RED+"OFF");
        }
        configInventory.setItem(15,anonymous);

        ItemStack team;
        if (GameManager.getInstance().gameMode == NZGameMode.FFA) {
            team = ItemEditor.getNewItem(Material.PAPER,ChatColor.GOLD+"Mode de jeu : FFA");
        }
        else {
            team = ItemEditor.getNewItem(Material.PAPER,ChatColor.GOLD+"Mode de jeu : Team");
        }
        configInventory.setItem(16,team);

        ItemStack close = ItemEditor.getNewItem(Material.BARRIER,ChatColor.RED+""+ChatColor.BOLD+"Annuler");
        configInventory.setItem(17, close);
    }

    public void updateTeam() {
        if (teamInventory == null) teamInventory = Bukkit.createInventory(null, 9, "Teams");
        teamInventory.clear();

        ItemStack redTeam = teamItem(Material.RED_WOOL,ChatColor.RED+"Red",NZColor.Red);
        teamInventory.addItem(redTeam);

        ItemStack blueTeam = teamItem(Material.BLUE_WOOL,ChatColor.BLUE+"Blue",NZColor.Blue);
        teamInventory.addItem(blueTeam);

        ItemStack close = ItemEditor.getNewItem(Material.BARRIER,ChatColor.RED+""+ChatColor.BOLD+"Annuler");
        configInventory.setItem(8, close);
    }

    private ItemStack teamItem(Material mat, String displayName, NZColor nzColor) {
        List<String> players = new ArrayList<>();
        ChatColor chatColor = nzColor.getColor();
        for (Player player : TeamManager.getInstance().getPlayers(nzColor)) {
            players.add(chatColor+"- "+player.getName());
        }

        ItemStack team = ItemEditor.getNewItem(mat,displayName+" ("+players.size()+")",players);
        return team;
    }

    public void closeInventory(Inventory invToClose) {
        List<HumanEntity> entities = invToClose.getViewers();
        for (int i = entities.size()-1; i >= 0; i--) {
            entities.get(i).closeInventory();
        }
    }

    public Inventory getConfigInventory() {
        updateConfig();
        return configInventory;
    }
    public Inventory getTeamInventory() {
        updateTeam();
        return teamInventory;
    }
}
