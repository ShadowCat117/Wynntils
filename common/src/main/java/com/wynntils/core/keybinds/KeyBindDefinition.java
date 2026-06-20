/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Managers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public record KeyBindDefinition(
        String id, KeyMapping.Category category, InputConstants.Type type, int defaultKey, boolean firstPress) {
    private static final List<KeyBindDefinition> DEFINITIONS = new ArrayList<>();

    public static List<KeyBindDefinition> definitions() {
        return Collections.unmodifiableList(DEFINITIONS);
    }

    public KeyBind create(Runnable onPress) {
        return Managers.KeyBind.createKeyBind(this, onPress, null);
    }

    public KeyBind create(Consumer<Slot> inventoryPress) {
        return Managers.KeyBind.createKeyBind(this, null, inventoryPress);
    }

    public KeyBind create(Runnable onPress, Consumer<Slot> inventoryPress) {
        return Managers.KeyBind.createKeyBind(this, onPress, inventoryPress);
    }

    public String translationKey() {
        return "wynntils.keybind." + id;
    }

    public String optionsKey() {
        return "key_" + translationKey();
    }

    // region Chat
    public static final KeyBindDefinition BOMB_RELAY_PARTY = register(
            "relayBombToParty",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition BOMB_RELAY_GUILD = register(
            "relayBombToGuild",
            Managers.KeyBind.CHAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition SHARE_ITEM =
            register("shareItem", Managers.KeyBind.CHAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F5, true);

    public static final KeyBindDefinition SAVE_ITEM_TO_RECORD = register(
            "saveItemToRecord", Managers.KeyBind.CHAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, true);

    public static final KeyBindDefinition OPEN_ITEM_RECORD = register(
            "openItemRecord", Managers.KeyBind.CHAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);
    // endregion

    // region Combat
    public static final KeyBindDefinition RIDE_MOUNT =
            register("mountHorse", Managers.KeyBind.COMBAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, true);

    public static final KeyBindDefinition CAST_FIRST_SPELL = register(
            "castFirstSpell", Managers.KeyBind.COMBAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, true);

    public static final KeyBindDefinition CAST_SECOND_SPELL = register(
            "castSecondSpell", Managers.KeyBind.COMBAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, true);

    public static final KeyBindDefinition CAST_THIRD_SPELL = register(
            "castThirdSpell", Managers.KeyBind.COMBAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, true);

    public static final KeyBindDefinition CAST_FOURTH_SPELL = register(
            "castFourthSpell", Managers.KeyBind.COMBAT_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, true);

    public static final KeyBindDefinition CAST_MELEE_ATTACK = register(
            "castMeleeAttack",
            Managers.KeyBind.COMBAT_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Commands
    public static final KeyBindDefinition CUSTOM_COMMAND_ONE = register(
            "customCommandOne",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_TWO = register(
            "customCommandTwo",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_THREE = register(
            "customCommandThree",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_FOUR = register(
            "customCommandFour",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_FIVE = register(
            "customCommandFive",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition CUSTOM_COMMAND_SIX = register(
            "customCommandSix",
            Managers.KeyBind.COMMANDS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Debug
    public static final KeyBindDefinition DUMP_CONTENT_BOOK = register(
            "dumpContentBook",
            Managers.KeyBind.DEBUG_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition LOG_ITEM_INFO = register(
            "logItemInfo", Managers.KeyBind.DEBUG_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition TEXTURE_RECORDER = register(
            "textureRecorder",
            Managers.KeyBind.DEBUG_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    // region Inventory
    public static final KeyBindDefinition OPEN_EMERALD_POUCH = register(
            "openEmeraldPouch",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_GUILD_BANK = register(
            "openGuildBank", Managers.KeyBind.INVENTORY_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, true);

    public static final KeyBindDefinition OPEN_INGREDIENT_POUCH = register(
            "openIngredientPouch",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition TOGGLE_FAVORITE = register(
            "toggleFavorite",
            Managers.KeyBind.INVENTORY_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition LOCK_SLOT = register(
            "lockSlot", Managers.KeyBind.INVENTORY_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, true);

    public static final KeyBindDefinition SCREENSHOT_ITEM = register(
            "screenshotItem", Managers.KeyBind.INVENTORY_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F4, true);
    // endregion

    // region Map
    public static final KeyBindDefinition OPEN_GUILD_MAP =
            register("openGuildMap", Managers.KeyBind.MAP_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, true);

    public static final KeyBindDefinition OPEN_MAIN_MAP =
            register("openMainMap", Managers.KeyBind.MAP_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, true);

    public static final KeyBindDefinition NEW_WAYPOINT =
            register("newWaypoint", Managers.KeyBind.MAP_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, true);

    public static final KeyBindDefinition MINIMAP_ZOOM_IN = register(
            "minimapZoomIn", Managers.KeyBind.MAP_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, false);

    public static final KeyBindDefinition MINIMAP_ZOOM_OUT = register(
            "minimapZoomOut", Managers.KeyBind.MAP_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, false);
    // endregion

    // region Overlays
    public static final KeyBindDefinition TOGGLE_STOPWATCH = register(
            "toggleStopwatch",
            Managers.KeyBind.OVERLAYS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_0,
            true);

    public static final KeyBindDefinition RESET_STOPWATCH = register(
            "resetStopwatch",
            Managers.KeyBind.OVERLAYS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_DECIMAL,
            true);
    // endregion

    // region Players
    public static final KeyBindDefinition OPEN_PARTY_MANAGEMENT = register(
            "openPartyManagement",
            Managers.KeyBind.PLAYERS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            true);

    public static final KeyBindDefinition VIEW_PLAYER = register(
            "viewPlayer",
            Managers.KeyBind.PLAYERS_CATEGORY,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            true);
    // endregion

    // region Tooltips
    public static final KeyBindDefinition HOLD_TO_COMPARE = register(
            "holdToCompare",
            Managers.KeyBind.TOOLTIPS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ENTER,
            false);

    public static final KeyBindDefinition SELECT_FOR_COMPARING = register(
            "selectForComparing",
            Managers.KeyBind.TOOLTIPS_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            true);
    // endregion

    // region Trade Market
    public static final KeyBindDefinition QUICK_SEARCH_TRADE_MARKET = register(
            "quickSearchTradeMarket",
            Managers.KeyBind.TRADEMARKET_CATEGORY,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            true);
    // endregion

    // region UI
    public static final KeyBindDefinition OPEN_TERRITORY_MENU = register(
            "openTerritoryMenu", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, true);

    public static final KeyBindDefinition OPEN_CONTENT_BOOK = register(
            "openContentBook", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, true);

    public static final KeyBindDefinition OPEN_WYNNTILS_MENU = register(
            "openWynntilsMenu", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, true);

    public static final KeyBindDefinition OPEN_OVERLAY_MENU = register(
            "openOverlayMenu", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_OVERLAY_FREE_MOVE = register(
            "openOverlayFreeMove",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_POWDER_GUIDE = register(
            "openPowerGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_ITEM_GUIDE = register(
            "openItemGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_INGREDIENT_GUIDE = register(
            "openIngredientGuide",
            Managers.KeyBind.UI_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);

    public static final KeyBindDefinition OPEN_CHARM_GUIDE = register(
            "openCharmGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_TOME_GUIDE = register(
            "openTomeGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_ASPECT_GUIDE = register(
            "openAspectGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_EMERALD_GUIDE = register(
            "openEmeraldGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_MISC_GUIDE = register(
            "openMiscGuide", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);

    public static final KeyBindDefinition OPEN_GUIDES_LIST = register(
            "openGuidesList", Managers.KeyBind.UI_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, true);
    // endregion

    // region Utilities
    public static final KeyBindDefinition TOGGLE_GAMMABRIGHT = register(
            "gammabright", Managers.KeyBind.UTILITIES_CATEGORY, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, true);

    public static final KeyBindDefinition TOGGLE_SILENCER = register(
            "toggleSilencer",
            Managers.KeyBind.UTILITIES_CATEGORY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            true);
    // endregion

    private static KeyBindDefinition register(
            String id, KeyMapping.Category category, InputConstants.Type type, int defaultKey, boolean firstPress) {
        KeyBindDefinition definition = new KeyBindDefinition(id, category, type, defaultKey, firstPress);
        DEFINITIONS.add(definition);
        return definition;
    }
}
