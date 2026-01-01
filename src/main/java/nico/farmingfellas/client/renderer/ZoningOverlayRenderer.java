package nico.farmingfellas.client.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
import nico.farmingfellas.FarmingFellasUtil;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.zoning.ZoningMapItem;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ZoningOverlayRenderer<T extends FellaGolemEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public ZoningOverlayRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    public static void renderItemDot(DrawContext drawContext, ItemStack stack, int x, int y) {
        drawContext.fill(x + 3, y + 9, x + 6, y + 14, 10000, ZoningMapItem.getColor(stack));
        drawContext.fill(x + 2, y + 10, x + 7, y + 13, 10000, ZoningMapItem.getColor(stack));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInGui()) return;

        ItemStack mainHandStack = MinecraftClient.getInstance().player.getStackInHand(Hand.MAIN_HAND);
        if (mainHandStack.getItem() instanceof ZoningMapItem) {
            int golemZoneId = entity.getZoneId();
            int planZoneId = mainHandStack.getSubNbt(ZoningMapItem.TAG_ZONE_DATA).getInt(ZoningMapItem.TAG_ZONE_ID);
            if (golemZoneId == planZoneId) {
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
            }
        }
    }

    public static void renderZone(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ItemStack holdingStack = client.player.getStackInHand(Hand.MAIN_HAND);

        HitResult hitResult = client.player.raycast(6, 0.0F, false);
        if (hitResult.getType() != HitResult.Type.BLOCK && !(ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_CORNER_A, holdingStack) && ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_CORNER_B, holdingStack)))
            return;

        if (!holdingStack.isOf(ModItems.BLANK_ZONING_MAP)) return;
        if (!ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_CORNER_A, holdingStack) && !ZoningMapItem.isZonePosSet(ZoningMapItem.TAG_CORNER_B, holdingStack))
            return;

        Vec3d pos1 = ZoningMapItem.getZonePos(ZoningMapItem.TAG_CORNER_A, holdingStack).orElseGet(() -> ((BlockHitResult) hitResult).getBlockPos()).toCenterPos();
        Vec3d pos2 = ZoningMapItem.getZonePos(ZoningMapItem.TAG_CORNER_B, holdingStack).orElseGet(() -> ((BlockHitResult) hitResult).getBlockPos()).toCenterPos();

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

        ZoningMapItem.getChestPositions(holdingStack).forEach(chestPosition -> {
            Box chestBox = new Box(chestPosition, chestPosition).expand(7/16f + 0.01);
            WorldRenderer.drawBox(
                    context.matrixStack(),
                    vc,
                    chestBox.offset(-camPos.x + 0.5, -camPos.y + 7/16f, -camPos.z + 0.5),
                    rgb[0], rgb[1], rgb[2], Math.min(alpha, 1)
            );
        });
    }

    public static void drawPanel(
            MatrixStack matrices,
            VertexConsumer vc,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float nx, float ny, float nz,
            float r, float g, float b, float a
    ) {
        Matrix4f pos = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();

        vc.vertex(pos, x1, y1, z1).color(r, g, b, a).normal(normal, nx, ny, nz).next();
        vc.vertex(pos, x1, y2, z1).color(r, g, b, a).normal(normal, nx, ny, nz).next();
        vc.vertex(pos, x2, y2, z2).color(r, g, b, a).normal(normal, nx, ny, nz).next();
        vc.vertex(pos, x2, y1, z2).color(r, g, b, a).normal(normal, nx, ny, nz).next();
    }

    public static void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha, red, green, blue);
    }

    /**
     * Draws a box spanning from [x1,y1,z1] to [x2,y2,z2].
     * The 3 axes centered at [x1,y1,z1] may be colored differently using xAxisRed, yAxisGreen, and zAxisBlue.
     *
     * <p>Note the coordinates the box spans are relative to current translation of the matrices.
     */
    public static void drawBox(MatrixStack matrices, VertexConsumer vc, double _x1, double _y1, double _z1, double _x2, double _y2, double _z2, float r, float g, float b, float a, float xAxisRed, float yAxisGreen, float zAxisBlue) {
        Matrix4f pos = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();

        float x1 = (float) _x1;
        float y1 = (float) _y1;
        float z1 = (float) _z1;
        float x2 = (float) _x2;
        float y2 = (float) _y2;
        float z2 = (float) _z2;

        // Facing +Z
        drawPanel(matrices, vc,
                x1, y1, z1,
                x2, y2, z2,
                0, 0, 1,
                r, g, b, a);

        // Facing -Z
        drawPanel(matrices, vc,
                x2, y1, z1,
                x1, y2, z2,
                0, 0, -1,
                r, g, b, a);

    }
}
