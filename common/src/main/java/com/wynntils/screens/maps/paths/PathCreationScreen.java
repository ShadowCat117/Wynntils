/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.paths;

import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.screens.base.widgets.ColorPickerWidget;
import com.wynntils.screens.maps.managers.IconSelectionScreen;
import com.wynntils.screens.maps.managers.widgets.StyledButton;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.screens.maps.managers.widgets.categorymanager.CategoryTreeWidget;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.impl.MapVisibilityImpl;
import com.wynntils.services.mapdata.attributes.resolving.MapAttributesResolver;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class PathCreationScreen extends WynntilsScreen {
    private record Caption(Component text, int x, int y) {}

    private final Screen previous;
    private final MapAttributesBuilder settings;
    private final List<Caption> captions = new ArrayList<>();
    private String category;
    private String label;
    private boolean recordHighlights;
    private String note = "";
    private String error = "";
    private int x;
    private int y;

    private PathCreationScreen(Screen previous) {
        super(Component.translatable("screens.wynntils.map.paths.title"));
        this.previous = previous;
        this.settings = new MapAttributesBuilder().from(Services.MapPaths.getAttributes());
        this.category = Services.MapPaths.getCategory();
        this.label = Services.MapPaths.getLabel();
        this.recordHighlights = Services.MapPaths.isRecordingHighlights();
    }

    public static Screen create(Screen previous) {
        return new PathCreationScreen(previous);
    }

    private MapPathAttributesImpl attributes() {
        return settings.asPathAttributes().build();
    }

    private ResolvedMapAttributes effective() {
        return MapAttributesResolver.resolve(
                new MapPathImpl("preview:path", category, attributes(), List.of(Vec3.ZERO, new Vec3(1, 0, 0))));
    }

    @Override
    protected void doInit() {
        super.doInit();
        captions.clear();
        setFocusedTextInput(null);
        x = (width - Texture.MANAGER_BACKGROUND.width()) / 2;
        y = (height - Texture.MANAGER_BACKGROUND.height()) / 2;
        int left = x + 15;
        int right = x + 220;
        CategoryTreeWidget categories = new CategoryTreeWidget(
                left,
                y + 42,
                195,
                165,
                selected -> {
                    category = selected;
                    rebuildWidgets();
                },
                id -> id.startsWith(PathCategoriesProvider.PATH_CATEGORY));
        this.addRenderableWidget(categories);
        categories.setCategories(java.util.stream.Stream.concat(
                        Services.MapData.allPossibleCategories(), java.util.stream.Stream.of(category))
                .distinct()
                .sorted()
                .toList());
        categories.selectCategory(category);
        captions.add(new Caption(Component.translatable("screens.wynntils.map.paths.category"), left, y + 29));
        TexturedTextInputBoxWidget categoryInput =
                field(left, y + 221, 195, "categoryId", category, value -> {}, TexturedTextInputBoxWidget.Mode.STRING);
        button(left, y + 246, 195, "useCategory", () -> {
            String selected = categoryInput.getTextBoxInput();
            if (!selected.matches("[a-z0-9_-]+(:[a-z0-9_-]+)*")) {
                error = I18n.get("screens.wynntils.map.paths.invalidCategory");
                return;
            }
            category = selected;
            rebuildWidgets();
        });
        field(left, y + 281, 130, "note", note, value -> note = value, TexturedTextInputBoxWidget.Mode.STRING);
        button(left + 135, y + 281, 60, "addNote", () -> {
            configure();
            if (note.isBlank() || !Services.MapPaths.addNote(Component.literal(note))) {
                error = I18n.get("screens.wynntils.map.paths.noDraft");
                return;
            }
            note = "";
            rebuildWidgets();
        });

        MapPathAttributesImpl attrs = attributes();
        ResolvedMapAttributes defaults = effective();
        field(right, y + 42, 341, "label", label, value -> label = value, TexturedTextInputBoxWidget.Mode.STRING);
        field(
                right,
                y + 73,
                341,
                "description",
                attrs.getDescription().orElse(""),
                settings::setDescription,
                TexturedTextInputBoxWidget.Mode.STRING);
        color(right, y + 104, 112, "pathColor", attrs.getPathColor(), defaults.pathColor(), settings::setPathColor);
        button(right + 117, y + 104, 46, "rainbow", () -> {
            settings.setPathColor(
                    defaults.pathColor().equals(WynncraftShaderColor.RAINBOW.color)
                            ? CommonColors.LIGHT_BLUE
                            : WynncraftShaderColor.RAINBOW.color);
            rebuildWidgets();
        });
        button(right + 173, y + 104, 168, "texture", () -> {
            configure();
            McUtils.setScreen(IconSelectionScreen.create(
                    this,
                    icon -> settings.setIcon(icon == null ? MapIcon.NO_ICON_ID : icon.getIconId()),
                    defaults.iconId()));
        });
        color(
                right,
                y + 135,
                163,
                "fillColor",
                MapAttributes.firstColor(attrs.getFillColors()),
                defaults.fillColor().getFirst(),
                settings::setFillColor);
        color(
                right + 173,
                y + 135,
                168,
                "borderColor",
                MapAttributes.firstColor(attrs.getBorderColors()),
                defaults.borderColor().getFirst(),
                settings::setBorderColor);
        number(right, y + 166, 78, "pathWidth", attrs.getPathWidth(), settings::setPathWidth);
        number(right + 88, y + 166, 78, "borderWidth", attrs.getBorderWidth(), settings::setBorderWidth);
        integer(right + 176, y + 166, 77, "priority", attrs.getPriority(), settings::setPriority);
        integer(right + 263, y + 166, 78, "level", attrs.getLevel(), settings::setLevel);
        color(right, y + 197, 163, "labelColor", attrs.getLabelColor(), defaults.labelColor(), settings::setLabelColor);
        StyledButton shadow = button(right + 173, y + 197, 78, "shadow", () -> {
            TextShadow current = attributes().getLabelShadow().orElse(defaults.labelShadow());
            settings.setLabelShadow(TextShadow.values()[(current.ordinal() + 1) % TextShadow.values().length]);
            rebuildWidgets();
        });
        shadow.setMessage(Component.literal(defaults.labelShadow().name()));
        StyledButton hasPath = button(right + 261, y + 197, 80, "hasPath", () -> {
            settings.setHasPath(!defaults.hasPath());
            rebuildWidgets();
        });
        hasPath.setMessage(Component.translatable("screens.wynntils.map.paths.hasPathValue", defaults.hasPath()));
        MapVisibilityImpl visibility =
                (MapVisibilityImpl) attrs.getLabelVisibility().orElse(new MapVisibilityImpl(null, null, null));
        number(
                right,
                y + 228,
                78,
                "labelMin",
                visibility.getMin(),
                value -> settings.setLabelVisibility(new MapVisibilityImpl(
                        value,
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getMax())
                                .orElse(null),
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getFade())
                                .orElse(null))));
        number(
                right + 88,
                y + 228,
                78,
                "labelMax",
                visibility.getMax(),
                value -> settings.setLabelVisibility(new MapVisibilityImpl(
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getMin())
                                .orElse(null),
                        value,
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getFade())
                                .orElse(null))));
        number(
                right + 176,
                y + 228,
                77,
                "labelFade",
                visibility.getFade(),
                value -> settings.setLabelVisibility(new MapVisibilityImpl(
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getMin())
                                .orElse(null),
                        attributes()
                                .getLabelVisibility()
                                .flatMap(v -> v.getMax())
                                .orElse(null),
                        value)));
        number(right + 263, y + 228, 78, "cycleDistance", attrs.getPathCycleDistance(), settings::setPathCycleDistance);
        StyledButton highlights = button(right, y + 254, 165, "recordHighlights", () -> {
            recordHighlights = !recordHighlights;
            rebuildWidgets();
        });
        highlights.setMessage(Component.translatable("screens.wynntils.map.paths.highlightsValue", recordHighlights));
        button(right + 175, y + 254, 80, "resetStyle", () -> {
            settings.from(new MapAttributesBuilder().asPathAttributes().build());
            rebuildWidgets();
        });
        button(right + 261, y + 254, 80, "stored", () -> {
            configure();
            McUtils.setScreen(new PathSelectionScreen(this));
        });
        button(right, y + 281, 107, Services.MapPaths.isRecording() ? "pause" : "record", () -> {
            configure();
            if (Services.MapPaths.isRecording()) Services.MapPaths.pauseRecording();
            else Services.MapPaths.startRecording();
            McUtils.setScreen(null);
        });
        button(right + 117, y + 281, 107, "save", () -> {
            configure();
            try {
                Services.MapPaths.save(label);
                onClose();
            } catch (Exception e) {
                error = e.getMessage();
            }
        });
        button(right + 234, y + 281, 107, "cancel", () -> {
            Services.MapPaths.cancel();
            onClose();
        });
        button(left, y + 12, 90, "back", this::onClose);
    }

    private void configure() {
        Services.MapPaths.configure(category, label, attributes(), recordHighlights);
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

    private TexturedTextInputBoxWidget field(
            int x,
            int y,
            int width,
            String key,
            String value,
            Consumer<String> update,
            TexturedTextInputBoxWidget.Mode mode) {
        captions.add(new Caption(Component.translatable("screens.wynntils.map.paths." + key), x, y - 10));
        TexturedTextInputBoxWidget input = new TexturedTextInputBoxWidget(x, y, width, 18, update, this, mode);
        input.setTextBoxInput(value);
        input.setTooltip(Tooltip.create(Component.translatable("screens.wynntils.map.paths.inherit")));
        return addRenderableWidget(input);
    }

    private void color(
            int x,
            int y,
            int width,
            String key,
            Optional<CustomColor> value,
            CustomColor inherited,
            Consumer<CustomColor> update) {
        TexturedTextInputBoxWidget input = field(
                x,
                y,
                width - 22,
                key,
                value.map(CustomColor::toHexString).orElse(""),
                text -> {
                    if (text.isBlank()) update.accept(null);
                    else {
                        CustomColor color = CustomColor.fromHexString(text);
                        if (color != CustomColor.NONE) update.accept(color);
                    }
                },
                TexturedTextInputBoxWidget.Mode.HEXSTRING);
        input.setTooltip(Tooltip.create(
                Component.translatable("screens.wynntils.map.paths.inheritValue", inherited.toHexString())));
        addRenderableWidget(new ColorPickerWidget(x + width - 20, y, 20, 18, input));
    }

    private void number(int x, int y, int width, String key, Optional<Float> value, Consumer<Float> update) {
        field(
                x,
                y,
                width,
                key,
                value.map(Object::toString).orElse(""),
                text -> {
                    try {
                        if (text.isBlank()) update.accept(null);
                        else {
                            float n = Float.parseFloat(text);
                            if (Float.isFinite(n) && n >= 0) update.accept(n);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                },
                TexturedTextInputBoxWidget.Mode.FLOAT);
    }

    private void integer(int x, int y, int width, String key, Optional<Integer> value, Consumer<Integer> update) {
        field(
                x,
                y,
                width,
                key,
                value.map(Object::toString).orElse(""),
                text -> {
                    try {
                        if (text.isBlank()) update.accept(null);
                        else {
                            int n = Integer.parseInt(text);
                            if (n >= 0) update.accept(n);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                },
                TexturedTextInputBoxWidget.Mode.INTEGER);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawTexturedRect(graphics, Texture.MANAGER_BACKGROUND, x, y);
        graphics.drawString(
                font,
                Component.translatable(
                        "screens.wynntils.map.paths.status",
                        Services.MapPaths.getPointCount(),
                        Services.MapPaths.getHighlightCount(),
                        Services.MapPaths.getNotes().size()),
                x + 220,
                y + 17,
                CommonColors.WHITE.asInt(),
                false);
        for (Caption caption : captions)
            graphics.drawString(font, caption.text(), caption.x(), caption.y(), CommonColors.WHITE.asInt(), false);
        renderables.forEach(r -> r.render(graphics, mouseX, mouseY, partialTick));
        if (!error.isBlank())
            graphics.drawString(
                    font,
                    Component.translatable("screens.wynntils.map.paths.error", error),
                    x + 15,
                    y + 308,
                    CommonColors.RED.asInt(),
                    false);
    }

    @Override
    public void onClose() {
        configure();
        McUtils.setScreen(previous);
    }
}
