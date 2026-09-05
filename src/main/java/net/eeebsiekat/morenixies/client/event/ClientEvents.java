package net.eeebsiekat.morenixies.client.event;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.client.render.NixieBargraphRenderer;
import net.eeebsiekat.morenixies.client.render.NixieFlightHudRenderer;
import net.eeebsiekat.morenixies.client.render.NixieSignalLampRenderer;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.eeebsiekat.morenixies.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
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
        event.registerBlockEntityRenderer(
                ModBlockEntities.NIXIE_BARGRAPH.get(),
                NixieBargraphRenderer::new);

        event.registerBlockEntityRenderer(
                ModBlockEntities.NIXIE_FLIGHT_HUD.get(),
                NixieFlightHudRenderer::new);

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_SIGNAL_LAMP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_BARGRAPH.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_FLIGHT_HUD.get(), RenderType.translucent());
    }
}
