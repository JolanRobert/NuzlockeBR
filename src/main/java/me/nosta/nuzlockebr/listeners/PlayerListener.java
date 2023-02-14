package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.InventoryManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.nztypes.Feu;
import me.nosta.nuzlockebr.nztypes.Glace;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {

    private static PlayerListener Instance;

    public static PlayerListener getInstance() {
        if (Instance == null) Instance = new PlayerListener();
        return Instance;
    }

    private List<Material> bannerList;

    public PlayerListener() {
        bannerList = new ArrayList<>();
        bannerList.add(Material.WHITE_BANNER);
        bannerList.add(Material.RED_BANNER);
        bannerList.add(Material.BLUE_BANNER);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) event.setCancelled(true);
        Material droppedItem = event.getItemDrop().getItemStack().getType();
        if (droppedItem != Material.GOLDEN_APPLE && droppedItem != Material.COBBLESTONE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack clickedItem = player.getInventory().getItemInMainHand();
        if (clickedItem.getType() == Material.AIR) return;

        //Team banner
        if (bannerList.contains(clickedItem.getType())) {
            player.openInventory(InventoryManager.getInstance().getTeamInventory());
        }

        //Ultime
        else if (clickedItem.getType() == Material.NETHER_STAR) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            if (nzPlayer == null) return;

            nzPlayer.getNZType().triggerUltime();
        }

        //Création de la Lumière
        else if (clickedItem.getType() == Material.SNOWBALL) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            if (nzPlayer == null) return;

            CreationLumiere.getInstance().activeCreation(nzPlayer,clickedItem);
        }

        //Création des Ténèbres
        else if (clickedItem.getType() == Material.ENDER_EYE) {
            NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
            if (nzPlayer == null) return;

            CreationTenebres.getInstance().activeCreation(nzPlayer,clickedItem);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player)event.getEntity();

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
        if (nzPlayer == null) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () ->
                ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth()),1);
    }

    @EventHandler
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player)event.getEntity();

        if (event.getNewEffect() != null) if (event.getNewEffect().getType().equals(PotionEffectType.ABSORPTION)) return;
        else if (event.getOldEffect() != null) if (event.getOldEffect().getType().equals(PotionEffectType.ABSORPTION)) return;
        else return;

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
        if (nzPlayer == null) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () ->
                ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth()),1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        Player player = event.getPlayer();
        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (nzPlayer.isFreeze && !nzPlayer.hasEffect(PotionEffectType.LEVITATION)) {
            double yVelocity = player.getVelocity().getY();
            if (yVelocity > 0) yVelocity = -yVelocity;
            player.setVelocity(new Vector(player.getVelocity().getX(),yVelocity,player.getVelocity().getZ()));
        }

        else if (nzPlayer.isFreeze && player.isOnGround()) {
            player.teleport(new Location(player.getWorld(),from.getX(),to.getY(),from.getZ(),to.getYaw(),to.getPitch()));
        }

        //if (player.getVelocity().getY() > 5) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        ItemStack clickedItem = nzPlayer.getPlayer().getInventory().getItemInMainHand();
        if (clickedItem.getType() != Material.GOLDEN_APPLE) return;

        Type playerType = nzPlayer.getNZType().getType();
        if (playerType == Type.Acier) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () ->
                    nzPlayer.setAbsorption(2),1);
        }

        else if (playerType == Type.Glace) {
            Glace glace = (Glace) nzPlayer.getNZType();
            if (glace.shieldHealth <= 0) return;
            double shieldAbso = Math.round(glace.shieldHealth * 2) / 2.0;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () ->
                    nzPlayer.setAbsorption(shieldAbso+4),1);
        }

        else if (playerType == Type.Normal) {
            nzPlayer.clearEffect(PotionEffectType.REGENERATION);
            nzPlayer.addEffect(PotionEffectType.REGENERATION,7.5f,2);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () ->
                ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth()),1);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(event.getPlayer());
        if (nzPlayer == null) return;

        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (nzPlayer.getPlayer() == event.getCaught()) return;

        Type playerType = nzPlayer.getNZType().getType();
        if (playerType == Type.Eau) {
            if (!(event.getCaught() instanceof Player)) return;
            Player player = (Player) event.getCaught();

            Vector direction = nzPlayer.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            double distance = nzPlayer.getLocation().distance(player.getLocation());
            float strength = (float)(distance/5);
            player.setVelocity(direction.multiply(strength));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        if (!(event.getHitEntity() instanceof Player)) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        NZPlayer attacker = PlayerManager.getInstance().getNZPlayer((Player) event.getEntity().getShooter());
        NZPlayer victim = PlayerManager.getInstance().getNZPlayer((Player) event.getHitEntity());

        if (attacker == null || victim == null) return;
        if (attacker == victim) return;

        if (GameManager.getInstance().gameMode == NZGameMode.Team && victim.getTeam() == attacker.getTeam()) {
            event.setCancelled(true);
            return;
        }

        NZType nzType = attacker.getNZType();
        if (nzType.getType() == Type.Feu) {
            if (victim.getPlayer().getFireTicks() > 0) ((Feu)nzType).nextFireArrow = true;
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl
                || event.getEntity() instanceof EnderSignal
                || event.getEntity() instanceof Snowball) event.setCancelled(true);
    }
}
