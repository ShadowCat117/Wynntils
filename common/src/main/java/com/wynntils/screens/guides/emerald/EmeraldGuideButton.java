/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;

public class EmeraldGuideButton extends GuideButton {
    private final GuideEmeraldItemStack emeraldItemStack;

    public EmeraldGuideButton(int x, int y, GuideEmeraldItemStack itemStack) {
        super(x, y, itemStack);

        this.emeraldItemStack = itemStack;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        RenderUtils.drawSprite(guiGraphics, Texture.HIGHLIGHT_WYNN, color, getX() - 6, getY() - 6);

        RenderUtils.renderItem(guiGraphics, itemStack, getX() + 2, getY() + 2);

        if (emeraldItemStack.getTier() > 0) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(String.valueOf(emeraldItemStack.getTier())),
                            getX() + 4,
                            getX() + 16,
                            getY() + 10,
                            0,
                            color,
                            HorizontalAlignment.CENTER,
                            TextShadow.OUTLINE);
        }

        if (Services.Favorites.isFavorite(itemName)) {
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    Texture.FAVORITE_ICON.identifier(),
                    getX(),
                    getY(),
                    9,
                    9,
                    Texture.FAVORITE_ICON.width(),
                    Texture.FAVORITE_ICON.height());
        }

        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(ChatFormatting.GREEN);
    }
}
