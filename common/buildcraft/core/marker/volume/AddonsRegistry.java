/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public enum AddonsRegistry {
    INSTANCE;

    private final List<AddonType> registry = new ArrayList<>();

    public void register(AddonType addonType) {
        if (getAddonTypeByName(addonType.name) == null) {
            registry.add(addonType);
        }
    }

    @Nullable
    public AddonType getAddonTypeByName(ResourceLocation name) {
        return registry.stream()
            .filter(addonType -> addonType.name.equals(name))
            .findFirst()
            .orElse(null);
    }

    @Nullable
    public AddonType getAddonTypeByClass(Class<? extends Addon> clazz) {
        return registry.stream()
            .filter(addonType -> addonType.clazz.equals(clazz))
            .findFirst()
            .orElse(null);
    }

    public static class AddonType {
        public final ResourceLocation name;
        public final Class<? extends Addon> clazz;
        public final Function<VolumeBox, ? extends Addon> create;

        public AddonType(ResourceLocation name,
                         Class<? extends Addon> clazz,
                         Function<VolumeBox, ? extends Addon> create) {
            this.name = name;
            this.clazz = clazz;
            this.create = create;
        }
    }
}
