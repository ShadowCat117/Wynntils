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

/** A positioned annotation stored as a Minecraft component, with its decoded component cached for rendering. */
public final class PathNote {
    private final Vec3 position;
    private final JsonElement text;
    private transient Component component;

    public PathNote(Vec3 position, JsonElement text) {
        this.position = position;
        this.text = text;
    }

    public static PathNote of(Vec3 position, Component text) {
        return new PathNote(
                position,
                ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }

    public Vec3 position() {
        return position;
    }

    public Component component() {
        if (component == null) {
            component =
                    ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, text).getOrThrow();
        }

        return component;
    }

    public boolean validate() {
        if (position == null
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())
                || text == null) return false;

        try {
            component();

            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
