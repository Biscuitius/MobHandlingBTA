package biscuitius.mobhandling.config;

import biscuitius.mobhandling.MobHandlingMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.monster.MobSlime;
import net.minecraft.core.util.collection.NamespaceID;

public final class MobHandlingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "mobhandling.json");
    private static final List<String> DEFAULT_MOB_BLACKLIST = Arrays.asList(
        "minecraft:creeper",
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:spider",
        "minecraft:ghast",
        "minecraft:giant",
        "minecraft:human",
        "minecraft:snowman",
        "minecraft:zombie_armored",
        "minecraft:zombie_pigman"
    );

    private static MobHandlingConfig instance = new MobHandlingConfig();

    @SerializedName("mob_blacklist")
    public List<String> mobBlacklist = createDefaultMobBlacklist();

    @SerializedName("max_carriable_slime_size")
    public int maxCarriableSlimeSize = 0;

    // Get the config instance currently in use.
    public static MobHandlingConfig get() {
        return instance;
    }

    public static void load() {
        // Read the JSON file once and keep the active config in memory.
        instance = readConfig();
    }

    // Check whether a mob can be carried right now.
    public boolean canCarry(Mob mob) {
        return mob != null && !isBlacklisted(mob) && (!(mob instanceof MobSlime) || ((MobSlime) mob).getSlimeSize() - 1 <= this.maxCarriableSlimeSize);
    }

    // Copy the default blacklist so callers can edit it safely.
    private static List<String> createDefaultMobBlacklist() {
        return new ArrayList<>(DEFAULT_MOB_BLACKLIST);
    }

    // Load the config file, or create a fresh one if it does not exist.
    private static MobHandlingConfig readConfig() {
        try {
            ensureParentDirectory();

            if (Files.notExists(CONFIG_PATH)) {
                MobHandlingConfig config = new MobHandlingConfig();
                config.resetToDefaults();
                config.save();
                return config;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                MobHandlingConfig config = GSON.fromJson(reader, MobHandlingConfig.class);
                if (config == null) {
                    config = new MobHandlingConfig();
                    config.resetToDefaults();
                }

                config.normalize();
                config.save();
                return config;
            }
        } catch (IOException | JsonParseException exception) {
            MobHandlingMod.LOGGER.warn("Failed to load mob handling config from {}. Falling back to defaults.", CONFIG_PATH, exception);
            MobHandlingConfig config = new MobHandlingConfig();
            config.resetToDefaults();
            return config;
        }
    }

    // Make sure the config folder exists before reading or writing.
    private static void ensureParentDirectory() throws IOException {
        Path parent = CONFIG_PATH.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    // Write the current config back to disk.
    public void save() {
        try {
            this.normalize();
            ensureParentDirectory();
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            MobHandlingMod.LOGGER.warn("Failed to write mob handling config to {}.", CONFIG_PATH, exception);
        }
    }

    // Reset the config values to their shipped defaults.
    public void resetToDefaults() {
        this.mobBlacklist = createDefaultMobBlacklist();
        this.maxCarriableSlimeSize = 0;
    }

    // Clean up list entries and keep values in range.
    private void normalize() {
        if (this.mobBlacklist == null) {
            this.mobBlacklist = new ArrayList<>();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String entry : this.mobBlacklist) {
            String trimmed = normalizeEntry(entry);
            if (trimmed != null) {
                normalized.add(trimmed);
            }
        }

        this.mobBlacklist = new ArrayList<>(normalized);

        if (this.maxCarriableSlimeSize < 0) {
            this.maxCarriableSlimeSize = 0;
        }
    }

    // Compare the mob's entity id against the blacklist.
    private boolean isBlacklisted(Mob mob) {
        NamespaceID entityId = EntityDispatcher.idForClass(mob.getClass());
        if (entityId == null) {
            return false;
        }

        String entityIdString = entityId.toString().toLowerCase(Locale.ROOT);

        for (String entry : this.mobBlacklist) {
            if (matches(entry, entityIdString)) {
                return true;
            }
        }

        return false;
    }

    // Lowercase and trim a blacklist entry before saving it.
    private static String normalizeEntry(String entry) {
        if (entry == null) {
            return null;
        }

        String trimmed = entry.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Match a stored blacklist entry against the mob id.
    private static boolean matches(String entry, String entityIdString) {
        return entry != null && !entry.isEmpty() && entityIdString.equals(entry);
    }
}


