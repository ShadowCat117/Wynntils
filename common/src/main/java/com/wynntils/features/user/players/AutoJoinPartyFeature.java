/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.players;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.models.players.event.PartyEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.PLAYERS)
public class AutoJoinPartyFeature extends UserFeature {
    @Config
    public boolean onlyFriends = true;

    @SubscribeEvent
    public void onPartyInvite(PartyEvent.Invited event) {
        if (onlyFriends && !Models.Friends.isFriend(event.getPlayerName())) return;
        if (Models.Party.isInParty()) return;

        Managers.Notification.queueMessage("Auto-joined " + event.getPlayerName() + "'s party");
        McUtils.playSound(SoundEvents.END_PORTAL_FRAME_FILL);

        Models.Party.partyJoin(event.getPlayerName());
    }
}