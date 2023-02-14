package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Damager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Vol extends NZType {

    public Vol() {
        super(Type.Vol);
        this.ultimeName = "Vol";
    }

    private final int ultimeTime = 20;

    private final float bowDamage = 0.3f;

    private final float confusionChance = 0.3f;
    private final float swapChance = 0.15f;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les effets Sauts Améliorés 4, Vitesse 1 et No Fall.")+
                addDescLine("Vous avez Force 1 lorsque vous êtes à une hauteur de 75 ou plus.")+
                addDescLine("Vous avez +100% de recul.")+
                addDescLine("Vos flèches ont 30% de chance de rendre confus l'ennemi et infligent 30% de dégâts supplémentaires.")+
                addDescLine("Vos flèches ont 15% de chance d'échanger votre position avec celle de votre ennemi, auquel cas vous gagnez Résistance 1 pendant 5s tandis que votre ennemi écope de Cécité pendant 5s.")+
                " \n"+
                addDescUlti("Vous disposez de la capacité de voler pendant 20s, de plus vos coups infligent 0.5\u2764 de dégâts bruts qui exécutent. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean jumpBoost = nzPlayer.hasEffect(PotionEffectType.JUMP);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        if (jumpBoost && speed && nzPlayer.getNoFall()) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.setNoFall(true);
        nzPlayer.addEffect(PotionEffectType.JUMP,Integer.MAX_VALUE,4);
        nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.getPlayer().setAllowFlight(true);
        nzPlayer.getPlayer().setFlying(true);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20);

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (!isUltimeActivate()) return 0;
            victim.brutDamage(1,nzPlayer);
        }

        else if (victim == nzPlayer) {
            Player p = attacker.getPlayer();
            Vector velocity = nzPlayer.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
            velocity.multiply(2);
            velocity.setY(p.getVelocity().getY());
            nzPlayer.getPlayer().setVelocity(velocity);
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (isUltimeActivate()) {
                victim.brutDamage(1,nzPlayer);
            }

            if (victim.getNZType().isCCImmune()) return bowDamage;

            if (Math.random() < confusionChance) {
                confuseEnemy(victim);
            }

            if (Math.random() < swapChance) {
                nzPlayer.swapPosition(victim);

                nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,5,1);
                victim.addEffect(PotionEffectType.BLINDNESS,5,1);
            }

            return bowDamage;
        }

        else if (victim == nzPlayer) {
            Player p = attacker.getPlayer();
            Vector velocity = nzPlayer.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
            velocity.multiply(2);
            velocity.setY(p.getVelocity().getY());
            nzPlayer.getPlayer().setVelocity(velocity);
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.getLocation().getY() >= 75) nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,Integer.MAX_VALUE,1);
        else nzPlayer.clearEffect(PotionEffectType.INCREASE_DAMAGE);
    }

    public void cancelUltime() {
        super.cancelUltime();
        nzPlayer.getPlayer().setFlying(false);
        nzPlayer.getPlayer().setAllowFlight(false);
    }
}
