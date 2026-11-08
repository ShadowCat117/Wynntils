/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.maps.managers.widgets.ManagerSearchWidget;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.CategoryTreeWidget;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PathCategoryScreen extends WynntilsScreen {
    private final Screen previous;
    private final Consumer<String> onSelect;
    private String category;
    private String error = "";
    private int x;
    private int y;

    public PathCategoryScreen(Screen previous, String category, Consumer<String> onSelect) {
        super(Component.translatable("screens.wynntils.map.paths.category"));
        this.previous = previous;
        this.category = category;
        this.onSelect = onSelect;
    }

    @Override
    protected void doInit() {
        super.doInit();
        x = (width - Texture.MANAGER_BACKGROUND.width()) / 2;
        y = (height - Texture.MANAGER_BACKGROUND.height()) / 2;
        TexturedTextInputBoxWidget input = new TexturedTextInputBoxWidget(
                x + 15, y + 282, 426, 20, value -> category = value, this, TexturedTextInputBoxWidget.Mode.STRING);
        CategoryTreeWidget tree = new CategoryTreeWidget(
                x + 15,
                y + 43,
                546,
                225,
                input::setTextBoxInput,
                id -> id.equals(PathCategoriesProvider.PATH_CATEGORY)
                        || id.startsWith(PathCategoriesProvider.PATH_CATEGORY + ":"));
        tree.setCategories(Stream.concat(Services.MapData.allPossibleCategories(), Stream.of(category))
                .distinct()
                .sorted()
                .toList());
        tree.selectCategory(category);
        addRenderableWidget(tree);
        input.setTextBoxInput(category);
        addRenderableWidget(input);
        addRenderableWidget(new ManagerSearchWidget(
                x + 15, y + 18, 541 - Texture.BUILD_LOADOUTS_SEARCH_ICON.width(), 20, tree::filter, this));
        addRenderableWidget(new StyledButton(
                x + 451,
                y + 282,
                110,
                20,
                Component.translatable("screens.wynntils.map.paths.useCategory"),
                Texture.MANAGER_WIDGET_BACKGROUND,
                () -> {
                    if (!category.matches("[a-z0-9_-]+(:[a-z0-9_-]+)*")) {
                        error = "screens.wynntils.map.paths.invalidCategory";

                        return;
                    }

                    onSelect.accept(category);
                    onClose();
                }));
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(graphics, Texture.MANAGER_BACKGROUND, x, y);
        renderables.forEach(widget -> widget.render(graphics, mouseX, mouseY, partialTick));
        if (!error.isEmpty()) {
            graphics.drawString(font, Component.translatable(error), x + 15, y + 271, CommonColors.RED.asInt(), false);
        }
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previous);
    }
}
