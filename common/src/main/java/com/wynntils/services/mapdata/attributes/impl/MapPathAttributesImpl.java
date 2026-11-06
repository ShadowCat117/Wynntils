/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.impl;

import com.wynntils.services.mapdata.attributes.type.MapPathAttributes;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;

public final class MapPathAttributesImpl extends MapAttributesImpl implements MapPathAttributes {
    public MapPathAttributesImpl(
            Integer priority,
            Integer level,
            String label,
            String description,
            MapVisibilityImpl labelVisibility,
            CustomColor labelColor,
            TextShadow labelShadow,
            String icon,
            CustomColor pathColor,
            Boolean hasPath,
            Float pathCycleDistance,
            CustomColor fillColor,
            Float pathWidth,
            CustomColor borderColor,
            Float borderWidth) {
        super(
                priority,
                level,
                label,
                description,
                labelVisibility,
                labelColor,
                labelShadow,
                icon,
                null,
                null,
                null,
                null,
                fillColor,
                borderColor,
                borderWidth,
                pathWidth,
                pathColor,
                hasPath,
                pathCycleDistance);
    }

    public MapPathAttributesImpl(MapAttributesImpl attributes) {
        super(attributes);
    }
}
