package me.chromisek.chromiumCore.commands;

import me.chromisek.chromiumCore.ChromiumCore;
import me.chromisek.chromiumCore.ChromiumLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChromiumCommand implements CommandExecutor, TabCompleter {
    
    private final ChromiumCore corePlugin;
    
    public ChromiumCommand(ChromiumCore corePlugin) {
        this.corePlugin = corePlugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                openMainGUI((Player) sender);
                break;
                
            case "reload":
                if (!sender.hasPermission("chromium.admin")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                    return true;
                }
                reloadPlugins(sender);
                break;
                
            case "version":
            case "ver":
                sendVersionInfo(sender);
                break;
                
            case "help":
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    private void openMainGUI(Player player) {
        // Try to find ChromiumGUI plugin
        Plugin guiPlugin = Bukkit.getPluginManager().getPlugin("ChromiumGUI");
        if (guiPlugin != null && guiPlugin.isEnabled()) {
            try {
                // Call the GUI manager through reflection to avoid direct dependency
                Object guiInstance = guiPlugin.getClass().getMethod("getInstance").invoke(null);
                Object guiManager = guiInstance.getClass().getMethod("getGUIManager").invoke(guiInstance);
                guiManager.getClass().getMethod("openMainGUI", Player.class).invoke(guiManager, player);
                
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                        ChatColor.GRAY + "» " + ChatColor.WHITE + "Opening main panel...");
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                        ChatColor.GRAY + "» " + ChatColor.WHITE + "Failed to open GUI. See console for details.");
                ChromiumLogger.severe("Failed to open GUI: " + e.getMessage());
                if (corePlugin.getGeneralConfig().getConfig().getBoolean("debug-mode", false)) {
                    e.printStackTrace();
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                    ChatColor.GRAY + "» " + ChatColor.WHITE + "ChromiumGUI plugin is not installed or enabled.");
        }
    }

    private void reloadPlugins(CommandSender sender) {
        try {
            // Always reload ChromiumCore
            corePlugin.getGeneralConfig().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                    ChatColor.GRAY + "» " + ChatColor.WHITE + "ChromiumCore config reloaded.");
            
            // Check for and reload ChromiumGUI
            Plugin guiPlugin = Bukkit.getPluginManager().getPlugin("ChromiumGUI");
            if (guiPlugin != null && guiPlugin.isEnabled()) {
                try {
                    // Call the reload method through reflection
                    Object guiInstance = guiPlugin.getClass().getMethod("getInstance").invoke(null);
                    guiInstance.getClass().getMethod("loadGuiItemsConfig").invoke(guiInstance);
                    
                    // Refresh GUIs
                    Object guiManager = guiInstance.getClass().getMethod("getGUIManager").invoke(guiInstance);
                    guiManager.getClass().getMethod("refreshAllGUIsCompletely").invoke(guiManager);
                    
                    sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "ChromiumGUI configuration reloaded.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "Failed to reload ChromiumGUI.");
                    ChromiumLogger.warning("Failed to reload ChromiumGUI: " + e.getMessage());
                }
            }
            
            // Check for and reload ChromiumReports
            Plugin reportsPlugin = Bukkit.getPluginManager().getPlugin("ChromiumReports");
            if (reportsPlugin != null && reportsPlugin.isEnabled()) {
                try {
                    // Call the reload method through reflection
                    Object reportsInstance = reportsPlugin.getClass().getMethod("getInstance").invoke(null);
                    
                    // Reload configuration
                    Object config = reportsInstance.getClass().getMethod("getConfiguration").invoke(reportsInstance);
                    config.getClass().getMethod("reloadConfig").invoke(config);
                    
                    // Reload GUI config
                    Object guiConfig = reportsInstance.getClass().getMethod("getGuiConfig").invoke(reportsInstance);
                    guiConfig.getClass().getMethod("reloadConfig").invoke(guiConfig);
                    
                    // Reload reports data
                    try {
                        // Check if reloadReports method exists, if not fall back to loading directly
                        reportsInstance.getClass().getMethod("reloadReports").invoke(reportsInstance);
                    } catch (NoSuchMethodException e) {
                        // Method doesn't exist, try to reload manually
                        ChromiumLogger.warning("reloadReports method not found, attempting manual reload");
                    }
                    
                    sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "ChromiumReports configuration and data reloaded.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "Failed to reload ChromiumReports.");
                    ChromiumLogger.warning("Failed to reload ChromiumReports: " + e.getMessage());
                }
            }
            
            // Check for and reload ChromiumWebhook
            Plugin webhookPlugin = Bukkit.getPluginManager().getPlugin("ChromiumWebhook");
            if (webhookPlugin != null && webhookPlugin.isEnabled()) {
                try {
                    webhookPlugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "ChromiumWebhook configuration reloaded.");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                            ChatColor.GRAY + "» " + ChatColor.WHITE + "Failed to reload ChromiumWebhook.");
                    ChromiumLogger.warning("Failed to reload ChromiumWebhook: " + e.getMessage());
                }
            }

            sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chromium " + 
                    ChatColor.GRAY + "» " + ChatColor.WHITE + "All plugins reloaded successfully!");
            ChromiumLogger.info("Configuration reloaded by " + sender.getName());

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Chromium " + 
                    ChatColor.GRAY + "» " + ChatColor.WHITE + "Error while reloading configuration!");
            ChromiumLogger.severe("Error reloading config: " + e.getMessage());
            if (corePlugin.getGeneralConfig().getConfig().getBoolean("debug-mode", false)) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendVersionInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Chromium Framework " + 
                ChatColor.GRAY + "- " + ChatColor.WHITE + "Version Information");
        sender.sendMessage("");
        
        // Core
        sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "ChromiumCore: " + 
                ChatColor.GREEN + corePlugin.getDescription().getVersion());
        
        // GUI
        Plugin guiPlugin = Bukkit.getPluginManager().getPlugin("ChromiumGUI");
        if (guiPlugin != null && guiPlugin.isEnabled()) {
            sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "ChromiumGUI: " + 
                    ChatColor.GREEN + guiPlugin.getDescription().getVersion());
        } else {
            sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "ChromiumGUI: " + 
                    ChatColor.RED + "Not loaded");
        }
        
        // Reports
        Plugin reportsPlugin = Bukkit.getPluginManager().getPlugin("ChromiumReports");
        if (reportsPlugin != null && reportsPlugin.isEnabled()) {
            sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "ChromiumReports: " + 
                    ChatColor.GREEN + reportsPlugin.getDescription().getVersion());
        } else {
            sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "ChromiumReports: " + 
                    ChatColor.RED + "Not loaded");
        }
        
        // Webhook
        Plugin webhookPlugin = Bukkit.getPluginManager().getPlugin("ChromiumWebhook");
        if (webhookPlugin != null && webhookPlugin.isEnabled()) {
            sender.sendMessage(ChatColor.WHITE + "└ " + ChatColor.YELLOW + "ChromiumWebhook: " + 
                    ChatColor.GREEN + webhookPlugin.getDescription().getVersion());
        } else {
            sender.sendMessage(ChatColor.WHITE + "└ " + ChatColor.YELLOW + "ChromiumWebhook: " + 
                    ChatColor.RED + "Not loaded");
        }
        
        sender.sendMessage("");
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage(ChatColor.GRAY + "Server: " + ChatColor.WHITE + player.getServer().getVersion());
            sender.sendMessage(ChatColor.GRAY + "API: " + ChatColor.WHITE + player.getServer().getBukkitVersion());
        } else {
            sender.sendMessage(ChatColor.GRAY + "Server: " + ChatColor.WHITE + Bukkit.getVersion());
            sender.sendMessage(ChatColor.GRAY + "API: " + ChatColor.WHITE + Bukkit.getBukkitVersion());
        }
        
        sender.sendMessage(ChatColor.GRAY + "Author: " + ChatColor.WHITE + "chromisek");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Chromium Framework " + 
                ChatColor.GRAY + "- " + ChatColor.WHITE + "Help Menu");
        sender.sendMessage("");
        
        sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "/chromium gui " + 
                ChatColor.GRAY + "- " + ChatColor.WHITE + "Open main control panel");
        sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "/chromium version " + 
                ChatColor.GRAY + "- " + ChatColor.WHITE + "Show version information");
        sender.sendMessage(ChatColor.WHITE + "└ " + ChatColor.YELLOW + "/chromium reload " + 
                ChatColor.GRAY + "- " + ChatColor.WHITE + "Reload configuration " + 
                ChatColor.RED + "(Admin)");
        
        // Show additional commands if plugins are loaded
        Plugin reportsPlugin = Bukkit.getPluginManager().getPlugin("ChromiumReports");
        if (reportsPlugin != null && reportsPlugin.isEnabled()) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "Reports Commands:");
            sender.sendMessage(ChatColor.WHITE + "├ " + ChatColor.YELLOW + "/report <player> " + 
                    ChatColor.GRAY + "- " + ChatColor.WHITE + "Report a player");
            sender.sendMessage(ChatColor.WHITE + "└ " + ChatColor.YELLOW + "/reports <player> " + 
                    ChatColor.GRAY + "- " + ChatColor.WHITE + "View reports for a player");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Direct GUI access: " + ChatColor.WHITE + "/gui " + 
                ChatColor.GRAY + "or " + ChatColor.WHITE + "/cgui");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("gui", "version", "help", "reload");
            String input = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    // Check permissions for admin commands
                    if (subcommand.equals("reload") && !sender.hasPermission("chromium.admin")) {
                        continue;
                    }
                    completions.add(subcommand);
                }
            }
        }
        
        return completions;
    }
}
