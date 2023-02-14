package me.nosta.nuzlockebr.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Glower {

    //Pour chaque joueur, la liste des joueurs en glow
    private static Map<NZPlayer, List<GlowingEntry>> glows = new HashMap<>();

    public static void initGlow() {
        glows.clear();

        for (NZPlayer observer : PlayerManager.getInstance().playerList) {
            glows.put(observer, new LinkedList<>());

            List<GlowingEntry> glowingEntries = glows.get(observer);
            for (NZPlayer glowingPlayer : PlayerManager.getInstance().playerList) {
                glowingEntries.add(new GlowingEntry(glowingPlayer, false));
            }
        }
    }

    public static void setGlow(NZPlayer observer, NZPlayer glowingPlayer, boolean shouldGlow) {
        for (GlowingEntry entry : glows.get(observer)) {
            if (entry.nzPlayer == glowingPlayer) {
                entry.shouldGlow = shouldGlow;
                return;
            }
        }
    }

    public static void glowMates() {
        for (NZPlayer observer : PlayerManager.getInstance().playerList) {
            for (NZPlayer glowingPlayer : PlayerManager.getInstance().playerList) {
                if (observer == glowingPlayer) continue;
                if (GameManager.getInstance().gameMode == NZGameMode.FFA || observer.getTeam() != glowingPlayer.getTeam()) continue;
                setGlow(observer,glowingPlayer,true);
            }
        }
    }

    public static void clearAllGlow() {
        for (NZPlayer observer : PlayerManager.getInstance().playerList) {
            for (NZPlayer glowingPlayer : PlayerManager.getInstance().playerList) {
                setGlowing(observer.getPlayer(), glowingPlayer.getPlayer(), false);
            }
        }
    }

    public static void refreshGlow() {
        for (NZPlayer observer : glows.keySet()) {
            for (GlowingEntry entry : glows.get(observer)) {
                setGlowing(observer.getPlayer(), entry.nzPlayer.getPlayer(), entry.shouldGlow);
            }
        }
    }

    private static void setGlowing(Player observer, Player glowingPlayer, boolean glow) {
        try {
            Entity entity = ((CraftPlayer) glowingPlayer).getHandle();

            DataWatcher toCloneDataWatcher = entity.ai();
            DataWatcher newDataWatcher = new DataWatcher(entity);

            Int2ObjectMap<DataWatcher.Item<Byte>> newMap = (Int2ObjectMap<DataWatcher.Item<Byte>>) FieldUtils.readDeclaredField(toCloneDataWatcher, "f", true);

            DataWatcher.Item<Byte> item = newMap.get(0);
            byte initialBitMask = item.b();
            byte bitMaskIndex = (byte) 6;
            if (glow) item.a((byte) (initialBitMask | 1 << bitMaskIndex));
            else item.a((byte) (initialBitMask & ~(1 << bitMaskIndex)));

            FieldUtils.writeDeclaredField(newDataWatcher, "f", newMap, true);
            PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(glowingPlayer.getEntityId(), newDataWatcher, true);

            ((CraftPlayer) observer).getHandle().b.a(metadataPacket);
        } catch (IllegalAccessException ignored) {}
    }

    private static class GlowingEntry {
        public NZPlayer nzPlayer;
        public boolean shouldGlow;

        public GlowingEntry(NZPlayer nzPlayer, boolean shouldGlow) {
            this.nzPlayer = nzPlayer;
            this.shouldGlow = shouldGlow;
        }
    }
}
