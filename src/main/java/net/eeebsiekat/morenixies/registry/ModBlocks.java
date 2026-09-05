package net.eeebsiekat.morenixies.registry;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.tterrag.registrate.util.entry.BlockEntry;

public class ModBlocks {
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MoreNixies.MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    public static final BlockEntry<NixieCasingBlock> NIXIE_CASING = REGISTRATE
            .block("nixie_casing", NixieCasingBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE).sound(SoundType.WOOD).strength(2.0F))
            .onRegister(CreateRegistrate.connectedTextures(ModCTBehaviours::nixieCasing))
            .item()
            .build()
            .register();

    public static final BlockEntry<NixieOscilloscopeBlock> NIXIE_OSCILLOSCOPE = REGISTRATE
            .block("nixie_oscilloscope", NixieOscilloscopeBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.METAL).strength(2.0F).noOcclusion())
            .onRegister(CreateRegistrate.connectedTextures(ModCTBehaviours::nixieOscilloscope))
            .item()
            .build()
            .register();

    public static void register() {}

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

    public static final DeferredBlock<Block> NIXIE_BARGRAPH = BLOCKS.register("nixie_bargraph",
            () -> new NixieBargraphBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
                    .lightLevel(state -> 10)
                    .strength(1.5f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
            ));

    public static final DeferredBlock<Block> NIXIE_FLIGHT_HUD = BLOCKS.register("nixie_flight_hud",
            () -> new NixieFlightHudBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
                    .lightLevel(state -> 10)
                    .strength(1.5f, 3.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.GLASS)
            ));
}