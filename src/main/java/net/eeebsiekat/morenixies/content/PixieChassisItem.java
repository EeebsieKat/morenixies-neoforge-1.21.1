package net.eeebsiekat.morenixies.content;

import net.eeebsiekat.morenixies.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PixieChassisItem extends ArmorItem {
    public static final int MAX_AIR_CAPACITY = 9000; // ~15 minutes

    public PixieChassisItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static int getAir(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AIR_SUPPLY.get(), 0);
    }

    public static void setAir(ItemStack stack, int air) {
        stack.set(ModDataComponents.AIR_SUPPLY.get(), Math.min(Math.max(0, air), MAX_AIR_CAPACITY));
    }

    public static boolean consumeAir(ItemStack stack, int amount) {
        int current = getAir(stack);
        if (current >= amount) {
            setAir(stack, current - amount);
            return true;
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Check if player is wearing the chassis in the chest slot
            if (player.getItemBySlot(EquipmentSlot.CHEST) == stack) {
                // If underwater or drowning, replenish player air supply using backtank air
                if (player.getAirSupply() < player.getMaxAirSupply()) {
                    if (consumeAir(stack, 1)) {
                        player.setAirSupply(player.getMaxAirSupply());
                    }
                }
            }
        }
    }
}