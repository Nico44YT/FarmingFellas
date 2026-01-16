package nico.farmingfellas.client.renderer.entity.feature;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import nico.farmingfellas.FarmingFellasMain;
import nico.farmingfellas.client.renderer.ModRenderLayers;
import nico.farmingfellas.client.renderer.entity.model.ZoneMissingModel;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.base.ZoneHolderEntity;
import org.joml.Matrix4f;

public class MissingZoneFeatureRenderer<T extends FellaGolemEntity & ZoneHolderEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private static ZoneMissingModel WARNING_MODEL;

    public MissingZoneFeatureRenderer(FeatureRendererContext<T, M> context, EntityRendererFactory.Context rendererFactoryContext, M model) {
        super(context);

        if(WARNING_MODEL == null) {
            WARNING_MODEL = new ZoneMissingModel(rendererFactoryContext.getPart(ModRenderLayers.NO_ZONE_WARNING_LAYER));
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.hasZoneSet() && entity.getState() != GolemAnimationState.GUI) {
            matrices.push();
            float yaw = MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));

            float time = entity.age + tickDelta;
            float bob = MathHelper.sin(time / 15.0f);

            matrices.translate(
                    0.0,
                    entity.getHeight() / 2.0 - ((bob + 1.0f) / 20f) - 1.75,
                    0.0
            );

            WARNING_MODEL.render(
                    matrices,
                    vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(getTexture(entity))),
                    light,
                    OverlayTexture.DEFAULT_UV,
                    1, 1, 1, 1
            );
            matrices.pop();
        }
    }

    /*
    private static final float[][] vertices = {
            {1, 1, 1}, // 0
            {-1, 1, 1}, // 1
            {-1, -1, 1}, // 2
            {1, -1, 1}, // 3
            {1, 1, -1}, // 4
            {-1, 1, -1}, // 5
            {-1, -1, -1}, // 6
            {1, -1, -1}, // 7
    };

    private static final int[][] quads = {
            // Front (+Z)
            {0, 1, 2, 3},
            // Back (-Z)
            {4, 7, 6, 5},
            // Right (+X)
            {0, 3, 7, 4},
            // Left (-X)
            {1, 5, 6, 2},
            // Top (+Y)
            {0, 4, 5, 1},
            // Bottom (-Y)
            {3, 2, 6, 7},
    };

    private static void drawSolidBox(MatrixStack matrices, VertexConsumer vc, float scale, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();

        for (int[] quad : quads) {
            for (int u : quad) {
                vc.vertex(mat,
                        vertices[u][0] * scale,
                        vertices[u][1] * scale,
                        vertices[u][2] * scale
                ).color(r, g, b, a).next();
            }
        }
    }
     */

    @Override
    protected Identifier getTexture(T entity) {
        return FarmingFellasMain.id("textures/entity/no_zone_warning.png");
    }
}
