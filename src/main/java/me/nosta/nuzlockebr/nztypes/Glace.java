package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Damager;
import me.nosta.nuzlockebr.utils.Structure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Glace extends NZType {

    public Glace() {
        super(Type.Glace);
        this.ultimeName = "Blizzard";
    }

    private final int ultimeTime = 15;

    private Location zoneCenter;
    private HashMap<Location,Material> oldMaterials = new HashMap<>();
    private final int zoneRadius = 15;

    private final float slowTime = 3;

    private int arrowHit;
    private final float freezeTime = 2.5f;

    private boolean spawnShield;
    public double shieldHealth;
    private boolean isShieldReloading;
    private final int shieldCooldown = 30;

    private List<Integer> tasks = new ArrayList<>();

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Résistance 1 et Lenteur 1.")+
                addDescLine("Vous transformez l'eau en glace autour de vous.")+
                addDescLine("Vos coups infligent Lenteur 2 pendant 3s.")+
                addDescLine("Toutes les 3 flèches touchées, vous étourdissez l'ennemi pendant 2.5s et gagnez 5s de Vitesse 2.")+
                addDescLine("Vous recevez un Voile Aurore (3\u2764 d'absorption) qui revient passivement toutes les 30s.")+
                " \n"+
                addDescUlti("Vous créez une zone enneigée dans un rayon de 15 blocs qui étourdi 1.25s les ennemis toutes les 3s. Pour chaque ennemi gelé dans votre zone, il subit 0.5\u2764 de dégât brut et vous êtes soigné de 1\u2764. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean resistance = nzPlayer.hasEffect(PotionEffectType.DAMAGE_RESISTANCE);
        boolean slowness = nzPlayer.hasEffect(PotionEffectType.SLOW);
        if (resistance && slowness) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,1);
        nzPlayer.addEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,1);
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

                if (loc.getBlock().getType() == Material.SOUL_SAND || loc.getBlock().getType() == Material.OBSIDIAN) {
                    if (Math.random() >= 0.5f) continue;
                }

                else oldMaterials.put(loc,loc.getBlock().getType());

                if (above.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.SNOW_BLOCK);
                    if (Math.random() < 0.05f && loc != zoneCenter) above.getBlock().setType(Material.SOUL_FIRE);
                }
                else loc.getBlock().setType(Material.BLUE_ICE);
            }
        }

        //Check players in zone
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                List<NZPlayer> frozenPlayers = new ArrayList<>();
                for (NZPlayer item : PlayerManager.getInstance().playerList) {
                    if (item == nzPlayer) continue;
                    if (item.getLocation().distance(zoneCenter) > zoneRadius) continue;
                    if (GameManager.getInstance().gameMode == NZGameMode.Team) {
                        if (item.getTeam() == nzPlayer.getTeam()) continue;
                        frozenPlayers.add(item);
                    }
                    else frozenPlayers.add(item);
                }

                if (frozenPlayers.size() == 0) return;
                String message = ChatColor.AQUA+"Vous avez étourdi ";
                for (int i = 0; i < frozenPlayers.size(); i++) {
                    if (i != 0) message += ChatColor.AQUA+"/";
                    NZPlayer item = frozenPlayers.get(i);
                    item.freezePlayer(1.25f);
                    item.brutDamage(1,nzPlayer);
                    item.sendMessage(ChatColor.AQUA+"Vous avez été étourdi par "+nzPlayer.getColoredName()+" !");
                    message += item.getColoredName();
                }
                nzPlayer.heal(frozenPlayers.size()*2);
                message += ChatColor.AQUA+" (+"+frozenPlayers.size()+"\u2764)";
                nzPlayer.sendMessage(message);
            }
        }.runTaskTimer(Main.getInstance(),0,60).getTaskId());

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;
            victim.addEffect(PotionEffectType.SLOW,slowTime,2);
        }
        else if (victim == nzPlayer) {
            damageShield(-event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            arrowHit(victim);

            if (victim.getNZType().isCCImmune()) return 0;
            victim.addEffect(PotionEffectType.SLOW,slowTime,2);
        }
        else if (victim == nzPlayer) {
            damageShield(-event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.isWaterTrapped()) return;

        Location baseLoc = nzPlayer.getLocation();
        Location underLoc = new Location(nzPlayer.getPlayer().getWorld(), baseLoc.getX(), baseLoc.getY()-1, baseLoc.getZ());

        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            underLoc.setX(baseLoc.getX()+x);
            for (int z = -radius; z < radius; z++) {
                underLoc.setZ(baseLoc.getZ()+z);
                if (underLoc.getBlock().getType() != Material.WATER) continue;

                Block b = underLoc.getBlock();
                Levelled l = (Levelled) b.getBlockData();
                if (l.getLevel() > 0) continue;

                b.setType(Material.FROSTED_ICE);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> b.setType(Material.WATER),5*20);
            }
        }

        reloadIceShield();
    }

    public void arrowHit(NZPlayer victim) {
        arrowHit++;
        if (arrowHit == 3) {
            nzPlayer.addEffect(PotionEffectType.SPEED,5,2);
            arrowHit = 0;

            if (victim.getNZType().isCCImmune()) return;
            stunEnemy(victim,freezeTime);
        }
    }

    public void damageShield(double amount) {
        if (shieldHealth > 0) {
            shieldHealth -= amount;
            shieldHealth = Math.max(shieldHealth,0);
        }
    }

    public void reloadIceShield() {
        if (!spawnShield) {
            spawnShield = true;
            iceShield();
        }
        else if (shieldHealth == 0 && !isShieldReloading) {
            isShieldReloading = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                if (shieldHealth == 0) iceShield();
            }, shieldCooldown*20L);
        }
    }

    private void iceShield() {
        shieldHealth = 6;
        isShieldReloading = false;

        nzPlayer.sendMessage(ChatColor.AQUA+"Voile Aurore rechargé !");
        double absoAmount = Math.min(nzPlayer.getPlayer().getAbsorptionAmount()+shieldHealth,10);
        nzPlayer.setAbsorption(absoAmount);
        ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth());
    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);

        if (zoneCenter != null) {
            for (Location loc : oldMaterials.keySet()) {
                loc.getBlock().setType(oldMaterials.get(loc));
            }
        }
    }

    public void resetRespawn() {
        super.resetRespawn();
        arrowHit = 0;
        spawnShield = false;
    }

    public void resetAll() {
        super.resetAll();
        arrowHit = 0;
        spawnShield = false;
        shieldHealth = 0;
        isShieldReloading = false;
    }
}
