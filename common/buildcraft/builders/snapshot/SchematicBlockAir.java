package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SchematicBlockAir implements ISchematicBlock<SchematicBlockAir> {
    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return true;
    }

    @Override
    public void init(SchematicBlockContext context) {
    }

    @Override
    public int getLevel() {
        return BLOCK_LEVEL;
    }

    @Override
    public boolean isAir() {
        return true;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(SchematicBlockContext context) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids(SchematicBlockContext context) {
        return Collections.emptyList();
    }

    @Override
    public SchematicBlockAir getRotated(Rotation rotation) {
        return this;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean buildWithoutChecks(World world, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
