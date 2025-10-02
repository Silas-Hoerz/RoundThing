/**
 * Represents a single particle circle. This class holds all properties of a circle,
 * such as its position, size, and rotation, and contains the logic for rendering it
 * and calculating its particle count.
 *
 * @author Silas Hörz
 * @version 1.0
 */
package de.roundthing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class ParticleCircle {

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
        this.dustOptions = new Particle.DustOptions(color, 1.5f);
    }

    /**
     * Draws the circle in the world by spawning particles.
     */
    public void draw() {
        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);

        double radX = Math.toRadians(this.rotationX);
        double radZ = Math.toRadians(this.rotationZ);
        double cosX = Math.cos(radX), sinX = Math.sin(radX);
        double cosZ = Math.cos(radZ), sinZ = Math.sin(radZ);

        Set<Location> particleLocations = new HashSet<>();

        // Draw the center marker particle
        Location centerBlockLocation = new Location(world, Math.floor(centerX) + 0.5, Math.floor(centerY) + 0.5, Math.floor(centerZ) + 0.5);
        world.spawnParticle(Particle.DUST, centerBlockLocation, 1, 0, 0, 0, 0, dustOptions);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                if (isVoxelOnShell(x, z, radius, thickness)) {
                    // Apply 3D rotation math
                    double yAfterX = -z * sinX;
                    double zAfterX = z * cosX;
                    double finalX = x * cosZ - yAfterX * sinZ;
                    double finalY = x * sinZ + yAfterX * cosZ;
                    double finalZ = zAfterX;

                    // Round to the nearest block to create the stair-step effect
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

    /**
     * Calculates the total number of unique particles this circle will generate.
     * @return The total particle count.
     */
    public int getParticleCount() {
        int count = 1; // Start with 1 for the center particle
        double radius = this.diameter / 2.0;
        int scanRadius = (int) Math.ceil(radius);

        Set<Long> uniquePositions = new HashSet<>();

        double radX = Math.toRadians(this.rotationX);
        double radZ = Math.toRadians(this.rotationZ);
        double cosX = Math.cos(radX), sinX = Math.sin(radX);
        double cosZ = Math.cos(radZ), sinZ = Math.sin(radZ);

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int z = -scanRadius; z <= scanRadius; z++) {
                if (isVoxelOnShell(x, z, radius, thickness)) {
                    double yAfterX = -z * sinX;
                    double zAfterX = z * cosX;
                    double finalX = x * cosZ - yAfterX * sinZ;
                    double finalY = x * sinZ + yAfterX * cosZ;
                    double finalZ = zAfterX;

                    long voxelOffsetX = Math.round(finalX);
                    long voxelOffsetY = Math.round(finalY);
                    long voxelOffsetZ = Math.round(finalZ);

                    // A simple way to encode a 3D position into a single long to count unique voxels
                    uniquePositions.add((voxelOffsetX << 42) + (voxelOffsetY << 21) + voxelOffsetZ);
                }
            }
        }
        count += uniquePositions.size();
        return count;
    }

    /**
     * Checks if a 2D voxel offset lies on the shell of the flat circle.
     * @return True if the voxel is part of the shape, false otherwise.
     */
    private boolean isVoxelOnShell(int x, int z, double radius, int thickness) {
        double distanceSquared = x * x + z * z;
        double radiusSquared = radius * radius;
        if (thickness >= radius) return distanceSquared <= radiusSquared; // Filled circle
        double innerRadius = radius - thickness;
        double innerRadiusSquared = innerRadius * innerRadius;
        return distanceSquared <= radiusSquared && distanceSquared >= innerRadiusSquared; // Hollow ring
    }

    // --- Getters for the StorageManager ---
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