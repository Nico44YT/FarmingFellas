package nico.farmingfellas.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nico.farmingfellas.client.renderer.ZoningOverlayRenderer;
import nico.farmingfellas.common.item.zoning.ZoningMapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Inject(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At("TAIL"))
    public void farming_fellas$drawColorForZoning(LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        if (stack.getItem() instanceof ZoningMapItem) {
            ZoningOverlayRenderer.renderItemDot((DrawContext) (Object) this, stack, x, y);
        }
    }
}
