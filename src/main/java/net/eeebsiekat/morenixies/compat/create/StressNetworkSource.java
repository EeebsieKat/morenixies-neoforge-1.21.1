package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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

public class StressNetworkSource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (context.getSourceBlockEntity() instanceof KineticBlockEntity kineticBE && kineticBE.hasNetwork()) {
            var network = kineticBE.getOrCreateNetwork();
            float capacity = network.calculateCapacity();
            float stress = network.calculateStress();

            if (capacity > 0) {
                float stressUsageRatio = (stress / capacity) * 100.0f;

                // If targeting a Bargraph, output continuous percentage
                if (context.getTargetBlockEntity() instanceof NixieBargraphEntity) {
                    return Component.literal((int) stressUsageRatio + "%");
                }

                // If targeting a Signal Lamp, output threshold check result
                if (context.getTargetBlockEntity() instanceof NixieSignalLampEntity) {
                    int modeIndex = context.sourceConfig().getInt("Mode");
                    int thresholdStep = context.sourceConfig().contains("Threshold") ? context.sourceConfig().getInt("Threshold") : 0;
                    float thresholdPercent = thresholdStep * 10.0f;

                    boolean active = (modeIndex == 0) ? (stressUsageRatio >= thresholdPercent) : (stressUsageRatio <= thresholdPercent);
                    return Component.literal(active ? "!" : "0");
                }

                // Default continuous fallback
                return Component.literal((int) stressUsageRatio + "%");
            }
        }
        return Component.literal("0%");
    }

    @Override
    protected String getTranslationKey() {
        return "stress_network";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) {
            builder.addSelectionScrollInput(0, 120,
                    (si, l) -> si.forOptions(List.of(
                            Component.literal("Stress Used"),
                            Component.literal("Stress Available")
                    )).titled(Component.literal("Check Type")),
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