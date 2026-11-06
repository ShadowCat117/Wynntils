/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.features.type.MapPath;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.joml.Vector2d;

public record CompiledMapPath(
        MapPath feature,
        List<Vector2d> simplifiedPath,
        Long2ObjectMap<List<ColoredPath>> points,
        Long2ObjectMap<Set<BlockPos>> highlights,
        Long2ObjectMap<List<PathNote>> notes) {}
