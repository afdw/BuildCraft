/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;

public class VolumeBox {
    public final World world;
    public final UUID id;
    public Box box;
    @Nullable
    private Change change;
    public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
    public final List<Lock> locks = new ArrayList<>();

    public VolumeBox(World world, PacketBufferBC buf) {
        this.world = world;
        id = buf.readUniqueId();
        box = new Box(buf);
    }

    public VolumeBox(World world, BlockPos at) {
        this.world = world;
        id = UUID.randomUUID();
        box = new Box(at, at);
    }

    public VolumeBox(World world, NBTTagCompound nbt) {
        this.world = world;
        id = Optional.ofNullable(nbt.getUniqueId("id")).orElseGet(UUID::randomUUID);
        box = new Box(nbt.getCompoundTag("box"));
        change = nbt.hasKey("change") ? new Change(nbt.getCompoundTag("change")) : null;
        NBTUtilBC.readCompoundList(nbt.getTag("addons")).forEach(addonsEntryTag -> {
            AddonsRegistry.AddonType addonType = AddonsRegistry.INSTANCE.getAddonTypeByName(
                new ResourceLocation(
                    addonsEntryTag.getString("typeName")
                )
            );
            if (addonType != null) {
                Addon addon = addonType.create.apply(this);
                addon.readFromNBT(addonsEntryTag.getCompoundTag("data"));
                EnumAddonSlot slot = NBTUtilBC.readEnum(addonsEntryTag.getTag("slot"), EnumAddonSlot.class);
                addons.put(slot, addon);
                addon.postReadFromNbt();
            }
        });
        NBTUtilBC.readCompoundList(nbt.getTag("locks")).map(lockTag -> {
            try {
                return new Lock(lockTag);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).forEach(locks::add);
    }

    public Stream<Lock.Target> getLockTargetsStream() {
        return locks.stream().flatMap(lock -> lock.targets.stream());
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("id", id);
        nbt.setTag("box", this.box.writeToNbt());
        if (change != null) {
            nbt.setTag("change", change.writeToNBT());
        }
        nbt.setTag(
            "addons",
            NBTUtilBC.writeCompoundList(
                addons.entrySet().stream().map(entry -> {
                    NBTTagCompound addonsEntryTag = new NBTTagCompound();
                    addonsEntryTag.setTag("slot", NBTUtilBC.writeEnum(entry.getKey()));
                    addonsEntryTag.setString(
                        "typeName",
                        Objects.requireNonNull(
                            AddonsRegistry.INSTANCE.getAddonTypeByClass(entry.getValue().getClass())
                        ).name.toString()
                    );
                    addonsEntryTag.setTag("data", entry.getValue().writeToNBT(new NBTTagCompound()));
                    return addonsEntryTag;
                })
            ));
        nbt.setTag("locks", NBTUtilBC.writeCompoundList(locks.stream().map(Lock::writeToNBT)));
        return nbt;
    }

    public void toBytes(PacketBufferBC buf) {
        buf.writeUniqueId(id);
        box.toBytes(buf);
        buf.writeBoolean(change != null);
        if (change != null) {
            change.toBytes(buf);
        }
        buf.writeInt(addons.size());
        addons.forEach((slot, addon) -> {
            buf.writeEnumValue(slot);
            buf.writeString(
                Objects.requireNonNull(
                    AddonsRegistry.INSTANCE.getAddonTypeByClass(addon.getClass())
                ).name.toString()
            );
            addon.toBytes(buf);
        });
        buf.writeInt(locks.size());
        locks.forEach(lock -> lock.toBytes(buf));
    }

    public void fromBytes(PacketBufferBC buf) throws IOException {
        change = buf.readBoolean() ? new Change(buf) : null;
        Map<EnumAddonSlot, Addon> newAddons = new EnumMap<>(EnumAddonSlot.class);
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            EnumAddonSlot slot = buf.readEnumValue(EnumAddonSlot.class);
            AddonsRegistry.AddonType addonType = AddonsRegistry.INSTANCE.getAddonTypeByName(
                new ResourceLocation(buf.readString())
            );
            if (addonType != null) {
                Addon addon = addonType.create.apply(this);
                addon.onAdded();
                addon.fromBytes(buf);
                newAddons.put(slot, addon);
            }
        }
        addons.keySet().removeIf(slot -> !newAddons.containsKey(slot));
        newAddons.entrySet().stream().filter(slotAddon -> !addons.containsKey(slotAddon.getKey()))
            .forEach(slotAddon -> addons.put(slotAddon.getKey(), slotAddon.getValue()));
        for (Map.Entry<EnumAddonSlot, Addon> slotAddon : newAddons.entrySet()) {
            PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
            slotAddon.getValue().toBytes(buffer);
            addons.get(slotAddon.getKey()).fromBytes(buffer);
        }
        locks.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> new Lock(buf)).forEach(locks::add);
    }

    @Nullable
    public Change getChange() {
        return change;
    }

    public void startChange(Change change) {
        this.change = change;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id.equals(((VolumeBox) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public class Change {
        private final UUID playerId;
        private final Box oldBox;
        private final BlockPos held;
        private final double dist;
        private boolean paused = false;

        public Change(UUID playerId, Box oldBox, BlockPos held, double dist) {
            this.playerId = playerId;
            this.oldBox = oldBox;
            this.held = held;
            this.dist = dist;
        }

        public Change(PacketBufferBC buf) {
            playerId = buf.readUniqueId();
            oldBox = new Box(buf);
            held = buf.readBlockPos();
            dist = buf.readDouble();
            paused = buf.readBoolean();
        }

        public Change(NBTTagCompound nbt) {
            playerId = NBTUtil.getUUIDFromTag(nbt.getCompoundTag("playerId"));
            oldBox = new Box(nbt.getCompoundTag("oldBox"));
            held = NBTUtil.getPosFromTag(nbt.getCompoundTag("held"));
            dist = nbt.getDouble("dist");
            paused = nbt.getBoolean("paused");
        }

        public void cancel() {
            box = oldBox;
            change = null;
        }

        public void confirm() {
            addons.values().forEach(Addon::onVolumeBoxSizeChange);
            change = null;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public BlockPos getHeld() {
            return held;
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        @Nullable
        private EntityPlayer getPlayer() {
            return !paused ? world.getPlayerEntityByUUID(playerId) : null;
        }

        @Nullable
        BlockPos getLookingAt() {
            return Optional.ofNullable(getPlayer()).map(player ->
                new BlockPos(
                    player.getPositionVector()
                        .addVector(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(dist))
                )
            ).orElse(null);
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("playerId", NBTUtil.createUUIDTag(playerId));
            nbt.setTag("oldBox", oldBox.writeToNbt());
            nbt.setTag("held", NBTUtil.createPosTag(held));
            nbt.setDouble("dist", dist);
            nbt.setBoolean("paused", paused);
            return nbt;
        }

        public void toBytes(PacketBufferBC buf) {
            buf.writeUniqueId(playerId);
            oldBox.toBytes(buf);
            buf.writeBlockPos(held);
            buf.writeDouble(dist);
            buf.writeBoolean(paused);
        }
    }
}
