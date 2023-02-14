package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Fee extends NZType {

    public Fee() {
        super(Type.Fee);
        this.ultimeName = "Câlinerie";
    }

    public final int creationCooldown = 10;

    private final float bowPropulsionChance = 0.5f;
    private final float bowDamage = -0.5f;

    private final int ultimeTime = 12;

    private List<Integer> tasks = new ArrayList<>();

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez l'effet Faiblesse 1 et +3\u2764.")+
                addDescLine("Vos flèches infligent 50% de dégâts en moins mais ont 50% de chance d'éjecter l'ennemi.")+
                addDescLine("Chaque assist vous rapporte 50% de points supplémentaires, une pomme en or et recharge la Création de la Lumière.")+
                addDescLine("Vous disposez de la Création de la Lumière (se recharge en 10s, /nz creafairy pour plus d'informations).")+
                " \n"+
                addDescUlti("Pendant 12s et dans un rayon de 10 blocs, tous les ennemis sont charmés et se tournent dans votre direction toutes les 3s. Vous gagnez également l'effet Résistance 2. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        boolean weakness = nzPlayer.hasEffect(PotionEffectType.WEAKNESS);
        if (weakness) return;
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.addEffect(PotionEffectType.WEAKNESS,Integer.MAX_VALUE,1);
        nzPlayer.setMaxHealth(26);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,ultimeTime,2);

        tasks.add(new BukkitRunnable() {
            int ultiStack = 0;

            @Override
            public void run() {
                for (NZPlayer item : PlayerManager.getInstance().getNZPlayersInRange(nzPlayer,10,false)) {
                    item.teleport(getLookedLocation(item,nzPlayer));
                    charmEnemy(item);
                }
                if (ultiStack++ == ultimeTime/3) cancel();
            }
        }.runTaskTimer(Main.getInstance(),0,60).getTaskId());

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    private Location getLookedLocation(NZPlayer observer, NZPlayer target) {
        Vector targetVector = target.getPlayer().getEyeLocation().toVector();
        Vector observerVector = observer.getPlayer().getEyeLocation().toVector();
        Vector direction = observerVector.subtract(targetVector).normalize();

        // Now change the angle
        Location changed = observer.getLocation().clone();
        changed.setYaw((float) (180 - Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()))));
        changed.setPitch((float) (90 - Math.toDegrees(Math.acos(direction.getY()))));
        System.out.println(observer.getLocation());
        System.out.println(changed);
        return changed;
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return bowDamage;

            if (Math.random() < bowPropulsionChance) {
                Vector direction = victim.getLocation().toVector().subtract(nzPlayer.getLocation().toVector()).normalize();
                direction.multiply(2f);
                direction.setY(1f);
                victim.setTemporaryNoFall(3);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> victim.getPlayer().setVelocity(direction),1);
            }

            return bowDamage;
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);
    }

    public void resetRespawn() {
        super.resetRespawn();
        CreationLumiere.getInstance().enableCreation(nzPlayer);
    }
}
