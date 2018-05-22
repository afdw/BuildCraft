/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class WorldEventListenerAdapter implements IWorldEventListener {
    @Override
    public void notifyBlockUpdate(World world,
                                  BlockPos pos,
                                  IBlockState oldState,
                                  IBlockState newState,
                                  int flags) {
    }

    @Override
    public void notifyLightSet(BlockPos pos) {
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player,
                                         SoundEvent sound,
                                         SoundCategory category,
                                         double x,
                                         double y,
                                         double z,
                                         float volume,
                                         float pitch) {
    }

    @Override
    public void playRecord(SoundEvent sound, BlockPos pos) {
    }

    @Override
    public void spawnParticle(int particleID,
                              boolean ignoreRange,
                              double xCoord,
                              double yCoord,
                              double zCoord,
                              double xSpeed,
                              double ySpeed,
                              double zSpeed,
                              int... parameters) {
    }

    @Override
    public void spawnParticle(int id,
                              boolean ignoreRange,
                              boolean minParticles,
                              double x,
                              double y,
                              double z,
                              double xSpeed,
                              double ySpeed,
                              double zSpeed,
                              int... parameters) {
    }

    @Override
    public void onEntityAdded(Entity entity) {
    }

    @Override
    public void onEntityRemoved(Entity entity) {
    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPos, int data) {
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
    }
}
