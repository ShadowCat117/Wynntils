/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.services.hades.event.HadesPlayerPingEvent;
import com.wynntils.services.hades.type.PlayerPingData;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.PlayerPingRenderer;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public class PlayerPingFeature extends Feature {
    private TimedSet<PlayerPingData> selfPlayerPingDataSet;
    private TimedSet<PlayerPingData> otherPlayerPingDataSet;

    @Persisted
    private final Config<MarkerStyle> markerStyle = new Config<>(MarkerStyle.PYLON);

    @Persisted
    private final Config<Boolean> showOwnPings = new Config<>(true);

    @Persisted
    private final Config<Integer> ownPingsDuration = new Config<>(5);

    @Persisted
    private final Config<Integer> otherPingsDuration = new Config<>(5);

    @Persisted
    private final Config<Float> pingVolume = new Config<>(1.0f);

    public PlayerPingFeature() {
        super(
                new ProfileDefault.Builder()
                        .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                        .build(),
                List.of(
                        ConfigDependency.functionality(Models.Friends.queryFriendsList),
                        ConfigDependency.functionality(Models.Party.queryPartyMembers),
                        ConfigDependency.functionality(Models.Guild.requestGuildMembers),
                        ConfigDependency.functionality(Services.Hades.connectToHades)));
    }

    @SubscribeEvent
    public void onSelfPlayerPing(HadesPlayerPingEvent.Self event) {
        if (!showOwnPings.get()) return;

        selfPlayerPingDataSet.put(event.getPlayerPingData());

        McUtils.playSoundAmbient(SoundEvents.EXPERIENCE_ORB_PICKUP, pingVolume.get(), 0.8f);
    }

    @SubscribeEvent
    public void onOtherPlayerPing(HadesPlayerPingEvent.Other event) {
        otherPlayerPingDataSet.put(event.getPlayerPingData());

        McUtils.playSoundAmbient(SoundEvents.EXPERIENCE_ORB_PICKUP, pingVolume.get(), 1.0f);
    }

    @SubscribeEvent
    public void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        for (PlayerPingData pingData : otherPlayerPingDataSet) {
            submitMarker(
                    event,
                    pingData.getLocation().toVec3(),
                    pingData.getPingType().getColor());
        }

        for (PlayerPingData pingData : selfPlayerPingDataSet) {
            submitMarker(
                    event,
                    pingData.getLocation().toVec3(),
                    tintSelfColor(pingData.getPingType().getColor()));
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "ownPingsDuration" -> {
                selfPlayerPingDataSet = new TimedSet<>(Math.max(0, ownPingsDuration.get()), TimeUnit.SECONDS, true);
            }
            case "otherPingsDuration" -> {
                otherPlayerPingDataSet = new TimedSet<>(Math.max(0, otherPingsDuration.get()), TimeUnit.SECONDS, true);
            }
        }
    }

    private void submitMarker(SubmitCustomGeometryEvent event, Vec3 worldPosition, int color) {
        if (markerStyle.get() == MarkerStyle.PYLON) {
            PlayerPingRenderer.submit(
                    event.getSubmitNodeCollector(),
                    event.getCameraRenderState().pos,
                    event.getPoseStack(),
                    worldPosition,
                    event.getLevelRenderState().gameTime,
                    color);
        } else {
            Gizmos.cuboid(new AABB(BlockPos.containing(worldPosition)), GizmoStyle.stroke(color));
        }
    }

    private int tintSelfColor(int color) {
        return CustomColor.fromARGBInt(color).brightnessShift(0.1f).asInt();
    }

    private enum MarkerStyle {
        PYLON,
        CUBE_OUTLINE
    }
}
