/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.attributes.type;

import java.util.List;

public interface MapAreaAttributes extends MapAttributes {
    static List<String> getUnsupportedAttributes() {
        return List.of(
                "pathColor",
                "hasPath",
                "pathCycleDistance",
                "pathWidth",
                "iconId",
                "iconVisibility",
                "iconColor",
                "iconDecoration",
                "hasMarker",
                "markerOptions");
    }
}
