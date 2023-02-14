package me.nosta.nuzlockebr.runnables;

import me.nosta.nuzlockebr.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class DayNightRunnable extends BukkitRunnable {

    private int cycleTime;
    private int currentTime;

    public DayNightRunnable(int cycleTime) {
        this.cycleTime = cycleTime;
        currentTime = (int)Bukkit.getWorld("world").getTime();
        runTaskTimer(Main.getInstance(),0,5);
    }

    @Override
    public void run() {
        currentTime += 12000/cycleTime/4;
        if (currentTime == 24000) currentTime = 0;
        Bukkit.getWorld("world").setTime(currentTime);
    }
}
