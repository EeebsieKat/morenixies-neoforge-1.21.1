package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.eeebsiekat.morenixies.content.NixieBargraphEntity;
import net.eeebsiekat.morenixies.content.NixieSignalLampEntity;
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
            if (controller != null) tankBE = controller;

            var tank = tankBE.getTankInventory();
            if (tank != null && tank.getCapacity() > 0) {
                float currentAmount = tank.getFluidAmount();
                float totalCapacity = tank.getCapacity();

                // If targeting a Bargraph, always return continuous fraction data
                if (context.getTargetBlockEntity() instanceof NixieBargraphEntity) {
                    return Component.literal((int) currentAmount + "/" + (int) totalCapacity);
                }

                // If targeting a Signal Lamp, perform threshold calculation
                if (context.getTargetBlockEntity() instanceof NixieSignalLampEntity) {
                    int modeIndex = context.sourceConfig().getInt("Mode");
                    int thresholdStep = context.sourceConfig().getInt("Threshold");
                    float thresholdPercent = thresholdStep * 10.0f;
                    float fillRatio = (currentAmount / totalCapacity) * 100.0f;

                    boolean active = (modeIndex == 1) ? (fillRatio >= thresholdPercent) : (fillRatio <= thresholdPercent);
                    return Component.literal(active ? "!" : "0");
                }

                // Default fallback to fraction format for other targets
                return Component.literal((int) currentAmount + "/" + (int) totalCapacity);
            }
        }
        return Component.literal("0/100");
    }

    @Override
    protected String getTranslationKey() {
        return "fluid_tank_fullness";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            builder.addSelectionScrollInput(0, 120,
                    (si, l) -> si.forOptions(List.of(
                            Component.literal("From Empty"),
                            Component.literal("From Full")
                    )).titled(Component.literal("Signal Condition")),
                    "Mode"
            );
        } else {
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