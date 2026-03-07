/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.rewards.type.TomeRequirements;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TomeTooltipComponent extends IdentifiableTooltipComponent<TomeInfo, TomeInstance> {
    @Override
    public List<Component> buildBaseStatsTooltip(
            TomeInfo tomeInfo, TomeInstance tomeInstance, boolean hideUnidentified, int maximumWidth) {
        List<Component> baseStats = new ArrayList<>();

        String prefix = tomeInstance == null && !hideUnidentified ? "Unidentified " : "";
        baseStats.add(Component.literal(prefix + tomeInfo.name())
                .withStyle(tomeInfo.tier().getChatFormatting()));
        baseStats.add(Component.empty());

        return baseStats;
    }

    @Override
    public List<Component> buildRequirementsTooltip(
            TomeInfo tomeInfo, TomeInstance tomeInstance, boolean hideUnidentified, int maximumWidth) {
        List<Component> requirementsLines = new ArrayList<>();

        TomeRequirements requirements = tomeInfo.requirements();
        int level = requirements.level();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            requirementsLines.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            requirementsLines.add(Component.empty());
        }

        return requirementsLines;
    }

    @Override
    public List<Component> buildExtraInfoTooltip(
            TomeInfo tomeInfo, TomeInstance tomeInstance, boolean hideUnidentified, int maximumWidth) {
        return List.of();
    }

    @Override
    public List<Component> buildFooterTooltip(
            TomeInfo tomeInfo, TomeInstance tomeInstance, boolean showItemType, int maximumWidth) {
        List<Component> footer = new ArrayList<>();

        footer.add(Component.empty());

        GearTier gearTier = tomeInfo.tier();
        MutableComponent itemTypeName = showItemType ? Component.literal("Tome") : Component.literal("Raid Reward");
        MutableComponent tier = Component.literal(gearTier.getName())
                .withStyle(gearTier.getChatFormatting())
                .append(" ")
                .append(itemTypeName);
        if (tomeInstance != null && tomeInstance.rerolls() > 1) {
            tier.append(" [" + tomeInstance.rerolls() + "]");
        }
        footer.add(tier);

        if (tomeInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            footer.add(Component.literal(StringUtils.capitalizeFirst(
                            tomeInfo.metaInfo().restrictions().getDescription()))
                    .withStyle(ChatFormatting.RED));
        }

        return footer;
    }
}
