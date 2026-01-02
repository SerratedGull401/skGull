package org.serratedgull.skGull;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.serratedgull.skGull.profiler.SkriptProfiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for /skriptprofile
 */
public class ProfileCommand implements CommandExecutor, TabCompleter {

    private final SkriptProfiler profiler;

    public ProfileCommand(SkriptProfiler profiler) {
        this.profiler = profiler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skgull.profile")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                profiler.start();
                sender.sendMessage(ChatColor.GREEN + "Skript profiling started!");
                sender.sendMessage(ChatColor.GRAY + "Run scripts and use '/skriptprofile report' to view results");
                break;

            case "stop":
                if (!profiler.isProfiling()) {
                    sender.sendMessage(ChatColor.YELLOW + "Profiler is not currently running");
                } else {
                    profiler.stop();
                    sender.sendMessage(ChatColor.GREEN + "Skript profiling stopped!");
                    sender.sendMessage(ChatColor.GRAY + "Use '/skriptprofile report' to view results");
                }
                break;

            case "report":
            case "summary":
                int topCount = 10;
                if (args.length > 1) {
                    try {
                        topCount = Integer.parseInt(args[1]);
                        topCount = Math.max(1, Math.min(50, topCount));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                        return true;
                    }
                }
                profiler.sendSummary(sender, topCount);
                break;

            case "status":
                if (profiler.isProfiling()) {
                    sender.sendMessage(ChatColor.GREEN + "Profiler is currently running");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Profiler is not running");
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Skript Profiler Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/skriptprofile start" + ChatColor.GRAY + " - Start profiling");
        sender.sendMessage(ChatColor.YELLOW + "/skriptprofile stop" + ChatColor.GRAY + " - Stop profiling");
        sender.sendMessage(ChatColor.YELLOW + "/skriptprofile report [count]" + ChatColor.GRAY + " - View profiling results");
        sender.sendMessage(ChatColor.YELLOW + "/skriptprofile status" + ChatColor.GRAY + " - Check profiler status");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("skgull.profile")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("start", "stop", "report", "status");
        }

        return new ArrayList<>();
    }
}