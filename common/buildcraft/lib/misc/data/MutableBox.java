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

import buildcraft.api.core.IBox;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;

/**
 * MUTABLE integer variant of AxisAlignedBB, with a few BC-specific methods
 */
public class MutableBox extends BoxBase {
    private BlockPos min;
    private BlockPos max;

    public MutableBox(BlockPos a, BlockPos b) {
        super(a, b);
        min = VecUtil.min(a, b);
        max = VecUtil.max(a, b);
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
    public MutableBox setMin(BlockPos min) {
        this.min = min;
        this.max = VecUtil.max(min, max);
        return this;
    }

    @Override
    public MutableBox setMax(BlockPos max) {
        this.min = VecUtil.min(min, max);
        this.max = max;
        return this;
    }

    public void toBytes(PacketBuffer stream) {
        MessageUtil.writeBlockPos(stream, min);
        MessageUtil.writeBlockPos(stream, max);
    }

    public void fromBytes(PacketBuffer stream) {
        min = MessageUtil.readBlockPos(stream);
        max = MessageUtil.readBlockPos(stream);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("min", NBTUtilBC.writeBlockPos(min()));
        nbt.setTag("max", NBTUtilBC.writeBlockPos(max()));
        return nbt;
    }

    public MutableBox readFromNbt(NBTTagCompound nbt) {
        return nbt.hasKey("xMin") ?
            setMin(new BlockPos(nbt.getInteger("xMin"), nbt.getInteger("yMin"), nbt.getInteger("zMin")))
                .setMax(new BlockPos(nbt.getInteger("xMax"), nbt.getInteger("yMax"), nbt.getInteger("zMax"))) :
            setMin(Objects.requireNonNull(NBTUtilBC.readBlockPos(nbt.getTag("min"))))
                .setMax(Objects.requireNonNull(NBTUtilBC.readBlockPos(nbt.getTag("max"))));
    }

    // Overrides for fixing return types

    @Override
    public MutableBox expand(int amount) {
        return (MutableBox) super.expand(amount);
    }

    @Override
    public MutableBox contract(int amount) {
        return (MutableBox) super.contract(amount);
    }

    @Nullable
    @Override
    public MutableBox intersect(IBox box) {
        return (MutableBox) super.intersect(box);
    }

    @Override
    public MutableBox extendToEncompass(IBox toBeContained) {
        return (MutableBox) super.extendToEncompass(toBeContained);
    }

    @Override
    public MutableBox extendToEncompass(BlockPos toBeContained) {
        return (MutableBox) super.extendToEncompass(toBeContained);
    }

    @Override
    public MutableBox extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        return (MutableBox) super.extendToEncompassBoth(newMin, newMax);
    }
}
