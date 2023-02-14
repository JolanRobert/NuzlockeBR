package me.nosta.nuzlockebr.managers;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.nztypes.Tenebres;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class StuffManager {

    private static StuffManager Instance;
    public static StuffManager getInstance() {
        if (Instance == null) Instance = new StuffManager();
        return Instance;
    }

    public void giveStuff(NZPlayer nzPlayer) {
        PlayerInventory inv = nzPlayer.getPlayer().getInventory();

        ItemStack sword = giveItem(Material.DIAMOND_SWORD,1,true, new HashMap<Enchantment, Integer>() {{
            put(Enchantment.DAMAGE_ALL, 1);
        }});

        ItemStack bow = giveItem(Material.BOW,1,true, new HashMap<Enchantment, Integer>() {{
            put(Enchantment.ARROW_DAMAGE, 1);
            put(Enchantment.ARROW_INFINITE, 1);
        }});

        ItemStack helmet, chestplate, leggings, boots;

        if (nzPlayer.getPvpLevel() == 1) {
            helmet = giveArmor(Material.IRON_HELMET,Enchantment.PROTECTION_PROJECTILE,3);
            chestplate = giveArmor(Material.IRON_CHESTPLATE,Enchantment.PROTECTION_ENVIRONMENTAL,3);
            leggings = giveArmor(Material.IRON_LEGGINGS,Enchantment.PROTECTION_ENVIRONMENTAL,3);
            boots = giveArmor(Material.IRON_BOOTS,Enchantment.PROTECTION_ENVIRONMENTAL,3);
        }

        else if (nzPlayer.getPvpLevel() == 2) {
            helmet = giveArmor(Material.IRON_HELMET,Enchantment.PROTECTION_PROJECTILE,3);
            chestplate = giveArmor(Material.DIAMOND_CHESTPLATE,Enchantment.PROTECTION_ENVIRONMENTAL,2);
            leggings = giveArmor(Material.IRON_LEGGINGS,Enchantment.PROTECTION_ENVIRONMENTAL,4);
            boots = giveArmor(Material.IRON_BOOTS,Enchantment.PROTECTION_ENVIRONMENTAL,3);
        }

        else {
            helmet = giveArmor(Material.DIAMOND_HELMET,Enchantment.PROTECTION_PROJECTILE,3);
            chestplate = giveArmor(Material.DIAMOND_CHESTPLATE,Enchantment.PROTECTION_ENVIRONMENTAL,2);
            leggings = giveArmor(Material.IRON_LEGGINGS,Enchantment.PROTECTION_ENVIRONMENTAL,4);
            boots = giveArmor(Material.DIAMOND_BOOTS,Enchantment.PROTECTION_ENVIRONMENTAL,2);
        }

        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ItemStack gapples = new ItemStack(Material.GOLDEN_APPLE,4+nzPlayer.getPvpLevel());
        ItemStack pickaxe = giveItem(Material.IRON_PICKAXE,1,true,new HashMap<Enchantment, Integer>() {{
            put(Enchantment.DIG_SPEED, 2);
        }});
        ItemStack blocks = new ItemStack(Material.COBBLESTONE, 64);
        ItemStack water_bucket = new ItemStack(Material.WATER_BUCKET, 1);
        ItemStack ultime = nzPlayer.getNZType().getUltime();

        inv.setItem(0,sword);
        inv.setItem(1,bow);
        inv.setItem(2,gapples);
        inv.setItem(3,pickaxe);
        inv.setItem(4,ultime);
        inv.setItem(7,blocks);
        inv.setItem(8,water_bucket);
        inv.setItem(28,arrow);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    public void giveSpecialStuff(NZPlayer nzPlayer) {
        PlayerInventory inv = nzPlayer.getPlayer().getInventory();
        Type type = nzPlayer.getNZType().getType();

        if (type == Type.Eau) {
            inv.getHelmet().addEnchantment(Enchantment.WATER_WORKER,1);
            inv.getBoots().addEnchantment(Enchantment.DEPTH_STRIDER,2);

            ItemStack rod = giveItem(Material.FISHING_ROD,1,true,new HashMap<Enchantment, Integer>() {{
                put(Enchantment.ARROW_INFINITE,1);
            }});

            inv.addItem(rod);
        }

        else if (type == Type.Fee) {
            ItemStack creation = CreationLumiere.getInstance().getCreationItem();
            inv.addItem(creation);
        }

        else if (type == Type.Feu) {
            inv.getItem(0).addEnchantment(Enchantment.FIRE_ASPECT,1);
            inv.getItem(1).addEnchantment(Enchantment.ARROW_FIRE,1);
        }

        else if (type == Type.Plante) {
            inv.remove(Material.GOLDEN_APPLE);
        }

        else if (type == Type.Psy) {
            inv.getItem(0).addEnchantment(Enchantment.KNOCKBACK,2);
            inv.getItem(1).addEnchantment(Enchantment.ARROW_KNOCKBACK,2);
            inv.getBoots().addEnchantment(Enchantment.PROTECTION_FALL,2);
        }

        else if (type == Type.Tenebres) {
            ItemStack creation = CreationTenebres.getInstance().getCreationItem();
            inv.addItem(creation);
        }
    }

    public void resetStuff(NZPlayer nzPlayer) {
        PlayerInventory pInv = nzPlayer.getPlayer().getInventory();

        if (nzPlayer.getNZType().getType() != Type.Plante) {
            int gapplesSlot = pInv.first(Material.GOLDEN_APPLE);
            pInv.remove(Material.GOLDEN_APPLE);
            if (gapplesSlot == -1) pInv.addItem(new ItemStack(Material.GOLDEN_APPLE,4+nzPlayer.getPvpLevel()));
            else pInv.setItem(gapplesSlot, new ItemStack(Material.GOLDEN_APPLE,4+ nzPlayer.getPvpLevel()));
        }

        int waterSlot = pInv.contains(Material.WATER_BUCKET) ? pInv.first(Material.WATER_BUCKET) : pInv.first(Material.BUCKET);
        pInv.setItem(waterSlot,new ItemStack(Material.WATER_BUCKET));

        int cobbleSlot = pInv.first(Material.COBBLESTONE);
        pInv.remove(Material.COBBLESTONE);
        if (cobbleSlot == -1) pInv.addItem(new ItemStack(Material.COBBLESTONE,64));
        else pInv.setItem(cobbleSlot, new ItemStack(Material.COBBLESTONE,64));
    }

    public ItemStack giveArmor(Material mat, Enchantment enchant, int enchantLevel) {
        return giveItem(mat,1,true, new HashMap<Enchantment, Integer>() {{
            put(enchant, enchantLevel);
        }});
    }

    public ItemStack giveItem(Material mat, int amount, boolean unbreakable, Map<Enchantment,Integer> enchants) {
        ItemStack item = new ItemStack(mat,amount);
        if (unbreakable) ItemEditor.setUnbreakable(item);
        if (enchants != null) item.addUnsafeEnchantments(enchants);

        return item;
    }
}
