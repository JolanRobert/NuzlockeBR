package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Dragon extends NZType {

    public Dragon() {
        super(Type.Dragon);
        this.ultimeName = "Danse Draco";
    }

    private final int ultimeTime = 20;

    private int lastEffect;
    private int timeBeforeNextEffect = 60;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez un effet aléatoire parmi Force/Vitesse/Résistance 1 (change toutes les minutes) ainsi que +2\u2764.")+
                addDescLine("Vous êtes insensible aux entraves ennemies (hors Ultime et Sabotage).")+
                " \n"+
                addDescUlti("Vous gagnez les effets Force 1, Vitesse 1, Résistance 1 et Régénération 1 pendant 20s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        if (isUltimeActivate()) {
            boolean regeneration = nzPlayer.hasEffect(PotionEffectType.REGENERATION);
            if (!regeneration) nzPlayer.addEffect(PotionEffectType.REGENERATION,getUltimeTimeLeft(),1);
        }
        crowdControlValue = 1;
        boolean strength = nzPlayer.hasEffect(PotionEffectType.INCREASE_DAMAGE);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        boolean resistance = nzPlayer.hasEffect(PotionEffectType.DAMAGE_RESISTANCE);
        if (strength || speed || resistance) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        Random rdm = new Random();
        int value = rdm.nextInt(3)+1;
        while (value == lastEffect) value = rdm.nextInt(3)+1;

        if (value == 1) nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,timeBeforeNextEffect,1);
        else if (value == 2) nzPlayer.addEffect(PotionEffectType.SPEED,timeBeforeNextEffect,1);
        else nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,timeBeforeNextEffect,1);

        nzPlayer.setMaxHealth(24);
        lastEffect = value;
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.clearEffect(PotionEffectType.INCREASE_DAMAGE);
        nzPlayer.clearEffect(PotionEffectType.SPEED);
        nzPlayer.clearEffect(PotionEffectType.DAMAGE_RESISTANCE);

        nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,ultimeTime,1);
        nzPlayer.addEffect(PotionEffectType.SPEED,ultimeTime,1);
        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,ultimeTime,1);
        nzPlayer.addEffect(PotionEffectType.REGENERATION,ultimeTime,1);

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
        return 0;
    }

    @Override
    public void passivePower() {

    }

    public void resetAll() {
        super.resetAll();
        lastEffect = 0;
    }
}
