package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.eeebsiekat.morenixies.content.NixieBargraphEntity;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class NixieBargraphDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        if (text == null || text.isEmpty()) return;

        if (context.getTargetBlockEntity() instanceof NixieBargraphEntity bargraph) {
            String fullText = text.stream()
                    .filter(c -> c != null)
                    .map(MutableComponent::getString)
                    .reduce("", (a, b) -> a + b)
                    .trim();

            float fillRatio = parseFillRatio(fullText);
            bargraph.setTargetLevel(fillRatio);
        }
    }

    private float parseFillRatio(String input) {
        if (input.isEmpty()) return 0.0f;

        String cleaned = input.replaceAll("[^0-9./%]", "");

        try {
            if (cleaned.contains("/")) {
                String[] parts = cleaned.split("/");
                float current = Float.parseFloat(parts[0]);
                float max = Float.parseFloat(parts[1]);
                return max > 0 ? current / max : 0.0f;
            }

            if (cleaned.contains("%")) {
                cleaned = cleaned.replace("%", "");
                return Float.parseFloat(cleaned) / 100.0f;
            }

            float val = Float.parseFloat(cleaned);
            return val > 1.0f ? val / 100.0f : val;

        } catch (NumberFormatException ignored) {
            return 0.0f;
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, 1, this);
    }
}