// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.example.mod;
   
public class farming_golem_Converted extends EntityModel<Entity> {
	private final ModelPart main;
	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart body;
	private final ModelPart left_arm;
	private final ModelPart right_arm;
	private final ModelPart chest;
	private final ModelPart left_leg;
	private final ModelPart right_leg;
	public farming_golem_Converted(ModelPart root) {
		this.main = root.getChild("main");
		this.head = root.getChild("head");
		this.hat = root.getChild("hat");
		this.body = root.getChild("body");
		this.left_arm = root.getChild("left_arm");
		this.right_arm = root.getChild("right_arm");
		this.chest = root.getChild("chest");
		this.left_leg = root.getChild("left_leg");
		this.right_leg = root.getChild("right_leg");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData head = main.addChild("head", ModelPartBuilder.create().uv(0, 13).cuboid(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -10.0F, 0.0F));

		ModelPartData hat = head.addChild("hat", ModelPartBuilder.create().uv(0, 45).cuboid(-3.0F, -5.0F, -2.0F, 6.0F, 2.0F, 4.0F, new Dilation(0.0F))
		.uv(-10, 51).cuboid(-5.0F, -3.0F, -5.0F, 10.0F, 0.0F, 10.0F, new Dilation(0.0F))
		.uv(41, 58).cuboid(-3.5F, -3.0F, -2.5F, 7.0F, 3.0F, 0.0F, new Dilation(0.0F))
		.uv(41, 58).cuboid(-3.5F, -3.0F, 2.5F, 7.0F, 3.0F, 0.0F, new Dilation(0.0F))
		.uv(43, 53).cuboid(3.5F, -3.0F, -2.5F, 0.0F, 3.0F, 5.0F, new Dilation(0.0F))
		.uv(43, 53).cuboid(-3.5F, -3.0F, -2.5F, 0.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		ModelPartData body = main.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -3.0F, 10.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		ModelPartData left_arm = body.addChild("left_arm", ModelPartBuilder.create().uv(8, 32).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
		.uv(16, 32).cuboid(-0.1F, 0.0F, -1.1F, 2.2F, 6.0F, 2.2F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -9.0F, 0.0F));

		ModelPartData right_arm = body.addChild("right_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
		.uv(16, 32).cuboid(-2.1F, 0.0F, -1.1F, 2.2F, 6.0F, 2.2F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -9.0F, 0.0F));

		ModelPartData chest = body.addChild("chest", ModelPartBuilder.create().uv(0, 20).cuboid(-3.0F, -2.25F, 3.0F, 6.0F, 6.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 29).cuboid(-1.0F, -1.0F, 5.5F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.75F, 0.0F));

		ModelPartData left_leg = main.addChild("left_leg", ModelPartBuilder.create().uv(20, 13).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -3.5F, 0.0F));

		ModelPartData right_leg = main.addChild("right_leg", ModelPartBuilder.create().uv(18, 21).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -3.5F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}