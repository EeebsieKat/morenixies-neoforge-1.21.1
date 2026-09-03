package net.eeebsiekat.morenixies.registry;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.registry.CreateRegistries;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.compat.create.NixieSignalLampDisplayTarget;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDisplayTargets {
    public static final DeferredRegister<DisplayTarget> DISPLAY_TARGETS =
            DeferredRegister.create(CreateRegistries.DISPLAY_TARGET, MoreNixies.MOD_ID);

    public static final DeferredHolder<DisplayTarget, NixieSignalLampDisplayTarget> NIXIE_SIGNAL_LAMP =
            DISPLAY_TARGETS.register("nixie_signal_lamp", NixieSignalLampDisplayTarget::new);
}