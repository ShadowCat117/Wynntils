/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.CompiledMapPath;
import com.wynntils.services.mapdata.paths.MapPathCompiler;
import com.wynntils.services.mapdata.paths.MapPathWorldRenderer;
import com.wynntils.services.mapdata.type.MapIcon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public final class MapPathsService extends Service {
    private final Map<MapPath, CachedPath> compiled = new HashMap<>();

    public MapPathsService() {
        super(List.of());
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

    private record CachedPath(ResolvedMapAttributes attributes, CompiledMapPath compiled) {}
}
