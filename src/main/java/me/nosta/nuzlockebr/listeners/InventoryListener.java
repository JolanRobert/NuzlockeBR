package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.InventoryManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.utils.Broadcaster;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private static InventoryListener Instance;

    public static InventoryListener getInstance() {
        if (Instance == null) Instance = new InventoryListener();
        return Instance;
    }

    private InventoryManager inv;

    public InventoryListener() {
        inv = InventoryManager.getInstance();
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        //If clicked elsewhere
        if(event.getClickedInventory() == null) return;

        //If empty slot
        if(event.getClickedInventory().getItem(event.getSlot()) == null) return;

        //If player inventory
        if((event.getClickedInventory().getHolder() instanceof Player)) return;

        //Custom Inventory
        event.setCancelled(true);
        Player player = (Player)event.getWhoClicked();

        if (event.getInventory() == inv.getConfigInventory()) onClickConfig(event);
        else if (event.getInventory() == inv.getTeamInventory()) onClickTeam(event);
    }

    private void onClickConfig(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
        }

        else if (clickedItem.getType() == Material.SPYGLASS) {
            GameManager.getInstance().isAnonymous = !GameManager.getInstance().isAnonymous;
            Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"Anonymous : "+ChatColor.BOLD+
                    (GameManager.getInstance().isAnonymous ? ChatColor.GREEN+"ON" : ChatColor.RED+"OFF"));
        }

        else if (clickedItem.getType() == Material.PAPER) {
            if (GameManager.getInstance().gameMode == NZGameMode.FFA) {
                GameManager.getInstance().setGameMode(NZGameMode.Team);
            }
            else {
                GameManager.getInstance().setGameMode(NZGameMode.FFA);
            }

            Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"Mode de jeu : "+GameManager.getInstance().gameMode);
        }

        else if (clickedItem.getType() == Material.PLAYER_HEAD) {
            Player clickedPlayer = Bukkit.getPlayer(event.getCurrentItem().getItemMeta().getDisplayName());
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(clickedPlayer);
            nzPlayer.addPvpLevel();

            Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+nzPlayer.getName()+" >> Level : "+nzPlayer.getPvpLevel());
        }

        inv.updateConfig();
    }

    private void onClickTeam(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
        }

        else if (clickedItem.getType() == Material.RED_WOOL) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            nzPlayer.setTeam(NZColor.Red);

            nzPlayer.getPlayer().getInventory().getItemInMainHand().setType(Material.RED_BANNER);
        }

        else if (clickedItem.getType() == Material.BLUE_WOOL) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            nzPlayer.setTeam(NZColor.Blue);

            nzPlayer.getPlayer().getInventory().getItemInMainHand().setType(Material.BLUE_BANNER);
        }

        inv.updateTeam();
    }
}
