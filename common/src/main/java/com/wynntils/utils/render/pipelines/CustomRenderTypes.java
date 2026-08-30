/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.pipelines;

import com.wynntils.utils.render.Texture;
import java.util.function.Function;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class CustomRenderTypes {
    public static final RenderType LOOTRUN_QUAD = RenderType.create(
            "wynntils_lootrun_quad",
            RenderSetup.builder(CustomRenderPipelines.LOOTRUN_QUAD_PIPELINE)
                    .withTexture("Sampler0", Texture.LOOTRUN_LINE.identifier())
                    .createRenderSetup());

    public static final RenderType POSITION_COLOR_QUAD = RenderType.create(
            "wynntils_position_color_quad",
            RenderSetup.builder(CustomRenderPipelines.POSITION_COLOR_QUAD_PIPELINE)
                    .createRenderSetup());

    private static final Function<Identifier, RenderType> ARMOR_TRANSLUCENT = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(CustomRenderPipelines.ARMOR_TRANSLUCENT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup();
        return RenderType.create("armor_translucent", state);
    });

    public static RenderType armorTranslucent(Identifier texture) {
        return ARMOR_TRANSLUCENT.apply(texture);
    }
}
