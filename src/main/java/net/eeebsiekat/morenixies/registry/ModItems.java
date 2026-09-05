package net.eeebsiekat.morenixies.registry;

import net.eeebsiekat.morenixies.MoreNixies;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoreNixies.MOD_ID);

    public static final DeferredItem<Item> NIXIE_SIGNAL_LAMP = ITEMS.register("nixie_signal_lamp",
            () -> new BlockItem(ModBlocks.NIXIE_SIGNAL_LAMP.get(), new Item.Properties()));

    public static final DeferredItem<Item> NIXIE_BARGRAPH = ITEMS.register("nixie_bargraph",
            () -> new BlockItem(ModBlocks.NIXIE_BARGRAPH.get(), new Item.Properties()));

    public static final DeferredItem<Item> NIXIE_FLIGHT_HUD = ITEMS.register("nixie_flight_hud",
            () -> new BlockItem(ModBlocks.NIXIE_FLIGHT_HUD.get(), new Item.Properties()));
}