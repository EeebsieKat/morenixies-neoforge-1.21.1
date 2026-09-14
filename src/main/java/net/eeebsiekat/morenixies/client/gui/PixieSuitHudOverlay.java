package net.eeebsiekat.morenixies.client.gui;

import net.eeebsiekat.morenixies.content.PixieChassisItem;
import net.eeebsiekat.morenixies.registry.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PixieSuitHudOverlay implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack chassis = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chassis.is(ModItems.PIXIE_CHASSIS.get())) return;

        int currentAir = PixieChassisItem.getAir(chassis);
        int maxAir = PixieChassisItem.MAX_AIR_CAPACITY;
        float airPercent = (float) currentAir / maxAir;

        int x = guiGraphics.guiWidth() - 100;
        int y = guiGraphics.guiHeight() - 40;

        guiGraphics.drawString(mc.font, "Air: " + (int) (airPercent * 100) + "%", x, y, 0x55FFFF, true);
    }
}