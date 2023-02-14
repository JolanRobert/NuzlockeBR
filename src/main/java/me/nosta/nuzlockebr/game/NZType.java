package me.nosta.nuzlockebr.game;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.utils.ItemEditor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class NZType {

    protected NZPlayer nzPlayer;
    private Type type;

    //Ultime
    protected String ultimeName;
    protected int ultiLocktime;
    protected int ultimeCooldown;
    protected int ultimeTimeLeft;

    //Other
    public float crowdControlValue;
    public boolean voixEnjoleuse;

    public NZType(Type type) {
        this.type = type;
    }

    public ItemStack getUltime() {
        ItemStack ultime = ItemEditor.getNewItem(Material.NETHER_STAR,ChatColor.GOLD+"[Ultime - "+ultimeName+"]");
        return ultime;
    }

    public boolean canTriggerUltime() {
        if (ultimeTimeLeft > 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Ultime déjà activé !");
            return false;
        }
        if (ultiLocktime > 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Ultime actuellement bloqué !");
            return false;
        }
        if (ultimeCooldown > 0) {
            nzPlayer.sendMessage(ChatColor.RED+"Ultime en cours de récupération !");
            return false;
        }
        return true;
    }

    public void cancelUltime() {
        setUltimeTimeLeft(0);
    }

    public void strikeLighning() {
        nzPlayer.getPlayer().getWorld().strikeLightningEffect(nzPlayer.getPlayer().getLocation());
        nzPlayer.getPlayer().getWorld().playSound(nzPlayer.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT,1,2);
    }

    public boolean isUltimeActivate() {return ultimeTimeLeft > 0;}
    public int getUltimeCooldown() {return ultimeCooldown;}
    public void setUltimeCooldown(int cooldown) {ultimeCooldown = Math.max(cooldown, 0);}
    public int getUltimeTimeLeft() {return ultimeTimeLeft;}
    public void setUltimeTimeLeft(int timeLeft) {ultimeTimeLeft = timeLeft;}
    public int getUltimeLocktime() {return ultiLocktime;}
    public void setUltimeLocktime(int timeLeft) {
        if (ultiLocktime == 0) {
            ultiLocktime = timeLeft;
            new BukkitRunnable() {
                @Override
                public void run() {
                    ultiLocktime--;
                    if (ultiLocktime == 0) this.cancel();
                }
            }.runTaskTimer(Main.getInstance(),20,20);
        }
        else ultiLocktime = timeLeft;
    }

    public boolean isCCImmune() {return (Math.random() < crowdControlValue);}
    public void stunEnemy(NZPlayer nzStuned, float time) {
        nzStuned.freezePlayer(time);
        nzPlayer.sendMessage(ChatColor.AQUA+"Vous avez étourdi "+nzStuned.getColoredName()+ChatColor.AQUA+" !");
        nzStuned.sendMessage(nzPlayer.getColoredName()+ChatColor.AQUA+" vous a étourdi !");
    }
    public void confuseEnemy(NZPlayer nzConfused) {
        nzConfused.setConfusion(3);
        nzPlayer.sendMessage(ChatColor.WHITE+"Vous avez rendu confus "+nzConfused.getColoredName()+ChatColor.WHITE+" !");
        nzConfused.sendMessage(nzPlayer.getColoredName()+ChatColor.WHITE+" vous a rendu confus !");
    }
    public void charmEnemy(NZPlayer nzCharmed) {
        nzCharmed.setCharme(1);
        nzPlayer.sendMessage(ChatColor.LIGHT_PURPLE+"Vous avez charmé "+nzCharmed.getColoredName()+ChatColor.LIGHT_PURPLE+" !");
        nzCharmed.sendMessage(nzPlayer.getColoredName()+ChatColor.LIGHT_PURPLE+" vous a charmé !");
    }

    public String addDescType() {
        return ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"Vous êtes de type "+type.getColor()+ChatColor.BOLD+type+"\n";
    }
    public String addDescLine(String info) {
        return ChatColor.GOLD+"> "+ChatColor.DARK_AQUA+info+"\n";
    }
    public String addDescUlti(String infoUlti) {
        return type.getColor()+"["+ultimeName+"] "+ChatColor.DARK_AQUA+infoUlti+"\n";
    }

    public void allMalus(NZPlayer victim, float chance) {
        if (Math.random() < chance) victim.addEffect(PotionEffectType.CONFUSION,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.BLINDNESS,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.DARKNESS,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.WEAKNESS,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.SLOW,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.POISON,5,1);
        if (Math.random() < chance) victim.addEffect(PotionEffectType.LEVITATION,5,1);
        if (Math.random() < chance) stunEnemy(victim,0.75f);
        if (Math.random() < chance) confuseEnemy(victim);
        if (Math.random() < chance) charmEnemy(victim);
    }

    public NZPlayer getNZPlayer() {return nzPlayer;}
    public void setNZPlayer(NZPlayer nzPlayer) {this.nzPlayer = nzPlayer;}
    public Type getType() {return type;}
    public String getName() {return type.name();}

    public abstract String getDescription();
    public abstract void checkEffects();
    public abstract void givePermanentEffects();
    public abstract void triggerUltime();
    public abstract double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim);
    public abstract double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim);
    public abstract void passivePower();

    public void resetRespawn() {
        setUltimeLocktime(0);
        cancelUltime();
    }

    public void resetAll() {
        cancelUltime();
        setNZPlayer(null);
        ultimeCooldown = 0;
        ultimeTimeLeft = 0;
        ultiLocktime = 0;

        crowdControlValue = 0;
    }
}
