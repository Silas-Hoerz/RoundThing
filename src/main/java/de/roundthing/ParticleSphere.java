package de.roundthing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class ParticleSphere {

    private final World world;
    private final double centerX, centerY, centerZ;
    private final double diameter;
    private final int thickness;
    private final Particle.DustOptions dustOptions;

    public ParticleSphere(Location center, double diameter, int thickness, Color color) {
        this.world = center.getWorld();
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.centerZ = center.getZ();
        this.diameter = diameter;
        this.thickness = thickness;
        this.dustOptions = new Particle.DustOptions(color, 1.5f);
    }

    public void draw() {
        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);
        Set<Location> particleLocations = new HashSet<>();

        Location centerBlockLocation = new Location(world, Math.floor(centerX) + 0.5, Math.floor(centerY) + 0.5, Math.floor(centerZ) + 0.5);
        world.spawnParticle(Particle.DUST, centerBlockLocation, 1, 0, 0, 0, 0, dustOptions);

        // Wir scannen jetzt einen Würfel (3D)
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    if (isVoxelAtEdgeOfSphere(x, y, z, radius, thickness)) {
                        Location particleLoc = new Location(
                                world,
                                Math.floor(this.centerX) + x,
                                Math.floor(this.centerY) + y,
                                Math.floor(this.centerZ) + z
                        );
                        particleLocations.add(particleLoc);
                    }
                }
            }
        }

        for (Location loc : particleLocations) {
            world.spawnParticle(Particle.DUST, loc.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0, dustOptions);
        }
    }

    private boolean isVoxelAtEdgeOfSphere(int x, int y, int z, double radius, int thickness) {
        // 3D-Distanzberechnung
        double distanceSquared = x * x + y * y + z * z;
        double radiusSquared = radius * radius;

        if (thickness >= radius) { // Gefüllte Kugel
            return distanceSquared <= radiusSquared;
        } else { // Hohle Kugel (Sphäre)
            double innerRadius = radius - thickness;
            double innerRadiusSquared = innerRadius * innerRadius;
            return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
        }
    }
    public int getParticleCount() {
        // Zählt den Mittelpunkt-Partikel
        int count = 1;

        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    if (isVoxelAtEdgeOfSphere(x, y, z, radius, thickness)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }


    // Getter für den StorageManager
    public World getWorld() { return world; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public double getDiameter() { return diameter; }
    public int getThickness() { return thickness; }
    public Color getColor() { return dustOptions.getColor(); }
}