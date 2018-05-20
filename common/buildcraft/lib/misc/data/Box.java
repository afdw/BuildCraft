/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;

public class Box extends BoxBase {
    private final BlockPos min;
    private final BlockPos max;

    public Box(BlockPos a, BlockPos b) {
        super(a, b);
        min = VecUtil.min(a, b);
        max = VecUtil.max(a, b);
    }

    public Box(IBox box) {
        this(box.min(), box.max());
    }

    public Box(IAreaProvider areaProvider) {
        this(areaProvider.min(), areaProvider.max());
    }

    public Box(PacketBuffer stream) {
        this(
            MessageUtil.readBlockPos(stream),
            MessageUtil.readBlockPos(stream)
        );
    }

    public Box(NBTTagCompound nbt) {
        this(
            nbt.hasKey("xMin") ?
                new BlockPos(nbt.getInteger("xMin"), nbt.getInteger("yMin"), nbt.getInteger("zMin")) :
                Objects.requireNonNull(NBTUtilBC.readBlockPos(nbt.getTag("min"))),
            nbt.hasKey("xMax") ?
                new BlockPos(nbt.getInteger("xMax"), nbt.getInteger("yMax"), nbt.getInteger("zMax")) :
                Objects.requireNonNull(NBTUtilBC.readBlockPos(nbt.getTag("max")))
        );
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public Box setMin(BlockPos min) {
        return new Box(
            min,
            VecUtil.max(min, max)
        );
    }

    @Override
    public Box setMax(BlockPos max) {
        return new Box(
            VecUtil.min(min, max),
            max
        );
    }

    public void toBytes(PacketBuffer stream) {
        MessageUtil.writeBlockPos(stream, min);
        MessageUtil.writeBlockPos(stream, max);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("min", NBTUtilBC.writeBlockPos(min()));
        nbt.setTag("max", NBTUtilBC.writeBlockPos(max()));
        return nbt;
    }

    // Overrides for fixing return types

    @Override
    public Box expand(int amount) {
        return (Box) super.expand(amount);
    }

    @Override
    public Box contract(int amount) {
        return (Box) super.contract(amount);
    }

    @Nullable
    @Override
    public Box intersect(IBox box) {
        return (Box) super.intersect(box);
    }

    @Override
    public Box extendToEncompass(IBox toBeContained) {
        return (Box) super.extendToEncompass(toBeContained);
    }

    @Override
    public Box extendToEncompass(BlockPos toBeContained) {
        return (Box) super.extendToEncompass(toBeContained);
    }

    @Override
    public Box extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        return (Box) super.extendToEncompassBoth(newMin, newMax);
    }
}
