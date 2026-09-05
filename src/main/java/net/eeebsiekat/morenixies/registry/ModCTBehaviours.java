package net.eeebsiekat.morenixies.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.SimpleCTBehaviour;
import net.minecraft.resources.ResourceLocation;

public class ModCTBehaviours {
    public static final CTSpriteShiftEntry NIXIE_CASING_CT = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            ResourceLocation.fromNamespaceAndPath("morenixies", "block/nixie_casing"),
            ResourceLocation.fromNamespaceAndPath("morenixies", "block/nixie_casing_connected")
    );

    public static ConnectedTextureBehaviour nixieCasing() {
        return new SimpleCTBehaviour(NIXIE_CASING_CT);
    }

    public static final CTSpriteShiftEntry NIXIE_OSCILLOSCOPE_CT = CTSpriteShifter.getCT(
            AllCTTypes.OMNIDIRECTIONAL,
            ResourceLocation.fromNamespaceAndPath("morenixies", "block/nixie_oscilloscope"),
            ResourceLocation.fromNamespaceAndPath("morenixies", "block/nixie_oscilloscope_connected")
    );

    public static ConnectedTextureBehaviour nixieOscilloscope() {
        return new SimpleCTBehaviour(NIXIE_OSCILLOSCOPE_CT);
    }
}