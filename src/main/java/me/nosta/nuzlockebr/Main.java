package me.nosta.nuzlockebr;

import me.nosta.nuzlockebr.commands.CommandCompleter;
import me.nosta.nuzlockebr.commands.GameCommands;
import me.nosta.nuzlockebr.listeners.*;
import me.nosta.nuzlockebr.managers.GameManager;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class Main extends JavaPlugin {

    private static Main Instance;
    public static Main getInstance() {return Instance;}

    @Override
    public void onEnable() {
        Instance = this;

        applyGameRules();
        registerListeners();
        registerCommands();

        GameManager.getInstance().init();
    }

    private void applyGameRules() {
        World world = this.getServer().getWorld("world");

        if (world == null) return;
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_TILE_DROPS, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    }

    private void registerListeners() {
        registerListener(new BlockListener());
        registerListener(new ConnexionListener());
        registerListener(new InventoryListener());
        registerListener(new NZTypeListener());
        registerListener(new PlayerListener());
        registerListener(new RespawnDeathListener());
    }

    public void registerListener(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands() {
        this.getCommand("nz").setExecutor(GameCommands.getInstance());
        this.getCommand("nz").setTabCompleter(CommandCompleter.getInstance());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
