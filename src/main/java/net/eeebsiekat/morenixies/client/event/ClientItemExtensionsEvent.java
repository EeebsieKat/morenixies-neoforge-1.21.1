package net.eeebsiekat.morenixies.client.event;

import net.eeebsiekat.morenixies.client.model.pixie_suit;
import net.eeebsiekat.morenixies.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = "morenixies")
public class ClientItemExtensionsEvent {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions armorExtension = new IClientItemExtensions() {
            private pixie_suit<LivingEntity> model;

            public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("morenixies", "textures/entity/equipment/humanoid/pixie_suit.png");

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.model == null) {
                    this.model = new pixie_suit<>(
                            Minecraft.getInstance().getEntityModels().bakeLayer(pixie_suit.LAYER_LOCATION)
                    );
                }

                // Copy vanilla animations (walking, crouching, head movement)
                original.copyPropertiesTo((HumanoidModel) this.model);

                // Toggle visibility based on equipped slot
                this.model.head.visible = (equipmentSlot == EquipmentSlot.HEAD);
                this.model.body.visible = (equipmentSlot == EquipmentSlot.CHEST);
                this.model.rightArm.visible = (equipmentSlot == EquipmentSlot.CHEST);
                this.model.leftArm.visible = (equipmentSlot == EquipmentSlot.CHEST);

                // Keep parent leg roots active for both LEGS and FEET slots
                boolean isLegs = (equipmentSlot == EquipmentSlot.LEGS);
                boolean isFeet = (equipmentSlot == EquipmentSlot.FEET);

                this.model.rightLeg.visible = isLegs || isFeet;
                this.model.leftLeg.visible = isLegs || isFeet;

                // Control child boot visibility independently
                this.model.rightBoot.visible = isFeet;
                this.model.leftBoot.visible = isFeet;

                return this.model;
            }
        };

        // Register the extension for your items
        event.registerItem(armorExtension,
                ModItems.PIXIE_HELM.get(),
                ModItems.PIXIE_CHASSIS.get(),
                ModItems.PIXIE_SERVOS.get(),
                ModItems.PIXIE_GROUNDERS.get()
        );
    }
}