/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;

public class AspectGuideButton extends GuideButton {
    private static final CustomColor TIER_1_HIGHLIGHT_COLOR = CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY);
    private static final CustomColor TIER_2_HIGHLIGHT_COLOR = new CustomColor(205, 127, 50);
    private static final CustomColor TIER_3_HIGHLIGHT_COLOR = new CustomColor(192, 192, 192);
    private static final CustomColor TIER_4_HIGHLIGHT_COLOR = new CustomColor(255, 215, 0);

    private final GuideAspectItemStack aspectItemStack;
    private boolean builtTooltip = false;

    private final CustomColor textColor;

    public AspectGuideButton(int x, int y, GuideAspectItemStack itemStack) {
        super(x, y, itemStack);

        this.aspectItemStack = itemStack;

        textColor = switch (itemStack.getTier()) {
            case 2 -> TIER_2_HIGHLIGHT_COLOR;
            case 3 -> TIER_3_HIGHLIGHT_COLOR;
            case 4 -> TIER_4_HIGHLIGHT_COLOR;
            default -> TIER_1_HIGHLIGHT_COLOR;
        };
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawSprite(guiGraphics, Texture.HIGHLIGHT_WYNN, getColor(), getX() - 6, getY() - 6);

        RenderUtils.renderItem(guiGraphics, itemStack, getX() + 2, getY() + 2);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(MathUtils.toRoman(aspectItemStack.getTier())),
                        getX() + 4,
                        getX() + 16,
                        getY() + 10,
                        0,
                        textColor,
                        HorizontalAlignment.CENTER,
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
        if (!builtTooltip) {
            aspectItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromChatFormatting(
                aspectItemStack.getAspectInfo().gearTier().getChatFormatting());
    }
}
