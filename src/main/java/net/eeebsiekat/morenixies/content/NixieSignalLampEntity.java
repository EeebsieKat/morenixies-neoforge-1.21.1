package net.eeebsiekat.morenixies.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;

public class NixieSignalLampEntity extends BlockEntity {

    public NixieSignalLampEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_SIGNAL_LAMP.get(), pos, state);
    }

    public boolean isLit() {
        return this.getBlockState().getValue(NixieSignalLampBlock.LIT);
    }

    public void displayLinkUpdate(String text) {
        if (level == null || level.isClientSide) return;

        // Direct boolean check: ON if "1", OFF if "0"
        boolean shouldBeLit = text.trim().equals("1");

        BlockState currentState = getBlockState();

        if (currentState.hasProperty(NixieSignalLampBlock.LIT) && currentState.getValue(NixieSignalLampBlock.LIT) != shouldBeLit) {
            level.setBlock(getBlockPos(), currentState.setValue(NixieSignalLampBlock.LIT, shouldBeLit), 3);
            setChanged();
        }
    }
}