package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Damager;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Tenebres extends NZType {

    public Tenebres() {
        super(Type.Tenebres);
        this.ultimeName = "Dernier Mot";
    }

    public final int creationCooldown = 10;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez l'effet Vitesse 1.")+
                addDescLine("Vos coups à l'épée infligent Obscurité pendant 3s.")+
                addDescLine("Vos flèches réduisent votre recharge d'ultime de 4s.")+
                addDescLine("Lorsque vous mourrez vous infligez 2\u2764 de dégâts bruts à votre meurtrier.")+
                addDescLine("Vous disposez de la Création des Ténèbres (se recharge en 10s, /nz creadark).")+
                " \n"+
                addDescUlti("Vous exécutez (4\u2764) l'ennemi le plus bas en vie dans un rayon de 7 blocs. S'il meurt, votre ultime ainsi que la Création des Ténèbres sont immédiatement rechargés. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean speed = nzPlayer.hasEffect(PotionEffectType.SPEED);
        if (speed) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;
        int ultimeDamage = 8;

        List<NZPlayer> playersInRange = PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,7,false);
        if (playersInRange.size() == 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Pas d'ennemis dans le rayon !");
            return;
        }

        NZPlayer nzLowestHealth = playersInRange.get(0);
        for (int i = 1; i < playersInRange.size(); i++) {
            if (nzLowestHealth.getPlayer().getHealth() <= playersInRange.get(i).getPlayer().getHealth()) continue;
            nzLowestHealth = playersInRange.get(i);
        }

        strikeLighning();

        setUltimeCooldown(120);
        new UltimeCDRunnable(this);

        if (nzLowestHealth.getPlayer().getHealth() <= ultimeDamage) {
            setUltimeCooldown(0);
            CreationTenebres.getInstance().enableCreation(nzPlayer);
        }

        nzLowestHealth.brutDamage(ultimeDamage,nzPlayer);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;
            victim.addEffect(PotionEffectType.DARKNESS,3,1);
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            setUltimeCooldown(getUltimeCooldown()-4);
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }

    public void resetRespawn() {
        super.resetRespawn();
        CreationTenebres.getInstance().enableCreation(nzPlayer);
    }
}
