package me.nosta.nuzlockebr.nztypes.capacity;

import com.mojang.datafixers.util.Pair;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.PlayerManager;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorVisibility {

    private static Map<NZPlayer,InvisibilityEntry> invisiblePlayers = new HashMap<>();
    private static List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> emptyArmor = new ArrayList<>();

    private static void initEquipmentList() {
        emptyArmor.add(new Pair<>(EnumItemSlot.c, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))));
        emptyArmor.add(new Pair<>(EnumItemSlot.d, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))));
        emptyArmor.add(new Pair<>(EnumItemSlot.e, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))));
        emptyArmor.add(new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR))));
    }

    public static List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> setArmor(NZPlayer nzPlayer) {
        PlayerInventory pInv = nzPlayer.getPlayer().getInventory();
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> armor = new ArrayList<>();
        armor.add(new Pair<>(EnumItemSlot.f, CraftItemStack.asNMSCopy(pInv.getHelmet())));
        armor.add(new Pair<>(EnumItemSlot.e, CraftItemStack.asNMSCopy(pInv.getChestplate())));
        armor.add(new Pair<>(EnumItemSlot.d, CraftItemStack.asNMSCopy(pInv.getLeggings())));
        armor.add(new Pair<>(EnumItemSlot.c, CraftItemStack.asNMSCopy(pInv.getBoots())));

        return armor;
    }

    public static void setVisibilityMode(NZPlayer nzPlayer, VisibilityMode mode) {
        if (!invisiblePlayers.containsKey(nzPlayer)) invisiblePlayers.put(nzPlayer, new InvisibilityEntry(null));

        InvisibilityEntry entry = invisiblePlayers.get(nzPlayer);
        entry.armor = setArmor(nzPlayer);
        entry.mode = mode;

        if (mode == VisibilityMode.Nothing) nzPlayer.addEffect(PotionEffectType.INVISIBILITY,Integer.MAX_VALUE,1);
        else nzPlayer.clearEffect(PotionEffectType.INVISIBILITY);

        if (mode == VisibilityMode.All) {
            sendVisibilityPacket(nzPlayer,entry.armor);
        }
    }

    public static VisibilityMode getVisibilityMode(NZPlayer nzPlayer) {
        return invisiblePlayers.get(nzPlayer).mode;
    }

    public static void refreshInvisibility() {
        if (emptyArmor.size() == 0) initEquipmentList();

        for (NZPlayer nzPlayer : invisiblePlayers.keySet()) {
            InvisibilityEntry entry = invisiblePlayers.get(nzPlayer);
            if (entry.mode != VisibilityMode.All) sendVisibilityPacket(nzPlayer,emptyArmor);
        }
    }

    public static void clearAllInvisibles() {
        for (NZPlayer nzPlayer : PlayerManager.getInstance().playerList) setVisibilityMode(nzPlayer,VisibilityMode.All);
        invisiblePlayers.clear();
    }

    private static void sendVisibilityPacket(NZPlayer nzPlayer, List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList) {
        for (NZPlayer item : PlayerManager.getInstance().playerList) {
            //if (nzPlayer == item) continue;

            EntityPlayer receiver = ((CraftPlayer)item.getPlayer()).getHandle();
            PlayerConnection playerConnection = receiver.b;
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(nzPlayer.getPlayer().getEntityId(),equipmentList);
            playerConnection.a(packet);
        }
    }

    public enum VisibilityMode {
        All,
        NoArmor,
        Nothing
    }

    public static class InvisibilityEntry {
        public List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> armor;
        public VisibilityMode mode;

        public InvisibilityEntry(List<Pair<EnumItemSlot, ItemStack>> armor) {
            this.armor = armor;
            this.mode = VisibilityMode.All;
        }
    }
}
