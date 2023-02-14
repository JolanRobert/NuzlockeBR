package me.nosta.nuzlockebr.runnables;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Skin;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.*;
import me.nosta.nuzlockebr.nztypes.capacity.ArmorVisibility;
import me.nosta.nuzlockebr.utils.Broadcaster;
import me.nosta.nuzlockebr.utils.Disguiser;
import me.nosta.nuzlockebr.utils.Glower;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class StartGameRunnable extends BukkitRunnable {

    private int timer = 5;

    public StartGameRunnable() {
        if (GameManager.getInstance().isAnonymous) {
            anonymousMode();
            runTaskTimer(Main.getInstance(),4L*PlayerManager.getInstance().playerList.size(),20);
        }
        else runTaskTimer(Main.getInstance(),0,20);
    }

    @Override
    public void run() {
        if (timer > 0) {
            String title = ChatColor.GREEN+"> "+(timer)+" <";
            Broadcaster.titleAll(title,"",0,20,0);
            Broadcaster.soundAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP,Integer.MAX_VALUE,1);

            if (timer == 5) {
                clearPlayers();
                teleportPlayers();
                freezePlayers();
            }
        }

        else if (timer == 0) {
            String title = ChatColor.RED+"GO !";
            Broadcaster.titleAll(title,"",0,20,20);
            Broadcaster.soundAll(Sound.ENTITY_ENDER_DRAGON_GROWL,Integer.MAX_VALUE,1);

            Bukkit.getWorld("world").setTime(Math.random() < 0.5f ? 0 : 12000);

            assignTypes();
            stuffPlayers();
            givePlayerEffects();

            Glower.glowMates();
            ScoreManager.getInstance().initPoints();
            ScoreManager.getInstance().initHealth();

            GameManager.getInstance().startGame();
            this.cancel();
        }

        timer--;
    }

    private void clearPlayers() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.getPlayer().getInventory().clear();
        }
    }

    private void teleportPlayers() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            Location loc = nzPlayer.getRespawnLocation();
            nzPlayer.teleport(loc);
        }
    }

    private void freezePlayers() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,10,255);
            nzPlayer.addEffect(PotionEffectType.BLINDNESS,5,255);
            nzPlayer.freezePlayer(5);
        }
    }

    private void assignTypes() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            if (nzPlayer.getNZType().getType() != Type.None) continue;
            NZType nzType = TypeManager.getInstance().getRandomType();
            nzPlayer.setNZType(nzType);
        }
    }

    private void stuffPlayers() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.getPlayer().getInventory().clear();
            StuffManager.getInstance().giveStuff(nzPlayer);
            StuffManager.getInstance().giveSpecialStuff(nzPlayer);
        }
    }

    private void givePlayerEffects() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            nzPlayer.getNZType().givePermanentEffects();
            nzPlayer.heal();
        }
    }

    private void anonymousMode() {
        Skin anonymousSkin = Skin.getRandomSkin();
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) {
            Disguiser.setSkin(nzPlayer,anonymousSkin);
            ArmorVisibility.setVisibilityMode(nzPlayer, ArmorVisibility.VisibilityMode.NoArmor);
            nzPlayer.getNZTeam().hideName(true);
        }
    }
}
