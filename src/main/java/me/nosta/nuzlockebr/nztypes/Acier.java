package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Acier extends NZType {

    public Acier() {
        super(Type.Acier);
        this.ultimeName = "Allègement";
    }

    private final int ultimeTime = 20;

    private final float knockbackResistance = 0.3f;
    private final float stunChance = 0.2f;
    private final float stunTime = 1;
    private final float pullChance = 0.5f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Résistance 2, Lenteur 2 et +2\u2764.")+
                addDescLine("Vous avez -30% de recul.")+
                addDescLine("Vos coups à l'épée ont 20% de chance d'étourdir pendant 1s.")+
                addDescLine("Vos flèches ont 50% de chance d'attirer l'ennemi vers vous.")+
                addDescLine("Les pommes en or ne vous donne qu'un \u2764 d'absorption.")+
                " \n"+
                addDescUlti("Vous perdez tout vos effets, en contrepartie vous obtenez les effets Vitesse 2, Sauts Améliorés 2, Force 1 et 4\u2764 d'absorption pendant 20s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        if (isUltimeActivate()) return;
        boolean resistance = nzPlayer.hasEffect(PotionEffectType.DAMAGE_RESISTANCE);
        boolean slowness = nzPlayer.hasEffect(PotionEffectType.SLOW);
        if (resistance && slowness) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,Integer.MAX_VALUE,2);
        nzPlayer.addEffect(PotionEffectType.SLOW,Integer.MAX_VALUE,2);
        nzPlayer.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(knockbackResistance);
        nzPlayer.setMaxHealth(24);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.clearEffect(PotionEffectType.DAMAGE_RESISTANCE);
        nzPlayer.clearEffect(PotionEffectType.SLOW);
        nzPlayer.addEffect(PotionEffectType.SPEED,ultimeTime,2);
        nzPlayer.addEffect(PotionEffectType.JUMP,ultimeTime,2);
        nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,ultimeTime,1);
        nzPlayer.addAbsorption(8);

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;
            if (Math.random() >= stunChance) return 0;

            stunEnemy(victim,stunTime);
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;
            if (Math.random() >= pullChance) return 0;

            Vector direction = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
            direction.setY(victim.getPlayer().getVelocity().getY());
            double distance = attacker.getLocation().distance(victim.getLocation());
            float strength = (float)(distance/3);
            victim.getPlayer().setVelocity(direction.multiply(strength));
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }
}
