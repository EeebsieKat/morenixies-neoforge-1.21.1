// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class pixie_suit<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "pixie_suit"), "main");
	private final ModelPart Helmet;
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart Chestplate;
	private final ModelPart Right Arm;
	private final ModelPart Left Arm;
	private final ModelPart Leggings;
	private final ModelPart Right Legging;
	private final ModelPart Left Legging;
	private final ModelPart Boots;
	private final ModelPart Left Boot;
	private final ModelPart Right Boot;

	public pixie_suit(ModelPart root) {
		this.Helmet = root.getChild("Helmet");
		this.bone = this.Helmet.getChild("bone");
		this.bone2 = this.Helmet.getChild("bone2");
		this.bone3 = this.Helmet.getChild("bone3");
		this.Chestplate = root.getChild("Chestplate");
		this.Right Arm = this.Chestplate.getChild("Right Arm");
		this.Left Arm = this.Chestplate.getChild("Left Arm");
		this.Leggings = root.getChild("Leggings");
		this.Right Legging = this.Leggings.getChild("Right Legging");
		this.Left Legging = this.Leggings.getChild("Left Legging");
		this.Boots = root.getChild("Boots");
		this.Left Boot = this.Boots.getChild("Left Boot");
		this.Right Boot = this.Boots.getChild("Right Boot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Helmet = partdefinition.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 69).addBox(-4.45F, -7.75F, -4.25F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(72, 15).addBox(2.45F, -7.75F, -4.25F, 2.0F, 8.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(24, 16).addBox(-4.0F, -8.0F, -1.0F, 8.0F, 8.0F, 5.0F, new CubeDeformation(0.3F))
		.texOffs(48, 29).addBox(-5.0F, -2.0F, 2.0F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Helmet.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(72, 43).addBox(-2.0006F, -5.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(72, 35).addBox(-7.8996F, -5.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4501F, -7.75F, -4.25F, -0.5672F, 0.0F, 0.0F));

		PartDefinition bone = Helmet.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.5F, -3.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 29).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition bone2 = Helmet.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.5F, -3.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -1.5F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.25F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition bone3 = Helmet.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.5F, -0.25F, 0.1309F, 0.0F, 0.0F));

		PartDefinition cube_r4 = bone3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(32, 7).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition Chestplate = partdefinition.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 58).addBox(-1.5F, 11.9F, -2.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(32, 0).addBox(-4.5F, 6.9F, -2.5F, 9.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = Chestplate.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(72, 51).addBox(-2.0F, -2.0F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9F, 10.8F, 1.8F, 0.0984F, 0.1228F, 0.0876F));

		PartDefinition cube_r6 = Chestplate.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(60, 0).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9F, 10.8F, 1.8F, 0.0984F, -0.1228F, -0.0876F));

		PartDefinition cube_r7 = Chestplate.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(74, 7).addBox(0.0004F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9062F, 8.4861F, 1.0F, -0.3927F, 0.0F, -0.48F));

		PartDefinition cube_r8 = Chestplate.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(68, 63).addBox(0.0004F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.9062F, 5.9861F, 1.0F, -0.3927F, 0.0F, -0.48F));

		PartDefinition cube_r9 = Chestplate.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(26, 68).addBox(-2.9996F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8938F, 5.9861F, 1.0F, -0.3927F, 0.0F, 0.48F));

		PartDefinition cube_r10 = Chestplate.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(74, 0).addBox(-1.9996F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8938F, 8.4861F, 1.0F, -0.3927F, 0.0F, 0.48F));

		PartDefinition cube_r11 = Chestplate.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(12, 68).addBox(0.0F, -5.0F, -2.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 4.9F, -1.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition cube_r12 = Chestplate.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(38, 64).addBox(-5.0F, -5.0F, -2.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 4.9F, -1.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition Right Arm = Chestplate.addOrReplaceChild("Right Arm", CubeListBuilder.create().texOffs(24, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition cube_r13 = Right Arm.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(38, 56).addBox(-4.0F, -1.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, 1.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

		PartDefinition cube_r14 = Right Arm.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 50).addBox(-6.0F, -1.0F, -2.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition Left Arm = Chestplate.addOrReplaceChild("Left Arm", CubeListBuilder.create().texOffs(40, 40).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition cube_r15 = Left Arm.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(56, 7).addBox(-1.0F, -1.0F, -2.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.5F, 1.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

		PartDefinition cube_r16 = Left Arm.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, -1.0F, -2.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition Leggings = partdefinition.addOrReplaceChild("Leggings", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition Right Legging = Leggings.addOrReplaceChild("Right Legging", CubeListBuilder.create().texOffs(22, 56).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r17 = Right Legging.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(38, 73).addBox(0.0004F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0062F, 9.4861F, 1.0F, -0.3927F, 0.0F, -0.48F));

		PartDefinition cube_r18 = Right Legging.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 58).addBox(0.0004F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0062F, 6.9861F, 1.0F, -0.3927F, 0.0F, -0.48F));

		PartDefinition cube_r19 = Right Legging.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(50, 15).addBox(-6.0F, -1.0F, -2.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-1.4F, 0.5F, 0.0F, 0.0F, 0.0F, -1.309F));

		PartDefinition Left Legging = Leggings.addOrReplaceChild("Left Legging", CubeListBuilder.create().texOffs(56, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.8F, 0.0F, 0.0F));

		PartDefinition cube_r20 = Left Legging.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(50, 22).addBox(-1.0F, -1.0F, -2.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(1.4F, 0.5F, 0.0F, 0.0F, 0.0F, 1.309F));

		PartDefinition cube_r21 = Left Legging.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(46, 73).addBox(-1.9996F, -4.9848F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9938F, 9.4861F, 1.0F, -0.3927F, 0.0F, 0.48F));

		PartDefinition cube_r22 = Left Legging.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(56, 63).addBox(-2.9996F, -7.9848F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9938F, 6.9861F, 1.0F, -0.3927F, 0.0F, 0.48F));

		PartDefinition Boots = partdefinition.addOrReplaceChild("Boots", CubeListBuilder.create(), PartPose.offset(2.0F, 20.0F, 0.0F));

		PartDefinition Left Boot = Boots.addOrReplaceChild("Left Boot", CubeListBuilder.create().texOffs(56, 47).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r23 = Left Boot.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(72, 26).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.15F, -2.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition Right Boot = Boots.addOrReplaceChild("Right Boot", CubeListBuilder.create().texOffs(56, 55).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, 0.0F));

		PartDefinition cube_r24 = Right Boot.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(48, 35).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 0.15F, -2.0F, 0.0F, 0.0F, -0.7854F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Helmet.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Chestplate.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leggings.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Boots.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}