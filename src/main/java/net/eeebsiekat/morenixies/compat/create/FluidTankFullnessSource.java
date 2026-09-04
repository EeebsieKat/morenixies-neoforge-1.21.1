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

import java.util.ArrayList;
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
                int thresholdStep = context.sourceConfig().getInt("Threshold"); // 0 to 10 step index
                float thresholdPercent = thresholdStep * 10.0f;

                float currentAmount = tank.getFluidAmount();
                float totalCapacity = tank.getCapacity();
                float fillRatio = (currentAmount / totalCapacity) * 100.0f;

                boolean active;
                if (modeIndex == 1) {
                    // Mode 1: From Full -> ON if fill ratio is >= threshold percentage
                    active = fillRatio >= thresholdPercent;
                } else {
                    // Mode 0: From Empty -> ON if fill ratio is <= threshold percentage
                    active = fillRatio <= thresholdPercent;
                }

                return Component.literal(active ? "!" : "0");
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
        if (isFirstLine) {
            // Line 1: Condition selector
            builder.addSelectionScrollInput(0, 120,
                    (si, l) -> si.forOptions(List.of(
                            Component.literal("From Empty"),
                            Component.literal("From Full")
                    )).titled(Component.literal("Signal Condition")),
                    "Mode"
            );
        } else {
            // Line 2: Percentage threshold selector
            List<Component> percentOptions = new ArrayList<>();
            for (int i = 0; i <= 100; i += 10) {
                percentOptions.add(Component.literal(i + "%"));
            }

            builder.addSelectionScrollInput(0, 120,
                    (si, l) -> si.forOptions(percentOptions)
                            .titled(Component.literal("Threshold %")),
                    "Threshold"
            );
        }
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return false;
    }
}