package me.nosta.nuzlockebr.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapReload {

    private static List<Block> blocks = new ArrayList<>();
    private static List<Material> mats = new ArrayList<>();

    public static void restore() {
        Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.GREEN+"Réinitialisation de la carte... ("+blocks.size()+" opérations)");

        Collections.reverse(blocks);
        Collections.reverse(mats);
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setType(mats.get(i));
        }

        Broadcaster.messageAllOps(ChatColor.GOLD+"[NZ] "+ChatColor.GREEN+"Réinitialisation de la carte terminée !");
        blocks.clear();
        mats.clear();
    }

    public static void addEntry(Block b, Material mat) {
        blocks.add(b);
        mats.add(mat);
    }
}
