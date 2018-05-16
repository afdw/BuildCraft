/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.core.client.BuildCraftLaserManager;

public class Lock {
    final Cause cause;
    final List<Target> targets;

    public Lock(Cause cause, Target... targets) {
        this.cause = cause;
        this.targets = ImmutableList.copyOf(Arrays.asList(targets));
    }

    Lock(NBTTagCompound nbt) {
        NBTTagCompound causeTag = nbt.getCompoundTag("cause");
        cause = Objects.requireNonNull(
            NBTUtilBC.readEnum(causeTag.getTag("type"), Cause.EnumCause.class)
        ).fromNbt.apply(causeTag.getCompoundTag("data"));
        targets = NBTUtilBC.readCompoundList(nbt.getTag("targets"))
            .map(targetTag ->
                Objects.requireNonNull(
                    NBTUtilBC.readEnum(targetTag.getTag("type"), Target.EnumTarget.class)
                ).fromNbt.apply(targetTag.getCompoundTag("data"))
            )
            .collect(Collectors.toList());
    }

    Lock(PacketBufferBC buf) {
        cause = new PacketBufferBC(buf).readEnumValue(Cause.EnumCause.class).fromBytes.apply(buf);
        targets = IntStream.range(0, buf.readInt())
            .mapToObj(i -> new PacketBufferBC(buf).readEnumValue(Target.EnumTarget.class).fromBytes.apply(buf))
            .collect(Collectors.toList());
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound causeTag = new NBTTagCompound();
        causeTag.setTag("type", NBTUtilBC.writeEnum(Cause.EnumCause.getForClass(cause.getClass())));
        causeTag.setTag("data", cause.writeToNBT(new NBTTagCompound()));
        nbt.setTag("cause", causeTag);
        nbt.setTag("targets", NBTUtilBC.writeCompoundList(targets.stream().map(target -> {
            NBTTagCompound targetTag = new NBTTagCompound();
            targetTag.setTag("type", NBTUtilBC.writeEnum(Target.EnumTarget.getForClass(target.getClass())));
            targetTag.setTag("data", target.writeToNBT(new NBTTagCompound()));
            return targetTag;
        })));
        return nbt;
    }

    public void toBytes(PacketBuffer buf) {
        new PacketBufferBC(buf).writeEnumValue(Cause.EnumCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target -> {
            new PacketBuffer(buf).writeEnumValue(Target.EnumTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public static abstract class Cause {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public abstract boolean stillWorks(World world);

        public static class CauseBlock extends Cause {
            public final BlockPos pos;
            public final Block block;

            public CauseBlock(BlockPos pos, Block block) {
                this.pos = pos;
                this.block = block;
            }

            private CauseBlock(NBTTagCompound nbt) {
                pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
                block = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block")));
            }

            private CauseBlock(PacketBuffer buf) {
                pos = MessageUtil.readBlockPos(buf);
                block = Block.REGISTRY.getObject(new ResourceLocation(buf.readString(1024)));
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("pos", NBTUtil.createPosTag(pos));
                nbt.setString("block", Block.REGISTRY.getNameForObject(block).toString());
                return nbt;
            }

            @Override
            public void toBytes(PacketBuffer buf) {
                MessageUtil.writeBlockPos(buf, pos);
                buf.writeString(Block.REGISTRY.getNameForObject(block).toString());
            }

            @Override
            public boolean stillWorks(World world) {
                return world.getBlockState(pos).getBlock() == block;
            }
        }

        enum EnumCause {
            BLOCK(CauseBlock.class, CauseBlock::new, CauseBlock::new);

            public final Class<? extends Cause> clazz;
            public final Function<NBTTagCompound, ? extends Cause> fromNbt;
            public final Function<PacketBufferBC, ? extends Cause> fromBytes;

            EnumCause(Class<? extends Cause> clazz,
                      Function<NBTTagCompound, ? extends Cause> fromNbt,
                      Function<PacketBufferBC, ? extends Cause> fromBytes) {
                this.clazz = clazz;
                this.fromNbt = fromNbt;
                this.fromBytes = fromBytes;
            }

            public static EnumCause getForClass(Class<? extends Cause> clazz) {
                return Arrays.stream(values())
                    .filter(enumCause -> enumCause.clazz == clazz)
                    .findFirst()
                    .orElseThrow(NullPointerException::new);
            }
        }
    }

    public static abstract class Target {
        public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

        public abstract void toBytes(PacketBuffer buf);

        public static class TargetRemove extends Target {
            public TargetRemove() {
            }

            private TargetRemove(@SuppressWarnings("unused") NBTTagCompound nbt) {
            }

            private TargetRemove(@SuppressWarnings("unused") PacketBuffer buf) {
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                return nbt;
            }

            @Override
            public void toBytes(PacketBuffer buf) {
            }
        }

        public static class TargetResize extends Target {
            public TargetResize() {
            }

            private TargetResize(@SuppressWarnings("unused") NBTTagCompound nbt) {
            }

            private TargetResize(@SuppressWarnings("unused") PacketBuffer buf) {
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                return nbt;
            }

            @Override
            public void toBytes(PacketBuffer buf) {
            }
        }

        public static class TargetAddon extends Target {
            public final EnumAddonSlot slot;

            public TargetAddon(EnumAddonSlot slot) {
                this.slot = slot;
            }

            private TargetAddon(NBTTagCompound nbt) {
                slot = Objects.requireNonNull(NBTUtilBC.readEnum(nbt.getTag("slot"), EnumAddonSlot.class));
            }

            private TargetAddon(PacketBuffer buf) {
                slot = new PacketBufferBC(buf).readEnumValue(EnumAddonSlot.class);
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("slot", NBTUtilBC.writeEnum(slot));
                return nbt;
            }

            @Override
            public void toBytes(PacketBuffer buf) {
                new PacketBufferBC(buf).writeEnumValue(slot);
            }
        }

        public static class TargetUsedByMachine extends Target {
            public final EnumType type;

            public TargetUsedByMachine(EnumType type) {
                this.type = type;
            }

            private TargetUsedByMachine(NBTTagCompound nbt) {
                type = Objects.requireNonNull(NBTUtilBC.readEnum(nbt.getTag("type"), EnumType.class));
            }

            private TargetUsedByMachine(PacketBuffer buf) {
                type = new PacketBufferBC(buf).readEnumValue(EnumType.class);
            }

            @Override
            public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
                nbt.setTag("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void toBytes(PacketBuffer buf) {
                new PacketBufferBC(buf).writeEnumValue(type);
            }

            public enum EnumType {
                STRIPES_WRITE {
                    @SideOnly(Side.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_WRITE;
                    }
                },
                STRIPES_READ {
                    @SideOnly(Side.CLIENT)
                    @Override
                    public LaserData_BC8.LaserType getLaserType() {
                        return BuildCraftLaserManager.STRIPES_READ;
                    }
                };

                @SideOnly(Side.CLIENT)
                public abstract LaserData_BC8.LaserType getLaserType();
            }
        }

        enum EnumTarget {
            REMOVE(TargetRemove.class, TargetRemove::new, TargetRemove::new),
            RESIZE(TargetResize.class, TargetResize::new, TargetResize::new),
            ADDON(TargetAddon.class, TargetAddon::new, TargetAddon::new),
            USED_BY_MACHINE(TargetUsedByMachine.class, TargetUsedByMachine::new, TargetUsedByMachine::new);

            public final Class<? extends Target> clazz;
            public final Function<NBTTagCompound, ? extends Target> fromNbt;
            public final Function<PacketBufferBC, ? extends Target> fromBytes;

            EnumTarget(Class<? extends Target> clazz,
                       Function<NBTTagCompound, ? extends Target> fromNbt,
                       Function<PacketBufferBC, ? extends Target> fromBytes) {
                this.clazz = clazz;
                this.fromNbt = fromNbt;
                this.fromBytes = fromBytes;
            }

            public static EnumTarget getForClass(Class<? extends Target> clazz) {
                return Arrays.stream(values())
                    .filter(enumTarget -> enumTarget.clazz == clazz)
                    .findFirst()
                    .orElseThrow(NullPointerException::new);
            }
        }
    }
}
