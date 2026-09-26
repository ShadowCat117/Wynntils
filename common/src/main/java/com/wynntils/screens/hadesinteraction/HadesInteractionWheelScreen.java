/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.hadesinteraction;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.features.players.HadesFeature;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class HadesInteractionWheelScreen extends WynntilsScreen {
    private final HadesFeature hadesFeature;

    private HadesInteractionWheelScreen(HadesFeature hadesFeature) {
        super(Component.literal("Hades Interaction Wheel"));
        this.hadesFeature = hadesFeature;
    }

    public static Screen create(HadesFeature hadesFeature) {
        return new HadesInteractionWheelScreen(hadesFeature);
    }

    @Override
    public void doInit() {
        rememberKeyHolds();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isKeyHeld()) {
            onClose();
            return;
        }
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(GuiGraphics partialTick) {}

    @Override
    public boolean keyPressed(KeyEvent event) {
        //        int emoteNum = -1;
        //
        //        if (event.key() >= InputConstants.KEY_1 && event.key() <= InputConstants.KEY_9) {
        //            emoteNum = event.key() - InputConstants.KEY_1;
        //        } else if (event.key() == InputConstants.KEY_0) {
        //            emoteNum = 9;
        //        }
        //
        //        if (emoteNum != -1 && emoteNum < numOfEmotes) {
        //            executeEmote(emoteNum);
        //        }

        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, true);

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        //        if (event.key()
        //                == emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping().key.getValue()) {
        //            executeEmote(hoveredEmoji);
        //            onClose();
        //        }

        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, false);

        return false;
    }

    private boolean isKeyHeld() {
        Window window = McUtils.mc().getWindow();
        InputConstants.Key key = hadesFeature.openInteractionWheelKeybind.getKeyMapping().key;
        return (key.getType() == InputConstants.Type.MOUSE)
                ? GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == InputConstants.PRESS
                : InputConstants.isKeyDown(window, key.getValue());
    }
}
