package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.nztypes.capacity.ArmorVisibility;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Spectre extends NZType {

    public Spectre() {
        super(Type.Spectre);
        this.ultimeName = "Malédiction";
    }

    private final int ultimeTime = 10;

    public long lastDamageTime;

    List<NZPlayer> cursedPlayers = new ArrayList<>();
    List<Integer> tasks = new ArrayList<>();

    private final float dodgeChance = 0.35f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous avez Force 1, Vitesse 1 et Vision Nocturne la nuit mais seulement Lenteur 1 le jour.")+
                addDescLine("Vous avez 35% de chance d'esquiver les flèches.")+
                addDescLine("Vous avez Speed 2 lorsque vous êtes à 5\u2764 ou moins.")+
                addDescLine("Vous êtes invisible aux yeux des autres joueurs, cet effet prend fin lorsque vous infligez/subissez des dégâts (revient après 5s sans infliger/subir de dégâts).")+
                " \n"+
                addDescUlti("Dans un rayon de 10 blocs vous maudissez vos ennemis et leur infligez Cécité, Faiblesse 1, Nausée et verrouillez leur ultime pendant 10s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean strength = nzPlayer.hasEffect(PotionEffectType.INCREASE_DAMAGE);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        boolean nightVision = nzPlayer.hasEffect(PotionEffectType.NIGHT_VISION);
        boolean slow = nzPlayer.hasEffect(PotionEffectType.SLOW);
        if ((strength && speed && nightVision) || slow) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        float time = (12000-(Bukkit.getWorld("world").getTime()%12000))* GameManager.getInstance().cycleTime/12000f;
        boolean isDay = Bukkit.getWorld("world").getTime() < 12000;

        if (isDay) {
            nzPlayer.addEffect(PotionEffectType.SLOW,time,1);
        }
        else {
            nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,time,1);
            nzPlayer.addEffect(PotionEffectType.SPEED,time,1);
            nzPlayer.addEffect(PotionEffectType.NIGHT_VISION,time,1);
        }
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        cursedPlayers.clear();
        cursedPlayers = PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,10,false);
        if (cursedPlayers.size() == 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Pas d'ennemis dans le rayon !");
            return;
        }

        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        for (NZPlayer item : cursedPlayers) {
            item.addEffect(PotionEffectType.BLINDNESS,10,1);
            item.addEffect(PotionEffectType.WEAKNESS,10,1);
            item.addEffect(PotionEffectType.CONFUSION,10,1);
            item.getNZType().setUltimeLocktime(ultimeTime);
        }

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            lastDamageTime = System.currentTimeMillis();
        }
        else if (victim == nzPlayer) {
            lastDamageTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            lastDamageTime = System.currentTimeMillis();
        }
        else if (victim == nzPlayer) {
            if (Math.random() < dodgeChance) {
                event.setCancelled(true);
                event.getDamager().remove();
                attacker.sendMessage(nzPlayer.getColoredName()+" a esquivé votre flèche !");
                victim.sendMessage(ChatColor.DARK_GRAY+"Vous avez esquivé la flèche de "+attacker.getColoredName()+ChatColor.DARK_GRAY+" !");
            }
            else lastDamageTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (System.currentTimeMillis() > lastDamageTime+5*1000L || lastDamageTime == 0) {
            if (ArmorVisibility.getVisibilityMode(nzPlayer) != ArmorVisibility.VisibilityMode.Nothing || !nzPlayer.hasEffect(PotionEffectType.INVISIBILITY)) {
                ArmorVisibility.setVisibilityMode(nzPlayer, ArmorVisibility.VisibilityMode.Nothing);
                nzPlayer.sendMessage(ChatColor.DARK_GRAY+"Vous êtes invisible !");
            }
        }
        else if (ArmorVisibility.getVisibilityMode(nzPlayer) == ArmorVisibility.VisibilityMode.Nothing) {
            ArmorVisibility.setVisibilityMode(nzPlayer, GameManager.getInstance().isAnonymous ? ArmorVisibility.VisibilityMode.NoArmor : ArmorVisibility.VisibilityMode.All);
            nzPlayer.sendMessage(ChatColor.DARK_GRAY+"Vous êtes visible !");
        }

        if (nzPlayer.getPlayer().getHealth() <= 10) nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,2);
        else {
            if (!nzPlayer.hasEffect(PotionEffectType.SPEED)) return;
            if (nzPlayer.getPlayer().getPotionEffect(PotionEffectType.SPEED).getAmplifier() != 1) return;
            nzPlayer.clearEffect(PotionEffectType.SPEED);
        }
    }

    public void cancelUltime() {
        super.cancelUltime();
    }

    public void resetRespawn() {
        super.resetRespawn();
        lastDamageTime = 0;
    }

    public void resetAll() {
        super.resetAll();
        lastDamageTime = 0;
    }
}
