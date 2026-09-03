package net.eeebsiekat.morenixies.compat.create;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.eeebsiekat.morenixies.content.NixieSignalLampEntity;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;

public class NixieSignalLampDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        if (text == null || text.isEmpty()) return;

        if (context.getTargetBlockEntity() instanceof NixieSignalLampEntity lamp) {
            String fullText = text.stream()
                    .filter(c -> c != null)
                    .map(MutableComponent::getString)
                    .reduce("", (a, b) -> a + b)
                    .trim();

            lamp.displayLinkUpdate(fullText);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, 1, this); // Adjust max line length as needed
    }
}