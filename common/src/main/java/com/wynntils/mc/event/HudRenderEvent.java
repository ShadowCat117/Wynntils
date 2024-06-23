/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class HudRenderEvent extends Event {
    private final RenderEvent.ElementType elementType;
    private final RenderOffset renderOffset;

    private int offset = 0;

    public HudRenderEvent(RenderEvent.ElementType elementType, RenderOffset renderOffset) {
        this.elementType = elementType;
        this.renderOffset = renderOffset;
    }

    public RenderEvent.ElementType getElementType() {
        return elementType;
    }

    public RenderOffset getRenderOffset() {
        return renderOffset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public enum RenderOffset {
        X,
        Y
    }
}
