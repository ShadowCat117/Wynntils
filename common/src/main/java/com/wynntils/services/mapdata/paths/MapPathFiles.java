/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.json.JsonManager;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class MapPathFiles {
    private MapPathFiles() {}

    public static String filename(String label) {
        String name =
                label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "-").replaceAll("^-|-$", "");
        return (name.isEmpty() ? "map-path" : name) + ".json";
    }

    public static JsonObject provider(MapPathImpl path) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        JsonObject features = new JsonObject();
        features.add("locations", new JsonArray());
        features.add("areas", new JsonArray());
        JsonArray paths = new JsonArray();
        paths.add(pathJson(path));
        features.add("paths", paths);
        json.add("features", features);
        json.add("categories", new JsonArray());
        json.add("icons", new JsonArray());
        return json;
    }

    public static JsonObject pathJson(MapPathImpl path) {
        JsonObject json = JsonManager.GSON.toJsonTree(path).getAsJsonObject();
        removeNulls(json);
        return json;
    }

    private static void removeNulls(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            object.entrySet().removeIf(entry -> entry.getValue().isJsonNull());
            object.entrySet().forEach(entry -> removeNulls(entry.getValue()));
        } else if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(MapPathFiles::removeNulls);
        }
    }

    public static JsonObject read(Path file) throws IOException {
        return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                .getAsJsonObject();
    }

    public static void write(Path file, JsonObject json, boolean replace) throws IOException {
        JsonProvider validated = JsonManager.GSON.fromJson(json, JsonProvider.class);
        if (validated == null) throw new IOException("Invalid map provider");
        Files.createDirectories(file.toAbsolutePath().getParent());
        if (!replace && Files.exists(file)) throw new FileAlreadyExistsException(file.toString());
        Path temporary = Files.createTempFile(file.toAbsolutePath().getParent(), ".map-path-", ".tmp");
        try {
            Files.writeString(temporary, JsonManager.GSON.toJson(json) + "\n", StandardCharsets.UTF_8);
            if (replace) {
                try {
                    Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                // Do not overwrite a provider that appeared while the file was being prepared.
                Files.move(temporary, file);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public static void update(Path file, MapPathImpl path) throws IOException {
        JsonObject json = read(file);
        JsonArray paths = json.getAsJsonObject("features").getAsJsonArray("paths");
        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).getAsJsonObject().get("featureId").getAsString().equals(path.getFeatureId())) {
                paths.set(i, pathJson(path));
                write(file, json, true);
                return;
            }
        }
        throw new IOException("Path no longer exists in provider");
    }
}
