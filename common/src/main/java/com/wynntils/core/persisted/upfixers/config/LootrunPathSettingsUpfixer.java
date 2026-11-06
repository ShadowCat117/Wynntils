/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.upfixers.Upfixer;
import com.wynntils.services.mapdata.paths.MapPathFiles;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.WynncraftShaderColor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/** Preserve the old feature's world rendering settings as editable map category definitions. */
public final class LootrunPathSettingsUpfixer implements Upfixer {
    @Override
    public boolean apply(JsonObject config, Set<PersistedValue<?>> persisteds) {
        if (config.keySet().stream().noneMatch(key -> key.startsWith("lootrunFeature."))) return true;
        Path destination =
                WynntilsMod.getModStorageDir("localProviders").toPath().resolve("lootrun-category-settings.json");
        try {
            if (!Files.exists(destination)) MapPathFiles.write(destination, convert(config), false);
            config.entrySet().removeIf(entry -> entry.getKey().startsWith("lootrunFeature."));
            return true;
        } catch (Exception e) {
            WynntilsMod.warn("Could not migrate lootrun rendering settings; leaving old config intact", e);
            return false;
        }
    }

    public static JsonObject convert(JsonObject config) {
        JsonArray categories = new JsonArray();
        for (boolean recording : new boolean[] {false, true}) {
            JsonObject attrs = new JsonObject();
            String colorKey = recording ? "lootrunFeature.recordingPathColor" : "lootrunFeature.activePathColor";
            if (config.has(colorKey))
                attrs.add("pathColor", config.get(colorKey).deepCopy());
            if (!recording
                    && config.has("lootrunFeature.rainbowLootRun")
                    && config.get("lootrunFeature.rainbowLootRun").getAsBoolean()) {
                attrs.addProperty("pathColor", WynncraftShaderColor.RAINBOW.color.toHexString());
            }
            if (config.has("lootrunFeature.cycleDistance"))
                attrs.add(
                        "pathCycleDistance",
                        config.get("lootrunFeature.cycleDistance").deepCopy());
            if (config.has("lootrunFeature.pathType")
                    && config.get("lootrunFeature.pathType").getAsString().equalsIgnoreCase("line")) {
                attrs.addProperty("icon", MapIcon.NO_ICON_ID);
            }
            JsonObject category = new JsonObject();
            category.addProperty(
                    "id", recording ? PathCategoriesProvider.RECORDING_CATEGORY : PathCategoriesProvider.PATH_CATEGORY);
            category.add("attributes", attrs);
            categories.add(category);
        }
        JsonObject provider = new JsonObject();
        provider.addProperty("version", 1);
        provider.addProperty("description", "Migrated lootrun category settings");
        JsonObject features = new JsonObject();
        features.add("locations", new JsonArray());
        features.add("areas", new JsonArray());
        features.add("paths", new JsonArray());
        provider.add("features", features);
        provider.add("categories", categories);
        provider.add("icons", new JsonArray());
        return provider;
    }
}
