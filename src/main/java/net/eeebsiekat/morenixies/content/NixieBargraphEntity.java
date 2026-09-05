package net.eeebsiekat.morenixies.content;

import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NixieBargraphEntity extends BlockEntity {

    public enum DisplayMode {
        BAR,
        DOT
    }

    private float currentLevel = 0.0f;  // Rendered position ratio
    private float targetLevel = 0.0f;   // Target ratio set by Display Link
    private float velocity = 0.0f;      // Spring bounce velocity

    private float redlineThreshold = 0.9f;
    private boolean isRedlined = false;
    private int color = 0xFF8400;       // Default classic Nixie orange glow
    private DisplayMode mode = DisplayMode.BAR;

    private int lastRedstoneLevel = 0;

    // Physics parameters
    private static final float STIFFNESS = 0.08f;
    private static final float DAMPING = 0.70f;

    public NixieBargraphEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_BARGRAPH.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieBargraphEntity be) {
        if (level.isClientSide) {
            be.updatePhysics();
        } else {
            be.updateServerState();
        }
    }

    private void updatePhysics() {
        NixieBargraphEntity master = getMaster();
        if (master != this) return;

        // Spring-Damper calculation
        float displacement = this.targetLevel - this.currentLevel;
        float force = displacement * STIFFNESS;

        float prevVelocity = this.velocity;
        this.velocity = (this.velocity + force) * DAMPING;
        this.currentLevel += this.velocity;

        // Settle when velocity and displacement become negligible
        if (Math.abs(this.velocity) < 0.0005f && Math.abs(displacement) < 0.0005f) {
            this.currentLevel = this.targetLevel;
            this.velocity = 0.0f;
        }

        // Sound trigger for sudden physical motion shifts
        if (Math.abs(this.velocity - prevVelocity) > 0.05f && this.level != null) {
            this.level.playLocalSound(
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5,
                    SoundEvents.TRIPWIRE_CLICK_ON,
                    SoundSource.BLOCKS,
                    0.25f,
                    1.6f + (this.level.random.nextFloat() * 0.4f),
                    false
            );
        }

        this.isRedlined = this.currentLevel >= this.redlineThreshold;
    }

    private void updateServerState() {
        NixieBargraphEntity master = getMaster();
        if (master != this) return;

        // Notify adjacent blocks if redstone output changed
        int currentRedstone = (int) Math.floor(this.currentLevel * 15.0f);
        if (currentRedstone != this.lastRedstoneLevel && this.level != null) {
            this.lastRedstoneLevel = currentRedstone;
            this.level.updateNeighborsAt(this.worldPosition, getBlockState().getBlock());
        }
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

        float newTarget = Mth.clamp(level, 0.0f, 1.0f);
        if (Math.abs(newTarget - this.targetLevel) > 0.001f) {
            this.targetLevel = newTarget;

            // Pre-dip physics impulse on change
            this.velocity -= (newTarget < this.currentLevel) ? 0.015f : 0.008f;

            notifyUpdate();
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

    public void cycleRedlineThreshold() {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.cycleRedlineThreshold();
            return;
        }

        // Cycle in steps of 0.10: 0.9 -> 1.0 -> 0.5 -> 0.6...
        float next = this.redlineThreshold + 0.10f;
        if (next > 1.05f) next = 0.50f;
        this.redlineThreshold = Math.round(next * 100.0f) / 100.0f;

        this.isRedlined = this.currentLevel >= this.redlineThreshold;
        notifyUpdate();
    }

    public int getColor() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getColor() : this.color;
    }

    public void setColor(int color) {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.setColor(color);
            return;
        }

        this.color = color;
        notifyUpdate();
    }

    public DisplayMode getMode() {
        NixieBargraphEntity master = getMaster();
        return master != null && master != this ? master.getMode() : this.mode;
    }

    public void toggleMode() {
        NixieBargraphEntity master = getMaster();
        if (master != null && master != this) {
            master.toggleMode();
            return;
        }

        this.mode = (this.mode == DisplayMode.BAR) ? DisplayMode.DOT : DisplayMode.BAR;
        notifyUpdate();
    }

    private void notifyUpdate() {
        setChanged();
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

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
        tag.putInt("Color", this.color);
        tag.putInt("Mode", this.mode.ordinal());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.targetLevel = tag.getFloat("TargetLevel");

        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            this.currentLevel = tag.getFloat("CurrentLevel");
        } else if (this.currentLevel == 0.0f && !tag.contains("Initialized")) {
            this.currentLevel = this.targetLevel;
        }

        this.redlineThreshold = tag.contains("RedlineThreshold") ? tag.getFloat("RedlineThreshold") : 0.9f;
        this.color = tag.contains("Color") ? tag.getInt("Color") : 0xFF8400;
        this.mode = tag.contains("Mode") ? DisplayMode.values()[tag.getInt("Mode")] : DisplayMode.BAR;
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