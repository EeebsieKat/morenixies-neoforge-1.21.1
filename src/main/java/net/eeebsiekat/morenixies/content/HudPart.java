package net.eeebsiekat.morenixies.content;

import net.minecraft.util.StringRepresentable;

public enum HudPart implements StringRepresentable {
    SINGLE("single", 0.0f),
    MIDDLE("middle", 0.0f),
    SMALL_SIDE_LEFT("small_side_left", 22.5f),
    SMALL_SIDE_RIGHT("small_side_right", -22.5f),
    SMALL_SIDE_LEFT_END("small_side_left_end", 22.5f),
    SMALL_SIDE_RIGHT_END("small_side_right_end", -22.5f),
    LARGE_SIDE_LEFT("large_side_left", 45.0f),
    LARGE_SIDE_RIGHT("large_side_right", -45.0f);

    private final String name;
    private final float yAngleOffset;

    HudPart(String name, float yAngleOffset) {
        this.name = name;
        this.yAngleOffset = yAngleOffset;
    }

    public float getYAngleOffset() {
        return yAngleOffset;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}