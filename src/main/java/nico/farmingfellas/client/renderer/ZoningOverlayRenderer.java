package nico.farmingfellas.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nico.farmingfellas.FarmingFellasUtil;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.zoning.ZoningMapItem;

public class ZoningOverlayRenderer<T extends FellaGolemEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ZoningOverlayRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    public static void renderItemDot(DrawContext drawContext, ItemStack stack, int x, int y) {
        drawContext.fill(x + 3, y + 9, x + 6, y + 14, 10000, ZoningMapItem.getColor(stack) | 0xFF000000);
        drawContext.fill(x + 2, y + 10, x + 7, y + 13, 10000, ZoningMapItem.getColor(stack) | 0xFF000000);
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

                float[] rgb = FarmingFellasUtil.intToRgbFloat(ZoningMapItem.getColor(mainHandStack));
                getContextModel().render(
                        matrices,
                        consumer,
                        light,
                        OverlayTexture.DEFAULT_UV,
                        rgb[0], rgb[1], rgb[2], 1f
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

        float[] rgb = FarmingFellasUtil.intToRgbFloat(ZoningMapItem.getColor(holdingStack));

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        float alpha = 0.5f + (float) ((Math.sin(client.player.age / 8f) + 1.0) / 2.0);

        WorldRenderer.drawBox(
                context.matrixStack(),
                vc,
                box.offset(-camPos.x, -camPos.y, -camPos.z),
                rgb[0], rgb[1], rgb[2], Math.min(alpha, 1)
        );
    }
}
