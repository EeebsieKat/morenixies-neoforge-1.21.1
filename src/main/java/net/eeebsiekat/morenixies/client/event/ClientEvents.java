package net.eeebsiekat.morenixies.client.event;

import com.simibubi.create.CreateClient;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.client.model.pixie_suit;
import net.eeebsiekat.morenixies.client.render.NixieBargraphRenderer;
import net.eeebsiekat.morenixies.client.render.NixieFlightHudRenderer;
import net.eeebsiekat.morenixies.client.render.NixieOscilloscopeRenderer;
import net.eeebsiekat.morenixies.client.render.NixieSignalLampRenderer;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.eeebsiekat.morenixies.registry.ModBlocks;
import net.eeebsiekat.morenixies.registry.ModCTBehaviours;
import net.eeebsiekat.morenixies.registry.ModItems;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

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

        event.registerBlockEntityRenderer(
                ModBlockEntities.NIXIE_OSCILLOSCOPE.get(),
                NixieOscilloscopeRenderer::new
        );

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_OSCILLOSCOPE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_SIGNAL_LAMP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_BARGRAPH.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.NIXIE_FLIGHT_HUD.get(), RenderType.translucent());

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

        });
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register the layer definition from your custom model class
        event.registerLayerDefinition(pixie_suit.LAYER_LOCATION, pixie_suit::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerModel<?> model = event.getRenderer().getModel();

        boolean hasLeggings = player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.PIXIE_SERVOS.get());
        boolean hasBoots = player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.PIXIE_GROUNDERS.get());

        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PIXIE_HELM.get())) {
            model.hat.visible = false;
        }

        if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.PIXIE_CHASSIS.get())) {
            model.jacket.visible = false;
            model.rightSleeve.visible = false;
            model.leftSleeve.visible = false;
        }

        if (hasLeggings && hasBoots) {
            model.rightPants.visible = false;
            model.leftPants.visible = false;
        }
    }
}
