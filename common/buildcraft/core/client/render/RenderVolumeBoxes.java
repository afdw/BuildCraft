/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;

import buildcraft.lib.client.render.DetachedRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock;

public enum RenderVolumeBoxes implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public void render(EntityPlayer player, float partialTicks) {
        GlStateManager.enableBlend();

        BufferBuilder bb = Tessellator.getInstance().getBuffer();

        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        ClientVolumeBoxes.INSTANCE.volumeBoxes.forEach(volumeBox -> {
            LaserBoxRenderer.renderLaserBoxDynamic(
                volumeBox.box,
                volumeBox.getChange() != null ?
                    BuildCraftLaserManager.MARKER_VOLUME_SIGNAL :
                    volumeBox.getLockTargetsStream()
                        .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                        .map(target -> ((Lock.Target.TargetUsedByMachine) target).type.getLaserType())
                        .findFirst().orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED),
                bb,
                false
            );

            volumeBox.addons.values().forEach(addon ->
                ((IFastAddonRenderer<Addon>) addon.getRenderer()).renderAddonFast(addon, player, partialTicks, bb)
            );
        });

        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
    }
}
