package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Structure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Poison extends NZType {

    public Poison() {
        super(Type.Poison);
        this.ultimeName = "Piège de Venin";
    }

    private int ultimeTime = 15;

    private Location zoneCenter;
    private HashMap<Location,Material> oldMaterials = new HashMap<>();
    private final int zoneRadius = 15;

    private List<Integer> tasks = new ArrayList<>();

    private final float poisonSwordChance = 0.8f;
    private final float poisonBowChance = 0.4f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous avez +2\u2764.")+
                addDescLine("Vous renvoyez 20% des dégâts reçus à l'épée.")+
                addDescLine("Vos coups ont une chance d'infliger un effet de Poison 2 pendant 4s qui se cumule (80% épée/40% arc).")+
                " \n"+
                addDescUlti("Vous créez une zone toxique dans un rayon de 15 blocs qui inflige à tout vos ennemis l'effet Wither III et vous octroie Régénération II pendant 15s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {

    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.setMaxHealth(24);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        //Generate poison zone
        zoneCenter = nzPlayer.getLocation();
        oldMaterials.clear();
        for (Location loc : Structure.generatenZone(zoneCenter,zoneRadius)) {
            if (loc.getBlock().getType() == Material.AIR) continue;
            else if (loc.getBlock().isLiquid()) continue;
            else if (loc.getBlock().isPassable()) continue;
            else {
                Location above = new Location(loc.getWorld(),loc.getX(),loc.getY()+1,loc.getZ());

                if (loc.getBlock().getType() == Material.SNOW_BLOCK || loc.getBlock().getType() == Material.BLUE_ICE) {
                    if (Math.random() >= 0.5f) continue;
                }

                else oldMaterials.put(loc,loc.getBlock().getType());

                if (above.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.SOUL_SAND);
                    if (Math.random() < 0.05f && loc != zoneCenter) above.getBlock().setType(Material.FIRE);
                }
                else loc.getBlock().setType(Material.OBSIDIAN);
            }
        }

        //Check players in zone
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (nzPlayer.getLocation().distance(zoneCenter) <= zoneRadius) {
                    if (!nzPlayer.hasEffect(PotionEffectType.REGENERATION)) nzPlayer.addEffect(PotionEffectType.REGENERATION,2.5f,2);
                }
                for (NZPlayer item : PlayerManager.getInstance().playerList) {
                    if (item == nzPlayer) continue;
                    if (item.getLocation().distance(zoneCenter) > zoneRadius) continue;
                    if (GameManager.getInstance().gameMode == NZGameMode.Team) {
                        if (item.getTeam() == nzPlayer.getTeam()) continue;
                    }
                    if (item.hasEffect(PotionEffectType.WITHER)) {
                        if (item.getPlayer().getPotionEffect(PotionEffectType.WITHER).getDuration() < 20) {
                            item.addEffect(PotionEffectType.WITHER,2,3);
                        }
                    }
                    else item.addEffect(PotionEffectType.WITHER,2,3);
                }
            }
        }.runTaskTimer(Main.getInstance(),0,5).getTaskId());

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;

            if (Math.random() < poisonSwordChance) {
                int poisonTick = victim.hasEffect(PotionEffectType.POISON) ? victim.getPlayer().getPotionEffect(PotionEffectType.POISON).getDuration() : 0;
                victim.addEffect(PotionEffectType.POISON,(poisonTick/20f)+4,2);
            }
        }
        else if (victim == nzPlayer) {
            attacker.getPlayer().damage(event.getFinalDamage()*0.2);
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;

            if (Math.random() < poisonBowChance) {
                int poisonTick = victim.hasEffect(PotionEffectType.POISON) ? victim.getPlayer().getPotionEffect(PotionEffectType.POISON).getDuration() : 0;
                victim.addEffect(PotionEffectType.POISON,(poisonTick/20f)+4,2);
            }
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);

        if (zoneCenter != null) {
            for (Location loc : oldMaterials.keySet()) {
                loc.getBlock().setType(oldMaterials.get(loc));
            }
        }

        nzPlayer.clearEffect(PotionEffectType.REGENERATION);
    }
}
