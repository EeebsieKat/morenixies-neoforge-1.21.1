package net.eeebsiekat.morenixies.client.event;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.registry.ModDataComponents;
import net.eeebsiekat.morenixies.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

@EventBusSubscriber(modid = MoreNixies.MOD_ID)
public class ModCapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidHandlerItemStack(ModDataComponents.FLUID_TANK, stack, 4000),
                ModItems.PIXIE_CHASSIS.get()
        );
    }
}