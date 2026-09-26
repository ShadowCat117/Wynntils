/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.type;

import com.wynntils.hades.protocol.enums.PlayerPingType;
import com.wynntils.hades.protocol.packets.server.HSPacketPlayerPing;
import com.wynntils.utils.mc.type.Location;

public class PlayerPingData {
    private final String username;
    private final Location location;
    private final PlayerPingType pingType;

    public PlayerPingData(String username, Location location, PlayerPingType pingType) {
        this.username = username;
        this.location = location;
        this.pingType = pingType;
    }

    public static PlayerPingData fromPacket(HSPacketPlayerPing packet) {
        return new PlayerPingData(
                packet.getUsername(),
                Location.containing(packet.getX(), packet.getY(), packet.getZ()),
                packet.getPingType());
    }

    public String getUsername() {
        return username;
    }

    public Location getLocation() {
        return location;
    }

    public PlayerPingType getPingType() {
        return pingType;
    }
}
