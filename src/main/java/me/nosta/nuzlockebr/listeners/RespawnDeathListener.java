package me.nosta.nuzlockebr.listeners;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameState;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.managers.StuffManager;
import me.nosta.nuzlockebr.nztypes.Insecte;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.utils.Damager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class RespawnDeathListener implements Listener {

    private static RespawnDeathListener Instance;

    public static RespawnDeathListener getInstance() {
        if (Instance == null) Instance = new RespawnDeathListener();
        return Instance;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        NZPlayer nzDead = PlayerManager.getInstance().getNZPlayer(event.getEntity());
        NZPlayer nzKiller = PlayerManager.getInstance().getNZPlayer(event.getEntity().getKiller());

        if (nzDead == null) return;

        List<NZPlayer> nzAssistants = Damager.getAssistants(nzDead);

        for (NZPlayer nzAssistant : nzAssistants) {
            if (nzAssistant == nzKiller) continue;
            nzAssistant.getNZStats().addAssist(nzDead);
            NZType nzType = nzAssistant.getNZType();
            if (nzType.getType() == Type.Fee) {
                CreationLumiere.getInstance().enableCreation(nzAssistant);
                nzAssistant.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE,1));
            }
            else if (nzType.getType() == Type.Insecte) ((Insecte)nzType).addAssist();
            else if (nzType.getType() == Type.Plante) nzAssistant.heal(3);
        }

        if (nzKiller == null || nzKiller == nzDead) {
            nzDead.getNZStats().addDeath(null);
            return;
        }

        if (nzKiller.getNZType().getType() == Type.Insecte) ((Insecte)nzKiller.getNZType()).gainMaxHealth();

        if (nzKiller.getNZType().getType() == Type.Plante) nzKiller.heal(6);
        else nzKiller.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE,2));

        if (nzDead.getNZType().getType() == Type.Tenebres) {
            nzKiller.brutDamage(4,nzDead);
        }

        nzKiller.getNZStats().addKill(nzDead);
        nzDead.getNZStats().addDeath(nzKiller);

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (GameManager.getInstance().gameState != NZGameState.Playing) return;

        Player player = event.getPlayer();
        NZPlayer nzPlayer = PlayerManager.getInstance().getNZPlayer(player);
        if (nzPlayer == null) return;

        nzPlayer.resetRespawn();
        StuffManager.getInstance().resetStuff(nzPlayer);

        NZType nzType = nzPlayer.getNZType();
        nzType.resetRespawn();

        Location respawnLoc = nzPlayer.getRespawnLocation();
        event.setRespawnLocation(respawnLoc);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,5,5);
            ScoreManager.getInstance().updateHealth(nzPlayer, nzPlayer.getHealth());
        },1);
    }
}
