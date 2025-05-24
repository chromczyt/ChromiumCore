package me.chromisek.chromiumCore;

import org.bukkit.ChatColor;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ChromiumLogger {
    private static Logger logger;
    private static String pluginPrefix;
    private ChromiumLogger() {}

    public static void init(org.bukkit.plugin.java.JavaPlugin pluginInstace, String prefix) {
        logger = pluginInstace.getLogger();
        pluginPrefix = prefix + " ";
    }
    public static void info(String message) {
        if (logger == null) {
            System.out.println("[ChromiumLogger INFO] Logger not initialized! Message: " + message);
            return;
        }
        logger.log(Level.INFO, pluginPrefix + ChatColor.GREEN + message + ChatColor.RESET);
    }
    public static void warning(String message) {
        if (logger == null) {
            System.out.println("[ChromiumLogger WARNING] Logger not initialized! Message (WARNING): " + message);
        }
        logger.log(Level.WARNING, pluginPrefix + ChatColor.YELLOW + message + ChatColor.RESET);
    }
    public static void severe(String message) {
        if (logger == null) {
            System.out.println("[ChromiumLogger SEVERE] Logger not initialized! Message (SEVERE):" + message);
        }
        logger.log(Level.SEVERE, pluginPrefix + ChatColor.RED + message + ChatColor.RESET);
    }
    public static void debug(String message) {
        if (logger == null) {
            System.out.println("[ChromiumLogger DEBUG] Logger not initialized! Message (DEBUG): " + message);
        }
        logger.log(Level.INFO, pluginPrefix + ChatColor.GRAY + "[DEBUG]" + message + ChatColor.RESET);
    }
}
