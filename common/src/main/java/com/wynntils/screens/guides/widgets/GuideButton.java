/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public abstract class GuideButton extends WynntilsButton {
    protected final GuideItemStack itemStack;
    protected final String itemName;

    protected GuideButton(int x, int y, int width, GuideItemStack itemStack) {
        super(x, y, width, 20, Component.empty());

        this.itemStack = itemStack;

        Optional<NamedItemProperty> namedItemPropertyOpt =
                Models.Item.asWynnItemProperty(itemStack, NamedItemProperty.class);
        if (namedItemPropertyOpt.isPresent()) {
            itemName = namedItemPropertyOpt.get().getName();
        } else if (itemStack != null) {
            // All guide items should be a NamedItemProperty but just in case
            itemName = StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting();
        } else {
            // Set guide buttons
            itemName = "";
        }
    }

    protected GuideButton(int x, int y, GuideItemStack itemStack) {
        this(x, y, 20, itemStack);
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawSprite(guiGraphics, Texture.HIGHLIGHT_WYNN, getColor(), getX() - 6, getY() - 6);

        RenderUtils.renderItem(guiGraphics, itemStack, getX() + 2, getY() + 2);

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

    protected abstract CustomColor getColor();

    protected abstract void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY);

    @Override
    public void onPress(InputWithModifiers input) {
        if (itemName.isEmpty()) return;

        if (input.hasShiftDown()) {
            Services.Favorites.toggleFavorite(itemName);
        }
    }
}
