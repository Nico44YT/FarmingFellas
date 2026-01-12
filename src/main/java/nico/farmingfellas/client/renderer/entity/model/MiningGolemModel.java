package nico.farmingfellas.client.renderer.entity.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.mining.MiningFellaEntity;

public class MiningGolemModel<T extends MiningFellaEntity> extends AbstractGolemModel<T> {

    public MiningGolemModel(ModelPart root) {
        super(root, RenderLayer::getEntityTranslucent);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData head = main.addChild("head", ModelPartBuilder.create().uv(0, 13).cuboid(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -10.0F, 0.0F));

        ModelPartData hat = head.addChild("hat", ModelPartBuilder.create().uv(0, 40).cuboid(-4.0F, -4.0F, -3.0F, 8.0F, 1.0F, 6.0F, new Dilation(0.0F))
                .uv(2, 52).cuboid(-2.5F, -3.5F, -2.5F, 5.0F, 3.0F, 1.0F, new Dilation(0.0F))
                .uv(13, 58).cuboid(-3.0F, -6.0F, -2.0F, 6.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 56).cuboid(-0.5F, -6.5F, -2.5F, 1.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(1, 1).cuboid(-1.0F, -6.0F, -3.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData body = main.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -3.0F, 10.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData left_arm = body.addChild("left_arm", ModelPartBuilder.create().uv(8, 32).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -9.0F, 0.0F));

        ModelPartData right_arm = body.addChild("right_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -9.0F, 0.0F));

        ModelPartData chest = body.addChild("chest", ModelPartBuilder.create().uv(0, 20).cuboid(-3.0F, -2.25F, 3.0F, 6.0F, 6.0F, 3.0F, new Dilation(0.0F))
                .uv(0, 29).cuboid(-1.0F, -1.0F, 5.5F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.75F, 0.0F));

        ModelPartData left_leg = main.addChild("left_leg", ModelPartBuilder.create().uv(20, 13).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -3.5F, 0.0F));

        ModelPartData right_leg = main.addChild("right_leg", ModelPartBuilder.create().uv(18, 21).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -3.5F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }
}