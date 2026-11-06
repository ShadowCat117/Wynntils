/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.impl.MapCategoryImpl;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.type.MapCategory;
import com.wynntils.utils.colors.CommonColors;
import java.util.List;
import java.util.stream.Stream;

public final class PathCategoriesProvider extends BuiltInProvider {
    public static final String PATH_CATEGORY = "wynntils:path";
    public static final String RECORDING_CATEGORY = "wynntils:path-recording";
    public static final String ARROW_ICON = "wynntils:icon:path:arrows";
    private final List<MapCategory> categories =
            List.of(category(PATH_CATEGORY, "Map paths", false), category(RECORDING_CATEGORY, "Recording paths", true));

    private static MapCategory category(String id, String name, boolean recording) {
        return new MapCategoryImpl(
                id,
                name,
                new MapAttributesBuilder()
                        .setPriority(900)
                        .setIcon(ARROW_ICON)
                        .setHasPath(true)
                        .setPathColor(recording ? CommonColors.RED : CommonColors.LIGHT_BLUE)
                        .setFillColor(recording ? CommonColors.RED : CommonColors.LIGHT_BLUE)
                        .setBorderColor(CommonColors.BLACK)
                        .setBorderWidth(1f)
                        .setPathWidth(4f)
                        .asPathAttributes()
                        .build());
    }

    @Override
    public String getProviderId() {
        return "path-categories";
    }

    @Override
    public Stream<MapCategory> getCategories() {
        return categories.stream();
    }

    @Override
    public void reloadData() {}
}
