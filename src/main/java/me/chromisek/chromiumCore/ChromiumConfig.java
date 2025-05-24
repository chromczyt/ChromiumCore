package me.chromisek.chromiumCore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ChromiumConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config = null;
    private File configFile = null;
    private final String configFileName;

    public ChromiumConfig(JavaPlugin plugin, String fileName) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;
        this.configFileName = fileName;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        this.configFile = new File(plugin.getDataFolder(), this.configFileName);
    }

    public void reloadConfig() {
        if (configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), this.configFileName);
        }
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defaultConfigStream = this.plugin.getResource(this.configFileName);
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            this.config.setDefaults(defaultConfig);
        }
    }
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return this.config;
    }
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(this.configFile);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, ex);
        }
    }
    public void saveDefaultConfig() {
        if (!this.configFile.exists()) {
            this.plugin.saveResource(this.configFileName, false);
        }
        reloadConfig();
    }
}
