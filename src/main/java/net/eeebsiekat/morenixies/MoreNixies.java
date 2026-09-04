package net.eeebsiekat.morenixies;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import net.eeebsiekat.morenixies.compat.create.NixieSignalLampDisplayTarget;
import net.eeebsiekat.morenixies.registry.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(MoreNixies.MOD_ID)
public class MoreNixies {
    public static final String MOD_ID = "morenixies";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("morenixies_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.NIXIE_SIGNAL_LAMP.get()))
                    .title(Component.translatable("creativetab.morenixies_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.NIXIE_SIGNAL_LAMP.get());
                    })
                    .build()
    );

    public MoreNixies(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModDisplaySources.DISPLAY_SOURCES.register(modEventBus);
        ModDisplayTargets.DISPLAY_TARGETS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Source binding
            var fluidTankType = BuiltInRegistries.BLOCK_ENTITY_TYPE
                    .get(ResourceLocation.fromNamespaceAndPath("create", "fluid_tank"));

            if (fluidTankType != null) {
                DisplaySource.BY_BLOCK_ENTITY.add(
                        fluidTankType,
                        ModDisplaySources.FLUID_TANK_FULLNESS.get()
                );
            }

            BuiltInRegistries.BLOCK_ENTITY_TYPE.keySet().stream()
                    .filter(key -> key.getNamespace().equals("create"))
                    .map(BuiltInRegistries.BLOCK_ENTITY_TYPE::get)
                    .filter(java.util.Objects::nonNull)
                    .forEach(type ->
                            DisplaySource.BY_BLOCK_ENTITY.add(type, ModDisplaySources.STRESS_NETWORK.get())
                    );


            // Target binding - Uses .register() for SimpleRegistry
            DisplayTarget.BY_BLOCK_ENTITY.register(
                    ModBlockEntities.NIXIE_SIGNAL_LAMP.get(),
                    ModDisplayTargets.NIXIE_SIGNAL_LAMP.get()
            );

            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.NIXIE_SIGNAL_LAMP.get(),
                    RenderType.translucent()
            );
        });
    }
}