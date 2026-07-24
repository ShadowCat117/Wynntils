/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public abstract class GuideNavigationButton extends WynntilsButton {
    private static final Identifier BACKGROUND =
            Identifier.withDefaultNamespace("textures/gui/sprites/container/inventory/effect_background.png");

    private final ItemStack itemToRender;

    protected GuideNavigationButton(int x, int y, ItemStack itemToRender) {
        super(x, y, 20, 20, Component.empty());
        this.itemToRender = itemToRender;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawScalingTexturedRect(guiGraphics, BACKGROUND, getX(), getY(), getWidth(), getHeight(), 32, 32);

        RenderUtils.renderItem(guiGraphics, itemToRender, getX() + 2, getY() + 2);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(getTooltip(), mouseX, mouseY);
        }
    }

    protected abstract Component getTooltip();
}
