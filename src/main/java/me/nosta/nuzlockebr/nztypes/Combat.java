package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class Combat extends NZType {

    public Combat() {
        super(Type.Combat);
        this.ultimeName = "Détection";
    }

    private final int ultimeTime = 20;

    private final int heartLimit = 6;
    private final float bowDamage = -0.25f;
    private final float ultiDodgeChance = 0.5f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Force 1 et Vitesse 1.")+
                addDescLine("Vous avez Force 2 lorsque vous êtes à 3\u2764 ou moins.")+
                addDescLine("Vous ignorez les effets de Résistance en mêlée.")+
                addDescLine("Vous infligez 25% de dégâts en moins à l'arc.")+
                " \n"+
                addDescUlti("Vous avez 50% de chance d'esquiver les dégâts en mêlée et vous êtes insensible aux flèches pendant 20s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean strength = nzPlayer.hasEffect(PotionEffectType.INCREASE_DAMAGE);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        if (strength && speed) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1);
        nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            double baseResistance = event.getDamage(EntityDamageEvent.DamageModifier.RESISTANCE);
            event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE,baseResistance/2.75);
        }
        else if (victim == nzPlayer) {
            if (isUltimeActivate() && Math.random() < ultiDodgeChance) {
                event.setDamage(0);
            }
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) return bowDamage;
        else if (victim == nzPlayer) {
            if (isUltimeActivate()) {
                event.setCancelled(true);
                event.getDamager().remove();
            }
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.getPlayer().getHealth() <= heartLimit) nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,2);
        else if (nzPlayer.getEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() == 1) {
            nzPlayer.clearEffect(PotionEffectType.INCREASE_DAMAGE);
            nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1);
        }
    }
}
