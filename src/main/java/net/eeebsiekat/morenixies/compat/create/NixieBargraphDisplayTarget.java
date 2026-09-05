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

            // Parse progress or raw percentages from source text
            float fillRatio = parseFillRatio(fullText);
            bargraph.setTargetLevel(fillRatio);
        }
    }

    private float parseFillRatio(String input) {
        if (input.isEmpty()) return 0.0f;

        // Clean out extra characters except numbers, dots, and slashes
        String cleaned = input.replaceAll("[^0-9./%]", "");

        try {
            // Handle fractional formats like "1200/4000 mB"
            if (cleaned.contains("/")) {
                String[] parts = cleaned.split("/");
                float current = Float.parseFloat(parts[0]);
                float max = Float.parseFloat(parts[1]);
                return max > 0 ? current / max : 0.0f;
            }

            // Handle standard percentage strings like "75%"
            if (cleaned.contains("%")) {
                cleaned = cleaned.replace("%", "");
                return Float.parseFloat(cleaned) / 100.0f;
            }

            // Fallback for raw floating-point numbers (0.0 to 1.0 or up to 100)
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