/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageManager;

import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public enum BCCoreEventDist {
    INSTANCE;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote && event.world.getMinecraftServer() != null) {
            WorldSavedDataVolumeBoxes.get(event.world).tick();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerMP) {
            // Delay sending join messages to player as it makes it work when in single-player
            MessageUtil.doDelayed(() ->
                MessageManager.sendTo(
                    new MessageVolumeBoxes(WorldSavedDataVolumeBoxes.get(event.getEntity().world).volumeBoxes),
                    (EntityPlayerMP) event.getEntity()
                )
            );
            WorldSavedDataVolumeBoxes.get(((EntityPlayerMP) event.getEntity()).world).volumeBoxes.stream()
                .map(VolumeBox::getChange)
                .filter(Objects::nonNull)
                .filter(change -> change.isPaused() && change.getPlayerId().equals(event.getEntity().getUniqueID()))
                .forEach(change -> change.setPaused(false));
        }
    }
}
