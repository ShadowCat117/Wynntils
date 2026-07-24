/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomModelData;

public class IngredientGuideButton extends GuideButton {
    private static final CustomColor INGREDIENT_HIGHLIGHT_COLOR = CustomColor.fromInt(0x4EDF48);
    private static final Identifier TOOLTIP_STYLE = Identifier.withDefaultNamespace("profession_ingredient");
    private static final String PROFESSION_STAR_KEY = "profession_tier_";

    private final GuideIngredientItemStack ingredientItemStack;

    public IngredientGuideButton(int x, int y, GuideIngredientItemStack itemStack) {
        super(x, y, itemStack);

        this.ingredientItemStack = itemStack;

        List<String> modelDataString =
                List.of(PROFESSION_STAR_KEY + itemStack.getIngredientInfo().tier());
        CustomModelData oldCustomModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        CustomModelData newCustomModelData = new CustomModelData(
                oldCustomModelData.floats(), oldCustomModelData.flags(), modelDataString, oldCustomModelData.colors());

        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, newCustomModelData);
        itemStack.set(DataComponents.TOOLTIP_STYLE, TOOLTIP_STYLE);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        itemStack.queueGuideTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == McUtils.options().keySwapOffhand.key.getValue()) {
            ingredientItemStack.changePage();
            return true;
        }

        return false;
    }

    // FIXME: This should be painted by ItemHighlightFeature instead...
    @Override
    protected CustomColor getColor() {
        return INGREDIENT_HIGHLIGHT_COLOR;
    }
}
