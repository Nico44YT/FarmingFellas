package nico.farmingfellas.client.renderer.entity.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.farming.FarmingFellaEntity;

public class FarmingGolemModel<T extends FarmingFellaEntity> extends EntityModel<T> implements ModelWithHead, ModelWithArms {
    private final ModelPart main;
    private final ModelPart head;
    private final ModelPart strawhat;
    private final ModelPart body;
    private final ModelPart left_arm;
    public final ModelPart right_arm;
    private final ModelPart chest;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public FarmingGolemModel(ModelPart root) {
        this.main = root.getChild("main");
        this.head = main.getChild("head");
        this.strawhat = head.getChild("strawhat");
        this.body = main.getChild("body");
        this.left_arm = body.getChild("left_arm");
        this.right_arm = body.getChild("right_arm");
        this.chest = body.getChild("chest");
        this.left_leg = main.getChild("left_leg");
        this.right_leg = main.getChild("right_leg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData head = main.addChild("head", ModelPartBuilder.create().uv(0, 13).cuboid(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -10.0F, 0.0F));

        ModelPartData strawhat = head.addChild("strawhat", ModelPartBuilder.create().uv(0, 45).cuboid(-3.0F, -5.0F, -2.0F, 6.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(-10, 51).cuboid(-5.0F, -3.0F, -5.0F, 10.0F, 0.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData body = main.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -3.0F, 10.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData left_arm = body.addChild("left_arm", ModelPartBuilder.create().uv(8, 32).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -9.0F, 0.0F));

        ModelPartData right_arm = body.addChild("right_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -9.0F, 0.0F));

        ModelPartData chest = body.addChild("chest", ModelPartBuilder.create().uv(0, 20).cuboid(-3.0F, -2.25F, 3.0F, 6.0F, 6.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 29).cuboid(-1.0F, -1.0F, 5.5F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.75F, 0.0F));

        ModelPartData left_leg = main.addChild("left_leg", ModelPartBuilder.create().uv(20, 13).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -3.5F, 0.0F));

        ModelPartData right_leg = main.addChild("right_leg", ModelPartBuilder.create().uv(18, 21).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -3.5F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.push();

        matrices.translate(0.0D, 1.5, 0.0D);
        matrices.scale(1.15f, 1.15f, 1.15f);

        head.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        left_leg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        right_leg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

        matrices.pop();
    }


    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        // Head rotation
        this.head.pitch = headPitch * ((float) Math.PI / 180F);
        this.head.yaw = headYaw * ((float) Math.PI / 180F);

        // Walking animation
        float walkSpeed = 1.0F;
        float walkDegree = 1.0F;

        this.right_leg.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;
        this.left_leg.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;

        if (entity.getState() == GolemAnimationState.BEGGING_COOKIE) {
            this.right_arm.pitch = -150 * ((float) Math.PI / 180F);
            this.left_arm.pitch = -150 * ((float) Math.PI / 180F);
            return;
        } else if (entity.getState() == GolemAnimationState.EATING_COOKIE) {
            this.right_arm.pitch = (-45f + (float) Math.sin(entity.age) * 15) * ((float) Math.PI / 180F);
            this.right_arm.yaw = -22.5f * ((float) Math.PI / 180F);
            this.left_arm.pitch = 0;
            this.left_arm.yaw = 0;
            return;
        }

        this.right_arm.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;
        this.left_arm.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;

        this.right_arm.yaw = 0;
        this.left_arm.yaw = 0;

        if(entity.isInGui()) {
            this.head.pitch = 0;
            this.head.yaw = 0;
        }
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        this.getArm(arm).rotate(matrices);
    }

    public ModelPart getArm(Arm arm) {
        return arm == Arm.LEFT ? left_arm : right_arm;
    }
}