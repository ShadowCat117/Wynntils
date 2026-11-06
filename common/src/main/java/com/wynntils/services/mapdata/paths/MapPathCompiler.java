/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.type.PathGeometry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public final class MapPathCompiler {
    private static final List<Integer> COLORS = List.of(
            ChatFormatting.RED.getColor(),
            ChatFormatting.GOLD.getColor(),
            ChatFormatting.YELLOW.getColor(),
            ChatFormatting.GREEN.getColor(),
            ChatFormatting.BLUE.getColor(),
            0x3f00ff,
            ChatFormatting.DARK_PURPLE.getColor());

    public static CompiledMapPath compile(MapPath feature, ResolvedMapAttributes attributes) {
        PathPoints raw = new PathPoints(feature.getPath());
        Set<BlockPos> highlights = new HashSet<>();
        feature.getHighlights().forEach(p -> highlights.add(BlockPos.containing(p)));
        return new CompiledMapPath(
                feature,
                generateSimplifiedPoints(raw),
                generatePointsByChunk(raw, attributes),
                getChests(highlights),
                getNotes(feature.getNotes()));
    }

    private static List<PathPoints> sample(PathPoints raw, float sampleRate) {
        List<PathPoints> positions = new ArrayList<>();
        PathPoints currentPositions = new PathPoints(new ArrayList<>());
        positions.add(currentPositions);
        for (Vec3 element : raw.points()) {
            if (!currentPositions.points().isEmpty()
                    && currentPositions.points().getLast().distanceTo(element) >= 32) {
                currentPositions = new PathPoints(new ArrayList<>());
                positions.add(currentPositions);
            }
            if (currentPositions.points().isEmpty()
                    || !currentPositions.points().getLast().equals(element)) {
                currentPositions.points().add(element);
            }
        }

        List<PathPoints> result = new ArrayList<>();
        for (PathPoints current : positions) {
            if (current.points().size() < 2) continue;
            float distance = 0f;
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderX =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderY =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderZ =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            for (int i = 0; i < current.points().size(); i++) {
                Vec3 position = current.points().get(i);
                if (i > 0) {
                    distance += current.points().get(i - 1).distanceTo(position);
                }

                float slopeX = 0f;
                float slopeY = 0f;
                float slopeZ = 0f;
                if (i < current.points().size() - 1) {
                    Vec3 next = current.points().get(i + 1);
                    slopeX = (float) ((next.x - position.x) / position.distanceTo(next));
                    slopeY = (float) ((next.y - position.y) / position.distanceTo(next));
                    slopeZ = (float) ((next.z - position.z) / position.distanceTo(next));
                }
                builderX.addPoint(distance, (float) position.x, slopeX);
                builderY.addPoint(distance, (float) position.y, slopeY);
                builderZ.addPoint(distance, (float) position.z, slopeZ);
            }
            CubicSpline<Float, BoundedFloatFunction<Float>> splineX = builderX.build();
            CubicSpline<Float, BoundedFloatFunction<Float>> splineY = builderY.build();
            CubicSpline<Float, BoundedFloatFunction<Float>> splineZ = builderZ.build();

            PathPoints newResult = new PathPoints(new ArrayList<>());
            for (float i = 0f; i < distance; i += (1f / sampleRate)) {
                newResult.points().add(new Vec3(splineX.apply(i), splineY.apply(i), splineZ.apply(i)));
            }
            newResult.points().add(current.points().getLast());
            result.add(newResult);
        }
        return result;
    }

    private static Long2ObjectMap<List<ColoredPath>> generatePointsByChunk(
            PathPoints raw, ResolvedMapAttributes attributes) {
        float sampleRate = 10f;

        List<List<Vec3>> sampled =
                sample(raw, sampleRate).stream().map(PathPoints::points).toList();
        List<Vec3> positions = sampled.stream().flatMap(List::stream).toList();

        ColoredPath locationsList = new ColoredPath(new ArrayList<>());

        Iterator<Integer> colorIterator = COLORS.iterator();
        Integer nextColor = colorIterator.next();
        Integer currentColor = nextColor;

        float differenceRed = 0;
        float differenceGreen = 0;
        float differenceBlue = 0;

        for (int i = 0; i < positions.size(); i++) {
            Vec3 position = positions.get(i);

            if (attributes.pathColor().equals(WynncraftShaderColor.RAINBOW.color)) {
                float cycleDistance = Math.max(1f, attributes.pathCycleDistance());
                int cycle = Math.max(1, (int) (sampleRate * cycleDistance));
                int parts = i % cycle;
                float done = (float) parts / (float) cycle;

                int usedColor;
                if (parts == 0) {
                    currentColor = nextColor;
                    if (!colorIterator.hasNext()) {
                        colorIterator = COLORS.iterator();
                    }
                    nextColor = colorIterator.next();
                    differenceRed = (float) (ARGB.red(nextColor) - ARGB.red(currentColor));
                    differenceGreen = (float) (ARGB.green(nextColor) - ARGB.green(currentColor));
                    differenceBlue = (float) (ARGB.blue(nextColor) - ARGB.blue(currentColor));
                    usedColor = currentColor;
                } else {
                    usedColor = currentColor;
                    usedColor += (0x010000) * (int) (differenceRed * done);
                    usedColor += (0x000100) * (int) (differenceGreen * done);
                    usedColor += (int) (differenceBlue * done);
                }

                locationsList.points().add(new ColoredPosition(position, usedColor | 0xff000000));
            } else {
                locationsList
                        .points()
                        .add(new ColoredPosition(
                                position, attributes.pathColor().asInt()));
            }
        }

        ColoredPath lastLocationList = null;
        Long2ObjectMap<List<ColoredPath>> sampleByChunk = new Long2ObjectOpenHashMap<>();
        ChunkPos lastChunkPos = null;
        for (int i = 0; i < locationsList.points().size(); i++) {
            Vec3 position = locationsList.points().get(i).position();
            ChunkPos currentChunkPos =
                    new ChunkPos(MathUtils.floor(position.x()) >> 4, MathUtils.floor(position.z()) >> 4);
            if (!currentChunkPos.equals(lastChunkPos)) {
                if (lastChunkPos != null
                        && position.distanceTo(locationsList.points().get(i - 1).position()) < 32) {
                    lastLocationList.points().add(locationsList.points().get(i));
                }

                lastChunkPos = currentChunkPos;
                sampleByChunk.putIfAbsent(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z), new ArrayList<>());
                lastLocationList = new ColoredPath(new ArrayList<>());
                sampleByChunk
                        .get(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z))
                        .add(lastLocationList);
            }
            lastLocationList.points().add(locationsList.points().get(i));
        }
        return sampleByChunk;
    }

    private static List<Vector2d> generateSimplifiedPoints(PathPoints raw) {
        return PathGeometry.simplifyForMap(raw.points()).stream()
                .map(point -> new Vector2d(point.x(), point.z()))
                .toList();
    }

    private static Long2ObjectMap<Set<BlockPos>> getChests(Set<BlockPos> chests) {
        Long2ObjectMap<Set<BlockPos>> result = new Long2ObjectOpenHashMap<>();
        for (BlockPos pos : chests) {
            Set<BlockPos> addTo = result.computeIfAbsent(new ChunkPos(pos).toLong(), (chunk) -> new HashSet<>());
            addTo.add(pos);
        }
        return result;
    }

    private static Long2ObjectMap<List<PathNote>> getNotes(List<PathNote> notes) {
        Long2ObjectMap<List<PathNote>> result = new Long2ObjectOpenHashMap<>();
        for (PathNote note : notes) {
            ChunkPos chunk = new ChunkPos(BlockPos.containing(note.position()));
            List<PathNote> notesChunk = result.computeIfAbsent(chunk.toLong(), (chunkPos) -> new ArrayList<>());
            notesChunk.add(note);
        }
        return result;
    }
}
