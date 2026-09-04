package net.eeebsiekat.morenixies.content;

import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NixieBargraphEntity extends BlockEntity {

    private float currentLevel = 0.0f;     // Normalized fill percentage (0.0 - 1.0)
    private float redlineThreshold = 0.9f; // Redline alert limit (default 90%)
    private boolean isRedlined = false;

    public NixieBargraphEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_BARGRAPH.get(), pos, state);
    }

    public float getCurrentLevel() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getCurrentLevel() : this.currentLevel;
    }

    public void setCurrentLevel(float level) {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.setCurrentLevel(level);
            return;
        }

        this.currentLevel = Mth.clamp(level, 0.0f, 1.0f);
        this.isRedlined = this.currentLevel >= this.redlineThreshold;
        setChanged();
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isRedlined() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.isRedlined() : this.isRedlined;
    }

    public float getRedlineThreshold() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getRedlineThreshold() : this.redlineThreshold;
    }

    public void setRedlineThreshold(float threshold) {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.setRedlineThreshold(threshold);
            return;
        }

        this.redlineThreshold = Mth.clamp(threshold, 0.0f, 1.0f);
        this.isRedlined = this.currentLevel >= this.redlineThreshold;
        setChanged();
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Finds the START block in the connected chain to act as the master controller.
     */
    public NixieBargraphEntity getMaster() {
        if (getLevel() == null) return this;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof NixieBargraphBlock)) return this;

        BargraphPart part = state.getValue(NixieBargraphBlock.PART);
        if (part == BargraphPart.START || part == BargraphPart.SINGLE) {
            return this;
        }

        var facing = state.getValue(NixieBargraphBlock.FACING);
        var searchPos = getBlockPos().relative(facing.getOpposite());

        for (int i = 0; i < 5; i++) {
            BlockState checkState = getLevel().getBlockState(searchPos);
            if (checkState.getBlock() instanceof NixieBargraphBlock && checkState.getValue(NixieBargraphBlock.FACING) == facing) {
                if (getLevel().getBlockEntity(searchPos) instanceof NixieBargraphEntity be) {
                    if (checkState.getValue(NixieBargraphBlock.PART) == BargraphPart.START) {
                        return be;
                    }
                }
                searchPos = searchPos.relative(facing.getOpposite());
            } else {
                break;
            }
        }

        return this;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Level", this.currentLevel);
        tag.putFloat("RedlineThreshold", this.redlineThreshold);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.currentLevel = tag.getFloat("Level");
        this.redlineThreshold = tag.contains("RedlineThreshold") ? tag.getFloat("RedlineThreshold") : 0.9f;
        this.isRedlined = this.currentLevel >= this.redlineThreshold;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}