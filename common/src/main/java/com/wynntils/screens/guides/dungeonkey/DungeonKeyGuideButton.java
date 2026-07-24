/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.dungeonkey;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.gui.GuiGraphics;

public class DungeonKeyGuideButton extends GuideButton {
    private final GuideDungeonKeyItemStack guideDungeonKeyItemStack;

    public DungeonKeyGuideButton(int x, int y, GuideDungeonKeyItemStack itemStack) {
        super(x, y, itemStack);

        this.guideDungeonKeyItemStack = itemStack;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor color = getColor();
        RenderUtils.drawSprite(guiGraphics, Texture.HIGHLIGHT_WYNN, color, getX() - 6, getY() - 6);

        RenderUtils.renderItem(guiGraphics, itemStack, getX() + 2, getY() + 2);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(
                                guideDungeonKeyItemStack.getDungeon().getInitials()),
                        getX() + 2,
                        getX() + 14,
                        getY() + 8,
                        0,
                        color,
                        HorizontalAlignment.LEFT,
                        TextShadow.OUTLINE);

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
        return CustomColor.fromChatFormatting(guideDungeonKeyItemStack.getHighlightColor());
    }
}
