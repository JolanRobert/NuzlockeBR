package me.nosta.nuzlockebr.nztypes.capacity;

import me.nosta.nuzlockebr.enums.Power;
import me.nosta.nuzlockebr.game.NZPlayer;
import org.bukkit.inventory.ItemStack;

public interface Creation {

    public ItemStack getCreationItem();
    public String getCreationInfo();

    public Power choosePower();
    public void activeCreation(NZPlayer nzPlayer, ItemStack creation);
    public void castCreation(NZPlayer nzPlayer, Power power, int creationCooldown);

    public void enableCreation(NZPlayer nzPlayer);
    public void disableCreation(NZPlayer nzPlayer);
}
