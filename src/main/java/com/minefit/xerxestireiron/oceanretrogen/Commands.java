package com.minefit.xerxestireiron.oceanretrogen;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final OceanRetrogen plugin;
    private final ConfigurationSection config;

    public Commands(OceanRetrogen instance) {
        this.plugin = instance;
        this.config = this.plugin.getConfig();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
        if (!command.getName().equalsIgnoreCase("retrogen") || arguments.length < 1) {
            return false;
        }

        if (arguments[0].equalsIgnoreCase("start")) {
            World mainWorld = Bukkit.getWorld(arguments[1]);
            World templateWorld = Bukkit.getWorld(arguments[2]);

            if (mainWorld == null) {
                sendMessage(sender, mainWorld + " is not a valid world!");
                return true;
            }

            if (templateWorld == null) {
                sendMessage(sender, templateWorld + " is not a valid world!");
                return true;
            }

            IterateChunks iterateChunks = new IterateChunks(this.plugin, mainWorld, templateWorld);
            this.plugin.worldIterators.put(mainWorld,
                    iterateChunks.runTaskTimer(this.plugin, 0, this.config.getInt("ticks-per-chunk", 1)));
            String message = "Beginning retrogen of world " + mainWorld.getName() + " using the world "
                    + templateWorld.getName() + " as template.";
            sendMessage(sender, message);
            return true;
        } else if (arguments[0].equalsIgnoreCase("stop")) {
            World mainWorld = Bukkit.getWorld(arguments[1]);

            if (mainWorld == null) {
                sendMessage(sender, mainWorld + " is not a valid world!");
                return true;
            }

            this.plugin.worldIterators.get(mainWorld).cancel();
            String message = "Halting retrogen of world " + mainWorld.getName() + ".";
            sendMessage(sender, message);
            return true;
        }

        return false;
    }

    private void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(message);
        } else {
            this.plugin.getLogger().info(message);
        }
    }
}