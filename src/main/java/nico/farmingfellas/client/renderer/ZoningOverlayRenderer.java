package nico.farmingfellas.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.zoning.ZoningMapItem;

public class ZoningOverlayRenderer<T extends FellaGolemEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ZoningOverlayRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        ItemStack mainHandStack = MinecraftClient.getInstance().player.getStackInHand(Hand.MAIN_HAND);
        if(mainHandStack.getItem() instanceof ZoningMapItem) {
            entity.getZone().ifPresent(pair -> {
                if(!ZoningMapItem.isSameZone(mainHandStack, pair.getLeft(), pair.getRight())) return;

                VertexConsumer consumer = vertexConsumers.getBuffer(
                        RenderLayer.getEntityTranslucent(getTexture(entity))
                );

                // RGBA overlay color
                float r = 0.0f;
                float g = 1.0f;
                float b = 0.0f;
                float a = 1.0f;

                getContextModel().render(
                        matrices,
                        consumer,
                        light,
                        OverlayTexture.DEFAULT_UV,
                        r, g, b, a
                );
            });
        }
    }

    public static void renderZone(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ItemStack holdingStack = client.player.getStackInHand(Hand.MAIN_HAND);

        HitResult hitResult = client.player.raycast(6, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK && !(ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_BLOCKPOS_1, holdingStack) && ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_BLOCKPOS_2, holdingStack)))
            return;

        if (!holdingStack.isOf(ModItems.BLANK_ZONING_MAP)) return;
        if (!ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_BLOCKPOS_1, holdingStack) && !ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_BLOCKPOS_2, holdingStack))
            return;

        Vec3d pos1 = ZoningMapItem.getZonePos(ZoningMapItem.TAG_BLOCKPOS_1, holdingStack).orElseGet(() -> ((BlockHitResult) hitResult).getBlockPos()).toCenterPos();
        Vec3d pos2 = ZoningMapItem.getZonePos(ZoningMapItem.TAG_BLOCKPOS_2, holdingStack).orElseGet(() -> ((BlockHitResult) hitResult).getBlockPos()).toCenterPos();

        Box box = new Box(pos1, pos2).expand(0.51f);

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        float alpha = 0.5f + (float) ((Math.sin(client.player.age / 8f) + 1.0) / 2.0);

        WorldRenderer.drawBox(
                context.matrixStack(),
                vc,
                box.offset(-camPos.x, -camPos.y, -camPos.z),
                0f, 1f, 0f, Math.min(alpha, 1)
        );
    }
}
