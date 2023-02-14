package me.nosta.nuzlockebr.nztypes;

import me.nosta.nuzlockebr.Main;
import me.nosta.nuzlockebr.enums.Type;
import me.nosta.nuzlockebr.game.NZPlayer;
import me.nosta.nuzlockebr.game.NZType;
import me.nosta.nuzlockebr.managers.GameManager;
import me.nosta.nuzlockebr.nztypes.capacity.CreationLumiere;
import me.nosta.nuzlockebr.nztypes.capacity.CreationTenebres;
import me.nosta.nuzlockebr.nztypes.capacity.ArmorVisibility;
import me.nosta.nuzlockebr.runnables.UltimeCDRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Normal extends NZType {

    public Normal() {
        super(Type.Normal);
        this.ultimeName = "Puissance Cachée";
    }

    private final int ultimeTime = 20;

    private List<Integer> tasks = new ArrayList<>();

    public List<Type> baseTypes = new ArrayList<>();
    private final int baseAmount = 2;

    public List<Type> ultimeTypes = new ArrayList<>();
    private final int ultimeAmount = 3;

    private boolean hasCreationTenebres, hasCreationLumiere;
    public final int creationCooldown = 15;

    public long lastDamageTime = 0;

    @Override
    public String getDescription() {
        String description = addDescType()+
                addDescLine("Vous avez +1\u2764.")+
                addDescLine("Les pommes en or vous régénèrent 3\u2764 au lieu de 2.")+
                addDescLine("Votre adaptabilité vous permet d'obtenir une partie des pouvoirs de 2 autres types à chaque apparition (/nz adapt).")+
                " \n"+
                addDescUlti("Vous récupérez une partie des pouvoirs de 3 types supplémentaires pendant 20s. (120s)");
        return description;
    }

    @Override
    public void checkEffects() {
        if (baseTypes.size() == 0) baseAdapt();
        givePermanentEffects();
    }

    @Override
    public void givePermanentEffects() {
        giveEffects(baseTypes,Integer.MAX_VALUE);
        updateMaxHealth();
    }

    private void giveEffects(List<Type> checkList, int time) {
        if (checkList.contains(Type.Acier)) {
            if (!nzPlayer.hasEffect(PotionEffectType.DAMAGE_RESISTANCE)) nzPlayer.addEffect(PotionEffectType.DAMAGE_RESISTANCE,time,1);
        }
        if (checkList.contains(Type.Combat)) {
            if (!nzPlayer.hasEffect(PotionEffectType.INCREASE_DAMAGE)) nzPlayer.addEffect(PotionEffectType.INCREASE_DAMAGE,time,1);
        }
        if (checkList.contains(Type.Dragon)) crowdControlValue = 0.5f;
        if (checkList.contains(Type.Eau)) {
            if (!nzPlayer.hasEffect(PotionEffectType.DOLPHINS_GRACE)) nzPlayer.addEffect(PotionEffectType.DOLPHINS_GRACE,time,1);
            if (!nzPlayer.hasEffect(PotionEffectType.WATER_BREATHING)) nzPlayer.addEffect(PotionEffectType.WATER_BREATHING,time,1);
        }
        if (checkList.contains(Type.Fee) && ! hasCreationLumiere) {
            hasCreationLumiere = true;
            nzPlayer.getPlayer().getInventory().addItem(CreationLumiere.getInstance().getCreationItem());
        }
        if (checkList.contains(Type.Feu)) {
            if (!nzPlayer.hasEffect(PotionEffectType.FIRE_RESISTANCE)) nzPlayer.addEffect(PotionEffectType.FIRE_RESISTANCE,time,1);
        }
        if (checkList.contains(Type.Electrique)) {
            if (!nzPlayer.hasEffect(PotionEffectType.SPEED)) nzPlayer.addEffect(PotionEffectType.SPEED,time,2);
        }
        if (checkList.contains(Type.Plante)) {
            if (!nzPlayer.hasEffect(PotionEffectType.REGENERATION)) nzPlayer.addEffect(PotionEffectType.REGENERATION,time,1);
        }
        if (checkList.contains(Type.Tenebres) && !hasCreationTenebres) {
            hasCreationTenebres = true;
            nzPlayer.getPlayer().getInventory().addItem(CreationTenebres.getInstance().getCreationItem());
        }
        if (checkList.contains(Type.Vol)) {
            if (!nzPlayer.hasEffect(PotionEffectType.JUMP)) nzPlayer.addEffect(PotionEffectType.JUMP,time,4);
            nzPlayer.setNoFall(true);
        }
    }

    private void updateMaxHealth() {
        double healthValue = 22;
        if (hasType(Type.Dragon)) healthValue += 2;
        if (hasType(Type.Eau)) healthValue += 2;
        if (hasType(Type.Glace)) healthValue += 2;
        if (hasType(Type.Plante)) healthValue += 4;

        nzPlayer.setMaxHealth(healthValue);
    }

    public void removeCreationTenebres() {
        if (!hasCreationTenebres) return;

        hasCreationTenebres = false;
        CreationTenebres.getInstance().removeTask(nzPlayer);
        PlayerInventory pInv = nzPlayer.getPlayer().getInventory();
        pInv.remove(Material.ENDER_EYE);
        pInv.remove(Material.ENDER_PEARL);
    }

    public void removeCreationLumiere() {
        if (!hasCreationLumiere) return;

        hasCreationLumiere = false;
        CreationLumiere.getInstance().removeTask(nzPlayer);
        PlayerInventory pInv = nzPlayer.getPlayer().getInventory();
        pInv.remove(Material.ENDER_EYE);
        pInv.remove(Material.SNOWBALL);
    }

    public boolean hasType(Type type) {
        return baseTypes.contains(type) || ultimeTypes.contains(type);
    }

    @Override
    public void triggerUltime() {
        if (!canTriggerUltime()) return;

        tasks.clear();
        tasks.add(Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::cancelUltime, ultimeTime*20L));

        ultimeAdapt();
        giveEffects(ultimeTypes,ultimeTime);
        updateMaxHealth();

        strikeLighning();

        setUltimeTimeLeft(ultimeTime);
        setUltimeCooldown(120);
        new UltimeCDRunnable(this);
    }

    @Override
    public double handleMelee(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            if (hasType(Type.Feu)) {
                if (Math.random() < 0.7f) victim.getPlayer().setFireTicks(80);
            }

            if (hasType(Type.Spectre)) lastDamageTime = System.currentTimeMillis();

            if (victim.getNZType().isCCImmune()) return 0;

            if (hasType(Type.Insecte)) {
                allMalus(victim,0.035f);
            }

            if (hasType(Type.Poison)) {
                if (Math.random() < 0.6f) victim.addEffect(PotionEffectType.POISON,4,2);
            }
        }
        else if (victim == nzPlayer) {
            if (hasType(Type.Spectre)) lastDamageTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public double handleBow(EntityDamageByEntityEvent event, NZPlayer attacker, NZPlayer victim) {
        if (attacker == nzPlayer) {
            float bonusDamage = 0;
            if (hasType(Type.Psy)) bonusDamage += 0.3f;
            if (hasType(Type.Vol)) bonusDamage += 0.15f;

            if (hasType(Type.Feu)) {
                if (Math.random() < 0.35f) victim.getPlayer().setFireTicks(100);
            }

            if (hasType(Type.Spectre)) lastDamageTime = System.currentTimeMillis();

            if (victim.getNZType().isCCImmune()) return bonusDamage;

            if (hasType(Type.Glace)) {
                if (Math.random() < 0.35f) {
                    stunEnemy(victim,1.5f);
                }
            }
            if (hasType(Type.Poison)) {
                if (Math.random() < 0.3f) victim.addEffect(PotionEffectType.POISON,4,2);
            }
            if (hasType(Type.Psy)) {
                if (Math.random() < 0.2) {
                    victim.addEffect(PotionEffectType.LEVITATION,1,4);
                }

                if (Math.random() < 0.3) {
                    victim.addEffect(PotionEffectType.DARKNESS,3,1);
                }
            }

            return bonusDamage;
        }
        else if (victim == nzPlayer) {
            if (hasType(Type.Spectre)) lastDamageTime = System.currentTimeMillis();
        }

        return 0;
    }

    @Override
    public void passivePower() {
        if (hasType(Type.Spectre)) {
            if (System.currentTimeMillis() > lastDamageTime+10*1000L || lastDamageTime == 0) {
                if (ArmorVisibility.getVisibilityMode(nzPlayer) != ArmorVisibility.VisibilityMode.Nothing || !nzPlayer.hasEffect(PotionEffectType.INVISIBILITY)) {
                    ArmorVisibility.setVisibilityMode(nzPlayer, ArmorVisibility.VisibilityMode.Nothing);
                    nzPlayer.sendMessage(ChatColor.DARK_GRAY+"Vous êtes invisible !");
                }
            }
            else if (ArmorVisibility.getVisibilityMode(nzPlayer) == ArmorVisibility.VisibilityMode.Nothing) {
                ArmorVisibility.setVisibilityMode(nzPlayer, GameManager.getInstance().isAnonymous ? ArmorVisibility.VisibilityMode.NoArmor : ArmorVisibility.VisibilityMode.All);
                nzPlayer.sendMessage(ChatColor.DARK_GRAY+"Vous êtes visible !");
            }
        }
    }

    private void baseAdapt() {
        Random rdm = new Random();
        List<Type> types = new ArrayList<>(Arrays.asList(Type.values()));
        types.remove(Type.None);
        types.remove(Type.Normal);

        for (int i = 0; i < baseAmount; i++) {
            Type rdmType = types.get(rdm.nextInt(types.size()));
            while (baseTypes.contains(rdmType)) rdmType = types.get(rdm.nextInt(types.size()));
            baseTypes.add(rdmType);
        }

        givePermanentEffects();
        nzPlayer.heal();
        adaptInfo();
    }

    private void ultimeAdapt() {
        Random rdm = new Random();
        List<Type> types = new ArrayList<>(Arrays.asList(Type.values()));
        types.remove(Type.None);
        types.remove(Type.Normal);

        for (int i = 0; i < ultimeAmount; i++) {
            Type rdmType = types.get(rdm.nextInt(types.size()));
            while (baseTypes.contains(rdmType) || ultimeTypes.contains(rdmType)) rdmType = types.get(rdm.nextInt(types.size()));
            ultimeTypes.add(rdmType);
        }

        givePermanentEffects();
        adaptInfo();
    }

    private void adaptInfo() {
        List<Type> types = new ArrayList<>();
        types.addAll(baseTypes);
        types.addAll(ultimeTypes);

        String message = "Adaptabilité > ";
        for (int i = 0; i < types.size(); i++) {
            if (i != 0) message += ChatColor.WHITE+"/";
            message += types.get(i).getColor()+types.get(i).toString();
        }
        nzPlayer.sendMessage(message);
    }

    public static String getAdaptInfo() {
        return  " \n"+
                ChatColor.GOLD+"[NZ] "+ChatColor.DARK_AQUA+"Adaptations du type Normal :\n"+
                ChatColor.GRAY+"Acier > "+ChatColor.DARK_AQUA+"Résistance 1.\n"+
                ChatColor.DARK_RED+"Combat > "+ChatColor.DARK_AQUA+"Force 1.\n"+
                ChatColor.DARK_BLUE+"Dragon > "+ChatColor.DARK_AQUA+"Esquive les entraves (50%) et +1\u2764.\n"+
                ChatColor.BLUE+"Eau > "+ChatColor.DARK_AQUA+"Apnée/Grâce du Dauphin et +1\u2764.\n"+
                ChatColor.YELLOW+"Electrique > "+ChatColor.DARK_AQUA+"Vitesse 2.\n"+
                ChatColor.LIGHT_PURPLE+"Fée > "+ChatColor.DARK_AQUA+"Création de la Lumière (se recharge en 15s).\n"+
                ChatColor.RED+"Feu > "+ChatColor.DARK_AQUA+"Les coups peuvent mettre en feu (70% épée/35% arc) et Résistance au feu.\n"+
                ChatColor.AQUA+"Glace > "+ChatColor.DARK_AQUA+"Les flèches peuvent geler (35%/1.5s) et +1\u2764.\n"+
                ChatColor.DARK_GREEN+"Insecte > "+ChatColor.DARK_AQUA+"Les coups d'épée peuvent infliger un effet négatif (3.5% chaque effet)."+
                ChatColor.GREEN+"Plante > "+ChatColor.DARK_AQUA+"Régénération 1 et +2\u2764.\n"+
                ChatColor.DARK_PURPLE+"Poison > "+ChatColor.DARK_AQUA+"Les coups peuvent peuvent empoisonner (60% épée/30% arc).\n"+
                ChatColor.LIGHT_PURPLE+"Psy > "+ChatColor.DARK_AQUA+"Les flèches peuvent infliger Obscurité/Lévitation (30/20%) et +30% de dégâts à l'arc.\n"+
                ChatColor.DARK_GRAY+"Spectre > "+ChatColor.DARK_AQUA+"Invisibilité (se recharge en 10s).\n"+
                ChatColor.BLACK+"Ténèbres > "+ChatColor.DARK_AQUA+"Création des Ténèbres (se recharge en 15s).\n"+
                ChatColor.WHITE+"Vol > "+ChatColor.DARK_AQUA+"Sauts Améliorés 4, No Fall et +15% de dégâts à l'arc.\n";
    }

    public void cancelUltime() {
        super.cancelUltime();

        if (ultimeTypes.contains(Type.Tenebres)) removeCreationTenebres();
        if (ultimeTypes.contains(Type.Fee)) removeCreationLumiere();
        ultimeTypes.clear();

        crowdControlValue = 0;
        nzPlayer.setNoFall(false);
        updateMaxHealth();
    }

    public void resetRespawn() {
        super.resetRespawn();

        baseTypes.clear();

        lastDamageTime = 0;
        crowdControlValue = 0;
        removeCreationTenebres();
        removeCreationLumiere();
    }

    public void resetAll() {
        super.resetAll();
        tasks.clear();
        baseTypes.clear();
        ultimeTypes.clear();
        hasCreationTenebres = false;
        hasCreationLumiere = false;
        lastDamageTime = 0;
    }
}
