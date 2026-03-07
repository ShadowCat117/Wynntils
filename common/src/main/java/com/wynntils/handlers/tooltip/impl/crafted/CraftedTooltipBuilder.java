/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.crafted;

import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipPage;
import com.wynntils.handlers.tooltip.type.TooltipSegment;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.items.properties.CraftedItemProperty;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CraftedTooltipBuilder extends TooltipBuilder {
    private final CraftedItemProperty craftedItem;

    private final List<Component> header;
    private final List<Component> footer;

    private CraftedTooltipBuilder(
            CraftedItemProperty craftedItem, List<Component> header, List<Component> footer, String source) {
        super(source);
        this.craftedItem = craftedItem;
        this.header = header;
        this.footer = footer;
    }

    private CraftedTooltipBuilder(CraftedItemProperty craftedItem, List<Component> header, List<Component> footer) {
        this(craftedItem, header, footer, "");
    }

    public static <T extends CraftedItemProperty> CraftedTooltipBuilder buildNewItem(
            T craftedItem, CraftedTooltipComponent<T> tooltipComponent, String source) {
        List<Component> header = tooltipComponent.buildHeaderTooltip(craftedItem);
        List<Component> footer = tooltipComponent.buildFooterTooltip(craftedItem);
        return new CraftedTooltipBuilder(craftedItem, header, footer, source);
    }

    public static CraftedTooltipBuilder fromParsedItemStack(ItemStack itemStack, CraftedItemProperty craftedItem) {
        List<Component> tooltips = LoreUtils.getTooltipLines(itemStack);

        Pair<List<Component>, List<Component>> splitLore = extractHeaderAndFooter(tooltips);
        List<Component> header = splitLore.a();
        List<Component> footer = splitLore.b();

        return new CraftedTooltipBuilder(craftedItem, header, footer);
    }

    @Override
    protected List<TooltipSegment> getSegmentOrder(TooltipPage page) {
        return switch (page) {
            case PAGE_1 -> List.of(TooltipSegment.BASE_STATS, TooltipSegment.IDENTIFICATIONS, TooltipSegment.FOOTER);
            case PAGE_2 -> List.of(TooltipSegment.BASE_STATS, TooltipSegment.FOOTER);
            case PAGE_3 -> List.of(TooltipSegment.PAGE_TEXT);
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
        return switch (segment) {
            case BASE_STATS -> new ArrayList<>(header);
            case IDENTIFICATIONS -> new ArrayList<>(identificationLines);
            case FOOTER -> new ArrayList<>(footer);
            case WEIGHTINGS, REQUIREMENTS, EXTRA_INFO, PAGE_TEXT -> List.of();
        };
    }

    @Override
    protected List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator) {
        return CraftedTooltipIdentifications.buildTooltip(craftedItem, currentClass, style);
    }
}
