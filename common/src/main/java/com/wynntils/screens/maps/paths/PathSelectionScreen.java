/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.services.mapdata.MapPathsService.StoredPath;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PathSelectionScreen extends WynntilsScreen {
    private final Screen previous;
    private String filter = "";
    private int page;
    private int x;
    private int y;
    private String error = "";
    private TexturedTextInputBoxWidget searchWidget;

    public PathSelectionScreen(Screen previous) {
        super(Component.translatable("screens.wynntils.map.paths.stored"));
        this.previous = previous;
    }

    @Override
    protected void doInit() {
        super.doInit();
        boolean searching = searchWidget != null && getFocusedTextInput() == searchWidget;
        setFocusedTextInput(null);
        x = (width - Texture.MANAGER_BACKGROUND.width()) / 2;
        y = (height - Texture.MANAGER_BACKGROUND.height()) / 2;
        List<StoredPath> entries = Services.MapPaths.getStoredPaths().stream()
                .filter(path -> path.label().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(StoredPath::label))
                .toList();
        page = Math.min(page, Math.max(0, (entries.size() - 1) / 8));
        TexturedTextInputBoxWidget search = new TexturedTextInputBoxWidget(
                x + 15,
                y + 18,
                350,
                20,
                text -> {
                    if (!text.equals(filter)) {
                        filter = text;
                        page = 0;
                        rebuildWidgets();
                    }
                },
                this,
                TexturedTextInputBoxWidget.Mode.STRING);
        search.setTextBoxInput(filter);
        addRenderableWidget(search);
        searchWidget = search;
        if (searching) {
            setFocusedTextInput(search);
            search.setFocused(true);
        }
        button(x + 375, y + 18, 90, "new", () -> {
            Services.MapPaths.cancel();
            Services.MapPaths.configure(
                    PathCategoriesProvider.PATH_CATEGORY,
                    "",
                    new MapAttributesBuilder().asPathAttributes().build(),
                    true);
            McUtils.setScreen(PathCreationScreen.create(this));
        });
        button(x + 475, y + 18, 86, "back", this::onClose);
        for (int row = 0; row < 8 && page * 8 + row < entries.size(); row++) {
            StoredPath path = entries.get(page * 8 + row);
            int rowY = y + 52 + row * 28;
            StyledButton name = button(x + 15, rowY, 278, "edit", () -> edit(path));
            name.setMessage(Component.literal(path.label()));
            button(x + 303, rowY, 80, "load", () -> {
                Services.MapPaths.load(path);
                McUtils.setScreen(null);
            });
            button(x + 393, rowY, 80, "edit", () -> edit(path));
            button(
                    x + 483,
                    rowY,
                    78,
                    "delete",
                    () -> McUtils.setScreen(new ConfirmScreen(
                            confirmed -> {
                                if (confirmed) {
                                    try {
                                        Services.MapPaths.delete(path);
                                    } catch (Exception e) {
                                        error = e.getMessage();
                                    }
                                }
                                McUtils.setScreen(this);
                            },
                            Component.translatable("screens.wynntils.map.paths.delete"),
                            Component.literal(path.label()))));
        }
        button(x + 15, y + 282, 80, "previous", () -> {
            page = Math.max(0, page - 1);
            rebuildWidgets();
        });
        button(x + 105, y + 282, 80, "next", () -> {
            if ((page + 1) * 8 < entries.size()) page++;
            rebuildWidgets();
        });
    }

    private void edit(StoredPath path) {
        Services.MapPaths.edit(path);
        McUtils.setScreen(PathCreationScreen.create(this));
    }

    private StyledButton button(int x, int y, int width, String key, Runnable action) {
        return addRenderableWidget(new StyledButton(
                x,
                y,
                width,
                20,
                Component.translatable("screens.wynntils.map.paths." + key),
                Texture.MANAGER_WIDGET_BACKGROUND,
                action));
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(graphics, Texture.MANAGER_BACKGROUND, x, y);
        renderables.forEach(r -> r.render(graphics, mouseX, mouseY, partialTick));
        if (!error.isEmpty())
            graphics.drawString(
                    font,
                    Component.translatable("screens.wynntils.map.paths.error", error),
                    x + 195,
                    y + 287,
                    CommonColors.RED.asInt(),
                    false);
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previous);
    }
}
