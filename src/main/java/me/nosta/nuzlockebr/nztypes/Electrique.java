package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Damager;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Electrique extends NZType {

    public Electrique() {
        super(Type.Electrique);
        this.ultimeName = "Cage-Eclair";
    }

    private final int ultimeTime = 6;
    private final int ultimeDamage = 6;

    private final float thunderMeleeChance = 0.25f;
    private final float thunderBowChance = 0.15f;
    private final double thunderDamage = 1;

    private final float paraChance = 0.4f;
    private final float paraTime = 0.75f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Vitesse 3 et Célérité 2.")+
                addDescLine("Vos coups ont une chance d'infliger 0.5\u2764 de dégâts bruts (25% épée/15% arc).")+
                addDescLine("Vos flèches ont 40% de chance de paralyser pendant 0.75s.")+
                " \n"+
                addDescUlti("Dans un rayon de 10 blocs vous foudroyez tout vos ennemis, ces éclairs appliquent les effets Cécité et Lenteur 4 pendant 6s et infligent 3\u2764 de dégâts bruts. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        boolean haste = nzPlayer.hasEffect(PotionEffectType.FAST_DIGGING);
        if (speed && haste) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,3);
        nzPlayer.addEffect(PotionEffectType.FAST_DIGGING,Integer.MAX_VALUE,2);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        List<NZPlayer> playersInRange = PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,10,false);
        if (playersInRange.size() == 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Pas d'ennemis dans le rayon !");
            return;
        }

        for (NZPlayer item : playersInRange) {
            item.getPlayer().getWorld().strikeLightningEffect(item.getPlayer().getLocation());

            item.addEffect(PotionEffectType.SLOW,ultimeTime,4);
            item.addEffect(PotionEffectType.BLINDNESS,ultimeTime,255);

            item.brutDamage(ultimeDamage,nzPlayer);
        }

        strikeLighning();

        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (Math.random() < thunderMeleeChance) {
                victim.brutDamage(thunderDamage,nzPlayer);
            }
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (Math.random() < thunderBowChance) {
                victim.brutDamage(thunderDamage,nzPlayer);
            }

            if (victim.getNZType().isCCImmune()) return 0;
            if (Math.random() < paraChance) {
                stunEnemy(victim,paraTime);
            }
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }
}
