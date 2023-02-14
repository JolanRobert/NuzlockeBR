package me.nosta.nuzlockebr.utils;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Structure {

    public static List<Location> generateCircle(Location centerBlock, int radius, int yOffset, boolean hollow) {
        List<Location> blocks = new ArrayList<>();
        int bX = centerBlock.getBlockX();
        int bZ = centerBlock.getBlockZ();

        for (int x = bX-radius; x <= bX+radius; x++) {
            for (int z = bZ-radius; z <= bZ+radius; z++) {
                double distance = (bX-x)*(bX-x)+(bZ-z)*(bZ-z);
                if (distance < radius*radius && !(hollow && distance < (radius-1)*(radius-1))) {
                    if (centerBlock.getWorld() == null) continue;
                    int y = centerBlock.getWorld().getHighestBlockYAt(x,z)+yOffset;
                    blocks.add(new Location(centerBlock.getWorld(),x,y,z));
                }
            }
        }

        return blocks;
    }

    public static List<Location> generatenZone(Location centerBlock, int radius) {
        List<Location> blocks = new ArrayList<>();
        int bX = centerBlock.getBlockX();
        int bY = centerBlock.getBlockY();
        int bZ = centerBlock.getBlockZ();

        for (int x = bX-radius; x <= bX+radius; x++) {
            for (int y = bY-radius; y < bY+radius; y++) {
                for (int z = bZ-radius; z <= bZ+radius; z++) {
                    double distance = (bX-x)*(bX-x)+(bZ-z)*(bZ-z);
                    if (distance < radius*radius) {
                        if (centerBlock.getWorld() == null) continue;
                        blocks.add(new Location(centerBlock.getWorld(),x,y,z));
                    }
                }
            }
        }
        return blocks;
    }

    public static List<Location> generateSphere(Location centerBlock, int radius, boolean hollow) {
        List<Location> blocks = new ArrayList<>();
        int bX = centerBlock.getBlockX();
        int bY = centerBlock.getBlockY();
        int bZ = centerBlock.getBlockZ();

        for (int x = bX-radius; x <= bX+radius; x++) {
            for (int y = bY-radius; y <= bY+radius; y++) {
                for (int z = bZ-radius; z <= bZ+radius; z++) {
                    double distance = (bX-x)*(bX-x)+(bY-y)*(bY-y)+(bZ-z)*(bZ-z);
                    if (distance < radius*radius && !(hollow && distance < (radius-1)*(radius-1))) {
                        blocks.add(new Location(centerBlock.getWorld(),x,y,z));
                    }
                }
            }
        }

        return blocks;
    }
}
