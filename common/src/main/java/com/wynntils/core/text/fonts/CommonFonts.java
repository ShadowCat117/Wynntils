/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class CommonFonts {
    public static final FontDescription LANGUAGE_WYNNCRAFT_FONT = font("language/wynncraft");
    public static final FontDescription PROFESSION_FONT = font("profession");
    public static final FontDescription SPACE_FONT = font("space");

    private static FontDescription.Resource font(String path) {
        return new FontDescription.Resource(Identifier.withDefaultNamespace(path));
    }
}
