package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class FluidTankFullnessSource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceBlockEntity() instanceof FluidTankBlockEntity tankBE) {
            FluidTankBlockEntity controller = tankBE.getControllerBE();
            if (controller != null) {
                tankBE = controller;
            }

            var tank = tankBE.getTankInventory();
            if (tank != null && tank.getCapacity() > 0) {
                int modeIndex = context.sourceConfig().getInt("Mode");
                int currentAmount = tank.getFluidAmount();
                int totalCapacity = tank.getCapacity();

                boolean active;
                if (modeIndex == 1) {
                    // Mode 1: From Full -> ON if tank is FULL
                    active = currentAmount >= totalCapacity;
                } else {
                    // Mode 0: From Empty -> ON if tank is NOT EMPTY
                    active = currentAmount > 0;
                }

                return Component.literal(active ? "1" : "0");
            }
        }
        return Component.literal("0");
    }

    @Override
    protected String getTranslationKey() {
        return "fluid_tank_fullness";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine)
            return; // Prevents SingleLineDisplaySource from generating a duplicate line widget

        builder.addSelectionScrollInput(0, 120,
                (si, l) -> si.forOptions(List.of(
                        Component.literal("From Empty"),
                        Component.literal("From Full")
                )).titled(Component.literal("Signal Condition")),
                "Mode"
        );
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false; // Keeps the UI clean without text input lines
    }
}