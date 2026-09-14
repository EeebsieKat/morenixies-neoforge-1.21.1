package net.eeebsiekat.morenixies.registry;

import net.eeebsiekat.morenixies.MoreNixies;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MoreNixies.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MoreNixies.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> NECTAR_FLUID_TYPE = FLUID_TYPES.register("nectar",
            () -> new FluidType(FluidType.Properties.create().descriptionId("fluid.morenixies.nectar")));

    private static final BaseFlowingFluid.Properties NECTAR_PROPERTIES = new BaseFlowingFluid.Properties(
            NECTAR_FLUID_TYPE,
            () -> ModFluids.SOURCE_NECTAR.get(),
            () -> ModFluids.FLOWING_NECTAR.get()
    )
            .block(() -> ModBlocks.NECTAR_BLOCK.get())
            .bucket(() -> ModItems.NECTAR_BUCKET.get());

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> SOURCE_NECTAR = FLUIDS.register("nectar",
            () -> new BaseFlowingFluid.Source(NECTAR_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_NECTAR = FLUIDS.register("flowing_nectar",
            () -> new BaseFlowingFluid.Flowing(NECTAR_PROPERTIES));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}