package nico.farmingfellas.client.renderer.entity.feature;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import nico.farmingfellas.FarmingFellasUtil;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.entity.base.GolemAnimationState;
import nico.farmingfellas.common.entity.base.ZoneHolderEntity;
import org.joml.Matrix4f;

public class MissingZoneFeatureRenderer<T extends FellaGolemEntity & ZoneHolderEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public MissingZoneFeatureRenderer(FeatureRendererContext<T, M> context, EntityRendererFactory.Context rendererFactoryContext) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.hasZoneSet() && entity.getState() != GolemAnimationState.GUI) {
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
            float[] rgb = FarmingFellasUtil.intToRgbFloat(0xFF_00_22);

            matrices.push();
            float yaw = MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));

            float time = entity.age + tickDelta;
            float bob = MathHelper.sin(time / 25.0f);

            matrices.translate(
                    0.0,
                    entity.getHeight() / 2.0 - ((bob + 1.0f) / 10.0f),
                    0.0
            );

            matrices.scale(0.05f, 0.05f, 0.05f);
            matrices.translate(0, -3, 0);
            drawSolidBox(matrices, vc, 1f, rgb[0], rgb[1], rgb[2], 1);

            matrices.translate(0.0, -6, 0.0);
            matrices.scale(1, 3, 1);
            drawSolidBox(matrices, vc, 1f, rgb[0], rgb[1], rgb[2], 1);
            matrices.pop();
        }
    }

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
}
