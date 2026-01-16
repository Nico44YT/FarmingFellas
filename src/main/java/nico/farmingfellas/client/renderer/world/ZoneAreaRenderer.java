package nico.farmingfellas.client.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import nico.farmingfellas.FarmingFellasUtil;
import nico.farmingfellas.client.renderer.ModRenderLayers;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.ZoneItem;
import org.joml.Matrix4f;

public class ZoneAreaRenderer {

    public static void renderZone(WorldRenderContext context) {
        MatrixStack matrix = context.matrixStack();
        matrix.push();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ItemStack holdingStack = client.player.getStackInHand(Hand.MAIN_HAND);

        HitResult hitResult = client.player.raycast(6, 0.0F, false);
        BlockPos resultPos = BlockPos.ORIGIN;
        if (!isZoneAreaCreated(holdingStack)) return;

        if (!holdingStack.isOf(ModItems.BLANK_ZONING_MAP)) return;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            resultPos = ((BlockHitResult) hitResult).getBlockPos();
        } else {
            var opt = ZoneItem.getCornerA(holdingStack);
            if (opt.isPresent()) resultPos = opt.get();
        }

        final BlockPos pos = resultPos;
        Vec3d pos1 = ZoneItem.getCornerA(holdingStack).orElseGet(() -> pos).toCenterPos();
        Vec3d pos2 = ZoneItem.getCornerB(holdingStack).orElseGet(() -> pos).toCenterPos();

        Box box = new Box(pos1, pos2).expand(0.51f);

        float[] rgb = FarmingFellasUtil.intToRgbFloat(0xFF00FF);

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        VertexConsumer vc = context.consumers().getBuffer(RenderLayer.getLines());
        float alpha = 0.5f + (float) ((Math.sin(client.player.age / 8f) + 1.0) / 2.0);

        WorldRenderer.drawBox(matrix, vc, box.offset(-camPos.x, -camPos.y, -camPos.z), rgb[0], rgb[1], rgb[2], Math.min(alpha, 1));

        highlightBlocks(holdingStack, pos, matrix, context);

        VertexConsumer debugQuads = context.consumers().getBuffer(ModRenderLayers.ZONE_OVERLAY_LAYER);
        ZoneItem.getChests(holdingStack).forEach(chestPos -> {
            drawSolidBox(
                    matrix,
                    chestPos.toCenterPos().add(-camPos.x, -camPos.y, -camPos.z),
                    debugQuads,
                    0.51f,
                    1,
                    1,
                    1,
                    0.25f
            );
        });

        matrix.pop();
    }

    public static boolean isZoneAreaCreated(ItemStack holdingStack) {
        var a = ZoneItem.getCornerA(holdingStack);
        var b = ZoneItem.getCornerB(holdingStack);
        return a.isPresent() || b.isPresent();
    }

    public static void highlightBlocks(ItemStack holdingStack, BlockPos pos, MatrixStack matrixStack, WorldRenderContext context) {
        BlockPos a = ZoneItem.getCornerA(holdingStack).orElse(pos);
        BlockPos b = ZoneItem.getCornerB(holdingStack).orElse(pos);

        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX());
        int maxY = Math.max(a.getY(), b.getY());
        int maxZ = Math.max(a.getZ(), b.getZ());

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        VertexConsumer vc = context.consumers().getBuffer(ModRenderLayers.ZONE_OVERLAY_LAYER);
        float[] rgb = FarmingFellasUtil.intToRgbFloat(0xFF00FF);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Vec3d offSetPos = new Vec3d((double) x - camPos.x, (double) y - camPos.y, (double) z - camPos.z);
                    if (camPos.distanceTo(offSetPos.add(camPos)) > 10) continue;

                    float distance = (float) camPos.distanceTo(offSetPos.add(camPos));

                    float alpha = MathHelper.clamp((-distance / 7.5f) + 1, 0f, 1f);

                    if (alpha <= Float.MIN_NORMAL) continue;

                    drawSolidBox(
                            matrixStack,
                            offSetPos.add(0.5, 0.5, 0.5),
                            vc,
                            0.126f,
                            rgb[0], rgb[1], rgb[2],
                            alpha * 0.25f
                    );
                }
            }
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

    private static void drawSolidBox(MatrixStack matrices, Vec3d pos, VertexConsumer vc, float scale, float r, float g, float b, float a) {

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();

        for (int[] quad : quads) {
            for (int u : quad) {
                vc.vertex(mat,
                        (float) (vertices[u][0] * scale + pos.x),
                        (float) (vertices[u][1] * scale + pos.y),
                        (float) (vertices[u][2] * scale + pos.z)
                ).color(r, g, b, a).next();
            }
        }
    }
}
