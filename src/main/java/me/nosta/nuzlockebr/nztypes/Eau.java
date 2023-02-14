package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.MapReload;
import me.nosta.nuzlockebr.utils.Structure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Eau extends NZType {

    public Eau() {
        super(Type.Eau);
        this.ultimeName = "Siphon";
    }

    private final int ultimeTime = 20;

    private final float waterBowChance = 0.25f;

    private Location sphereCenter;
    private final int sphereRadius = 10;
    private final int height = 30;

    public boolean inWater;

    List<NZPlayer> trappedPlayers = new ArrayList<>();
    List<Integer> tasks = new ArrayList<>();

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Apnée/Grâce du Dauphin et +2\u2764.")+
                addDescLine("Vous possédez les enchantements Affinité/Agilité Aquatique II.")+
                addDescLine("Vos flèches ont 25% de chance de faire apparaître une source d'eau en touchant un ennemi.")+
                addDescLine("Vous disposez d'une canne à pêche permettant d'attirer les joueurs vers vous si vous les touchez.")+
                addDescLine("Vous avez Force 1 et Vision Nocturne si vous êtes dans l'eau ainsi qu'un effet de Vitesse 1 résiduel une fois sorti.")+
                " \n"+
                addDescUlti("Dans un rayon de 10 blocs vous téléportez tout vos ennemis dans une sphère d'eau géante et leur infligez Cécité pendant 20s. Vous gagnez également l'effet Résistance 1 et verrouillez les ultimes ennemis. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean waterBreathing = nzPlayer.hasEffect(PotionEffectType.WATER_BREATHING);
        boolean dolphinGrace = nzPlayer.hasEffect(PotionEffectType.DOLPHINS_GRACE);
        if (waterBreathing && dolphinGrace) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.WATER_BREATHING,Integer.MAX_VALUE,1);
        nzPlayer.addEffect(PotionEffectType.DOLPHINS_GRACE,Integer.MAX_VALUE,1);
        nzPlayer.setMaxHealth(24);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        trappedPlayers.clear();
        trappedPlayers = PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,10,false);
        if (trappedPlayers.size() == 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Pas d'ennemis dans le rayon !");
            return;
        }

        //Clear sphere
        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,ultimeTime,1);

        //Create Sphere
        sphereCenter = nzPlayer.getLocation();
        int ground = sphereCenter.getWorld().getHighestBlockYAt(sphereCenter);
        sphereCenter.setY(ground+height);

        for (Location location : Structure.generateSphere(sphereCenter,sphereRadius,false)) {
            location.getBlock().setType(Material.WATER,false);
        }

        for (Location location : Structure.generateSphere(sphereCenter,sphereRadius+1,true)) {
            location.getBlock().setType(Material.GLASS);
        }

        //TP players
        nzPlayer.teleport(sphereCenter);
        nzPlayer.setTemporaryNoFall(25);
        for (NZPlayer item : trappedPlayers) {
            item.teleport(sphereCenter);
            item.getNZType().setUltimeLocktime(ultimeTime);
            item.setWaterTrapped(true);
            item.setTemporaryNoFall(25);
            item.addEffect(PotionEffectType.BLINDNESS,ultimeTime,1);
        }

        //Check players in zone
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < trappedPlayers.size(); i++) {
                    if (!trappedPlayers.get(i).isWaterTrapped()) trappedPlayers.remove(trappedPlayers.get(i));
                }
                if (trappedPlayers.size() == 0) cancelUltime();
            }
        }.runTaskTimer(Main.getInstance(),0,5).getTaskId());

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;
            if (Math.random() >= waterBowChance) return 0;

            victim.getLocation().getBlock().setType(Material.WATER);
            MapReload.addEntry(victim.getLocation().getBlock(),Material.AIR);
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.getPlayer().isInWater()) {
            inWater = true;
            nzPlayer.clearEffect(PotionEffectType.SPEED);
            nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1);
            nzPlayer.addEffect(PotionEffectType.NIGHT_VISION,Integer.MAX_VALUE,1);
        }
        else if (inWater) {
            inWater = false;
            nzPlayer.addEffect(PotionEffectType.SPEED,5,1);
            nzPlayer.clearEffect(PotionEffectType.INCREASE_DAMAGE);
            nzPlayer.clearEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);

        for (NZPlayer item : trappedPlayers) item.setWaterTrapped(false);

        //Clear sphere
        if (sphereCenter != null) {
            for (Location location : Structure.generateSphere(sphereCenter,sphereRadius+1,false)) {
                location.getBlock().setType(Material.AIR);
            }
        }

        //Unlock ultis
        for (NZPlayer item : trappedPlayers) {
            item.getNZType().setUltimeLocktime(0);
            item.setWaterTrapped(false);
        }
    }

    public void resetAll() {
        super.resetAll();
        inWater = false;
    }
}
