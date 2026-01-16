package nico.farmingfellas.client.renderer.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.client.renderer.entity.feature.MissingZoneFeatureRenderer;
import nico.farmingfellas.common.entity.FellaVariant;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;

import java.util.function.Function;

public class FellaGolemRenderer<E extends FellaGolemEntity, M extends EntityModel<E> & ModelWithArms> extends MobEntityRenderer<E, M> {
    private static final Function<FellaVariant, Identifier> TEXTURE = variant -> FarmingFellasMain.id("textures/entity/" + variant.asString() + ".png");

    public FellaGolemRenderer(EntityRendererFactory.Context context, M model) {
        super(context, model, 0.4F);
        this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()) {

            @Override
            protected void renderItem(
                    LivingEntity entity,
                    ItemStack stack,
                    ModelTransformationMode mode,
                    Arm arm,
                    MatrixStack matrices,
                    VertexConsumerProvider consumers,
                    int light
            ) {
                matrices.translate(-0.05F, 1.2F, 0.2); // Y up, Z forward/back

                super.renderItem(entity, stack, mode, arm, matrices, consumers, light);
            }
        });

        this.addFeature(new MissingZoneFeatureRenderer<>(this, context, model));
    }

    @Override
    public Identifier getTexture(FellaGolemEntity entity) {
        if (entity.hasCustomName()) {
            Identifier ID = FarmingFellasMain.id("textures/entity/" + entity.getVariant().asString() + "/" + entity.getCustomName().getString().toLowerCase().replace(" ", "%20") + ".png");
            if (!MinecraftClient.getInstance().getResourceManager().getAllResources(ID).isEmpty()) {
                return ID;
            }
        }

        return TEXTURE.apply(entity.getVariant());
    }
}
