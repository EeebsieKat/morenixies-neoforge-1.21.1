package net.eeebsiekat.morenixies.client.event;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.client.render.NixieSignalLampRenderer;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MoreNixies.MOD_ID)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.NIXIE_SIGNAL_LAMP.get(),
                NixieSignalLampRenderer::new
        );
    }
}
