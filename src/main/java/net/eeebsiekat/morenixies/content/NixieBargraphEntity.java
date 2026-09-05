package net.eeebsiekat.morenixies.content;

import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NixieBargraphEntity extends BlockEntity {

    private float currentLevel = 0.0f;  // Current rendered position ratio
    private float targetLevel = 0.0f;   // Target ratio set by Display Link
    private float velocity = 0.0f;      // Physics velocity for spring bounce

    private float redlineThreshold = 0.9f;
    private boolean isRedlined = false;

    // Physics parameters (Tweak for different bounce feels)
    private static final float STIFFNESS = 0.08f;
    private static final float DAMPING = 0.70f;

    public NixieBargraphEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_BARGRAPH.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieBargraphEntity be) {
        if (level.isClientSide) {
            be.updatePhysics();
        }
    }

    private void updatePhysics() {
        NixieBargraphEntity master = getMaster();
        if (master != this) return;

        // Spring-Damper calculation
        float displacement = this.targetLevel - this.currentLevel;
        float force = displacement * STIFFNESS;

        this.velocity = (this.velocity + force) * DAMPING;
        this.currentLevel += this.velocity;

        // Settle when velocity and displacement become negligible
        if (Math.abs(this.velocity) < 0.0005f && Math.abs(displacement) < 0.0005f) {
            this.currentLevel = this.targetLevel;
            this.velocity = 0.0f;
        }

        this.isRedlined = this.currentLevel >= this.redlineThreshold;
    }

    public float getCurrentLevel() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getCurrentLevel() : this.currentLevel;
    }

    public float getVelocity() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getVelocity() : this.velocity;
    }

    public void setTargetLevel(float level) {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.setTargetLevel(level);
            return;
        }

        this.targetLevel = Mth.clamp(level, 0.0f, 1.0f);
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
     * Traverses DOWN to find the START/SINGLE block controller.
     */
    public NixieBargraphEntity getMaster() {
        if (getLevel() == null) return this;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof NixieBargraphBlock)) return this;

        BargraphPart part = state.getValue(NixieBargraphBlock.PART);
        if (part == BargraphPart.START || part == BargraphPart.SINGLE) {
            return this;
        }

        BlockPos searchPos = getBlockPos().below();
        for (int i = 0; i < 16; i++) {
            BlockState checkState = getLevel().getBlockState(searchPos);
            if (checkState.getBlock() instanceof NixieBargraphBlock) {
                if (checkState.getValue(NixieBargraphBlock.PART) == BargraphPart.START) {
                    if (getLevel().getBlockEntity(searchPos) instanceof NixieBargraphEntity be) {
                        return be;
                    }
                }
                searchPos = searchPos.below();
            } else {
                break;
            }
        }

        return this;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("TargetLevel", this.targetLevel);
        tag.putFloat("CurrentLevel", this.currentLevel);
        tag.putFloat("RedlineThreshold", this.redlineThreshold);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.targetLevel = tag.getFloat("TargetLevel");

        // Only force currentLevel on first load/spawn (when currentLevel is 0)
        // On continuous updates, let the client physics interpolate seamlessly
        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            this.currentLevel = tag.getFloat("CurrentLevel");
        } else if (this.currentLevel == 0.0f && !tag.contains("Initialized")) {
            this.currentLevel = this.targetLevel;
        }

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