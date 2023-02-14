package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Insecte extends NZType {

    public Insecte() {
        super(Type.Insecte);
        this.ultimeName = "Papillodanse";
    }

    private List<NZPlayer> firstImpressionList = new ArrayList<>();
    private final int firstImpressionDamage = 4;

    private final float cobwebChance = 0.3f;
    private List<Block> cobwebs = new ArrayList<>();

    private final float allMalusChance = 0.07f;
    private final int ultimeTime = 10;

    private double bonusHealth;
    private int assistCount;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous avez -4\u2764.")+
                addDescLine("Votre premier coup d'épée contre un joueur inflige 2\u2764 de dégâts bruts supplémentaires (20s de cooldown pour chaque joueur).")+
                addDescLine("Vos flèches ont 30% de chance de faire apparaître une toile d'araignée en touchant un ennemi (disparaît après 5s).")+
                addDescLine("Vos coups peuvent infliger des effets négatifs (Nausée/Cécité/Obscurité/Faiblesse/Lenteur/Poison/Lévitation/Étourdissement/Confusion/Charme)(7% pour chaque effet).")+
                " \n"+
                addDescUlti("Actif : Vous gagnez Vitesse 2 et 6\u2764 d'absorption pendant 10s.\nPassif : Faire un kill et/ou réaliser trois assists vous fait gagner 0.5\u2764 de façon permanente. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        nzPlayer.setMaxHealth(12+bonusHealth);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        nzPlayer.addEffect(PotionEffectType.SPEED,10,2);
        nzPlayer.addAbsorption(12);

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (!firstImpressionList.contains(victim)) {
                victim.brutDamage(firstImpressionDamage,nzPlayer);
                firstImpressionList.add(victim);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> firstImpressionList.remove(victim), 20*20L);
            }

            allMalus(victim,allMalusChance);
        }
        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (victim.getNZType().isCCImmune()) return 0;

            if (Math.random() < cobwebChance) {
                Block b = victim.getLocation().getBlock();
                b.setType(Material.COBWEB);
                cobwebs.add(b);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::clearCobwebs, 5*20L);
            }

            allMalus(victim,allMalusChance);
        }

        return 0;
    }

    @Override
    public void passivePower() {

    }

    private void clearCobwebs() {
        for (int i = cobwebs.size()-1; i >= 0; i--) {
            Block b = cobwebs.get(i);
            b.setType(Material.AIR);
            cobwebs.remove(b);
        }
    }

    public void addAssist() {
        assistCount++;
        if (assistCount%3 == 0) gainMaxHealth();
    }

    public void gainMaxHealth() {
        bonusHealth++;
        nzPlayer.sendMessage("Vie max augmentée de 0.5\u2764 !");
    }

    public void resetRespawn() {
        firstImpressionList.clear();
        clearCobwebs();
    }

    public void resetAll() {
        firstImpressionList.clear();
        clearCobwebs();

        bonusHealth = 0;
        assistCount = 0;
    }
}
