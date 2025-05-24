package me.chromisek.chromiumCore;

import org.bukkit.plugin.java.JavaPlugin;

public final class ChromiumCore extends JavaPlugin {
    private static ChromiumCore instance;
    private ChromiumConfig generalConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        ChromiumLogger.init(this,"[" + this.getDescription().getName() + "]");

        this.generalConfig = new ChromiumConfig(this, "config.yml");
        this.generalConfig.saveDefaultConfig();

        ChromiumLogger.info("Plugin enabled");
        ChromiumLogger.info("General config loaded: " + generalConfig.getConfig().getString("welcome-message"));
        ChromiumLogger.info("Debug mode: " + generalConfig.getConfig().getBoolean("debug-mode"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ChromiumLogger.info("Plugin disabled");
    }
    public static ChromiumCore getInstance(){
        return instance;
    }
    public ChromiumConfig getGeneralConfig() {
        return generalConfig;
    }
}
