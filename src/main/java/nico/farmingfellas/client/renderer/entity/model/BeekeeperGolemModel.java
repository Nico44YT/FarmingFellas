package nico.farmingfellas.client.renderer.entity.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import nico.farmingfellas.common.entity.beekeeper.BeekeeperFellaEntity;

public class BeekeeperGolemModel<T extends BeekeeperFellaEntity> extends AbstractGolemModel<T> {

    public BeekeeperGolemModel(ModelPart root) {
        super(root, RenderLayer::getEntityTranslucent);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData main = modelPartData.addChild("main", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData head = main.addChild("head", ModelPartBuilder.create().uv(0, 13).cuboid(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -10.0F, 0.0F));

        ModelPartData hat = head.addChild("hat", ModelPartBuilder.create().uv(0, 45).cuboid(-3.0F, -5.0F, -2.0F, 6.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(-10, 51).cuboid(-5.0F, -3.0F, -5.0F, 10.0F, 0.0F, 10.0F, new Dilation(0.0F))
                .uv(41, 58).cuboid(-3.5F, -3.0F, -2.5F, 7.1F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(41, 58).cuboid(-3.5F, -3.0F, 2.5F, 7.0F, 3.0F, 0.0F, new Dilation(0.0F))
                .uv(43, 53).cuboid(3.5F, -3.0F, -2.5F, 0.0F, 3.0F, 5.0F, new Dilation(0.0F))
                .uv(43, 53).cuboid(-3.5F, -3.0F, -2.5F, 0.0F, 3.0F, 5.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData body = main.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -3.0F, 10.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData left_arm = body.addChild("left_arm", ModelPartBuilder.create().uv(8, 32).cuboid(0.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(16, 32).cuboid(-0.1F, 0.0F, -1.1F, 2.2F, 6.0F, 2.2F, new Dilation(0.0F)), ModelTransform.pivot(5.0F, -9.0F, 0.0F));

        ModelPartData right_arm = body.addChild("right_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
                .uv(16, 32).cuboid(-2.1F, 0.0F, -1.1F, 2.2F, 6.0F, 2.2F, new Dilation(0.0F)), ModelTransform.pivot(-5.0F, -9.0F, 0.0F));

        ModelPartData chest = body.addChild("chest", ModelPartBuilder.create().uv(32, 3).cuboid(-2.0F, -2.25F, 3.0F, 4.0F, 7.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -7.75F, 0.0F));

        ModelPartData left_leg = main.addChild("left_leg", ModelPartBuilder.create().uv(20, 13).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, -3.5F, 0.0F));

        ModelPartData right_leg = main.addChild("right_leg", ModelPartBuilder.create().uv(18, 21).cuboid(-1.5F, 0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(-2.0F, -3.5F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }
}