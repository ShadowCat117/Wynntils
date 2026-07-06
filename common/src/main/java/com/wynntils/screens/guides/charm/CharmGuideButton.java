/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.charm;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CharmGuideButton extends GuideButton {
    private final GuideCharmItemStack charmItemStack;
    private boolean builtTooltip = false;

    public CharmGuideButton(int x, int y, GuideCharmItemStack itemStack) {
        super(x, y, itemStack);

        this.charmItemStack = itemStack;
    }

    @Override
    protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!builtTooltip) {
            charmItemStack.buildTooltip();
            builtTooltip = true;
        }

        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected CustomColor getColor() {
        return CustomColor.fromTextColor(charmItemStack.getCharmInfo().tier().getTextColor());
    }
}
