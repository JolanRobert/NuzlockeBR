package me.nosta.nuzlockebr.nztypes.capacity;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Power;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.nztypes.Normal;
import me.nosta.nuzlockebr.nztypes.Tenebres;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreationTenebres implements Creation {

    private ItemStack creation = new ItemStack(Material.ENDER_EYE);
    private final int creationRange = 10;

    private Map<NZPlayer, Integer> tasks = new HashMap<>();

    private static CreationTenebres Instance;
    public static CreationTenebres getInstance() {
        if (Instance == null) Instance = new CreationTenebres();
        return Instance;
    }

    @Override
    public ItemStack getCreationItem() {
        Power power = choosePower();

        creation.setType(Material.ENDER_EYE);
        if (power == Power.Coup_Bas) {
            ItemEditor.setDisplayName(creation, ""+ChatColor.RED+ChatColor.BOLD+"Coup Bas");

        }
        else if (power == Power.Punition) {
            ItemEditor.setDisplayName(creation, ""+ChatColor.RED+ChatColor.BOLD+"Punition");
        }
        else if (power == Power.Sabotage) {
            ItemEditor.setDisplayName(creation, ""+ChatColor.RED+ChatColor.BOLD+"Sabotage");
        }

        return creation;
    }

    @Override
    public String getCreationInfo() {
        return  " \n"+
                ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"La Création des Ténèbres est un item cliquable à la manière d'un ultime. Il octroie un des 3 pouvoirs ci-dessous.\n"+
                " \n"+
                ChatColor.RED+"[Coup Bas] "+ChatColor.DARK_AQUA+"Vous vous téléportez dans le dos du joueur ciblé et gagnez Force I pendant 2s.\n"+
                " \n"+
                ChatColor.RED+"[Punition] "+ChatColor.DARK_AQUA+"Vous retournez de 180° le joueur ciblé et lui infligez Cécité pendant 2s.\n"+
                " \n"+
                ChatColor.RED+"[Sabotage] "+ChatColor.DARK_AQUA+"Vous mélangez la hotbar du joueur ciblé.\n"+
                " \n"+
                "Tout ces pouvoirs ne fonctionnent que sur des joueurs ennemis et avec une portée maximale de 10 blocs. Pour utiliser un pouvoir il suffit de faire clique droit en direction de votre cible.\n";
    }

    @Override
    public Power choosePower() {
        Random rdm = new Random();
        int value = rdm.nextInt(3)+1;
        if (value == 1) return Power.Coup_Bas;
        else if (value == 2) return Power.Punition;
        else return Power.Sabotage;
    }

    @Override
    public void activeCreation(NZPlayer nzPlayer, ItemStack creation) {
        String itemName = creation.getItemMeta().getDisplayName();

        Power power;
        if (itemName.equalsIgnoreCase(""+ChatColor.RED+ChatColor.BOLD+"Coup Bas")) power = Power.Coup_Bas;
        else if (itemName.equalsIgnoreCase(""+ChatColor.RED+ChatColor.BOLD+"Punition")) power = Power.Punition;
        else power = Power.Sabotage;

        int creationCooldown;
        if (nzPlayer.getNZType().getType() == Type.Tenebres) creationCooldown = ((Tenebres)nzPlayer.getNZType()).creationCooldown;
        else if (nzPlayer.getNZType().getType() == Type.Normal) creationCooldown = ((Normal)nzPlayer.getNZType()).creationCooldown;
        else return;

        castCreation(nzPlayer,power,creationCooldown);
    }

    @Override
    public void castCreation(NZPlayer nzPlayer, Power power, int creationCooldown) {
        if (power == Power.None) {
            nzPlayer.sendMessage(ChatColor.RED+"Création des Ténèbres en cours de récupération !");
            return;
        }

        RayTraceResult ray = nzPlayer.getPlayer().getWorld().rayTraceEntities(nzPlayer.getLocation(),nzPlayer.getLocation().getDirection(),
                creationRange-1,1f,entity -> entity instanceof Player && entity != nzPlayer.getPlayer());
        if (ray == null) {
            nzPlayer.sendMessage(ChatColor.RED+"Aucun ennemi ciblé/à portée !");
            return;
        }

        Player hitPlayer = (Player) ray.getHitEntity();
        NZPlayer nzHit = PlayerManager.getInstance().getNZPlayer(hitPlayer);
        if (nzHit == null) return;
        if (GameManager.getInstance().gameMode == NZGameMode.Team && nzHit.getTeam() == nzPlayer.getTeam()) {
            nzPlayer.sendMessage(ChatColor.RED+"Vous ne pouvez pas cibler un allié !");
            return;
        }

        switch (power) {
            case Coup_Bas -> coupBas(nzPlayer,nzHit);
            case Punition -> punition(nzHit);
            case Sabotage -> sabotage(nzPlayer,nzHit);
        }

        disableCreation(nzPlayer);
        int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> enableCreation(nzPlayer), creationCooldown*20L);
        tasks.put(nzPlayer,taskID);
    }

    @Override
    public void enableCreation(NZPlayer nzPlayer) {
        removeTask(nzPlayer);
        Inventory inv = nzPlayer.getPlayer().getInventory();

        int creationSlot = inv.first(Material.ENDER_PEARL);
        if (creationSlot == -1) return;
        inv.setItem(creationSlot,getCreationItem());

        nzPlayer.sendMessage(ChatColor.DARK_RED+"Création des Ténèbres rechargée !");
        nzPlayer.getPlayer().playSound(nzPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,Integer.MAX_VALUE,1);
    }

    @Override
    public void disableCreation(NZPlayer nzPlayer) {
        Inventory inv = nzPlayer.getPlayer().getInventory();

        creation.setType(Material.ENDER_PEARL);
        ItemEditor.setDisplayName(creation, ""+ChatColor.RED+ChatColor.BOLD+"Recharge");

        int creationSlot = inv.first(Material.ENDER_EYE);
        if (creationSlot == -1) return;
        inv.setItem(creationSlot,creation);
    }

    public void removeTask(NZPlayer nzPlayer) {
        if (tasks.get(nzPlayer) == null) return;
        Bukkit.getScheduler().cancelTask(tasks.get(nzPlayer));
        tasks.remove(nzPlayer);
    }

    private void coupBas(NZPlayer nzCaster, NZPlayer nzHit) {
        Location loc = nzHit.getLocation();
        Vector offsetBehind = loc.getDirection();
        Location newLoc = loc.subtract(offsetBehind).add(0,0.5,0);
        nzCaster.teleport(newLoc);
        nzCaster.addEffect(PotionEffectType.INCREASE_DAMAGE,2,1);
    }

    public void punition(NZPlayer nzHit) {
        Location loc = nzHit.getLocation();
        Location newLoc = new Location(loc.getWorld(),loc.getX(),loc.getY(),loc.getZ(),loc.getYaw()+180,loc.getPitch());
        nzHit.teleport(newLoc);
        if (nzHit.getNZType().isCCImmune()) return;
        nzHit.addEffect(PotionEffectType.BLINDNESS,2,1);
    }

    private void sabotage(NZPlayer nzCaster, NZPlayer nzHit) {
        Inventory inv = nzHit.getPlayer().getInventory();
        ItemStack[] hotbar;
        Random rdm = new Random();

        for (int i = 8; i > 0; i--) {
            int value = rdm.nextInt(i+1);
            if (i == value) continue;

            ItemStack firstItem = inv.getItem(i);
            ItemStack secondItem = inv.getItem(value);

            inv.setItem(i,secondItem);
            inv.setItem(value,firstItem);
        }
        nzCaster.sendMessage(ChatColor.DARK_RED+"Vous avez saboté "+nzHit.getColoredName()+ChatColor.DARK_RED+" !");
    }
}
