package org.serratedgull.skGull.profiler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Performance profiler for Skript scripts
 * Tracks execution time and frequency of Skript events and sections
 */
public class SkriptProfiler {

    private boolean profiling = false;
    private long startTime;

    // Map: script name -> ProfileData
    private final Map<String, ProfileData> profileData = new ConcurrentHashMap<>();

    /**
     * Start profiling
     */
    public void start() {
        if (profiling) {
            return;
        }

        profiling = true;
        startTime = System.currentTimeMillis();
        profileData.clear();

        Bukkit.getLogger().info("[skGull] Profiling started");
    }

    /**
     * Stop profiling
     */
    public void stop() {
        if (!profiling) {
            return;
        }

        profiling = false;
        Bukkit.getLogger().info("[skGull] Profiling stopped");
    }

    /**
     * Check if currently profiling
     */
    public boolean isProfiling() {
        return profiling;
    }

    /**
     * Record execution time for a script section
     */
    public void recordExecution(String scriptName, String section, long executionTimeNanos) {
        if (!profiling) {
            return;
        }

        profileData.computeIfAbsent(scriptName, k -> new ProfileData(scriptName))
                .recordExecution(section, executionTimeNanos);
    }

    /**
     * Get profiling summary
     */
    public String getSummary(int topCount) {
        if (profileData.isEmpty()) {
            return ChatColor.YELLOW + "No profiling data available. Start profiling with /skriptprofile start";
        }

        long totalTime = System.currentTimeMillis() - startTime;

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GOLD).append("=== Skript Performance Profile ===\n");
        sb.append(ChatColor.GRAY).append("Total profiling time: ")
                .append(formatTime(totalTime * 1_000_000)).append("\n\n");

        // Get top scripts by total execution time
        List<ProfileData> sortedScripts = profileData.values().stream()
                .sorted(Comparator.comparingLong(ProfileData::getTotalTime).reversed())
                .limit(topCount)
                .collect(Collectors.toList());

        sb.append(ChatColor.AQUA).append("Top ").append(topCount).append(" Slowest Scripts:\n");

        int rank = 1;
        for (ProfileData data : sortedScripts) {
            sb.append(ChatColor.WHITE).append(rank++).append(". ")
                    .append(ChatColor.YELLOW).append(data.getScriptName()).append("\n");
            sb.append(ChatColor.GRAY).append("   Total time: ")
                    .append(formatTime(data.getTotalTime())).append("\n");
            sb.append(ChatColor.GRAY).append("   Executions: ")
                    .append(data.getTotalExecutions()).append("\n");
            sb.append(ChatColor.GRAY).append("   Avg time: ")
                    .append(formatTime(data.getAverageTime())).append("\n");

            // Show top sections within this script
            List<Map.Entry<String, SectionData>> topSections = data.getTopSections(3);
            if (!topSections.isEmpty()) {
                sb.append(ChatColor.GRAY).append("   Top sections:\n");
                for (Map.Entry<String, SectionData> entry : topSections) {
                    sb.append(ChatColor.DARK_GRAY).append("     - ")
                            .append(entry.getKey()).append(": ")
                            .append(formatTime(entry.getValue().getTotalTime()))
                            .append(" (").append(entry.getValue().getExecutions()).append("x)\n");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Format nanoseconds to readable time
     */
    private String formatTime(long nanos) {
        if (nanos < 1000) {
            return nanos + "ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2fµs", nanos / 1000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2fms", nanos / 1_000_000.0);
        } else {
            return String.format("%.2fs", nanos / 1_000_000_000.0);
        }
    }

    /**
     * Send summary to a command sender
     */
    public void sendSummary(CommandSender sender, int topCount) {
        String summary = getSummary(topCount);
        for (String line : summary.split("\n")) {
            sender.sendMessage(line);
        }
    }
}

/**
 * Profile data for a single script
 */
class ProfileData {
    private final String scriptName;
    private final Map<String, SectionData> sections = new ConcurrentHashMap<>();

    public ProfileData(String scriptName) {
        this.scriptName = scriptName;
    }

    public void recordExecution(String section, long timeNanos) {
        sections.computeIfAbsent(section, k -> new SectionData())
                .recordExecution(timeNanos);
    }

    public String getScriptName() {
        return scriptName;
    }

    public long getTotalTime() {
        return sections.values().stream()
                .mapToLong(SectionData::getTotalTime)
                .sum();
    }

    public long getTotalExecutions() {
        return sections.values().stream()
                .mapToLong(SectionData::getExecutions)
                .sum();
    }

    public long getAverageTime() {
        long executions = getTotalExecutions();
        return executions > 0 ? getTotalTime() / executions : 0;
    }

    public List<Map.Entry<String, SectionData>> getTopSections(int count) {
        return sections.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> -e.getValue().getTotalTime()))
                .limit(count)
                .collect(Collectors.toList());
    }
}

/**
 * Profile data for a section within a script
 */
class SectionData {
    private long totalTime = 0;
    private long executions = 0;

    public synchronized void recordExecution(long timeNanos) {
        totalTime += timeNanos;
        executions++;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getExecutions() {
        return executions;
    }
}