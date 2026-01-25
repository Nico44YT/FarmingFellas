package nico.farmingfellas.client.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
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
import nico.farmingfellas.common.block.fertilizer_holder.FertilizerHolderBlock;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.custom.ZoneItem;
import org.joml.Matrix4f;

public class ZoneAreaRenderer {
    public static final int MAXIMUM_SIZE = 16 * 16 * 16;
    public static final float[] ZONE_TOO_LARGE = FarmingFellasUtil.intToRgbFloat(0xDD_11_11);
    public static final float[] DEFAULT_ZONE_OUTLINE = FarmingFellasUtil.intToRgbFloat(0xFF_00_FF);
    public static final float[] DEFAULT_ZONE_SIDES = new float[]{1, 1, 1};
    public static final float[] CHEST_HIGHLIGHT_OUTLINE = FarmingFellasUtil.intToRgbFloat(0xDD_33_33);
    public static final float[] FERTILIZER_HOLDER_HIGHLIGHT_OUTLINE = FarmingFellasUtil.intToRgbFloat(0x11_DD_11);

    private static Vec3d smoothA = null;
    private static Vec3d smoothB = null;

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
        Vec3d targetA = ZoneItem.getCornerA(holdingStack).orElseGet(() -> pos).toCenterPos();
        Vec3d targetB = ZoneItem.getCornerB(holdingStack).orElseGet(() -> pos).toCenterPos();

        if (smoothA == null || smoothB == null) {
            smoothA = targetA;
            smoothB = targetB;
        }

        float tickDelta = context.tickDelta();
        float speed = 0.025f;

        smoothA = lerp(smoothA, targetA, speed * tickDelta * 20f);
        smoothB = lerp(smoothB, targetB, speed * tickDelta * 20f);

        Box box = new Box(smoothA, smoothB).expand(0.5f);
        final Box sizeBox = new Box(targetA, targetB);

        float[] color = DEFAULT_ZONE_OUTLINE;
        float[] sidesColor = DEFAULT_ZONE_SIDES;
        double volume = sizeBox.getXLength() * sizeBox.getYLength() * sizeBox.getZLength();
        if (volume >= MAXIMUM_SIZE) {
            color = ZONE_TOO_LARGE;
            sidesColor = ZONE_TOO_LARGE;
        }

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        float alpha = 0.5f + (float) ((Math.sin(client.player.age / 8f) + 1.0) / 2.0);

        WorldRenderer.drawBox(matrix, context.consumers().getBuffer(ModRenderLayers.ZONE_OVERLAY_LINES_LAYER), box.offset(-camPos.x, -camPos.y, -camPos.z), color[0], color[1], color[2], Math.min(alpha, 1));

        //highlightBlocks(holdingStack, pos, matrix, context);

        ZoneItem.getImportantBlocks(holdingStack).forEach(chestPos -> {
            assert MinecraftClient.getInstance().world != null;
            final var blockState = MinecraftClient.getInstance().world.getBlockState(chestPos);
            final Box blockBox = new Box(chestPos);

            float[] _color = null;
            if (blockState.getBlock() instanceof ChestBlock) _color = CHEST_HIGHLIGHT_OUTLINE;
            else if (blockState.getBlock() instanceof FertilizerHolderBlock) _color = FERTILIZER_HOLDER_HIGHLIGHT_OUTLINE;

            if (_color != null) WorldRenderer.drawBox(matrix, context.consumers().getBuffer(ModRenderLayers.ZONE_OVERLAY_LINES_LAYER), blockBox.offset(-camPos.x, -camPos.y, -camPos.z), _color[0], _color[1], _color[2], Math.min(alpha, 1));
        });

        Vec3d centerPos = new Vec3d(
                (smoothA.getX() + smoothB.getX()) / 2d,
                (smoothA.getY() + smoothB.getY()) / 2d,
                (smoothA.getZ() + smoothB.getZ()) / 2d
        );

        drawSolidBox(matrix,
                centerPos.subtract(camPos),
                context.consumers().getBuffer(ModRenderLayers.ZONE_OVERLAY_LAYER),
                ((float) (Math.abs(smoothB.getX() - smoothA.getX())) * 0.5f) + 0.5f,
                ((float) (Math.abs(smoothB.getY() - smoothA.getY())) * 0.5f) + 0.5f,
                ((float) (Math.abs(smoothB.getZ() - smoothA.getZ())) * 0.5f) + 0.5f,
                sidesColor[0], sidesColor[1], sidesColor[2], 0.15f
        );

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
        drawSolidBox(matrices, pos, vc, scale, scale, scale, r, g, b, a);
    }

    private static void drawSolidBox(MatrixStack matrices, Vec3d pos, VertexConsumer vc, float scaleX, float scaleY, float scaleZ, float r, float g, float b, float a) {

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();

        for (int[] quad : quads) {
            for (int u : quad) {
                vc.vertex(mat,
                                (float) (vertices[u][0] * scaleX + pos.x),
                                (float) (vertices[u][1] * scaleY + pos.y),
                                (float) (vertices[u][2] * scaleZ + pos.z)
                        )
                        .color(r, g, b, a)
                        .normal(0, 1, 0)
                        .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .next();
            }
        }
    }

    private static Vec3d lerp(Vec3d from, Vec3d to, float t) {
        return from.add(to.subtract(from).multiply(t));
    }
}
