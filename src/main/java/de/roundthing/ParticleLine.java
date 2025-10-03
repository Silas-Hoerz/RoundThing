package de.roundthing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class ParticleLine {

    private final World world;
    private final Location start, end;
    private final Particle.DustOptions dustOptions;

    public ParticleLine(Location start, Location end, Color color) {
        this.world = start.getWorld();
        this.start = start;
        this.end = end;
        this.dustOptions = new Particle.DustOptions(color, 1.5f);
    }

    private Set<Location> calculateVoxelPositions() {
        Set<Location> locations = new HashSet<>();
        int x1 = start.getBlockX(), y1 = start.getBlockY(), z1 = start.getBlockZ();
        int x2 = end.getBlockX(), y2 = end.getBlockY(), z2 = end.getBlockZ();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        if (dx >= dy && dx >= dz) { // X ist die dominante Achse
            int err1 = 2 * dy - dx;
            int err2 = 2 * dz - dx;
            while (x1 != x2) {
                locations.add(new Location(world, x1, y1, z1));
                if (err1 > 0) { y1 += sy; err1 -= 2 * dx; }
                if (err2 > 0) { z1 += sz; err2 -= 2 * dx; }
                err1 += 2 * dy;
                err2 += 2 * dz;
                x1 += sx;
            }
        } else if (dy >= dx && dy >= dz) { // Y ist die dominante Achse
            int err1 = 2 * dx - dy;
            int err2 = 2 * dz - dy;
            while (y1 != y2) {
                locations.add(new Location(world, x1, y1, z1));
                if (err1 > 0) { x1 += sx; err1 -= 2 * dy; }
                if (err2 > 0) { z1 += sz; err2 -= 2 * dy; }
                err1 += 2 * dx;
                err2 += 2 * dz;
                y1 += sy;
            }
        } else { // Z ist die dominante Achse
            int err1 = 2 * dx - dz;
            int err2 = 2 * dy - dz;
            while (z1 != z2) {
                locations.add(new Location(world, x1, y1, z1));
                if (err1 > 0) { x1 += sx; err1 -= 2 * dz; }
                if (err2 > 0) { y1 += sy; err2 -= 2 * dz; }
                err1 += 2 * dx;
                err2 += 2 * dy;
                z1 += sz;
            }
        }
        locations.add(new Location(world, x2, y2, z2)); // Füge den Endpunkt hinzu
        return locations;
    }

    public void draw() {
        Set<Location> locations = calculateVoxelPositions();
        for (Location loc : locations) {
            world.spawnParticle(Particle.DUST, loc.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0, dustOptions);
        }
    }

    public int getParticleCount() {
        return calculateVoxelPositions().size();
    }

    // Getter für den StorageManager
    public Location getStart() { return start; }
    public Location getEnd() { return end; }
    public World getWorld() { return world; }
    public Color getColor() { return dustOptions.getColor(); }
}