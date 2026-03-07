/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable;

import com.wynntils.handlers.tooltip.type.TooltipStyle;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Represents a tooltip component generator that can be used in {@link IdentifiableTooltipBuilder}
 * @param <T> The type of the gear info
 * @param <U> The type of the gear instance
 */
public abstract class IdentifiableTooltipComponent<T, U> {
    public static final int DEFAULT_TOOLTIP_WIDTH = 150;

    public int getMaximumWidth(
            T itemInfo,
            U itemInstance,
            TooltipStyle style,
            List<Component> identificationLines,
            int defaultMaximumWidth) {
        return defaultMaximumWidth;
    }

    public abstract List<Component> buildBaseStatsTooltip(
            T itemInfo, U itemInstance, boolean hideUnidentified, int maximumWidth);

    public abstract List<Component> buildRequirementsTooltip(
            T itemInfo, U itemInstance, boolean hideUnidentified, int maximumWidth);

    public abstract List<Component> buildExtraInfoTooltip(
            T itemInfo, U itemInstance, boolean hideUnidentified, int maximumWidth);

    public abstract List<Component> buildFooterTooltip(T itemInfo, U itemInstance, boolean showItemType, int maximumWidth);

    protected MutableComponent buildRequirementLine(String requirementName, boolean fulfilled) {
        MutableComponent requirement;

        requirement = fulfilled
                ? Component.literal("✔ ").withStyle(ChatFormatting.GREEN)
                : Component.literal("✖ ").withStyle(ChatFormatting.RED);
        requirement.append(Component.literal(requirementName).withStyle(ChatFormatting.GRAY));
        return requirement;
    }
}
