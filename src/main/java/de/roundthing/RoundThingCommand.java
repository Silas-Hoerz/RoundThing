package de.roundthing;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RoundThingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl ausführen.");
            return true;
        }
        Player spieler = (Player) sender;

        // Wir brauchen 5 Argumente: x y z durchmesser dicke
        if (args.length != 5) {
            spieler.sendMessage("§cFehler! Bitte benutze: /rt <x> <y> <z> <durchmesser> <dicke>");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            double durchmesser = Double.parseDouble(args[3]);
            int dicke = Integer.parseInt(args[4]); // Dicke ist meist eine ganze Zahl

            World welt = spieler.getWorld();

            // Wir rufen unsere Zeichen-Methode auf und geben ihr alle Infos, die sie braucht
            drawCircle(welt, x, y, z, durchmesser, dicke);

            spieler.sendMessage("§bEin Kreis wurde bei " + x + ", " + y + ", " + z + " gezeichnet!");

        } catch (NumberFormatException e) {
            spieler.sendMessage("§cFehler! Bitte gib gültige Zahlen für die Koordinaten an.");
        }

        return true;
    }

    // Die Methode ist nicht mehr static und bekommt die Welt und die Z-Koordinate übergeben
    public void drawCircle(World world, double centerX, double centerY, double centerZ, double durchmesser, int dicke) {
        double radius = durchmesser / 2.0;

        // Wir iterieren in einem Quadrat um den Mittelpunkt des Kreises
        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {

                double abstandVomZentrum = Math.sqrt(x*x + y*y);

                // Prüfen, ob der Punkt innerhalb des Rings liegt (Radius +/- halbe Dicke)
                if (abstandVomZentrum >= radius - (dicke / 2.0) && abstandVomZentrum <= radius + (dicke / 2.0)) {

                    double partikelX = centerX + x;
                    double partikelY = centerY + y;

                    // Wir rufen unsere Partikel-Methode für jeden Punkt des Kreises auf
                    spawnParticle(world, partikelX, partikelY, centerZ);
                }
            }
        }
    }

    // Eine simple Methode, die nur noch für das Spawnen eines Partikels zuständig ist
    private void spawnParticle(World world, double x, double y, double z) {
        // Wir erstellen die Optionen und die Location direkt hier
        Particle.DustOptions staubOptionen = new Particle.DustOptions(Color.BLUE, 1.0f);
        Location partikelPosition = new Location(world, x, y, z);

        // Partikel spawnen
        world.spawnParticle(Particle.DUST, partikelPosition, 1, 0, 0, 0, 0, staubOptionen);
    }
}