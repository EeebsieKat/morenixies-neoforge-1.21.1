package net.eeebsiekat.morenixies.registry;

import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.PixieSuitArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
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

    public static final DeferredItem<Item> PIXE = ITEMS.register("pixe",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<PixieSuitArmorItem> PIXIE_HELM = ITEMS.register("pixie_helm",
            () -> new PixieSuitArmorItem(ModArmorMaterials.SUIT_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(42))));
    public static final DeferredItem<PixieSuitArmorItem> PIXIE_CHASSIS = ITEMS.register("pixie_chassis",
            () -> new PixieSuitArmorItem(ModArmorMaterials.SUIT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(42))));
    public static final DeferredItem<PixieSuitArmorItem> PIXIE_SERVOS = ITEMS.register("pixie_servos",
            () -> new PixieSuitArmorItem(ModArmorMaterials.SUIT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(42))));
    public static final DeferredItem<PixieSuitArmorItem> PIXIE_GROUNDERS = ITEMS.register("pixie_grounders",
            () -> new PixieSuitArmorItem(ModArmorMaterials.SUIT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(42))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}