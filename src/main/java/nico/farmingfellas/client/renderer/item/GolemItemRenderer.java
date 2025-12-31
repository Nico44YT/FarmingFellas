package nico.farmingfellas.client.renderer.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.common.item.GolemItem;
import org.jetbrains.annotations.Nullable;

public class GolemItemRenderer extends ItemRenderer {

    private final MinecraftClient client;
    private FellaGolemEntity cachedGolem;
    private World cachedWorld;

    public GolemItemRenderer(MinecraftClient client, TextureManager manager, BakedModelManager bakery, ItemColors colors, BuiltinModelItemRenderer builtinModelItemRenderer) {
        super(client, manager, bakery, colors, builtinModelItemRenderer);
        this.client = client;
    }

    @Override
    public void renderItem(@Nullable LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed) {
        renderItem(stack, renderMode, light, overlay, matrices, vertexConsumers, world, seed);
    }

    @Override
    public void renderItem(ItemStack stack, ModelTransformationMode transformMode, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int seed) {
        if (!(stack.getItem() instanceof GolemItem golemItem) || world == null) {
            return;
        }

        var dispatcher = client.getEntityRenderDispatcher();

        cachedWorld = world;
        cachedGolem = golemItem.createGolem(world);
        cachedGolem.setYaw(180.0F);
        cachedGolem.prevYaw = 180.0F;
        cachedGolem.setPitch(0.0F);

        // Apply name only if needed
        if (stack.hasCustomName() && !stack.getName().equals(cachedGolem.getCustomName())) {
            cachedGolem.setCustomName(stack.getName());
        }

        matrices.push();

        float rotation = 1;
        if(transformMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || transformMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND) rotation = -1;

        // Item-scale transform
        if(transformMode == ModelTransformationMode.GUI) matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20));
        matrices.translate(0, -0.4, 0);
        matrices.scale(0.8F, 0.8F, 0.8F);
        if(transformMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || transformMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
            matrices.scale(0.8F, 0.8F, 0.8F);
            matrices.translate(0, 0.5, 0);
        }
        if(transformMode.isFirstPerson()) {
            matrices.translate(0, 0.2, 0.05);
        }
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30 * rotation));

        dispatcher.setRenderShadows(false);

        try {
            dispatcher.render(cachedGolem, 0.0, 0.0, 0.0, 0.0F, client.getTickDelta(), matrices, vertexConsumers, light);
        } finally {
            dispatcher.setRenderShadows(true);
        }

        matrices.pop();
    }
}