package buildcraft.lib.statement;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;

/** Util class for holding, saving, loading and networking {@link StatementWrapper} and its
 * {@link IStatementParameter}'s. */
public class FullStatement<S extends IStatement> implements IReference<S> {
    public final StatementType<S> type;
    public final int maxParams;
    public boolean canInteract = true;
    @Nullable
    private final IStatementChangeListener listener;
    private final IStatementParameter[] params;
    private final ParamRef[] paramRefs;
    @Nullable
    private S statement;

    public FullStatement(StatementType<S> type,
                         int maxParams,
                         @Nullable IStatementChangeListener listener) {
        this.type = type;
        this.statement = type.defaultStatement;
        this.listener = listener;
        this.maxParams = maxParams;
        this.params = new IStatementParameter[maxParams];
        this.paramRefs = new FullStatement.ParamRef[maxParams];
        for (int i = 0; i < maxParams; i++) {
            paramRefs[i] = new ParamRef(this, i);
        }
    }

    // NBT

    public void readFromNbt(NBTTagCompound nbt) {
        statement = type.readFromNbt(nbt.getCompoundTag("s"));
        if (statement == null) {
            Arrays.fill(params, null);
        } else {
            for (int p = 0; p < params.length; p++) {
                NBTTagCompound pNbt = nbt.getCompoundTag(Integer.toString(p));
                params[p] = StatementTypeParam.INSTANCE.readFromNbt(pNbt);
            }
        }
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (statement != null) {
            nbt.setTag("s", type.writeToNbt(statement));
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p];
                if (param != null) {
                    nbt.setTag(Integer.toString(p), StatementTypeParam.INSTANCE.writeToNbt(param));
                }
            }
        }
        return nbt;
    }

    // Networking

    public void readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            statement = type.readFromBuffer(buffer);
            for (int p = 0; p < params.length; p++) {
                params[p] = StatementTypeParam.INSTANCE.readFromBuffer(buffer);
            }
        } else {
            statement = type.defaultStatement;
            Arrays.fill(params, null);
        }
    }

    public void writeToBuffer(PacketBufferBC buffer) {
        if (statement == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            type.writeToBuffer(buffer, statement);
            for (IStatementParameter param : params) {
                StatementTypeParam.INSTANCE.writeToBuffer(buffer, param);
            }
        }
    }

    // IReference

    @Override
    public S get() {
        return statement;
    }

    @Override
    public void set(@Nullable S to) {
        statement = to;
        if (statement == null) {
            Arrays.fill(params, null);
            return;
        }
        for (int i = 0; i < params.length; i++) {
            params[i] = i <= statement.maxParameters() ? statement.createParameter(params[i], i) : null;
        }
    }

    @Override
    public boolean canSet(@Nullable S value) {
        if (value == null) {
            return true;
        }
        return value.minParameters() <= params.length;
    }

    @Override
    public S convertToType(@Nullable Object value) {
        S val = IReference.super.convertToType(value);
        if (value != null && val == null) {
            return type.convertToType(value);
        }
        return val;
    }

    @Override
    public Class<S> getHeldType() {
        return type.clazz;
    }

    static class ParamRef implements IReference<IStatementParameter> {
        public final IReference<? extends IStatement> statementRef;
        public final IStatementParameter[] array;
        public final int index;

        public ParamRef(FullStatement<?> full, int index) {
            statementRef = full;
            this.array = full.params;
            this.index = index;
        }

        @Override
        @Nullable
        public IStatementParameter get() {
            return array[index];
        }

        @Override
        public void set(@Nullable IStatementParameter to) {
            array[index] = to;
        }

        @Override
        public boolean canSet(@Nullable IStatementParameter value) {
            IStatement statement = statementRef.get();
            if (statement == null) {
                return false;
            }
            return statement.createParameter(value, index) == value;
        }

        @Override
        public Class<IStatementParameter> getHeldType() {
            return IStatementParameter.class;
        }
    }

    // Params

    public IReference<IStatementParameter> getParamRef(int i) {
        return paramRefs[i];
    }

    @Nullable
    public IStatementParameter get(int index) {
        return getParamRef(index).get();
    }

    public void set(int index, IStatementParameter param) {
        getParamRef(index).set(param);
    }

    public boolean canSet(int index, IStatementParameter param) {
        return getParamRef(index).canSet(param);
    }

    public int getParamCount() {
        return params.length;
    }

    public IStatementParameter[] getParameters() {
        return params;
    }

    // Gui change listeners

    /** Gui elements should call this after calling {@link #set(IStatement)} or {@link #set(int, IStatementParameter)},
     * with either -1 or the param index respectively. */
    public void postSetFromGui(int paramIndex) {
        if (listener != null) {
            listener.onChange(this, paramIndex);
        }
    }

    @FunctionalInterface
    public interface IStatementChangeListener {
        /** @param statement The statement that changed
         * @param paramIndex The index of the parameter that changed, or -1 if the main {@link IStatement} changed. */
        void onChange(FullStatement<?> statement, int paramIndex);
    }
}
