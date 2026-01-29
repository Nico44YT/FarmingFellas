package nico.farmingfellas.client.renderer.entity.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;

import java.util.function.Function;

public abstract class AbstractGolemModel<T extends FellaGolemEntity> extends EntityModel<T> implements ModelWithHead, ModelWithArms {
    protected final ModelPart main;
    protected final ModelPart head;
    protected final ModelPart hat;
    protected final ModelPart body;
    protected final ModelPart left_arm;
    protected final ModelPart right_arm;
    protected final ModelPart chest;
    protected final ModelPart left_leg;
    protected final ModelPart right_leg;

    public AbstractGolemModel(ModelPart root) {
        this(root, RenderLayer::getEntityTranslucentCull);
    }

    public AbstractGolemModel(ModelPart root, Function<Identifier, RenderLayer> renderLayerFunction) {
        super(renderLayerFunction);
        this.main = root.getChild("main");
        this.head = main.getChild("head");
        this.hat = head.getChild("hat");
        this.body = main.getChild("body");
        this.left_arm = body.getChild("left_arm");
        this.right_arm = body.getChild("right_arm");
        this.chest = body.getChild("chest");
        this.left_leg = main.getChild("left_leg");
        this.right_leg = main.getChild("right_leg");
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
        this.head.resetTransform();
        this.body.resetTransform();
        this.right_arm.resetTransform();
        this.left_arm.resetTransform();
        this.right_leg.resetTransform();
        this.left_leg.resetTransform();

        GolemAnimationState state = entity.getState();
        // Head rotation
        this.head.pitch = headPitch * ((float) Math.PI / 180F);
        this.head.yaw = headYaw * ((float) Math.PI / 180F);

        // Walking animation
        float walkSpeed = 1.0F;
        float walkDegree = 1.0F;

        this.right_leg.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;
        this.left_leg.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;

        this.right_arm.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;
        this.left_arm.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;

        this.right_arm.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;
        this.left_arm.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;

        this.right_arm.roll = (MathHelper.cos(entity.age / 15f) + 1f) / 16f;
        this.left_arm.roll = -(MathHelper.cos(entity.age / 15f) + 1f) / 16f;

        switch (state) {
            case GUI -> {
                this.head.pitch = 0;
                this.head.yaw = 0;
            }
            case BEGGING_COOKIE -> {
                this.right_arm.pitch = -150 * ((float) Math.PI / 180F);
                this.left_arm.pitch = -150 * ((float) Math.PI / 180F);
            }
            case EATING_COOKIE -> {
                this.right_arm.pitch = (-45f + (float) Math.sin(entity.age) * 15) * ((float) Math.PI / 180F);
                this.right_arm.yaw = -22.5f * ((float) Math.PI / 180F);
                this.left_arm.pitch = 0;
                this.left_arm.yaw = 0;
            }
            case WORKING -> {
                float time = entity.age * 1.15f; // slower, smoother loop

                float workSwing = MathHelper.sin(time);
                float counterSwing = MathHelper.sin(time + (float) Math.PI);

                // Pitch: alternating work motion
                this.right_arm.pitch = (float) Math.toRadians(-35f + workSwing * 30f);

                // Yaw: slight inward/outward motion
                this.right_arm.yaw = (float) Math.toRadians(10f + workSwing * 10f);

                // Roll: subtle twist adds life
                this.right_arm.roll = (float) Math.toRadians(-workSwing * 8f);
            }
            case IDLE -> {
                if(entity.getVelocity().squaredDistanceTo(Vec3d.ZERO) > 0) break;
                this.right_leg.pitch = (float)Math.toRadians(-90);
                this.left_leg.pitch = (float)Math.toRadians(-90);

                this.right_leg.yaw = (float)Math.toRadians(12.5);
                this.left_leg.yaw = (float)Math.toRadians(-12.5);

                this.right_leg.pivotX -= 0.5f;
                this.left_leg.pivotX += 0.5f;

                this.right_leg.pivotZ -= 1.75f;
                this.left_leg.pivotZ -= 1.75f;

                this.right_leg.pivotY += 2f;
                this.left_leg.pivotY += 2f;
                this.body.pivotY += 3f;
                this.head.pivotY += 3f;

                this.right_arm.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;
                this.left_arm.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;

                this.right_arm.pitch = MathHelper.cos(limbAngle * walkSpeed) * walkDegree * limbDistance;
                this.left_arm.pitch = MathHelper.cos(limbAngle * walkSpeed + (float) Math.PI) * walkDegree * limbDistance;

                this.right_arm.roll = (MathHelper.cos(entity.age / 15f) + 1f) / 16f;
                this.left_arm.roll = -(MathHelper.cos(entity.age / 15f) + 1f) / 16f;
            }
            default -> {

            }
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
