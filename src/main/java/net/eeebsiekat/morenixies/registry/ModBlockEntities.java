package net.eeebsiekat.morenixies.registry;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.NixieBargraphEntity;
import net.eeebsiekat.morenixies.content.NixieSignalLampEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MoreNixies.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NixieSignalLampEntity>> NIXIE_SIGNAL_LAMP =
            BLOCK_ENTITIES.register("nixie_signal_lamp",
                    () -> BlockEntityType.Builder.of(NixieSignalLampEntity::new, ModBlocks.NIXIE_SIGNAL_LAMP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NixieBargraphEntity>> NIXIE_BARGRAPH =
            BLOCK_ENTITIES.register("nixie_bargraph",
                    () -> BlockEntityType.Builder.of(NixieBargraphEntity::new, ModBlocks.NIXIE_BARGRAPH.get()).build(null));
}