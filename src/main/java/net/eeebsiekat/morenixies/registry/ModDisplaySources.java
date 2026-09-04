package net.eeebsiekat.morenixies.registry;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.compat.create.FluidTankFullnessSource;
import net.eeebsiekat.morenixies.compat.create.StressNetworkSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDisplaySources {
    public static final DeferredRegister<DisplaySource> DISPLAY_SOURCES =
            DeferredRegister.create(CreateRegistries.DISPLAY_SOURCE, MoreNixies.MOD_ID);

    public static final DeferredHolder<DisplaySource, FluidTankFullnessSource> FLUID_TANK_FULLNESS =
            DISPLAY_SOURCES.register("fluid_tank_fullness", FluidTankFullnessSource::new);

    public static final DeferredHolder<DisplaySource, StressNetworkSource> STRESS_NETWORK =
            DISPLAY_SOURCES.register("stress_network", StressNetworkSource::new);

    public static void register(IEventBus modEventBus) {
        DISPLAY_SOURCES.register(modEventBus);
    }
}