/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipPage;
import com.wynntils.handlers.tooltip.type.TooltipSegment;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.handlers.tooltip.type.TooltipWeightDecorator;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.ItemWeightSource;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class TooltipBuilder {
    private static final TooltipStyle DEFAULT_TOOLTIP_STYLE =
            new TooltipStyle(StatListOrdering.WYNNCRAFT, false, false, true, true);
    private final String source;

    // The identification cache is only valid if the cached dependencies match
    private ClassType cachedCurrentClass;
    private TooltipStyle cachedStyle;
    private TooltipIdentificationDecorator cachedIdentificationDecorator;
    private List<Component> identificationsCache;

    private final Map<PageKey, List<Component>> tooltipCache = new LinkedHashMap<>();

    protected TooltipBuilder(String source) {
        this.source = source;
    }

    public List<Component> getTooltipLines(ClassType currentClass) {
        return getTooltipLines(currentClass, DEFAULT_TOOLTIP_STYLE, null, ItemWeightSource.NONE, null, TooltipPage.PAGE_1);
    }

    public List<Component> getTooltipLines(
            ClassType currentClass,
            TooltipStyle style,
            TooltipIdentificationDecorator identificationDecorator,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        return getTooltipLines(
                currentClass, style, identificationDecorator, weightSource, weightDecorator, TooltipPage.PAGE_1);
    }

    public List<Component> getTooltipLines(
            ClassType currentClass,
            TooltipStyle style,
            TooltipIdentificationDecorator identificationDecorator,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator,
            TooltipPage page) {
        refreshIdentificationCache(currentClass, style, identificationDecorator);

        int maximumWidth = getMaximumWidth(page, style, identificationsCache, weightSource, weightDecorator);
        PageKey cacheKey = new PageKey(page, maximumWidth, weightSource, style, weightDecorator);

        List<Component> cachedTooltip = tooltipCache.get(cacheKey);
        if (cachedTooltip != null) {
            return new ArrayList<>(cachedTooltip);
        }

        List<Component> tooltip = new ArrayList<>();
        for (TooltipSegment segment : getSegmentOrder(page)) {
            tooltip.addAll(getSegmentLines(segment, page, maximumWidth, style, identificationsCache, weightSource, weightDecorator));
        }

        if (!source.isEmpty()) {
            tooltip.add(
                    1,
                    Component.literal(source)
                            .withStyle(ChatFormatting.DARK_GRAY)
                            .withStyle(ChatFormatting.ITALIC));
        }

        tooltipCache.put(cacheKey, List.copyOf(tooltip));

        return tooltip;
    }

    protected int getMaximumWidth(
            TooltipPage page,
            TooltipStyle style,
            List<Component> identificationLines,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator) {
        return 150;
    }

    protected static int getMaximumComponentWidth(List<Component> lines) {
        return lines.stream()
                .mapToInt(component -> FontRenderer.getInstance().getFont().width(component))
                .max()
                .orElse(0);
    }

    protected abstract List<TooltipSegment> getSegmentOrder(TooltipPage page);

    protected static List<TooltipSegment> insertSegmentAfter(
            List<TooltipSegment> segments, TooltipSegment anchor, TooltipSegment segmentToInsert) {
        List<TooltipSegment> updatedSegments = new ArrayList<>(segments);
        int anchorIndex = updatedSegments.indexOf(anchor);
        if (anchorIndex == -1) {
            updatedSegments.add(segmentToInsert);
            return updatedSegments;
        }

        updatedSegments.add(anchorIndex + 1, segmentToInsert);
        return updatedSegments;
    }

    protected static List<TooltipSegment> insertSegmentBefore(
            List<TooltipSegment> segments, TooltipSegment anchor, TooltipSegment segmentToInsert) {
        List<TooltipSegment> updatedSegments = new ArrayList<>(segments);
        int anchorIndex = updatedSegments.indexOf(anchor);
        if (anchorIndex == -1) {
            updatedSegments.add(0, segmentToInsert);
            return updatedSegments;
        }

        updatedSegments.add(anchorIndex, segmentToInsert);
        return updatedSegments;
    }

    protected abstract List<Component> getSegmentLines(
            TooltipSegment segment,
            TooltipPage page,
            int maximumWidth,
            TooltipStyle style,
            List<Component> identificationLines,
            ItemWeightSource weightSource,
            TooltipWeightDecorator weightDecorator);

    protected abstract List<Component> getIdentificationLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator decorator);

    protected static Pair<List<Component>, List<Component>> extractHeaderAndFooter(List<Component> lore) {
        List<Component> header = new ArrayList<>();
        List<Component> footer = new ArrayList<>();

        boolean headerEnded = false;
        boolean footerStarted = false;
        boolean skillPointsStarted = false;

        boolean foundSkills = false;
        boolean foundIdentifications = false;
        for (Component loreLine : lore) {
            StyledText codedLine = StyledText.fromComponent(loreLine).getNormalized();

            if (!footerStarted) {
                if (codedLine.matches(WynnItemParser.SET_BONUS_PATTERN)) {
                    headerEnded = true;
                    footerStarted = true;
                } else {
                    Matcher matcher = codedLine.getMatcher(WynnItemParser.IDENTIFICATION_STAT_PATTERN);
                    if (matcher.matches()) {
                        // Some orders do not have a blank line after a skill point line,
                        // so reset the flag here
                        skillPointsStarted = false;

                        String statName = matcher.group("statName");

                        if (Skill.isSkill(statName)) {
                            skillPointsStarted = true;
                            foundSkills = true;
                            // Skill points are in a separate section to the rest of the identifications,
                            // but we still don't want to keep them
                        } else {
                            foundIdentifications = true;
                            // Don't keep identifications lines at all
                        }

                        headerEnded = true;
                        continue;
                    } else if (skillPointsStarted) {
                        // If there were skill points, there might be a blank line after them
                        skillPointsStarted = false;
                        continue;
                    }
                }
            }

            // We want to keep this line, so figure out where to put it
            if (!headerEnded) {
                header.add(loreLine);
            } else {
                // From now on, we can skip looking for identification lines
                footerStarted = true;
                footer.add(loreLine);
            }
        }

        if (foundSkills && !foundIdentifications) {
            // If there were skills but no identifications,
            // then the footer is missing a blank line
            footer.addFirst(Component.literal(""));
        }

        return Pair.of(header, footer);
    }

    private void refreshIdentificationCache(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator identificationDecorator) {
        if (currentClass == cachedCurrentClass
                && Objects.equals(cachedStyle, style)
                && Objects.equals(cachedIdentificationDecorator, identificationDecorator)) {
            return;
        }

        identificationsCache = getIdentificationLines(currentClass, style, identificationDecorator);
        cachedCurrentClass = currentClass;
        cachedStyle = style;
        cachedIdentificationDecorator = identificationDecorator;
        tooltipCache.clear();
    }

    protected void invalidateTooltipCache() {
        tooltipCache.clear();
    }

    private record PageKey(
            TooltipPage page,
            int maximumWidth,
            ItemWeightSource weightSource,
            TooltipStyle style,
            TooltipWeightDecorator weightDecorator) {}
}
