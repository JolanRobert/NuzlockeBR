package me.nosta.nuzlockebr.game;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.NZColor;
import me.nosta.nuzlockebr.enums.NZGameMode;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.managers.PlayerManager;
import me.nosta.nuzlockebr.managers.ScoreManager;
import me.nosta.nuzlockebr.managers.TypeManager;
import me.nosta.nuzlockebr.utils.Damager;
import me.nosta.nuzlockebr.utils.Disguiser;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class NZPlayer {

    private Player myPlayer;
    private NZType myType;
    private NZTeam myTeam;
    private NZStats myStats;

    private int pvpLevel = 1;

    //Trigger only one time
    private boolean noFall;
    private float noFallTime;

    public boolean isFreeze;

    //GameEffects
    protected boolean isWaterTrapped;
    private int confusion;
    private int charme;

    public NZPlayer(Player player) {
        myPlayer = player;
        myTeam = new NZTeam(this);
        myStats = new NZStats(this);
        resetAll();
    }

    public void resetAll() {
        myPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(100);
        myPlayer.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
        myStats.resetAll();

        if (myType != null) myType.resetAll();
        setNZType(TypeManager.getInstance().noneType);
        myTeam.resetAll();
        setTeam(NZColor.None);

        updateName();

        myPlayer.getInventory().clear();
        clearEffects();
        myPlayer.setAbsorptionAmount(0);
        setNoFall(false);
        setMaxHealth(20);
        heal();

        setWaterTrapped(false);
        setConfusion(0);
        setCharme(0);
    }

    /*
     * GET - SET
     */

    public Player getPlayer() {return myPlayer;}

    public NZColor getTeam() {return myTeam.getNZColor();}
    public void setTeam(NZColor nzColor) {
        myTeam.setNZColor(nzColor);
        updateName();
    }

    public NZType getNZType() {return myType;}
    public void setNZType(NZType nzType) {
        if (myType != null) myType.setNZPlayer(null);
        myType = nzType;
        if (nzType.getType() != Type.None) {
            nzType.setNZPlayer(this);
            myPlayer.sendMessage(myType.getDescription());
        }

        updateName();
    }

    public NZTeam getNZTeam() {return myTeam;}
    public NZStats getNZStats() {return myStats;}

    public int getPvpLevel(){return pvpLevel;}
    public void addPvpLevel() {
        pvpLevel++;
        if (pvpLevel > 3) pvpLevel = 1;
    }

    public boolean getNoFall() {return noFall;}
    public void setNoFall(boolean state) {noFall = state;}
    public void setTemporaryNoFall(float time) {
        setNoFall(true);
        if (noFallTime == 0) {
            noFallTime = time;
            new BukkitRunnable() {
                @Override
                public void run() {
                    noFallTime--;
                    if (noFallTime == 0) this.cancel();
                }
            }.runTaskTimer(Main.getInstance(),20,20);
        }
        else noFallTime = time;
    }

    public void setWaterTrapped(boolean state) {isWaterTrapped = state;}
    public boolean isWaterTrapped() {return isWaterTrapped;}

    public int getConfusion() {return confusion;}
    public void setConfusion(int value) {confusion = value;}

    public int getCharme() {return charme;}
    public void setCharme(int value) {charme = value;}

    public void resetRespawn() {
        myPlayer.setAbsorptionAmount(0);
        isFreeze = false;
        setNoFall(false);

        setWaterTrapped(false);
        setConfusion(0);
        setCharme(0);
    }

    /*
     * UTILITIES
     */

    public void updateName() {
        Type type = myType.getType();
        myTeam.setPrefix(type.getPrefix());
        if (GameManager.getInstance().gameMode == NZGameMode.FFA) {
            //myTeam.setPrefix("");
            myTeam.setColor(type.getColor());
        }
        else {
            /*if (myType == null || myType.getType() == Type.None) myTeam.setPrefix("");
            else myTeam.setPrefix(type.getPrefix()+" ");*/
            myTeam.setColor(myTeam.getNZColor().getColor());
        }
    }
    public String getName() {return myPlayer.getName();}
    public ChatColor getColor() {return myTeam.getChatColor();}
    public String getColoredName() {return myTeam.getChatColor()+getName();}
    public void setBounty(int bounty) {
        if (bounty == 0) myTeam.setSuffix("");
        else myTeam.setSuffix(ChatColor.GOLD+" ["+bounty+"pts]");
    }

    public void addEffect(PotionEffectType potionEffectType, float duration, int level) {
        if (duration == Integer.MAX_VALUE) duration = 1000000;
        myPlayer.addPotionEffect(new PotionEffect(potionEffectType,(int)(duration*20),level-1,false,false,true));
    }
    public void clearEffect(PotionEffectType potionEffectType) {
        myPlayer.removePotionEffect(potionEffectType);
    }
    public void clearEffects() {
        for (PotionEffect effect : myPlayer.getActivePotionEffects()) myPlayer.removePotionEffect(effect.getType());
    }
    public boolean hasEffect(PotionEffectType potionEffectType) {
        return myPlayer.hasPotionEffect(potionEffectType);
    }
    public PotionEffect getEffect(PotionEffectType potionEffectType) {
        if (!hasEffect(potionEffectType)) return null;
        return myPlayer.getPotionEffect(potionEffectType);
    }

    public void heal() {myPlayer.setHealth(myPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());}
    public void heal(double amount) {
        if (myPlayer.getHealth()+amount > myPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) heal();
        else myPlayer.setHealth(myPlayer.getHealth()+amount);
    }
    public int getHealth() {return (int)Math.round(myPlayer.getHealth()+myPlayer.getAbsorptionAmount());}
    public void setMaxHealth(double health) {
        myPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        if (myPlayer.getHealth() > getMaxHealth()) {
            myPlayer.setHealth(getMaxHealth());
            ScoreManager.getInstance().updateHealth(this,getHealth());
        }
    }
    public double getMaxHealth() {return myPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();}

    public void addAbsorption(double amount) {myPlayer.setAbsorptionAmount(myPlayer.getAbsorptionAmount()+amount);}
    public void setAbsorption(double amount) {myPlayer.setAbsorptionAmount(amount);}

    public void brutDamage(double amount, NZPlayer attacker) {
        if (myPlayer.getHealth() == 0.0) return;

        if (myPlayer.getHealth() <= amount) myPlayer.setHealth(0);
        else myPlayer.setHealth(myPlayer.getHealth()-amount);

        if (attacker != null) Damager.addEntry(this,attacker);

        myPlayer.damage(0.001f);
        ScoreManager.getInstance().updateHealth(this, getHealth());
    }

    public void sendMessage(String message) {
        if (GameManager.getInstance().isAnonymous) {
            message = Disguiser.anonymizeMessage(message);
        }
        myPlayer.sendMessage(message);
    }

    public Location getLocation() {return myPlayer.getLocation();}
    public void teleport(Location location) {myPlayer.teleport(location);}
    public Location getRespawnLocation() {
        int min = 0;
        int max = PlayerManager.getInstance().playerList.size()*12-1;

        int xPos = (int) ((Math.random() * ((max - min) + 1)) + min);
        int zPos = (int) ((Math.random() * ((max - min) + 1)) + min);

        if (Math.random() >= 0.5) xPos = -xPos;
        if (Math.random() >= 0.5) zPos = -zPos;

        int yPos = myPlayer.getWorld().getHighestBlockYAt(xPos,zPos)+1;
        return new Location(myPlayer.getWorld(),xPos+0.5f,yPos,zPos+0.5f);
    }

    public void swapPosition(NZPlayer nzSwap) {
        Location currentLoc = getLocation();
        this.teleport(nzSwap.getLocation());
        nzSwap.teleport(currentLoc);
    }

    public void freezePlayer(float time) {
        addEffect(PotionEffectType.JUMP,time,150);
        addEffect(PotionEffectType.SLOW,time,128);

        isFreeze = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> isFreeze = false, (long)(time*20));
    }

    public ItemStack getPlayerHead() {
        String displayName = myPlayer.getName();
        String lore = pvpLevel == 0 ? ChatColor.GOLD + "Level : Unset" : ChatColor.GOLD + "Level : " + pvpLevel;

        ItemStack head = ItemEditor.getPlayerHead(myPlayer,displayName,lore);
        return head;
    }
}
