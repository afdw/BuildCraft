/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public enum AddonsRegistry {
    INSTANCE;

    private final Map<ResourceLocation, Supplier<? extends Addon>> registry = new HashMap<>();

    public void register(ResourceLocation name, Supplier<? extends Addon> supplier) {
        if (!registry.containsKey(name)) {
            registry.put(name, supplier);
        }
    }

    @Nullable
    public Supplier<? extends Addon> getSupplierByName(ResourceLocation name) {
        return registry.get(name);
    }

    public ResourceLocation getNameByClass(Class<? extends Addon> clazz) {
        return registry.entrySet().stream()
            .filter(entry -> entry.getValue().get().getClass().equals(clazz))
            .findFirst()
            .orElseThrow(IllegalStateException::new)
            .getKey();
    }
}
