package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

public class Plante extends NZType {

    public Plante() {
        super(Type.Plante);
        this.ultimeName = "Giga-Sangsue";
    }

    private final int ultimeTime = 20;

    private int swordHit;
    public long lastHitTime;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez l'effet Régénération 1 ainsi que +4\u2764, l'effet passe à Régénération 2 si vous n'avez pas subi de dégâts pendant 12s.")+
                addDescLine("Vous avez Vitesse 1 le jour et Lenteur 1 la nuit.")+
                addDescLine("Vous régénérez 0.5\u2764 tous les 3 coups d'épée.")+
                addDescLine("Vos flèches vous donne 0.5\u2764 d'absorption (6\u2764 max).")+
                addDescLine("Vous êtes soigné de 3\u2764 par kill et 1.5\u2764 par assist.")+
                addDescLine("Vous n'avez pas de pommes en or.")+
                " \n"+
                addDescUlti("Pendant 20s, vous gagnez les effets Régénération 2 et Vitesse 2, vos coups régénèrent 1.5\u2764 tous les 3 coups. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean regeneration = nzPlayer.hasEffect(PotionEffectType.REGENERATION);
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        boolean slowness = nzPlayer.hasEffect(PotionEffectType.SLOW);
        if (regeneration && (speed || slowness)) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,1);

        float time = (12000-(Bukkit.getWorld("world").getTime()%12000))* GameManager.getInstance().cycleTime/12000f;
        boolean isDay = Bukkit.getWorld("world").getTime() < 12000;
        nzPlayer.addEffect(isDay ? PotionEffectType.SPEED : PotionEffectType.SLOW,time,1);

        nzPlayer.setMaxHealth(28);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.addEffect(PotionEffectType.REGENERATION,ultimeTime,2);
        nzPlayer.addEffect(PotionEffectType.SPEED,ultimeTime,2);

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            swordHit();
        }
        else if (victim == nzPlayer) {
            lastHitTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            double absorption = Math.min(attacker.getPlayer().getAbsorptionAmount()+1,12);
            attacker.setAbsorption(absorption);
        }

        else if (victim == nzPlayer) {
            lastHitTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (System.currentTimeMillis() > lastHitTime+12*1000L || lastHitTime == 0) {
            regeneration(2);
        }
        else {
            if (isUltimeActivate()) regeneration(2);
            else regeneration(1);
        }
    }

    public void swordHit() {
        swordHit++;
        if (swordHit == 3) {
            nzPlayer.heal(isUltimeActivate() ? 3 : 1);
            swordHit = 0;
        }
    }

    public void regeneration(int level) {
        if (nzPlayer.hasEffect(PotionEffectType.REGENERATION)) {
            if (nzPlayer.getPlayer().getPotionEffect(PotionEffectType.REGENERATION).getAmplifier() == level-1) return;
        }

        nzPlayer.clearEffect(PotionEffectType.REGENERATION);
        nzPlayer.addEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,level);
    }

    public void resetRespawn() {
        super.resetRespawn();
        swordHit = 0;
    }

    public void resetAll() {
        super.resetAll();
        swordHit = 0;
        lastHitTime = 0;
    }
}
