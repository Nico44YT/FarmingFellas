package nico.farmingfellas.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import nico.farmingfellas.client.renderer.ModRenderLayers;
import nico.farmingfellas.client.renderer.entity.FellaGolemRenderer;
import nico.farmingfellas.client.renderer.entity.FishingFellaBobberRenderer;
import nico.farmingfellas.client.renderer.entity.model.*;
import nico.farmingfellas.client.renderer.world.ZoneAreaRenderer;
import nico.farmingfellas.common.block.ModBlocks;
import nico.farmingfellas.common.entity.ModEntities;
import nico.farmingfellas.common.entity.base.FellaGolemEntity;
import nico.farmingfellas.screen.ModHandledScreens;
import nico.farmingfellas.screen.custom.Generic3x2ContainerScreen;

import java.util.function.Function;

public class FarmingFellasClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModRenderLayers.register();

        registerEntityRenderer(ModEntities.FARMING_GOLEM, ModRenderLayers.FARMING_GOLEM_LAYER, FarmingGolemModel::new);
        registerEntityRenderer(ModEntities.LUMBERJACK_GOLEM, ModRenderLayers.LUMBERJACK_GOLEM_LAYER, LumberjackGolemModel::new);
        registerEntityRenderer(ModEntities.MINING_GOLEM, ModRenderLayers.MINING_GOLEM_LAYER, MiningGolemModel::new);
        registerEntityRenderer(ModEntities.BEEKEEPER_GOLEM, ModRenderLayers.BEEKEEPER_GOLEM_LAYER, BeekeeperGolemModel::new);
        registerEntityRenderer(ModEntities.FISHING_GOLEM, ModRenderLayers.FISHING_GOLEM_LAYER, FishingGolemModel::new);

        EntityRendererRegistry.register(ModEntities.FISHING_BOBBER, FishingFellaBobberRenderer::new);

        HandledScreens.register(ModHandledScreens.GENERIC_3X2, Generic3x2ContainerScreen::new);

        WorldRenderEvents.LAST.register(ZoneAreaRenderer::renderZone);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.SAPLING_HOLDER);
    }

    private static <T extends FellaGolemEntity> void registerEntityRenderer(EntityType<T> entityType, EntityModelLayer modelLayer, Function<ModelPart, AbstractGolemModel<T>> factory) {
        EntityRendererRegistry.register(entityType, ctx -> new FellaGolemRenderer<>(ctx, factory.apply(ctx.getPart(modelLayer))));
    }
}
