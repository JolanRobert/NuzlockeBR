package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class None extends NZType {

    public None() {
        super(Type.None);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void checkEffects() {

    }

    @Override
    public void givePermanentEffects() {

    }

    @Override
    public void triggerUltime() {

    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        return 0;
    }

    @Override
    public void passivePower() {

    }
}
