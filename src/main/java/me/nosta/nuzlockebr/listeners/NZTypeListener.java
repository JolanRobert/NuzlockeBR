package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.nztypes.Glace;
import me.nosta.nuzlockebr.nztypes.Normal;
import me.nosta.nuzlockebr.nztypes.Plante;
import me.nosta.nuzlockebr.nztypes.Spectre;
import me.nosta.nuzlockebr.utils.Damager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NZTypeListener implements Listener {

    private final int strengthPercent = 25; //Base 37.5%

    @EventHandler
    private void onEntityDamageByEntityMelee(EntityDamageByEntityEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        NZPlayer attacker = PlayerManager.getInstance().getNZPlayer((Player) event.getDamager());
        NZPlayer victim = PlayerManager.getInstance().getNZPlayer((Player) event.getEntity());

        if (attacker == null || victim == null) return;
        if (attacker == victim) return;

        if (GameManager.getInstance().gameMode == NZGameMode.Team && victim.getTeam() == attacker.getTeam()) {
            event.setCancelled(true);
            return;
        }

        if (isCharmed(attacker,victim) || isConfuse(attacker)) {
            event.setCancelled(true);
            return;
        }

        //Nerfed strength
        PotionEffect strength = attacker.getEffect(PotionEffectType.INCREASE_DAMAGE);
        if (strength != null) {
            int strengthLevel = strength.getAmplifier()+1;
            event.setDamage(event.getDamage()-3*strengthLevel);
            event.setDamage(event.getDamage()+strengthLevel*3*strengthPercent/37.5f);
        }

        double damageModifier = 0.0;

        damageModifier += victim.getNZType().handleMelee(event,attacker,victim);
        if (event.isCancelled()) return;
        damageModifier += attacker.getNZType().handleMelee(event,attacker,victim);
        if (victim.getPlayer().getHealth() == 0.0) event.setCancelled(true);
        if (event.isCancelled()) return;

        if (attacker.getNZType().voixEnjoleuse) {
            attacker.getNZType().voixEnjoleuse = false;
            attacker.heal(4);
            attacker.getNZType().charmEnemy(victim);
        }

        if (damageModifier != 0.0) event.setDamage(event.getDamage()+event.getDamage()*damageModifier);

        Damager.addEntry(victim,attacker);
    }

    @EventHandler
    public void onEntityDamageByEntityBow(EntityDamageByEntityEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        if (!(((Arrow) event.getDamager()).getShooter() instanceof Player)) return;

        NZPlayer attacker = PlayerManager.getInstance().getNZPlayer((Player) ((Arrow) event.getDamager()).getShooter());
        NZPlayer victim = PlayerManager.getInstance().getNZPlayer((Player) event.getEntity());

        if (attacker == null || victim == null) return;
        if (attacker == victim) event.setCancelled(true);

        if (GameManager.getInstance().gameMode == NZGameMode.Team && victim.getTeam() == attacker.getTeam()) event.setCancelled(true);

        if (event.isCancelled()) {
            event.getDamager().remove();
            return;
        }

        if (isCharmed(attacker,victim) || isConfuse(attacker)) {
            event.setCancelled(true);
            return;
        }

        double damageModifier = 0.0;

        damageModifier += victim.getNZType().handleBow(event,attacker,victim);
        if (event.isCancelled()) return;
        damageModifier += attacker.getNZType().handleBow(event,attacker,victim);
        if (victim.getPlayer().getHealth() == 0.0) event.setCancelled(true);
        if (event.isCancelled()) return;

        if (damageModifier != 0.0) event.setDamage(event.getDamage()+event.getDamage()*damageModifier);

        Damager.addEntry(victim,attacker);

        ChatColor deathColor;
        double damage = event.getFinalDamage()-event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION);
        int victimHealth = (int) Math.round(victim.getHealth()-damage);
        if (victim.getHealth()-damage < 0) return;
        if (victimHealth >= 18) deathColor = ChatColor.GREEN;
        else if (victimHealth >= 10) deathColor = ChatColor.YELLOW;
        else if (victimHealth >= 3) deathColor = ChatColor.RED;
        else deathColor = ChatColor.DARK_RED;

        attacker.sendMessage(victim.getColoredName()+ChatColor.RESET+" est à "+deathColor+victimHealth+"\u2764");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) event.setCancelled(true);

        if (!(event.getEntity() instanceof Player)) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> ((Player)event.getEntity()).setArrowsInBody(0),1);
        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer((Player) event.getEntity());
        if (nzPlayer == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth()),1);

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (nzPlayer.getPlayer().getHealth() == 0.0) event.setCancelled(true);

        else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (nzPlayer.getNoFall()) {
                event.setCancelled(true);
            }
        }

        if (event.isCancelled()) return;

        NZType nzType = nzPlayer.getNZType();
        if (nzType.getType() == Type.Plante) {
            ((Plante)nzType).lastHitTime = System.currentTimeMillis();
        }

        else if (nzType.getType() == Type.Glace) {
            ((Glace)nzType).damageShield(-event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
        }

        else if (nzType.getType() == Type.Spectre) {
            ((Spectre)nzType).lastDamageTime = System.currentTimeMillis();
        }

        else if (nzType.getType() == Type.Normal) {
            Normal normal = (Normal)nzPlayer.getNZType();
            if (normal.hasType(Type.Spectre)) normal.lastDamageTime = System.currentTimeMillis();
        }
    }

    private boolean isCharmed(NZPlayer attacker, NZPlayer victim) {
        if (attacker.getCharme() > 0) {
            victim.heal(2);
            attacker.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez soigné 1\u2764 à "+victim.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
            attacker.setCharme(attacker.getCharme()-1);
            return true;
        }
        return false;
    }

    private boolean isConfuse(NZPlayer attacker) {
        if (attacker.getConfusion() > 0) {
            if (Math.random() < 1/3f) {
                attacker.brutDamage(1,null);
                attacker.sendMessage("Vous vous êtes blessé dans votre confusion.");
                return true;
            }
            attacker.setConfusion(attacker.getConfusion()-1);
        }
        return false;
    }
}
