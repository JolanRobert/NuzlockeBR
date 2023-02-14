package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Feu extends NZType {

    public Feu() {
        super(Type.Feu);
        this.ultimeName = "Nitrocharge";
    }

    private final int ultimeTime = 20;

    private double bonusSpeed;
    private int speedLevel = 1;

    private final float fireDamage = 0.2f;
    public boolean nextFireArrow;

    private List<Integer> tasks = new ArrayList<>();

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez l'effet Résistance au feu et Vitesse 1.")+
                addDescLine("Vous possédez les enchantements Aura de feu I et Flamme.")+
                addDescLine("Vos coups infligent 20% de dégâts supplémentaires contre les ennemis enflammés.")+
                addDescLine("Vous avez l'effet Nausée dans l'eau.")+
                " \n"+
                addDescUlti("Votre vitesse augmente progressivement de Vitesse 1 à Vitesse 5 en 10s. Vous gagnez également l'effet Résistance 1 pendant 20s et enflammez tous les ennemis autour de vous dans un rayon de 7 blocs. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean fireResistance = nzPlayer.hasEffect(PotionEffectType.FIRE_RESISTANCE);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        if (fireResistance && speed) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,1);
        nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;
        int timeBetweenSpeedLevel = 10/4;

        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,ultimeTime,1);

        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        bonusSpeed = 0;
        speedLevel = 1;
        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                gainSpeed(20f/timeBetweenSpeedLevel);
                if (speedLevel == 5) cancel();
            }
        }.runTaskTimer(Main.getInstance(),0,20).getTaskId());

        tasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                for (NZPlayer item : PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,7,false)) {
                    if (item.getPlayer().getFireTicks() != -20) continue;
                    item.getPlayer().setFireTicks(80);
                }
            }
        }.runTaskTimer(Main.getInstance(),0,10).getTaskId());

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getPlayer().getFireTicks() != 20 && victim.getPlayer().getFireTicks() > 0) {
                return fireDamage;
            }
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (nextFireArrow) {
                nextFireArrow = false;
                return fireDamage;
            }
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.getPlayer().isInWater()) nzPlayer.addEffect(PotionEffectType.CONFUSION,Integer.MAX_VALUE,1);
        else nzPlayer.clearEffect(PotionEffectType.CONFUSION);
    }

    private void gainSpeed(double amount) {
        bonusSpeed += amount;
        if (bonusSpeed >= 20) {
            speedLevel++;
            nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,speedLevel);
            bonusSpeed -= 20;
        }

        //double finalSpeed = Math.min(0.1+speedLevel*0.02+bonusSpeed/1000,0.18);
        double finalSpeed = 0.1+bonusSpeed/1000;
        nzPlayer.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(finalSpeed);
    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);

        //Reset speed
        nzPlayer.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        nzPlayer.clearEffect(PotionEffectType.SPEED);
    }

    public void resetAll() {
        super.resetAll();
        bonusSpeed = 0;
        speedLevel = 1;
        nextFireArrow = false;
    }
}
