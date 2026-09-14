package net.eeebsiekat.morenixies.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class pixie_suit<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("morenixies", "textures/entity/equipment/humanoid/pixie_suit.png");

    public static final ResourceLocation EMISSIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("morenixies", "textures/entity/equipment/humanoid/pixie_suit_e.png");

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("morenixies", "pixie_suit"), "main"
    );

    private final ModelPart mask;
    private final ModelPart fin1;
    private final ModelPart fin2;
    private final ModelPart backpack;
    private final ModelPart gears;

    // Kept public so PixieSuitArmorItem can toggle their visibility
    public final ModelPart rightBoot;
    public final ModelPart leftBoot;

    public pixie_suit(ModelPart root) {
        super(root, RenderType::entityTranslucent);

        this.mask = this.head.getChild("Mask");
        this.fin1 = this.head.getChild("fin1");
        this.fin2 = this.head.getChild("fin2");
        this.backpack = this.body.getChild("Backpack");
        this.gears = this.backpack.getChild("gears");

        // Retreive boot parts from their respective leg parents
        this.rightBoot = this.rightLeg.getChild("right_boot");
        this.leftBoot = this.leftLeg.getChild("left_boot");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(54, 74).addBox(-4.0F, -2.0F, -1.0F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Head_r1 = head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(0, 104).addBox(-0.5F, -2.5F, -3.0F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -5.25F, 0.75F, 0.0076F, 0.0869F, 0.0876F));
        PartDefinition Head_r2 = head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(0, 104).addBox(-0.5F, -2.5F, -3.0F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -5.25F, 0.75F, 0.0076F, -0.0869F, -0.0876F));

        PartDefinition Mask = head.addOrReplaceChild("Mask", CubeListBuilder.create().texOffs(0, 91).addBox(-2.5F, -5.954F, -1.4115F, 1.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(1, 92).addBox(-2.5F, 2.046F, -1.4115F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 104).addBox(-0.5F, 2.546F, -1.4115F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(1, 92).addBox(1.5F, 2.046F, -1.4115F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 91).addBox(1.5F, -5.954F, -1.4115F, 1.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(42, 92).addBox(0.5F, -4.754F, -1.2115F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(50, 92).addBox(-0.5F, -4.754F, -0.8615F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(42, 92).addBox(-1.5F, -4.754F, -1.2115F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(42, 88).addBox(-1.5F, -5.754F, -1.2115F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 91).addBox(-1.5F, 1.246F, -1.2115F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 99).addBox(-3.0F, -5.604F, -0.5115F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.546F, -4.5885F));

        PartDefinition cube_r1 = Mask.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(1, 92).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.8876F, 2.6925F, -0.4115F, 0.0F, 0.0F, 1.309F));
        PartDefinition cube_r2 = Mask.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 92).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8876F, 2.6925F, -0.4115F, 0.0F, 0.0F, -1.309F));

        PartDefinition fin1 = head.addOrReplaceChild("fin1", CubeListBuilder.create().texOffs(90, 44).addBox(-0.5F, -0.5F, -0.7071F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(90, 46).addBox(-0.25F, -5.4497F, 4.2426F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-3.0F, -8.0F, -2.5F, -0.7854F, 0.0F, -0.1309F));
        PartDefinition cube_r3 = fin1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(87, 25).addBox(0.0F, 0.0F, -7.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 5.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r4 = fin1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(84, 44).addBox(-2.0F, -6.5F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -0.5F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition fin2 = head.addOrReplaceChild("fin2", CubeListBuilder.create().texOffs(90, 46).addBox(-0.75F, -5.4497F, 4.2426F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(90, 48).addBox(-1.5F, -0.5F, -0.7071F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -8.0F, -2.5F, -0.7854F, 0.0F, 0.1309F));
        PartDefinition cube_r5 = fin2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(89, 51).addBox(-2.0F, -6.5F, -0.5F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.5F, 0.0F, -0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r6 = fin2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(87, 25).addBox(0.0F, 0.0F, -7.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 5.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(38, 81).addBox(-4.0F, 1.5F, -3.0F, 8.0F, 5.0F, 2.0F, new CubeDeformation(0.2F))
                .texOffs(58, 81).addBox(-4.0F, 2.25F, -3.3F, 8.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(39, 81).addBox(-4.0F, 0.5F, -2.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.2F))
                .texOffs(12, 58).addBox(-1.5F, 9.9F, -2.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 87).addBox(-2.5F, 9.9F, -2.2F, 5.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(33, 1).addBox(-4.0F, 6.9F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(16, 85).addBox(-8.0F, -1.0F, -2.0F, 9.0F, 3.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-3.3F, 10.5F, 0.0F, 0.0F, 0.0F, -1.309F));
        PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 92).addBox(-1.0F, -1.0F, -2.0F, 9.0F, 3.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(3.3F, 10.5F, 0.0F, 0.0F, 0.0F, 1.309F));
        PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(60, 0).addBox(-2.0F, -2.0F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9F, 10.8F, 1.8F, 0.0984F, 0.1228F, 0.0876F));
        PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(60, 0).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9F, 10.8F, 1.8F, 0.0984F, -0.1228F, -0.0876F));
        PartDefinition cube_r11 = body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(74, 7).addBox(0.0004F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9062F, 8.4861F, 1.0F, -0.3927F, 0.0F, -0.48F));
        PartDefinition cube_r12 = body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(68, 63).addBox(0.0004F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9062F, 5.9861F, 1.0F, -0.3927F, 0.0F, -0.48F));
        PartDefinition cube_r13 = body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(26, 68).addBox(-2.9996F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8938F, 5.9861F, 1.0F, -0.3927F, 0.0F, 0.48F));
        PartDefinition cube_r14 = body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(74, 0).addBox(-1.9996F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8938F, 8.4861F, 1.0F, -0.3927F, 0.0F, 0.48F));
        PartDefinition Body_r1 = body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(39, 81).addBox(-4.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 1.3F, -2.21F, 0.7854F, 0.0F, 0.0F));

        PartDefinition Backpack = body.addOrReplaceChild("Backpack", CubeListBuilder.create().texOffs(82, 16).addBox(-6.1F, -1.0F, 2.1F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(82, 0).addBox(0.1F, -1.0F, 2.1F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(81, 35).addBox(-4.1F, -2.0F, 4.1F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(81, 39).addBox(-5.1F, -3.0F, 3.1F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition gears = Backpack.addOrReplaceChild("gears", CubeListBuilder.create().texOffs(100, 16).addBox(-2.0F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 16).addBox(1.0F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(74, 32).addBox(-2.5F, -0.5F, 2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.1F, 3.5F, 5.1F));

        PartDefinition cube_r15 = gears.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(100, 16).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 16).addBox(-3.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 3.0F, -1.5708F, 0.0F, 0.0F));
        PartDefinition cube_r16 = gears.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(100, 16).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 16).addBox(-3.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 3.0F, -2.3562F, 0.0F, 0.0F));
        PartDefinition cube_r17 = gears.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(100, 16).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 18).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(82, 18).addBox(-3.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.1F))
                .texOffs(100, 16).addBox(-3.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 3.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition cube_r18 = right_arm.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(38, 56).addBox(-4.0F, -1.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-0.5F, 1.0F, 0.0F, 0.0F, 0.0F, 0.2182F));
        PartDefinition cube_r19 = right_arm.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 50).addBox(-6.0F, -1.0F, -2.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 40).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition cube_r20 = left_arm.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(56, 7).addBox(-1.0F, -1.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.5F, 1.0F, 0.0F, 0.0F, 0.0F, -0.2182F));
        PartDefinition cube_r21 = left_arm.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, -1.0F, -2.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 56).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition cube_r22 = right_leg.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(74, 7).addBox(0.0004F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0062F, 9.4861F, 1.0F, -0.3927F, 0.0F, -0.48F));
        PartDefinition cube_r23 = right_leg.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(68, 63).addBox(0.0004F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0062F, 6.9861F, 1.0F, -0.3927F, 0.0F, -0.48F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(56, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition cube_r24 = left_leg.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(74, 0).addBox(-1.9996F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9938F, 9.4861F, 1.0F, -0.3927F, 0.0F, 0.48F));
        PartDefinition cube_r25 = left_leg.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(26, 68).addBox(-2.9996F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9938F, 6.9861F, 1.0F, -0.3927F, 0.0F, 0.48F));

        // Parented directly to right_leg and left_leg with PartPose.ZERO
        PartDefinition right_boot = right_leg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(56, 55).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.ZERO);
        PartDefinition cube_r26 = right_boot.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(72, 26).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.15F, -2.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition left_boot = left_leg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(56, 47).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.ZERO);
        PartDefinition cube_r27 = left_boot.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(72, 26).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.15F, -2.0F, 0.0F, 0.0F, -0.7854F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // Manual copyFrom removed: child parts inherit parent animations automatically
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        var bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource();

        VertexConsumer baseConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderModelParts(poseStack, baseConsumer, packedLight, packedOverlay, color);

        VertexConsumer glowConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(EMISSIVE_TEXTURE));

        // Lightmap coordinate: Block light 12, Sky light 12 (0x00C000C0) for a softer glow instead of max bright
        int softGlowLight = 0x00F000F0;

        // Peachy-Orange Tint (ARGB: Alpha=255, Red=255, Green=171, Blue=118)
        int peachColor = 0xFFFFAB76;

        renderModelParts(poseStack, glowConsumer, softGlowLight, packedOverlay, peachColor);
    }

    // Helper method to keep visibility logic clean and DRY
    private void renderModelParts(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        if (this.head.visible) this.head.render(poseStack, consumer, light, overlay, color);
        if (this.body.visible) this.body.render(poseStack, consumer, light, overlay, color);
        if (this.rightArm.visible) this.rightArm.render(poseStack, consumer, light, overlay, color);
        if (this.leftArm.visible) this.leftArm.render(poseStack, consumer, light, overlay, color);

        if (this.rightLeg.visible) {
            if (!this.rightBoot.visible) {
                boolean prevRightBootVis = this.rightBoot.visible;
                boolean prevLeftBootVis = this.leftBoot.visible;
                this.rightBoot.visible = false;
                this.leftBoot.visible = false;

                this.rightLeg.render(poseStack, consumer, light, overlay, color);
                this.leftLeg.render(poseStack, consumer, light, overlay, color);

                this.rightBoot.visible = prevRightBootVis;
                this.leftBoot.visible = prevLeftBootVis;
            } else {
                this.rightLeg.render(poseStack, consumer, light, overlay, color);
                this.leftLeg.render(poseStack, consumer, light, overlay, color);
            }
        }
    }
}