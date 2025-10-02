package de.roundthing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParticleCircle {
    // Getter-Methoden hinzugefügt, damit der StorageManager die Daten auslesen kann
    private final World world;
    private final double centerX, centerY, centerZ;
    private final double diameter;
    private final int thickness;
    private final double rotationX;
    private final double rotationZ;
    private final Color color;
    private final Particle.DustOptions dustOptions;

    public ParticleCircle(Location center, double diameter, int thickness, Color color, double rotationX, double rotationZ) {
        this.world = center.getWorld();
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.centerZ = center.getZ();
        this.diameter = diameter;
        this.thickness = thickness;
        this.color = color;
        this.rotationX = rotationX;
        this.rotationZ = rotationZ;
        // Feature 3: Intensivere Partikel durch größere Partikelgröße
        this.dustOptions = new Particle.DustOptions(color, 1.5f);
    }

    public void draw() {
        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);

        double radX = Math.toRadians(this.rotationX);
        double radZ = Math.toRadians(this.rotationZ);
        double cosX = Math.cos(radX), sinX = Math.sin(radX);
        double cosZ = Math.cos(radZ), sinZ = Math.sin(radZ);

        Set<Location> particleLocations = new HashSet<>();

        // Feature 1: Mittelpunkt-Marker anpassen
        Location centerBlockLocation = new Location(world, Math.floor(centerX) + 0.5, Math.floor(centerY) + 0.5, Math.floor(centerZ) + 0.5);
        world.spawnParticle(Particle.DUST, centerBlockLocation, 1, 0, 0, 0, 0, dustOptions);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                if (isVoxelAtEdge(x, z, radius, thickness)) {
                    double y_nach_x = -z * sinX;
                    double z_nach_x = z * cosX;
                    double finalX = x * cosZ - y_nach_x * sinZ;
                    double finalY = x * sinZ + y_nach_x * cosZ;
                    double finalZ = z_nach_x;

                    long voxelOffsetX = Math.round(finalX);
                    long voxelOffsetY = Math.round(finalY);
                    long voxelOffsetZ = Math.round(finalZ);

                    Location particleLoc = new Location(
                            world,
                            Math.floor(this.centerX) + voxelOffsetX,
                            Math.floor(this.centerY) + voxelOffsetY,
                            Math.floor(this.centerZ) + voxelOffsetZ
                    );
                    particleLocations.add(particleLoc);
                }
            }
        }

        for (Location loc : particleLocations) {
            world.spawnParticle(Particle.DUST, loc.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0, dustOptions);
        }
    }

    public int getParticleCount() {
        // Zählt den Mittelpunkt-Partikel
        int count = 1;

        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);

        // Wir nutzen ein Set, um doppelte Positionen zu vermeiden, genau wie bei draw()
        Set<Long> uniquePositions = new HashSet<>();

        double radX = Math.toRadians(this.rotationX);
        double radZ = Math.toRadians(this.rotationZ);
        double cosX = Math.cos(radX), sinX = Math.sin(radX);
        double cosZ = Math.cos(radZ), sinZ = Math.sin(radZ);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                if (isVoxelAtEdge(x, z, radius, thickness)) {
                    double y_nach_x = -z * sinX;
                    double z_nach_x = z * cosX;
                    double finalX = x * cosZ - y_nach_x * sinZ;
                    double finalY = x * sinZ + y_nach_x * cosZ;
                    double finalZ = z_nach_x;

                    long voxelOffsetX = Math.round(finalX);
                    long voxelOffsetY = Math.round(finalY);
                    long voxelOffsetZ = Math.round(finalZ);

                    // Ein einfacher Weg, eine 3D-Position in einer einzigen Zahl zu kodieren
                    uniquePositions.add((voxelOffsetX << 42) + (voxelOffsetY << 21) + voxelOffsetZ);
                }
            }
        }
        count += uniquePositions.size();
        return count;
    }

    private boolean isVoxelAtEdge(int x, int z, double radius, int thickness) {
        double distanceSquared = x * x + z * z;
        double radiusSquared = radius * radius;
        if (thickness >= radius) return distanceSquared <= radiusSquared;
        double innerRadius = radius - thickness;
        double innerRadiusSquared = innerRadius * innerRadius;
        return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared;
    }

    // Getter für den StorageManager
    public World getWorld() { return world; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getCenterZ() { return centerZ; }
    public double getDiameter() { return diameter; }
    public int getThickness() { return thickness; }
    public double getRotationX() { return rotationX; }
    public double getRotationZ() { return rotationZ; }
    public Color getColor() { return color; }
}