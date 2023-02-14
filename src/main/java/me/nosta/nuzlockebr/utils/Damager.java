package me.nosta.nuzlockebr.utils;

import me.nosta.nuzlockebr.game.NZPlayer;

import java.util.*;

public class Damager {

    private static int assistTime = 7;
    private static Map<NZPlayer, List<DamageEntry>> assists = new HashMap<>();

    public static void addEntry(NZPlayer victim, NZPlayer attacker) {
        if (!assists.containsKey(victim)) {
            assists.put(victim,new LinkedList<>());
        }

        List<DamageEntry> damageEntries = assists.get(victim);
        for (DamageEntry entry : damageEntries) {
            if (entry.attacker == attacker) {
                entry.updateTime();
                return;
            }
        }

        damageEntries.add(new DamageEntry(attacker,System.currentTimeMillis()));
    }

    public static List<NZPlayer> getAssistants(NZPlayer nzDead) {
        List<NZPlayer> nzPlayers = new ArrayList<>();
        if (assists.get(nzDead) == null) return nzPlayers;

        for (DamageEntry entry : assists.get(nzDead)) {
            if (System.currentTimeMillis() > entry.time+assistTime*1000L) continue;
            nzPlayers.add(entry.attacker);
        }

        assists.get(nzDead).clear();
        return nzPlayers;
    }

    private static class DamageEntry {
        public NZPlayer attacker;
        private long time;

        public DamageEntry(NZPlayer attacker, long time) {
            this.attacker = attacker;
            this.time = time;
        }

        public void updateTime() {this.time = System.currentTimeMillis();}
    }
}
