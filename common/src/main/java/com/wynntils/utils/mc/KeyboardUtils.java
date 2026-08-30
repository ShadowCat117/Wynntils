/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import java.nio.ByteBuffer;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLScancode;

public final class KeyboardUtils {
    public static boolean isKeyDown(int keyCode) {
        ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
        return keyboardState != null && keyboardState.get(keyCode) != 0;
    }

    public static boolean isShiftDown() {
        return isKeyDown(SDLScancode.SDL_SCANCODE_LSHIFT) || isKeyDown(SDLScancode.SDL_SCANCODE_RSHIFT);
    }

    public static boolean isControlDown() {
        return isKeyDown(SDLScancode.SDL_SCANCODE_LCTRL) || isKeyDown(SDLScancode.SDL_SCANCODE_RCTRL);
    }

    public static boolean isAltDown() {
        return isKeyDown(SDLScancode.SDL_SCANCODE_LALT) || isKeyDown(SDLScancode.SDL_SCANCODE_RALT);
    }
}
