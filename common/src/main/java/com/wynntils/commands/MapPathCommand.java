/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.screens.maps.paths.PathCreationScreen;
import com.wynntils.screens.maps.paths.PathSelectionScreen;
import com.wynntils.services.mapdata.MapDataService;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.utils.mc.McUtils;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MapPathCommand {
    @FunctionalInterface
    private interface Action {
        void run(CommandContext<CommandSourceStack> context) throws Exception;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> named(String literal, Action action) {
        return Commands.literal(literal)
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Services.MapPaths.getStoredPaths().stream()
                                        .map(path -> StringArgumentType.escapeIfRequired(path.label())),
                                builder))
                        .executes(context -> execute(context, action)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("path").executes(context -> open(false));
        root.then(Commands.literal("screen").executes(context -> open(false)));
        root.then(Commands.literal("browse").executes(context -> open(true)));
        root.then(Commands.literal("list")
                .executes(context -> execute(context, c -> {
                    Services.MapPaths.getStoredPaths()
                            .forEach(path -> c.getSource().sendSuccess(() -> Component.literal(path.label()), false));
                })));
        root.then(Commands.literal("record")
                .executes(context -> execute(
                        context,
                        c -> {
                            if (Services.MapPaths.isRecording()) Services.MapPaths.pauseRecording();
                            else {
                                if (!Services.MapPaths.hasDraft())
                                    Services.MapPaths.configure(
                                            PathCategoriesProvider.PATH_CATEGORY,
                                            "",
                                            new MapAttributesBuilder()
                                                    .asPathAttributes()
                                                    .build(),
                                            true);
                                Services.MapPaths.startRecording();
                            }
                        },
                        () -> Component.translatable(
                                Services.MapPaths.isRecording()
                                        ? "command.wynntils.map.path.recordingStarted"
                                        : "command.wynntils.map.path.recordingPaused"))));
        root.then(Commands.literal("pause")
                .executes(context -> execute(context, c -> Services.MapPaths.pauseRecording())));
        root.then(Commands.literal("cancel").executes(context -> execute(context, c -> Services.MapPaths.cancel())));
        root.then(Commands.literal("clear").executes(context -> execute(context, c -> Services.MapPaths.clear())));
        root.then(Commands.literal("undo")
                .executes(context -> execute(context, c -> {
                    if (!Services.MapPaths.undo())
                        throw new IllegalStateException("Return near an earlier point in the recording before undoing");
                })));
        root.then(Commands.literal("save")
                .then(Commands.argument("label", StringArgumentType.string())
                        .executes(context -> execute(
                                context, c -> Services.MapPaths.save(StringArgumentType.getString(c, "label"))))));
        root.then(named(
                "load", c -> Services.MapPaths.load(Services.MapPaths.find(StringArgumentType.getString(c, "name")))));
        root.then(named("edit", c -> {
            Services.MapPaths.edit(Services.MapPaths.find(StringArgumentType.getString(c, "name")));
            open(false);
        }));
        root.then(named(
                "delete",
                c -> Services.MapPaths.delete(Services.MapPaths.find(StringArgumentType.getString(c, "name")))));
        root.then(Commands.literal("rename")
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("label", StringArgumentType.string())
                                .executes(context -> execute(
                                        context,
                                        c -> Services.MapPaths.rename(
                                                Services.MapPaths.find(StringArgumentType.getString(c, "name")),
                                                StringArgumentType.getString(c, "label")))))));
        root.then(Commands.literal("highlight")
                .then(Commands.literal("add")
                        .executes(context -> execute(context, c -> addHighlight(defaultHighlight())))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context ->
                                        execute(context, c -> addHighlight(BlockPosArgument.getBlockPos(c, "pos"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> execute(context, c -> {
                                    if (!Services.MapPaths.removeHighlight(BlockPosArgument.getBlockPos(c, "pos")))
                                        throw new IllegalArgumentException("Highlight not found");
                                })))));
        LiteralArgumentBuilder<CommandSourceStack> notes = Commands.literal("note");
        LiteralArgumentBuilder<CommandSourceStack> addNote = Commands.literal("add");
        addNote.then(Commands.literal("text")
                .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(context -> execute(
                                context, c -> addNote(Component.literal(StringArgumentType.getString(c, "text")))))));
        addNote.then(Commands.literal("json")
                .then(Commands.argument("text", ComponentArgument.textComponent(buildContext))
                        .executes(context ->
                                execute(context, c -> addNote(ComponentArgument.getRawComponent(c, "text"))))));
        notes.then(addNote);
        notes.then(Commands.literal("list")
                .executes(context -> execute(
                        context,
                        c -> Services.MapPaths.getNotes()
                                .forEach(note -> c.getSource()
                                        .sendSuccess(
                                                () -> Component.literal(
                                                                BlockPos.containing(note.position())
                                                                                .toShortString() + ": ")
                                                        .append(note.component()),
                                                false)))));
        notes.then(Commands.literal("delete")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> execute(context, c -> {
                            if (!Services.MapPaths.deleteNote(BlockPosArgument.getBlockPos(c, "pos")))
                                throw new IllegalArgumentException("Note not found");
                        }))));
        root.then(notes);
        root.then(Commands.literal("folder")
                .executes(
                        context -> execute(context, c -> Util.getPlatform().openFile(MapDataService.LOCAL_PROVIDERS))));
        return root;
    }

    private static void addNote(Component text) {
        if (!Services.MapPaths.addNote(text))
            throw new IllegalStateException("Start recording or edit a path before adding notes");
    }

    private static void addHighlight(BlockPos pos) {
        if (!Services.MapPaths.addHighlight(Vec3.atLowerCornerOf(pos)))
            throw new IllegalStateException("Start recording or edit a path before adding highlights");
    }

    private static BlockPos defaultHighlight() {
        return McUtils.mc().hitResult instanceof BlockHitResult hit
                ? hit.getBlockPos()
                : McUtils.player().blockPosition();
    }

    private static int open(boolean browse) {
        Managers.TickScheduler.scheduleLater(
                () -> McUtils.setScreen(browse ? new PathSelectionScreen(null) : PathCreationScreen.create(null)), 2);
        return 1;
    }

    private static int execute(CommandContext<CommandSourceStack> context, Action action) {
        return execute(context, action, () -> Component.translatable("command.wynntils.map.path.success"));
    }

    private static int execute(CommandContext<CommandSourceStack> context, Action action, Supplier<Component> success) {
        try {
            action.run(context);
            context.getSource().sendSuccess(success, false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("command.wynntils.map.path.error", e.getMessage()));
            return 0;
        }
    }
}
