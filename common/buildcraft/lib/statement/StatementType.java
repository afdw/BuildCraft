package buildcraft.lib.statement;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.net.PacketBufferBC;

public abstract class StatementType<S extends IGuiSlot> {

    public final Class<S> clazz;
    @Nullable
    public final S defaultStatement;

    public StatementType(Class<S> clazz, @Nullable S defaultStatement) {
        this.clazz = clazz;
        this.defaultStatement = defaultStatement;
    }

    /** Reads a {@link StatementWrapper} from the given {@link NBTTagCompound}. The tag compound will be equal to the
     * one returned by {@link #writeToNbt(IGuiSlot)} */
    @Nullable
    public abstract S readFromNbt(NBTTagCompound nbt);

    public abstract NBTTagCompound writeToNbt(@Nullable S slot);

    /** Reads a {@link StatementWrapper} from the given {@link PacketBufferBC}. The buffer will return the data written
     * to a different buffer by {@link #writeToBuffer(PacketBufferBC, IGuiSlot)}. */
    @Nullable
    public abstract S readFromBuffer(PacketBufferBC buffer) throws IOException;

    public abstract void writeToBuffer(PacketBufferBC buffer, @Nullable S slot);

    @Nullable
    public abstract S convertToType(Object value);
}
