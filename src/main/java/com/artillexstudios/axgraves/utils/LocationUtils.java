package com.artillexstudios.axgraves.utils;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axgraves.AxGraves.CONFIG;

public class LocationUtils {

    @NotNull
    public static Location getCenterOf(@NotNull Location location, boolean keepYaw, boolean keepPitch) {
        Location loc = location.getBlock().getLocation().add(0.5, 0.5, 0.5);
        if (keepYaw) loc.setYaw(location.getYaw());
        if (keepPitch) loc.setPitch(location.getPitch());
        return loc;
    }

    public static int getNearestDirection(float x) {
        return Math.round(x / 90f) * 90;
    }

    public static void clampLocation(Location location) {
        Section section = CONFIG.getSection("spawn-height-limits." + location.getWorld().getName());
        double min, max;
        if (section != null) {
            min = section.getDouble("min");
            max = section.getDouble("max");
        } else {
            switch (location.getWorld().getEnvironment()) {
                case NETHER, THE_END -> {
                    min = 0;
                    max = 255;
                }
                default -> {
                    min = -64;
                    max = 319;
                }
            }
        }
        location.setY(Math.clamp(location.getY(), min, max));

        // If the grave was clamped onto the void floor (no solid support underneath),
        // relocate it to the nearest solid block so players can actually see and reach it.
        World world = location.getWorld();
        if (world != null && location.getY() <= min + 1.0 && !hasSolidSupport(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), (int) min)) {
            Location safe = findNearestSafeLocation(world, location.getBlockX(), location.getBlockZ(), (int) min, (int) max);
            if (safe != null) {
                location.setX(safe.getX());
                location.setY(safe.getY());
                location.setZ(safe.getZ());
            } else {
                Location spawn = world.getSpawnLocation();
                location.setX(spawn.getBlockX() + 0.5);
                location.setY(Math.clamp(spawn.getY(), min, max));
                location.setZ(spawn.getBlockZ() + 0.5);
            }
        }
    }

    private static boolean hasSolidSupport(World world, int x, int y, int z, int minY) {
        if (y - 1 < minY) {
            return false;
        }
        Block below = world.getBlockAt(x, y - 1, z);
        return !below.isPassable() && !below.isLiquid();
    }

    private static Location findNearestSafeLocation(World world, int originX, int originZ, int minY, int maxY) {
        Location sameColumn = safeInColumn(world, originX, originZ, minY, maxY);
        if (sameColumn != null) {
            return sameColumn;
        }
        for (int radius = 2; radius <= 16; radius += 2) {
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    Location candidate = safeInColumn(world, originX + dx, originZ + dz, minY, maxY);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static Location safeInColumn(World world, int x, int z, int minY, int maxY) {
        int highestY = world.getHighestBlockYAt(x, z);
        if (highestY < minY || highestY + 1 > maxY) {
            return null;
        }
        Block support = world.getBlockAt(x, highestY, z);
        if (support.isPassable() || support.isLiquid()) {
            return null;
        }
        return new Location(world, x + 0.5, highestY + 1.0, z + 0.5);
    }

    @NotNull
    public static String getWorldName(World world) {
        if (world == null) return "---";
        return CONFIG.getString("world-name." + world.getName(), world.getName());
    }
}
