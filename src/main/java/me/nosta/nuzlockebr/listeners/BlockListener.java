package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.utils.MapReload;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BlockListener implements Listener {

    private static BlockListener Instance;

    public static BlockListener getInstance() {
        if (Instance == null) Instance = new BlockListener();
        return Instance;
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (GameManager.getInstance().gameState != NZGameState.Playing) {
            event.setCancelled(true);
            return;
        }

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        if (nzPlayer.isWaterTrapped()) {
            event.setCancelled(true);
            return;
        }

        MapReload.addEntry(event.getBlock(), event.getBlock().getType());
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (GameManager.getInstance().gameState != NZGameState.Playing) {
            event.setCancelled(true);
            return;
        }

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        if (nzPlayer.isWaterTrapped()) {
            event.setCancelled(true);
            return;
        }

        BlockState oldBlock = event.getBlockReplacedState();
        if (oldBlock.getType() == Material.WATER) {
            Levelled l = (Levelled) oldBlock.getBlockData();
            if (l.getLevel() == 0) MapReload.addEntry(oldBlock.getBlock(), oldBlock.getType());
            else MapReload.addEntry(oldBlock.getBlock(), Material.AIR);
        }
        else MapReload.addEntry(oldBlock.getBlock(), oldBlock.getType());
    }

    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (GameManager.getInstance().gameState != NZGameState.Playing) {
            event.setCancelled(true);
            return;
        }

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        if (nzPlayer.isWaterTrapped()) {
            event.setCancelled(true);
            return;
        }

        MapReload.addEntry(event.getBlock(), event.getBlock().getType());
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        if (GameManager.getInstance().gameState != NZGameState.Playing) {
            event.setCancelled(true);
            return;
        }

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        if (nzPlayer.isWaterTrapped()) {
            event.setCancelled(true);
            return;
        }

        MapReload.addEntry(event.getBlock(), event.getBlock().getType());
    }
}
