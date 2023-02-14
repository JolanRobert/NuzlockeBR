package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import me.nosta.nuzlockebr.utils.Damager;
import me.nosta.nuzlockebr.utils.Glower;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Psy extends NZType {

    public Psy() {
        super(Type.Psy);
        this.ultimeName = "Frappe Psy";
    }

    private int ultimeTime = 30;

    private final float swordReceiveDamage = 0.3f;
    private final double levitationChance = 0.3f;
    private final double darknessChance = 0.5f;
    private final double punitionChance = 0.2f;

    public float prescienceChance = 0;

    public final int heartLimit = 6;
    public boolean canTeleport = true;

    private List<Integer> tasks = new ArrayList<>();

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous possédez les enchantements Frappe II, Recul II et Chute Amortie II.")+
                addDescLine("Vous infligez 60% de dégâts supplémentaires à l'arc mais vous subissez +30% de dégâts à l'épée.")+
                addDescLine("Vos flèches ont respectivement 50/30/20% de chance d'infliger Obscurité/Lévitation/Punition à l'impact.")+
                addDescLine("Vos flèches ont X% de chance d'infliger 4\u2764 de dégâts bruts (X -> +2.5% par flèche touchée/revient à 0% après avoir réussi).")+
                addDescLine("Vous échangez votre position avec un joueur aléatoire lorsque vous avez 3\u2764 ou moins (1 fois par apparition)")+
                " \n"+
                addDescUlti("Pendant 30s, vos effets d'Obscurité/Lévitation sont améliorés et ont 100% de chance de réussite. Vos dégâts supplémentaires sont également augmentés à +120% et vous avez une Vision Pure sur vos ennemis (glowing). (120s)");
        return description;
    }

    @Override
    public void checkEffects() {

    }

    @Override
    public void givePermanentEffects() {

    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        //Glow enemies
        for (NZPlayer glowingPlayer : PlayerManager.getInstance().playerList) {
            if (nzPlayer == glowingPlayer) continue;
            if (GameManager.getInstance().gameMode == NZGameMode.Team && nzPlayer.getTeam() == glowingPlayer.getTeam()) continue;
            Glower.setGlow(nzPlayer,glowingPlayer,true);
        }


        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (victim == nzPlayer) return swordReceiveDamage;

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (Math.random()*100 < prescienceChance) {
                victim.brutDamage(8,nzPlayer);
                nzPlayer.sendMessage(ChatColor.LIGHT_PURPLE+"Votre prescience a frappé "+victim.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
                victim.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez frappé par la prescience de "+nzPlayer.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
                prescienceChance = 0;
            }
            else {
                prescienceChance += 2.5f;
                if (prescienceChance%10 == 0) {
                    nzPlayer.sendMessage(ChatColor.LIGHT_PURPLE+"Prescience chargée à "+prescienceChance+"% !");
                }
            }

            float bowDamage = isUltimeActivate() ? 1.2f : 0.6f;
            if (victim.getNZType().isCCImmune()) return bowDamage;

            if (Math.random() < levitationChance || isUltimeActivate()) {
                int levitationLevel = isUltimeActivate() ? 12 : 4;
                victim.addEffect(PotionEffectType.LEVITATION,1,levitationLevel);
            }

            if (Math.random() < darknessChance || isUltimeActivate()) {
                int darknessTime = isUltimeActivate() ? 6 : 3;
                victim.addEffect(PotionEffectType.DARKNESS,darknessTime,1);
            }

            if (Math.random() < punitionChance) {
                CreationTenebres.getInstance().punition(victim);
            }

            return bowDamage;
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (nzPlayer.getPlayer().getHealth() <= heartLimit && canTeleport) {
            canTeleport = false;
            List<NZPlayer> exceptions = new ArrayList<>();
            exceptions.add(nzPlayer);
            NZPlayer nzSwap = PlayerManager.getInstance().getRandomPlayer(exceptions);

            nzPlayer.sendMessage(ChatColor.LIGHT_PURPLE+"Votre Teleport vous a échangé avec "+nzSwap.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
            nzSwap.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez été échangé par le Teleport de "+nzPlayer.getColoredName()+" !");
            nzPlayer.swapPosition(nzSwap);
        }
    }

    public void cancelUltime() {
        super.cancelUltime();
        for (Integer taskID : tasks) Bukkit.getScheduler().cancelTask(taskID);

        //Unglow enemies
        for (NZPlayer item : PlayerManager.getInstance().playerList) {
            if (nzPlayer == item) continue;
            if (GameManager.getInstance().gameMode == NZGameMode.Team && nzPlayer.getTeam() == item.getTeam()) continue;
            Glower.setGlow(nzPlayer,item,false);
        }
    }

    public void resetRespawn() {
        super.resetRespawn();
        canTeleport = true;
    }

    public void resetAll() {
        super.resetAll();
        prescienceChance = 0;
        canTeleport = true;
    }
}
