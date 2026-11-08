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

        JsonObject attrs = new JsonObject();
        if (config.has("lootrunFeature.activePathColor")) {
            attrs.add("pathColor", config.get("lootrunFeature.activePathColor").deepCopy());
        }

        if (config.has("lootrunFeature.rainbowLootRun")
                && config.get("lootrunFeature.rainbowLootRun").getAsBoolean()) {
            attrs.addProperty("pathColor", WynncraftShaderColor.RAINBOW.color.toHexString());
        }

        if (config.has("lootrunFeature.pathType")
                && config.get("lootrunFeature.pathType").getAsString().equalsIgnoreCase("line")) {
            attrs.addProperty("pathIcon", MapIcon.NO_ICON_ID);
        }

        JsonObject category = new JsonObject();
        category.addProperty("id", PathCategoriesProvider.PATH_CATEGORY);
        category.add("attributes", attrs);
        categories.add(category);

        JsonObject recordingAttrs = new JsonObject();
        if (config.has("lootrunFeature.recordingPathColor")) {
            recordingAttrs.add(
                    "pathColor", config.get("lootrunFeature.recordingPathColor").deepCopy());
            recordingAttrs.add(
                    "pathFillColor",
                    config.get("lootrunFeature.recordingPathColor").deepCopy());
        }

        if (attrs.has("pathIcon")) {
            recordingAttrs.add("pathIcon", attrs.get("pathIcon").deepCopy());
        }

        JsonObject recordingCategory = new JsonObject();
        recordingCategory.addProperty("id", PathCategoriesProvider.RECORDING_CATEGORY);
        recordingCategory.add("attributes", recordingAttrs);
        categories.add(recordingCategory);

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
