/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
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
    private final RecordingPathProvider recordingProvider = new RecordingPathProvider();

    private final Map<MapPath, CachedPath> compiled = new HashMap<>();
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
        Services.MapData.registerBuiltInProvider(recordingProvider, this, false);
        List<Path> migrated = LootrunPathMigration.migrate(
                WynntilsMod.getModStorageDir("lootruns").toPath(), MapDataService.LOCAL_PROVIDERS.toPath());
        for (Path file : migrated) {
            String id = file.getFileName().toString().replaceFirst("\\.json$", "");
            JsonProviderInfo providerInfo =
                    JsonProviderInfo.createLocal(id, file.toAbsolutePath().toString());
            Services.MapData.addJsonProvider(providerInfo);
            Services.MapData.toggleProvider(providerInfo.completeId());
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

    public int getNoteCount() {
        return notes.size();
    }

    public boolean canEditNotes() {
        return recording || editing != null;
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

    public boolean startRecording() {
        if (editing != null) {
            McUtils.sendErrorToClient(Component.translatable("command.wynntils.map.path.editingInProgress")
                    .getString());

            return false;
        }

        if (recording) {
            McUtils.sendErrorToClient(Component.translatable("command.wynntils.map.path.alreadyRecording")
                    .getString());

            return false;
        }

        if (!draft) {
            points.clear();
            highlights.clear();
            notes.clear();
            editing = null;
            featureId = "user:path:" + UUID.randomUUID();
            draft = true;
        }

        recording = true;
        publish();

        return true;
    }

    public void cancel() {
        recording = false;
        draft = false;
        editing = null;
        category = PathCategoriesProvider.PATH_CATEGORY;
        label = "";
        attributes = new MapAttributesBuilder().asPathAttributes().build();
        recordHighlights = true;
        featureId = null;
        points.clear();
        highlights.clear();
        notes.clear();
        recordingProvider.update(null);
        compiled.clear();
    }

    public boolean addHighlight(Vec3 position) {
        if (!recording || highlights.contains(position)) return false;

        highlights.add(position);
        publish();

        return true;
    }

    public boolean removeHighlight(BlockPos position) {
        if (!recording) return false;

        boolean removed = highlights.removeIf(p -> BlockPos.containing(p).equals(position));
        if (removed) {
            publish();
        }

        return removed;
    }

    public boolean addNote(Component text) {
        if (!canEditNotes() || McUtils.player() == null) return false;

        return addNote(McUtils.player().getRootVehicle().position(), text);
    }

    public boolean addNote(Vec3 position, Component text) {
        if (!canEditNotes()
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return false;

        notes.add(PathNote.of(position, text));
        publish();

        return true;
    }

    public PathNote updateNote(PathNote note, Vec3 position, Component text) {
        if (!canEditNotes()
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return null;

        int index = notes.indexOf(note);
        if (index < 0) return null;

        PathNote updated = PathNote.of(position, text);
        notes.set(index, updated);
        publish();

        return updated;
    }

    public boolean deleteNote(PathNote note) {
        if (!canEditNotes()) return false;

        boolean removed = notes.remove(note);
        if (removed) {
            publish();
        }

        return removed;
    }

    public boolean deleteNote(BlockPos position) {
        if (!canEditNotes()) return false;

        boolean removed =
                notes.removeIf(note -> BlockPos.containing(note.position()).equals(position));
        if (removed) {
            publish();
        }

        return removed;
    }

    private MapPathImpl snapshot(String categoryId, MapPathAttributesImpl style) {
        return new MapPathImpl(
                featureId,
                categoryId,
                new MapAttributesBuilder()
                        .from(style)
                        .setLabel(label)
                        .setDescription(attributes.getDescription().orElse(null))
                        .asPathAttributes()
                        .build(),
                points,
                highlights,
                notes);
    }

    private void publish() {
        recordingProvider.update(
                recording && points.size() >= 2 ? snapshot(PathCategoriesProvider.RECORDING_CATEGORY, null) : null);
    }

    public StoredPath save(String name) throws IOException {
        if (!draft || points.size() < 2) throw new IOException("Record at least two path points before saving");

        if (name.isBlank()) throw new IOException("A path label is required");

        label = name;
        MapPathImpl path = snapshot(category, attributes);
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
        cancel();

        return saved;
    }

    public List<StoredPath> getStoredPaths() {
        List<StoredPath> paths = new ArrayList<>();
        for (JsonProviderInfo info : Services.MapData.getJsonProviderInfos().values()) {
            if (info.providerType() != JsonProviderInfo.JsonProviderType.LOCAL) continue;

            Services.MapData.getProviderFromId(info.completeId()).ifPresent(provider -> {
                for (var feature : provider.getFeatures().toList()) {
                    if (feature instanceof MapPathImpl path) {
                        paths.add(new StoredPath(info.providerId(), Path.of(info.providerFilePath()), path));
                    }
                }
            });
        }

        return List.copyOf(paths);
    }

    public StoredPath find(String name) {
        List<StoredPath> paths = getStoredPaths();
        List<StoredPath> matches = paths.stream()
                .filter(path -> path.path().getFeatureId().equals(name))
                .toList();
        if (matches.isEmpty()) {
            matches = paths.stream()
                    .filter(path -> path.providerId().equals(name))
                    .toList();
        }

        if (matches.isEmpty()) {
            matches = paths.stream().filter(path -> path.label().equals(name)).toList();
        }

        if (matches.isEmpty()) throw new IllegalArgumentException("Map path not found: " + name);

        if (matches.size() > 1) {
            throw new IllegalArgumentException("Multiple paths match '" + name + "'; use the feature ID instead");
        }

        return matches.getFirst();
    }

    public boolean edit(StoredPath stored) {
        if (draft) return false;

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

        return true;
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

            Identifier texture = attrs.pathIconId().equals(MapIcon.NO_ICON_ID)
                    ? null
                    : Services.MapData.getIconOrFallback(attrs.pathIconId()).getIdentifier();
            MapPathWorldRenderer.renderPath(
                    event.getPoseStack(),
                    event.getSubmitNodeCollector(),
                    cached.compiled(),
                    attrs.pathColor().asInt(),
                    texture);
        });
        compiled.keySet().retainAll(active);
    }

    public record StoredPath(String providerId, Path file, MapPathImpl path) {
        public String label() {
            return path.getAttributes().flatMap(a -> a.getLabel()).orElse(path.getFeatureId());
        }
    }

    private record CachedPath(ResolvedMapAttributes attributes, CompiledMapPath compiled) {}
}
