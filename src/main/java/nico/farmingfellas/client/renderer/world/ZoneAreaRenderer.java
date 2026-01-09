package nico.farmingfellas.client.renderer.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nico.farmingfellas.FarmingFellasUtil;
import nico.farmingfellas.common.item.ModItems;
import nico.farmingfellas.common.item.ZoneItem;

public class ZoneAreaRenderer {

    public static void renderZone(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ItemStack holdingStack = client.player.getStackInHand(Hand.MAIN_HAND);

        HitResult hitResult = client.player.raycast(6, 0.0F, false);
        BlockPos resultPos = BlockPos.ORIGIN;
        if (!isZoneAreaCreated(holdingStack)) return;

        if (!holdingStack.isOf(ModItems.BLANK_ZONING_MAP)) return;

        if(hitResult.getType() == HitResult.Type.BLOCK) {
            resultPos = ((BlockHitResult)hitResult).getBlockPos();
        } else {
            var opt = ZoneItem.getCornerA(holdingStack);
            if(opt.isPresent()) resultPos = opt.get();
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

        WorldRenderer.drawBox(
                context.matrixStack(),
                vc,
                box.offset(-camPos.x, -camPos.y, -camPos.z),
                rgb[0], rgb[1], rgb[2], Math.min(alpha, 1)
        );
    }

    public static boolean isZoneAreaCreated(ItemStack holdingStack) {
        var a = ZoneItem.getCornerA(holdingStack);
        var b = ZoneItem.getCornerB(holdingStack);
        return a.isPresent() || b.isPresent();
    }
}
