package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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

public class StressNetworkSource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceBlockEntity() instanceof KineticBlockEntity kineticBE) {
            if (kineticBE.hasNetwork()) {
                var network = kineticBE.getOrCreateNetwork();

                float capacity = network.calculateCapacity();
                float stress = network.calculateStress();

                if (capacity > 0) {
                    int modeIndex = context.sourceConfig().getInt("Mode");

                    // Read threshold safely with fallback
                    int thresholdStep = context.sourceConfig().contains("Threshold")
                            ? context.sourceConfig().getInt("Threshold")
                            : 0;

                    float thresholdPercent = thresholdStep * 10.0f;

                    // Calculate actual percentage (0.0% to 100.0%+)
                    float stressUsageRatio = (stress / capacity) * 100.0f;

                    boolean active;
                    if (modeIndex == 0) {
                        // Mode 0: Stress Used -> ON if current usage % >= target threshold %
                        active = stressUsageRatio >= thresholdPercent;
                    } else {
                        // Mode 1: Stress Available -> ON if current usage % <= target threshold %
                        active = stressUsageRatio <= thresholdPercent;
                    }

                    return Component.literal(active ? "!" : "0");
                }
            }
        }
        return Component.literal("0");
    }

    @Override
    protected String getTranslationKey() {
        return "stress_network";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            // Line 1 Widget: Mode selection
            builder.addSelectionScrollInput(0, 120,
                    (si, l) -> si.forOptions(List.of(
                            Component.literal("Stress Used"),
                            Component.literal("Stress Available")
                    )).titled(Component.literal("Check Type")),
                    "Mode"
            );
        } else {
            // Line 2 Widget: Percentage threshold selector
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