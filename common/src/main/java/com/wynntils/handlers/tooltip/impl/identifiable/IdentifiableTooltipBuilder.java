/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.core.components.Services;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipPage;
import com.wynntils.handlers.tooltip.type.TooltipSegment;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.services.itemweight.type.ItemWeighting;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * A builder for identifiable item tooltips.
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public final class IdentifiableTooltipBuilder<T, U> extends TooltipBuilder {
    private final IdentifiableItemProperty<T, U> itemProperty;

    private final IdentifiableTooltipComponent<T, U> tooltipComponent;
    private final T itemInfo;
    private final U itemInstance;
    private final boolean hideUnidentified;
    private final boolean showItemType;

    private final List<Component> parsedBaseStats;
    private final List<Component> parsedFooter;

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemProperty,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            boolean hideUnidentified,
            boolean showItemType,
            String source) {
        super(source);
        this.itemProperty = itemProperty;
        this.tooltipComponent = tooltipComponent;
        this.itemInfo = itemProperty.getItemInfo();
        this.itemInstance = itemProperty.getItemInstance().orElse(null);
        this.hideUnidentified = hideUnidentified;
        this.showItemType = showItemType;
        this.parsedBaseStats = null;
        this.parsedFooter = null;
    }

    private IdentifiableTooltipBuilder(
            IdentifiableItemProperty<T, U> itemProperty,
            List<Component> parsedBaseStats,
            List<Component> parsedFooter) {
        super("");
        this.itemProperty = itemProperty;
        this.tooltipComponent = null;
        this.itemInfo = itemProperty.getItemInfo();
        this.itemInstance = itemProperty.getItemInstance().orElse(null);
        this.hideUnidentified = false;
        this.showItemType = false;
        this.parsedBaseStats = parsedBaseStats;
        this.parsedFooter = parsedFooter;
    }

    /**
     * Creates a tooltip builder that provides synthetic tooltip segments
     */
    public static <T, U> IdentifiableTooltipBuilder<T, U> buildNewItem(
            IdentifiableItemProperty<T, U> identifiableItem,
            IdentifiableTooltipComponent<T, U> tooltipComponent,
            boolean hideUnidentified,
            boolean showItemType,
            String source) {
        return new IdentifiableTooltipBuilder<>(
                identifiableItem, tooltipComponent, hideUnidentified, showItemType, source);
    }

    /**
     * Creates a tooltip builder that parses segments from an existing tooltip
     */
    public static IdentifiableTooltipBuilder fromParsedItemStack(
            ItemStack itemStack, IdentifiableItemProperty itemInfo) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        return new IdentifiableTooltipBuilder(itemInfo, splitLore.a(), splitLore.b());
    }

    @Override
    protected int getMaximumWidth(
            TooltipPage page,
            TooltipStyle style,
            List<Component> identificationLines,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        if (tooltipComponent == null) {
            return super.getMaximumWidth(page, style, identificationLines, weightSource, weightDecorator);
        }

        int maximumWidth = tooltipComponent.getMaximumWidth(
                itemInfo,
                itemInstance,
                style,
                identificationLines,
                IdentifiableTooltipComponent.DEFAULT_TOOLTIP_WIDTH);

        List<Component> widthProbeLines = new ArrayList<>();
        for (TooltipSegment segment : getSegmentOrder(page)) {
            switch (segment) {
                case IDENTIFICATIONS -> widthProbeLines.addAll(identificationLines);
                case WEIGHTINGS -> widthProbeLines.addAll(getWeightingLines(weightSource, weightDecorator, page));
                case BASE_STATS -> widthProbeLines.addAll(
                        tooltipComponent.buildBaseStatsTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
                case REQUIREMENTS -> widthProbeLines.addAll(
                        tooltipComponent.buildRequirementsTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
                case EXTRA_INFO -> widthProbeLines.addAll(
                        tooltipComponent.buildExtraInfoTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
                case FOOTER -> widthProbeLines.addAll(
                        tooltipComponent.buildFooterTooltip(itemInfo, itemInstance, showItemType, maximumWidth));
                case PAGE_TEXT -> {
                    // handled later by item-specific page text segment builders
                }
            }
        }

        return Math.max(maximumWidth, getMaximumComponentWidth(widthProbeLines));
    }

    @Override
    protected List<TooltipSegment> getSegmentOrder(TooltipPage page) {
        if (tooltipComponent == null) {
            return switch (page) {
                case PAGE_1 -> List.of(TooltipSegment.BASE_STATS, TooltipSegment.IDENTIFICATIONS, TooltipSegment.FOOTER);
                case PAGE_2 -> List.of(TooltipSegment.BASE_STATS, TooltipSegment.FOOTER);
                case PAGE_3 -> List.of(TooltipSegment.PAGE_TEXT);
            };
        }

        return switch (page) {
            case PAGE_1 -> insertSegmentBefore(
                    List.of(
                            TooltipSegment.BASE_STATS,
                            TooltipSegment.REQUIREMENTS,
                            TooltipSegment.EXTRA_INFO,
                            TooltipSegment.IDENTIFICATIONS,
                            TooltipSegment.FOOTER),
                    TooltipSegment.IDENTIFICATIONS,
                    TooltipSegment.WEIGHTINGS);
            case PAGE_2 -> List.of(TooltipSegment.BASE_STATS, TooltipSegment.EXTRA_INFO, TooltipSegment.FOOTER);
            case PAGE_3 -> List.of(TooltipSegment.REQUIREMENTS, TooltipSegment.PAGE_TEXT);
        };
    }

    @Override
    protected List<Component> getSegmentLines(
            TooltipSegment segment,
            TooltipPage page,
            int maximumWidth,
            TooltipStyle style,
            List<Component> identificationLines,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        if (tooltipComponent == null) {
            return switch (segment) {
                case BASE_STATS -> new ArrayList<>(parsedBaseStats);
                case IDENTIFICATIONS -> new ArrayList<>(identificationLines);
                case FOOTER -> new ArrayList<>(parsedFooter);
                case WEIGHTINGS, REQUIREMENTS, EXTRA_INFO, PAGE_TEXT -> List.of();
            };
        }

        return switch (segment) {
            case BASE_STATS -> new ArrayList<>(
                    tooltipComponent.buildBaseStatsTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
            case WEIGHTINGS -> getWeightingLines(weightSource, weightDecorator, page);
            case REQUIREMENTS -> new ArrayList<>(
                    tooltipComponent.buildRequirementsTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
            case EXTRA_INFO -> new ArrayList<>(
                    tooltipComponent.buildExtraInfoTooltip(itemInfo, itemInstance, hideUnidentified, maximumWidth));
            case IDENTIFICATIONS -> new ArrayList<>(identificationLines);
            case FOOTER -> new ArrayList<>(
                    tooltipComponent.buildFooterTooltip(itemInfo, itemInstance, showItemType, maximumWidth));
            case PAGE_TEXT -> List.of();
        };
    }

    private List<Component> getWeightingLines(
            ItemWeightSource weightSource, TooltipWeightDecorator weightDecorator, TooltipPage page) {
        // Only gear page 1 has weightings
        if (page != TooltipPage.PAGE_1
                || weightSource == ItemWeightSource.NONE
                || !itemProperty.hasOverallValue()
                || !(itemProperty.getItemInfo() instanceof GearInfo gearInfo)
                || weightDecorator == null) {
            return List.of();
        }

        List<ItemWeighting> noriWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.NORI);
        List<ItemWeighting> wynnpoolWeightings =
                Services.ItemWeight.getItemWeighting(gearInfo.name(), ItemWeightSource.WYNNPOOL);

        boolean addNori = (weightSource == ItemWeightSource.NORI || weightSource == ItemWeightSource.ALL)
                && !noriWeightings.isEmpty();
        boolean addWynnpool = (weightSource == ItemWeightSource.WYNNPOOL || weightSource == ItemWeightSource.ALL)
                && !wynnpoolWeightings.isEmpty();

        if (!addNori && !addWynnpool) {
            return List.of();
        }

        List<Component> weightingLines = new ArrayList<>();

        if (addNori) {
            weightingLines.add(Services.ItemWeight.NORI_HEADER);
            addWeightingLines(weightingLines, noriWeightings, weightDecorator);

            if (!weightingLines.isEmpty() && !weightingLines.getLast().equals(Component.empty())) {
                weightingLines.add(Component.empty());
            }
        }

        if (addWynnpool) {
            weightingLines.add(Services.ItemWeight.WYNNPOOL_HEADER);
            addWeightingLines(weightingLines, wynnpoolWeightings, weightDecorator);

            if (!weightingLines.isEmpty() && !weightingLines.getLast().equals(Component.empty())) {
                weightingLines.add(Component.empty());
            }
        }

        return weightingLines;
    }

    private void addWeightingLines(
            List<Component> output,
            List<ItemWeighting> weightings,
            TooltipWeightDecorator weightDecorator) {
        for (ItemWeighting weighting : weightings) {
            for (MutableComponent component : weightDecorator.getLines(weighting, itemProperty)) {
                output.add(component);
            }
        }
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return TooltipIdentifications.buildTooltip(itemProperty, currentClass, decorator, style);
    }
}
