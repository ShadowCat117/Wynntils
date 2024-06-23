/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.HudRenderEvent;
import com.wynntils.mc.event.RenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class CustomHudOffsetsFeature extends Feature {
    @Persisted
    public final Config<Integer> actionBarXOffset = new Config<>(0);

    @Persisted
    public final Config<Integer> actionBarYOffset = new Config<>(0);

    @Persisted
    public final Config<Boolean> hideCrosshair = new Config<>(false);

    @Persisted
    public final Config<Boolean> hideHeldItemName = new Config<>(false);

    @Persisted
    public final Config<Integer> heldItemNameXOffset = new Config<>(0);

    @Persisted
    public final Config<Integer> heldItemNameYOffset = new Config<>(0);

    @Persisted
    public final Config<Boolean> hideHotbar = new Config<>(false);

    @Persisted
    public final Config<Integer> hotbarXOffset = new Config<>(0);

    @Persisted
    public final Config<Integer> hotbarYOffset = new Config<>(0);

    @Persisted
    public final Config<Boolean> hideXpBar = new Config<>(false);

    @Persisted
    public final Config<Integer> xpBarXOffset = new Config<>(0);

    @Persisted
    public final Config<Integer> xpBarYOffset = new Config<>(0);

    @SubscribeEvent
    public void onRender(RenderEvent.Pre event) {
        if (event.getType() == RenderEvent.ElementType.CROSSHAIR && hideCrosshair.get()) {
            event.setCanceled(true);
        } else if (event.getType() == RenderEvent.ElementType.HELD_ITEM_NAME && hideHeldItemName.get()) {
            event.setCanceled(true);
        } else if (event.getType() == RenderEvent.ElementType.HOTBAR && hideHotbar.get()) {
            event.setCanceled(true);
        } else if (event.getType() == RenderEvent.ElementType.XP_BAR && hideXpBar.get()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHudRender(HudRenderEvent event) {
        if (event.getElementType() == RenderEvent.ElementType.ACTION_BAR) {
            if (event.getRenderOffset() == HudRenderEvent.RenderOffset.X) {
                event.setOffset(actionBarXOffset.get());
            } else {
                event.setOffset(actionBarYOffset.get());
            }
        } else if (event.getElementType() == RenderEvent.ElementType.HELD_ITEM_NAME) {
            if (event.getRenderOffset() == HudRenderEvent.RenderOffset.X) {
                event.setOffset(heldItemNameXOffset.get());
            } else {
                event.setOffset(heldItemNameYOffset.get());
            }
        } else if (event.getElementType() == RenderEvent.ElementType.HOTBAR) {
            if (event.getRenderOffset() == HudRenderEvent.RenderOffset.X) {
                event.setOffset(hotbarXOffset.get());
            } else {
                event.setOffset(hotbarYOffset.get());
            }
        } else if (event.getElementType() == RenderEvent.ElementType.XP_BAR) {
            if (event.getRenderOffset() == HudRenderEvent.RenderOffset.X) {
                event.setOffset(xpBarXOffset.get());
            } else {
                event.setOffset(xpBarYOffset.get());
            }
        }
    }
}
