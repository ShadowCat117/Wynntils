/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.activities.quests.QuestInfo;
import com.wynntils.models.activities.type.ActivityStatus;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class GearTooltipComponent extends IdentifiableTooltipComponent<GearInfo, GearInstance> {
    private static final Style SPACING_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("space")))
            .withoutShadow();

    private static final Style RESTRICTION_STYLE = Style.EMPTY
            .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/restriction")))
            .withoutShadow();

    private static final FontDescription WYNNCRAFT_LANGUAGE_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("language/wynncraft"));

    private static final Component DIVIDER = Component.literal("\uE000").withStyle(Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/divider"))));

    private static final Integer ELEMENTAL_DEFENSES_WIDTH = 40;

    private List<Component> buildStructuredTopTooltip(
            GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();

        header.add(Component.empty());

        // name
        MutableComponent nameLine = Component.empty().withStyle(ChatFormatting.WHITE);

        // spacing
        nameLine.append(Component.literal("\uDAFF\uDFF0").withStyle(SPACING_STYLE));

        String frameCode = gearInfo.type().getFrameCode();
        String spriteCode = gearInfo.type().getFrameSpriteCode();
        String setName = gearInfo.setInfo().map(SetInfo::name).orElse("");

        if (!setName.isEmpty()) {
            frameCode = String.valueOf((char) (frameCode.charAt(0) + 0x1000));
            spriteCode = String.valueOf((char) (spriteCode.charAt(0) + 0x1000));
        }

        // frame (hardcoded to weapon rn)
        nameLine.append(Component.literal(frameCode)
                .withStyle(Style.EMPTY
                        .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/frame")))
                        .withoutShadow()));

        // spacing
        nameLine.append(Component.literal("\uDAFF\uDFCF").withStyle(SPACING_STYLE));

        // frame sprite
        nameLine.append(Component.literal(spriteCode)
                .withStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(Identifier.withDefaultNamespace("tooltip/emblem/sprite"))))
                .withColor(0x00eb1c)
                .withoutShadow());

        // spacing
        nameLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

        boolean isShiny = gearInstance != null && gearInstance.shinyStat().isPresent();
        String itemName = isShiny ? "Shiny " + gearInfo.name() : gearInfo.name();

        // item name
        nameLine.append(Component.literal(itemName)
                .withStyle(Style.EMPTY
                        .withFont(WYNNCRAFT_LANGUAGE_FONT)
                        .withColor(gearInfo.tier().getChatFormatting())));

        header.add(nameLine);

        MutableComponent rarityTypeLine = Component.empty().withStyle(ChatFormatting.WHITE);

        rarityTypeLine.append(Component.literal("\uDB00\uDC23").withStyle(SPACING_STYLE));

        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.tier().getName(),
                CustomColor.fromChatFormatting(gearInfo.tier().getChatFormatting()),
                CommonColors.BLACK,
                "\uDB00\uDC02"));

        rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(SPACING_STYLE));

        boolean untradable = gearInfo.metaInfo().restrictions() == GearRestrictions.UNTRADABLE;
        CustomColor secondaryTierColor = getSecondaryTierColor(gearInfo.tier());

        rarityTypeLine.append(BannerBoxFont.buildMessage(
                gearInfo.type().name(), secondaryTierColor, CommonColors.BLACK, untradable ? "\uDB00\uDC02" : ""));

        if (untradable) {
            rarityTypeLine.append(Component.literal("\uDB00\uDC01").withStyle(SPACING_STYLE));

            rarityTypeLine.append(
                    Component.literal("\uE002").withStyle(RESTRICTION_STYLE).withColor(0xff4242));
            rarityTypeLine.append(Component.literal("\uDAFF\uDFF6\uF002").withStyle(RESTRICTION_STYLE));
        }

        header.add(rarityTypeLine);

        if (!setName.isBlank()) {
            MutableComponent setBonusLine = Component.empty().withColor(secondaryTierColor.asInt());

            setBonusLine.append(Component.literal("\uDB00\uDC23").withStyle(SPACING_STYLE));

            setBonusLine.append(
                    BannerBoxFont.buildMessage(setName + " set", secondaryTierColor, CommonColors.BLACK, ""));

            header.add(setBonusLine);
        }

        header.add(Component.empty());

        // Health for armor & accessories
        if (gearInfo.type().isArmor() || gearInfo.type().isAccessory()) {
            MutableComponent healthLine = Component.empty().withStyle(ChatFormatting.WHITE);

            healthLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            healthLine.append(Component.literal(StringUtils.toSignedCommaString(
                            gearInfo.fixedStats().healthBuff()))
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                            .withColor(secondaryTierColor.asInt())));

            healthLine.append(Component.literal(" Health").withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT)));

            header.add(healthLine);
        } else if (gearInfo.type().isWeapon()) { // DPS for weapons
            MutableComponent dpsLine = Component.empty().withStyle(ChatFormatting.WHITE);

            dpsLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            dpsLine.append(
                    Component.literal(String.format("%,d", gearInfo.fixedStats().averageDps()))
                            .withStyle(Style.EMPTY
                                    .withFont(new FontDescription.Resource(
                                            Identifier.withDefaultNamespace("offset/wynncraft_quad/12")))
                                    .withColor(secondaryTierColor.asInt())));

            dpsLine.append(Component.literal(" DPS").withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT)));

            header.add(dpsLine);
        }

        // attack speed
        if (gearInfo.fixedStats().attackSpeed().isPresent()) {
            MutableComponent attackSpeedLine = Component.empty().withStyle(ChatFormatting.WHITE);

            attackSpeedLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));

            attackSpeedLine.append(Component.literal("\uE007")
                    .withStyle(Style.EMPTY
                            .withFont(new FontDescription.Resource(
                                    Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                            .withoutShadow()));

            attackSpeedLine.append(Component.literal(
                            " " + gearInfo.fixedStats().attackSpeed().get().getName() + " ")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

            attackSpeedLine.append(Component.literal(
                            "(" + gearInfo.fixedStats().attackSpeed().get().getHitsPerSecond() + " hits/s)")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.DARK_GRAY)));

            header.add(attackSpeedLine);
        }

        List<Pair<Element, Integer>> defenses = gearInfo.fixedStats().defences();

        if (!defenses.isEmpty()) {
            MutableComponent defensesHeader = Component.empty().withStyle(ChatFormatting.WHITE);

            defensesHeader.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
            defensesHeader.append(Component.literal("Elemental Defences")
                    .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));
            header.add(defensesHeader);

            MutableComponent defenseLine = Component.empty().withStyle(ChatFormatting.WHITE);

            for (int i = 0; i < defenses.size(); i++) {
                if (i == 0 || i == 3) {
                    defenseLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
                }

                Pair<Element, Integer> defenseStat = defenses.get(i);

                MutableComponent elementComponent = Component.empty();
                String elementSymbol = defenseStat.a() == Element.EARTH
                        ? "\uDAFF\uDFFF" + defenseStat.a().getTooltipSprite()
                        : defenseStat.a().getTooltipSprite();

                if (defenseStat.a() == Element.EARTH
                        || defenseStat.a() == Element.THUNDER
                        || defenseStat.a() == Element.AIR) {
                    elementSymbol += "\uDAFF\uDFFF";
                }

                elementComponent.append(Component.literal(elementSymbol)
                        .withStyle(Style.EMPTY
                                .withFont(new FontDescription.Resource(
                                        Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                                .withColor(ChatFormatting.WHITE))
                        .withoutShadow());
                elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                elementComponent.append(Component.literal(StringUtils.toSignedCommaString(defenseStat.b()))
                        .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

                // Doesn't match vanilla perfectly but is close enough
                int width = McUtils.mc().font.width(elementComponent);
                if (width < ELEMENTAL_DEFENSES_WIDTH) {
                    String offset = Managers.Font.calculateOffset(width, ELEMENTAL_DEFENSES_WIDTH);
                    elementComponent.append(
                            Component.literal(offset).withStyle(SPACING_STYLE).withStyle(ChatFormatting.GRAY));
                }

                boolean lastInRow = i == 2 || i == defenses.size() - 1;
                if (!lastInRow) {
                    elementComponent.append(Component.literal(" ")
                            .withStyle(Style.EMPTY
                                    .withFont(WYNNCRAFT_LANGUAGE_FONT)
                                    .withColor(ChatFormatting.GRAY)));
                }

                defenseLine.append(elementComponent);

                // 3 elements per line max
                if (i == defenses.size() - 1 || i == 2) {
                    header.add(defenseLine);
                    defenseLine = Component.empty().withStyle(ChatFormatting.WHITE);
                }
            }
        }

        // elemental damages
        List<Pair<DamageType, RangedValue>> damages = gearInfo.fixedStats().damages();

        if (!damages.isEmpty()) {
            MutableComponent damageLine = Component.empty().withStyle(ChatFormatting.WHITE);

            for (int i = 0; i < damages.size(); i++) {
                if (i == 0 || i == 3) {
                    damageLine.append(Component.literal("\uDB00\uDC02").withStyle(SPACING_STYLE));
                }

                Pair<DamageType, RangedValue> damageStat = damages.get(i);
                Element element = damageStat.a().getElement().orElse(null);

                MutableComponent elementComponent = Component.empty();
                String elementSymbol = element == Element.EARTH || damageStat.a() == DamageType.NEUTRAL
                        ? "\uDAFF\uDFFF" + damageStat.a().getTooltipSprite()
                        : damageStat.a().getTooltipSprite();

                if (damageStat.a() == DamageType.NEUTRAL
                        || element == Element.EARTH
                        || element == Element.THUNDER
                        || element == Element.AIR) {
                    elementSymbol += "\uDAFF\uDFFF";
                }

                elementComponent.append(Component.literal(elementSymbol)
                        .withStyle(Style.EMPTY
                                .withFont(new FontDescription.Resource(
                                        Identifier.withDefaultNamespace("tooltip/attribute/sprite")))
                                .withColor(ChatFormatting.WHITE))
                        .withoutShadow());
                elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                elementComponent.append(Component.literal(damageStat.b().asString())
                        .withStyle(Style.EMPTY.withFont(WYNNCRAFT_LANGUAGE_FONT).withColor(ChatFormatting.GRAY)));

                boolean lastInRow = i == 2 || i == damages.size() - 1;
                if (!lastInRow) {
                    elementComponent.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                }

                damageLine.append(elementComponent);

                // 3 elements per line max
                if (i == damages.size() - 1 || i == 2) {
                    header.add(damageLine);
                    damageLine = Component.empty().withStyle(ChatFormatting.WHITE);
                }
            }
        }



        // requirements
        int requirementsCount = 0;
        GearRequirements requirements = gearInfo.requirements();
        if (requirements.classType().isPresent()) {
            ClassType classType = requirements.classType().get();
            boolean fulfilled = Models.Character.getClassType() == classType;
            header.add(buildRequirementLine("Class Req: " + classType.getFullName(), fulfilled));
            requirementsCount++;
        }
        if (requirements.quest().isPresent()) {
            String questName = requirements.quest().get();
            Optional<QuestInfo> quest = Models.Quest.getQuestFromName(questName);
            boolean fulfilled = quest.isPresent() && quest.get().status() == ActivityStatus.COMPLETED;
            header.add(buildRequirementLine("Quest Req: " + questName, fulfilled));
            requirementsCount++;
        }
        int level = requirements.level();
        if (level != 0) {
            boolean fulfilled = Models.CombatXp.getCombatLevel().current() >= level;
            header.add(buildRequirementLine("Combat Lv. Min: " + level, fulfilled));
            requirementsCount++;
        }
        if (!requirements.skills().isEmpty()) {
            for (Pair<Skill, Integer> skillRequirement : requirements.skills()) {
                // FIXME: CharacterModel is still missing info about our skill points
                header.add(buildRequirementLine(
                        skillRequirement.key().getDisplayName() + " Min: " + skillRequirement.value(), false));
                requirementsCount++;
            }
        }
        if (requirementsCount > 0) {
            header.add(Component.literal(""));
        }

        if (gearInstance != null && gearInstance.shinyStat().isPresent()) {
            ShinyStat shinyStat = gearInstance.shinyStat().get();
            if (shinyStat.shinyRerolls() == 0) {
                header.add(Component.literal("⬡ " + shinyStat.statType().displayName() + ": ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(shinyStat.value()))
                                .withStyle(ChatFormatting.WHITE)));
            } else {
                header.add(Component.literal("⬡ " + shinyStat.statType().displayName() + ": ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(shinyStat.value()))
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" [" + shinyStat.shinyRerolls() + "]")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }

            header.add(Component.literal(""));
        }

        return header;
    }

    @Override
    public List<Component> buildBaseStatsTooltip(
            GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified, int maximumWidth) {
        List<Component> header = buildStructuredTopTooltip(gearInfo, gearInstance, hideUnidentified);
        List<Component> baseStats = new ArrayList<>();

        for (Component line : header) {
            String plain = line.getString();
            if (plain.startsWith("✔ ") || plain.startsWith("✖ ")) {
                break;
            }
            baseStats.add(line);
        }

        return baseStats;
    }

    @Override
    public List<Component> buildRequirementsTooltip(
            GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified, int maximumWidth) {
        List<Component> header = buildStructuredTopTooltip(gearInfo, gearInstance, hideUnidentified);
        List<Component> requirements = new ArrayList<>();
        boolean requirementsStarted = false;

        for (Component line : header) {
            String plain = line.getString();
            if (plain.startsWith("✔ ") || plain.startsWith("✖ ")) {
                requirementsStarted = true;
            }

            if (!requirementsStarted) {
                continue;
            }

            if (plain.startsWith("⬡ ")) {
                break;
            }

            requirements.add(line);
        }

        return requirements;
    }

    @Override
    public List<Component> buildExtraInfoTooltip(
            GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified, int maximumWidth) {
        List<Component> header = buildStructuredTopTooltip(gearInfo, gearInstance, hideUnidentified);
        List<Component> extraInfo = new ArrayList<>();

        for (Component line : header) {
            if (line.getString().startsWith("⬡ ")) {
                extraInfo.add(line);
            }
        }

        return extraInfo;
    }

    @Override
    public List<Component> buildFooterTooltip(
            GearInfo gearInfo, GearInstance gearInstance, boolean showItemType, int maximumWidth) {
        List<Component> footer = new ArrayList<>();

        // major ids
        if (gearInfo.fixedStats().majorIds().isPresent()) {
            GearMajorId majorId = gearInfo.fixedStats().majorIds().get();

            // The majorId lore contains the name, and colors
            // This dance to and from component is needed to properly recolor all neutral text
            StyledText lore = StyledText.fromComponent(Component.empty()
                    .withStyle(ChatFormatting.DARK_AQUA)
                    .append(majorId.lore().getComponent()));

            Stream.of(RenderedStringUtils.wrapTextBySize(lore, maximumWidth))
                    .forEach(c -> footer.add(c.getComponent()));
        }

        footer.add(Component.literal(""));

        // powder slots
        if (gearInfo.powderSlots() > 0) {
            if (gearInstance == null) {
                footer.add(Component.literal("[" + gearInfo.powderSlots() + " Powder Slots]")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                MutableComponent powderLine = Component.literal(
                                "[" + gearInstance.powders().size() + "/" + gearInfo.powderSlots() + "] Powder Slots ")
                        .withStyle(ChatFormatting.GRAY);
                if (!gearInstance.powders().isEmpty()) {
                    MutableComponent powderList = Component.literal("[");
                    for (Powder p : gearInstance.powders()) {
                        String symbol = String.valueOf(p.getSymbol());
                        if (!powderList.getSiblings().isEmpty()) {
                            powderList.append(Component.empty()
                                    .withStyle(Style.EMPTY.withColor(p.getLightColor()))
                                    .append(Component.literal(" "))
                                    .append(Component.literal(symbol)
                                            .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(
                                                    Identifier.withDefaultNamespace("common"))))));
                            continue;
                        }
                        powderList.append(Component.literal(symbol)
                                .withStyle(Style.EMPTY
                                        .withFont(
                                                new FontDescription.Resource(Identifier.withDefaultNamespace("common")))
                                        .withColor(p.getLightColor())));
                    }
                    powderList.append(Component.literal("]"));
                    powderLine.append(powderList);
                }
                footer.add(powderLine);
            }
        }

        // tier & rerolls
        GearTier gearTier = gearInfo.tier();
        MutableComponent itemTypeName = showItemType
                ? Component.literal(
                        StringUtils.capitalizeFirst(gearInfo.type().name().toLowerCase(Locale.ROOT)))
                : Component.literal("Item");
        MutableComponent tier = Component.literal(gearTier.getName())
                .withStyle(gearTier.getChatFormatting())
                .append(" ")
                .append(itemTypeName);
        if (gearInstance != null && gearInstance.rerolls() > 1) {
            tier.append(" [" + gearInstance.rerolls() + "]");
        }
        footer.add(tier);

        // restrictions (untradable, quest item)
        if (gearInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            footer.add(Component.literal(StringUtils.capitalizeFirst(
                            gearInfo.metaInfo().restrictions().getDescription()))
                    .withStyle(ChatFormatting.RED));
        }

        // lore
        Optional<StyledText> lore = gearInfo.metaInfo().lore();
        if (lore.isPresent()) {
            Stream.of(RenderedStringUtils.wrapTextBySize(lore.get(), maximumWidth))
                    .forEach(c -> footer.add(c.getComponent().withStyle(ChatFormatting.DARK_GRAY)));
        }

        return footer;
    }

    private static CustomColor getSecondaryTierColor(GearTier gearTier) {
        return switch (gearTier) {
            case NORMAL -> CustomColor.fromInt(0xe0e0e0);
            case UNIQUE -> CustomColor.fromInt(0xfff2b3);
            case RARE -> CustomColor.fromInt(0xf2c2f2);
            case LEGENDARY -> CustomColor.fromInt(0xcff9f9);
            case FABLED -> CustomColor.fromInt(0xf2c2c2);
            case MYTHIC -> CustomColor.fromInt(0xe0b3e6);
            // Crafteds shouldn't be used here
            default -> CustomColor.NONE;
        };
    }
}
