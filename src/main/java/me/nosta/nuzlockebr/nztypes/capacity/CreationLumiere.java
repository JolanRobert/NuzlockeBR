package me.nosta.nuzlockebr.nztypes.capacity;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Power;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.nztypes.Fee;
import me.nosta.nuzlockebr.nztypes.Normal;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreationLumiere implements Creation {

    private ItemStack creation = new ItemStack(Material.SNOWBALL);
    private final int creationRange = 10;

    private Map<NZPlayer, Integer> tasks = new HashMap<>();

    private static CreationLumiere Instance;
    public static CreationLumiere getInstance() {
        if (Instance == null) Instance = new CreationLumiere();
        return Instance;
    }

    @Override
    public ItemStack getCreationItem() {
        Power power = choosePower();

        creation.setType(Material.SNOWBALL);
        if (power == Power.Vigilance) {
            ItemEditor.setDisplayName(creation, ""+ ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Vigilance");

        }
        else if (power == Power.Rayon_Lune) {
            ItemEditor.setDisplayName(creation, ""+ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Rayon Lune");
        }
        else if (power == Power.Voix_Enjoleuse) {
            ItemEditor.setDisplayName(creation, ""+ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Voix Enjôleuse");
        }

        return creation;
    }

    @Override
    public String getCreationInfo() {
        return  " \n"+
                ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"La Création de la Lumière est un item cliquable à la manière d'un ultime. Il octroie un des 3 pouvoirs ci-dessous.\n"+
                " \n"+
                ChatColor.LIGHT_PURPLE+"[Vigilance] "+ChatColor.DARK_AQUA+"Octroie une immunité aux entraves (hors Ultime et Sabotage) et Résistance 1 pendant 10s.\n"+
                " \n"+
                ChatColor.LIGHT_PURPLE+"[Rayon Lune] "+ChatColor.DARK_AQUA+"Octroie Régénération 2 et 4\u2764 d'absorption pendant 10s.\n"+
                " \n"+
                ChatColor.LIGHT_PURPLE+"[Voix Enjôleuse] "+ChatColor.DARK_AQUA+"Octroie Vitesse 2 pendant 10s, votre prochain coup charme votre ennemi et vous rend 2\u2764.\n"+
                " \n"+
                "Tout ces pouvoirs ne fonctionnent que sur des joueurs alliés et avec une portée maximale de 10 blocs. Pour utiliser un pouvoir il suffit de faire clique droit en direction de votre cible.\n";
    }

    @Override
    public Power choosePower() {
        Random rdm = new Random();
        int value = rdm.nextInt(3)+1;
        if (value == 1) return Power.Vigilance;
        else if (value == 2) return Power.Rayon_Lune;
        else return Power.Voix_Enjoleuse;
    }

    @Override
    public void activeCreation(NZPlayer nzPlayer, ItemStack creation) {
        String itemName = creation.getItemMeta().getDisplayName();

        Power power;
        if (itemName.equalsIgnoreCase(""+ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Vigilance")) power = Power.Vigilance;
        else if (itemName.equalsIgnoreCase(""+ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Rayon Lune")) power = Power.Rayon_Lune;
        else power = Power.Voix_Enjoleuse;

        int creationCooldown;
        if (nzPlayer.getNZType().getType() == Type.Fee) creationCooldown = ((Fee)nzPlayer.getNZType()).creationCooldown;
        else if (nzPlayer.getNZType().getType() == Type.Normal) creationCooldown = ((Normal)nzPlayer.getNZType()).creationCooldown;
        else return;

        castCreation(nzPlayer,power,creationCooldown);
    }

    @Override
    public void castCreation(NZPlayer nzPlayer, Power power, int creationCooldown) {
        if (power == Power.None) {
            nzPlayer.sendMessage(ChatColor.RED+"Création de la Lumière en cours de récupération !");
            return;
        }

        RayTraceResult ray = nzPlayer.getPlayer().getWorld().rayTraceEntities(nzPlayer.getLocation(),nzPlayer.getLocation().getDirection(),
                creationRange-1,1.1f,entity -> entity instanceof Player && entity != nzPlayer.getPlayer());

        NZPlayer nzHit = null;

        if (ray == null) {
            nzHit = nzPlayer;
        }

        else {
            Player hitPlayer = (Player) ray.getHitEntity();
            nzHit = PlayerManager.getInstance().getNZPlayer(hitPlayer);
            if (nzHit == null) return;
            if (GameManager.getInstance().gameMode == NZGameMode.FFA ||
                    (GameManager.getInstance().gameMode == NZGameMode.Team && nzHit.getTeam() != nzPlayer.getTeam())) {
                nzHit = nzPlayer;
            }
        }

        switch (power) {
            case Vigilance -> vigilance(nzPlayer,nzHit);
            case Rayon_Lune -> rayon_lune(nzPlayer,nzHit);
            case Voix_Enjoleuse -> voix_enjoleuse(nzPlayer,nzHit);
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

        nzPlayer.sendMessage(ChatColor.LIGHT_PURPLE+"Création de la Lumière rechargée !");
        nzPlayer.getPlayer().playSound(nzPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,Integer.MAX_VALUE,1);
    }

    @Override
    public void disableCreation(NZPlayer nzPlayer) {
        Inventory inv = nzPlayer.getPlayer().getInventory();

        creation.setType(Material.ENDER_PEARL);
        ItemEditor.setDisplayName(creation, ""+ChatColor.LIGHT_PURPLE+ChatColor.BOLD+"Recharge");

        int creationSlot = inv.first(Material.SNOWBALL);
        if (creationSlot == -1) return;
        inv.setItem(creationSlot,creation);
    }

    public void removeTask(NZPlayer nzPlayer) {
        if (tasks.get(nzPlayer) == null) return;
        Bukkit.getScheduler().cancelTask(tasks.get(nzPlayer));
        tasks.remove(nzPlayer);
    }

    public void vigilance(NZPlayer nzCaster, NZPlayer nzHit) {
        nzHit.getNZType().crowdControlValue = 1;
        nzHit.addEffect(PotionEffectType.DAMAGE_RESISTANCE,10,1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> nzHit.getNZType().crowdControlValue = 0, 10*20L);

        if (nzCaster == nzHit) nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous vous êtes octroyé Vigilance !");
        else {
            nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez octroyé Vigilance à "+nzHit.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
            nzHit.sendMessage(nzCaster.getColoredName()+ChatColor.LIGHT_PURPLE+" vous a octroyé Vigilance !");
        }
    }

    public void rayon_lune(NZPlayer nzCaster, NZPlayer nzHit) {
        nzHit.addEffect(PotionEffectType.REGENERATION,10,1);
        nzHit.addAbsorption(8);

        if (nzCaster == nzHit) nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous vous êtes octroyé Rayon Lune !");
        else {
            nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez octroyé Rayon Lune à "+nzHit.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
            nzHit.sendMessage(nzCaster.getColoredName()+ChatColor.LIGHT_PURPLE+" vous a octroyé Rayon Lune !");
        }
    }

    public void voix_enjoleuse(NZPlayer nzCaster, NZPlayer nzHit) {
        nzHit.addEffect(PotionEffectType.SPEED,10,2);
        nzHit.getNZType().voixEnjoleuse = true;

        if (nzCaster == nzHit) nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous vous êtes octroyé Voix Enjôleuse !");
        else {
            nzCaster.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez octroyé Voix Enjôleuse à "+nzHit.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
            nzHit.sendMessage(nzCaster.getColoredName()+ChatColor.LIGHT_PURPLE+" vous a octroyé Voix Enjôleuse !");
        }
    }
}
