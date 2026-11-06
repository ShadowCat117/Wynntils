/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.json.JsonManager;
import com.wynntils.services.mapdata.providers.json.JsonProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** Converts legacy routes once, retaining the original files in lootruns/migrated after a verified write. */
public final class LootrunPathMigration {
    private LootrunPathMigration() {}

    public static JsonObject convert(String label, JsonObject legacy) {
        JsonArray points = legacy.getAsJsonArray("points");
        if (points == null || points.size() < 2)
            throw new IllegalArgumentException("A route needs at least two points");
        JsonObject attributes = new JsonObject();
        attributes.addProperty("label", label);
        attributes.addProperty("priority", 900);
        attributes.addProperty("fillColor", "#55FFFF");
        attributes.addProperty("borderColor", "#000000");
        attributes.addProperty("borderWidth", 1);
        attributes.addProperty("pathWidth", 4);
        JsonObject path = new JsonObject();
        path.addProperty(
                "featureId", "wynntils:path:" + UUID.nameUUIDFromBytes(label.getBytes(StandardCharsets.UTF_8)));
        path.addProperty("categoryId", "wynntils:path");
        path.add("attributes", attributes);
        path.add("path", points.deepCopy());
        path.add("highlights", legacy.has("chests") ? legacy.get("chests").deepCopy() : new JsonArray());
        JsonArray notes = new JsonArray();
        if (legacy.has("notes") && legacy.get("notes").isJsonArray()) {
            for (JsonElement value : legacy.getAsJsonArray("notes")) {
                JsonObject oldNote = value.getAsJsonObject();
                JsonObject note = new JsonObject();
                note.add(
                        "position",
                        oldNote.has("position")
                                ? oldNote.get("position").deepCopy()
                                : oldNote.get("location").deepCopy());
                note.add("text", normalizeLegacyText(oldNote.get("note")));
                notes.add(note);
            }
        }
        path.add("notes", notes);
        JsonArray paths = new JsonArray();
        paths.add(path);
        JsonObject features = new JsonObject();
        features.add("locations", new JsonArray());
        features.add("areas", new JsonArray());
        features.add("paths", paths);
        JsonObject provider = new JsonObject();
        provider.addProperty("version", 1);
        provider.addProperty("description", "Migrated lootrun: " + label);
        provider.add("features", features);
        provider.add("categories", new JsonArray());
        provider.add("icons", new JsonArray());
        return provider;
    }

    private static JsonElement normalizeLegacyText(JsonElement text) {
        JsonElement normalized = text.deepCopy();
        if (normalized.isJsonArray()) {
            for (int i = 0; i < normalized.getAsJsonArray().size(); i++) {
                normalized
                        .getAsJsonArray()
                        .set(i, normalizeLegacyText(normalized.getAsJsonArray().get(i)));
            }
        } else if (normalized.isJsonObject()) {
            JsonObject object = normalized.getAsJsonObject();
            for (String key : List.of("bold", "italic", "underlined", "strikethrough", "obfuscated")) {
                JsonElement value = object.get(key);
                if (value != null
                        && value.isJsonPrimitive()
                        && value.getAsJsonPrimitive().isString()
                        && (value.getAsString().equalsIgnoreCase("true")
                                || value.getAsString().equalsIgnoreCase("false"))) {
                    object.addProperty(key, Boolean.parseBoolean(value.getAsString()));
                }
            }
            for (var entry : object.entrySet()) {
                if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    entry.setValue(normalizeLegacyText(entry.getValue()));
                }
            }
        }
        return normalized;
    }

    public static List<Path> migrate(Path legacyDirectory, Path providersDirectory) {
        List<Path> created = new ArrayList<>();
        if (!Files.isDirectory(legacyDirectory)) return created;
        try (var files = Files.list(legacyDirectory)) {
            for (Path source : files.filter(
                            p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .sorted()
                    .toList()) {
                try {
                    String label = source.getFileName().toString().replaceFirst("\\.json$", "");
                    String hash = checksum(Files.readAllBytes(source));
                    String slug = MapPathFiles.filename(label).replaceFirst("\\.json$", "");
                    String id = UUID.nameUUIDFromBytes(
                                    source.getFileName().toString().getBytes(StandardCharsets.UTF_8))
                            .toString()
                            .substring(0, 8);
                    Path destination = providersDirectory.resolve("lootrun-" + slug + "-" + id + ".json");
                    if (Files.exists(destination)) {
                        JsonObject existing = MapPathFiles.read(destination);
                        if (!existing.has("legacyLootrunChecksum")
                                || !hash.equals(
                                        existing.get("legacyLootrunChecksum").getAsString())) {
                            throw new IOException(
                                    "Migration destination already exists; leaving source untouched: " + destination);
                        }
                        JsonManager.GSON.fromJson(existing, JsonProvider.class);
                    } else {
                        JsonObject provider = convert(label, MapPathFiles.read(source));
                        provider.addProperty("legacyLootrunChecksum", hash);
                        MapPathFiles.write(destination, provider, false);
                        created.add(destination);
                    }
                    Path archive = legacyDirectory.resolve("migrated");
                    Files.createDirectories(archive);
                    Files.move(source, archive.resolve(source.getFileName()));
                } catch (Exception e) {
                    WynntilsMod.warn("Could not migrate lootrun '" + source + "'; original file retained", e);
                }
            }
        } catch (IOException e) {
            WynntilsMod.warn("Could not list legacy lootruns for migration", e);
        }
        return created;
    }

    private static String checksum(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Disable each migrated provider once, including providers migrated before this tracking was introduced. */
    public static boolean disableMigratedProvidersOnce(
            Path providersDirectory, Set<String> initialized, Consumer<String> disableProvider) {
        boolean changed = false;
        if (!Files.isDirectory(providersDirectory)) return false;
        try (var files = Files.list(providersDirectory)) {
            for (Path file : files.filter(
                            p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .toList()) {
                String id = "local:" + file.getFileName().toString().replaceFirst("\\.json$", "");
                if (initialized.contains(id)) continue;
                try {
                    if (!MapPathFiles.read(file).has("legacyLootrunChecksum")) continue;
                    disableProvider.accept(id);
                    initialized.add(id);
                    changed = true;
                } catch (Exception e) {
                    WynntilsMod.warn("Could not initialize migrated lootrun provider '" + file + "'", e);
                }
            }
        } catch (IOException e) {
            WynntilsMod.warn("Could not list migrated lootrun providers", e);
        }
        return changed;
    }
}
