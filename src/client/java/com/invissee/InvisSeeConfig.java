package com.invissee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InvisSeeConfig {
    public static boolean enabled = true;
    public static boolean showInvisible = true;
    public static boolean onlyPlayers = false;
    public static int range = 128;
    // ARGB colors - default: mobs green, players red (as per user request example)
    public static int entityColor = 0xFF00FF00; // green for mobs/other entities
    public static int color = entityColor; // legacy alias
    public static int playerColor = 0xFFFF0000; // red for players
    public static float lineWidth = 1.5f;
    public static List<Integer> recentColors = new ArrayList<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vanilla-entity-indicator.json");

    private static class Data {
        boolean enabled = InvisSeeConfig.enabled;
        boolean showInvisible = InvisSeeConfig.showInvisible;
        boolean onlyPlayers = InvisSeeConfig.onlyPlayers;
        int range = InvisSeeConfig.range;
        int entityColor = InvisSeeConfig.entityColor;
        int playerColor = InvisSeeConfig.playerColor;
        float lineWidth = InvisSeeConfig.lineWidth;
        List<Integer> recentColors = new ArrayList<>(InvisSeeConfig.recentColors);
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try {
            String json = Files.readString(CONFIG_PATH);
            Data d = GSON.fromJson(json, Data.class);
            if (d != null) {
                enabled = d.enabled;
                showInvisible = d.showInvisible;
                onlyPlayers = d.onlyPlayers;
                range = d.range;
                entityColor = d.entityColor;
                color = d.entityColor;
                playerColor = d.playerColor;
                lineWidth = d.lineWidth;
                if (d.recentColors != null) {
                    recentColors = new ArrayList<>(d.recentColors);
                }
            }
        } catch (IOException e) {
            System.err.println("[vanilla-entity-indicator] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.enabled = enabled;
            d.showInvisible = showInvisible;
            d.onlyPlayers = onlyPlayers;
            d.range = range;
            d.entityColor = entityColor;
            d.playerColor = playerColor;
            d.lineWidth = lineWidth;
            d.recentColors = new ArrayList<>(recentColors);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(d));
            color = entityColor;
        } catch (IOException e) {
            System.err.println("[vanilla-entity-indicator] Failed to save config: " + e.getMessage());
        }
    }

    public static int getColorForPlayer(boolean isPlayer) {
        return isPlayer ? playerColor : entityColor;
    }

    /** Most-recent-first, max 6, no duplicates. */
    public static void pushRecent(int argb) {
        recentColors.removeIf(c -> c.intValue() == argb);
        recentColors.add(0, argb);
        while (recentColors.size() > 6) {
            recentColors.remove(recentColors.size() - 1);
        }
    }
}
