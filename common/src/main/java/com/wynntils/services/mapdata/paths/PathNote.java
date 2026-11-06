/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.phys.Vec3;

/** A positioned, rich-text annotation belonging to a map path. */
public record PathNote(Vec3 position, JsonElement text) {
    public static PathNote of(Vec3 position, Component text) {
        return new PathNote(
                position,
                ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }

    public Component component() {
        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, text)
                .result()
                .orElse(Component.empty());
    }

    public boolean validate() {
        return position != null
                && (Double.isFinite(position.x()) && Double.isFinite(position.y()) && Double.isFinite(position.z()))
                && text != null
                && ComponentSerialization.CODEC
                        .parse(JsonOps.INSTANCE, text)
                        .result()
                        .isPresent();
    }
}
