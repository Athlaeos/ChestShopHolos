package me.athlaeos.chestshopholos.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.gmail.filoghost.holographicdisplays.object.line.CraftItemLine;
import com.gmail.filoghost.holographicdisplays.object.line.CraftTextLine;
import me.athlaeos.chestshopholos.Main;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.holographicdisplays.exception.HologramNotFoundException;
import com.gmail.filoghost.holographicdisplays.exception.InvalidFormatException;
import com.gmail.filoghost.holographicdisplays.exception.HologramLineParseException;
import com.gmail.filoghost.holographicdisplays.exception.WorldNotFoundException;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;
import com.gmail.filoghost.holographicdisplays.util.ConsoleLogger;

public class HologramDatabase {

    private static File file;
    private static FileConfiguration config;

    public static void loadYamlFile(Plugin plugin) {
        file = new File(plugin.getDataFolder(), "database.yml");

        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("database.yml", true);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public static NamedHologram loadHologram(String name) throws HologramNotFoundException, InvalidFormatException, WorldNotFoundException, HologramLineParseException {
        ConfigurationSection configSection = config.getConfigurationSection(name);
        if (configSection == null) {
            throw new HologramNotFoundException();
        }

        List<String> lines = configSection.getStringList("lines");
        String locationString = configSection.getString("location");

        if (locationString == null || lines.size() == 0) {
            throw new HologramNotFoundException();
        }

        Location loc = LocationSerializer.locationFromString(locationString);

        NamedHologram hologram = new NamedHologram(loc, name);

        for (String line : lines) {
            hologram.getLinesUnsafe().add(HologramLineParser.parseLine(hologram, line, false));
        }

        return hologram;
    }

    public static String serializeHologramLine(CraftHologramLine line) {
        if (line instanceof CraftItemLine){
            return "icon:" + ((CraftItemLine) line).getItemStack().getType().toString();
        } else if (line instanceof CraftTextLine){
            if (((CraftTextLine) line).getText().trim().equals("")) return "{empty}";
            return ((CraftTextLine) line).getText().replace('§', '&');
        }
        return line.getSerializedConfigValue();
    }

    public static void deleteHologram(String name) {
        config.set(name, null);
    }

    public static void saveHologram(NamedHologram hologram) {
        List<String> serializedLines = new ArrayList<>();
        for (CraftHologramLine line : hologram.getLinesUnsafe()) {
            String serializedLine = serializeHologramLine(line);
            serializedLines.add(serializedLine);
        }

        ConfigurationSection hologramSection = getOrCreateSection(hologram.getName());
        hologramSection.set("location", LocationSerializer.locationToString(hologram.getLocation()));
        hologramSection.set("lines", serializedLines);
    }

    public static Set<String> getHolograms() {
        return config.getConfigurationSection("").getKeys(false);
    }

    public static boolean isExistingHologram(String name) {
        return config.isConfigurationSection(name);
    }

    private static ConfigurationSection getOrCreateSection(String name) {
        if (config.isConfigurationSection(name)) {
            return config.getConfigurationSection(name);
        } else {
            return config.createSection(name);
        }
    }

    public static void saveToDisk() throws IOException {
        if (config != null && file != null) {
            config.save(file);
        }
    }

    public static void trySaveToDisk() {
        try {
            saveToDisk();
        } catch (IOException ex) {
            ConsoleLogger.log(Level.SEVERE, "Unable to save database.yml to disk!", ex);
        }
    }
}