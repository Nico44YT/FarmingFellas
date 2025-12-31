package nico.farmingfellas.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nico.farmingfellas.client.FarmingFellasClient;
import nico.farmingfellas.common.item.GolemItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V", at = @At("HEAD"), cancellable = true)
    public void farming_fellas$renderItem(ItemStack stack, ModelTransformationMode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int seed, CallbackInfo ci) {
        if (stack.getItem() instanceof GolemItem) {
            FarmingFellasClient.golemItemRenderer.renderItem(stack, transformationType, light, overlay, matrices, vertexConsumers, world, seed);
            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void farming_fellas$renderItem(ItemStack stack, ModelTransformationMode transformationType, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (stack.getItem() instanceof GolemItem) {
            FarmingFellasClient.golemItemRenderer.renderItem(stack, transformationType, light, overlay, matrices, vertexConsumers, client.world, 0);
            ci.cancel();
        }
    }
}
