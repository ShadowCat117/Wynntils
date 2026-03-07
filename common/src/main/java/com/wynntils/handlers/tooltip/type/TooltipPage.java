/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

public enum TooltipPage {
    PAGE_1(0),
    PAGE_2(1),
    PAGE_3(2);

    private final int index;

    TooltipPage(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static TooltipPage fromIndex(int index) {
        return switch (index) {
            case 1 -> PAGE_2;
            case 2 -> PAGE_3;
            default -> PAGE_1;
        };
    }
}

