/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.IBox;

import buildcraft.lib.misc.PositionUtil;

abstract class BoxBase implements IBox {
    BoxBase(BlockPos a, BlockPos b) {
    }

    public List<BlockPos> getBlocksInArea() {
        return Lists.newArrayList(BlockPos.getAllInBox(min(), max()));
    }

    public List<BlockPos> getBlocksOnEdge() {
        return PositionUtil.getAllOnEdge(min(), max());
    }

    /**
     * Delegate for {@link PositionUtil#isCorner(BlockPos, BlockPos, BlockPos)}
     */
    public boolean isCorner(BlockPos pos) {
        return PositionUtil.isCorner(min(), max(), pos);
    }

    /**
     * Delegate for {@link PositionUtil#isOnEdge(BlockPos, BlockPos, BlockPos)}
     */
    public boolean isOnEdge(BlockPos pos) {
        return PositionUtil.isOnEdge(min(), max(), pos);
    }

    /**
     * Delegate for {@link PositionUtil#isOnFace(BlockPos, BlockPos, BlockPos)}
     */
    public boolean isOnFace(BlockPos pos) {
        return PositionUtil.isOnFace(min(), max(), pos);
    }

    /**
     * Calculates the total number of blocks on the edge. This is identical to (but faster than) calling
     * {@link #getBlocksOnEdge()}.{@link List#size() size()}
     *
     * @return The size of the list returned by {@link #getBlocksOnEdge()}.
     */
    public int getBlocksOnEdgeCount() {
        return PositionUtil.getCountOnEdge(min(), max());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
            obj != null &&
                obj.getClass() == getClass() &&
                Objects.equal(min(), ((BoxBase) obj).min()) &&
                Objects.equal(max(), ((BoxBase) obj).max());
    }

    @Override
    public String toString() {
        return "Box[min = " + min() + ", max = " + max() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min(), max());
    }
}
