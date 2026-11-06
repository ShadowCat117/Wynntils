/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.containers.event.LootChestOpenedEvent;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.CompiledMapPath;
import com.wynntils.services.mapdata.paths.LootrunPathMigration;
import com.wynntils.services.mapdata.paths.MapPathCompiler;
import com.wynntils.services.mapdata.paths.MapPathFiles;
import com.wynntils.services.mapdata.paths.MapPathWorldRenderer;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.services.mapdata.paths.RecordingPathProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class MapPathsService extends Service {
    public record StoredPath(String providerId, Path file, MapPathImpl path) {
        public String label() {
            return path.getAttributes().flatMap(a -> a.getLabel()).orElse(path.getFeatureId());
        }
    }

    private record CachedPath(ResolvedMapAttributes attributes, CompiledMapPath compiled) {}

    private final RecordingPathProvider recordingProvider = new RecordingPathProvider();

    @Persisted
    private final Storage<Set<String>> initializedMigratedProviders = new Storage<>(new HashSet<>());

    private final Map<MapPath, CachedPath> compiled = new HashMap<>();
    private final Set<String> loadedProviders = new HashSet<>();
    private final List<Vec3> points = new ArrayList<>();
    private final List<Vec3> highlights = new ArrayList<>();
    private final List<PathNote> notes = new ArrayList<>();
    private String category = PathCategoriesProvider.PATH_CATEGORY;
    private String label = "";
    private MapPathAttributesImpl attributes =
            new MapAttributesBuilder().asPathAttributes().build();
    private boolean recording;
    private boolean draft;
    private boolean recordHighlights = true;
    private StoredPath editing;
    private String featureId;

    public MapPathsService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(new PathCategoriesProvider(), this, false);
        Services.MapData.registerBuiltInProvider(recordingProvider, this, false);
        List<Path> migrated = LootrunPathMigration.migrate(
                WynntilsMod.getModStorageDir("lootruns").toPath(), MapDataService.LOCAL_PROVIDERS.toPath());
        for (Path file : migrated) {
            String id = file.getFileName().toString().replaceFirst("\\.json$", "");
            Services.MapData.addJsonProvider(
                    JsonProviderInfo.createLocal(id, file.toAbsolutePath().toString()));
        }
        if (LootrunPathMigration.disableMigratedProvidersOnce(
                MapDataService.LOCAL_PROVIDERS.toPath(), initializedMigratedProviders.get(), id -> {
                    if (Services.MapData.isProviderEnabled(id)) Services.MapData.toggleProvider(id);
                })) {
            initializedMigratedProviders.touched();
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean hasDraft() {
        return draft;
    }

    public boolean isRecordingHighlights() {
        return recordHighlights;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public MapPathAttributesImpl getAttributes() {
        return attributes;
    }

    public List<PathNote> getNotes() {
        return List.copyOf(notes);
    }

    public int getPointCount() {
        return points.size();
    }

    public int getHighlightCount() {
        return highlights.size();
    }

    public StoredPath getEditing() {
        return editing;
    }

    public void configure(String category, String label, MapPathAttributesImpl attributes, boolean recordHighlights) {
        this.category = category;
        this.label = label;
        this.attributes = attributes;
        this.recordHighlights = recordHighlights;
        if (draft) publish();
    }

    public void startRecording() {
        if (recording) throw new IllegalStateException("Already recording a path");
        if (!draft) {
            points.clear();
            highlights.clear();
            notes.clear();
            editing = null;
            featureId = "user:path:" + UUID.randomUUID();
            draft = true;
        }
        recording = true;
    }

    public void pauseRecording() {
        recording = false;
        publish();
    }

    public void cancel() {
        recording = false;
        draft = false;
        editing = null;
        points.clear();
        highlights.clear();
        notes.clear();
        recordingProvider.update(null);
        compiled.clear();
    }

    public void clear() {
        cancel();
        for (String id : loadedProviders) {
            if (Services.MapData.isProviderEnabled(id)) Services.MapData.toggleProvider(id);
        }
        loadedProviders.clear();
    }

    public boolean addHighlight(Vec3 position) {
        if (!draft || highlights.contains(position)) return false;
        highlights.add(position);
        publish();
        return true;
    }

    public boolean removeHighlight(BlockPos position) {
        boolean removed = highlights.removeIf(p -> BlockPos.containing(p).equals(position));
        if (removed) publish();
        return removed;
    }

    public boolean addNote(Component text) {
        if (!draft || McUtils.player() == null) return false;
        return addNote(McUtils.player().getRootVehicle().position(), text);
    }

    public boolean addNote(Vec3 position, Component text) {
        if (!draft) return false;
        notes.add(PathNote.of(position, text));
        publish();
        return true;
    }

    public boolean deleteNote(BlockPos position) {
        boolean removed =
                notes.removeIf(note -> BlockPos.containing(note.position()).equals(position));
        if (removed) publish();
        return removed;
    }

    public boolean undo() {
        return McUtils.player() != null && undo(McUtils.player().position());
    }

    boolean undo(Vec3 position) {
        if (!recording || points.size() < 2) return false;
        boolean left = false;
        int keep = -1;
        for (int i = points.size() - 1; i >= 0; i--) {
            if (points.get(i).distanceToSqr(position) < 4) {
                if (left) {
                    keep = i + 1;
                    break;
                }
            } else left = true;
        }
        if (keep < 1) return false;
        points.subList(keep, points.size()).clear();
        publish();
        return true;
    }

    private MapPathImpl snapshot(boolean preview) {
        return new MapPathImpl(
                featureId,
                preview && recording ? PathCategoriesProvider.RECORDING_CATEGORY : category,
                new MapAttributesBuilder()
                        .from(attributes)
                        .setLabel(label)
                        .asPathAttributes()
                        .build(),
                points,
                highlights,
                notes);
    }

    private void publish() {
        recordingProvider.update(points.size() >= 2 ? snapshot(true) : null);
    }

    public StoredPath save(String name) throws IOException {
        if (!draft || points.size() < 2) throw new IOException("Record at least two path points before saving");
        if (name.isBlank()) throw new IOException("A path label is required");
        label = name;
        MapPathImpl path = snapshot(false);
        Path file;
        String providerId;
        if (editing != null) {
            file = editing.file();
            providerId = editing.providerId();
            MapPathFiles.update(file, path);
        } else {
            file = MapDataService.LOCAL_PROVIDERS.toPath().resolve(MapPathFiles.filename(name));
            providerId = file.getFileName().toString().replaceFirst("\\.json$", "");
            if (Services.MapData.getJsonProviderInfos().containsKey(providerId))
                throw new IOException("A provider with that name already exists");
            MapPathFiles.write(file, MapPathFiles.provider(path), false);
        }
        Services.MapData.addJsonProvider(
                JsonProviderInfo.createLocal(providerId, file.toAbsolutePath().toString()));
        StoredPath saved = new StoredPath(providerId, file, path);
        loadedProviders.add("local:" + providerId);
        cancel();
        return saved;
    }

    public List<StoredPath> getStoredPaths() {
        List<StoredPath> paths = new ArrayList<>();
        for (JsonProviderInfo info : Services.MapData.getJsonProviderInfos().values()) {
            if (info.providerType() != JsonProviderInfo.JsonProviderType.LOCAL) continue;
            Path file = Path.of(info.providerFilePath());
            try {
                JsonObject provider = MapPathFiles.read(file);
                if (!provider.has("features") || !provider.get("features").isJsonObject()) continue;
                JsonArray definitions = provider.getAsJsonObject("features").getAsJsonArray("paths");
                if (definitions == null) continue;
                for (var json : definitions) {
                    MapPathImpl path = JsonManager.GSON.fromJson(json, MapPathImpl.class);
                    if (path.validate()) paths.add(new StoredPath(info.providerId(), file, path));
                }
            } catch (Exception e) {
                WynntilsMod.warn("Could not read map paths in '" + file + "'", e);
            }
        }
        return List.copyOf(paths);
    }

    public StoredPath find(String name) {
        return getStoredPaths().stream()
                .filter(p -> p.label().equals(name)
                        || p.path().getFeatureId().equals(name)
                        || p.providerId().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Map path not found: " + name));
    }

    public void load(StoredPath stored) {
        String id = "local:" + stored.providerId();
        if (!Services.MapData.isProviderEnabled(id)) Services.MapData.toggleProvider(id);
        loadedProviders.add(id);
    }

    public void edit(StoredPath stored) {
        cancel();
        editing = stored;
        draft = true;
        featureId = stored.path().getFeatureId();
        category = stored.path().getCategoryId();
        attributes = new MapAttributesBuilder()
                .from(stored.path().getAttributes().orElse(null))
                .asPathAttributes()
                .build();
        label = stored.label();
        points.addAll(stored.path().getPath());
        highlights.addAll(stored.path().getHighlights());
        notes.addAll(stored.path().getNotes());
        publish();
    }

    public void rename(StoredPath stored, String name) throws IOException {
        if (name.isBlank()) throw new IOException("A path label is required");
        MapPathImpl path = new MapPathImpl(
                stored.path().getFeatureId(),
                stored.path().getCategoryId(),
                new MapAttributesBuilder()
                        .from(stored.path().getAttributes().orElse(null))
                        .setLabel(name)
                        .asPathAttributes()
                        .build(),
                stored.path().getPath(),
                stored.path().getHighlights(),
                stored.path().getNotes());
        MapPathFiles.update(stored.file(), path);
        Services.MapData.reloadData();
    }

    public void delete(StoredPath stored) throws IOException {
        JsonObject provider = MapPathFiles.read(stored.file());
        JsonArray definitions = provider.getAsJsonObject("features").getAsJsonArray("paths");
        definitions
                .asList()
                .removeIf(json -> json.getAsJsonObject()
                        .get("featureId")
                        .getAsString()
                        .equals(stored.path().getFeatureId()));
        MapPathFiles.write(stored.file(), provider, true);
        Services.MapData.reloadData();
        if (editing != null
                && editing.path().getFeatureId().equals(stored.path().getFeatureId())) cancel();
    }

    @SubscribeEvent
    public void recordMovement(TickEvent event) {
        if (!recording || McUtils.player() == null) return;
        recordPosition(McUtils.player().getRootVehicle().position());
    }

    void recordPosition(Vec3 position) {
        if (!recording
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return;
        if (points.isEmpty() || points.getLast().distanceToSqr(position) >= 4) {
            points.add(position);
            publish();
        }
    }

    @SubscribeEvent
    public void onLootChestOpened(LootChestOpenedEvent event) {
        if (recording && recordHighlights) addHighlight(Vec3.atLowerCornerOf(event.getBlockPos()));
    }

    @SubscribeEvent
    public void renderPaths(SubmitCustomGeometryEvent event) {
        Set<MapPath> active = new HashSet<>();
        Services.MapData.getFeatures().filter(f -> f instanceof MapPath).forEach(feature -> {
            MapPath path = (MapPath) feature;
            active.add(path);
            ResolvedMapAttributes attrs = Services.MapData.resolveMapAttributes(path);
            if (!attrs.hasPath()) return;
            CachedPath cached = compiled.get(path);
            if (cached == null || !cached.attributes().equals(attrs)) {
                cached = new CachedPath(attrs, MapPathCompiler.compile(path, attrs));
                compiled.put(path, cached);
            }
            Identifier texture = attrs.iconId().equals(MapIcon.NO_ICON_ID)
                    ? null
                    : Services.MapData.getIconOrFallback(attrs.iconId()).getIdentifier();
            MapPathWorldRenderer.renderPath(
                    event.getPoseStack(),
                    event.getSubmitNodeCollector(),
                    cached.compiled(),
                    attrs.pathColor().equals(WynncraftShaderColor.RAINBOW.color)
                            ? CommonColors.LIGHT_BLUE.asInt()
                            : attrs.pathColor().asInt(),
                    texture);
        });
        compiled.keySet().retainAll(active);
    }
}
