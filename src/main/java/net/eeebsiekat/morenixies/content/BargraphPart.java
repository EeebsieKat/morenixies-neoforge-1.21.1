package net.eeebsiekat.morenixies.content;

import net.minecraft.util.StringRepresentable;

public enum BargraphPart implements StringRepresentable {
    SINGLE("single"),
    START("start"),
    MIDDLE("middle"),
    END("end");

    private final String name;

    BargraphPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}