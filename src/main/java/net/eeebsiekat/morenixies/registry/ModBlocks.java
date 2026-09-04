package net.eeebsiekat.morenixies.registry;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.NixieSignalLampBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MoreNixies.MOD_ID);

    public static final DeferredBlock<Block> NIXIE_SIGNAL_LAMP = BLOCKS.register("nixie_signal_lamp",
            () -> new NixieSignalLampBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
                    .lightLevel(state -> state.getValue(NixieSignalLampBlock.LIT) ? 15 : 10)
                    .strength(1.5f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
            ));
}