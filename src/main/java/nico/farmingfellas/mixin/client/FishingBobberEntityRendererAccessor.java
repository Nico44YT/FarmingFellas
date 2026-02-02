package nico.farmingfellas.mixin.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FishingBobberEntityRenderer.class)
public interface FishingBobberEntityRendererAccessor {
    @Accessor("field_33632")
    static double field_33632() {
        return 0d;
    }

    @Accessor("TEXTURE")
    static Identifier TEXTURE() {
        return null;
    }

    @Accessor("LAYER")
    static RenderLayer LAYER() {
        return null;
    }

    @Invoker("percentage")
    static float percentage(int value, int max) {
        return 0;
    }

    @Invoker("vertex")
    static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {

    }

    @Invoker("renderFishingLine")
    static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {

    }
}
