package me.nosta.nuzlockebr.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.List;

public class ItemEditor {

    public static void setDisplayName(ItemStack item, String displayName) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RESET+displayName);
        item.setItemMeta(itemMeta);
    }

    public static void setLore(ItemStack item, String lore) {
        ItemMeta itemMeta = item.getItemMeta();
        if (lore == null) itemMeta.setLore(null);
        else itemMeta.setLore(Collections.singletonList(lore));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);
    }

    public static void setLore(ItemStack item, List<String> lore) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);
    }

    public static void setUnbreakable(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(itemMeta);
    }

    public static ItemStack getNewItem(Material mat, String displayName) {
        ItemStack newItem = new ItemStack(mat);
        ItemMeta itemMeta = newItem.getItemMeta();
        itemMeta.setDisplayName(displayName);
        newItem.setItemMeta(itemMeta);

        return newItem;
    }

    public static ItemStack getNewItem(Material mat, String displayName, String lore) {
        ItemStack newItem = getNewItem(mat,displayName);
        setLore(newItem,lore);
        return newItem;
    }

    public static ItemStack getNewItem(Material mat, String displayName, List<String> lore) {
        ItemStack newItem = getNewItem(mat,displayName);
        setLore(newItem,lore);
        return newItem;
    }

    public static ItemStack getNewItem(Material mat, String displayName, String lore, boolean unbreakable) {
        ItemStack newItem = getNewItem(mat,displayName,lore);
        setUnbreakable(newItem);
        return newItem;
    }

    public static ItemStack getNewItem(Material mat, String displayName, List<String> lore, boolean unbreakable) {
        ItemStack newItem = getNewItem(mat,displayName,lore);
        setLore(newItem,lore);
        return newItem;
    }

    public static ItemStack getPlayerHead(Player player) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta)skullItem.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    public static ItemStack getPlayerHead(Player player, String displayName, String lore) {
        ItemStack skullItem = getPlayerHead(player);
        setDisplayName(skullItem,displayName);
        setLore(skullItem,lore);
        return skullItem;
    }
}
